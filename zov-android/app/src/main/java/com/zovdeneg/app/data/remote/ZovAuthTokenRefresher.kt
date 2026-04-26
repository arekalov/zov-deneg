package com.zovdeneg.app.data.remote

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.RefreshTokenRequestDto
import com.zovdeneg.app.data.remote.dto.TokensDto
import com.zovdeneg.app.di.ZovPlainHttpClient
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Обновление пары токенов через user-service ([ZovApiPaths.AUTH_REFRESH]) без циклической
 * зависимости с основным [HttpClient] (отдельный клиент без Auth-плагина).
 */
@Singleton
internal class ZovAuthTokenRefresher @Inject constructor(
    @param:ZovPlainHttpClient private val client: HttpClient,
) {
    suspend fun refresh(refreshToken: String): TokensDto =
        client.post(ZovApiPaths.AUTH_REFRESH) {
            setBody(RefreshTokenRequestDto(refreshToken = refreshToken))
        }.body()
}
