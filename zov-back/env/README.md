# Конфигурация файлов окружения

Эта директория содержит файлы конфигурации окружения для платформы ЗОВ Денег.

## Структура файлов

```
env/
├── postgres.env           # Конфигурация базы данных PostgreSQL
├── clickhouse.env         # Конфигурация базы данных ClickHouse
├── jwt.env               # Конфигурация JWT аутентификации (общий)
├── user-service.env      # Конфигурация user-service
└── securities-service.env # Конфигурация securities-service
```

## Файлы окружения

### postgres.env
Конфигурация базы данных PostgreSQL для User Service.

**Переменные:**
- `POSTGRES_HOST` - Имя хоста БД (внутренний: postgres)
- `POSTGRES_PORT` - Порт БД (по умолчанию: 5432)
- `POSTGRES_DB` - Имя базы данных
- `POSTGRES_USER` - Пользователь БД
- `POSTGRES_PASSWORD` - Пароль БД

### clickhouse.env
Конфигурация базы данных ClickHouse для Securities Service.

**Переменные:**
- `CLICKHOUSE_HOST` - Имя хоста БД (внутренний: clickhouse)
- `CLICKHOUSE_PORT_HTTP` - Порт HTTP интерфейса (по умолчанию: 8123)
- `CLICKHOUSE_PORT_NATIVE` - Порт нативного протокола (по умолчанию: 9000)
- `CLICKHOUSE_DB` - Имя базы данных
- `CLICKHOUSE_USER` - Пользователь БД
- `CLICKHOUSE_PASSWORD` - Пароль БД
- `CLICKHOUSE_POOL_MAX_SIZE` - Максимальный размер пула соединений (по умолчанию: 10)
- `CLICKHOUSE_POOL_MIN_IDLE` - Минимальное количество idle соединений (по умолчанию: 2)
- `CLICKHOUSE_POOL_CONNECTION_TIMEOUT` - Таймаут подключения в мс (по умолчанию: 30000)
- `CLICKHOUSE_POOL_IDLE_TIMEOUT` - Таймаут простоя в мс (по умолчанию: 600000)
- `CLICKHOUSE_POOL_MAX_LIFETIME` - Максимальное время жизни соединения в мс (по умолчанию: 1800000)

### jwt.env
Общая конфигурация JWT для всех сервисов.

**Переменные:**
- `JWT_SECRET` - Секретный ключ для подписи токенов (мин. 32 символа)
- `JWT_ISSUER` - Идентификатор издателя токена
- `JWT_AUDIENCE` - Аудитория токена
- `JWT_ACCESS_TTL_SEC` - Время жизни access токена в секундах (по умолчанию: 900 = 15 мин)
- `JWT_REFRESH_TTL_DAYS` - Время жизни refresh токена в днях (по умолчанию: 30)
- `JWT_REALM` - Область аутентификации

### user-service.env
Конфигурация specific для User Service.

**Переменные:**
- `USER_SERVICE_PORT` - Внешний порт User Service (по умолчанию: 8080)
- `SECURITIES_SERVICE_URL` - Внутренний URL Securities Service

### securities-service.env
Конфигурация specific для Securities Service.

**Переменные:**
- `SECURITIES_SERVICE_PORT` - Внешний порт Securities Service (по умолчанию: 8081)

## Корневой файл .env

Корневой файл `.env` (в корне проекта) содержит маппинг портов для Docker Compose:

**Переменные:**
- `POSTGRES_PORT` - Внешний порт PostgreSQL
- `CLICKHOUSE_PORT_HTTP` - Внешний HTTP порт ClickHouse
- `CLICKHOUSE_PORT_NATIVE` - Внешний нативный порт ClickHouse
- `USER_SERVICE_PORT` - Внешний порт User Service
- `SECURITIES_SERVICE_PORT` - Внешний порт Securities Service

## Использование

### Запуск всех сервисов

```bash
cd /Users/m.s.taranenko/IdeaProjects/itmo/zov-deneg/zov-back
docker-compose up -d
```

### Остановка всех сервисов

```bash
docker-compose down
```

### Просмотр логов

```bash
# Все сервисы
docker-compose logs -f

# Конкретный сервис
docker-compose logs -f user-service
docker-compose logs -f securities-service
```

### Доступ к сервисам

- **User Service:** http://localhost:8080
- **Securities Service:** http://localhost:8081
- **PostgreSQL:** localhost:5432
- **ClickHouse HTTP:** localhost:8123
- **ClickHouse Native:** localhost:9000

## Заметки по безопасности

⚠️ **Важно:** 
- Никогда не коммитьте файлы `.env` с продакшен секретами в контроль версий
- Используйте `.env.example` как шаблон
- Измените пароли по умолчанию перед развёртыванием в продакшене
- Используйте сильные уникальные JWT секреты в продакшене
