package com.zovdeneg.app.domain.auth

/**
 * Ответ [io.ktor.http.HttpStatusCode.NotFound] на POST /auth/login — номер не зарегистрирован.
 */
class UserNotFoundForLoginException : Exception("Пользователь с таким номером не найден")
