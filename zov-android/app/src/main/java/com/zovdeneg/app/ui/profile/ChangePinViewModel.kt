package com.zovdeneg.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.usecase.ChangeAppPinUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChangePinUiState(
    val isSubmitting: Boolean = false,
    val failed: Boolean = false,
    val succeeded: Boolean = false,
)

@HiltViewModel
class ChangePinViewModel @Inject constructor(
    private val changeAppPin: ChangeAppPinUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ChangePinUiState())
    val uiState: StateFlow<ChangePinUiState> = _uiState.asStateFlow()

    fun submit() {
        viewModelScope.launch {
            _uiState.update { ChangePinUiState(isSubmitting = true) }
            changeAppPin().fold(
                onSuccess = { _uiState.update { ChangePinUiState(succeeded = true) } },
                onFailure = { _uiState.update { ChangePinUiState(failed = true) } },
            )
        }
    }

    fun acknowledge() {
        _uiState.update { ChangePinUiState() }
    }
}
