package com.zovdeneg.app.domain.auth

interface AuthRepository {
    suspend fun loginDemo(): Result<Unit>

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
    ): Result<Unit>
}
