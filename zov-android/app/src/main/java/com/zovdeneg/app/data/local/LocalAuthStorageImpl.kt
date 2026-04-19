package com.zovdeneg.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.zovdeneg.app.domain.auth.LocalAuthStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class LocalAuthStorageImpl @Inject constructor(
    @ApplicationContext context: Context,
) : LocalAuthStorage {

    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    override fun hasPin(): Boolean = prefs.getString(KEY_PIN_SHA256, null) != null

    override fun savePinFromPlain(pin: String) {
        prefs.edit().putString(KEY_PIN_SHA256, sha256Hex(pin)).apply()
    }

    override fun verifyPin(pin: String): Boolean {
        val stored = prefs.getString(KEY_PIN_SHA256, null) ?: return false
        return stored == sha256Hex(pin)
    }

    override fun setBiometricUnlockEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_UNLOCK, enabled).apply()
    }

    override fun isBiometricUnlockEnabled(): Boolean = prefs.getBoolean(KEY_BIOMETRIC_UNLOCK, false)

    private fun sha256Hex(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }

    private companion object {
        const val PREFS_NAME = "zov_local_auth_encrypted"
        const val KEY_PIN_SHA256 = "pin_sha256"
        const val KEY_BIOMETRIC_UNLOCK = "biometric_unlock_enabled"

        fun createEncryptedPrefs(context: Context): SharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            return EncryptedSharedPreferences.create(
                PREFS_NAME,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        }
    }
}
