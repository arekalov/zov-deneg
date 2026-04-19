package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel

internal fun zovMockEngine(json: ZovMockAssetJson): MockEngine =
    MockEngine { request ->
        val body = mockJsonBody(request.method, request.url.encodedPath, json)
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

private fun mockJsonBody(method: HttpMethod, path: String, json: ZovMockAssetJson): String? =
    mockGetJson(method, path, json)
        ?: mockPostJson(method, path, json)
        ?: mockPutJson(method, path, json)

private fun mockGetJson(method: HttpMethod, path: String, json: ZovMockAssetJson): String? {
    if (method != HttpMethod.Get) return null
    return when (path) {
        ZovApiPaths.PORTFOLIO_SUMMARY -> json.portfolioSummary()
        ZovApiPaths.PORTFOLIO_HOLDINGS -> json.portfolioHoldings()
        ZovApiPaths.SECURITIES_POPULAR -> json.popularSecurities()
        ZovApiPaths.TRANSACTIONS -> json.transactions()
        ZovApiPaths.BALANCE -> json.balance()
        ZovApiPaths.USERS_ME -> json.userProfile()
        else -> securityDetailSlugBody(path, json)
    }
}

private fun securityDetailSlugBody(path: String, json: ZovMockAssetJson): String? {
    if (!path.startsWith("/v1/securities/")) return null
    val slug = path.removePrefix("/v1/securities/").substringBefore("/")
    return if (slug.isEmpty() || slug == "popular") null else json.securityDetail(slug)
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
