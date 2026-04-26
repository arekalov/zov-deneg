package com.zovdeneg.app.ui.auth

import android.content.Context
import com.zovdeneg.app.R
import com.zovdeneg.app.domain.auth.AuthRepository
import com.zovdeneg.app.domain.auth.LocalAuthStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

private const val LOGIN_PIN_LENGTH = 4

private fun loginCredentialFailureMessage(appContext: Context, e: Throwable): String =
    when (e) {
        is IOException -> appContext.getString(R.string.login_failed_network)
        else ->
            e.message?.takeIf { it.isNotBlank() }
                ?: appContext.getString(R.string.login_failed_network)
    }

@HiltViewModel
class ZovLoginViewModel @Inject constructor(
    private val localAuthStorage: LocalAuthStorage,
    private val authRepository: AuthRepository,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _draftPin = MutableStateFlow("")
    val draftPin: StateFlow<String> = _draftPin.asStateFlow()

    private val _wrongPin = MutableStateFlow(false)
    val wrongPin: StateFlow<Boolean> = _wrongPin.asStateFlow()

    private val _mustRegisterFirst = MutableStateFlow(false)
    val mustRegisterFirst: StateFlow<Boolean> = _mustRegisterFirst.asStateFlow()

    private val _remoteSessionFailed = MutableStateFlow(false)
    val remoteSessionFailed: StateFlow<Boolean> = _remoteSessionFailed.asStateFlow()

    private val _remoteSessionSyncing = MutableStateFlow(false)
    val remoteSessionSyncing: StateFlow<Boolean> = _remoteSessionSyncing.asStateFlow()

    private val _credentialPhone = MutableStateFlow("")
    val credentialPhone: StateFlow<String> = _credentialPhone.asStateFlow()

    private val _credentialPassword = MutableStateFlow("")
    val credentialPassword: StateFlow<String> = _credentialPassword.asStateFlow()

    private val _credentialError = MutableStateFlow<String?>(null)
    val credentialError: StateFlow<String?> = _credentialError.asStateFlow()

    private val _credentialSubmitting = MutableStateFlow(false)
    val credentialSubmitting: StateFlow<Boolean> = _credentialSubmitting.asStateFlow()

    fun shouldShowPinUnlock(): Boolean =
        localAuthStorage.hasPin() && authRepository.hasPersistedJwtPair()

    fun setCredentialPhone(value: String) {
        _credentialPhone.value = value
        _credentialError.value = null
    }

    fun setCredentialPassword(value: String) {
        _credentialPassword.value = value
        _credentialError.value = null
    }

    fun submitCredentials(onLoggedIn: () -> Unit, onNeedPinSetup: () -> Unit) {
        viewModelScope.launch {
            _credentialSubmitting.value = true
            _credentialError.value = null
            authRepository
                .loginWithCredentials(_credentialPhone.value, _credentialPassword.value)
                .fold(
                    onSuccess = {
                        if (localAuthStorage.hasPin()) {
                            onLoggedIn()
                        } else {
                            _credentialPassword.value = ""
                            onNeedPinSetup()
                        }
                    },
                    onFailure = { e ->
                        _credentialError.value = loginCredentialFailureMessage(appContext, e)
                    },
                )
            _credentialSubmitting.value = false
        }
    }

    fun appendDigit(digit: Int) {
        if (digit !in 0..9) return
        if (_draftPin.value.length >= LOGIN_PIN_LENGTH) return
        clearHints()
        _draftPin.value += digit.toString()
    }

    fun deleteLast() {
        val cur = _draftPin.value
        if (cur.isNotEmpty()) _draftPin.value = cur.dropLast(1)
        clearHints()
    }

    fun clearDraft() {
        _draftPin.value = ""
    }

    fun hasFullPin(): Boolean = _draftPin.value.length == LOGIN_PIN_LENGTH

    fun tryCompleteLogin(onLoggedIn: () -> Unit) {
        if (!hasFullPin()) return
        clearHints()
        if (!localAuthStorage.hasPin()) {
            _mustRegisterFirst.value = true
            clearDraft()
            return
        }
        val pin = _draftPin.value
        if (localAuthStorage.verifyPin(pin)) {
            clearDraft()
            syncRemoteSessionThen(onLoggedIn)
        } else {
            _wrongPin.value = true
            clearDraft()
        }
    }

    fun onBiometricAuthenticated(onLoggedIn: () -> Unit) {
        clearHints()
        clearDraft()
        syncRemoteSessionThen(onLoggedIn)
    }

    private fun syncRemoteSessionThen(onLoggedIn: () -> Unit) {
        viewModelScope.launch {
            _remoteSessionSyncing.value = true
            _remoteSessionFailed.value = false
            authRepository.ensureRemoteSessionAfterLocalUnlock().fold(
                onSuccess = { onLoggedIn() },
                onFailure = { _remoteSessionFailed.value = true },
            )
            _remoteSessionSyncing.value = false
        }
    }

    fun clearHints() {
        _wrongPin.value = false
        _mustRegisterFirst.value = false
        _remoteSessionFailed.value = false
    }

    fun isBiometricUnlockEnabled(): Boolean = localAuthStorage.isBiometricUnlockEnabled()
}
