package zov.deneg

import com.clickhouse.jdbc.ClickHouseDataSource
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.util.*
import java.util.Properties

class DatabaseConfig(
    val url: String,
    val database: String,
    val username: String,
    val password: String,
    val driver: String,
    val pool: PoolConfig
)

class PoolConfig(
    val maxSize: Int,
    val minIdle: Int,
    val connectionTimeout: Long,
    val idleTimeout: Long,
    val maxLifetime: Long
)

// Application attribute key for ClickHouse DataSource
val ClickHouseDataSourceKey = AttributeKey<ClickHouseDataSource>("ClickHouseDataSource")

fun Application.configureDatabase() {
    val clickhouseConfig = environment.config.config("clickhouse")

    // Get environment variables with defaults
    val clickhouseHost = System.getenv("CLICKHOUSE_HOST") ?: "clickhouse"
    val clickhousePort = System.getenv("CLICKHOUSE_PORT_HTTP") ?: "8123"
    val clickhouseDb = System.getenv("CLICKHOUSE_DB") ?: "securities"
    val clickhouseUser = System.getenv("CLICKHOUSE_USER") ?: "default"
    val clickhousePassword = System.getenv("CLICKHOUSE_PASSWORD") ?: ""

    // Use database in URL for v1 driver
    val url = "jdbc:clickhouse://${clickhouseHost}:${clickhousePort}/${clickhouseDb}"
    val username = clickhouseUser
    val password = clickhousePassword
    val driver = clickhouseConfig.property("driver").getString()

    val poolConfig = PoolConfig(
        maxSize = clickhouseConfig.property("pool.max-size").getString().toInt(),
        minIdle = clickhouseConfig.property("pool.min-idle").getString().toInt(),
        connectionTimeout = clickhouseConfig.property("pool.connection-timeout").getString().toLong(),
        idleTimeout = clickhouseConfig.property("pool.idle-timeout").getString().toLong(),
        maxLifetime = clickhouseConfig.property("pool.max-lifetime").getString().toLong()
    )

    val config = DatabaseConfig(url, clickhouseDb, username, password, driver, poolConfig)

    val properties = Properties().apply {
        setProperty("user", config.username)
        if (config.password.isNotEmpty()) {
            setProperty("password", config.password)
        }
    }

    val dataSource = ClickHouseDataSource(config.url, properties)

    // Register dataSource as application attribute
    attributes.put(ClickHouseDataSourceKey, dataSource)

    environment.monitor.subscribe(ApplicationStarted) {
        println("ClickHouse database connection established: ${config.url}")
    }

    environment.monitor.subscribe(ApplicationStopPreparing) {
        try {
            println("ClickHouse database connection stopped")
        } catch (e: Exception) {
            println("Error stopping ClickHouse connection: ${e.message}")
        }
    }
}
