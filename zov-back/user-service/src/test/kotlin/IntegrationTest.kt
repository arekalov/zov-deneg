package zov.deneg

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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
        "h2.url" to postgres.jdbcUrl,
        "h2.user" to postgres.username,
        "h2.password" to postgres.password
    )

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Вспомогательная: регистрация пользователя и получение токенов.
     * Возвращает Triple(userId, accessToken, refreshToken)
     */
    private suspend fun HttpClient.registerAndGetTokens(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String
    ): Triple<String, String, String> {
        val response = post("/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "firstName" to firstName,
                "lastName" to lastName,
                "email" to email,
                "phone" to phone,
                "password" to password
            ))
        }
        assertEquals(HttpStatusCode.Created, response.status, "Registration failed: ${response.bodyAsText()}")
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val user = body["user"]!!.jsonObject
        val tokens = body["tokens"]!!.jsonObject
        return Triple(
            user["id"]!!.jsonPrimitive.content,
            tokens["accessToken"]!!.jsonPrimitive.content,
            tokens["refreshToken"]!!.jsonPrimitive.content
        )
    }

    /**
     * Вспомогательная: логин и получение токенов.
     * Возвращает Pair(accessToken, refreshToken)
     */
    private suspend fun HttpClient.loginAndGetTokens(
        phone: String,
        password: String
    ): Pair<String, String> {
        val response = post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(mapOf(
                "phone" to phone,
                "password" to password
            ))
        }
        assertEquals(HttpStatusCode.OK, response.status, "Login failed: ${response.bodyAsText()}")
        val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
        val tokens = body["tokens"]!!.jsonObject
        return Pair(
            tokens["accessToken"]!!.jsonPrimitive.content,
            tokens["refreshToken"]!!.jsonPrimitive.content
        )
    }

    // ==================== AUTHENTICATION TESTS ====================

    @Test
    @Order(1)
    fun `test register user - success`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Иван",
                    "lastName" to "Иванов",
                    "email" to "ivan@test.com",
                    "phone" to "+79001112233",
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
            assertEquals("+79001112233", user["phone"]!!.jsonPrimitive.content)
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
            val client = jsonClient()

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Петр",
                    "lastName" to "Петров",
                    "email" to "ivan@test.com",
                    "phone" to "+79002223344",
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
            val client = jsonClient()

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Петр",
                    "lastName" to "Петров",
                    "email" to "petr@test.com",
                    "phone" to "+79001112233",
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
            val client = jsonClient()

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Тест",
                    "lastName" to "Тестов",
                    "email" to "invalid-email",
                    "phone" to "+79003334455",
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
            val client = jsonClient()

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Тест",
                    "lastName" to "Тестов",
                    "email" to "short@test.com",
                    "phone" to "+79004445566",
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
            val client = jsonClient()

            // Пользователь уже создан в @Order(1), данные в БД сохранились.
            // Но токен мог устареть, поэтому перелогиниваемся.
            val response = client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("user"))
            assertTrue(body.containsKey("tokens"))

            // Обновляем токены для последующих тестов
            val tokens = body["tokens"]!!.jsonObject
            userAccessToken = tokens["accessToken"]!!.jsonPrimitive.content
            userRefreshToken = tokens["refreshToken"]!!.jsonPrimitive.content
        }
    }

    @Test
    @Order(7)
    fun `test login - wrong password`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()

            val response = client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "phone" to "+79001112233",
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
            val client = jsonClient()

            val response = client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "phone" to "+79999999999",
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
            val client = jsonClient()

            // Убеждаемся, что refreshToken инициализирован
            ensureUserLoggedIn(client)

            val response = client.post("/auth/token/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("refreshToken" to userRefreshToken))
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("accessToken"))
            assertTrue(body.containsKey("refreshToken"))

            // Обновляем токены
            userAccessToken = body["accessToken"]!!.jsonPrimitive.content
            userRefreshToken = body["refreshToken"]!!.jsonPrimitive.content
        }
    }

    @Test
    @Order(10)
    fun `test refresh token - invalid token`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/users/me") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("firstName"))
            assertTrue(body.containsKey("lastName"))
        }
    }

    @Test
    @Order(12)
    fun `test update current user profile`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Admin",
                    "lastName" to "Adminov",
                    "email" to "admin@test.com",
                    "phone" to "+79005556677",
                    "password" to "admin12345"
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
            val client = jsonClient()
            ensureAdminLoggedIn(client)

            val response = client.get("/users") {
                header(HttpHeaders.Authorization, "Bearer $adminAccessToken")
            }

            assertEquals(HttpStatusCode.Forbidden, response.status)
        }
    }

    // ==================== BALANCE TESTS ====================

    @Test
    @Order(17)
    fun `test get balance - initial zero`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

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
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/transactions?type=deposit") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray

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
            val client = jsonClient()

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
            val client = jsonClient()

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
            val client = jsonClient()

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
            val client = jsonClient()

            val response = client.get("/")

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    // ==================== PORTFOLIO TESTS ====================

    @Test
    @Order(31)
    fun `test get portfolio - empty`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/portfolio") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("0.00", body["totalValue"]!!.jsonPrimitive.content)
            assertEquals("0.00", body["securitiesValue"]!!.jsonPrimitive.content)
            val items = body["items"]!!.jsonArray
            assertTrue(items.isEmpty())
        }
    }

    @Test
    @Order(32)
    fun `test get portfolio summary - empty`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/portfolio/summary") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("0.00", body["totalValue"]!!.jsonPrimitive.content)
            assertEquals("0.00", body["profitLoss"]!!.jsonPrimitive.content)
        }
    }

    // ==================== ORDER TESTS ====================

    @Test
    @Order(33)
    fun `test create order - success`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            // Используем buildJsonObject чтобы избежать проблемы с разными типами в Map
            val orderBody = buildJsonObject {
                put("securityId", "550e8400-e29b-41d4-a716-446655440000")
                put("side", "buy")
                put("quantity", 10)
            }

            val response = client.post("/orders") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(orderBody)
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals("pending", body["status"]!!.jsonPrimitive.content)
            assertEquals("buy", body["side"]!!.jsonPrimitive.content)
            assertEquals(10, body["quantity"]!!.jsonPrimitive.intOrNull ?: 0)
        }
    }

    @Test
    @Order(34)
    fun `test create order - invalid quantity`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val orderBody = buildJsonObject {
                put("securityId", "550e8400-e29b-41d4-a716-446655440001")
                put("side", "buy")
                put("quantity", 0)
            }

            val response = client.post("/orders") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
                contentType(ContentType.Application.Json)
                setBody(orderBody)
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    @Order(35)
    fun `test create order - unauthorized`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()

            val orderBody = buildJsonObject {
                put("securityId", "550e8400-e29b-41d4-a716-446655440002")
                put("side", "buy")
                put("quantity", 10)
            }

            val response = client.post("/orders") {
                contentType(ContentType.Application.Json)
                setBody(orderBody)
            }

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }

    @Test
    @Order(36)
    fun `test get orders list`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/orders") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("data"))
            assertTrue(body.containsKey("pagination"))
        }
    }

    @Test
    @Order(37)
    fun `test get orders - filter by status`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/orders?status=pending") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray

            data.forEach { order ->
                assertEquals("pending", order.jsonObject["status"]!!.jsonPrimitive.content)
            }
        }
    }

    @Test
    @Order(38)
    fun `test get orders - filter by side`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/orders?side=buy") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = json.parseToJsonElement(response.bodyAsText()).jsonObject
            val data = body["data"]!!.jsonArray

            data.forEach { order ->
                assertEquals("buy", order.jsonObject["side"]!!.jsonPrimitive.content)
            }
        }
    }

    @Test
    @Order(39)
    fun `test get order by id - not found`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.get("/orders/00000000-0000-0000-0000-000000000000") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    @Test
    @Order(40)
    fun `test cancel order - not found`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            val client = jsonClient()
            ensureUserLoggedIn(client)

            val response = client.delete("/orders/00000000-0000-0000-0000-000000000000") {
                header(HttpHeaders.Authorization, "Bearer $userAccessToken")
            }

            assertEquals(HttpStatusCode.NotFound, response.status)
        }
    }

    // ==================== HELPER METHODS ====================

    /**
     * Гарантирует, что userAccessToken и userRefreshToken инициализированы.
     * Если нет — выполняет логин.
     */
    private suspend fun ensureUserLoggedIn(client: HttpClient) {
        if (!::userAccessToken.isInitialized) {
            val (token, refresh) = client.loginAndGetTokens("+79001112233", "password123")
            userAccessToken = token
            userRefreshToken = refresh
        }
    }

    /**
     * Гарантирует, что adminAccessToken инициализирован.
     * Если нет — выполняет логин.
     */
    private suspend fun ensureAdminLoggedIn(client: HttpClient) {
        if (!::adminAccessToken.isInitialized) {
            val (token, _) = client.loginAndGetTokens("+79005556677", "admin12345")
            adminAccessToken = token
        }
    }
}

private fun ApplicationTestBuilder.jsonClient() = createClient {
    install(ContentNegotiation) {
        json()
    }
}