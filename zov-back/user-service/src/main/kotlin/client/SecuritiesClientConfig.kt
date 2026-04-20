package zov.deneg.client

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Configuration for the Securities Service client
 */
data class SecuritiesClientConfig(
    val baseUrl: String,
    val connectTimeout: Int = 5000,
    val requestTimeout: Int = 10000,
    val maxConnections: Int = 10
)

/**
 * Creates and configures an HTTP client for calling the Securities Service
 */
fun createSecuritiesClient(config: SecuritiesClientConfig): HttpClient {
    return HttpClient(CIO) {
        engine {
            maxConnectionsCount = config.maxConnections
        }

        install(ContentNegotiation) {
            json(Json {
                prettyPrint = false
                isLenient = true
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
        }
    }
}
