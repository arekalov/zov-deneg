package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.ZovJson
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

import java.util.Locale

import kotlin.math.PI
import kotlin.math.sin

private const val MOCK_PRICE_HISTORY_POINT_TARGET = 96

private val NavKeyAsUuidRegex =
    Regex("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")

internal fun mockSecurityOrderBook(navKeyRaw: String, securityDetailsRoot: JsonObject): String {
    val tickerKey = resolveTickerKey(navKeyRaw, securityDetailsRoot)
    val detail = securityDetailsRoot[tickerKey]?.jsonObject ?: securityDetailsRoot["__DEFAULT__"]!!.jsonObject
    val ob = detail["orderBook"]?.jsonObject
    val sid = detail["securityId"]?.jsonPrimitive?.content ?: navKeyRaw
    val tk = detail["ticker"]?.jsonPrimitive?.content ?: tickerKey
    if (ob == null) {
        return ZovJson.encodeToString(
            buildJsonObject {
                put("securityId", sid)
                put("ticker", tk)
                put("timestamp", 0L)
                put("asks", buildJsonArray { })
                put("bids", buildJsonArray { })
                put("spread", "0")
            },
        )
    }
    return ZovJson.encodeToString(
        buildJsonObject {
            put("securityId", sid)
            put("ticker", tk)
            put("timestamp", 0L)
            put("asks", ob["askLevels"] ?: buildJsonArray { })
            put("bids", ob["bidLevels"] ?: buildJsonArray { })
            put("spread", "0")
        },
    )
}

internal fun mockSecurityDetail(tickerRaw: String, securityDetailsRoot: JsonObject): String {
    val navTrimmed = tickerRaw.trim()
    val t = resolveTickerKey(tickerRaw, securityDetailsRoot).uppercase(Locale.getDefault())
    val root = securityDetailsRoot
    val hit = root[t]
    if (hit != null) {
        val merged = mergeSecurityDetailOrderBookIfAbsent(hit.jsonObject, root)
        return ZovJson.encodeToString(JsonElement.serializer(), merged)
    }
    val base = root["__DEFAULT__"]!!.jsonObject
    val positive = t.hashCode() % 2 == 0
    val useNavUuidAsSecurityId = NavKeyAsUuidRegex.matches(navTrimmed)
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
                    "securityId" ->
                        if (useNavUuidAsSecurityId) {
                            put("securityId", navTrimmed)
                        } else {
                            put(k, v)
                        }

                    else -> put(k, v)
                }
            }
        }
    return ZovJson.encodeToString(JsonObject.serializer(), out)
}

internal fun mockSecurityPriceHistory(
    tickerRaw: String,
    from: Long,
    to: Long,
    securityDetailsRoot: JsonObject,
): String {
    val t = resolveTickerKey(tickerRaw, securityDetailsRoot)
    val detailObj = securityDetailsRoot[t]?.jsonObject
    val securityId =
        detailObj?.get("securityId")?.jsonPrimitive?.content
            ?: "a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11"
    val tickerOut = detailObj?.get("ticker")?.jsonPrimitive?.content ?: t
    val start = minOf(from, to)
    val endInclusive = maxOf(from, to)
    val span = (endInclusive - start).coerceAtLeast(1L)
    val step =
        (span / MOCK_PRICE_HISTORY_POINT_TARGET).coerceIn(
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

private fun resolveTickerKey(navKeyRaw: String, securityDetailsRoot: JsonObject): String {
    val trimmed = navKeyRaw.trim()
    if (securityDetailsRoot.containsKey(trimmed)) {
        return trimmed
    }
    securityDetailsRoot.forEach { (key, value) ->
        if (value is JsonObject) {
            val sid = value["securityId"]?.jsonPrimitive?.content
            if (sid != null && sid.equals(trimmed, ignoreCase = true)) {
                return key
            }
        }
    }
    return trimmed.uppercase(Locale.getDefault())
}
