package com.zovdeneg.app.domain.auth

/** Сброс JWT и кэша Bearer в HTTP-клиентах (выход из аккаунта). */
fun interface ClearRemoteSessionUseCase {
    operator fun invoke()
}
