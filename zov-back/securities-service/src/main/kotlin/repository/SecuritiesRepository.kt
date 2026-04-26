package zov.deneg.repository

import com.clickhouse.jdbc.ClickHouseDataSource
import zov.deneg.model.*
import java.util.*

class SecuritiesRepository(private val dataSource: ClickHouseDataSource) {

    // ── SECURITIES LIST ──────────────────────────────────────────────────────

    fun getSecurities(
        query: String?,
        type: SecurityType?,
        exchange: Exchange?,
        sector: SecuritySector?,
        page: Int,
        pageSize: Int
    ): Pair<List<Security>, Int> {
        try {
            dataSource.connection.use { conn ->
                // Count total
                val countSql = buildCountQuery(query, type, exchange, sector)
                val totalItems = conn.createStatement().use { stmt ->
                    stmt.executeQuery(countSql).use { rs ->
                        if (rs.next()) rs.getInt(1) else 0
                    }
                }

                // Fetch data
                val dataSql = buildDataQuery(query, type, exchange, sector, page, pageSize)
                val securities = mutableListOf<Security>()

                conn.createStatement().use { stmt ->
                    stmt.executeQuery(dataSql).use { rs ->
                        while (rs.next()) {
                            securities.add(
                                Security(
                                    id = rs.getString("id"),
                                    ticker = rs.getString("ticker"),
                                    name = rs.getString("name"),
                                    description = rs.getString("description"),
                                    type = SecurityType.valueOf(rs.getString("type").uppercase()),
                                    exchange = Exchange.valueOf(rs.getString("exchange")),
                                    sector = mapSector(rs.getString("sector")),
                                    lotSize = rs.getInt("lot_size"),
                                    lastPrice = rs.getBigDecimal("last_price")?.toPlainString() ?: "0.00",
                                    priceChange = rs.getBigDecimal("price_change")?.toPlainString() ?: "0.00",
                                    priceChangePct = rs.getBigDecimal("price_change_pct")?.toPlainString() ?: "0.00"
                                )
                            )
                        }
                    }
                }

                return securities to totalItems
            }
        } catch (e: Exception) {
            println("Error in getSecurities: ${e.message}")
            e.printStackTrace()
            throw e
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

    private fun buildCountQuery(
        query: String?,
        type: SecurityType?,
        exchange: Exchange?,
        sector: SecuritySector?
    ): String {
        val where = buildWhereClause(query, type, exchange, sector)
        return """
            SELECT COUNT(*) FROM (
                SELECT s.id FROM `securities_dict` AS s
                ${if (where.isNotEmpty()) "WHERE $where" else ""}
            )
        """.trimIndent()
    }

    private fun buildDataQuery(
        query: String?,
        type: SecurityType?,
        exchange: Exchange?,
        sector: SecuritySector?,
        page: Int,
        pageSize: Int
    ): String {
        val where = buildWhereClause(query, type, exchange, sector)
        val offset = (page - 1) * pageSize
        return """
            SELECT
                s.id,
                s.ticker,
                s.name,
                s.description,
                s.type,
                s.exchange,
                s.sector,
                s.lot_size,
                sl.last_price,
                sl.last_price - sl.day_open_price AS price_change,
                round((sl.last_price - sl.day_open_price) / NULLIF(sl.day_open_price, 0) * 100, 2) AS price_change_pct
            FROM `securities_dict` AS s
            LEFT JOIN `securities_latest` AS sl ON s.id = sl.security_id
            ${if (where.isNotEmpty()) "WHERE $where" else ""}
            ORDER BY s.ticker
            LIMIT $pageSize OFFSET $offset
        """.trimIndent()
    }

    private fun buildWhereClause(
        query: String?,
        type: SecurityType?,
        exchange: Exchange?,
        sector: SecuritySector?
    ): String {
        val conditions = mutableListOf<String>()
        
        if (!query.isNullOrBlank()) {
            conditions.add("(s.ticker ILIKE '%$query%' OR s.name ILIKE '%$query%')")
        }
        if (type != null) {
            conditions.add("s.type = '${type.name.lowercase()}'")
        }
        if (exchange != null) {
            conditions.add("s.exchange = '${exchange.name}'")
        }
        if (sector != null) {
            val sectorName = when (sector) {
                SecuritySector.FINANCE -> "Финансы"
                SecuritySector.ENERGY -> "Энергетика"
                SecuritySector.METALLURGY -> "Металлургия"
                SecuritySector.TELECOM -> "Телекоммуникации"
                SecuritySector.CONSUMER -> "Потребительский сектор"
                SecuritySector.IT -> "Информационные технологии"
                SecuritySector.TRANSPORT -> "Транспорт"
                SecuritySector.CHEMICAL -> "Химическая промышленность"
                SecuritySector.CONSTRUCTION -> "Строительство"
                SecuritySector.OTHER -> "Другое"
            }
            conditions.add("s.sector = '$sectorName'")
        }
        
        return if (conditions.isNotEmpty()) conditions.joinToString(" AND ") else ""
    }

    // ── SECURITY BY ID ──────────────────────────────────────────────────────

    fun getSecurityById(id: UUID): Security? {
        dataSource.connection.use { conn ->
            val sql = """
                SELECT
                    s.id,
                    s.ticker,
                    s.name,
                    s.description,
                    s.type,
                    s.exchange,
                    s.sector,
                    s.lot_size,
                    sl.last_price,
                    sl.last_price - sl.day_open_price AS price_change,
                    round((sl.last_price - sl.day_open_price) / sl.day_open_price * 100, 2) AS price_change_pct
                FROM `securities_dict` AS s
                LEFT JOIN `securities_latest` AS sl ON s.id = sl.security_id
                WHERE s.id = '$id'
            """.trimIndent()

            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    if (rs.next()) {
                        return Security(
                            id = rs.getString("id"),
                            ticker = rs.getString("ticker"),
                            name = rs.getString("name"),
                            description = rs.getString("description"),
                            type = SecurityType.valueOf(rs.getString("type").uppercase()),
                            exchange = Exchange.valueOf(rs.getString("exchange")),
                            sector = mapSector(rs.getString("sector")),
                            lotSize = rs.getInt("lot_size"),
                            lastPrice = rs.getBigDecimal("last_price")?.toPlainString() ?: "0.00",
                            priceChange = rs.getBigDecimal("price_change")?.toPlainString() ?: "0.00",
                            priceChangePct = rs.getBigDecimal("price_change_pct")?.toPlainString() ?: "0.00"
                        )
                    }
                }
            }
            return null
        }
    }

    // ── PRICE HISTORY ──────────────────────────────────────────────────────

    fun getPriceHistory(securityId: UUID, from: Long, to: Long): PriceHistoryResponse? {
        // Get ticker
        val ticker = getTickerById(securityId) ?: return null

        // Calculate step based on interval duration (from and to are in seconds)
        val step = when {
            (to - from) < 86400 -> 60        // < 1 day → 1 min
            (to - from) < 604800 -> 300      // < 7 days → 5 min
            (to - from) < 2592000 -> 3600    // < 30 days → 1 hour
            else -> 86400                    // > 30 days → 1 day
        }

        dataSource.connection.use { conn ->
            val sql = """
                SELECT
                    toUnixTimestamp(
                        toStartOfInterval(timestamp, INTERVAL $step SECOND)
                    ) AS ts,
                    avg(price) AS price
                FROM `quotes`
                WHERE security_id = '$securityId'
                  AND timestamp >= toDateTime64($from, 0, 'UTC')
                  AND timestamp <= toDateTime64($to, 0, 'UTC')
                GROUP BY ts
                ORDER BY ts
            """.trimIndent()

            val points = mutableListOf<PricePoint>()
            conn.createStatement().use { stmt ->
                stmt.executeQuery(sql).use { rs ->
                    while (rs.next()) {
                        points.add(
                            PricePoint(
                                timestamp = rs.getLong("ts"),
                                price = rs.getBigDecimal("price")?.toPlainString() ?: "0.00"
                            )
                        )
                    }
                }
            }

            return PriceHistoryResponse(
                securityId = securityId.toString(),
                ticker = ticker,
                from = from,
                to = to,
                data = points
            )
        }
    }

    private fun getTickerById(id: UUID): String? {
        dataSource.connection.use { conn ->
            conn.createStatement().use { stmt ->
                stmt.executeQuery("SELECT ticker FROM `securities_dict` WHERE id = '$id'").use { rs ->
                    if (rs.next()) return rs.getString("ticker")
                }
            }
        }
        return null
    }

    // ── ORDER BOOK ──────────────────────────────────────────────────────

    fun getOrderBook(securityId: UUID, depth: Int): OrderBookResponse? {
        // Get ticker
        val ticker = getTickerById(securityId) ?: return null

        dataSource.connection.use { conn ->
            // Get latest snapshot
            val latestSql = """
                SELECT max(snapshot_id) AS sid FROM `order_book` WHERE security_id = '$securityId'
            """.trimIndent()

            val latestSnapshotId = conn.createStatement().use { stmt ->
                stmt.executeQuery(latestSql).use { rs ->
                    if (rs.next()) rs.getLong("sid") else return null
                }
            }

            // Get asks and bids
            val orderBookSql = """
                SELECT side, price, quantity, timestamp
                FROM `order_book`
                WHERE security_id = '$securityId'
                  AND snapshot_id = $latestSnapshotId
                ORDER BY
                    side,
                    CASE WHEN side = 'ask' THEN price END ASC,
                    CASE WHEN side = 'bid' THEN price END DESC
                LIMIT $depth BY side
            """.trimIndent()

            val asks = mutableListOf<OrderBookLevel>()
            val bids = mutableListOf<OrderBookLevel>()
            var timestamp = 0L

            conn.createStatement().use { stmt ->
                stmt.executeQuery(orderBookSql).use { rs ->
                    while (rs.next()) {
                        val ts = rs.getTimestamp("timestamp")
                        timestamp = ts?.time ?: 0L
                        val level = OrderBookLevel(
                            price = rs.getBigDecimal("price")?.toPlainString() ?: "0.00",
                            quantity = rs.getLong("quantity").toInt()
                        )
                        when (rs.getString("side")) {
                            "ask" -> asks.add(level)
                            "bid" -> bids.add(level)
                        }
                    }
                }
            }

            // Calculate spread
            val spread = if (asks.isNotEmpty() && bids.isNotEmpty()) {
                val bestAsk = asks.minOf { it.price.toBigDecimal() }
                val bestBid = bids.maxOf { it.price.toBigDecimal() }
                (bestAsk - bestBid).toPlainString()
            } else {
                "0.00"
            }

            return OrderBookResponse(
                securityId = securityId.toString(),
                ticker = ticker,
                timestamp = timestamp / 1000, // Convert to seconds
                asks = asks,
                bids = bids,
                spread = spread
            )
        }
    }
}
