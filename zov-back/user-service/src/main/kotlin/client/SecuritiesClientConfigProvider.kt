package zov.deneg.client

import io.ktor.server.application.*

/**
 * Securities service client configuration loaded from application.conf and environment variables
 */
class SecuritiesClientConfigProvider(private val environment: ApplicationEnvironment) {

    val baseUrl: String =
        System.getenv("SECURITIES_SERVICE_URL")
            ?: environment.config.property("securities-service.baseUrl").getString()

    val connectTimeout: Int =
        System.getenv("SECURITIES_SERVICE_CONNECT_TIMEOUT")
            ?.toIntOrNull()
            ?: environment.config.propertyOrNull("securities-service.connectTimeout")?.getString()?.toIntOrNull()
            ?: throw IllegalStateException("securities-service.connectTimeout must be configured")

    val requestTimeout: Int =
        System.getenv("SECURITIES_SERVICE_REQUEST_TIMEOUT")
            ?.toIntOrNull()
            ?: environment.config.propertyOrNull("securities-service.requestTimeout")?.getString()?.toIntOrNull()
            ?: throw IllegalStateException("securities-service.requestTimeout must be configured")

    val maxConnections: Int =
        System.getenv("SECURITIES_SERVICE_MAX_CONNECTIONS")
            ?.toIntOrNull()
            ?: environment.config.propertyOrNull("securities-service.maxConnections")?.getString()?.toIntOrNull()
            ?: throw IllegalStateException("securities-service.maxConnections must be configured")
}
