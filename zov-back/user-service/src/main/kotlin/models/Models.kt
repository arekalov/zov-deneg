package zov.deneg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
