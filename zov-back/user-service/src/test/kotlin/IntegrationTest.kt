package zov.deneg

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class UserIntegrationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15-alpine").apply {
            withDatabaseName("test_users")
            withUsername("test")
            withPassword("test")
            withStartupTimeout(Duration.ofMinutes(2))
        }

        lateinit var jdbcUrl: String
        lateinit var dbUser: String
        lateinit var dbPassword: String

        @JvmStatic
        @BeforeAll
        fun setup() {
            jdbcUrl = postgres.jdbcUrl
            dbUser = postgres.username
            dbPassword = postgres.password
        }
    }

    private lateinit var userAccessToken: String
    private lateinit var userRefreshToken: String
    private lateinit var userId: String
    private lateinit var adminAccessToken: String
    private lateinit var adminId: String

    private fun createTestConfig(): MapApplicationConfig = MapApplicationConfig(
        "jwt.secret" to "test-secret-key-for-integration-tests-min-32-chars",
        "jwt.audience" to "zov-deneg-users",
        "jwt.issuer" to "zov-deneg-user-service",
        "jwt.realm" to "zov-deneg user service",
        "jwt.accessTokenTtlSeconds" to "900",
        "jwt.refreshTokenTtlDays" to "30",
        "database.useEmbedded" to "true",
        "h2.driver" to "org.postgresql.Driver",
        "h2.url" to jdbcUrl,
        "h2.user" to dbUser,
        "h2.password" to dbPassword
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @Order(1)
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
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("user"))
            assertTrue(body.containsKey("tokens"))

            val user = body["user"]!!.jsonObject
            userId = user["id"]!!.jsonPrimitive.content
            assertEquals("Иван", user["firstName"]!!.jsonPrimitive.content)
            assertEquals("Иванов", user["lastName"]!!.jsonPrimitive.content)
            assertEquals("ivan@test.com", user["email"]!!.jsonPrimitive.content)
            assertEquals("+79001234567", user["phone"]!!.jsonPrimitive.content)
            assertEquals("user", user["role"]!!.jsonPrimitive.content)
            assertFalse(user["isBlocked"]!!.jsonPrimitive.booleanOrNull ?: false)

            val tokens = body["tokens"]!!.jsonObject
            userAccessToken = tokens["accessToken"]!!.jsonPrimitive.content
            userRefreshToken = tokens["refreshToken"]!!.jsonPrimitive.content
            assertEquals(900, tokens["expiresIn"]!!.jsonPrimitive.intOrNull ?: 900)
        }
    }

    @Test
    @Order(2)
    fun `test register duplicate email`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Петр",
                    "lastName" to "Петров",
                    "email" to "ivan@test.com",
                    "phone" to "+79007654321",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.Conflict, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("EMAIL_ALREADY_EXISTS", body["code"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(3)
    fun `test register duplicate phone`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Петр",
                    "lastName" to "Петров",
                    "email" to "petr@test.com",
                    "phone" to "+79001234567",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.Conflict, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("PHONE_ALREADY_EXISTS", body["code"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(4)
    fun `test register validation errors`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            // Test invalid email
            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Тест",
                    "lastName" to "Тестов",
                    "email" to "invalid-email",
                    "phone" to "+79009999999",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("VALIDATION_ERROR", body["code"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(5)
    fun `test register short password`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Тест",
                    "lastName" to "Тестов",
                    "email" to "short@test.com",
                    "phone" to "+79008888888",
                    "password" to "short"
                ))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    @Order(6)
    fun `test login - success`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "phone" to "+79001234567",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("user"))
            assertTrue(body.containsKey("tokens"))
        }
    }

    @Test
    @Order(7)
    fun `test login - wrong password`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "phone" to "+79001234567",
                    "password" to "wrongpassword"
                ))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    @Order(8)
    fun `test login - user not found`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "phone" to "+79990000000",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    @Test
    @Order(9)
    fun `test refresh token`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/token/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to userRefreshToken))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("accessToken"))
            assertTrue(body.containsKey("refreshToken"))
        }
    }

    @Test
    @Order(10)
    fun `test refresh token - invalid token`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/token/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to "invalid-token"))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    // ==================== USER PROFILE TESTS ====================

    @Test
    @Order(11)
    fun `test get current user profile`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/users/me") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("Иван", body["firstName"]!!.jsonPrimitive.content)
            assertEquals("Иванов", body["lastName"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(12)
    fun `test update current user profile`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.put("/users/me") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "ИванUpdated",
                    "email" to "ivan.updated@test.com"
                ))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("ИванUpdated", body["firstName"]!!.jsonPrimitive.content)
            assertEquals("ivan.updated@test.com", body["email"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(13)
    fun `test update profile - unauthorized`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.put("/users/me") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("firstName" to "Test"))
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    @Order(14)
    fun `test get users list - non-admin forbidden`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/users") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    // ==================== ADMIN TESTS ====================

    @Test
    @Order(15)
    fun `test register admin user`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Admin",
                    "lastName" to "Adminov",
                    "email" to "admin@test.com",
                    "phone" to "+79009998877",
                    "password" to "admin123"
                ))
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            adminId = body["user"]!!.jsonObject["id"]!!.jsonPrimitive.content
            adminAccessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content
        }
    }

    @Test
    @Order(16)
    fun `test admin cannot access users before role update`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/users") {
                header(HttpHeaders.Authorization, "Bearer $adminAccessToken")
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    // Note: To fully test admin functionality, you would need to update the user's role in DB
    // This would require direct database access in tests

    // ==================== BALANCE TESTS ====================

    @Test
    @Order(17)
    fun `test get balance - initial zero`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/balance") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("0.00", body["available"]!!.jsonPrimitive.content)
            assertEquals("0.00", body["total"]!!.jsonPrimitive.content)
            assertEquals("0.00", body["blocked"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(18)
    fun `test deposit - success`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "10000.00"))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("10000.00", body["available"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(19)
    fun `test deposit - invalid amount zero`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "0"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    @Order(20)
    fun `test deposit - invalid amount negative`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "-100"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    @Order(21)
    fun `test deposit - minimum amount`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "0.50"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    @Order(22)
    fun `test withdraw - success`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/balance/withdraw") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "5000.00"))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("5000.00", body["available"]!!.jsonPrimitive.content)
        }
    }

    @Test
    @Order(23)
    fun `test withdraw - insufficient funds`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/balance/withdraw") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "100000.00"))
            }

            assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
        }
    }

    // ==================== TRANSACTION TESTS ====================

    @Test
    @Order(24)
    fun `test get transactions - with data`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/transactions") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("data"))
            assertTrue(body.containsKey("pagination"))

            val data = body["data"]!!.jsonArray
            val pagination = body["pagination"]!!.jsonObject

            assertTrue(data.size >= 2) // deposit + withdrawal
            assertEquals(1, pagination["page"]!!.jsonPrimitive.intOrNull ?: 1)
        }
    }

    @Test
    @Order(25)
    fun `test get transactions - pagination`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/transactions?page=1&pageSize=1") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray
            val pagination = body["pagination"]!!.jsonObject

            assertEquals(1, data.size)
            assertEquals(1, pagination["page"]!!.jsonPrimitive.intOrNull ?: 1)
            assertEquals(1, pagination["pageSize"]!!.jsonPrimitive.intOrNull ?: 1)
        }
    }

    @Test
    @Order(26)
    fun `test get transactions - filter by type`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/transactions?type=deposit") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray

            // All transactions should be of type deposit
            data.forEach { tx ->
                assertEquals("deposit", tx.jsonObject["type"]!!.jsonPrimitive.content)
            }
        }
    }

    // ==================== SECURITY TESTS ====================

    @Test
    @Order(27)
    fun `test unauthorized access to balance`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/balance")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    @Order(28)
    fun `test unauthorized access to transactions`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/transactions")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    @Order(29)
    fun `test invalid token format`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/users/me") {
                header(HttpHeaders.Authorization, "Bearer invalid_token_format")
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    @Order(30)
    fun `test root endpoint returns 404`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }
}
