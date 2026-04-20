# Securities Service API Test Results

## Test Summary

**Test Date:** 2026-04-20T17:55:07+03:00
**Base URL:** http://localhost:8081

| Test | Endpoint | Method | Status | Response Time |
|------|----------|--------|--------|---------------|

## Health Check Tests

| Root endpoint returns OK | / | GET | \033[0;32mPASS\033[0m | 115ms |

## Securities List Tests

| Get securities list (no filters) | /securities | GET | \033[0;32mPASS\033[0m | 282ms |
| Get securities with pagination | /securities?page=1&pageSize=10 | GET | \033[0;32mPASS\033[0m | 27ms |
| Search securities by ticker | /securities?q=SBER | GET | \033[0;32mPASS\033[0m | 25ms |
| Filter by type (stock) | /securities?type=stock | GET | \033[0;32mPASS\033[0m | 31ms |
| Filter by exchange | /securities?exchange=MOEX | GET | \033[0;32mPASS\033[0m | 24ms |
| Filter by sector | /securities?sector=Финансы | GET | \033[0;32mPASS\033[0m | 25ms |
| Invalid page size (max 100) | /securities?pageSize=200 | GET | \033[0;32mPASS\033[0m | 26ms |

## Security By ID Tests

| Get security by valid ID | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11 | GET | \033[0;32mPASS\033[0m | 27ms |
| Get security by invalid UUID | /securities/invalid-uuid | GET | \033[0;32mPASS\033[0m | 17ms |
| Get security with missing ID | /securities/ | GET | \033[0;32mPASS\033[0m | 17ms |

## Price History Tests

| Get price history (valid range) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history?from=1776610507&to=1776696907 | GET | \033[0;32mPASS\033[0m | 23ms |
| Get price history (invalid range - from > to) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history?from=1776696907&to=1776610507 | GET | \033[0;32mPASS\033[0m | 18ms |
| Get price history (missing parameters) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history | GET | \033[0;32mPASS\033[0m | 16ms |
| Get price history (invalid timestamp format) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/price/history?from=invalid | GET | \033[0;32mPASS\033[0m | 17ms |

## Order Book Tests

| Get orderbook (default depth) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook | GET | \033[0;32mPASS\033[0m | 23ms |
| Get orderbook (depth=5) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook?depth=5 | GET | \033[0;32mPASS\033[0m | 22ms |
| Get orderbook (max depth) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook?depth=50 | GET | \033[0;32mPASS\033[0m | 23ms |
| Get orderbook (depth > max) | /securities/a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11/orderbook?depth=100 | GET | \033[0;32mPASS\033[0m | 26ms |

## Authentication Tests

| Public endpoint without token | /securities | GET | \033[0;32mPASS\033[0m | 31ms |

## Error Handling Tests

| Non-existent endpoint | /nonexistent | GET | \033[0;32mPASS\033[0m | 20ms |
| Invalid UUID format | /securities/not-a-uuid/price/history?from=1&to=2 | GET | \033[0;32mPASS\033[0m | 20ms |

## Summary

- **Total Tests:** 22
- **Passed:** 22
- **Failed:** 0
- **Success Rate:** 100%
