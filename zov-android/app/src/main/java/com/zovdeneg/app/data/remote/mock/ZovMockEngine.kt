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
        val path = request.url.encodedPath
        val method = request.method
        val body: String? =
            when {
                method == HttpMethod.Get && path == ZovApiPaths.PORTFOLIO_SUMMARY -> json.portfolioSummary()
                method == HttpMethod.Get && path == ZovApiPaths.PORTFOLIO_HOLDINGS -> json.portfolioHoldings()
                method == HttpMethod.Get && path == ZovApiPaths.SECURITIES_POPULAR -> json.popularSecurities()
                method == HttpMethod.Get && path == ZovApiPaths.TRANSACTIONS -> json.transactions()
                method == HttpMethod.Get && path.startsWith("/v1/securities/") -> {
                    val slug = path.removePrefix("/v1/securities/").substringBefore("/")
                    when {
                        slug.isEmpty() || slug == "popular" -> null
                        else -> json.securityDetail(slug)
                    }
                }
                method == HttpMethod.Get && path == ZovApiPaths.BALANCE -> json.balance()
                method == HttpMethod.Post && path == ZovApiPaths.BALANCE_DEPOSIT -> json.balance()
                method == HttpMethod.Post && path == ZovApiPaths.BALANCE_WITHDRAW -> json.balanceAfterWithdraw()
                method == HttpMethod.Get && path == ZovApiPaths.USERS_ME -> json.userProfile()
                method == HttpMethod.Put && path == ZovApiPaths.USERS_ME -> json.userProfile()
                method == HttpMethod.Post && path == ZovApiPaths.USERS_ME_PIN -> json.pinChangeOk()
                method == HttpMethod.Post && path == ZovApiPaths.ORDERS -> json.orderCreated()
                method == HttpMethod.Post && path == ZovApiPaths.AUTH_LOGIN -> json.authLoginResponse()
                method == HttpMethod.Post && path == ZovApiPaths.AUTH_REGISTER -> json.authRegisterResponse()
                else -> null
            }
        if (body != null) {
            val status =
                when {
                    method == HttpMethod.Post && path == ZovApiPaths.ORDERS -> HttpStatusCode.Created
                    method == HttpMethod.Post && path == ZovApiPaths.AUTH_REGISTER -> HttpStatusCode.Created
                    else -> HttpStatusCode.OK
                }
            respond(
                content = ByteReadChannel(body),
                status = status,
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
