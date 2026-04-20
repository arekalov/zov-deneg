# User Service - NESSY Context

## Project Overview

**User Service** is a Kotlin/Ktor-based microservice for the "Зов Денег" (Zov Deneg) financial platform. It provides user management, authentication, and authorization capabilities with JWT tokens and PostgreSQL persistence.

### Core Features
- User registration and authentication
- JWT-based authentication (Access + Refresh tokens)
- Profile management (view/update)
- Role-based access control (user/admin)
- Balance and transaction tracking
- Admin user management endpoints

### Technology Stack
| Component | Technology |
|-----------|------------|
| Language | Kotlin 2.3.0 (JVM 21) |
| Framework | Ktor 3.4.2 (Netty server) |
| ORM | Exposed 0.57.0 |
| Database | PostgreSQL 15 |
| Authentication | JWT (java-jwt 4.5.0) |
| Password Hashing | BCrypt (cost 12) |
| Build Tool | Gradle 8.5 |
| Containerization | Docker + Docker Compose |

## Project Structure

```
user-service/
├── src/main/kotlin/
│   ├── Application.kt          # Entry point, module configuration
│   ├── Security.kt             # JWT security configuration
│   ├── Routing.kt              # Route registration
│   ├── data/
│   │   ├── DatabaseConfig.kt   # Database connection setup
│   │   ├── Tables.kt           # Exposed table definitions
│   │   └── UserRepository.kt   # Data access layer
│   ├── models/
│   │   └── Models.kt           # DTOs, request/response models
│   ├── routes/
│   │   ├── AuthRoutes.kt       # /auth/* endpoints
│   │   ├── UserRoutes.kt       # /users/* endpoints
│   │   ├── BalanceRoutes.kt    # /balance/* endpoints
│   │   └── TransactionRoutes.kt # /transactions/* endpoints
│   └── security/
│       ├── JwtConfig.kt        # JWT token generation/validation
│       └── PasswordHasher.kt   # BCrypt password utilities
├── src/main/resources/
│   ├── application.yaml        # Ktor configuration
│   └── openapi-user-service.yaml
├── docker-compose.yml          # PostgreSQL + App orchestration
├── Dockerfile                  # Application container
├── .env                        # Environment variables
├── build.gradle.kts            # Gradle configuration
├── Makefile                    # Development commands
└── test-api.sh                 # API integration tests
```

## Building and Running

### Prerequisites
- JDK 21+
- Docker & Docker Compose (for containerized deployment)
- Gradle 8.5+ (bundled via `gradlew`)

### Local Development

```bash
# Build the application
make build
# or: ./gradlew installDist --no-daemon

# Run locally
make run
# or: ./gradlew run --no-daemon

# Clean build artifacts
make clean
```

### Docker Deployment

```bash
# Build Docker image
make docker-build

# Start containers (PostgreSQL + App)
make docker-run

# View logs
make logs
make logs-tail          # Last 100 lines

# Stop containers
make docker-stop

# Full cleanup (containers, volumes, images)
make docker-clean
```

### Environment Configuration

Edit `.env` before running:

```bash
# Database
DB_HOST=postgres
DB_PORT=5432
DB_NAME=userservice
DB_USER=postgres
DB_PASSWORD=<change-me>

# JWT (min 32 characters)
JWT_SECRET=<your-secret-key>
JWT_ISSUER=zov-deneg-user-service
JWT_AUDIENCE=zov-deneg-users
JWT_ACCESS_TTL_SEC=900
JWT_REFRESH_TTL_DAYS=30

# Server
SERVER_PORT=8080
```

## Testing

### API Integration Tests

```bash
# Run tests against running service
make test
# or: ./test-api.sh

# Full pipeline: build → docker → test → stop
make test-docker
```

### Test Coverage
- Authentication: register, login, token refresh
- Profile management: get/update current user
- Access control: 403 for non-admin users
- Error handling: 401, 404, 409 responses

### Database Access

```bash
# Connect to PostgreSQL
make db-access
# or: docker exec -it user-service-db psql -U postgres -d userservice
```

## API Endpoints

### Authentication
| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/auth/register` | Register new user | 201, 400, 409 |
| POST | `/auth/login` | Login with phone/password | 200, 404 |
| POST | `/auth/token/refresh` | Refresh access token | 200, 401 |

### User Profile
| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/users/me` | Get current profile | 200, 401 |
| PUT | `/users/me` | Update current profile | 200, 400, 401, 409 |

### Admin (requires `admin` role)
| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/users` | List all users | 200, 403 |
| GET | `/users/{userId}` | Get user by ID | 200, 403, 404 |
| PUT | `/users/{userId}` | Update user | 200, 400, 403, 404, 409 |
| DELETE | `/users/{userId}` | Delete user | 204, 400, 403, 404 |

### Balance & Transactions
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/balance` | Get user balance |
| POST | `/balance/deposit` | Deposit funds |
| POST | `/balance/withdraw` | Withdraw funds |
| GET | `/transactions` | List user transactions |

## Database Schema

### Tables (via Exposed ORM)

**users**
- `id` (UUID, primary key)
- `first_name`, `last_name` (VARCHAR)
- `email`, `phone` (VARCHAR, unique)
- `password_hash` (TEXT)
- `role` (VARCHAR, default 'user')
- `is_blocked` (BOOLEAN)
- `created_at`, `updated_at` (TIMESTAMP)

**refresh_tokens**
- `id` (UUID, primary key)
- `user_id` (FK → users.id, CASCADE delete)
- `token` (VARCHAR, unique)
- `expires_at`, `is_revoked`, `created_at`

**user_balances**
- `user_id` (FK → users.id, primary key)
- `available`, `blocked` (DECIMAL 19,4)
- `updated_at` (TIMESTAMP)

**transactions**
- `id` (UUID, primary key)
- `user_id` (FK → users.id)
- `type` (VARCHAR: buy/sell/deposit/withdrawal/dividend)
- `security_id`, `ticker`, `security_name` (nullable)
- `quantity`, `price`, `amount`, `commission` (DECIMAL)
- `order_id` (UUID, nullable)
- `created_at` (TIMESTAMP)

## Development Conventions

### Code Style
- Kotlin idioms and best practices
- Data classes for DTOs/models
- `kotlinx.serialization` for JSON (de)serialization
- Exposed DSL for database operations

### Architecture Patterns
- **Layered architecture**: routes → repository → database
- **Dependency injection**: manual (via `module()` function)
- **Security**: JWT authentication via Ktor auth feature
- **Error handling**: Structured error responses with codes

### Testing Practices
- Integration tests via `test-api.sh` (curl-based)
- Testcontainers for isolated database testing
- Random data generation to avoid conflicts

### Git/Commit Conventions
- Conventional Commits format recommended
- Clear, descriptive commit messages
- Feature branches for new development

## Common Commands

```bash
# Development workflow
make clean && make build && make docker-build && make docker-run

# Check service health
curl http://localhost:8080/  # Expected: 404 (no root endpoint)

# View container status
make status

# Restart service
make docker-restart

# Debug: check environment
make env
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Port 8080 busy | `lsof -ti:8080 \| xargs kill -9` or change `SERVER_PORT` |
| Container won't start | `make logs` to check errors, verify `.env` |
| DB connection failed | Ensure PostgreSQL is healthy: `docker-compose ps` |
| Tests failing | Run `make docker-restart` then `make test` |

## Related Services

Part of the "Зов Денег" microservices ecosystem:
- `securities-service` - Securities/stocks management
- Other services in `zov-back/` directory

## Key Files Reference

| File | Purpose |
|------|---------|
| `build.gradle.kts` | Dependencies, JVM target, test configuration |
| `Application.kt` | Main entry point, Ktor module setup |
| `Models.kt` | All request/response DTOs |
| `Tables.kt` | Database schema definitions |
| `docker-compose.yml` | Service orchestration |
| `.env` | Runtime configuration (DO NOT COMMIT secrets) |
| `test-api.sh` | API integration test suite |
