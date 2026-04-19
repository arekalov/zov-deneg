package com.zovdeneg.app.ui.auth

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.usecase.RegisterNewAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val KEY_FIRST = "reg_first"
private const val KEY_LAST = "reg_last"
private const val KEY_EMAIL = "reg_email"
private const val KEY_PHONE = "reg_phone"
private const val KEY_PASSWORD = "reg_password"

data class RegisterStep1UiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val validationError: Boolean = false,
    val networkError: Boolean = false,
)

@HiltViewModel
class ZovRegisterStep1ViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val registerNewAccount: RegisterNewAccountUseCase,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            RegisterStep1UiState(
                firstName = savedStateHandle.get<String>(KEY_FIRST).orEmpty(),
                lastName = savedStateHandle.get<String>(KEY_LAST).orEmpty(),
                email = savedStateHandle.get<String>(KEY_EMAIL).orEmpty(),
                phone = savedStateHandle.get<String>(KEY_PHONE).orEmpty(),
                password = savedStateHandle.get<String>(KEY_PASSWORD).orEmpty(),
            ),
        )
    val uiState: StateFlow<RegisterStep1UiState> = _uiState.asStateFlow()

    fun setFirstName(value: String) {
        savedStateHandle[KEY_FIRST] = value
        _uiState.update { it.copy(firstName = value, validationError = false, networkError = false) }
    }

    fun setLastName(value: String) {
        savedStateHandle[KEY_LAST] = value
        _uiState.update { it.copy(lastName = value, validationError = false, networkError = false) }
    }

    fun setEmail(value: String) {
        savedStateHandle[KEY_EMAIL] = value
        _uiState.update { it.copy(email = value, validationError = false, networkError = false) }
    }

    fun setPhone(value: String) {
        savedStateHandle[KEY_PHONE] = value
        _uiState.update { it.copy(phone = value, validationError = false, networkError = false) }
    }

    fun setPassword(value: String) {
        savedStateHandle[KEY_PASSWORD] = value
        _uiState.update { it.copy(password = value, validationError = false, networkError = false) }
    }

    fun submit(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (s.firstName.isBlank() || s.lastName.isBlank() || s.email.isBlank() || s.phone.isBlank() || s.password.length < 8) {
            _uiState.update { it.copy(validationError = true) }
            return
        }
        val normalizedPhone = normalizePhone(s.phone)
        if (!normalizedPhone.matches(PHONE_REGEX)) {
            _uiState.update { it.copy(validationError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, validationError = false, networkError = false) }
            registerNewAccount(s.firstName.trim(), s.lastName.trim(), s.email.trim(), normalizedPhone, s.password)
                .fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSubmitting = false) }
                        onSuccess()
                    },
                    onFailure = {
                        _uiState.update { it.copy(isSubmitting = false, networkError = true) }
                    },
                )
        }
    }

    private fun normalizePhone(raw: String): String {
        val digits = raw.filter { it.isDigit() || it == '+' }
        return when {
            digits.startsWith("+7") && digits.length == 12 -> digits
            digits.startsWith("8") && digits.length == 11 -> "+7" + digits.drop(1)
            digits.startsWith("7") && digits.length == 11 -> "+${digits}"
            digits.length == 10 -> "+7$digits"
            else -> raw.trim()
        }
    }

    private companion object {
        val PHONE_REGEX = Regex("^\\+7\\d{10}$")
    }
}
