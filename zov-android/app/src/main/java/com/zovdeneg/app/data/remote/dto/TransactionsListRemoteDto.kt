package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class PaginationRemoteDto(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
)

@Serializable
internal data class TransactionRemoteDto(
    val id: String,
    val type: String,
    val ticker: String? = null,
    val securityName: String? = null,
    val quantity: Int? = null,
    val price: String? = null,
    val amount: String,
    val commission: String? = null,
    val createdAt: Long,
)

@Serializable
internal data class TransactionsListRemoteDto(
    val data: List<TransactionRemoteDto>,
    val pagination: PaginationRemoteDto,
)
