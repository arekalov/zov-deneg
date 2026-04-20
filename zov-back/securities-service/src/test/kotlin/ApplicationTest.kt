package zov.deneg

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRoot() = testApplication {
        environment {
            config = MapApplicationConfig(
                "jwt.secret" to "test-secret-key-for-testing-purposes-only-min-32-chars",
                "jwt.issuer" to "zov-deneg-test",
                "jwt.audience" to "zov-deneg-test",
                "jwt.realm" to "test realm",
                "clickhouse.url" to "jdbc:clickhouse://localhost:8123",
                "clickhouse.database" to "test",
                "clickhouse.username" to "default",
                "clickhouse.password" to "",
                "clickhouse.driver" to "com.clickhouse.jdbc.ClickHouseDriver",
                "clickhouse.pool.max-size" to "5",
                "clickhouse.pool.min-idle" to "1",
                "clickhouse.pool.connection-timeout" to "10000",
                "clickhouse.pool.idle-timeout" to "300000",
                "clickhouse.pool.max-lifetime" to "900000"
            )
        }
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
        }
    }

}
