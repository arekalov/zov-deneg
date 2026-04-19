package com.zovdeneg.app.domain.auth

interface LocalAuthStorage {
    fun hasPin(): Boolean

    fun savePinFromPlain(pin: String)

    fun verifyPin(pin: String): Boolean

    fun setBiometricUnlockEnabled(enabled: Boolean)

    fun isBiometricUnlockEnabled(): Boolean
}
