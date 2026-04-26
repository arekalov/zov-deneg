package zov.deneg.data

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConfig(val database: Database) {

    fun init(dropExisting: Boolean = false) {
        transaction(database) {
            if (dropExisting) {
                SchemaUtils.drop(
                    UsersTable,
                    RefreshTokensTable,
                    UserBalancesTable,
                    TransactionsTable,
                    PortfolioTable,
                    OrdersTable
                )
            }

            SchemaUtils.createMissingTablesAndColumns(
                UsersTable,
                RefreshTokensTable,
                UserBalancesTable,
                TransactionsTable,
                PortfolioTable,
                OrdersTable
            )
        }
    }
}

fun Application.configureDatabase(): DatabaseConfig {
    val useEmbedded = System.getenv("DATABASE_USE_EMBEDDED")?.toBoolean()
        ?: environment.config.propertyOrNull("database.useEmbedded")?.getString()?.toBoolean()
        ?: false

    val (driver, url, user, password) = if (useEmbedded) {
        listOf(
            environment.config.property("h2.driver").getString(),
            environment.config.property("h2.url").getString(),
            environment.config.property("h2.user").getString(),
            environment.config.property("h2.password").getString()
        )
    } else {
        listOf(
            System.getenv("POSTGRES_DRIVER") ?: environment.config.property("postgres.driver").getString(),
            System.getenv("POSTGRES_URL") ?: environment.config.property("postgres.url").getString(),
            System.getenv("POSTGRES_USER") ?: environment.config.property("postgres.user").getString(),
            System.getenv("POSTGRES_PASSWORD") ?: environment.config.property("postgres.password").getString()
        )
    }

    Class.forName(driver)
    val db = Database.connect(url, driver = driver, user = user, password = password)

    val config = DatabaseConfig(db)
    config.init(dropExisting = useEmbedded)

    return config
}
