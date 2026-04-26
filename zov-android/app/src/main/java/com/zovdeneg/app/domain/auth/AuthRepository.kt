package com.zovdeneg.app.domain.auth

interface AuthRepository {
    suspend fun loginDemo(): Result<Unit>

    suspend fun loginWithCredentials(phone: String, password: String): Result<Unit>

    /** В хранилище есть и access, и refresh JWT (после регистрации или входа по паролю). */
    fun hasPersistedJwtPair(): Boolean

    /**
     * После успешного локального PIN/биометрии: проверяет, что пара JWT уже в хранилище.
     * Если токенов нет — ошибка (нужен повторный вход по телефону и паролю).
     */
    suspend fun ensureRemoteSessionAfterLocalUnlock(): Result<Unit>

    suspend fun register(
        firstName: String,
        lastName: String,
        email: String,
        phone: String,
        password: String,
    ): Result<Unit>
}
