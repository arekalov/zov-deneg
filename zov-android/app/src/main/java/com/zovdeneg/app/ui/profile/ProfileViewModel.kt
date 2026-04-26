package com.zovdeneg.app.ui.profile

import com.zovdeneg.app.domain.auth.ClearRemoteSessionUseCase
import com.zovdeneg.app.domain.auth.LocalAuthStorage
import com.zovdeneg.app.domain.profile.UserProfile
import com.zovdeneg.app.domain.usecase.LoadUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val biometricUnlockEnabled: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loadUserProfile: LoadUserProfileUseCase,
    private val localAuthStorage: LocalAuthStorage,
    private val clearRemoteSession: ClearRemoteSessionUseCase,
) : ViewModel() {
    private val _uiState =
        MutableStateFlow(
            ProfileUiState(biometricUnlockEnabled = localAuthStorage.isBiometricUnlockEnabled()),
        )
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                    biometricUnlockEnabled = localAuthStorage.isBiometricUnlockEnabled(),
                )
            }
            loadUserProfile().fold(
                onSuccess = { p ->
                    _uiState.update {
                        ProfileUiState(
                            profile = p,
                            isLoading = false,
                            biometricUnlockEnabled = localAuthStorage.isBiometricUnlockEnabled(),
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        ProfileUiState(
                            isLoading = false,
                            loadFailed = true,
                            biometricUnlockEnabled = localAuthStorage.isBiometricUnlockEnabled(),
                        )
                    }
                },
            )
        }
    }

    fun setBiometricUnlockEnabled(enabled: Boolean) {
        localAuthStorage.setBiometricUnlockEnabled(enabled)
        _uiState.update { it.copy(biometricUnlockEnabled = enabled) }
    }

    fun logout(navigateToLogin: () -> Unit) {
        clearRemoteSession()
        navigateToLogin()
    }
}
