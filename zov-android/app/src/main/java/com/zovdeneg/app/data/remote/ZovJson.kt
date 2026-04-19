package com.zovdeneg.app.data.remote

import kotlinx.serialization.json.Json

internal val ZovJson = Json {
    ignoreUnknownKeys = true
    isLenient = true
    encodeDefaults = true
}
