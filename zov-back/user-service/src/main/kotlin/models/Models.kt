package zov.deneg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── User & Auth ──────────────────────────────────────────────────────────────

@Serializable
enum class UserRole {
    @SerialName("user")
    USER,

    @SerialName("admin")
    ADMIN
}

@Serializable
data class UserProfile(
    val id: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val role: UserRole,
    val isBlocked: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class Tokens(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int = 900
)

@Serializable
data class RegisterRequest(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String
)

@Serializable
data class LoginRequest(
    val phone: String,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refreshToken: String
)

@Serializable
data class UpdateProfileRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null
)

@Serializable
data class UpdateUserRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: UserRole? = null,
    val isBlocked: Boolean? = null
)

@Serializable
data class AuthResponse(
    val user: UserProfile,
    val tokens: Tokens
)

// ── Common ──────────────────────────────────────────────────────────────

@Serializable
data class PaginatedResponse<T>(
    val data: List<T>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int
)

@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)

@Serializable
data class ValidationErrorResponse(
    val code: String,
    val message: String,
    val fields: List<FieldError>
)

@Serializable
data class FieldError(
    val field: String,
    val message: String
)

// ── Balance ──────────────────────────────────────────────────────────────

@Serializable
data class Balance(
    val available: String,
    val total: String,
    val blocked: String
)

@Serializable
data class DepositRequest(
    val amount: String
)

@Serializable
data class WithdrawRequest(
    val amount: String
)

// ── Transactions ──────────────────────────────────────────────────────────────

@Serializable
enum class TransactionType {
    @SerialName("buy")
    BUY,

    @SerialName("sell")
    SELL,

    @SerialName("deposit")
    DEPOSIT,

    @SerialName("withdrawal")
    WITHDRAWAL,

    @SerialName("dividend")
    DIVIDEND
}

@Serializable
data class Transaction(
    val id: String,
    val type: TransactionType,
    val securityId: String? = null,
    val ticker: String? = null,
    val securityName: String? = null,
    val quantity: Int? = null,
    val price: String? = null,
    val amount: String,
    val commission: String? = null,
    val orderId: String? = null,
    val createdAt: Long
)

@Serializable
data class TransactionsListResponse(
    val data: List<Transaction>,
    val pagination: Pagination
)

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

// ── Orders ──────────────────────────────────────────────────────────────

@Serializable
enum class OrderType {
    @SerialName("market")
    MARKET
}

@Serializable
enum class OrderSide {
    @SerialName("buy")
    BUY,

    @SerialName("sell")
    SELL
}

@Serializable
enum class OrderStatus {
    @SerialName("pending")
    PENDING,

    @SerialName("executed")
    EXECUTED,

    @SerialName("partial")
    PARTIAL,

    @SerialName("cancelled")
    CANCELLED,

    @SerialName("rejected")
    REJECTED
}

@Serializable
data class Order(
    val id: String,
    val securityId: String,
    val ticker: String,
    val type: OrderType,
    val side: OrderSide,
    val status: OrderStatus,
    val quantity: Int,
    val executedPrice: String? = null,
    val executedQuantity: Int? = null,
    val totalAmount: String? = null,
    val commission: String? = null,
    val createdAt: Long,
    val updatedAt: Long
)

@Serializable
data class CreateOrderRequest(
    val securityId: String,
    val side: OrderSide,
    val quantity: Int
)

@Serializable
data class OrdersListResponse(
    val data: List<Order>,
    val pagination: Pagination
)
