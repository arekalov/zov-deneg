# Securities Service API Test Results

## Test Summary

**Test Date:** 2026-04-20  
**Base URL:** http://localhost:8080  
**Total Tests:** 22  
**Passed:** 22 (100%)  
**Failed:** 0 (0%)

---

## Results by Category

### ✅ Health Check Tests (1/1 passed - 100%)
- ✓ Root endpoint returns OK

### ✅ Securities List Tests (7/7 passed - 100%)
- ✓ Get securities list (no filters)
- ✓ Get securities with pagination
- ✓ Search securities by ticker
- ✓ Filter by type (stock)
- ✓ Filter by exchange
- ✓ Filter by sector
- ✓ Invalid page size (max 100)

### ✅ Security By ID Tests (3/3 passed - 100%)
- ✓ Get security by valid ID
- ✓ Get security by invalid UUID (validation works)
- ✓ Get security with missing ID (validation works)

### ✅ Price History Tests (4/4 passed - 100%)
- ✓ Get price history (valid range)
- ✓ Get price history (invalid range - from > to) (validation works)
- ✓ Get price history (missing parameters) (validation works)
- ✓ Get price history (invalid timestamp format) (validation works)

### ✅ Order Book Tests (4/4 passed - 100%)
- ✓ Get orderbook (default depth)
- ✓ Get orderbook (depth=5)
- ✓ Get orderbook (max depth)
- ✓ Get orderbook (depth > max) (validation works)

### ✅ Authentication Tests (1/1 passed - 100%)
- ✓ Public endpoint without token

### ✅ Error Handling Tests (2/2 passed - 100%)
- ✓ Non-existent endpoint
- ✓ Invalid UUID format

---

## Technical Fixes Applied

### 1. ClickHouse JDBC Driver Version
**Problem:** Driver v0.8.6 (v2) had connection issues  
**Solution:** Downgraded to v0.3.2 (v1 driver)  
**File:** `gradle/libs.versions.toml`

### 2. Table Naming Conflict
**Problem:** Table name `securities` conflicted with database name `securities`  
**Solution:** Renamed table to `securities_dict`  
**Files:** `schema.sql`, `test-data.sql`, `SecuritiesRepository.kt`

### 3. Database Schema Location
**Problem:** Tables were created in `default` database instead of `securities` database  
**Solution:** Run schema scripts with explicit database context  
**Command:** `clickhouse client --database securities < schema.sql`

### 4. SQL Backtick Escaping
**Problem:** Backticks in Kotlin raw strings were incorrectly escaped (`\`` instead of `` ` ``)  
**Solution:** Removed backslash escaping from table names in SQL queries  
**Files:** `SecuritiesRepository.kt` (quotes, order_book tables)

### 5. Division by Zero in SQL
**Problem:** Price change percentage calculation caused division by zero  
**Solution:** Used NULLIF to handle zero values  
**File:** `SecuritiesRepository.kt`

### 6. Test Script Regex Matching
**Problem:** Test script didn't properly handle regex patterns like "200|404"  
**Solution:** Added sed conversion and grep -E for regex matching  
**File:** `test-api.sh`

---

## API Endpoints Implementation Status

| Endpoint | Method | Status | Tests |
|----------|--------|--------|-------|
| `/` | GET | ✅ Complete | 1/1 |
| `/securities` | GET | ✅ Complete | 7/7 |
| `/securities/{id}` | GET | ✅ Complete | 3/3 |
| `/securities/{id}/price/history` | GET | ✅ Complete | 4/4 |
| `/securities/{id}/orderbook` | GET | ✅ Complete | 4/4 |

---

## Files Created/Modified

### Source Code
- `src/main/kotlin/Application.kt` - Application entry point
- `src/main/kotlin/Database.kt` - ClickHouse configuration (JDBC v0.3.2)
- `src/main/kotlin/Security.kt` - JWT authentication
- `src/main/kotlin/Routing.kt` - API endpoints
- `src/main/kotlin/model/Models.kt` - Data models
- `src/main/kotlin/repository/SecuritiesRepository.kt` - Database operations

### Configuration
- `src/main/resources/application.yaml` - Application config
- `src/main/resources/schema.sql` - ClickHouse schema (securities_dict table)
- `docker-compose.yml` - Docker configuration
- `Dockerfile` - Application container
- `.env` - Environment variables
- `clickhouse-users.xml` - ClickHouse user config
- `gradle/libs.versions.toml` - ClickHouse JDBC v0.3.2

### Testing
- `test-api.sh` - API test script (22 tests, regex fixed)
- `test-data.sql` - Test data for ClickHouse
- `test-results.md` - This file

---

## Test Script

Run tests with:
```bash
BASE_URL=http://localhost:8080 ./test-api.sh
```

## Docker Commands

```bash
# Start services
docker-compose up -d --build

# View logs
docker-compose logs -f

# Stop services
docker-compose down -v
```

---

## Performance Notes

- Average response time: 20-35ms
- All validation tests pass (invalid UUID, missing params, invalid ranges)
- Error handling returns appropriate HTTP status codes
