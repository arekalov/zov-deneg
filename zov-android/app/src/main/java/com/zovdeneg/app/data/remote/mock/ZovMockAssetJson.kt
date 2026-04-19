package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.ZovJson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

import android.content.Context

import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

import kotlin.math.PI
import kotlin.math.sin

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
            val merged = mergeSecurityDetailOrderBookIfAbsent(hit.jsonObject, root)
            return ZovJson.encodeToString(JsonElement.serializer(), merged)
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

    fun securityPriceHistory(tickerRaw: String, from: Long, to: Long): String {
        val t = tickerRaw.uppercase(Locale.getDefault())
        val detailObj = securityDetailsRoot[t]?.jsonObject
        val securityId =
            detailObj?.get("securityId")?.jsonPrimitive?.content
                ?: "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
        val tickerOut = detailObj?.get("ticker")?.jsonPrimitive?.content ?: t
        val start = minOf(from, to)
        val endInclusive = maxOf(from, to)
        val span = (endInclusive - start).coerceAtLeast(1L)
        val stepCount = 48L
        val step =
            (span / stepCount).coerceIn(
                60L,
                14L * 86_400L,
            )
        val data = buildJsonArray {
            var time = start
            val seed = 180.0 + t.sumOf { it.code } % 80
            val spanD = span.toDouble().coerceAtLeast(1.0)
            while (time <= endInclusive) {
                val u = (time - start) / spanD
                val mid = seed + u * 18.0
                val macro = sin(u * PI * 2.0 * 3.0) * 8.0
                val daily = sin((time.toDouble() / 86_400.0) * PI * 2.0) * 3.5
                val y = mid + macro + daily
                add(
                    buildJsonObject {
                        put("timestamp", time)
                        put("price", String.format(Locale.US, "%.2f", y))
                    },
                )
                time += step
            }
        }
        val envelope =
            buildJsonObject {
                put("securityId", securityId)
                put("ticker", tickerOut)
                put("from", start)
                put("to", endInclusive)
                put("data", data)
            }
        return ZovJson.encodeToString(JsonObject.serializer(), envelope)
    }
}
