package com.zovdeneg.app.ui.profile

import com.zovdeneg.app.domain.usecase.LoadUserProfileUseCase
import com.zovdeneg.app.domain.usecase.UpdateUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

data class EditProfileUiState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val phone: String = "",
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isSaving: Boolean = false,
    val saveFailed: Boolean = false,
    val saveSucceeded: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val loadUserProfile: LoadUserProfileUseCase,
    private val updateUserProfile: UpdateUserProfileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadUserProfile().fold(
                onSuccess = { p ->
                    _uiState.update {
                        EditProfileUiState(
                            firstName = p.firstName,
                            lastName = p.lastName,
                            email = p.email,
                            phone = p.phone,
                            isLoading = false,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { EditProfileUiState(isLoading = false, loadFailed = true) }
                },
            )
        }
    }

    fun setFirstName(value: String) {
        _uiState.update { it.copy(firstName = value, saveFailed = false, saveSucceeded = false) }
    }

    fun setLastName(value: String) {
        _uiState.update { it.copy(lastName = value, saveFailed = false, saveSucceeded = false) }
    }

    fun setEmail(value: String) {
        _uiState.update { it.copy(email = value, saveFailed = false, saveSucceeded = false) }
    }

    fun setPhone(value: String) {
        _uiState.update { it.copy(phone = value, saveFailed = false, saveSucceeded = false) }
    }

    fun save() {
        val s = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveFailed = false, saveSucceeded = false) }
            updateUserProfile(s.firstName, s.lastName, s.email, s.phone).fold(
                onSuccess = { profile ->
                    _uiState.update { st ->
                        st.copy(
                            firstName = profile.firstName,
                            lastName = profile.lastName,
                            email = profile.email,
                            phone = profile.phone,
                            isSaving = false,
                            saveSucceeded = true,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { st -> st.copy(isSaving = false, saveFailed = true) }
                },
            )
        }
    }

    fun acknowledgeSave() {
        _uiState.update { it.copy(saveSucceeded = false) }
    }
}
