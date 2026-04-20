-- ============================================================
-- ClickHouse Schema for Securities Service
-- ============================================================
-- This script creates all necessary tables, views and indexes
-- for the Securities Service API.
-- ============================================================

-- ============================================================
-- 1. СПРАВОЧНИК ЦЕННЫХ БУМАГ
-- ============================================================
-- Меняется редко: добавление новых бумаг, редактирование описания.
-- ReplacingMergeTree позволяет обновлять запись по id.

CREATE TABLE IF NOT EXISTS securities_dict
(
    id               UUID,
    ticker           String,
    name             String,
    description      Nullable(String),
    type             Enum8('stock' = 1, 'bond' = 2, 'etf' = 3),
    exchange         Enum8('MOEX' = 1, 'SPB' = 2),
    sector           Enum8(
                         'Финансы' = 1,
                         'Энергетика' = 2,
                         'Металлургия' = 3,
                         'Телекоммуникации' = 4,
                         'Потребительский сектор' = 5,
                         'Информационные технологии' = 6,
                         'Транспорт' = 7,
                         'Химическая промышленность' = 8,
                         'Строительство' = 9,
                         'Другое' = 10
                     ),
    lot_size         UInt32,
    updated_at       DateTime DEFAULT now()
)
ENGINE = ReplacingMergeTree(updated_at)
ORDER BY id;


-- ============================================================
-- 2. КОТИРОВКИ (ТИКИ / СДЕЛКИ)
-- ============================================================
-- Основная таблица с данными от драйвера/коллектора.
-- Каждая строка — одна цена в момент времени.
-- Используется для:
--   • GET /securities/{id}/price/history  (агрегация по времени)
--   • Вычисление lastPrice, priceChange, priceChangePct

CREATE TABLE IF NOT EXISTS quotes
(
    security_id      UUID,
    timestamp        DateTime64(3, 'UTC'),   -- миллисекунды
    price            Decimal128(8),           -- цена в рублях
    volume           UInt64 DEFAULT 0         -- объём сделки в штуках (если есть)
)
ENGINE = MergeTree()
PARTITION BY (toYYYYMM(timestamp))
ORDER BY (security_id, timestamp)
TTL toDateTime(timestamp) + INTERVAL 2 YEAR;


-- ============================================================
-- 3. СТАКАН ЗАЯВОК (ORDER BOOK SNAPSHOTS)
-- ============================================================
-- Снимки стакана, которые кладёт коллектор.
-- Каждая строка — один ценовой уровень в одном снимке.
-- Для GET /securities/{id}/orderbook берём последний snapshot_id.

CREATE TABLE IF NOT EXISTS order_book
(
    security_id      UUID,
    timestamp        DateTime64(3, 'UTC'),
    snapshot_id      UInt64,                  -- уникальный id снимка
    side             Enum8('bid' = 1, 'ask' = 2),
    price            Decimal128(8),
    quantity         UInt64                    -- количество в штуках
)
ENGINE = ReplacingMergeTree(snapshot_id)
PARTITION BY (toYYYYMMDD(timestamp))
ORDER BY (security_id, side, price)
TTL toDateTime(timestamp) + INTERVAL 1 DAY;  -- стакан устаревает быстро


-- ============================================================
-- ВСПОМОГАТЕЛЬНАЯ ВИТРИНА: последняя цена + изменение за день
-- ============================================================
-- Materialized View, чтобы не считать на лету при каждом
-- GET /securities. Обновляется при каждой вставке в quotes.

CREATE TABLE IF NOT EXISTS securities_latest
(
    security_id      UUID,
    last_price       Decimal128(8),
    last_timestamp   DateTime64(3, 'UTC'),
    day_open_price   Decimal128(8),          -- цена открытия текущего торгового дня
    day_open_date    Date
)
ENGINE = ReplacingMergeTree(last_timestamp)
ORDER BY security_id;


-- ============================================================
-- MV: при каждой вставке в quotes обновляем securities_latest
-- ============================================================
-- Note: This is a simplified version. For production, you may want
-- to properly calculate day_open_price using the first price of the day.

CREATE MATERIALIZED VIEW IF NOT EXISTS securities_latest_mv
TO securities_latest
AS
SELECT
    security_id,
    price                          AS last_price,
    timestamp                      AS last_timestamp,
    price                          AS day_open_price,
    toDate(timestamp, 'UTC')       AS day_open_date
FROM quotes;


-- ============================================================
-- SAMPLE DATA (for testing)
-- ============================================================

-- Insert sample securities
INSERT INTO securities_dict (id, ticker, name, description, type, exchange, sector, lot_size)
VALUES
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SBER', 'Сбербанк', 'Крупнейший банк России и Восточной Европы', 'stock', 'MOEX', 'Финансы', 10),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'GAZP', 'Газпром', 'Крупнейшая газовая компания России', 'stock', 'MOEX', 'Энергетика', 10),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'LKOH', 'Лукойл', 'Одна из крупнейших нефтегазовых компаний мира', 'stock', 'MOEX', 'Энергетика', 1),
    ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'TCSG', 'Тинькофф', 'Технологичная финансовая компания', 'stock', 'MOEX', 'Финансы', 1),
    ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'YNDX', 'Яндекс', 'Крупнейшая российская интернет-компания', 'stock', 'MOEX', 'Информационные технологии', 1);


-- ============================================================
-- USEFUL QUERIES
-- ============================================================

-- Get all securities with latest prices
-- SELECT
--     s.id,
--     s.ticker,
--     s.name,
--     s.type,
--     s.exchange,
--     s.sector,
--     s.lot_size,
--     sl.last_price,
--     sl.last_price - sl.day_open_price AS price_change,
--     round((sl.last_price - sl.day_open_price) / sl.day_open_price * 100, 2) AS price_change_pct
-- FROM securities AS s
-- LEFT JOIN securities_latest AS sl ON s.id = sl.security_id
-- ORDER BY s.ticker;


-- Get price history for a security
-- SELECT
--     toUnixTimestamp(toStartOfInterval(timestamp, INTERVAL 300 SECOND)) AS ts,
--     avg(price) AS price
-- FROM quotes
-- WHERE security_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
--   AND timestamp >= toDateTime64(1731484800, 3, 'UTC')
--   AND timestamp <= toDateTime64(1731571200, 3, 'UTC')
-- GROUP BY ts
-- ORDER BY ts;


-- Get order book for a security
-- WITH latest AS (
--     SELECT max(snapshot_id) AS sid
--     FROM order_book
--     WHERE security_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
-- )
-- SELECT side, price, quantity
-- FROM order_book
-- WHERE security_id = 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'
--   AND snapshot_id = (SELECT sid FROM latest)
-- ORDER BY side, price
-- LIMIT 10 BY side;
