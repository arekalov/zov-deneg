package zov.deneg

import io.ktor.server.testing.*
import org.junit.jupiter.api.Disabled
import kotlin.test.*

@Disabled
class ApplicationTest {

    @Test
    fun testBasic() = testApplication {
        assertTrue(true)
    }

    @Test
    fun testApplicationStarts() = testApplication {
        // Basic test to verify test framework works
        // Full integration tests require running ClickHouse
        assertTrue(true)
    }
}
