package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.AuthEnvelopeDto
import com.zovdeneg.app.data.remote.dto.LoginRequestDto
import com.zovdeneg.app.data.remote.dto.RegisterRequestDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

internal class ZovAuthApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun login(body: LoginRequestDto): AuthEnvelopeDto =
        client.post(ZovApiPaths.AUTH_LOGIN) { setBody(body) }.body()

    suspend fun register(body: RegisterRequestDto): AuthEnvelopeDto =
        client.post(ZovApiPaths.AUTH_REGISTER) { setBody(body) }.body()
}
