package com.zovdeneg.app.domain

data class PageEnvelope<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
    val totalItems: Int,
) {
    val hasNextPage: Boolean
        get() {
            if (totalItems <= 0) return false
            val p = page.coerceAtLeast(1)
            val size = pageSize.coerceAtLeast(1)
            val loadedThrough = (p - 1) * size + items.size
            return loadedThrough < totalItems
        }
}
