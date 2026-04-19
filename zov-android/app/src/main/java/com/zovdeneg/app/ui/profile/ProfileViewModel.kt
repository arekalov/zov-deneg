package com.zovdeneg.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.profile.UserProfile
import com.zovdeneg.app.domain.usecase.LoadUserProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val profile: UserProfile? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val loadUserProfile: LoadUserProfileUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { ProfileUiState(isLoading = true, loadFailed = false) }
            loadUserProfile().fold(
                onSuccess = { p -> _uiState.update { ProfileUiState(profile = p, isLoading = false) } },
                onFailure = { _uiState.update { ProfileUiState(isLoading = false, loadFailed = true) } },
            )
        }
    }
}
