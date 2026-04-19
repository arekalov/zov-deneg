package com.zovdeneg.app.ui.trade

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.usecase.LoadSecurityDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SecurityDetailUiState(
    val detail: SecurityDetail? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class SecurityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSecurityDetail: LoadSecurityDetailUseCase,
) : ViewModel() {
    private val ticker = savedStateHandle.get<String>("ticker").orEmpty()

    private val _uiState = MutableStateFlow(SecurityDetailUiState())
    val uiState: StateFlow<SecurityDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { SecurityDetailUiState(isLoading = true, loadFailed = false) }
            loadSecurityDetail(ticker).fold(
                onSuccess = { detail ->
                    _uiState.update {
                        SecurityDetailUiState(detail = detail, isLoading = false, loadFailed = false)
                    }
                },
                onFailure = {
                    _uiState.update { SecurityDetailUiState(detail = null, isLoading = false, loadFailed = true) }
                },
            )
        }
    }
}
