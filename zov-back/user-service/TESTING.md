# User Service - Testing Guide

## Overview

The User Service has comprehensive test coverage including:
- **Unit Tests** - Test individual components
- **Integration Tests** - Test API endpoints with Testcontainers
- **Repository Tests** - Test database operations
- **Validation Tests** - Test input validation
- **API Tests** - End-to-end tests against running service

## Test Files

| File | Purpose | Tests |
|------|---------|-------|
| `ApplicationTest.kt` | Module loading test | 1 |
| `IntegrationTest.kt` | API integration tests | 30 |
| `RepositoryTest.kt` | Database layer tests | 31 |
| `ValidationTest.kt` | Input validation tests | 18 |
| `test-api.sh` | Shell-based API tests | 14 |

**Total: 94 tests**

## Running Tests

### All Tests (Gradle)

```bash
make test
# or
./gradlew test
```

### Specific Test Categories

```bash
# Unit tests only
make test-unit

# Integration tests (requires Docker)
make test-integration

# API tests against running service
make test-api
```

### Full Test Pipeline

```bash
# Clean build → Docker → Tests → Stop
make test-docker
```

## Test Coverage

### Authentication Tests
- ✅ User registration
- ✅ Duplicate email/phone detection
- ✅ Login with phone/password
- ✅ Token refresh
- ✅ Password validation
- ✅ Input validation

### User Profile Tests
- ✅ Get current user profile
- ✅ Update profile
- ✅ Validation on update

### Balance Tests
- ✅ Get balance (auto-creates)
- ✅ Deposit funds
- ✅ Withdraw funds
- ✅ Insufficient funds handling
- ✅ Amount validation

### Transaction Tests
- ✅ List transactions
- ✅ Pagination
- ✅ Filter by type
- ✅ Filter by security

### Security Tests
- ✅ Unauthorized access (401)
- ✅ Invalid token handling
- ✅ Role-based access (403)

### Admin Tests
- ✅ List all users
- ✅ Get user by ID
- ✅ Update user
- ✅ Delete user
- ✅ Cannot delete self

## Test Configuration

### Testcontainers Setup

Integration tests use Testcontainers to spin up a real PostgreSQL database:

```kotlin
@Container
val postgres = PostgreSQLContainer("postgres:15-alpine").apply {
    withDatabaseName("test_users")
    withUsername("test")
    withPassword("test")
}
```

### Test Environment

Tests use isolated configuration:
- JWT secret: `test-secret-key-for-integration-tests-min-32-chars`
- Database: PostgreSQL 15 (Testcontainers)
- Embedded H2 for unit tests

## Writing New Tests

### Integration Test Example

```kotlin
@Test
fun `test register user - success`() = runTest {
    testApplication {
        environment { config = createTestConfig() }
        application { module() }

        val response = client.post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "firstName" to "Иван",
                "lastName" to "Иванов",
                "email" to "ivan@test.com",
                "phone" to "+79001234567",
                "password" to "password123"
            ))
        }

        assertEquals(HttpStatusCode.Created, response.status)
    }
}
```

### Repository Test Example

```kotlin
@Test
fun `test create user`() {
    val user = userRepository.create(
        firstName = "John",
        lastName = "Doe",
        email = "john@example.com",
        phone = "+79001234567",
        passwordHash = "hashed_password"
    )

    assertNotNull(user)
    assertEquals("John", user.firstName)
}
```

## Troubleshooting

### Testcontainers Issues

If tests fail with container startup errors:
```bash
# Ensure Docker is running
docker ps

# Increase startup timeout if needed
withStartupTimeout(Duration.ofMinutes(5))
```

### Port Conflicts

If port 8080 is busy during API tests:
```bash
# Find and kill process
lsof -ti:8080 | xargs kill -9

# Or change port in .env
SERVER_PORT=8081
```

### Database State Issues

If tests fail due to database state:
```bash
# Clean test containers
docker rm -f $(docker ps -aq --filter "name=test_")
```

## Test Results

### Viewing Results

```bash
# HTML report
open build/reports/tests/test/index.html

# API test results (Markdown)
cat test-results.md
```

### Expected Results

- **Unit Tests**: Should pass in < 5 seconds
- **Integration Tests**: Should pass in < 60 seconds (includes container startup)
- **Repository Tests**: Should pass in < 30 seconds
- **API Tests**: Should pass in < 10 seconds (against running service)

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run Tests
  run: make test-docker

- name: Upload Test Results
  uses: actions/upload-artifact@v3
  with:
    name: test-results
    path: build/reports/tests/test/
```

### GitLab CI Example

```yaml
test:
  image: gradle:8.5-jdk21
  services:
    - docker:dind
  script:
    - make test-docker
  artifacts:
    reports:
      junit: build/test-results/test/*.xml
```

## Best Practices

1. **Use Testcontainers** for integration tests - ensures real database behavior
2. **Clean state between tests** - use `@BeforeEach` to reset database
3. **Test validation** - cover edge cases and invalid inputs
4. **Test error responses** - verify proper HTTP status codes
5. **Use meaningful test names** - describe the scenario being tested
6. **Keep tests independent** - no shared state between tests
7. **Run tests frequently** - catch issues early

## Performance

Typical test execution times:
- Unit tests: ~2-5 seconds
- Integration tests: ~30-60 seconds (with container startup)
- Full suite: ~60-90 seconds

## Coverage Goals

| Category | Target | Current |
|----------|--------|---------|
| Authentication | 100% | ✅ |
| User Management | 100% | ✅ |
| Balance Operations | 100% | ✅ |
| Transactions | 100% | ✅ |
| Validation | 100% | ✅ |
| Error Handling | 100% | ✅ |

## Support

For test-related issues:
1. Check test output logs
2. Review HTML test report
3. Verify Docker is running
4. Ensure all dependencies are installed
