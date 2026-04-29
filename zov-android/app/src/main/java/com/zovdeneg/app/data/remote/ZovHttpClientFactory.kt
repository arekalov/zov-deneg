package com.zovdeneg.app.data.remote

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.authProviders
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal data class ZovBearerClientDeps(
    val sessionTokens: ZovSessionTokens,
    val tokenRefresher: ZovAuthTokenRefresher,
    val invalidator: ZovBearerAuthInvalidator,
)

private object ZovHttpLogger : Logger {
    private const val TAG = "ZovHttp"

    override fun log(message: String) {
        Log.d(TAG, message)
    }
}

internal object ZovHttpClientFactory {

    fun createAuthenticatedClient(
        engine: HttpClientEngine,
        json: Json,
        baseUrl: String,
        deps: ZovBearerClientDeps,
        httpLogEnabled: Boolean,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(json)
            }
            install(Auth) {
                bearer {
                    loadTokens {
                        val access = deps.sessionTokens.peekAccessToken() ?: return@loadTokens null
                        val refresh = deps.sessionTokens.peekRefreshToken() ?: return@loadTokens null
                        BearerTokens(access, refresh)
                    }
                    refreshTokens {
                        val oldRefresh = oldTokens?.refreshToken ?: return@refreshTokens null
                        val tokens = deps.tokenRefresher.refresh(oldRefresh)
                        deps.sessionTokens.persist(tokens.accessToken, tokens.refreshToken)
                        BearerTokens(tokens.accessToken, tokens.refreshToken)
                    }
                    sendWithoutRequest { request ->
                        !request.url.build().encodedPath.startsWith("/auth/")
                    }
                }
            }
            if (httpLogEnabled) {
                // OkHttp: тело ответа логируется в OkHttp‑интерцепторе через peekBody.
                // MockEngine: безопасно включить BODY в Ktor Logging.
                install(Logging) {
                    logger = ZovHttpLogger
                    level = if (engine is MockEngine) LogLevel.BODY else LogLevel.HEADERS
                }
            }
            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                if (httpLogEnabled && engine !is MockEngine) {
                    headers.append(HttpHeaders.Connection, "close")
                }
            }
        }.also { client ->
            deps.invalidator.registerClearAction {
                clearBearerTokens(client)
            }
        }

    fun createPlainClient(
        engine: HttpClientEngine,
        json: Json,
        baseUrl: String,
        httpLogEnabled: Boolean,
    ): HttpClient =
        HttpClient(engine) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(json)
            }
            if (httpLogEnabled) {
                install(Logging) {
                    logger = ZovHttpLogger
                    level = if (engine is MockEngine) LogLevel.BODY else LogLevel.HEADERS
                }
            }
            defaultRequest {
                url(baseUrl)
                contentType(ContentType.Application.Json)
                if (httpLogEnabled && engine !is MockEngine) {
                    headers.append(HttpHeaders.Connection, "close")
                }
            }
        }

    private fun clearBearerTokens(client: HttpClient) {
        client.authProviders
            .filterIsInstance<BearerAuthProvider>()
            .forEach(BearerAuthProvider::clearToken)
    }
}
