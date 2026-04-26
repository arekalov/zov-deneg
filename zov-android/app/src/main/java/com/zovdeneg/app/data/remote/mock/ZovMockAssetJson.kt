package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.ZovJson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

import android.content.Context

import java.util.concurrent.ConcurrentHashMap

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Loads mock API JSON from [android.content.res.AssetManager] under `assets/mock/`.
 */
@Singleton
internal class ZovMockAssetJson @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val assets = context.assets
    private val textCache = ConcurrentHashMap<String, String>()
    private val securityDetailsRoot: JsonObject by lazy {
        ZovJson.parseToJsonElement(read(ZovMockAssetPaths.SECURITY_DETAILS)).jsonObject
    }

    private fun read(path: String): String =
        textCache.getOrPut(path) {
            assets.open(path).bufferedReader(Charsets.UTF_8).use { it.readText() }
        }

    fun portfolioSummary(): String = read(ZovMockAssetPaths.PORTFOLIO_SUMMARY)

    fun portfolio(): String = read(ZovMockAssetPaths.PORTFOLIO_HOLDINGS)

    fun securitiesList(): String = read(ZovMockAssetPaths.SECURITIES_LIST)

    fun transactionsList(): String = read(ZovMockAssetPaths.TRANSACTIONS_LIST)

    fun balance(): String = read(ZovMockAssetPaths.BALANCE)

    fun balanceAfterWithdraw(): String = read(ZovMockAssetPaths.BALANCE_AFTER_WITHDRAW)

    fun userProfile(): String = read(ZovMockAssetPaths.USER_PROFILE)

    /** Сырой JSON из `assets/mock/` (логин, refresh и т.д.). */
    fun mockAssetText(assetPath: String): String = read(assetPath)

    fun pinChangeOk(): String = read(ZovMockAssetPaths.PIN_CHANGE_OK)

    fun orderCreated(): String = read(ZovMockAssetPaths.ORDER_CREATED)

    fun securityOrderBook(navKeyRaw: String): String =
        mockSecurityOrderBook(navKeyRaw, securityDetailsRoot)

    fun securityDetail(tickerRaw: String): String =
        mockSecurityDetail(tickerRaw, securityDetailsRoot)

    fun securityPriceHistory(tickerRaw: String, from: Long, to: Long): String =
        mockSecurityPriceHistory(tickerRaw, from, to, securityDetailsRoot)
}
