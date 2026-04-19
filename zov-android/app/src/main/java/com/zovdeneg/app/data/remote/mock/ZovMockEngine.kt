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
        val body = mockJsonBody(request.method, request.url.encodedPath, request.url.parameters, json)
        if (body != null) {
            respond(
                content = ByteReadChannel(body),
                status = mockHttpStatus(request.method, request.url.encodedPath),
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
        ZovApiPaths.PORTFOLIO_HOLDINGS -> json.portfolioHoldings()
        ZovApiPaths.SECURITIES_POPULAR -> json.popularSecurities()
        ZovApiPaths.TRANSACTIONS -> json.transactions()
        ZovApiPaths.BALANCE -> json.balance()
        ZovApiPaths.USERS_ME -> json.userProfile()
        else -> securityGetBody(path, parameters, json)
    }
}

private fun securityGetBody(path: String, parameters: Parameters, json: ZovMockAssetJson): String? {
    if (!path.startsWith("/v1/securities/")) return null
    val tail = path.removePrefix("/v1/securities/")
    if (tail.isEmpty() || tail == "popular") return null
    if (tail.endsWith("/price/history")) {
        val ticker = tail.removeSuffix("/price/history").trim('/')
        if (ticker.isEmpty() || ticker.contains('/')) return null
        val from = parameters["from"]?.toLongOrNull() ?: return null
        val to = parameters["to"]?.toLongOrNull() ?: return null
        return json.securityPriceHistory(ticker, from, to)
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
        ZovApiPaths.AUTH_LOGIN -> json.authLoginResponse()
        ZovApiPaths.AUTH_REGISTER -> json.authRegisterResponse()
        else -> null
    }
}

private fun mockPutJson(method: HttpMethod, path: String, json: ZovMockAssetJson): String? {
    if (method == HttpMethod.Put && path == ZovApiPaths.USERS_ME) return json.userProfile()
    return null
}
