package zov.deneg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
