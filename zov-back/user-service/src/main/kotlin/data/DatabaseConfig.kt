package zov.deneg.data

import io.ktor.server.application.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

class DatabaseConfig(val database: Database) {

    fun init() {
        transaction(database) {
            SchemaUtils.create(UsersTable, RefreshTokensTable, UserBalancesTable, TransactionsTable)
        }
    }
}

fun Application.configureDatabase(): DatabaseConfig {
    // Read environment variables for Docker deployment
    val dbHost = System.getenv("DB_HOST") ?: "localhost"
    val dbPort = System.getenv("DB_PORT") ?: "5432"
    val dbName = System.getenv("DB_NAME") ?: "zov_deneg_users"
    val dbUser = System.getenv("DB_USER") ?: "postgres"
    val dbPassword = System.getenv("DB_PASSWORD") ?: "postgres"

    val useEmbedded = environment.config.propertyOrNull("database.useEmbedded")?.getString()?.toBoolean() ?: false

    val (driver, url, user, password) = if (useEmbedded) {
        listOf(
            environment.config.property("h2.driver").getString(),
            environment.config.property("h2.url").getString(),
            environment.config.property("h2.user").getString(),
            environment.config.property("h2.password").getString()
        )
    } else {
        listOf(
            "org.postgresql.Driver",
            "jdbc:postgresql://${dbHost}:${dbPort}/${dbName}",
            dbUser,
            dbPassword
        )
    }

    Class.forName(driver)
    val db = Database.connect(url, driver = driver, user = user, password = password)

    val config = DatabaseConfig(db)
    config.init()

    return config
}
