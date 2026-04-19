package com.zovdeneg.app.data.remote.mock

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.utils.io.ByteReadChannel

internal fun zovMockEngine(): MockEngine =
    MockEngine { request ->
        val path = request.url.encodedPath
        val method = request.method
        val json: String? =
            when {
                method == HttpMethod.Get && path == ZovApiPaths.PORTFOLIO_SUMMARY -> ZovMockJsonBodies.portfolioSummary()
                method == HttpMethod.Get && path == ZovApiPaths.PORTFOLIO_HOLDINGS -> ZovMockJsonBodies.portfolioHoldings()
                method == HttpMethod.Get && path == ZovApiPaths.SECURITIES_POPULAR -> ZovMockJsonBodies.popularSecurities()
                method == HttpMethod.Get && path == ZovApiPaths.TRANSACTIONS -> ZovMockJsonBodies.transactions()
                method == HttpMethod.Get && path.startsWith("/v1/securities/") -> {
                    val slug = path.removePrefix("/v1/securities/").substringBefore("/")
                    when {
                        slug.isEmpty() || slug == "popular" -> null
                        else -> ZovMockJsonBodies.securityDetail(slug)
                    }
                }
                method == HttpMethod.Get && path == ZovApiPaths.BALANCE -> ZovMockJsonBodies.balance()
                method == HttpMethod.Post && path == ZovApiPaths.BALANCE_DEPOSIT -> ZovMockJsonBodies.balance()
                method == HttpMethod.Post && path == ZovApiPaths.BALANCE_WITHDRAW -> ZovMockJsonBodies.balanceAfterWithdraw()
                method == HttpMethod.Get && path == ZovApiPaths.USERS_ME -> ZovMockJsonBodies.userProfile()
                method == HttpMethod.Put && path == ZovApiPaths.USERS_ME -> ZovMockJsonBodies.userProfile()
                method == HttpMethod.Post && path == ZovApiPaths.USERS_ME_PIN -> ZovMockJsonBodies.pinChangeOk()
                method == HttpMethod.Post && path == ZovApiPaths.ORDERS -> ZovMockJsonBodies.orderCreated()
                method == HttpMethod.Post && path == ZovApiPaths.AUTH_LOGIN -> ZovMockJsonBodies.authLoginResponse()
                method == HttpMethod.Post && path == ZovApiPaths.AUTH_REGISTER -> ZovMockJsonBodies.authRegisterResponse()
                else -> null
            }
        if (json != null) {
            val status =
                when {
                    method == HttpMethod.Post && path == ZovApiPaths.ORDERS -> HttpStatusCode.Created
                    method == HttpMethod.Post && path == ZovApiPaths.AUTH_REGISTER -> HttpStatusCode.Created
                    else -> HttpStatusCode.OK
                }
            respond(
                content = ByteReadChannel(json),
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
