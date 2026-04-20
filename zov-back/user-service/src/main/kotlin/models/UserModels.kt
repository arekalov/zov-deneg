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
