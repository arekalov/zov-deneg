package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.TransactionsEnvelopeDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

import javax.inject.Inject

internal class ZovTransactionsApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getTransactionsEnvelope(): TransactionsEnvelopeDto =
        client.get(ZovApiPaths.TRANSACTIONS).body()
}
