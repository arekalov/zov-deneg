package zov.deneg.data

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
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
    val userId = uuid("user_id").references(UsersTable.id, onDelete = org.jetbrains.exposed.sql.ReferenceOption.CASCADE)
    val token = varchar("token", 512).uniqueIndex()
    val expiresAt = timestamp("expires_at")
    val isRevoked = bool("is_revoked").default(false)
    val createdAt = timestamp("created_at").clientDefault { java.time.Instant.now() }
    
    override val primaryKey = PrimaryKey(id)
}
