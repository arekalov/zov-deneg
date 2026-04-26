package zov.deneg.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import zov.deneg.models.*
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class OrderRepository(private val database: Database) {

    /**
     * Create a new order
     */
    fun create(
        userId: UUID,
        securityId: UUID,
        ticker: String,
        type: OrderType,
        side: OrderSide,
        quantity: Int
    ): Order? = transaction(database) {
        val inserted = OrdersTable.insert {
            it[OrdersTable.userId] = userId
            it[OrdersTable.securityId] = securityId
            it[OrdersTable.ticker] = ticker
            it[OrdersTable.type] = type.name.lowercase()
            it[OrdersTable.side] = side.name.lowercase()
            it[OrdersTable.status] = OrderStatus.PENDING.name.lowercase()
            it[OrdersTable.quantity] = quantity
            it[OrdersTable.createdAt] = Instant.now()
            it[OrdersTable.updatedAt] = Instant.now()
        }

        inserted.resultedValues?.firstOrNull()?.toOrder()
    }

    /**
     * Get order by ID
     */
    fun findById(id: UUID): Order? = transaction(database) {
        OrdersTable.selectAll()
            .where { OrdersTable.id eq id }
            .singleOrNull()
            ?.toOrder()
    }

    /**
     * Get order by ID and user ID (for access control)
     */
    fun findByIdAndUserId(id: UUID, userId: UUID): Order? = transaction(database) {
        OrdersTable.selectAll()
            .where { (OrdersTable.id eq id) and (OrdersTable.userId eq userId) }
            .singleOrNull()
            ?.toOrder()
    }

    /**
     * Get all orders for a user with pagination and filters
     */
    fun findByUserId(
        userId: UUID,
        page: Int = 1,
        pageSize: Int = 20,
        status: OrderStatus? = null,
        side: OrderSide? = null,
        securityId: UUID? = null
    ): Pair<List<Order>, Int> = transaction(database) {
        val conditions = mutableListOf<Op<Boolean>>(OrdersTable.userId eq userId)

        if (status != null) {
            conditions.add(OrdersTable.status eq status.name.lowercase())
        }
        if (side != null) {
            conditions.add(OrdersTable.side eq side.name.lowercase())
        }
        if (securityId != null) {
            conditions.add(OrdersTable.securityId eq securityId)
        }

        val query = OrdersTable.selectAll().where { conditions.reduce { acc, op -> acc and op } }

        // Count total
        val total = query.count().toInt()

        // Apply pagination and ordering
        val offset = (page - 1) * pageSize
        val pagedQuery = query.orderBy(OrdersTable.createdAt to SortOrder.DESC).limit(pageSize, offset.toLong())

        pagedQuery.map { it.toOrder() } to total
    }

    /**
     * Update order status
     */
    fun updateStatus(id: UUID, status: OrderStatus): Order? = transaction(database) {
        OrdersTable.update({ OrdersTable.id eq id }) {
            it[OrdersTable.status] = status.name.lowercase()
            it[OrdersTable.updatedAt] = Instant.now()
        }

        findById(id)
    }

    /**
     * Execute order (called after trade execution)
     */
    fun execute(
        id: UUID,
        executedPrice: BigDecimal,
        executedQuantity: Int,
        totalAmount: BigDecimal,
        commission: BigDecimal? = null
    ): Order? = transaction(database) {
        OrdersTable.update({ OrdersTable.id eq id }) {
            it[OrdersTable.executedPrice] = executedPrice
            it[OrdersTable.executedQuantity] = executedQuantity
            it[OrdersTable.totalAmount] = totalAmount
            it[OrdersTable.commission] = commission
            it[OrdersTable.status] = OrderStatus.EXECUTED.name.lowercase()
            it[OrdersTable.updatedAt] = Instant.now()
        }

        findById(id)
    }

    /**
     * Partially execute order
     */
    fun partialExecute(
        id: UUID,
        executedPrice: BigDecimal,
        executedQuantity: Int,
        totalAmount: BigDecimal,
        commission: BigDecimal? = null
    ): Order? = transaction(database) {
        val existing = OrdersTable.selectAll()
            .where { OrdersTable.id eq id }
            .singleOrNull() ?: return@transaction null

        val currentExecutedQty = existing[OrdersTable.executedQuantity] ?: 0
        val newExecutedQty = currentExecutedQty + executedQuantity
        val totalQty = existing[OrdersTable.quantity]

        val newStatus = if (newExecutedQty >= totalQty) {
            OrderStatus.EXECUTED
        } else {
            OrderStatus.PARTIAL
        }

        OrdersTable.update({ OrdersTable.id eq id }) {
            it[OrdersTable.executedPrice] = executedPrice
            it[OrdersTable.executedQuantity] = newExecutedQty
            it[OrdersTable.totalAmount] = totalAmount
            it[OrdersTable.commission] = commission
            it[OrdersTable.status] = newStatus.name.lowercase()
            it[OrdersTable.updatedAt] = Instant.now()
        }

        findById(id)
    }

    /**
     * Cancel order
     */
    fun cancel(id: UUID): Order? = transaction(database) {
        val existing = OrdersTable.selectAll()
            .where { OrdersTable.id eq id }
            .singleOrNull() ?: return@transaction null

        // Can only cancel pending orders
        if (existing[OrdersTable.status] != "pending") {
            return@transaction null
        }

        OrdersTable.update({ OrdersTable.id eq id }) {
            it[OrdersTable.status] = OrderStatus.CANCELLED.name.lowercase()
            it[OrdersTable.updatedAt] = Instant.now()
        }

        findById(id)
    }

    /**
     * Reject order
     */
    fun reject(id: UUID, reason: String? = null): Order? = transaction(database) {
        OrdersTable.update({ OrdersTable.id eq id }) {
            it[OrdersTable.status] = OrderStatus.REJECTED.name.lowercase()
            it[OrdersTable.updatedAt] = Instant.now()
        }

        findById(id)
    }

    /**
     * Get pending orders for a user
     */
    fun getPendingOrders(userId: UUID): List<Order> = transaction(database) {
        OrdersTable.selectAll()
            .where { (OrdersTable.userId eq userId) and (OrdersTable.status eq "pending") }
            .orderBy(OrdersTable.createdAt to SortOrder.DESC)
            .map { it.toOrder() }
    }

    /**
     * Delete order (only cancelled or rejected)
     */
    fun delete(id: UUID): Boolean = transaction(database) {
        val existing = OrdersTable.selectAll()
            .where { OrdersTable.id eq id }
            .singleOrNull() ?: return@transaction false

        // Can only delete cancelled or rejected orders
        if (existing[OrdersTable.status] !in listOf("cancelled", "rejected")) {
            return@transaction false
        }

        OrdersTable.deleteWhere { OrdersTable.id eq id } > 0
    }

    /**
     * Delete all orders for a user (cleanup)
     */
    fun deleteAll(userId: UUID): Int = transaction(database) {
        OrdersTable.deleteWhere { OrdersTable.userId eq userId }
    }
}

private fun ResultRow.toOrder(): Order {
    return Order(
        id = this[OrdersTable.id].toString(),
        securityId = this[OrdersTable.securityId].toString(),
        ticker = this[OrdersTable.ticker],
        type = OrderType.valueOf(this[OrdersTable.type].uppercase()),
        side = OrderSide.valueOf(this[OrdersTable.side].uppercase()),
        status = OrderStatus.valueOf(this[OrdersTable.status].uppercase()),
        quantity = this[OrdersTable.quantity],
        executedPrice = this[OrdersTable.executedPrice]?.toPlainString(),
        executedQuantity = this[OrdersTable.executedQuantity],
        totalAmount = this[OrdersTable.totalAmount]?.toPlainString(),
        commission = this[OrdersTable.commission]?.toPlainString(),
        createdAt = this[OrdersTable.createdAt].epochSecond,
        updatedAt = this[OrdersTable.updatedAt].epochSecond
    )
}
