package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.BalanceAmountRequestDto
import com.zovdeneg.app.data.remote.dto.BalanceDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

internal class ZovBalanceApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getBalance(): BalanceDto = client.get(ZovApiPaths.BALANCE).body()

    suspend fun deposit(amount: String): BalanceDto =
        client.post(ZovApiPaths.BALANCE_DEPOSIT) {
            setBody(BalanceAmountRequestDto(amount = amount))
        }.body()

    suspend fun withdraw(amount: String): BalanceDto =
        client.post(ZovApiPaths.BALANCE_WITHDRAW) {
            setBody(BalanceAmountRequestDto(amount = amount))
        }.body()
}
