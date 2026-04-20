package zov.deneg.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.math.BigDecimal
import java.util.UUID

object UsersTable : Table("users") {
    val id = uuid("id").autoGenerate()
    val firstName = varchar("first_name", 50)
    val lastName = varchar("last_name", 50)
    val email = varchar("email", 255).uniqueIndex()
    val phone = varchar("phone", 20).uniqueIndex()
    val passwordHash = text("password_hash")
    val role = varchar("role", 20).default("user")
    val isBlocked = bool("is_blocked").default(false)
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
}

object RefreshTokensTable : Table("refresh_tokens") {
    val id = uuid("id").autoGenerate()
    val userId = reference("user_id", UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
}

object UserBalancesTable : Table("user_balances") {
    val userId = reference("user_id", UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val available = decimal("available", 19, 4).default(BigDecimal.ZERO)
    val blocked = decimal("blocked", 19, 4).default(BigDecimal.ZERO)
    val updatedAt = timestamp("updated_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(userId)
}

object TransactionsTable : Table("transactions") {
    val id = uuid("id").autoGenerate()
    val userId = reference("user_id", UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val type = varchar("type", 20)
    val securityId = uuid("security_id").nullable()
    val ticker = varchar("ticker", 50).nullable()
    val securityName = varchar("security_name", 255).nullable()
    val quantity = integer("quantity").nullable()
    val price = decimal("price", 19, 4).nullable()
    val amount = decimal("amount", 19, 4)
    val commission = decimal("commission", 19, 4).nullable()
    val orderId = uuid("order_id").nullable()
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }

    override val primaryKey = PrimaryKey(id)
    init {
        index("user_id_type_idx", false, userId, type)
        index("user_id_security_id_idx", false, userId, securityId)
    }
}
