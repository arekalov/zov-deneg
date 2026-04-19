package com.zovdeneg.app.domain.profile

interface UserProfileRepository {
    suspend fun getProfile(): Result<UserProfile>

    suspend fun updateProfile(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
    ): Result<UserProfile>

    suspend fun changePin(): Result<Unit>
}
