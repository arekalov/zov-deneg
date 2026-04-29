package com.zovdeneg.app.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
internal data class ZovApiErrorBody(
    val code: String? = null,
    val message: String? = null,
)
