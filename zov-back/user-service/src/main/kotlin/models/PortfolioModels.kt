package zov.deneg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Portfolio ──────────────────────────────────────────────────────────────

@Serializable
data class Security(
    val id: String,
    val ticker: String,
    val name: String,
    val description: String? = null,
    val type: SecurityType,
    val exchange: Exchange,
    val sector: SecuritySector,
    val lotSize: Int,
    val lastPrice: String,
    val priceChange: String,
    val priceChangePct: String
)

@Serializable
enum class SecurityType {
    @SerialName("stock")
    STOCK,

    @SerialName("bond")
    BOND,

    @SerialName("etf")
    ETF
}

@Serializable
enum class Exchange {
    @SerialName("MOEX")
    MOEX,

    @SerialName("SPB")
    SPB
}

@Serializable
enum class SecuritySector {
    @SerialName("Финансы")
    FINANCE,

    @SerialName("Энергетика")
    ENERGY,

    @SerialName("Металлургия")
    METALLURGY,

    @SerialName("Телекоммуникации")
    TELECOM,

    @SerialName("Потребительский сектор")
    CONSUMER,

    @SerialName("Информационные технологии")
    IT,

    @SerialName("Транспорт")
    TRANSPORT,

    @SerialName("Химическая промышленность")
    CHEMICAL,

    @SerialName("Строительство")
    CONSTRUCTION,

    @SerialName("Другое")
    OTHER
}

@Serializable
data class PortfolioItem(
    val securityId: String,
    val security: Security,
    val quantity: Int,
    val averagePrice: String,
    val currentPrice: String,
    val currentValue: String,
    val profitLoss: String,
    val profitLossPct: String
)

@Serializable
data class Portfolio(
    val totalValue: String,
    val securitiesValue: String,
    val cashBalance: String,
    val dailyChange: String,
    val dailyChangePct: String,
    val totalProfitLoss: String,
    val items: List<PortfolioItem>
)

@Serializable
data class PortfolioSummary(
    val totalValue: String,
    val profitLoss: String,
    val profitLossPct: String
)
