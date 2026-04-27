package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel

internal fun zovMockEngine(json: ZovMockAssetJson): MockEngine =
    MockEngine { request ->
        val path = request.url.encodedPath
        if (request.method == HttpMethod.Delete && isOrdersDetailPath(path)) {
            return@MockEngine respond(
                content = ByteReadChannel(""),
                status = HttpStatusCode.NoContent,
                headers = headersOf(),
            )
        }
        val body = mockJsonBody(request.method, path, request.url.parameters, json)
        if (body != null) {
            respond(
                content = ByteReadChannel(body),
                status = mockHttpStatus(request.method, path),
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        } else {
            respond(
                content = ByteReadChannel("""{"error":"not_found"}"""),
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json"),
            )
        }
    }

private fun isOrdersDetailPath(path: String): Boolean {
    if (!path.startsWith("/orders/")) return false
    val tail = path.removePrefix("/orders/").trim('/')
    return tail.isNotEmpty() && !tail.contains('/')
}

private fun mockHttpStatus(method: HttpMethod, path: String): HttpStatusCode =
    when {
        method == HttpMethod.Post && path == ZovApiPaths.ORDERS -> HttpStatusCode.Created
        method == HttpMethod.Post && path == ZovApiPaths.AUTH_REGISTER -> HttpStatusCode.Created
        else -> HttpStatusCode.OK
    }

private fun mockJsonBody(
    method: HttpMethod,
    path: String,
    parameters: Parameters,
    json: ZovMockAssetJson,
): String? =
    mockGetJson(method, path, parameters, json)
        ?: mockPostJson(method, path, json)
        ?: mockPutJson(method, path, json)

private fun mockGetJson(
    method: HttpMethod,
    path: String,
    parameters: Parameters,
    json: ZovMockAssetJson,
): String? {
    if (method != HttpMethod.Get) return null
    return when (path) {
        ZovApiPaths.PORTFOLIO_SUMMARY -> json.portfolioSummary()
        ZovApiPaths.PORTFOLIO -> json.portfolio()
        ZovApiPaths.SECURITIES_LIST -> json.securitiesListPaged(parameters)
        ZovApiPaths.TRANSACTIONS -> json.transactionsListPaged(parameters)
        ZovApiPaths.BALANCE -> json.balance()
        ZovApiPaths.USERS_ME -> json.userProfile()
        ZovApiPaths.ORDERS -> json.ordersList()
        else -> orderDetailGet(path, json) ?: securityGetBody(path, parameters, json)
    }
}

private fun orderDetailGet(path: String, json: ZovMockAssetJson): String? {
    if (!path.startsWith("/orders/")) return null
    val tail = path.removePrefix("/orders/").trim('/')
    if (tail.isEmpty() || tail.contains('/')) return null
    return json.orderDetail(tail)
}

private fun securityGetBody(path: String, parameters: Parameters, json: ZovMockAssetJson): String? {
    if (!path.startsWith("/securities/")) return null
    if (path.endsWith("/orderbook")) {
        val idPart = path.removeSuffix("/orderbook").substringAfterLast('/')
        return json.securityOrderBook(idPart)
    }
    val tail = path.removePrefix("/securities/")
    if (tail.isEmpty()) return null
    if (tail.endsWith("/price/history")) {
        val navKey = tail.removeSuffix("/price/history").trim('/')
        if (navKey.isEmpty() || navKey.contains('/')) return null
        val from = parameters["from"]?.toLongOrNull() ?: return null
        val to = parameters["to"]?.toLongOrNull() ?: return null
        return json.securityPriceHistory(navKey, from, to)
    }
    return if (!tail.contains('/')) {
        json.securityDetail(tail)
    } else {
        null
    }
}

private fun mockPostJson(method: HttpMethod, path: String, json: ZovMockAssetJson): String? {
    if (method != HttpMethod.Post) return null
    return when (path) {
        ZovApiPaths.BALANCE_DEPOSIT -> json.balance()
        ZovApiPaths.BALANCE_WITHDRAW -> json.balanceAfterWithdraw()
        ZovApiPaths.USERS_ME_PIN -> json.pinChangeOk()
        ZovApiPaths.ORDERS -> json.orderCreated()
        ZovApiPaths.AUTH_LOGIN -> json.mockAssetText(ZovMockAssetPaths.AUTH_ENVELOPE)
        ZovApiPaths.AUTH_REGISTER -> json.mockAssetText(ZovMockAssetPaths.AUTH_ENVELOPE)
        ZovApiPaths.AUTH_REFRESH -> json.mockAssetText(ZovMockAssetPaths.AUTH_TOKENS_REFRESH)
        else -> null
    }
}

private fun mockPutJson(method: HttpMethod, path: String, json: ZovMockAssetJson): String? {
    if (method == HttpMethod.Put && path == ZovApiPaths.USERS_ME) return json.userProfile()
    return null
}
