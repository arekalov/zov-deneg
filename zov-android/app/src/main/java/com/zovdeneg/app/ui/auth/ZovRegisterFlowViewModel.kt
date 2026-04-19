package com.zovdeneg.app.ui.auth

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val REGISTER_PIN_LENGTH = 4

internal class ZovRegisterFlowViewModel : ViewModel() {
    private val _draftPin = MutableStateFlow("")
    val draftPin: StateFlow<String> = _draftPin.asStateFlow()

    private val _firstPin = MutableStateFlow("")

    private val _confirmPinMismatch = MutableStateFlow(false)
    val confirmPinMismatch: StateFlow<Boolean> = _confirmPinMismatch.asStateFlow()

    fun resetFlow() {
        _draftPin.value = ""
        _firstPin.value = ""
        _confirmPinMismatch.value = false
    }

    fun clearDraft() {
        _draftPin.value = ""
        _confirmPinMismatch.value = false
    }

    fun appendDigit(digit: Int) {
        _confirmPinMismatch.value = false
        if (digit !in 0..9) return
        if (_draftPin.value.length >= REGISTER_PIN_LENGTH) return
        _draftPin.value += digit.toString()
    }

    fun deleteLast() {
        _confirmPinMismatch.value = false
        val cur = _draftPin.value
        if (cur.isNotEmpty()) _draftPin.value = cur.dropLast(1)
    }

    fun commitFirstPin(): Boolean {
        if (_draftPin.value.length != REGISTER_PIN_LENGTH) return false
        _firstPin.value = _draftPin.value
        _draftPin.value = ""
        return true
    }

    fun tryFinishSecondPinStep(): Boolean {
        if (_draftPin.value.length != REGISTER_PIN_LENGTH) return false
        if (_draftPin.value != _firstPin.value) {
            _confirmPinMismatch.value = true
            _draftPin.value = ""
            return false
        }
        _draftPin.value = ""
        _firstPin.value = ""
        _confirmPinMismatch.value = false
        return true
    }
}
