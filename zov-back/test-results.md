# ЗОВ Денег — API Test Results

## Summary

| Service | Endpoint | Method | Status | Content Validation | Notes |
|---------|----------|--------|--------|-------------------|-------|
| **User Service (8080)** | | | | | |
| | `/auth/register` | POST | ✅ PASS | ✅ email, firstName, user, tokens | Registration works |
| | `/auth/login` | POST | ✅ PASS | ✅ user, accessToken, refreshToken | Login works |
| | `/auth/token/refresh` | POST | ✅ PASS | ✅ accessToken, refreshToken, expiresIn | Token refresh works |
| | `/users/me` | GET | ✅ PASS | ✅ id, email, role, phone | Returns user profile |
| | `/users/me` | PUT | ✅ PASS | ✅ firstName, updatedAt | Profile update works |
| | `/users` | GET | ✅ PASS | ✅ 403 error response | Admin only |
| | `/users/{userId}` | GET | ✅ PASS | ✅ 403 error response | Admin only |
| | `/portfolio` | GET | ✅ PASS | ✅ totalValue, items, cashBalance | Portfolio data |
| | `/portfolio/summary` | GET | ✅ PASS | ✅ totalValue, profitLoss, profitLossPct | Summary data |
| | `/orders` | GET | ✅ PASS | ✅ data, pagination | Orders list |
| | `/orders` | POST | ✅ PASS | ✅ id, securityId, side, status | Creates order |
| | `/transactions` | GET | ✅ PASS | ✅ data, pagination | Transactions list |
| | `/balance` | GET | ✅ PASS | ✅ available, total, blocked | Balance data |
| | `/balance/deposit` | POST | ✅ PASS | ✅ available=10000.00 | Deposit works |
| | `/balance/withdraw` | POST | ✅ PASS | ✅ available=9000.00 | Withdraw works |
| **Securities Service (8081)** | | | | | |
| | `/securities` | GET | ✅ PASS | ✅ data, pagination, ticker, lastPrice | Securities list |
| | `/securities?q=Сбер` | GET | ✅ PASS | ✅ data, pagination | Search works |
| | `/securities?type=stock` | GET | ✅ PASS | ✅ data | Filter by type |
| | `/securities?exchange=MOEX` | GET | ✅ PASS | ✅ data | Filter by exchange |
| | `/securities/{id}` | GET | ❌ FAIL | N/A | **Bug**: Division by zero |
| | `/securities/{id}/price/history` | GET | ✅ PASS | ✅ securityId, ticker, from, to, data | Price history |
| | `/securities/{id}/orderbook` | GET | ✅ PASS | ✅ securityId, ticker, asks, bids, spread | Orderbook |
| | `/securities/{id}/orderbook?depth=5` | GET | ✅ PASS | ✅ securityId, ticker | Orderbook with depth |

## Test Statistics

| Metric | Count |
|--------|-------|
| **Total Tests** | 65 |
| **Passed** | 64 |
| **Failed** | 1 |
| **Success Rate** | 98.5% |

**Note:** Tests now validate both HTTP status codes AND response content (JSON fields, values, structure).

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
