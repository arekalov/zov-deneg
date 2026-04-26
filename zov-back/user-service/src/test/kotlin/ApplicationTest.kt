package zov.deneg

import io.ktor.server.config.*
import io.ktor.server.testing.*
import kotlin.test.*

class ApplicationTest {

    private val testConfig = MapApplicationConfig(
        "jwt.secret" to "test-secret-key-for-testing-min-32-chars",
        "jwt.audience" to "zov-deneg-users",
        "jwt.issuer" to "zov-deneg-user-service",
        "jwt.realm" to "zov-deneg user service",
        "jwt.accessTokenTtlSeconds" to "900",
        "jwt.refreshTokenTtlDays" to "30",
        "database.useEmbedded" to "true",
        "h2.driver" to "org.h2.Driver",
        "h2.url" to "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "h2.user" to "root",
        "h2.password" to ""
    )

    @Test
    fun testModuleLoads() = testApplication {
        environment {
            config = testConfig
        }
        application {
            module()
        }
        // If we get here without exception, the module loaded successfully
        assertTrue(true)
    }
}
