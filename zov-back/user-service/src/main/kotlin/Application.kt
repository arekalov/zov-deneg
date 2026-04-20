package zov.deneg

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import zov.deneg.data.UserRepository
import zov.deneg.data.configureDatabase
import zov.deneg.routes.configureAuthRoutes
import zov.deneg.routes.configureUserRoutes
import zov.deneg.security.JwtConfig

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val jwtConfig = JwtConfig(environment)
    val dbConfig = configureDatabase()
    val userRepository = UserRepository(dbConfig.database)
    
    configureSerialization()
    configureSecurity(jwtConfig)
    
    routing {
        configureAuthRoutes(userRepository, jwtConfig)
        configureUserRoutes(userRepository)
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
            encodeDefaults = true
        })
    }
}
