package com.zovdeneg.app.domain.transactions

import com.zovdeneg.app.domain.PageEnvelope

interface TransactionsRepository {
    suspend fun getTransactionsPage(
        page: Int,
        pageSize: Int,
        type: String?,
    ): Result<PageEnvelope<Transaction>>
}
