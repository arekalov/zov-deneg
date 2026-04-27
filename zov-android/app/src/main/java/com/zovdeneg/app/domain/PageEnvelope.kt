package com.zovdeneg.app.domain

data class PageEnvelope<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
) {
    val hasNextPage: Boolean
        get() = page < totalPages
}
