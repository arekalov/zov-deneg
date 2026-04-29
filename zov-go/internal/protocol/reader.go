package protocol

import (
    "encoding/binary"
    "fmt"
    "io"
    "net"
)

var le = binary.LittleEndian

func ReadMessage(conn net.Conn) (uint8, []byte, error) {
    var hdr Header
    if err := binary.Read(conn, le, &hdr); err != nil {
        return 0, nil, fmt.Errorf("read header: %w", err)
    }

    if hdr.Magic != Magic {
        return 0, nil, fmt.Errorf("bad magic: 0x%04X", hdr.Magic)
    }
    if hdr.Version != Version {
        return 0, nil, fmt.Errorf("unsupported version: %d", hdr.Version)
    }
    if hdr.PayloadLen > MaxPayload {
        return 0, nil, fmt.Errorf("payload too large: %d", hdr.PayloadLen)
    }

    payload := make([]byte, hdr.PayloadLen)
    if hdr.PayloadLen > 0 {
        if _, err := io.ReadFull(conn, payload); err != nil {
            return 0, nil, fmt.Errorf("read payload: %w", err)
        }
    }

    var xor uint8
    for _, b := range payload {
        xor ^= b
    }
    if xor != hdr.Checksum {
        return 0, nil, fmt.Errorf("checksum mismatch: got 0x%02X want 0x%02X", xor, hdr.Checksum)
    }

    return hdr.MsgType, payload, nil
}

func ParseQuote(data []byte) (Quote, error) {
	if len(data) != 36 {
		return Quote{}, fmt.Errorf("quote: expected 36 bytes, got %d", len(data))
	}

	var q Quote
	q.Ticker = string(data[0:16])
	q.TimestampMs = int64(le.Uint64(data[16:24]))
	q.Price = int64(le.Uint64(data[24:32]))
	q.Volume = le.Uint32(data[32:36])
	return q, nil
}

func ParseOrderBook(data []byte) (OrderBook, error) {
	if len(data) < 36 {
		return OrderBook{}, fmt.Errorf("orderbook: too short (%d bytes)", len(data))
	}

	var ob OrderBook
	ob.Ticker = string(data[0:16])
	ob.TimestampMs = int64(le.Uint64(data[16:24]))
	ob.SnapshotID = le.Uint64(data[24:32])
	askN := int(le.Uint16(data[32:34]))
	bidN := int(le.Uint16(data[34:36]))
	offset := 36

	expected := offset + (askN+bidN)*12
	if len(data) != expected {
		return OrderBook{}, fmt.Errorf("orderbook: size %d != expected %d", len(data), expected)
	}

	ob.Asks = make([]OrderBookLevel, askN)
	for i := range ob.Asks {
		ob.Asks[i].Price = int64(le.Uint64(data[offset : offset+8]))
		ob.Asks[i].Quantity = le.Uint32(data[offset+8 : offset+12])
		offset += 12
	}

	ob.Bids = make([]OrderBookLevel, bidN)
	for i := range ob.Bids {
		ob.Bids[i].Price = int64(le.Uint64(data[offset : offset+8]))
		ob.Bids[i].Quantity = le.Uint32(data[offset+8 : offset+12])
		offset += 12
	}

	return ob, nil
}

func ParseSessionStart(data []byte) (SessionStart, error) {
	if len(data) < 10 {
		return SessionStart{}, fmt.Errorf("session_start: too short")
	}

	var ss SessionStart
	ss.TimestampMs = int64(le.Uint64(data[0:8]))
	count := int(le.Uint16(data[8:10]))

	offset := 10
	ss.Tickers = make([]string, count)
	for i := 0; i < count; i++ {
		if len(data) < offset+16 {
			return SessionStart{}, fmt.Errorf("session_start: ticker %d data missing", i)
		}
		ss.Tickers[i] = string(data[offset : offset+16])
		offset += 16
	}

	if len(data) != offset {
		return SessionStart{}, fmt.Errorf("session_start: size %d != expected %d", len(data), offset)
	}

	return ss, nil
}

func ParseSessionEnd(data []byte) (SessionEnd, error) {
    if len(data) != 8 {
        return SessionEnd{}, fmt.Errorf("session_end: expected 8 bytes, got %d", len(data))
    }
    return SessionEnd{
        TimestampMs: int64(le.Uint64(data[0:8])),
    }, nil
}