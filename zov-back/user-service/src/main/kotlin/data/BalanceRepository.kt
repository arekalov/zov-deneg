package zov.deneg.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal
import java.util.UUID

class BalanceRepository(private val database: Database) {

    fun getBalance(userId: UUID): Balance = transaction(database) {
        val userBalance = UserBalancesTable.selectAll()
            .where { UserBalancesTable.userId eq userId }
            .singleOrNull()

        if (userBalance == null) {
            // Create default balance if not exists
            UserBalancesTable.insert {
                it[UserBalancesTable.userId] = userId
                it[UserBalancesTable.available] = BigDecimal.ZERO
                it[UserBalancesTable.blocked] = BigDecimal.ZERO
            }
            Balance(
                available = "0.00",
                total = "0.00",
                blocked = "0.00"
            )
        } else {
            val available = userBalance[UserBalancesTable.available]
            val blocked = userBalance[UserBalancesTable.blocked]
            Balance(
                available = available.toPlainString(),
                total = available.add(blocked).toPlainString(),
                blocked = blocked.toPlainString()
            )
        }
    }

    fun deposit(userId: UUID, amount: BigDecimal): Balance {
        return transaction(database) {
            val existing = UserBalancesTable.selectAll()
                .where { UserBalancesTable.userId eq userId }
                .singleOrNull()

            if (existing == null) {
                UserBalancesTable.insert {
                    it[UserBalancesTable.userId] = userId
                    it[UserBalancesTable.available] = amount
                    it[UserBalancesTable.blocked] = BigDecimal.ZERO
                }
            } else {
                val newAvailable = existing[UserBalancesTable.available].add(amount)
                UserBalancesTable.update({ UserBalancesTable.userId eq userId }) {
                    it[UserBalancesTable.available] = newAvailable
                }
            }

            getBalance(userId)
        }
    }

    fun withdraw(userId: UUID, amount: BigDecimal): Balance? {
        return transaction(database) {
            val userBalance = UserBalancesTable.selectAll()
                .where { UserBalancesTable.userId eq userId }
                .singleOrNull() ?: return@transaction null

            val available = userBalance[UserBalancesTable.available]
            if (available < amount) {
                return@transaction null
            }

            val newAvailable = available.subtract(amount)
            UserBalancesTable.update({ UserBalancesTable.userId eq userId }) {
                it[UserBalancesTable.available] = newAvailable
            }

            getBalance(userId)
        }
    }

    fun blockFunds(userId: UUID, amount: BigDecimal): Boolean {
        return transaction(database) {
            val userBalance = UserBalancesTable.selectAll()
                .where { UserBalancesTable.userId eq userId }
                .singleOrNull() ?: return@transaction false

            val available = userBalance[UserBalancesTable.available]
            if (available < amount) {
                return@transaction false
            }

            val newAvailable = available.subtract(amount)
            val newBlocked = userBalance[UserBalancesTable.blocked].add(amount)
            
            UserBalancesTable.update({ UserBalancesTable.userId eq userId }) {
                it[UserBalancesTable.available] = newAvailable
                it[UserBalancesTable.blocked] = newBlocked
            }
            true
        }
    }

    fun unblockFunds(userId: UUID, amount: BigDecimal): Boolean {
        return transaction(database) {
            val userBalance = UserBalancesTable.selectAll()
                .where { UserBalancesTable.userId eq userId }
                .singleOrNull() ?: return@transaction false

            val blocked = userBalance[UserBalancesTable.blocked]
            if (blocked < amount) {
                return@transaction false
            }

            val newAvailable = userBalance[UserBalancesTable.available].add(amount)
            val newBlocked = blocked.subtract(amount)
            
            UserBalancesTable.update({ UserBalancesTable.userId eq userId }) {
                it[UserBalancesTable.available] = newAvailable
                it[UserBalancesTable.blocked] = newBlocked
            }
            true
        }
    }
}
