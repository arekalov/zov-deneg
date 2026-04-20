package zov.deneg

import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import zov.deneg.data.*
import zov.deneg.routes.*
import zov.deneg.security.JwtConfig

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val jwtConfig = JwtConfig(environment)
    val dbConfig = configureDatabase()
    val userRepository = UserRepository(dbConfig.database)
    val balanceRepository = BalanceRepository(dbConfig.database)
    val transactionRepository = TransactionRepository(dbConfig.database)
    val portfolioRepository = PortfolioRepository(dbConfig.database)
    val orderRepository = OrderRepository(dbConfig.database)

    configureSerialization()
    configureSecurity(jwtConfig)

    routing {
        configureAuthRoutes(userRepository, jwtConfig)
        configureUserRoutes(userRepository)
        configureBalanceRoutes(balanceRepository, transactionRepository)
        configureTransactionRoutes(transactionRepository)
        configurePortfolioRoutes(portfolioRepository)
        configureOrderRoutes(orderRepository, portfolioRepository, balanceRepository)
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
