package com.zovdeneg.app.data.remote.mock

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

import java.util.Locale

/**
 * Если в деталях бумаги нет [orderBook], подставляет шаблон из `__DEFAULT__` в [root],
 * масштабируя цены под [priceLine] (середина спреда best ask / best bid шаблона → середина по цене в строке).
 */
internal fun mergeSecurityDetailOrderBookIfAbsent(
    detail: JsonObject,
    root: JsonObject,
): JsonObject {
    if ("orderBook" in detail) return detail
    val defaultBook = root["__DEFAULT__"]?.jsonObject?.get("orderBook") ?: return detail
    val priceLine = detail["priceLine"]?.jsonPrimitive?.content
    val scaled = scaleOrderBookFromTemplate(defaultBook, priceLine)
    return buildJsonObject {
        detail.forEach { (k, v) -> put(k, v) }
        put("orderBook", scaled)
    }
}

internal fun scaleOrderBookFromTemplate(
    template: JsonElement,
    priceLine: String?,
): JsonObject {
    val tpl = template.jsonObject
    val factor = computeOrderBookScaleFactor(tpl, priceLine) ?: return tpl
    return buildScaledOrderBook(tpl, factor)
}

private fun computeOrderBookScaleFactor(
    tpl: JsonObject,
    priceLine: String?,
): Double? {
    val targetMid = parseMockPriceLineToDouble(priceLine) ?: return null
    val bestAsk = tpl["bestAskPrice"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return null
    val bestBid = tpl["bestBidPrice"]?.jsonPrimitive?.content?.toDoubleOrNull() ?: return null
    val templateMid = (bestAsk + bestBid) / 2.0
    if (templateMid <= 1e-6) return null
    return targetMid / templateMid
}

private fun buildScaledOrderBook(
    tpl: JsonObject,
    factor: Double,
): JsonObject {
    fun scalePrice(raw: String): String {
        val v = raw.toDoubleOrNull() ?: return raw
        return String.format(Locale.US, "%.2f", v * factor)
    }

    fun scaleLevel(element: JsonElement): JsonObject {
        val o = element.jsonObject
        return buildJsonObject {
            o.forEach { (k, v) ->
                when (k) {
                    "price" -> put(k, JsonPrimitive(scalePrice(v.jsonPrimitive.content)))
                    else -> put(k, v)
                }
            }
        }
    }

    fun scaleLevels(arr: JsonArray): JsonArray =
        buildJsonArray {
            arr.forEach { add(scaleLevel(it)) }
        }

    return buildJsonObject {
        tpl["askLevels"]?.jsonArray?.let { put("askLevels", scaleLevels(it)) }
        tpl["bestAskPrice"]?.jsonPrimitive?.content?.let { put("bestAskPrice", JsonPrimitive(scalePrice(it))) }
        tpl["bestBidPrice"]?.jsonPrimitive?.content?.let { put("bestBidPrice", JsonPrimitive(scalePrice(it))) }
        tpl["bidLevels"]?.jsonArray?.let { put("bidLevels", scaleLevels(it)) }
    }
}

private fun parseMockPriceLineToDouble(raw: String?): Double? {
    if (raw.isNullOrBlank()) return null
    val cleaned =
        raw
            .replace("₽", "")
            .trim()
            .replace("\u00a0", "")
            .replace(" ", "")
            .replace(',', '.')
    if (cleaned == "—" || cleaned == "-") return null
    return cleaned.toDoubleOrNull()
}
