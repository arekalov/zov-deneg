package com.zovdeneg.app.data.remote

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Персистентное хранение пары JWT (access + refresh) для zov-back.
 */
@Singleton
internal class ZovSessionTokens @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    private val lock = Any()

    fun peekAccessToken(): String? =
        synchronized(lock) {
            prefs.getString(KEY_ACCESS, null)?.takeIf { it.isNotBlank() }
        }

    fun peekRefreshToken(): String? =
        synchronized(lock) {
            prefs.getString(KEY_REFRESH, null)?.takeIf { it.isNotBlank() }
        }

    fun persist(accessToken: String, refreshToken: String) {
        synchronized(lock) {
            prefs.edit()
                .putString(KEY_ACCESS, accessToken)
                .putString(KEY_REFRESH, refreshToken)
                .apply()
        }
    }

    fun clear() {
        synchronized(lock) {
            prefs.edit().remove(KEY_ACCESS).remove(KEY_REFRESH).commit()
        }
    }

    private companion object {
        const val PREFS_NAME = "zov_session_tokens"
        const val KEY_ACCESS = "access_token"
        const val KEY_REFRESH = "refresh_token"

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
