package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.TransactionsListRemoteDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

internal class ZovTransactionsApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getTransactionsPage(
        page: Int,
        pageSize: Int,
        type: String?,
    ): TransactionsListRemoteDto =
        client.get(ZovApiPaths.TRANSACTIONS) {
            parameter("page", page)
            parameter("pageSize", pageSize)
            if (!type.isNullOrBlank()) {
                parameter("type", type)
            }
        }.body()
}
