package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequestDto(
    val phone: String,
    val password: String,
)

@Serializable
internal data class RegisterRequestDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
    val password: String,
)

@Serializable
internal data class TokensDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int = 900,
)

@Serializable
internal data class RefreshTokenRequestDto(
    val refreshToken: String,
)

@Serializable
internal data class AuthEnvelopeDto(
    val user: UserProfileDto,
    val tokens: TokensDto,
)
