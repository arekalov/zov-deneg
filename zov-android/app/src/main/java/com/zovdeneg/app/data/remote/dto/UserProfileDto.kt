package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class UserProfileDto(
    val id: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val isBlocked: Boolean? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null,
)

@Serializable
internal data class UserProfileUpdateDto(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
)

@Serializable
internal data class PinChangeAckDto(
    val ok: Boolean = true,
)
