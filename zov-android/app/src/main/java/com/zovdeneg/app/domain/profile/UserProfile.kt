package com.zovdeneg.app.domain.profile

data class UserProfile(
    val firstName: String,
    val lastName: String,
    val email: String,
    val phone: String,
) {
    val displayName: String
        get() {
            val initial = lastName.trim().firstOrNull()?.let { "${it.titlecaseChar()}." } ?: ""
            return if (initial.isEmpty()) firstName else "$firstName $initial"
        }
}
