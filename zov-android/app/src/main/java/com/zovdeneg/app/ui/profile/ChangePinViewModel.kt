package com.zovdeneg.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.auth.LocalAuthStorage
import com.zovdeneg.app.domain.usecase.ChangeAppPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ChangePinFlowStep {
    EnterCurrent,
    EnterNew,
    ConfirmNew,
}

data class ChangePinUiState(
    val step: ChangePinFlowStep = ChangePinFlowStep.EnterCurrent,
    val draft: String = "",
    val newPin: String = "",
    val wrongCurrentPin: Boolean = false,
    val confirmMismatch: Boolean = false,
    val isSubmitting: Boolean = false,
    val failed: Boolean = false,
    val succeeded: Boolean = false,
)

@HiltViewModel
class ChangePinViewModel @Inject constructor(
    private val changeAppPin: ChangeAppPinUseCase,
    private val localAuth: LocalAuthStorage,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePinUiState())
    val uiState: StateFlow<ChangePinUiState> = _uiState.asStateFlow()

    fun appendDigit(digit: Int) {
        _uiState.update { s ->
            if (s.isSubmitting || s.draft.length >= PIN_LEN) return@update s
            s.copy(
                draft = s.draft + digit,
                wrongCurrentPin = false,
                failed = false,
                confirmMismatch = false,
            )
        }
    }

    fun deleteLast() {
        _uiState.update { s ->
            if (s.isSubmitting) return@update s
            if (s.draft.isEmpty()) return@update s
            s.copy(
                draft = s.draft.dropLast(1),
                wrongCurrentPin = false,
                failed = false,
                confirmMismatch = false,
            )
        }
    }

    fun onKeypadConfirm() {
        val s = _uiState.value
        if (s.isSubmitting || s.draft.length != PIN_LEN) return
        when (s.step) {
            ChangePinFlowStep.EnterCurrent -> {
                if (!localAuth.verifyPin(s.draft)) {
                    _uiState.update { it.copy(wrongCurrentPin = true) }
                    return
                }
                _uiState.update {
                    ChangePinUiState(step = ChangePinFlowStep.EnterNew, draft = "")
                }
            }
            ChangePinFlowStep.EnterNew -> {
                val newPlain = s.draft
                _uiState.update {
                    ChangePinUiState(
                        step = ChangePinFlowStep.ConfirmNew,
                        newPin = newPlain,
                        draft = "",
                    )
                }
            }
            ChangePinFlowStep.ConfirmNew -> {
                if (s.draft != s.newPin) {
                    _uiState.update { it.copy(confirmMismatch = true, draft = "") }
                    return
                }
                submitRemote(s.newPin)
            }
        }
    }

    private fun submitRemote(newPlain: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(isSubmitting = true, wrongCurrentPin = false, failed = false, confirmMismatch = false)
            }
            changeAppPin().fold(
                onSuccess = {
                    localAuth.savePinFromPlain(newPlain)
                    _uiState.update { ChangePinUiState(succeeded = true) }
                },
                onFailure = {
                    _uiState.update {
                        ChangePinUiState(
                            step = ChangePinFlowStep.ConfirmNew,
                            newPin = newPlain,
                            draft = "",
                            failed = true,
                        )
                    }
                },
            )
        }
    }

    fun acknowledge() {
        _uiState.update { ChangePinUiState() }
    }

    private companion object {
        const val PIN_LEN = 4
    }
}
