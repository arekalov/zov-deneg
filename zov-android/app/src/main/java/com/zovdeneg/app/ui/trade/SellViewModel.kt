package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.orders.InsufficientSecuritiesForOrderException
import com.zovdeneg.app.domain.usecase.LoadSecurityDetailUseCase
import com.zovdeneg.app.domain.usecase.PlaceMarketSellOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

sealed class SellSubmitHint {
    data object None : SellSubmitHint()

    /** В портфеле меньше одного лота или позиция неизвестна. */
    data object CannotSellMinimalLot : SellSubmitHint()

    data class InsufficientSecuritiesFromApi(val serverMessage: String?) : SellSubmitHint()

    data object GenericFailure : SellSubmitHint()
}

data class SellUiState(
    val displayTicker: String = "",
    val detail: SecurityDetail? = null,
    val lots: Int = 1,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitHint: SellSubmitHint = SellSubmitHint.None,
    val orderJustPlaced: Boolean = false,
) {
    val maxSellLots: Int
        get() =
            detail?.let { d ->
                val q = d.portfolioQuantity ?: return@let 0
                if (q < 1 || d.lotSize < 1) {
                    0
                } else {
                    q / d.lotSize
                }
            } ?: 0

    val canSubmitMarketSell: Boolean
        get() =
            detail != null &&
                !isLoading &&
                !loadFailed &&
                !isSubmitting &&
                maxSellLots >= 1 &&
                lots in 1..maxSellLots &&
                submitHint !is SellSubmitHint.CannotSellMinimalLot
}

@HiltViewModel
class SellViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSecurityDetail: LoadSecurityDetailUseCase,
    private val placeMarketSellOrder: PlaceMarketSellOrderUseCase,
) : ViewModel() {
    private val securityNavId =
        savedStateHandle.get<String>("securityId").orEmpty().replace('_', '/')
    private val displayTicker =
        savedStateHandle.get<String>("displayTicker").orEmpty().replace('_', '/')

    private val _uiState = MutableStateFlow(SellUiState(displayTicker = displayTicker))
    val uiState: StateFlow<SellUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadSecurityDetail(securityNavId).fold(
                onSuccess = { detail ->
                    val maxLots = maxSellLotsForDetail(detail)
                    val hint =
                        if (maxLots < 1) {
                            SellSubmitHint.CannotSellMinimalLot
                        } else {
                            SellSubmitHint.None
                        }
                    _uiState.update {
                        SellUiState(
                            displayTicker = displayTicker,
                            detail = detail,
                            lots = 1.coerceIn(1, maxLots.coerceAtLeast(1)),
                            isLoading = false,
                            loadFailed = false,
                            submitHint = hint,
                        )
                    }
                },
                onFailure = {
                    _uiState.update {
                        SellUiState(
                            displayTicker = displayTicker,
                            isLoading = false,
                            loadFailed = true,
                        )
                    }
                },
            )
        }
    }

    fun bumpLots(delta: Int) {
        _uiState.update { s ->
            val cap = s.maxSellLots
            if (cap < 1) return@update s
            val next = (s.lots + delta).coerceIn(1, cap)
            s.copy(lots = next, submitHint = SellSubmitHint.None)
        }
    }

    fun submitOrder() {
        if (!_uiState.value.canSubmitMarketSell) return
        val detail = _uiState.value.detail ?: return
        val lots = _uiState.value.lots
        val qty = lots * detail.lotSize
        val orderSecurityId = securityNavId.ifBlank { detail.securityId }
        if (orderSecurityId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitHint = SellSubmitHint.None) }
            placeMarketSellOrder(orderSecurityId, qty).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(isSubmitting = false, orderJustPlaced = true, submitHint = SellSubmitHint.None)
                    }
                },
                onFailure = { e ->
                    val hint =
                        when (e) {
                            is InsufficientSecuritiesForOrderException ->
                                SellSubmitHint.InsufficientSecuritiesFromApi(e.message)

                            else -> SellSubmitHint.GenericFailure
                        }
                    _uiState.update { s -> s.copy(isSubmitting = false, submitHint = hint) }
                },
            )
        }
    }

    fun acknowledgeOrderPlaced() {
        _uiState.update { it.copy(orderJustPlaced = false) }
    }

    private fun maxSellLotsForDetail(detail: SecurityDetail): Int {
        val q = detail.portfolioQuantity ?: return 0
        if (q < 1 || detail.lotSize < 1) return 0
        return q / detail.lotSize
    }
}
