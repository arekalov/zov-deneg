package zov.deneg.data

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import zov.deneg.models.PortfolioItem
import zov.deneg.models.Security
import zov.deneg.models.SecurityType
import zov.deneg.models.Exchange
import zov.deneg.models.SecuritySector
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

class PortfolioRepository(private val database: Database) {

    /**
     * Get or create portfolio item for a user
     */
    fun getOrCreate(userId: UUID, securityId: UUID, ticker: String, securityName: String): PortfolioItem? = transaction(database) {
        val existing = PortfolioTable.selectAll()
            .where { (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) }
            .singleOrNull()

        if (existing == null) {
            // Create new portfolio item with zero quantity
            PortfolioTable.insert {
                it[PortfolioTable.userId] = userId
                it[PortfolioTable.securityId] = securityId
                it[PortfolioTable.ticker] = ticker
                it[PortfolioTable.securityName] = securityName
                it[PortfolioTable.quantity] = 0
                it[PortfolioTable.averagePrice] = BigDecimal.ZERO
                it[PortfolioTable.currentPrice] = BigDecimal.ZERO
                it[PortfolioTable.currentValue] = BigDecimal.ZERO
                it[PortfolioTable.profitLoss] = BigDecimal.ZERO
                it[PortfolioTable.profitLossPct] = BigDecimal.ZERO
                it[PortfolioTable.updatedAt] = Instant.now()
            }
            get(userId, securityId)
        } else {
            existing.toPortfolioItem()
        }
    }

    /**
     * Get portfolio item by user and security ID
     */
    fun get(userId: UUID, securityId: UUID): PortfolioItem? = transaction(database) {
        PortfolioTable.selectAll()
            .where { (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) }
            .singleOrNull()
            ?.toPortfolioItem()
    }

    /**
     * Get all portfolio items for a user
     */
    fun getAll(userId: UUID): List<PortfolioItem> = transaction(database) {
        PortfolioTable.selectAll()
            .where { PortfolioTable.userId eq userId }
            .map { it.toPortfolioItem() }
    }

    /**
     * Update portfolio item after trade execution
     */
    fun updateAfterTrade(
        userId: UUID,
        securityId: UUID,
        ticker: String,
        securityName: String,
        quantity: Int,
        price: BigDecimal,
        isBuy: Boolean
    ): PortfolioItem? = transaction(database) {
        val existing = PortfolioTable.selectAll()
            .where { (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) }
            .singleOrNull()

        if (existing == null) {
            // Create new position
            PortfolioTable.insert {
                it[PortfolioTable.userId] = userId
                it[PortfolioTable.securityId] = securityId
                it[PortfolioTable.ticker] = ticker
                it[PortfolioTable.securityName] = securityName
                it[PortfolioTable.quantity] = quantity
                it[PortfolioTable.averagePrice] = price
                it[PortfolioTable.currentPrice] = price
                it[PortfolioTable.currentValue] = price.multiply(BigDecimal.valueOf(quantity.toLong()))
                it[PortfolioTable.profitLoss] = BigDecimal.ZERO
                it[PortfolioTable.profitLossPct] = BigDecimal.ZERO
                it[PortfolioTable.updatedAt] = Instant.now()
            }
            get(userId, securityId)
        } else {
            val currentQty = existing[PortfolioTable.quantity]
            val avgPrice = existing[PortfolioTable.averagePrice]
            val currentPrice = existing[PortfolioTable.currentPrice]

            val newQty = if (isBuy) {
                currentQty + quantity
            } else {
                currentQty - quantity
            }

            // Calculate new average price (only for buys)
            val newAvgPrice = if (isBuy) {
                val totalCost = avgPrice.multiply(BigDecimal.valueOf(currentQty.toLong()))
                    .add(price.multiply(BigDecimal.valueOf(quantity.toLong())))
                totalCost.divide(BigDecimal.valueOf(newQty.toLong()), 4, java.math.RoundingMode.HALF_UP)
            } else {
                avgPrice
            }

            // Update current price from trade
            val newCurrentPrice = price
            val newCurrentValue = newCurrentPrice.multiply(BigDecimal.valueOf(newQty.toLong()))

            // Calculate profit/loss
            val costBasis = newAvgPrice.multiply(BigDecimal.valueOf(newQty.toLong()))
            val profitLoss = newCurrentValue.subtract(costBasis)
            val profitLossPct = if (costBasis.compareTo(BigDecimal.ZERO) != 0) {
                profitLoss.divide(costBasis, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            } else {
                BigDecimal.ZERO
            }

            if (newQty <= 0) {
                // Close position
                PortfolioTable.deleteWhere { (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) }
                null
            } else {
                PortfolioTable.update({ (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) }) {
                    it[PortfolioTable.quantity] = newQty
                    it[PortfolioTable.averagePrice] = newAvgPrice
                    it[PortfolioTable.currentPrice] = newCurrentPrice
                    it[PortfolioTable.currentValue] = newCurrentValue
                    it[PortfolioTable.profitLoss] = profitLoss
                    it[PortfolioTable.profitLossPct] = profitLossPct
                    it[PortfolioTable.updatedAt] = Instant.now()
                }
                get(userId, securityId)
            }
        }
    }

    /**
     * Update current price (for mark-to-market)
     */
    fun updateCurrentPrice(userId: UUID, securityId: UUID, currentPrice: BigDecimal): PortfolioItem? = transaction(database) {
        val existing = PortfolioTable.selectAll()
            .where { (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) }
            .singleOrNull() ?: return@transaction null

        val quantity = existing[PortfolioTable.quantity]
        val avgPrice = existing[PortfolioTable.averagePrice]

        val currentValue = currentPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
        val costBasis = avgPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
        val profitLoss = currentValue.subtract(costBasis)
        val profitLossPct = if (costBasis.compareTo(BigDecimal.ZERO) != 0) {
            profitLoss.divide(costBasis, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
        } else {
            BigDecimal.ZERO
        }

        PortfolioTable.update({ (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) }) {
            it[PortfolioTable.currentPrice] = currentPrice
            it[PortfolioTable.currentValue] = currentValue
            it[PortfolioTable.profitLoss] = profitLoss
            it[PortfolioTable.profitLossPct] = profitLossPct
            it[PortfolioTable.updatedAt] = Instant.now()
        }

        get(userId, securityId)
    }

    /**
     * Update all portfolio items with current prices from external service
     */
    fun updateAllPrices(userId: UUID, prices: Map<UUID, BigDecimal>) {
        transaction(database) {
            prices.forEach { (securityId, price) ->
                updateCurrentPrice(userId, securityId, price)
            }
        }
    }

    /**
     * Delete portfolio item (close position)
     */
    fun delete(userId: UUID, securityId: UUID): Boolean = transaction(database) {
        PortfolioTable.deleteWhere { (PortfolioTable.userId eq userId) and (PortfolioTable.securityId eq securityId) } > 0
    }

    /**
     * Delete all portfolio items for a user
     */
    fun deleteAll(userId: UUID): Int = transaction(database) {
        PortfolioTable.deleteWhere { PortfolioTable.userId eq userId }
    }
}

private fun ResultRow.toPortfolioItem(): PortfolioItem {
    val security = Security(
        id = this[PortfolioTable.securityId].toString(),
        ticker = this[PortfolioTable.ticker],
        name = this[PortfolioTable.securityName],
        description = null,
        type = SecurityType.STOCK, // Default, should be fetched from securities service
        exchange = Exchange.MOEX, // Default, should be fetched from securities service
        sector = SecuritySector.OTHER, // Default, should be fetched from securities service
        lotSize = 1, // Default, should be fetched from securities service
        lastPrice = this[PortfolioTable.currentPrice].toPlainString(),
        priceChange = "0.00", // Should be calculated from price change
        priceChangePct = "0.00" // Should be calculated from price change
    )

    return PortfolioItem(
        securityId = this[PortfolioTable.securityId].toString(),
        security = security,
        quantity = this[PortfolioTable.quantity],
        averagePrice = this[PortfolioTable.averagePrice].toPlainString(),
        currentPrice = this[PortfolioTable.currentPrice].toPlainString(),
        currentValue = this[PortfolioTable.currentValue].toPlainString(),
        profitLoss = this[PortfolioTable.profitLoss].toPlainString(),
        profitLossPct = this[PortfolioTable.profitLossPct].toPlainString()
    )
}
