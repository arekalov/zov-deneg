package main

import (
    "context"
    "flag"
    "log"
    "os"
    "os/signal"
    "syscall"

    "github.com/zovdengi/collector/internal/collector"
    "github.com/zovdengi/collector/internal/protocol"
    "github.com/zovdengi/collector/internal/storage"
)

func main() {
    log.SetFlags(log.Ldate | log.Ltime | log.Lmicroseconds | log.Lshortfile)

    // ── Флаги с fallback на env ──────────────────────────────
    chAddr := flag.String("ch-addr", getEnv("CLICKHOUSE_ADDR", "localhost:9000"),
        "ClickHouse address (host:port)")
    chDB := flag.String("ch-db", getEnv("CLICKHOUSE_DB", "securities"),
        "ClickHouse database")
    chUser := flag.String("ch-user", getEnv("CLICKHOUSE_USER", "default"),
        "ClickHouse user")
    chPassword := flag.String("ch-password", getEnv("CLICKHOUSE_PASSWORD", ""),
        "ClickHouse password")
    socketPath := flag.String("socket", getEnv("SOCKET_PATH", protocol.DefaultSocketPath),
        "Unix socket path")

    flag.Parse()

    // ── Storage ──────────────────────────────────────────────
    cfg := storage.Config{
        Addr:     *chAddr,
        Database: *chDB,
        User:     *chUser,
        Password: *chPassword,
    }

    store, err := storage.New(cfg)
    if err != nil {
        log.Fatalf("failed to connect to ClickHouse: %v", err)
    }
    defer store.Close()

    // ── Graceful shutdown ────────────────────────────────────
    ctx, cancel := signal.NotifyContext(context.Background(),
        os.Interrupt, syscall.SIGTERM)
    defer cancel()

    // ── Run ──────────────────────────────────────────────────
    c := collector.New(store)

    log.Printf("[main] socket: %s", *socketPath)
    log.Printf("[main] clickhouse: %s/%s", *chAddr, *chDB)

    if err := c.Run(ctx, *socketPath); err != nil {
        log.Fatalf("collector error: %v", err)
    }

    log.Println("[main] collector stopped")
}

func getEnv(key, fallback string) string {
    if v, ok := os.LookupEnv(key); ok {
        return v
    }
    return fallback
}