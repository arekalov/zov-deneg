# ЗОВ Денег — API Test Results

## Summary

| Service | Endpoint | Method | Status | Notes |
|---------|----------|--------|--------|-------|
| **User Service (8080)** | | | | |
| | `/auth/register` | POST | ✅ PASS | Registration works, returns tokens |
| | `/auth/login` | POST | ✅ PASS | Login works, returns tokens |
| | `/auth/token/refresh` | POST | ✅ PASS | Token refresh works |
| | `/users/me` | GET | ✅ PASS | Returns user profile |
| | `/users/me` | PUT | ✅ PASS | Profile update works |
| | `/users` | GET | ✅ PASS | Returns 403 (admin only) |
| | `/users/{userId}` | GET | ✅ PASS | Returns 403 (admin only) |
| | `/portfolio` | GET | ✅ PASS | Returns portfolio data |
| | `/portfolio/summary` | GET | ✅ PASS | Returns portfolio summary |
| | `/orders` | GET | ✅ PASS | Returns orders list |
| | `/orders` | POST | ✅ PASS | Creates order |
| | `/transactions` | GET | ✅ PASS | Returns transactions list |
| | `/balance` | GET | ✅ PASS | Returns balance |
| | `/balance/deposit` | POST | ✅ PASS | Deposit works |
| | `/balance/withdraw` | POST | ✅ PASS | Withdraw works |
| **Securities Service (8081)** | | | | |
| | `/securities` | GET | ✅ PASS | Returns securities list |
| | `/securities?q=Сбер` | GET | ✅ PASS | Search works |
| | `/securities?type=stock` | GET | ✅ PASS | Filter by type works |
| | `/securities?exchange=MOEX` | GET | ✅ PASS | Filter by exchange works |
| | `/securities/{id}` | GET | ❌ FAIL | **Bug**: Division by zero error |
| | `/securities/{id}/price/history` | GET | ✅ PASS | Price history works |
| | `/securities/{id}/orderbook` | GET | ✅ PASS | Orderbook works |
| | `/securities/{id}/orderbook?depth=5` | GET | ✅ PASS | Orderbook with depth works |

## Test Statistics

| Metric | Count |
|--------|-------|
| **Total Tests** | 24 |
| **Passed** | 23 |
| **Failed** | 1 |
| **Success Rate** | 95.8% |

## Known Issues

### 1. Securities Service - Division by Zero Error

**Endpoint:** `GET /securities/{securityId}`

**Error:**
```
Code: 153. DB::Exception: Division by zero: while executing 
'FUNCTION divide(minus(__table2.last_price, __table2.day_open_price), 
__table2.day_open_price)'
```

**Cause:** When `day_open_price` is 0, the price change percentage calculation fails.

**Fix Required:** Add null/zero check in the SQL query or backend logic before division.

### 2. Orders Service - Missing Validation

**Endpoint:** `POST /orders`

**Issue:** Orders can be created with invalid security IDs (e.g., `00000000-0000-0000-0000-000000000000`).

**Expected:** Should return 400 Bad Request with validation error.

**Actual:** Returns 201 Created with `ticker: "UNKNOWN"`.

## How to Run Tests

```bash
cd /Users/m.s.taranenko/IdeaProjects/itmo/zov-deneg/zov-back
./test-apis.sh
```

**Requirements:**
- Both services must be running:
  - User Service: `http://localhost:8080`
  - Securities Service: `http://localhost:8081`
- Bash shell
- curl installed

## Test Data

Each test run creates a new user with:
- **Email:** `test{RANDOM}@example.com`
- **Phone:** `+7999999{RANDOM}`
- **Password:** `TestPassword123!`

## API Documentation

- **User Service:** `user-service/src/main/resources/openapi-user-service.yaml`
- **Securities Service:** `securities-service/src/main/resources/openapi-securities-service.yaml`
