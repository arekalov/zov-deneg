package collector

import (
    "context"
    "log"
    "net"
    "sync"
    "time"

    "github.com/google/uuid"
    "github.com/zovdengi/collector/internal/protocol"
    "github.com/zovdengi/collector/internal/storage"
)

type Collector struct {
    storage *storage.Storage

    // Буфер котировок для батчевой вставки
    mu          sync.Mutex
    quoteBuf    []protocol.Quote
    maxBufSize  int
    flushPeriod time.Duration
}

func New(store *storage.Storage) *Collector {
    return &Collector{
        storage:     store,
        quoteBuf:    make([]protocol.Quote, 0, 1024),
        maxBufSize:  1000,
        flushPeriod: 1 * time.Second,
    }
}

// Run запускает unix socket listener и обработку подключений
func (c *Collector) Run(ctx context.Context, socketPath string) error {
    ln, err := protocol.Listen(socketPath)
    if err != nil {
        return err
    }
    defer ln.Close()
    log.Printf("[collector] listening on %s", socketPath)

    // Фоновый flush буфера котировок по таймеру
    go c.periodicFlush(ctx)

    for {
        conn, err := ln.Accept()
        if err != nil {
            select {
            case <-ctx.Done():
                return nil
            default:
                log.Printf("[collector] accept error: %v", err)
                continue
            }
        }
        log.Printf("[collector] driver connected")
        go c.handleConnection(ctx, conn)
    }
}

func (c *Collector) handleConnection(ctx context.Context, conn net.Conn) {
    defer conn.Close()

    for {
        select {
        case <-ctx.Done():
            return
        default:
        }

        // Таймаут на чтение: heartbeat ожидается каждые 5 сек,
        // если 15 сек тишина — считаем соединение мёртвым
        conn.SetReadDeadline(time.Now().Add(15 * time.Second))

        msgType, payload, err := protocol.ReadMessage(conn)
        if err != nil {
            log.Printf("[collector] read error: %v", err)
            return
        }

        switch msgType {
        case protocol.MsgHeartbeat:
            // ничего не делаем, просто сбросили deadline

        case protocol.MsgQuote:
            q, err := protocol.ParseQuote(payload)
            if err != nil {
                log.Printf("[collector] parse quote error: %v", err)
                continue
            }
            c.bufferQuote(q)

        case protocol.MsgOrderBook:
            ob, err := protocol.ParseOrderBook(payload)
            if err != nil {
                log.Printf("[collector] parse orderbook error: %v", err)
                continue
            }
            // Стакан вставляем сразу — приходит реже, нужен актуальный
            if err := c.storage.InsertOrderBook(ctx, ob); err != nil {
                log.Printf("[collector] insert orderbook error: %v", err)
            }

        case protocol.MsgSessionStart:
            ss, err := protocol.ParseSessionStart(payload)
            if err != nil {
                log.Printf("[collector] parse session_start error: %v", err)
                continue
            }
            ids := make([]string, len(ss.SecurityIDs))
            for i, raw := range ss.SecurityIDs {
                u, _ := uuid.FromBytes(raw[:])
                ids[i] = u.String()
            }
            log.Printf("[collector] session started, %d securities: %v",
                len(ss.SecurityIDs), ids)

        case protocol.MsgSessionEnd:
            se, err := protocol.ParseSessionEnd(payload)
            if err != nil {
                log.Printf("[collector] parse session_end error: %v", err)
            } else {
                log.Printf("[collector] session ended at %d", se.TimestampMs)
            }
            // Финальный flush перед отключением
            c.flush(ctx)
            return

        default:
            log.Printf("[collector] unknown msg_type: 0x%02X", msgType)
        }
    }
}

// ────────────────────────────────────────────────────────────
// Buffered quote insert
// ────────────────────────────────────────────────────────────

func (c *Collector) bufferQuote(q protocol.Quote) {
    c.mu.Lock()
    c.quoteBuf = append(c.quoteBuf, q)
    shouldFlush := len(c.quoteBuf) >= c.maxBufSize
    c.mu.Unlock()

    if shouldFlush {
        c.flush(context.Background())
    }
}

func (c *Collector) flush(ctx context.Context) {
    c.mu.Lock()
    if len(c.quoteBuf) == 0 {
        c.mu.Unlock()
        return
    }
    buf := c.quoteBuf
    c.quoteBuf = make([]protocol.Quote, 0, 1024)
    c.mu.Unlock()

    if err := c.storage.InsertQuotes(ctx, buf); err != nil {
        log.Printf("[collector] flush quotes error (%d items): %v", len(buf), err)
        return
    }
    log.Printf("[collector] flushed %d quotes", len(buf))
}

func (c *Collector) periodicFlush(ctx context.Context) {
    ticker := time.NewTicker(c.flushPeriod)
    defer ticker.Stop()

    for {
        select {
        case <-ctx.Done():
            c.flush(ctx)
            return
        case <-ticker.C:
            c.flush(ctx)
        }
    }
}