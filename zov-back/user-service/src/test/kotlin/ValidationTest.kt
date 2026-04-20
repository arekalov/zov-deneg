package zov.deneg

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.*
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for input validation across all endpoints
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class ValidationTest {

    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:15-alpine").apply {
            withDatabaseName("test_validation")
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

    private lateinit var accessToken: String

    private fun createTestConfig(): MapApplicationConfig = MapApplicationConfig(
        "jwt.secret" to "test-secret-key-for-validation-tests-min-32-chars",
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

    // ==================== REGISTRATION VALIDATION ====================

    @Test
    fun `test register - blank first name`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "",
                    "lastName" to "Test",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test register - long first name`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "A".repeat(51),
                    "lastName" to "Test",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test register - invalid email formats`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val invalidEmails = listOf(
                "plainaddress",
                "@missinglocal.com",
                "missing@.com",
                "missing@domain",
                "spaces in@email.com",
                "double@@domain.com"
            )

            for (email in invalidEmails) {
                val response = client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf(
                        "firstName" to "Test",
                        "lastName" to "User",
                        "email" to email,
                        "phone" to "+79001112233",
                        "password" to "password123"
                    ))
                }

                assertEquals(HttpStatusCode.BadRequest, response.status, "Failed for email: $email")
            }
        }
    }

    @Test
    fun `test register - invalid phone formats`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val invalidPhones = listOf(
                "89001112233",      // Should start with +7
                "+7900111223",      // Too short
                "+790011122334",    // Too long
                "+19001112233",     // Wrong country code
                "abc+79001112233",  // Contains letters
                "+7(900)111-22-33"  // Contains special chars
            )

            invalidPhones.forEach { phone ->
                val response = client.post("/auth/register") {
                    contentType(ContentType.Application.Json)
                    setBody(mapOf(
                        "firstName" to "Test",
                        "lastName" to "User",
                        "email" to "test@example.com",
                        "phone" to phone,
                        "password" to "password123"
                    ))
                }

                assertEquals(HttpStatusCode.BadRequest, response.status, "Failed for phone: $phone")
            }
        }
    }

    @Test
    fun `test register - short password`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "1234567"  // 7 chars
                ))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test register - valid phone format`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "valid@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            assertEquals(HttpStatusCode.Created, response.status)
        }
    }

    // ==================== PROFILE UPDATE VALIDATION ====================

    @Test
    fun `test update profile - invalid email`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            // First register a user
            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            // Try to update with invalid email
            val response = client.put("/users/me") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("email" to "invalid-email"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test update profile - invalid phone`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            val response = client.put("/users/me") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("phone" to "89001112233"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test update profile - blank name`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            val response = client.put("/users/me") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("firstName" to ""))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    // ==================== BALANCE VALIDATION ====================

    @Test
    fun `test deposit - invalid amount format`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "not-a-number"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test deposit - negative amount`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "-100.50"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test deposit - zero amount`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "0"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test deposit - amount below minimum`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            val response = client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "0.99"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test deposit - valid amounts`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            val validAmounts = listOf("1", "1.00", "1000.50", "999999.99")

            validAmounts.forEach { amount ->
                val response = client.post("/balance/deposit") {
                    header(HttpHeaders.Authorization, "Bearer $accessToken")
                    contentType(ContentType.Application.Json)
                    setBody(mapOf("amount" to amount))
                }

                assertEquals(HttpStatusCode.OK, response.status, "Failed for amount: $amount")
            }
        }
    }

    @Test
    fun `test withdraw - negative amount`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val registerResponse = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User",
                    "email" to "test@example.com",
                    "phone" to "+79001112233",
                    "password" to "password123"
                ))
            }

            val body = json.parseToJsonElement(registerResponse.bodyAsText()).jsonObject
            accessToken = body["tokens"]!!.jsonObject["accessToken"]!!.jsonPrimitive.content

            // First deposit some funds
            client.post("/balance/deposit") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "1000"))
            }

            val response = client.post("/balance/withdraw") {
                header(HttpHeaders.Authorization, "Bearer $accessToken")
                contentType(ContentType.Application.Json)
                setBody(mapOf("amount" to "-100"))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    // ==================== MALFORMED REQUESTS ====================

    @Test
    fun `test register - missing required fields`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "firstName" to "Test",
                    "lastName" to "User"
                    // missing email, phone, password
                ))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test register - invalid JSON`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/register") {
                contentType(ContentType.Application.Json)
                setBody("not valid json")
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test login - missing fields`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(mapOf("phone" to "+79001112233"))
                // missing password
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `test refresh token - missing field`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.post("/auth/token/refresh") {
                contentType(ContentType.Application.Json)
                setBody(mapOf<String, Any>())  // missing refreshToken
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }
}
