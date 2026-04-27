package storage

import (
	"context"
	"fmt"
	"log"
	"sync"
	"time"

	"github.com/ClickHouse/clickhouse-go/v2"
	"github.com/ClickHouse/clickhouse-go/v2/lib/driver"
	"github.com/google/uuid"
	"example.com/collector/internal/protocol"
)

type Config struct {
	Addr     string // "localhost:9000"
	Database string // "securities"
	User     string
	Password string
}

type Storage struct {
	conn         driver.Conn
	tickerCache  sync.Map // map[ticker]string -> UUID
}

func New(cfg Config) (*Storage, error) {
    conn, err := clickhouse.Open(&clickhouse.Options{
        Addr: []string{cfg.Addr},
        Auth: clickhouse.Auth{
            Database: cfg.Database,
            Username: cfg.User,
            Password: cfg.Password,
        },
        Settings: clickhouse.Settings{
            "max_execution_time": 60,
        },
        DialTimeout:     5 * time.Second,
        ConnMaxLifetime: time.Hour,
    })
    if err != nil {
        return nil, fmt.Errorf("clickhouse open: %w", err)
    }

    if err := conn.Ping(context.Background()); err != nil {
        return nil, fmt.Errorf("clickhouse ping: %w", err)
    }

    log.Println("[storage] connected to ClickHouse")
    return &Storage{conn: conn}, nil
}

func (s *Storage) Close() error {
	return s.conn.Close()
}

// GetUUIDByTicker returns UUID for a ticker, using cache or querying database
func (s *Storage) GetUUIDByTicker(ctx context.Context, ticker string) (uuid.UUID, error) {
	// Check cache first
	if cached, ok := s.tickerCache.Load(ticker); ok {
		return cached.(uuid.UUID), nil
	}

	// Query database
	var id uuid.UUID
	err := s.conn.QueryRow(ctx, "SELECT id FROM securities WHERE ticker = ?", ticker).Scan(&id)
	if err != nil {
		return uuid.Nil, fmt.Errorf("lookup ticker %s: %w", ticker, err)
	}

	// Cache the result
	s.tickerCache.Store(ticker, id)
	return id, nil
}

// ────────────────────────────────────────────────────────────
// Batch writers
// ────────────────────────────────────────────────────────────

func (s *Storage) InsertQuotes(ctx context.Context, quotes []protocol.Quote) error {
	if len(quotes) == 0 {
		return nil
	}

	batch, err := s.conn.PrepareBatch(ctx, "INSERT INTO quotes (security_id, timestamp, price, volume)")
	if err != nil {
		return fmt.Errorf("prepare quotes batch: %w", err)
	}

	for _, q := range quotes {
		secID, err := s.GetUUIDByTicker(ctx, q.Ticker)
		if err != nil {
			log.Printf("[storage] lookup ticker %s error, skipping: %v", q.Ticker, err)
			continue
		}

		ts := time.UnixMilli(q.TimestampMs).UTC()
		priceStr := priceToDecimalString(q.Price)

		if err := batch.Append(secID, ts, priceStr, uint64(q.Volume)); err != nil {
			return fmt.Errorf("append quote: %w", err)
		}
	}

	if err := batch.Send(); err != nil {
		return fmt.Errorf("send quotes batch: %w", err)
	}

	return nil
}

func (s *Storage) InsertOrderBook(ctx context.Context, ob protocol.OrderBook) error {
	batch, err := s.conn.PrepareBatch(ctx,
		"INSERT INTO order_book (security_id, timestamp, snapshot_id, side, price, quantity)")
	if err != nil {
		return fmt.Errorf("prepare orderbook batch: %w", err)
	}

	secID, err := s.GetUUIDByTicker(ctx, ob.Ticker)
	if err != nil {
		return fmt.Errorf("lookup ticker %s: %w", ob.Ticker, err)
	}

	ts := time.UnixMilli(ob.TimestampMs).UTC()

	for _, ask := range ob.Asks {
		priceStr := priceToDecimalString(ask.Price)
		if err := batch.Append(secID, ts, ob.SnapshotID, "ask", priceStr, uint64(ask.Quantity)); err != nil {
			return fmt.Errorf("append ask: %w", err)
		}
	}

	for _, bid := range ob.Bids {
		priceStr := priceToDecimalString(bid.Price)
		if err := batch.Append(secID, ts, ob.SnapshotID, "bid", priceStr, uint64(bid.Quantity)); err != nil {
			return fmt.Errorf("append bid: %w", err)
		}
	}

	if err := batch.Send(); err != nil {
		return fmt.Errorf("send orderbook batch: %w", err)
	}

	return nil
}

// ────────────────────────────────────────────────────────────
// Helpers
// ────────────────────────────────────────────────────────────

// priceToDecimalString: int64 fixed-point (×10^8) → "298.45000000"
func priceToDecimalString(price int64) string {
    sign := ""
    if price < 0 {
        sign = "-"
        price = -price
    }
    whole := price / protocol.PriceScale
    frac := price % protocol.PriceScale
    return fmt.Sprintf("%s%d.%08d", sign, whole, frac)
}