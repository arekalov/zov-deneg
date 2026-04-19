package com.zovdeneg.app.data.repository

import com.zovdeneg.app.data.remote.api.ZovAuthApi
import com.zovdeneg.app.data.remote.dto.LoginRequestDto
import com.zovdeneg.app.data.remote.dto.RegisterRequestDto
import com.zovdeneg.app.domain.auth.AuthRepository

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class AuthRepositoryImpl @Inject constructor(
    private val authApi: ZovAuthApi,
) : AuthRepository {
    override suspend fun loginDemo(): Result<Unit> =
        runCatching {
            authApi.login(
                LoginRequestDto(
                    phone = DEMO_PHONE,
                    password = DEMO_PASSWORD,
                ),
            )
            Unit
        }

    override suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
    ): Result<Unit> =
        runCatching {
            authApi.register(
                RegisterRequestDto(
                    firstName = firstName,
                    lastName = lastName,
                    email = email,
                    phone = phone,
                    password = password,
                ),
            )
            Unit
        }

    private companion object {
        const val DEMO_PHONE = "+79001234567"
        const val DEMO_PASSWORD = "password12"
    }
}
