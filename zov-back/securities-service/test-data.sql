USE
securities;

-- ============================================================
-- SAMPLE SECURITIES
-- ============================================================

INSERT INTO securities_dict (id, ticker, name, description, type, exchange, sector, lot_size)
VALUES (generateUUIDv4(), 'SBER', 'Сбербанк', 'Крупнейший банк России и Восточной Европы', 'stock', 'MOEX', 'Финансы',
        10),
       (generateUUIDv4(), 'GAZP', 'Газпром', 'Крупнейшая газовая компания России', 'stock', 'MOEX', 'Энергетика', 10),
       (generateUUIDv4(), 'LKOH', 'Лукойл', 'Одна из крупнейших нефтегазовых компаний мира', 'stock', 'MOEX',
        'Энергетика', 1),
       (generateUUIDv4(), 'TCSG', 'Тинькофф', 'Технологичная финансовая компания', 'stock', 'MOEX', 'Финансы', 1),
       (generateUUIDv4(), 'YNDX', 'Яндекс', 'Крупнейшая российская интернет-компания', 'stock', 'MOEX',
        'Информационные технологии', 1),
       (generateUUIDv4(), 'VTBR', 'ВТБ', 'Второй крупнейший банк России', 'stock', 'MOEX', 'Финансы', 100),
       (generateUUIDv4(), 'ROSN', 'Роснефть', 'Крупнейшая российская нефтяная компания', 'stock', 'MOEX', 'Энергетика',
        10),
       (generateUUIDv4(), 'SILV', 'Сильвинит', 'Производитель калийных удобрений', 'stock', 'MOEX',
        'Химическая промышленность', 100),
       (generateUUIDv4(), 'MTSS', 'МТС', 'Крупнейший российский оператор связи', 'stock', 'MOEX', 'Телекоммуникации',
        10),
       (generateUUIDv4(), 'GMKN', 'Норникель', 'Крупнейший производитель палладия и никеля', 'stock', 'MOEX',
        'Металлургия', 1);

-- ============================================================
-- SAMPLE QUOTES — ссылаемся на security_id через подзапрос по ticker
-- ============================================================

INSERT INTO quotes (security_id, timestamp, price, volume)
SELECT (SELECT id FROM securities_dict WHERE ticker = 'SBER' LIMIT 1) as security_id,
    toDateTime64('2024-01-15 10:00:00', 0, 'UTC') + INTERVAL (number * 300) SECOND as timestamp,
    280 + (rand() % 30) + (number / 10) as price,
    (rand() % 10000) + 1000 as volume
FROM numbers(100);

INSERT INTO quotes (security_id, timestamp, price, volume)
SELECT (SELECT id FROM securities_dict WHERE ticker = 'GAZP' LIMIT 1) as security_id,
    toDateTime64('2024-01-15 10:00:00', 0, 'UTC') + INTERVAL (number * 300) SECOND as timestamp,
    150 + (rand() % 20) + (number / 20) as price,
    (rand() % 15000) + 2000 as volume
FROM numbers(100);

INSERT INTO quotes (security_id, timestamp, price, volume)
SELECT (SELECT id FROM securities_dict WHERE ticker = 'LKOH' LIMIT 1) as security_id,
    toDateTime64('2024-01-15 10:00:00', 0, 'UTC') + INTERVAL (number * 300) SECOND as timestamp,
    700 + (rand() % 50) + (number / 15) as price,
    (rand() % 5000) as volume
FROM numbers(100);