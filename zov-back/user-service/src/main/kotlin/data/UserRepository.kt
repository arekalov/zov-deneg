@file:Suppress("DEPRECATION")

package zov.deneg.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.transactions.transaction
import zov.deneg.models.UserProfile
import zov.deneg.models.UserRole
import java.time.Instant
import java.util.UUID

class UserRepository(private val database: Database) {

    fun create(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        passwordHash: String,
        role: UserRole = UserRole.USER
    ): UserProfile? = transaction(database) {
        val now = Instant.now()
        val userId = UUID.randomUUID()

        val inserted = UsersTable.insert {
            it[UsersTable.id] = userId
            it[UsersTable.firstName] = firstName
            it[UsersTable.lastName] = lastName
            it[UsersTable.email] = email
            it[UsersTable.phone] = phone
            it[UsersTable.passwordHash] = passwordHash
            it[UsersTable.role] = role.name.lowercase()
            it[UsersTable.isBlocked] = false
            it[UsersTable.createdAt] = now
            it[UsersTable.updatedAt] = now
        }

        inserted.resultedValues?.firstOrNull()?.toUserProfile()
    }

    fun findByEmail(email: String): UserProfile? = transaction(database) {
        UsersTable.selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
            ?.toUserProfile()
    }

    fun findByPhone(phone: String): UserProfile? = transaction(database) {
        UsersTable.selectAll()
            .where { UsersTable.phone eq phone }
            .singleOrNull()
            ?.toUserProfile()
    }

    fun findById(id: UUID): UserProfile? = transaction(database) {
        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toUserProfile()
    }

    fun findByPasswordHash(phone: String): Pair<UserProfile, String>? = transaction(database) {
        UsersTable.selectAll()
            .where { UsersTable.phone eq phone }
            .singleOrNull()
            ?.let { row ->
                row.toUserProfile() to row[UsersTable.passwordHash]
            }
    }

    fun update(
        id: UUID,
        firstName: String? = null,
        lastName: String? = null,
        email: String? = null,
        phone: String? = null,
        role: UserRole? = null,
        isBlocked: Boolean? = null
    ): UserProfile? = transaction(database) {
        val now = Instant.now()

        UsersTable.update({ UsersTable.id eq id }) { stmt ->
            firstName?.let { stmt[UsersTable.firstName] = firstName }
            lastName?.let { stmt[UsersTable.lastName] = lastName }
            email?.let { stmt[UsersTable.email] = email }
            phone?.let { stmt[UsersTable.phone] = phone }
            role?.let { stmt[UsersTable.role] = role.name.lowercase() }
            isBlocked?.let { stmt[UsersTable.isBlocked] = isBlocked }
            stmt[UsersTable.updatedAt] = now
        }

        UsersTable.selectAll()
            .where { UsersTable.id eq id }
            .singleOrNull()
            ?.toUserProfile()
    }

    fun delete(id: UUID): Boolean = transaction(database) {
        UsersTable.deleteWhere { UsersTable.id eq id } > 0
    }

    fun existsByEmail(email: String, excludeId: UUID? = null): Boolean = transaction(database) {
        val row = UsersTable.selectAll()
            .where { UsersTable.email eq email }
            .singleOrNull()
        row?.let {
            excludeId == null || it[UsersTable.id] != excludeId
        } ?: false
    }

    fun existsByPhone(phone: String, excludeId: UUID? = null): Boolean = transaction(database) {
        val row = UsersTable.selectAll()
            .where { UsersTable.phone eq phone }
            .singleOrNull()
        row?.let {
            excludeId == null || it[UsersTable.id] != excludeId
        } ?: false
    }

    fun findAll(
        page: Int = 1,
        pageSize: Int = 20,
        search: String? = null,
        role: UserRole? = null,
        isBlocked: Boolean? = null
    ): Pair<List<UserProfile>, Int> = transaction(database) {
        val conditions = mutableListOf<Op<Boolean>>()

        if (role != null) {
            conditions.add(UsersTable.role eq role.name.lowercase())
        }
        if (isBlocked != null) {
            conditions.add(UsersTable.isBlocked eq isBlocked)
        }

        val query = if (conditions.isEmpty()) {
            UsersTable.selectAll()
        } else {
            UsersTable.selectAll().where { conditions.reduce { acc, op -> acc and op } }
        }

        // Count total
        val total = query.count().toInt()

        // Apply pagination
        val offset = (page - 1) * pageSize
        val pagedQuery = query.limit(pageSize, offset.toLong())
        pagedQuery.map { it.toUserProfile() } to total
    }

    fun saveRefreshToken(userId: UUID, token: String, expiresAt: java.time.Instant): UUID = transaction(database) {
        RefreshTokensTable.insert {
            it[RefreshTokensTable.userId] = userId
            it[RefreshTokensTable.token] = token
            it[RefreshTokensTable.expiresAt] = expiresAt
            it[RefreshTokensTable.isRevoked] = false
        } get RefreshTokensTable.id
    }

    fun findRefreshToken(token: String): RefreshTokenRow? = transaction(database) {
        RefreshTokensTable.selectAll()
            .where { RefreshTokensTable.token eq token }
            .singleOrNull()
            ?.let { row ->
                RefreshTokenRow(
                    id = row[RefreshTokensTable.id],
                    userId = row[RefreshTokensTable.userId],
                    token = row[RefreshTokensTable.token],
                    expiresAt = row[RefreshTokensTable.expiresAt],
                    isRevoked = row[RefreshTokensTable.isRevoked]
                )
            }
    }

    fun revokeRefreshToken(token: String): Boolean = transaction(database) {
        RefreshTokensTable.update({ RefreshTokensTable.token eq token }) {
            it[isRevoked] = true
        } > 0
    }

    fun revokeAllUserTokens(userId: UUID): Int = transaction(database) {
        RefreshTokensTable.update({ RefreshTokensTable.userId eq userId }) {
            it[isRevoked] = true
        }
    }

    fun deleteExpiredTokens(): Int = transaction(database) {
        val now = Instant.now()
        RefreshTokensTable.deleteWhere { RefreshTokensTable.expiresAt lessEq now }
    }
}

data class RefreshTokenRow(
    val id: UUID,
    val userId: UUID,
    val token: String,
    val expiresAt: java.time.Instant,
    val isRevoked: Boolean
)

private fun ResultRow.toUserProfile(): UserProfile {
    return UserProfile(
        id = this[UsersTable.id].toString(),
        firstName = this[UsersTable.firstName],
        lastName = this[UsersTable.lastName],
        email = this[UsersTable.email],
        phone = this[UsersTable.phone],
        role = UserRole.valueOf(this[UsersTable.role].uppercase()),
        isBlocked = this[UsersTable.isBlocked],
        createdAt = this[UsersTable.createdAt].epochSecond,
        updatedAt = this[UsersTable.updatedAt].epochSecond
    )
}
