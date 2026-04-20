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

fun Application.configureDatabase(): ClickHouseDataSource {
    val clickhouseConfig = environment.config.config("clickhouse")

    // Get environment variables with defaults from config
    val clickhouseHost = System.getenv("CLICKHOUSE_HOST") 
        ?: clickhouseConfig.propertyOrNull("host")?.getString() 
        ?: "clickhouse"
    val clickhousePort = System.getenv("CLICKHOUSE_PORT_HTTP") 
        ?: clickhouseConfig.propertyOrNull("port")?.getString() 
        ?: "8123"
    val clickhouseDb = System.getenv("CLICKHOUSE_DB") 
        ?: clickhouseConfig.propertyOrNull("database")?.getString() 
        ?: "securities"
    val clickhouseUser = System.getenv("CLICKHOUSE_USER") 
        ?: clickhouseConfig.propertyOrNull("username")?.getString() 
        ?: "default"
    val clickhousePassword = System.getenv("CLICKHOUSE_PASSWORD") 
        ?: clickhouseConfig.propertyOrNull("password")?.getString() 
        ?: ""

    // Use database in URL for v1 driver
    val url = "jdbc:clickhouse://${clickhouseHost}:${clickhousePort}/${clickhouseDb}"
    val username = clickhouseUser
    val password = clickhousePassword
    val driver = clickhouseConfig.property("driver").getString()

    // Get pool config from environment or config
    val poolMaxSize = System.getenv("CLICKHOUSE_POOL_MAX_SIZE")?.toIntOrNull()
        ?: clickhouseConfig.propertyOrNull("pool.max-size")?.getString()?.toIntOrNull()
        ?: 10
    val poolMinIdle = System.getenv("CLICKHOUSE_POOL_MIN_IDLE")?.toIntOrNull()
        ?: clickhouseConfig.propertyOrNull("pool.min-idle")?.getString()?.toIntOrNull()
        ?: 2
    val poolConnectionTimeout = System.getenv("CLICKHOUSE_POOL_CONNECTION_TIMEOUT")?.toLongOrNull()
        ?: clickhouseConfig.propertyOrNull("pool.connection-timeout")?.getString()?.toLongOrNull()
        ?: 30000
    val poolIdleTimeout = System.getenv("CLICKHOUSE_POOL_IDLE_TIMEOUT")?.toLongOrNull()
        ?: clickhouseConfig.propertyOrNull("pool.idle-timeout")?.getString()?.toLongOrNull()
        ?: 600000
    val poolMaxLifetime = System.getenv("CLICKHOUSE_POOL_MAX_LIFETIME")?.toLongOrNull()
        ?: clickhouseConfig.propertyOrNull("pool.max-lifetime")?.getString()?.toLongOrNull()
        ?: 1800000

    val poolConfig = PoolConfig(
        maxSize = poolMaxSize,
        minIdle = poolMinIdle,
        connectionTimeout = poolConnectionTimeout,
        idleTimeout = poolIdleTimeout,
        maxLifetime = poolMaxLifetime
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

    return dataSource
}
