-- Test Data for Securities Service
-- Run this after schema.sql to populate test data

-- ============================================================
-- SAMPLE SECURITIES
-- ============================================================

INSERT INTO securities_dict (id, ticker, name, description, type, exchange, sector, lot_size)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'SBER', 'Сбербанк', 'Крупнейший банк России и Восточной Европы', 'stock', 'MOEX', 'Финансы', 10),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'GAZP', 'Газпром', 'Крупнейшая газовая компания России', 'stock', 'MOEX', 'Энергетика', 10),
    ('c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'LKOH', 'Лукойл', 'Одна из крупнейших нефтегазовых компаний мира', 'stock', 'MOEX', 'Энергетика', 1),
    ('d3eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'TCSG', 'Тинькофф', 'Технологичная финансовая компания', 'stock', 'MOEX', 'Финансы', 1),
    ('e4eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'YNDX', 'Яндекс', 'Крупнейшая российская интернет-компания', 'stock', 'MOEX', 'Информационные технологии', 1),
    ('f5eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'VTBR', 'ВТБ', 'Второй крупнейший банк России', 'stock', 'MOEX', 'Финансы', 100),
    ('a6eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'ROSN', 'Роснефть', 'Крупнейшая российская нефтяная компания', 'stock', 'MOEX', 'Энергетика', 10),
    ('b7eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'SILV', 'Сильвинит', 'Производитель калийных удобрений', 'stock', 'MOEX', 'Химическая промышленность', 100),
    ('c8eebc99-9c0b-4ef8-bb6d-6bb9bd380a99', 'MTSS', 'МТС', 'Крупнейший российский оператор связи', 'stock', 'MOEX', 'Телекоммуникации', 10),
    ('d9eebc99-9c0b-4ef8-bb6d-6bb9bd380aaa', 'GMKN', 'Норникель', 'Крупнейший производитель палладия и никеля', 'stock', 'MOEX', 'Металлургия', 1);

-- ============================================================
-- SAMPLE QUOTES (Last 24 hours)
-- ============================================================

-- Generate quotes for SBER
INSERT INTO quotes (security_id, timestamp, price, volume)
SELECT 
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11' as security_id,
    toDateTime64('2024-01-15 10:00:00', 3, 'UTC') + INTERVAL (number * 300) SECOND as timestamp,
    280 + (rand() % 30) + (number / 10) as price,
    (rand() % 10000) + 1000 as volume
FROM numbers(100);

-- Generate quotes for GAZP
INSERT INTO quotes (security_id, timestamp, price, volume)
SELECT 
    'b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22' as security_id,
    toDateTime64('2024-01-15 10:00:00', 3, 'UTC') + INTERVAL (number * 300) SECOND as timestamp,
    150 + (rand() % 20) + (number / 20) as price,
    (rand() % 15000) + 2000 as volume
FROM numbers(100);

-- Generate quotes for LKOH
INSERT INTO quotes (security_id, timestamp, price, volume)
SELECT 
    'c2eebc99-9c0b-4ef8-bb6d-6bb9bd380a33' as security_id,
    toDateTime64('2024-01-15 10:00:00', 3, 'UTC') + INTERVAL (number * 300) SECOND as timestamp,
    700 + (rand() % 50) + (number / 15) as price,
    (rand() % 5000) + 500 as volume
FROM numbers(100);

-- ============================================================
-- SAMPLE ORDER BOOK
-- ============================================================

-- Order book for SBER
INSERT INTO order_book (security_id, timestamp, snapshot_id, side, price, quantity)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 295.50, 1000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 296.00, 2500),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 296.50, 5000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 297.00, 7500),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 298.00, 10000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 295.00, 1500),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 294.50, 3000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 294.00, 4500),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 293.50, 6000),
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 293.00, 8000);

-- Order book for GAZP
INSERT INTO order_book (security_id, timestamp, snapshot_id, side, price, quantity)
VALUES 
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 165.00, 2000),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 165.50, 4000),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'ask', 166.00, 6000),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 164.50, 2500),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 164.00, 5000),
    ('b1eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', toDateTime64('2024-01-15 14:30:00', 3, 'UTC'), 1, 'bid', 163.50, 7500);

-- ============================================================
-- UPDATE securities_latest with current data
-- ============================================================

-- This is automatically done by the materialized view, but we can manually insert for testing
INSERT INTO securities_latest (security_id, last_price, last_timestamp, day_open_price, day_open_date)
SELECT 
    security_id,
    argMax(price, timestamp) as last_price,
    argMax(timestamp, timestamp) as last_timestamp,
    argMin(price, timestamp) as day_open_price,
    toDate(argMin(timestamp, timestamp)) as day_open_date
FROM quotes
GROUP BY security_id;
