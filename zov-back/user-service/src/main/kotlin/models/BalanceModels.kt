package zov.deneg.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

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
