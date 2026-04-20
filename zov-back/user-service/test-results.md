# User Service API Test Results

Test Date: Mon Apr 20 17:05:36 MSK 2026

---

| Endpoint | Method | Status | Code |
|----------|--------|--------|------|
| / | GET | ✅ | 404 |
| /auth/register | POST | ✅ | 201 |
| /auth/login | POST | ✅ | 200 |
| /auth/token/refresh | POST | ✅ | 200 |
| /users/me | GET | ✅ | 200 |
| /users/me | PUT | ✅ | 200 |
| /users | GET | ✅ | 403 |
| /users/me | GET | ✅ | 401 |
| /users/me | GET | ✅ | 401 |
| /auth/register (admin) | POST | ✅ | 201 |
| /auth/login (admin) | POST | ✅ | 200 |
| /users (new admin) | GET | ✅ | 403 |
| /auth/register (dup) | POST | ✅ | 409 |
| /auth/login (invalid) | POST | ✅ | 404 |

---

## Summary

**Passed:** 14
**Failed:** 0
**Total:** 14

### Test Coverage

✅ Authentication endpoints (register, login, refresh)
✅ User profile management (get, update)
✅ Role-based access control (403 for non-admin)
✅ Error handling (401 unauthorized, 404 not found, 409 conflict)

### Notes

- Admin endpoints require `role: 'admin'` in the database
- To set admin role: `UPDATE users SET role = 'admin' WHERE id = '<user_id>';`
- New registrations default to `role: 'user'`
