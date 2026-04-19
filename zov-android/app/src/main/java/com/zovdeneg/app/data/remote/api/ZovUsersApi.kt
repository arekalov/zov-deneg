package com.zovdeneg.app.data.remote.api

import com.zovdeneg.app.data.remote.contract.ZovApiPaths
import com.zovdeneg.app.data.remote.dto.PinChangeAckDto
import com.zovdeneg.app.data.remote.dto.UserProfileDto
import com.zovdeneg.app.data.remote.dto.UserProfileUpdateDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.content.TextContent
import javax.inject.Inject

internal class ZovUsersApi @Inject constructor(
    private val client: HttpClient,
) {
    suspend fun getMe(): UserProfileDto = client.get(ZovApiPaths.USERS_ME).body()

    suspend fun putMe(body: UserProfileUpdateDto): UserProfileDto =
        client.put(ZovApiPaths.USERS_ME) { setBody(body) }.body()

    suspend fun postPinChange(): PinChangeAckDto =
        client.post(ZovApiPaths.USERS_ME_PIN) {
            setBody(TextContent("{}", ContentType.Application.Json))
        }.body()
}
