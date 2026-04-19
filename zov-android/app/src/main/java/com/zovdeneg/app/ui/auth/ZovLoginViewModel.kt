package com.zovdeneg.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.usecase.LoginWithMockBackendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val LOGIN_PIN_LENGTH = 4

@HiltViewModel
class ZovLoginViewModel @Inject constructor(
    private val loginWithMockBackend: LoginWithMockBackendUseCase,
) : ViewModel() {
    private val _draftPin = MutableStateFlow("")
    val draftPin: StateFlow<String> = _draftPin.asStateFlow()

    private val _loginFailed = MutableStateFlow(false)
    val loginFailed: StateFlow<Boolean> = _loginFailed.asStateFlow()

    fun appendDigit(digit: Int) {
        if (digit !in 0..9) return
        if (_draftPin.value.length >= LOGIN_PIN_LENGTH) return
        _draftPin.value += digit.toString()
        _loginFailed.value = false
    }

    fun deleteLast() {
        val cur = _draftPin.value
        if (cur.isNotEmpty()) _draftPin.value = cur.dropLast(1)
    }

    fun clearDraft() {
        _draftPin.value = ""
    }

    fun hasFullPin(): Boolean = _draftPin.value.length == LOGIN_PIN_LENGTH

    fun tryCompleteLogin(onLoggedIn: () -> Unit) {
        if (!hasFullPin()) return
        viewModelScope.launch {
            runLogin(onLoggedIn)
        }
    }

    fun loginWithBiometric(onLoggedIn: () -> Unit) {
        viewModelScope.launch {
            clearDraft()
            runLogin(onLoggedIn)
        }
    }

    private suspend fun runLogin(onLoggedIn: () -> Unit) {
        _loginFailed.value = false
        loginWithMockBackend().fold(
            onSuccess = {
                clearDraft()
                onLoggedIn()
            },
            onFailure = {
                clearDraft()
                _loginFailed.value = true
            },
        )
    }
}
