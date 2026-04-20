package zov.deneg.repository

import com.clickhouse.jdbc.ClickHouseDataSource
import zov.deneg.model.*
import java.math.BigDecimal
import java.time.Instant
import java.util.*

class OrderRepository(private val dataSource: ClickHouseDataSource) {

    fun createOrder(
        userId: UUID,
        securityId: UUID,
        ticker: String,
        type: OrderType,
        side: OrderSide,
        quantity: Int
    ): Order {
        val now = Instant.now()
        val orderId = UUID.randomUUID()

        dataSource.connection.use { conn ->
            val sql = """
                INSERT INTO orders (
                    id, user_id, security_id, ticker, type, side, status,
                    quantity, executed_price, executed_quantity, total_amount,
                    commission, created_at, updated_at
                ) VALUES (
                    '$orderId', '$userId', '$securityId', '$ticker',
                    '${type.name.lowercase()}', '${side.name.lowercase()}', 'pending',
                    $quantity, NULL, NULL, NULL, NULL,
                    toDateTime64(${now.toEpochMilli() / 1000.0}, 3, 'UTC'),
                    toDateTime64(${now.toEpochMilli() / 1000.0}, 3, 'UTC')
                )
            """.trimIndent()

            conn.createStatement().use { stmt ->
                stmt.executeUpdate(sql)
            }
        }

        return Order(
            id = orderId.toString(),
            securityId = securityId.toString(),
            ticker = ticker,
            type = type,
            side = side,
            status = OrderStatus.PENDING,
            quantity = quantity,
            executedPrice = null,
            executedQuantity = null,
            totalAmount = null,
            commission = null,
            createdAt = now.epochSecond,
            updatedAt = now.epochSecond
        )
    }

    fun getOrderById(orderId: UUID): Order? {
        dataSource.connection.use { conn ->
            val sql = """
                SELECT * FROM orders WHERE id = '$orderId'
            """.trimIndent()

            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    if (rs.next()) {
                        return rs.toOrder()
                    }
                }
            }
        }
        return null
    }

    fun getOrdersByUserId(
        userId: UUID,
        page: Int = 1,
        pageSize: Int = 20,
        status: OrderStatus? = null,
        side: OrderSide? = null,
        securityId: UUID? = null
    ): Pair<List<Order>, Int> {
        val conditions = mutableListOf("user_id = '$userId'")

        if (status != null) {
            conditions.add("status = '${status.name.lowercase()}'")
        }
        if (side != null) {
            conditions.add("side = '${side.name.lowercase()}'")
        }
        if (securityId != null) {
            conditions.add("security_id = '$securityId'")
        }

        val whereClause = if (conditions.isNotEmpty()) {
            "WHERE " + conditions.joinToString(" AND ")
        } else {
            ""
        }

        dataSource.connection.use { conn ->
            // Count total
            val countSql = """
                SELECT COUNT(*) FROM orders $whereClause
            """.trimIndent()

            val totalItems = conn.createStatement().use { stmt ->
                stmt.executeQuery(countSql).use { rs ->
                    if (rs.next()) rs.getInt(1) else 0
                }
            }

            // Fetch data
            val offset = (page - 1) * pageSize
            val dataSql = """
                SELECT * FROM orders $whereClause
                ORDER BY created_at DESC
                LIMIT $pageSize OFFSET $offset
            """.trimIndent()

            val orders = mutableListOf<Order>()
            conn.createStatement().use { stmt ->
                stmt.executeQuery(dataSql).use { rs ->
                    while (rs.next()) {
                        orders.add(rs.toOrder())
                    }
                }
            }

            return orders to totalItems
        }
    }

    fun updateOrderStatus(
        orderId: UUID,
        status: OrderStatus,
        executedPrice: BigDecimal? = null,
        executedQuantity: Int? = null,
        totalAmount: BigDecimal? = null,
        commission: BigDecimal? = null
    ): Order? {
        val now = Instant.now()

        dataSource.connection.use { conn ->
            val priceClause = executedPrice?.let { ", executed_price = $it" } ?: ""
            val quantityClause = executedQuantity?.let { ", executed_quantity = $it" } ?: ""
            val amountClause = totalAmount?.let { ", total_amount = $it" } ?: ""
            val commissionClause = commission?.let { ", commission = $it" } ?: ""

            val sql = """
                UPDATE orders
                SET status = '${status.name.lowercase()}'$priceClause$quantityClause$amountClause$commissionClause,
                    updated_at = toDateTime64(${now.toEpochMilli() / 1000.0}, 3, 'UTC')
                WHERE id = '$orderId'
            """.trimIndent()

            conn.createStatement().use { stmt ->
                stmt.executeUpdate(sql)
            }
        }

        return getOrderById(orderId)
    }

    fun cancelOrder(orderId: UUID): Boolean {
        dataSource.connection.use { conn ->
            val sql = """
                UPDATE orders
                SET status = 'cancelled',
                    updated_at = toDateTime64(${Instant.now().toEpochMilli() / 1000.0}, 3, 'UTC')
                WHERE id = '$orderId' AND status = 'pending'
            """.trimIndent()

            conn.createStatement().use { stmt ->
                val rows = stmt.executeUpdate(sql)
                return rows > 0
            }
        }
    }

    private fun java.sql.ResultSet.toOrder(): Order {
        return Order(
            id = getString("id"),
            securityId = getString("security_id"),
            ticker = getString("ticker"),
            type = OrderType.valueOf(getString("type").uppercase()),
            side = OrderSide.valueOf(getString("side").uppercase()),
            status = OrderStatus.valueOf(getString("status").uppercase()),
            quantity = getInt("quantity"),
            executedPrice = getBigDecimal("executed_price")?.toPlainString(),
            executedQuantity = getInt("executed_quantity"),
            totalAmount = getBigDecimal("total_amount")?.toPlainString(),
            commission = getBigDecimal("commission")?.toPlainString(),
            createdAt = getTimestamp("created_at")?.time?.div(1000) ?: 0,
            updatedAt = getTimestamp("updated_at")?.time?.div(1000) ?: 0
        )
    }
}
