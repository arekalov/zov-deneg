package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovUsersApi
import com.zovdeneg.app.data.remote.dto.UserProfileDto
import com.zovdeneg.app.data.remote.dto.UserProfileUpdateDto
import com.zovdeneg.app.domain.profile.UserProfile
import com.zovdeneg.app.domain.profile.UserProfileRepository
import io.ktor.client.plugins.ClientRequestException

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class UserProfileRepositoryImpl @Inject constructor(
    private val usersApi: ZovUsersApi,
) : UserProfileRepository {
    override suspend fun getProfile(): Result<UserProfile> =
        runCatching { usersApi.getMe().toDomain() }

    override suspend fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
    ): Result<UserProfile> =
        runCatching {
            usersApi.putMe(UserProfileUpdateDto(firstName, lastName, email, phone)).toDomain()
        }

    override suspend fun changePin(): Result<Unit> =
        runCatching {
            usersApi.postPinChange()
        }.fold(
            onSuccess = { Result.success(Unit) },
            onFailure = { e ->
                val code = (e as? ClientRequestException)?.response?.status?.value
                if (code == 404 || code == 405 || code == 501) {
                    Result.success(Unit)
                } else {
                    Result.failure(e)
                }
            },
        )

    private fun UserProfileDto.toDomain(): UserProfile =
        UserProfile(
            firstName = firstName.orEmpty(),
            lastName = lastName.orEmpty(),
            email = email.orEmpty(),
            phone = phone.orEmpty(),
        )
}
