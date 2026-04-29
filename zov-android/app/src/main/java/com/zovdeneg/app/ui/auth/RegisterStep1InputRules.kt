package com.zovdeneg.app.ui.auth

import android.util.Patterns

internal val REGISTER_PHONE_E164: Regex = Regex("^\\+7\\d{10}$")

/**
 * Из произвольного ввода или сохранённого значения (в т.ч. старый формат +7…11 цифр)
 * получает до 10 национальных цифр после кода страны 7.
 */
internal fun extractNationalTenDigits(raw: String): String {
    var d = raw.filter { it.isDigit() }
    if (d.startsWith("8") && d.length >= 11) d = "7" + d.drop(1)
    return when {
        d.length >= 11 && d.startsWith("7") -> d.drop(1).take(10)
        else -> d.take(10)
    }
}

internal fun registerPhoneE164FromNationalTen(digits10: String): String {
    val d = digits10.filter { it.isDigit() }.take(10)
    return if (d.length == 10) "+7$d" else ""
}

internal fun isRegisterPhoneComplete(digits10: String): Boolean =
    digits10.filter { it.isDigit() }.length == 10

internal fun isRegisterEmailValid(email: String): Boolean =
    Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

internal fun RegisterStep1UiState.canSubmitStep1(): Boolean {
    if (firstName.isBlank() || lastName.isBlank() || password.length < 8) return false
    if (!isRegisterPhoneComplete(phoneDigits10)) return false
    return isRegisterEmailValid(email)
}
