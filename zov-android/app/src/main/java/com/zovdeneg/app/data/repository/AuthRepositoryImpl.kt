package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.ZovBearerAuthInvalidator
import com.zovdeneg.app.data.remote.ZovJson
import com.zovdeneg.app.data.remote.ZovSessionTokens
import com.zovdeneg.app.data.remote.api.ZovAuthApi
import com.zovdeneg.app.data.remote.dto.LoginRequestDto
import com.zovdeneg.app.data.remote.dto.RegisterRequestDto
import com.zovdeneg.app.domain.auth.AuthRepository
import com.zovdeneg.app.domain.auth.UserNotFoundForLoginException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.io.IOException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuthRepositoryImpl @Inject constructor(
    private val authApi: ZovAuthApi,
    private val sessionTokens: ZovSessionTokens,
    private val bearerInvalidator: ZovBearerAuthInvalidator,
) : AuthRepository {
    override suspend fun loginDemo(): Result<Unit> =
        try {
            val env =
                authApi.login(
                    LoginRequestDto(
                        phone = DEMO_PHONE,
                        password = DEMO_PASSWORD,
                    ),
                )
            persistTokensAndInvalidateBearer(env.tokens.accessToken, env.tokens.refreshToken)
            Result.success(Unit)
        } catch (e: ResponseException) {
            Result.failure(mapLoginResponseException(e))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        }

    override suspend fun loginWithCredentials(phone: String, password: String): Result<Unit> =
        try {
            val env =
                authApi.login(
                    LoginRequestDto(
                        phone = phone.trim(),
                        password = password,
                    ),
                )
            persistTokensAndInvalidateBearer(env.tokens.accessToken, env.tokens.refreshToken)
            Result.success(Unit)
        } catch (e: ResponseException) {
            Result.failure(mapLoginResponseException(e))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        }

    override fun hasPersistedJwtPair(): Boolean {
        val accessOk = !sessionTokens.peekAccessToken().isNullOrBlank()
        val refreshOk = !sessionTokens.peekRefreshToken().isNullOrBlank()
        return accessOk && refreshOk
    }

    override suspend fun ensureRemoteSessionAfterLocalUnlock(): Result<Unit> {
        if (hasPersistedJwtPair()) {
            return Result.success(Unit)
        }
        return Result.failure(
            IllegalStateException("Нет сохранённой сессии. Войдите по телефону и паролю."),
        )
    }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
    ): Result<Unit> =
        try {
            val env =
                authApi.register(
                    RegisterRequestDto(
                        firstName = firstName,
                        lastName = lastName,
                        email = email,
                        phone = phone,
                        password = password,
                    ),
                )
            persistTokensAndInvalidateBearer(env.tokens.accessToken, env.tokens.refreshToken)
            Result.success(Unit)
        } catch (e: ResponseException) {
            Result.failure(Exception(authFailureMessage(e.response)))
        } catch (e: IOException) {
            Result.failure(e)
        } catch (e: SerializationException) {
            Result.failure(e)
        }

    private fun persistTokensAndInvalidateBearer(access: String, refresh: String) {
        sessionTokens.persist(access, refresh)
        bearerInvalidator.invalidateAll()
    }

    private companion object {
        const val DEMO_PHONE = "+79001234567"
        const val DEMO_PASSWORD = "password12"
    }
}

private suspend fun mapLoginResponseException(e: ResponseException): Throwable =
    if (e.response.status == HttpStatusCode.NotFound) {
        UserNotFoundForLoginException()
    } else {
        Exception(authFailureMessage(e.response))
    }

private suspend fun authFailureMessage(response: HttpResponse): String {
    val raw =
        try {
            response.bodyAsText()
        } catch (_: IOException) {
            return genericAuthServerMessage(response.status.value)
        }
    return parseAuthErrorJson(raw) ?: genericAuthServerMessage(response.status.value)
}

private fun parseAuthErrorJson(raw: String): String? {
    val obj =
        try {
            ZovJson.parseToJsonElement(raw).jsonObject
        } catch (_: SerializationException) {
            return null
        }
    val fields = obj["fields"]
    if (fields is JsonArray && fields.isNotEmpty()) {
        val joined =
            fields.mapNotNull { el ->
                if (el is JsonObject) {
                    el["message"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
                } else {
                    null
                }
            }.joinToString("\n")
        if (joined.isNotBlank()) {
            return joined
        }
    }
    return obj["message"]?.jsonPrimitive?.content?.takeIf { it.isNotBlank() }
}

private fun genericAuthServerMessage(code: Int): String = "Ошибка сервера ($code). Повторите позже"
