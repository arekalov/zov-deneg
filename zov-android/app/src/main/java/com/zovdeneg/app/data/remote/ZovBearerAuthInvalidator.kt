package com.zovdeneg.app.data.remote

import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Сбрасывает кэш Bearer в Ktor [io.ktor.client.plugins.auth.Auth] на всех зарегистрированных [io.ktor.client.HttpClient]
 * после логина, регистрации или выхода (новые токены или очистка сессии).
 */
@Singleton
internal class ZovBearerAuthInvalidator @Inject constructor() {
    private val clearActions = CopyOnWriteArrayList<() -> Unit>()

    fun registerClearAction(action: () -> Unit) {
        clearActions.add(action)
    }

    fun invalidateAll() {
        clearActions.forEach { it.invoke() }
    }
}
