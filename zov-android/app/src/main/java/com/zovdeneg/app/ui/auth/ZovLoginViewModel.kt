package com.zovdeneg.app.ui.auth

import com.zovdeneg.app.domain.auth.LocalAuthStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import androidx.lifecycle.ViewModel

import javax.inject.Inject

private const val LOGIN_PIN_LENGTH = 4

@HiltViewModel
class ZovLoginViewModel @Inject constructor(
    private val localAuthStorage: LocalAuthStorage,
) : ViewModel() {
    private val _draftPin = MutableStateFlow("")
    val draftPin: StateFlow<String> = _draftPin.asStateFlow()

    private val _wrongPin = MutableStateFlow(false)
    val wrongPin: StateFlow<Boolean> = _wrongPin.asStateFlow()

    private val _mustRegisterFirst = MutableStateFlow(false)
    val mustRegisterFirst: StateFlow<Boolean> = _mustRegisterFirst.asStateFlow()

    fun appendDigit(digit: Int) {
        if (digit !in 0..9) return
        if (_draftPin.value.length >= LOGIN_PIN_LENGTH) return
        _draftPin.value += digit.toString()
        clearHints()
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
            onLoggedIn()
        } else {
            _wrongPin.value = true
            clearDraft()
        }
    }

    fun onBiometricAuthenticated(onLoggedIn: () -> Unit) {
        clearHints()
        clearDraft()
        onLoggedIn()
    }

    fun clearHints() {
        _wrongPin.value = false
        _mustRegisterFirst.value = false
    }

    fun isBiometricUnlockEnabled(): Boolean = localAuthStorage.isBiometricUnlockEnabled()
}
