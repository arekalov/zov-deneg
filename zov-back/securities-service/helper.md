# Схема данных ClickHouse для Securities Service

## Анализ контракта

Из OpenAPI нужно поддержать три эндпоинта:

1. **`GET /securities`** и **`GET /securities/{id}`** — список бумаг с последней ценой и изменением за день
2. **`GET /securities/{id}/price/history`** — история цен (точки timestamp + price)
3. **`GET /securities/{id}/orderbook`** — текущий стакан заявок

## Схема

```sql
-- ============================================================
-- 1. СПРАВОЧНИК ЦЕННЫХ БУМАГ
-- ============================================================
-- Меняется редко: добавление новых бумаг, редактирование описания.
-- ReplacingMergeTree позволяет обновлять запись по id.

CREATE TABLE securities
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

CREATE TABLE quotes
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

CREATE TABLE order_book
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

CREATE TABLE securities_latest
(
    security_id      UUID,
    last_price       Decimal128(8),
    last_timestamp   DateTime64(3, 'UTC'),
    day_open_price   Decimal128(8),          -- цена открытия текущего торгового дня
    day_open_date    Date
)
ENGINE = ReplacingMergeTree(last_timestamp)
ORDER BY security_id;

-- MV: при каждой вставке в quotes обновляем securities_latest
CREATE MATERIALIZED VIEW securities_latest_mv
TO securities_latest
AS
SELECT
    security_id,
    price                          AS last_price,
    timestamp                      AS last_timestamp,
    -- берём первую цену текущего дня как open
    price                          AS day_open_price,
    toDate(timestamp, 'UTC')       AS day_open_date
FROM quotes;
```

## Какие запросы покрывает каждая таблица

### `GET /securities` — список бумаг с ценами

```sql
SELECT
    s.id,
    s.ticker,
    s.name,
    s.description,
    s.type,
    s.exchange,
    s.sector,
    s.lot_size,
    sl.last_price,
    sl.last_price - sl.day_open_price                           AS price_change,
    round((sl.last_price - sl.day_open_price) / sl.day_open_price * 100, 2)
                                                                 AS price_change_pct
FROM securities AS s
LEFT JOIN securities_latest AS sl ON s.id = sl.security_id
WHERE 1 = 1
    -- AND s.ticker ILIKE '%SBER%' OR s.name ILIKE '%Сбер%'  -- ?q=
    -- AND s.type = 'stock'                                    -- ?type=
    -- AND s.exchange = 'MOEX'                                 -- ?exchange=
    -- AND s.sector = 'Финансы'                                -- ?sector=
ORDER BY s.ticker
LIMIT {pageSize} OFFSET {offset};
```

### `GET /securities/{id}/price/history` — история цен

```sql
SELECT
    toUnixTimestamp(
        toStartOfInterval(timestamp, INTERVAL {step} SECOND)
    )                               AS ts,
    avg(price)                      AS price
FROM quotes
WHERE security_id = {securityId}
  AND timestamp >= toDateTime64({from}, 3, 'UTC')
  AND timestamp <= toDateTime64({to}, 3, 'UTC')
GROUP BY ts
ORDER BY ts;

-- {step} выбирается на бэкенде по длительности (to - from):
--   < 1 дня   → 60      (1 мин)
--   < 7 дней  → 300     (5 мин)
--   < 30 дней → 3600    (1 час)
--   > 30 дней → 86400   (1 день)
```

### `GET /securities/{id}/orderbook` — стакан

```sql
-- Находим последний snapshot
WITH latest AS (
    SELECT max(snapshot_id) AS sid
    FROM order_book
    WHERE security_id = {securityId}
)
SELECT
    side,
    price,
    quantity
FROM order_book
WHERE security_id = {securityId}
  AND snapshot_id = (SELECT sid FROM latest)
ORDER BY
    side,
    CASE WHEN side = 'ask' THEN price END ASC,
    CASE WHEN side = 'bid' THEN price END DESC
LIMIT {depth} BY side;
```