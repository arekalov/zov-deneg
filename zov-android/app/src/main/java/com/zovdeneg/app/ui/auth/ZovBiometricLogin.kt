package com.zovdeneg.app.ui.auth

import com.zovdeneg.app.R

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

internal fun canAuthenticateWithBiometric(activity: FragmentActivity): Boolean {
    val manager = BiometricManager.from(activity)
    return manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) == BiometricManager.BIOMETRIC_SUCCESS
}

internal fun showFingerprintLoginPrompt(
    activity: FragmentActivity,
    onSuccess: () -> Unit,
    onError: (message: String) -> Unit,
) {
    val manager = BiometricManager.from(activity)
    if (manager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) {
        onError(activity.getString(R.string.biometric_hardware_unavailable))
        return
    }
    val executor = ContextCompat.getMainExecutor(activity)
    val callback =
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(
                errorCode: Int,
                errString: CharSequence,
            ) {
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                    errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
                ) {
                    return
                }
                onError(errString.toString())
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                onSuccess()
            }

            override fun onAuthenticationFailed() {
                onError(activity.getString(R.string.biometric_not_recognized))
            }
        }
    val prompt = BiometricPrompt(activity, executor, callback)
    val info =
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.auth_biometric_prompt_title))
            .setSubtitle(activity.getString(R.string.auth_biometric_prompt_subtitle))
            .setNegativeButtonText(activity.getString(R.string.action_cancel))
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
            .build()
    prompt.authenticate(info)
}
