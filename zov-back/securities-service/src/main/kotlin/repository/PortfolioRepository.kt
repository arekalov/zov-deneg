package zov.deneg.repository

import com.clickhouse.jdbc.ClickHouseDataSource
import zov.deneg.model.*
import java.math.BigDecimal
import java.util.*

class PortfolioRepository(private val dataSource: ClickHouseDataSource) {

    fun getPortfolio(userId: UUID): Portfolio {
        // This is a simplified implementation
        // In production, this would fetch positions from the database
        // and calculate the portfolio value based on current prices

        dataSource.connection.use { conn ->
            // Get user positions (simplified - would need a positions table)
            val positionsSql = """
                SELECT 
                    p.security_id,
                    p.quantity,
                    p.average_price,
                    s.ticker,
                    s.name,
                    s.type,
                    s.exchange,
                    s.sector,
                    s.lot_size,
                    sl.last_price as current_price
                FROM positions p
                JOIN securities_dict s ON p.security_id = s.id
                LEFT JOIN securities_latest sl ON p.security_id = sl.security_id
                WHERE p.user_id = '$userId'
            """.trimIndent()

            val items = mutableListOf<PortfolioItem>()
            var securitiesValue = BigDecimal.ZERO
            var totalProfitLoss = BigDecimal.ZERO

            try {
                conn.createStatement().use { stmt ->
                    stmt.executeQuery(positionsSql).use { rs ->
                        while (rs.next()) {
                            val securityId = rs.getString("security_id")
                            val quantity = rs.getInt("quantity")
                            val averagePrice = rs.getBigDecimal("average_price") ?: BigDecimal.ZERO
                            val currentPrice = rs.getBigDecimal("current_price") ?: BigDecimal.ZERO

                            val currentValue = currentPrice.multiply(BigDecimal.valueOf(quantity.toLong()))
                            val costBasis = averagePrice.multiply(BigDecimal.valueOf(quantity.toLong()))
                            val profitLoss = currentValue.subtract(costBasis)
                            val profitLossPct = if (costBasis > BigDecimal.ZERO) {
                                profitLoss.divide(costBasis, 4, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                            } else {
                                BigDecimal.ZERO
                            }

                            val security = Security(
                                id = securityId,
                                ticker = rs.getString("ticker"),
                                name = rs.getString("name"),
                                description = rs.getString("description"),
                                type = SecurityType.valueOf(rs.getString("type").uppercase()),
                                exchange = Exchange.valueOf(rs.getString("exchange")),
                                sector = mapSector(rs.getString("sector")),
                                lotSize = rs.getInt("lot_size"),
                                lastPrice = currentPrice.toPlainString(),
                                priceChange = "0.00",
                                priceChangePct = "0.00"
                            )

                            items.add(
                                PortfolioItem(
                                    securityId = securityId,
                                    security = security,
                                    quantity = quantity,
                                    averagePrice = averagePrice.toPlainString(),
                                    currentPrice = currentPrice.toPlainString(),
                                    currentValue = currentValue.toPlainString(),
                                    profitLoss = profitLoss.toPlainString(),
                                    profitLossPct = profitLossPct.toPlainString()
                                )
                            )

                            securitiesValue = securitiesValue.add(currentValue)
                            totalProfitLoss = totalProfitLoss.add(profitLoss)
                        }
                    }
                }
            } catch (e: Exception) {
                // Table might not exist yet, return empty portfolio
                println("Error fetching positions: ${e.message}")
            }

            // Get cash balance (would need to fetch from user service or cache)
            val cashBalance = "0.00"

            val totalValue = securitiesValue.add(BigDecimal(cashBalance))
            val dailyChange = BigDecimal.ZERO // Would need historical data

            return Portfolio(
                totalValue = totalValue.toPlainString(),
                securitiesValue = securitiesValue.toPlainString(),
                cashBalance = cashBalance,
                dailyChange = dailyChange.toPlainString(),
                dailyChangePct = "0.00",
                totalProfitLoss = totalProfitLoss.toPlainString(),
                items = items
            )
        }
    }

    fun getPortfolioSummary(userId: UUID): PortfolioSummary {
        val portfolio = getPortfolio(userId)
        return PortfolioSummary(
            totalValue = portfolio.totalValue,
            profitLoss = portfolio.totalProfitLoss,
            profitLossPct = calculateTotalProfitLossPct(portfolio)
        )
    }

    private fun calculateTotalProfitLossPct(portfolio: Portfolio): String {
        if (portfolio.items.isEmpty()) return "0.00"

        var totalCost = BigDecimal.ZERO
        var totalValue = BigDecimal.ZERO

        for (item in portfolio.items) {
            val avgPrice = BigDecimal(item.averagePrice)
            val currentPrice = BigDecimal(item.currentPrice)
            val qty = BigDecimal(item.quantity)

            totalCost = totalCost.add(avgPrice.multiply(qty))
            totalValue = totalValue.add(currentPrice.multiply(qty))
        }

        return if (totalCost > BigDecimal.ZERO) {
            totalValue.subtract(totalCost)
                .divide(totalCost, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .toPlainString()
        } else {
            "0.00"
        }
    }

    private fun mapSector(sectorDb: String): SecuritySector {
        return when (sectorDb) {
            "Финансы" -> SecuritySector.FINANCE
            "Энергетика" -> SecuritySector.ENERGY
            "Металлургия" -> SecuritySector.METALLURGY
            "Телекоммуникации" -> SecuritySector.TELECOM
            "Потребительский сектор" -> SecuritySector.CONSUMER
            "Информационные технологии" -> SecuritySector.IT
            "Транспорт" -> SecuritySector.TRANSPORT
            "Химическая промышленность" -> SecuritySector.CHEMICAL
            "Строительство" -> SecuritySector.CONSTRUCTION
            else -> SecuritySector.OTHER
        }
    }
}
