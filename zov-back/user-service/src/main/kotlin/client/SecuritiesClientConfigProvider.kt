package zov.deneg.client

import io.ktor.server.application.*

/**
 * Securities service client configuration loaded from application.yaml and environment variables
 */
class SecuritiesClientConfigProvider(private val environment: ApplicationEnvironment) {

    val baseUrl: String = 
        System.getenv("SECURITIES_SERVICE_URL") 
            ?: environment.config.property("securities-service.baseUrl").getString()
    
    val connectTimeout: Int = 
        System.getenv("SECURITIES_SERVICE_CONNECT_TIMEOUT")
            ?.toIntOrNull() 
            ?: environment.config.propertyOrNull("securities-service.connectTimeout")?.getString()?.toIntOrNull() 
            ?: 5000
    
    val requestTimeout: Int = 
        System.getenv("SECURITIES_SERVICE_REQUEST_TIMEOUT")
            ?.toIntOrNull() 
            ?: environment.config.propertyOrNull("securities-service.requestTimeout")?.getString()?.toIntOrNull() 
            ?: 10000
    
    val maxConnections: Int = 
        System.getenv("SECURITIES_SERVICE_MAX_CONNECTIONS")
            ?.toIntOrNull() 
            ?: environment.config.propertyOrNull("securities-service.maxConnections")?.getString()?.toIntOrNull() 
            ?: 10
}
