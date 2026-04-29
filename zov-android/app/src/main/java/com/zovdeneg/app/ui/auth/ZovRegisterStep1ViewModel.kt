package com.zovdeneg.app.ui.auth

import android.content.Context
import com.zovdeneg.app.R
import com.zovdeneg.app.domain.usecase.RegisterNewAccountUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

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
    /** Десять цифр российского мобильного без префикса +7 (в поле отображается с префиксом +7). */
    val phoneDigits10: String = "",
    val password: String = "",
    val isSubmitting: Boolean = false,
    val validationError: Boolean = false,
    /** Текст с бэкенда или общая сетевая ошибка; `null` — ошибки нет. */
    val submitError: String? = null,
)

@HiltViewModel
class ZovRegisterStep1ViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val registerNewAccount: RegisterNewAccountUseCase,
    @ApplicationContext private val appContext: Context,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            RegisterStep1UiState(
                firstName = savedStateHandle.get<String>(KEY_FIRST).orEmpty(),
                lastName = savedStateHandle.get<String>(KEY_LAST).orEmpty(),
                email = savedStateHandle.get<String>(KEY_EMAIL).orEmpty(),
                phoneDigits10 = extractNationalTenDigits(savedStateHandle.get<String>(KEY_PHONE).orEmpty()),
                password = savedStateHandle.get<String>(KEY_PASSWORD).orEmpty(),
            ),
        )
    val uiState: StateFlow<RegisterStep1UiState> = _uiState.asStateFlow()

    fun setFirstName(value: String) {
        savedStateHandle[KEY_FIRST] = value
        _uiState.update {
            it.copy(
                firstName = value,
                validationError = false,
                submitError = null,
            )
        }
    }

    fun setLastName(value: String) {
        savedStateHandle[KEY_LAST] = value
        _uiState.update { it.copy(lastName = value, validationError = false, submitError = null) }
    }

    fun setEmail(value: String) {
        savedStateHandle[KEY_EMAIL] = value
        _uiState.update { it.copy(email = value, validationError = false, submitError = null) }
    }

    fun setPhoneDigits10(value: String) {
        val digits = extractNationalTenDigits(value)
        savedStateHandle[KEY_PHONE] = digits
        _uiState.update { it.copy(phoneDigits10 = digits, validationError = false, submitError = null) }
    }

    fun setPassword(value: String) {
        savedStateHandle[KEY_PASSWORD] = value
        _uiState.update { it.copy(password = value, validationError = false, submitError = null) }
    }

    fun submit(onSuccess: () -> Unit) {
        val s = _uiState.value
        if (!s.canSubmitStep1()) {
            _uiState.update { it.copy(validationError = true) }
            return
        }
        val normalizedPhone = registerPhoneE164FromNationalTen(s.phoneDigits10)
        if (!normalizedPhone.matches(REGISTER_PHONE_E164)) {
            _uiState.update { it.copy(validationError = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isSubmitting = true,
                    validationError = false,
                    submitError = null,
                )
            }
            registerNewAccount(
                s.firstName.trim(),
                s.lastName.trim(),
                s.email.trim(),
                normalizedPhone,
                s.password,
            )
                .fold(
                    onSuccess = {
                        _uiState.update { it.copy(isSubmitting = false) }
                        onSuccess()
                    },
                    onFailure = { t ->
                        val fallback = appContext.getString(R.string.register_network_error)
                        val msg = t.message?.takeIf { it.isNotBlank() } ?: fallback
                        _uiState.update { it.copy(isSubmitting = false, submitError = msg) }
                    },
                )
        }
    }
}
