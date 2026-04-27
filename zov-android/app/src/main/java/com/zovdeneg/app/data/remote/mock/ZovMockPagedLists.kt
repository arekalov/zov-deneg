package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.ZovJson
import io.ktor.http.Parameters
import java.util.Locale
import kotlin.math.max
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun ZovMockAssetJson.securitiesListPaged(parameters: Parameters): String {
    val page = parameters["page"]?.toIntOrNull() ?: 1
    val pageSize = (parameters["pageSize"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
    val q = parameters["q"]?.trim()?.lowercase(Locale.ROOT).orEmpty()
    val type = parameters["type"]?.trim()?.lowercase(Locale.ROOT).orEmpty()
    val raw = read(ZovMockAssetPaths.SECURITIES_LIST)
    val root = ZovJson.parseToJsonElement(raw).jsonObject
    val data = root["data"]?.jsonArray?.map { it.jsonObject }.orEmpty()
    val filtered =
        data.filter { card ->
            val okType = type.isEmpty() || card.stringField("type").lowercase(Locale.ROOT) == type
            val okQ =
                q.isEmpty() ||
                    card.stringField("ticker").lowercase(Locale.ROOT).contains(q) ||
                    card.stringField("name").lowercase(Locale.ROOT).contains(q)
            okType && okQ
        }
    return buildPagedJson(filtered, page, pageSize)
}

internal fun ZovMockAssetJson.transactionsListPaged(parameters: Parameters): String {
    val page = parameters["page"]?.toIntOrNull() ?: 1
    val pageSize = (parameters["pageSize"]?.toIntOrNull() ?: 20).coerceIn(1, 100)
    val type = parameters["type"]?.trim()?.lowercase(Locale.ROOT).orEmpty()
    val raw = read(ZovMockAssetPaths.TRANSACTIONS_LIST)
    val root = ZovJson.parseToJsonElement(raw).jsonObject
    val data = root["data"]?.jsonArray?.map { it.jsonObject }.orEmpty()
    val filtered =
        if (type.isEmpty()) {
            data
        } else {
            data.filter { it.stringField("type").lowercase(Locale.ROOT) == type }
        }
    return buildPagedJson(filtered, page, pageSize)
}

private fun JsonObject.stringField(name: String): String =
    this[name]?.jsonPrimitive?.content ?: ""

private fun buildPagedJson(items: List<JsonObject>, page: Int, pageSize: Int): String {
    val totalItems = items.size
    val totalPages = if (totalItems == 0) 1 else (totalItems + pageSize - 1) / pageSize
    val safePage = page.coerceIn(1, max(1, totalPages))
    val from = (safePage - 1) * pageSize
    val slice = items.drop(from).take(pageSize)
    val out =
        buildJsonObject {
            put("data", JsonArray(slice))
            put(
                "pagination",
                buildJsonObject {
                    put("page", JsonPrimitive(safePage))
                    put("pageSize", JsonPrimitive(pageSize))
                    put("totalItems", JsonPrimitive(totalItems))
                    put("totalPages", JsonPrimitive(totalPages))
                },
            )
        }
    return ZovJson.encodeToString(JsonObject.serializer(), out)
}
