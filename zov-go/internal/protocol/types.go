package protocol

const (
    SocketPath = "/tmp/zovdengi/driver.sock"
    Magic      uint16 = 0xDACE
    Version    uint8  = 0x01
    HeaderSize        = 9
    PriceScale int64  = 100_000_000
    MaxPayload uint32 = 1 << 20

    MsgHeartbeat    uint8 = 0x00
    MsgQuote        uint8 = 0x01
    MsgOrderBook    uint8 = 0x02
    MsgSessionStart uint8 = 0x10
    MsgSessionEnd   uint8 = 0x11
)

type Header struct {
    Magic      uint16
    Version    uint8
    MsgType    uint8
    PayloadLen uint32
    Checksum   uint8
}

type Quote struct {
    Ticker      string
    TimestampMs int64
    Price       int64
    Volume      uint32
}

type OrderBookLevel struct {
    Price    int64
    Quantity uint32
}

type OrderBook struct {
    Ticker      string
    TimestampMs int64
    SnapshotID  uint64
    Asks        []OrderBookLevel
    Bids        []OrderBookLevel
}

type SessionStart struct {
    TimestampMs int64
    Tickers     []string
}

type SessionEnd struct {
    TimestampMs int64
}