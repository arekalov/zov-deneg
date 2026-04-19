package com.zovdeneg.app.data.remote.mock

import android.content.Context
import com.zovdeneg.app.data.remote.ZovJson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put

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

    fun portfolioHoldings(): String = read(ZovMockAssetPaths.PORTFOLIO_HOLDINGS)

    fun popularSecurities(): String = read(ZovMockAssetPaths.POPULAR_SECURITIES)

    fun transactions(): String = read(ZovMockAssetPaths.TRANSACTIONS)

    fun balance(): String = read(ZovMockAssetPaths.BALANCE)

    fun balanceAfterWithdraw(): String = read(ZovMockAssetPaths.BALANCE_AFTER_WITHDRAW)

    fun userProfile(): String = read(ZovMockAssetPaths.USER_PROFILE)

    fun authLoginResponse(): String = read(ZovMockAssetPaths.AUTH_ENVELOPE)

    fun authRegisterResponse(): String = authLoginResponse()

    fun pinChangeOk(): String = read(ZovMockAssetPaths.PIN_CHANGE_OK)

    fun orderCreated(): String = read(ZovMockAssetPaths.ORDER_CREATED)

    fun securityDetail(tickerRaw: String): String {
        val t = tickerRaw.uppercase()
        val root = securityDetailsRoot
        val hit = root[t]
        if (hit != null) {
            return ZovJson.encodeToString(JsonElement.serializer(), hit)
        }
        val base = root["__DEFAULT__"]!!.jsonObject
        val positive = t.hashCode() % 2 == 0
        val out: JsonObject =
            buildJsonObject {
                base.forEach { (k, v) ->
                    when (k) {
                        "ticker" -> put("ticker", t)
                        "subtitle" -> put("subtitle", "$t · бумага (расширенный мок)")
                        "changeLine" ->
                            put(
                                "changeLine",
                                if (positive) "+0,0% · мок" else "−0,0% · мок",
                            )
                        "changePositive" -> put("changePositive", positive)
                        else -> put(k, v)
                    }
                }
            }
        return ZovJson.encodeToString(JsonObject.serializer(), out)
    }
}
