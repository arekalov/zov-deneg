package zov.deneg.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import zov.deneg.models.Transaction
import zov.deneg.models.TransactionType
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class TransactionRepository(private val database: Database) {

    fun create(
        userId: UUID,
        type: TransactionType,
        amount: BigDecimal,
        securityId: UUID? = null,
        ticker: String? = null,
        securityName: String? = null,
        quantity: Int? = null,
        price: BigDecimal? = null,
        commission: BigDecimal? = null,
        orderId: UUID? = null
    ): Transaction? = transaction(database) {
        val inserted = TransactionsTable.insert {
            it[TransactionsTable.userId] = userId
            it[TransactionsTable.type] = type.name.lowercase()
            it[TransactionsTable.amount] = amount
            if (securityId != null) it[TransactionsTable.securityId] = securityId
            if (ticker != null) it[TransactionsTable.ticker] = ticker
            if (securityName != null) it[TransactionsTable.securityName] = securityName
            if (quantity != null) it[TransactionsTable.quantity] = quantity
            if (price != null) it[TransactionsTable.price] = price
            if (commission != null) it[TransactionsTable.commission] = commission
            if (orderId != null) it[TransactionsTable.orderId] = orderId
            it[TransactionsTable.createdAt] = Instant.now()
        }

        inserted.resultedValues?.firstOrNull()?.toTransaction()
    }

    fun findById(id: UUID): Transaction? = transaction(database) {
        TransactionsTable.selectAll()
            .where { TransactionsTable.id eq id }
            .singleOrNull()
            ?.toTransaction()
    }

    fun findByUserId(
        userId: UUID,
        page: Int = 1,
        pageSize: Int = 20,
        type: TransactionType? = null,
        securityId: UUID? = null
    ): Pair<List<Transaction>, Int> = transaction(database) {
        val conditions = mutableListOf<Op<Boolean>>(TransactionsTable.userId eq userId)

        if (type != null) {
            conditions.add(TransactionsTable.type eq type.name.lowercase())
        }
        if (securityId != null) {
            conditions.add(TransactionsTable.securityId eq securityId)
        }

        val query = TransactionsTable.selectAll().where { conditions.reduce { acc, op -> acc and op } }

        // Count total
        val total = query.count().toInt()

        // Apply pagination
        val offset = (page - 1) * pageSize
        val pagedQuery = query.orderBy(TransactionsTable.createdAt to SortOrder.DESC).limit(pageSize, offset.toLong())

        pagedQuery.map { it.toTransaction() } to total
    }
}

private fun ResultRow.toTransaction(): Transaction {
    return Transaction(
        id = this[TransactionsTable.id].toString(),
        type = TransactionType.valueOf(this[TransactionsTable.type].uppercase()),
        securityId = this[TransactionsTable.securityId]?.toString(),
        ticker = this[TransactionsTable.ticker],
        securityName = this[TransactionsTable.securityName],
        quantity = this[TransactionsTable.quantity],
        price = this[TransactionsTable.price]?.toPlainString(),
        amount = this[TransactionsTable.amount].toPlainString(),
        commission = this[TransactionsTable.commission]?.toPlainString(),
        orderId = this[TransactionsTable.orderId]?.toString(),
        createdAt = this[TransactionsTable.createdAt].epochSecond
    )
}
