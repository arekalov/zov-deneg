# Securities Service

Сервис ценных бумаг с использованием ClickHouse для хранения котировок и данных стакана заявок.

## Быстрый старт

### Запуск через Docker Compose

```bash
# Сборка и запуск сервиса
docker-compose up --build

# Запуск в фоновом режиме
docker-compose up -d --build

# Остановка
docker-compose down
```

После запуска:
- API сервиса: http://localhost:8080
- ClickHouse HTTP интерфейс: http://localhost:8123
- ClickHouse native порт: localhost:9000

### Локальная разработка

```bash
# Сборка проекта
./gradlew build

# Запуск
./gradlew run

# Запуск тестов
./gradlew test
```

## Конфигурация

Основные параметры в `.env`:

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `CLICKHOUSE_HOST` | Хост ClickHouse | `clickhouse` |
| `CLICKHOUSE_PORT_HTTP` | HTTP порт ClickHouse | `8123` |
| `CLICKHOUSE_DB` | Имя базы данных | `securities` |
| `CLICKHOUSE_USER` | Пользователь ClickHouse | `default` |
| `CLICKHOUSE_PASSWORD` | Пароль ClickHouse | (пусто) |
| `JWT_SECRET` | Секретный ключ JWT | (требуется) |
| `JWT_ISSUER` | Issuer JWT токенов | `zov-deneg-securities-service` |
| `JWT_AUDIENCE` | Audience JWT токенов | `zov-deneg-securities` |
| `SERVER_PORT` | Порт сервера | `8080` |

## API Endpoints

### Ценные бумаги

- `GET /securities` - Список ценных бумаг с фильтрацией и пагинацией
- `GET /securities/{securityId}` - Детальная информация по бумаге
- `GET /securities/{securityId}/price/history?from={ts}&to={ts}` - История цен
- `GET /securities/{securityId}/orderbook?depth={n}` - Стакан заявок

### Параметры запросов

**GET /securities:**
- `q` - поиск по тику или названию
- `type` - тип бумаги (stock, bond, etf)
- `exchange` - биржа (MOEX, SPB)
- `sector` - сектор экономики
- `page` - номер страницы (default: 1)
- `pageSize` - размер страницы (default: 20, max: 100)

**GET /securities/{id}/price/history:**
- `from` - начало интервала (Unix timestamp в секундах)
- `to` - конец интервала (Unix timestamp в секундах)

**GET /securities/{id}/orderbook:**
- `depth` - глубина стакана (default: 10, max: 50)

## База данных

Схема ClickHouse находится в `src/main/resources/schema.sql` и автоматически применяется при первом запуске контейнера.

Таблицы:
- `securities` - справочник ценных бумаг
- `quotes` - котировки (тики/сделки)
- `order_book` - снимки стакана заявок
- `securities_latest` - материализованное представление с последними ценами

## Структура проекта

```
securities-service/
├── src/main/kotlin/
│   ├── Application.kt          # Точка входа
│   ├── Database.kt             # Конфигурация ClickHouse
│   ├── Security.kt             # JWT аутентификация
│   ├── Routing.kt              # API endpoints
│   ├── model/                  # Data модели
│   └── repository/             # Репозиторий для работы с БД
├── src/main/resources/
│   ├── application.yaml        # Конфигурация приложения
│   └── schema.sql              # Схема БД
├── docker-compose.yml          # Docker Compose конфигурация
├── Dockerfile                  # Docker образ сервиса
└── .env                        # Переменные окружения
```
