package com.zovdeneg.app.ui.trade

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.usecase.LoadSecurityDetailUseCase
import com.zovdeneg.app.domain.usecase.PlaceMarketBuyOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BuyUiState(
    val detail: SecurityDetail? = null,
    val lots: Int = 1,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitFailed: Boolean = false,
    val orderJustPlaced: Boolean = false,
)

@HiltViewModel
class BuyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSecurityDetail: LoadSecurityDetailUseCase,
    private val placeMarketBuyOrder: PlaceMarketBuyOrderUseCase,
) : ViewModel() {
    private val ticker = savedStateHandle.get<String>("ticker").orEmpty()

    private val _uiState = MutableStateFlow(BuyUiState())
    val uiState: StateFlow<BuyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadSecurityDetail(ticker).fold(
                onSuccess = { detail ->
                    _uiState.update {
                        BuyUiState(
                            detail = detail,
                            lots = 1,
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { BuyUiState(isLoading = false, loadFailed = true) }
                },
            )
        }
    }

    fun bumpLots(delta: Int) {
        _uiState.update { s ->
            val detail = s.detail ?: return@update s
            val next = (s.lots + delta).coerceIn(1, 99)
            s.copy(
                lots = next,
                submitFailed = false,
            )
        }
    }

    fun submitOrder() {
        val detail = _uiState.value.detail ?: return
        val lots = _uiState.value.lots
        val qty = lots * detail.lotSize
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitFailed = false) }
            placeMarketBuyOrder(detail.securityId, qty).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(isSubmitting = false, orderJustPlaced = true, submitFailed = false)
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isSubmitting = false, submitFailed = true) }
                },
            )
        }
    }

    fun acknowledgeOrderPlaced() {
        _uiState.update { it.copy(orderJustPlaced = false) }
    }
}
