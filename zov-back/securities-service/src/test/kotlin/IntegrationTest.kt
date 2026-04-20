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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.testcontainers.clickhouse.ClickHouseContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.shaded.org.bouncycastle.util.test.SimpleTest.runTest
import java.time.Duration

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class SecuritiesIntegrationTest {

    companion object {
        @Container
        val clickhouse = ClickHouseContainer("clickhouse/clickhouse-server:23-alpine").apply {
            withStartupTimeout(Duration.ofMinutes(2))
        }

        lateinit var jdbcUrl: String
        lateinit var dbUser: String
        lateinit var dbPassword: String

        @JvmStatic
        @BeforeAll
        fun setup() {
            jdbcUrl = clickhouse.getJdbcUrl()
            dbUser = clickhouse.username
            dbPassword = clickhouse.password
        }
    }

    private lateinit var accessToken: String

    private fun createTestConfig(): MapApplicationConfig = MapApplicationConfig(
        "jwt.secret" to "test-secret-key-for-integration-tests-min-32-chars",
        "jwt.audience" to "zov-deneg-securities",
        "jwt.issuer" to "zov-deneg-securities-service",
        "jwt.realm" to "zov-deneg securities service",
        "jwt.accessTokenTtlSeconds" to "900",
        "jwt.refreshTokenTtlDays" to "30",
        "database.useEmbedded" to "false",
        "clickhouse.driver" to "com.clickhouse.jdbc.ClickHouseDriver",
        "clickhouse.url" to jdbcUrl,
        "clickhouse.user" to dbUser,
        "clickhouse.password" to dbPassword
    )

    @Test
    @Order(1)
    fun `test get securities list`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            val response = client.get("/securities") {
                header(HttpHeaders.Authorization, "Bearer test-token")
            }

            // Should return empty list or securities if seeded
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    @Order(2)
    fun `test get security by id`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            val response = client.get("/securities/SBER") {
                header(HttpHeaders.Authorization, "Bearer test-token")
            }//

            // May return 404 if security doesn't exist, or 200 if it does
            assertTrue(response.status in listOf(HttpStatusCode.OK, HttpStatusCode.NotFound))
        }
    }

    @Test
    @Order(3)
    fun `test get portfolio without positions`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            val response = client.get("/portfolio") {
                header(HttpHeaders.Authorization, "Bearer test-token")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("positions"))
            assertTrue(body.containsKey("totalValue"))
        }
    }

    @Test
    @Order(4)
    fun `test create buy order`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            val response = client.post("/orders") {
                header(HttpHeaders.Authorization, "Bearer test-token")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "securityId" to "SBER",
                    "orderType" to "BUY",
                    "quantity" to 10,
                    "price" to "250.50"
                ))
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("orderId"))
            assertEquals("PENDING", body["status"]?.jsonPrimitive?.content)
        }
    }

    @Test
    @Order(5)
    fun `test create sell order`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            val response = client.post("/orders") {
                header(HttpHeaders.Authorization, "Bearer test-token")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "securityId" to "GAZP",
                    "orderType" to "SELL",
                    "quantity" to 5,
                    "price" to "180.00"
                ))
            }

            assertEquals(HttpStatusCode.Created, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("orderId"))
        }
    }

    @Test
    @Order(6)
    fun `test get orders list`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            val response = client.get("/orders") {
                header(HttpHeaders.Authorization, "Bearer test-token")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body.containsKey("data"))
            assertTrue(body.containsKey("pagination"))
        }
    }

    @Test
    @Order(7)
    fun `test get order by id`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            // First create an order
            val createResponse = client.post("/orders") {
                header(HttpHeaders.Authorization, "Bearer test-token")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "securityId" to "LKOH",
                    "orderType" to "BUY",
                    "quantity" to 3,
                    "price" to "2800.00"
                ))
            }

            val orderId = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject["orderId"]?.jsonPrimitive?.content

            // Then get it
            val response = client.get("/orders/$orderId") {
                header(HttpHeaders.Authorization, "Bearer test-token")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertEquals(orderId, body["orderId"]?.jsonPrimitive?.content)
        }
    }

    @Test
    @Order(8)
    fun `test cancel order`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            // First create an order
            val createResponse = client.post("/orders") {
                header(HttpHeaders.Authorization, "Bearer test-token")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "securityId" to "YNDX",
                    "orderType" to "BUY",
                    "quantity" to 2,
                    "price" to "3500.00"
                ))
            }

            val orderId = Json.parseToJsonElement(createResponse.bodyAsText()).jsonObject["orderId"]?.jsonPrimitive?.content

            // Then cancel it
            val response = client.post("/orders/$orderId/cancel") {
                header(HttpHeaders.Authorization, "Bearer test-token")
            }

            assertEquals(HttpStatusCode.OK, response.status)
            val body = Json.parseToJsonElement(response.bodyAsText()).jsonObject
            assertTrue(body["status"]?.jsonPrimitive?.content in listOf("CANCELLED", "PENDING"))
        }
    }

    @Test
    @Order(9)
    fun `test invalid order quantity`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }
            client.install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json { isLenient = true; ignoreUnknownKeys = true })
            }

            val response = client.post("/orders") {
                header(HttpHeaders.Authorization, "Bearer test-token")
                contentType(ContentType.Application.Json)
                setBody(mapOf(
                    "securityId" to "SBER",
                    "orderType" to "BUY",
                    "quantity" to 0,
                    "price" to "250.50"
                ))
            }

            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    @Order(10)
    fun `test unauthorized access`() = runTest {
        testApplication {
            environment { config = createTestConfig() }
            application { module() }

            val response = client.get("/portfolio")

            assertEquals(HttpStatusCode.Unauthorized, response.status)
        }
    }
}
