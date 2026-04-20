# Securities Service - Project Context

## Project Overview

**Securities Service** is a Kotlin-based backend service for managing securities (stocks, bonds, ETFs) with real-time quote storage and order book data using **ClickHouse** as the primary database. Part of the "zov-deneg" (зо́в де́нег - "call of money") investment platform.

### Key Technologies

| Component | Technology |
|-----------|------------|
| **Language** | Kotlin 2.3.0 (JVM 21) |
| **Framework** | Ktor (server, auth, JWT, serialization) |
| **Database** | ClickHouse 24.8 (quotes, order book snapshots) |
| **Build Tool** | Gradle (Kotlin DSL) |
| **Containerization** | Docker, Docker Compose |
| **Testing** | JUnit 5, Testcontainers, Ktor test helpers |

### Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Securities Service                        │
│  ┌─────────────┐  ┌──────────────┐  ┌─────────────────────┐ │
│  │   Routing   │  │   Security   │  │    Repositories     │ │
│  │  (REST API) │  │   (JWT)      │  │  (ClickHouse JDBC)  │ │
│  └─────────────┘  └──────────────┘  └─────────────────────┘ │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
                    ┌─────────────────┐
                    │   ClickHouse    │
                    │  - securities   │
                    │  - quotes       │
                    │  - order_book   │
                    │  - securities_latest (MV) │
                    └─────────────────┘
```

### Database Schema

**Tables:**
- `securities_dict` - Securities reference data (ticker, name, type, exchange, sector)
- `quotes` - Time-series price data (ticks/trades) with 2-year TTL
- `order_book` - Order book snapshots with 1-day TTL
- `securities_latest` - Materialized view with last prices and daily changes

## Building and Running

### Prerequisites

- **JDK 21** (Eclipse Temurin recommended)
- **Docker & Docker Compose**
- **Gradle 8.x** (wrapper included)

### Quick Start (Docker Compose)

```bash
# Build and start all services (service + ClickHouse)
docker-compose up --build

# Start in detached mode
docker-compose up -d --build

# Stop and remove containers
docker-compose down

# View logs
docker-compose logs -f securities-service
```

**Services available after startup:**
- API: http://localhost:8080
- ClickHouse HTTP: http://localhost:8123
- ClickHouse Native: localhost:9000

### Local Development

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run locally (requires ClickHouse running)
./gradlew run

# Create distribution package
./gradlew installDist

# Run the packaged application
./build/install/securities-service/bin/securities-service
```

### Environment Configuration

Create a `.env` file in the project root:

```bash
# ClickHouse
CLICKHOUSE_HOST=localhost
CLICKHOUSE_PORT_HTTP=8123
CLICKHOUSE_DB=securities
CLICKHOUSE_USER=default
CLICKHOUSE_PASSWORD=

# JWT Authentication
JWT_SECRET=your-super-secret-jwt-key-change-this-in-production-min-32-chars
JWT_ISSUER=zov-deneg-securities-service
JWT_AUDIENCE=zov-deneg-securities
JWT_REALM=zov-deneg securities service

# Server
SERVER_PORT=8080
```

## API Endpoints

### Public Endpoints (No Auth Required)

| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/` | Health check |
| `GET` | `/securities` | List securities with filtering & pagination |
| `GET` | `/securities/{id}` | Get security details by UUID |
| `GET` | `/securities/{id}/price/history` | Get price history (from/to timestamps) |
| `GET` | `/securities/{id}/orderbook` | Get order book snapshot |
| `GET` | `/portfolio` | Get user portfolio (auth required) |
| `POST` | `/orders` | Create order (auth required) |
| `GET` | `/orders` | List user orders (auth required) |

### Query Parameters

**GET /securities:**
- `q` - Search by ticker or name (partial match)
- `type` - Filter by type: `stock`, `bond`, `etf`
- `exchange` - Filter by exchange: `MOEX`, `SPB`
- `sector` - Filter by sector (Russian names, e.g., "Финансы")
- `page` - Page number (default: 1)
- `pageSize` - Items per page (default: 20, max: 100)

**GET /securities/{id}/price/history:**
- `from` - Start timestamp (Unix seconds, required)
- `to` - End timestamp (Unix seconds, required)

**GET /securities/{id}/orderbook:**
- `depth` - Order book depth (default: 10, max: 50)

### Testing the API

```bash
# Run the test script
./test-api.sh

# With custom base URL
BASE_URL=http://localhost:8080 ./test-api.sh

# Results saved to test-results.md
```

## Project Structure

```
securities-service/
├── src/main/kotlin/
│   ├── Application.kt          # Main entry point, module configuration
│   ├── Database.kt             # ClickHouse DataSource configuration
│   ├── Security.kt             # JWT authentication setup
│   ├── Routing.kt              # API route definitions
│   ├── model/
│   │   └── Models.kt           # Data classes, enums, DTOs
│   ├── repository/
│   │   ├── SecuritiesRepository.kt
│   │   ├── PortfolioRepository.kt
│   │   └── OrderRepository.kt
│   └── routes/
│       ├── PortfolioRoutes.kt
│       └── OrderRoutes.kt
├── src/main/resources/
│   ├── application.yaml        # Ktor & app configuration
│   ├── schema.sql              # ClickHouse schema (auto-applied)
│   ├── logback.xml             # Logging configuration
│   └── openapi-securities-service.yaml
├── src/test/kotlin/
│   ├── ApplicationTest.kt      # Unit tests
│   └── IntegrationTest.kt      # Integration tests with Testcontainers
├── docker-compose.yml          # Docker Compose configuration
├── Dockerfile                  # Production image build
├── test-api.sh                 # API test script
├── test-data.sql               # Test data for ClickHouse
└── build.gradle.kts            # Gradle build configuration
```

## Development Conventions

### Code Style

- **Kotlin coding conventions** (official)
- **Functional style** preferred in repositories
- **Explicit error handling** with try-catch in routes
- **UUIDs** for all entity identifiers
- **Decimal128** for all monetary values (8 decimal places)

### Testing Practices

- **Unit tests**: Use `testApplication` from Ktor test helpers
- **Integration tests**: Use Testcontainers for ClickHouse
- **API tests**: Use `test-api.sh` shell script
- Test data is auto-loaded via `test-data.sql`

### Database Patterns

- **Read operations**: Direct SQL in repositories (no ORM)
- **Time-series data**: Aggregated on read with dynamic intervals
- **Materialized views**: For frequently accessed computed data
- **TTL policies**: 2 years for quotes, 1 day for order book

### Commit Message Style

Follow conventional commits:
```
feat: add order book endpoint
fix: handle null price in history response
docs: update API documentation
test: add integration tests for portfolio
```

## Common Tasks

### Adding a New Endpoint

1. Add route in `Routing.kt` or create new file in `routes/`
2. Add request/response models in `Models.kt`
3. Add repository method if database access needed
4. Add tests in `src/test/kotlin/`

### Database Schema Changes

1. Update `src/main/resources/schema.sql`
2. The schema is auto-applied on first ClickHouse container start
3. For existing volumes, manually apply: `docker-compose exec clickhouse clickhouse-client --query="$(cat schema.sql)"`

### Debugging

```bash
# View service logs
docker-compose logs -f securities-service

# View ClickHouse logs
docker-compose logs -f clickhouse

# Access ClickHouse CLI
docker-compose exec clickhouse clickhouse-client

# Query ClickHouse directly
curl "http://localhost:8123/?query=SELECT%20*%20FROM%20securities_dict"
```

## Troubleshooting

| Issue | Solution |
|-------|----------|
| Service won't start | Check ClickHouse is healthy: `docker-compose ps` |
| JWT auth failures | Verify `JWT_SECRET` is at least 32 characters |
| Empty securities list | Ensure `test-data.sql` was loaded into ClickHouse |
| Port conflicts | Change `SERVER_PORT` in `.env` |
| Build fails | Ensure JDK 21 is installed: `java -version` |

## Related Documentation

- [README.md](README.md) - User-facing documentation
- [helper.md](helper.md) - Database schema design notes
- [test-results.md](test-results.md) - Latest API test results
- [OpenAPI Spec](src/main/resources/openapi-securities-service.yaml) - API contract
