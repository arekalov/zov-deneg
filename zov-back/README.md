# ЗОВ Денег — Единая Docker Compose Конфигурация

## Обзор

Эта директория содержит единую Docker Compose конфигурацию для платформы ЗОВ Денег, объединяющую все сервисы и базы данных в одну оркестрированную среду.

## Архитектура

```
┌─────────────────────────────────────────────────────────────┐
│                    zov-deneg-network                        │
│                                                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐  │
│  │   postgres   │  │  clickhouse  │  │  user-service    │  │
│  │   (5432)     │  │  (8123,9000) │  │     (8080)       │  │
│  └──────────────┘  └──────────────┘  └──────────────────┘  │
│         │                  │                  │             │
│         │                  │                  │             │
│         └──────────────────┴──────────────────┘             │
│                             │                               │
│                  ┌──────────────────┐                       │
│                  │ securities-svc   │                       │
│                  │     (8080)       │                       │
│                  └──────────────────┘                       │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
                    Внешние порты
                8080, 8081, 5432, 8123, 9000
```

## Быстрый старт

### 1. Клонирование и настройка

```bash
cd /Users/m.s.taranenko/IdeaProjects/itmo/zov-deneg/zov-back

# Скопировать пример .env при необходимости
cp .env.example .env
```

### 2. Запуск всех сервисов

```bash
# Собрать и запустить все сервисы
docker-compose up -d --build

# Или просто запустить (если уже собраны)
docker-compose up -d
```

### 3. Проверка статуса

```bash
# Просмотр запущенных контейнеров
docker-compose ps

# Просмотр логов
docker-compose logs -f
```

### 4. Остановка сервисов

```bash
# Остановить все сервисы
docker-compose down

# Остановить и удалить тома (⚠️ удаляет данные)
docker-compose down -v
```

## Сервисы

| Сервис | Имя контейнера | Порт | Описание |
|--------|---------------|------|----------|
| **postgres** | zov-postgres | 5432 | PostgreSQL база данных для User Service |
| **clickhouse** | zov-clickhouse | 8123, 9000 | ClickHouse база данных для Securities Service |
| **user-service** | zov-user-service | 8080 | Управление пользователями и аутентификация |
| **securities-service** | zov-securities-service | 8081 | Ценные бумаги и рыночные данные |

## Точки доступа

### Приложения
- **User Service:** http://localhost:8080
- **Securities Service:** http://localhost:8081

### Базы данных
- **PostgreSQL:** localhost:5432
  - База данных: `userservice`
  - Пользователь: `postgres`
  - Пароль: `postgres_secret_password_123`

- **ClickHouse:** 
  - HTTP: localhost:8123
  - Native: localhost:9000
  - База данных: `securities`
  - Пользователь: `default`
  - Пароль: `securities_pass`

## Конфигурация

### Файлы окружения

Конфигурация разделена на функциональные файлы в директории `env/`:

```
env/
├── postgres.env           # Настройки PostgreSQL
├── clickhouse.env         # Настройки ClickHouse
├── jwt.env               # JWT аутентификация (общий)
├── user-service.env      # Настройки user-service
└── securities-service.env # Настройки securities-service
```

### Корневой файл .env

Корневой файл `.env` управляет маппингом внешних портов:

```bash
POSTGRES_PORT=5432
CLICKHOUSE_PORT_HTTP=8123
CLICKHOUSE_PORT_NATIVE=9000
USER_SERVICE_PORT=8080
SECURITIES_SERVICE_PORT=8081
```

## Основные команды

### Просмотр логов
```bash
# Все сервисы
docker-compose logs -f

# Конкретный сервис
docker-compose logs -f user-service
docker-compose logs -f securities-service
docker-compose logs -f postgres
docker-compose logs -f clickhouse
```

### Перезапуск сервисов
```bash
# Перезапустить все
docker-compose restart

# Перезапустить конкретный
docker-compose restart user-service
```

### Пересборка сервисов
```bash
# Пересобрать и перезапустить
docker-compose up -d --build

# Форсированная пересборка без кэша
docker-compose build --no-cache
```

### Доступ к базам данных
```bash
# PostgreSQL
docker exec -it zov-postgres psql -U postgres -d userservice

# ClickHouse
docker exec -it zov-clickhouse clickhouse-client -u default -p securities_pass
```

## Проверки здоровья

Все сервисы настроены с проверками здоровья:

```bash
# Проверка статуса
docker-compose ps

# Проверка конкретного сервиса
docker inspect zov-postgres --format='{{.State.Health.Status}}'
docker inspect zov-clickhouse --format='{{.State.Health.Status}}'
```

## Сеть

Все сервисы подключены к общей bridge сети:
- **Имя сети:** zov-deneg-network
- **Драйвер:** bridge

Сервисы могут общаться используя имена контейнеров:
- `postgres`
- `clickhouse`
- `zov-user-service`
- `zov-securities-service`

## Тома

Постоянные данные хранятся в Docker томах:

| Том | Назначение | Сервис |
|--------|---------|---------|
| `zov-postgres-data` | Данные PostgreSQL | postgres |
| `zov-clickhouse-data` | Данные ClickHouse | clickhouse |

### Резервное копирование томов
```bash
# Резервное копирование PostgreSQL
docker run --rm \
  -v zov-postgres-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/postgres-backup.tar.gz -C /data .

# Резервное копирование ClickHouse
docker run --rm \
  -v zov-clickhouse-data:/data \
  -v $(pwd):/backup \
  alpine tar czf /backup/clickhouse-backup.tar.gz -C /data .
```

## Решение проблем

### Сервисы не запускаются
```bash
# Проверить логи
docker-compose logs

# Проверить занятость портов
lsof -i :8080
lsof -i :8081
lsof -i :5432
```

### Проблемы подключения к БД
```bash
# Проверить здоровье БД
docker-compose ps

# Тест подключения
docker exec zov-postgres pg_isready -U postgres
docker exec zov-clickhouse clickhouse-client --query "SELECT 1"
```

### Чистый перезапуск
```bash
# Остановить и удалить всё
docker-compose down -v

# Собрать заново
docker-compose up -d --build
```

## Заметки по безопасности

⚠️ **Перед продакшеном:**
1. Измените все пароли по умолчанию в `env/*.env`
2. Сгенерируйте сильный JWT секрет (мин. 32 символа)
3. Не коммитьте файлы `.env` в контроль версий
4. Используйте конфигурацию для конкретного окружения
5. Включите SSL/TLS для подключений к БД

## Тестирование API

После запуска сервисов запустите тестовый скрипт:

```bash
./test-apis.sh
```

Это протестирует все эндпоинты обоих сервисов и создаст отчёт.

## OpenAPI Документация

- **User Service:** `user-service/src/main/resources/openapi-user-service.yaml`
- **Securities Service:** `securities-service/src/main/resources/openapi-securities-service.yaml`

Просмотр в Swagger UI (если включено):
- User Service: http://localhost:8080/swagger-ui.html
- Securities Service: http://localhost:8081/swagger-ui.html
