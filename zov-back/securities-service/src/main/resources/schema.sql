-- ============================================================
-- ClickHouse Schema for Securities Service
-- ============================================================

CREATE DATABASE IF NOT EXISTS securities;

USE securities;

-- ============================================================
-- 1. СПРАВОЧНИК ЦЕННЫХ БУМАГ
-- ============================================================

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

CREATE TABLE IF NOT EXISTS quotes
(
    security_id      UUID,
    timestamp        DateTime64(3, 'UTC'),
    price            Decimal128(8),
    volume           UInt64 DEFAULT 0
    )
    ENGINE = MergeTree()
    PARTITION BY (toYYYYMM(timestamp))
    ORDER BY (security_id, timestamp)
    TTL toDateTime(timestamp) + INTERVAL 2 YEAR;


-- ============================================================
-- 3. СТАКАН ЗАЯВОК (ORDER BOOK SNAPSHOTS)
-- ============================================================

CREATE TABLE IF NOT EXISTS order_book
(
    security_id      UUID,
    timestamp        DateTime64(3, 'UTC'),
    snapshot_id      UInt64,
    side             Enum8('bid' = 1, 'ask' = 2),
    price            Decimal128(8),
    quantity         UInt64
    )
    ENGINE = ReplacingMergeTree(snapshot_id)
    PARTITION BY (toYYYYMMDD(timestamp))
    ORDER BY (security_id, side, price)
    TTL toDateTime(timestamp) + INTERVAL 1 DAY;


-- ============================================================
-- ВСПОМОГАТЕЛЬНАЯ ВИТРИНА: последняя цена + изменение за день
-- ============================================================

CREATE TABLE IF NOT EXISTS securities_latest
(
    security_id      UUID,
    last_price       Decimal128(8),
    last_timestamp   DateTime64(3, 'UTC'),
    day_open_price   Decimal128(8),
    day_open_date    Date
    )
    ENGINE = ReplacingMergeTree(last_timestamp)
    ORDER BY security_id;


-- ============================================================
-- MV: при каждой вставке в quotes обновляем securities_latest
-- ============================================================

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