# User Service - Сервис пользователей

Сервис управления пользователями для платформы "Зов Денег" с аутентификацией через JWT и базой данных PostgreSQL.

## 📋 Содержание

- [Обзор](#обзор)
- [Технологии](#технологии)
- [Быстрый старт](#быстрый-старт)
- [Команды Makefile](#команды-makefile)
- [API Endpoints](#api-endpoints)
- [Конфигурация](#конфигурация)
- [Docker](#docker)
- [Тестирование](#тестирование)
- [База данных](#база-данных)
- [Структура проекта](#структура-проекта)

---

## 📖 Обзор

User Service предоставляет следующие возможности:

- **Регистрация и аутентификация** пользователей
- **JWT токены**: Access (15 мин), Refresh (30 дней)
- **Управление профилем**: просмотр и редактирование
- **Ролевая модель**: обычный пользователь и администратор
- **Администрирование**: управление всеми пользователями

---

## 🛠 Технологии

| Компонент | Технология |
|-----------|------------|
| **Язык** | Kotlin 2.3.0 |
| **Фреймворк** | Ktor 3.4.2 |
| **ORM** | Exposed 0.57.0 |
| **База данных** | PostgreSQL 15 |
| **Аутентификация** | JWT (java-jwt 4.5.0) |
| **Хеширование** | BCrypt (cost 12) |
| **Сборка** | Gradle 8.5 |
| **Контейнеризация** | Docker + Docker Compose |

---

## 🚀 Быстрый старт

### 1. Клонирование и настройка

```bash
cd user-service
```

### 2. Настройка переменных окружения

Отредактируйте файл `.env`:

```bash
# База данных
DB_HOST=postgres
DB_PORT=5432
DB_NAME=userservice
DB_USER=postgres
DB_PASSWORD=ваш_пароль

# JWT
JWT_SECRET=ваш-секретный-ключ-мин-32-символа
JWT_ISSUER=zov-deneg-user-service
JWT_AUDIENCE=zov-deneg-users
JWT_ACCESS_TTL_MIN=15
JWT_REFRESH_TTL_DAYS=30

# Сервер
SERVER_PORT=8080
```

### 3. Запуск через Docker

```bash
# Сборка и запуск
make docker-build
make docker-run

# Проверка статуса
make status

# Запуск тестов
make test

# Остановка
make docker-stop
```

### 4. Локальный запуск (без Docker)

```bash
# Сборка
make build

# Запуск
make run

# Тесты
./test-api.sh
```

---

## 📜 Команды Makefile

### Разработка

| Команда | Описание |
|---------|----------|
| `make build` | Собрать приложение через Gradle |
| `make run` | Запустить приложение локально |
| `make clean` | Очистить артефакты сборки |
| `make help` | Показать все команды |

### Docker

| Команда | Описание |
|---------|----------|
| `make docker-build` | Создать Docker образ |
| `make docker-run` | Запустить контейнеры (PostgreSQL + App) |
| `make docker-stop` | Остановить контейнеры |
| `make docker-restart` | Перезапустить контейнеры |
| `make docker-clean` | Удалить контейнеры, тома и образы |
| `make logs` | Просмотр логов (режим реального времени) |
| `make logs-tail` | Просмотр последних 100 строк логов |
| `make status` | Показать статус контейнеров |

### Тестирование

| Команда | Описание |
|---------|----------|
| `make test` | Запустить API тесты |
| `make test-docker` | Полный цикл: сборка → Docker → тесты |
| `make health` | Проверка здоровья сервиса |

### База данных

| Команда | Описание |
|---------|----------|
| `make db-access` | Подключиться к PostgreSQL через psql |

### Утилиты

| Команда | Описание |
|---------|----------|
| `make env` | Показать конфигурацию окружения |

---

## 🔌 API Endpoints

### Аутентификация

| Метод | Endpoint | Описание | Коды |
|-------|----------|----------|------|
| `POST` | `/auth/register` | Регистрация пользователя | 201, 400, 409 |
| `POST` | `/auth/login` | Вход в систему | 200, 404 |
| `POST` | `/auth/token/refresh` | Обновление токена | 200, 401 |

**Пример регистрации:**
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Иван",
    "lastName": "Иванов",
    "email": "ivan@example.com",
    "phone": "+79991234567",
    "password": "password123"
  }'
```

**Ответ:**
```json
{
  "user": {
    "id": "uuid",
    "firstName": "Иван",
    "lastName": "Иванов",
    "email": "ivan@example.com",
    "phone": "+79991234567",
    "role": "user",
    "isBlocked": false,
    "createdAt": 1234567890,
    "updatedAt": 1234567890
  },
  "tokens": {
    "accessToken": "eyJhbGc...",
    "refreshToken": "uuid",
    "expiresIn": 900
  }
}
```

### Управление профилем

| Метод | Endpoint | Описание | Коды |
|-------|----------|----------|------|
| `GET` | `/users/me` | Получить текущий профиль | 200, 401 |
| `PUT` | `/users/me` | Обновить текущий профиль | 200, 400, 401, 409 |

**Пример обновления:**
```bash
curl -X PUT http://localhost:8080/users/me \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Иван",
    "lastName": "Обновленный"
  }'
```

### Администрирование

| Метод | Endpoint | Описание | Коды |
|-------|----------|----------|------|
| `GET` | `/users` | Список всех пользователей | 200, 403 |
| `GET` | `/users/{userId}` | Получить пользователя по ID | 200, 403, 404 |
| `PUT` | `/users/{userId}` | Обновить пользователя | 200, 400, 403, 404, 409 |
| `DELETE` | `/users/{userId}` | Удалить пользователя | 204, 400, 403, 404 |

> **Примечание:** Требуется роль `admin`. Новые пользователи создаются с ролью `user`.

---

## ⚙️ Конфигурация

### Переменные окружения

| Переменная | Описание | По умолчанию |
|------------|----------|--------------|
| `DB_HOST` | Хост базы данных | `postgres` |
| `DB_PORT` | Порт базы данных | `5432` |
| `DB_NAME` | Имя базы данных | `userservice` |
| `DB_USER` | Пользователь БД | `postgres` |
| `DB_PASSWORD` | Пароль БД | `postgres` |
| `JWT_SECRET` | Секретный ключ JWT | - |
| `JWT_ISSUER` | Издатель токена | `zov-deneg-user-service` |
| `JWT_AUDIENCE` | Аудитория токена | `zov-deneg-users` |
| `JWT_ACCESS_TTL_MIN` | Время жизни access токена (мин) | `15` |
| `JWT_REFRESH_TTL_DAYS` | Время жизни refresh токена (дни) | `30` |
| `SERVER_PORT` | Порт HTTP сервера | `8080` |
| `USE_EMBEDDED_DB` | Использовать H2 вместо PostgreSQL | `false` |

### Файлы конфигурации

- **`.env`** - Переменные окружения для Docker
- **`application.yaml`** - Конфигурация Ktor приложения
- **`docker-compose.yml`** - Оркестрация контейнеров

---

## 🐳 Docker

### Архитектура

```
┌─────────────────────────────────────┐
│     user-service-network            │
│           (bridge)                  │
│                                     │
│  ┌──────────────┐    ┌───────────┐ │
│  │   PostgreSQL │◄──►│user-service│ │
│  │     :5432    │    │   :8080   │ │
│  └──────────────┘    └───────────┘ │
│         │                   │       │
└─────────┼───────────────────┼───────┘
          │                   │
      volume:            host port:
   postgres_data          8080:8080
```

### Сборка образа

```bash
# 1. Сборка приложения
./gradlew installDist

# 2. Создание Docker образа
docker-compose build

# Или одной командой:
make docker-build
```

### Запуск

```bash
# Запуск всех сервисов
docker-compose up -d

# Или:
make docker-run

# Проверка статуса
docker-compose ps
```

### Логи

```bash
# Просмотр логов
docker-compose logs -f user-service-app

# Последние 100 строк
docker-compose logs --tail=100 user-service-app
```

### Остановка

```bash
# Остановка контейнеров
docker-compose down

# Остановка с удалением томов
docker-compose down -v
```

---

## ✅ Тестирование

### Запуск тестов

```bash
# Быстрый запуск
make test

# Полный цикл (сборка + Docker + тесты)
make test-docker
```

### Покрытие тестов

| Категория | Тесты | Статус |
|-----------|-------|--------|
| **Аутентификация** | register, login, refresh | ✅ 4/4 |
| **Профиль** | get, update | ✅ 3/3 |
| **Доступ** | 403 для non-admin | ✅ 3/3 |
| **Ошибки** | 401, 404, 409 | ✅ 4/4 |
| **Итого** | | **✅ 14/14** |

### Сценарии тестирования

1. **Регистрация** - создание нового пользователя
2. **Вход** - аутентификация по телефону/паролю
3. **Обновление токена** - получение новой пары токенов
4. **Профиль** - просмотр и редактирование
5. **Права доступа** - проверка ролевой модели
6. **Валидация** - обработка некорректных данных
7. **Дубликаты** - проверка уникальности email/телефона

---

## 🗄 База данных

### Подключение

```bash
# Через Makefile
make db-access

# Вручную
docker exec -it user-service-db psql -U postgres -d userservice
```

### Таблицы

**users**
```sql
CREATE TABLE users (
    id UUID PRIMARY KEY,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    phone VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) DEFAULT 'user',
    is_blocked BOOLEAN DEFAULT false,
    created_at BIGINT NOT NULL,
    updated_at BIGINT NOT NULL
);
```

**refresh_tokens**
```sql
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    token VARCHAR(255) UNIQUE NOT NULL,
    expires_at BIGINT NOT NULL,
    created_at BIGINT NOT NULL
);
```

### Полезные SQL запросы

```sql
-- Список всех пользователей
SELECT id, first_name, last_name, email, phone, role, is_blocked 
FROM users;

-- Назначить роль администратора
UPDATE users SET role = 'admin' WHERE email = 'admin@example.com';

-- Заблокировать пользователя
UPDATE users SET is_blocked = true WHERE id = 'uuid';

-- Удалить пользователя
DELETE FROM users WHERE id = 'uuid';

-- Просмотреть refresh токены
SELECT u.email, rt.token, rt.expires_at 
FROM refresh_tokens rt 
JOIN users u ON rt.user_id = u.id;
```

---

## 📁 Структура проекта

```
user-service/
├── Makefile                      # Команды сборки и запуска
├── docker-compose.yml            # Docker оркестрация
├── Dockerfile                    # Образ приложения
├── .env                          # Переменные окружения
├── .dockerignore                 # Исключения для Docker
├── build.gradle.kts              # Конфигурация Gradle
├── settings.gradle.kts           # Настройки проекта
├── test-api.sh                   # Скрипт API тестов
├── README.md                     # Эта документация
│
├── src/
│   └── main/
│       ├── kotlin/zov/deneg/
│       │   ├── Application.kt    # Точка входа
│       │   ├── Security.kt       # JWT конфигурация
│       │   │
│       │   ├── data/
│       │   │   ├── DatabaseConfig.kt  # Подключение к БД
│       │   │   ├── Tables.kt          # Exposed таблицы
│       │   │   └── UserRepository.kt  # Доступ к данным
│       │   │
│       │   ├── models/
│       │   │   └── Models.kt     # Data классы и DTO
│       │   │
│       │   ├── routes/
│       │   │   ├── AuthRoutes.kt     # /auth/* endpoints
│       │   │   └── UserRoutes.kt     # /users/* endpoints
│       │   │
│       │   └── security/
│       │       ├── JwtConfig.kt      # JWT логика
│       │       └── PasswordHasher.kt # BCrypt хеширование
│       │
│       └── resources/
│           ├── application.yaml      # Конфигурация Ktor
│           └── openapi-user-service.yaml  # OpenAPI спецификация
│
└── build/                          # Артефакты сборки
```

---

## 🔐 Безопасность

### JWT Токены

- **Access Token**: 15 минут, используется для запросов к API
- **Refresh Token**: 30 дней, используется для обновления пары токенов
- **Алгоритм**: HS256
- **Хранение**: Refresh токены хранятся в БД

### Пароли

- **Алгоритм**: BCrypt
- **Cost Factor**: 12
- **Соль**: Автоматически генерируется

### Рекомендации для Production

1. Измените `JWT_SECRET` на случайную строку (мин. 32 символа)
2. Измените пароль базы данных
3. Используйте HTTPS для продакшена
4. Настройте rate limiting
5. Включите логирование и мониторинг

---

## 🐛 Решение проблем

### Порт 8080 занят

```bash
# Найти процесс
lsof -ti:8080

# Убить процесс
lsof -ti:8080 | xargs kill -9

# Или изменить порт в .env
SERVER_PORT=8081
```

### Контейнер не запускается

```bash
# Проверить логи
make logs

# Пересобрать
make docker-clean
make docker-build
make docker-run
```

### Ошибки подключения к БД

```bash
# Проверить статус PostgreSQL
docker-compose ps

# Перезапустить
make docker-restart

# Проверить переменные окружения
make env
```

### Тесты не проходят

```bash
# Проверить что сервис запущен
make status
make health

# Посмотреть логи
make logs-tail

# Перезапустить сервис
make docker-restart
make test
```

---

## 📞 Контакты

- **Проект**: Зов Денег
- **Сервис**: User Service
- **Версия**: 1.0.0

---

## 📝 Лицензия

Внутренняя разработка для проекта "Зов Денег".
