package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.domain.balance.BrokerageBalance
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.orders.InsufficientFundsForOrderException
import com.zovdeneg.app.domain.usecase.LoadBrokerageBalanceUseCase
import com.zovdeneg.app.domain.usecase.LoadSecurityDetailUseCase
import com.zovdeneg.app.domain.usecase.PlaceMarketBuyOrderUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import java.math.BigDecimal

import javax.inject.Inject

sealed class BuySubmitHint {
    data object None : BuySubmitHint()

    /** Оценка по стакану (лучший ask × шт.) и доступному остатку. */
    data object EstimatedInsufficientBalance : BuySubmitHint()

    /** Доступно по счёту 0 ₽ или меньше после загрузки баланса. */
    data object NoAvailableFunds : BuySubmitHint()

    /** Баланс не пришёл или поле «доступно» не распознано — не отправляем заявку с клиента. */
    data object BalanceUnavailable : BuySubmitHint()

    data class InsufficientFundsFromApi(val serverMessage: String?) : BuySubmitHint()

    data object GenericFailure : BuySubmitHint()
}

data class BuyUiState(
    val displayTicker: String = "",
    val detail: SecurityDetail? = null,
    val balance: BrokerageBalance? = null,
    val lots: Int = 1,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isSubmitting: Boolean = false,
    val submitHint: BuySubmitHint = BuySubmitHint.None,
    val orderJustPlaced: Boolean = false,
) {
    val canSubmitMarketBuy: Boolean
        get() =
            detail != null &&
                !isLoading &&
                !loadFailed &&
                !isSubmitting &&
                when (submitHint) {
                    BuySubmitHint.EstimatedInsufficientBalance,
                    BuySubmitHint.NoAvailableFunds,
                    BuySubmitHint.BalanceUnavailable,
                    -> false

                    else -> true
                }
}

@HiltViewModel
class BuyViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSecurityDetail: LoadSecurityDetailUseCase,
    private val loadBrokerageBalance: LoadBrokerageBalanceUseCase,
    private val placeMarketBuyOrder: PlaceMarketBuyOrderUseCase,
) : ViewModel() {
    private val securityNavId =
        savedStateHandle.get<String>("securityId").orEmpty().replace('_', '/')
    private val displayTicker =
        savedStateHandle.get<String>("displayTicker").orEmpty().replace('_', '/')

    private val _uiState = MutableStateFlow(BuyUiState(displayTicker = displayTicker))
    val uiState: StateFlow<BuyUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            coroutineScope {
                val detailAsync = async { loadSecurityDetail(securityNavId) }
                val balanceAsync = async { loadBrokerageBalance() }
                val detailResult = detailAsync.await()
                val balanceResult = balanceAsync.await()
                val balance = balanceResult.getOrNull()
                detailResult.fold(
                    onSuccess = { detail ->
                        _uiState.update {
                            BuyUiState(
                                displayTicker = displayTicker,
                                detail = detail,
                                balance = balance,
                                lots = 1,
                                isLoading = false,
                                loadFailed = false,
                            ).withRecalculatedHint()
                        }
                    },
                    onFailure = {
                        _uiState.update {
                            BuyUiState(
                                displayTicker = displayTicker,
                                balance = balance,
                                isLoading = false,
                                loadFailed = true,
                            )
                        }
                    },
                )
            }
        }
    }

    fun bumpLots(delta: Int) {
        _uiState.update { s ->
            val detail = s.detail ?: return@update s
            val next = (s.lots + delta).coerceIn(1, 99)
            s.copy(lots = next, submitHint = BuySubmitHint.None).withRecalculatedHint()
        }
    }

    fun submitOrder() {
        if (!_uiState.value.canSubmitMarketBuy) return
        val detail = _uiState.value.detail ?: return
        val lots = _uiState.value.lots
        val qty = lots * detail.lotSize
        val orderSecurityId = securityNavId.ifBlank { detail.securityId }
        if (orderSecurityId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, submitHint = BuySubmitHint.None) }
            placeMarketBuyOrder(orderSecurityId, qty).fold(
                onSuccess = {
                    _uiState.update { s ->
                        s.copy(isSubmitting = false, orderJustPlaced = true, submitHint = BuySubmitHint.None)
                    }
                },
                onFailure = { e ->
                    val hint =
                        when (e) {
                            is InsufficientFundsForOrderException ->
                                BuySubmitHint.InsufficientFundsFromApi(e.message)

                            else -> BuySubmitHint.GenericFailure
                        }
                    _uiState.update { s -> s.copy(isSubmitting = false, submitHint = hint) }
                },
            )
        }
    }

    fun acknowledgeOrderPlaced() {
        _uiState.update { it.copy(orderJustPlaced = false) }
    }

    private fun BuyUiState.withRecalculatedHint(): BuyUiState =
        copy(submitHint = computeBalancePrecheckHint(this))

    private fun computeBalancePrecheckHint(state: BuyUiState): BuySubmitHint {
        val detail = state.detail ?: return BuySubmitHint.None
        val balance = state.balance ?: return BuySubmitHint.BalanceUnavailable
        val available = parseAvailableRub(balance) ?: return BuySubmitHint.BalanceUnavailable
        if (available <= BigDecimal.ZERO) {
            return BuySubmitHint.NoAvailableFunds
        }
        val estimate = estimateMarketBuyUpperBoundRub(detail, state.lots)
        if (estimate == null) {
            return BuySubmitHint.None
        }
        return if (estimate > available) {
            BuySubmitHint.EstimatedInsufficientBalance
        } else {
            BuySubmitHint.None
        }
    }

    private fun parseAvailableRub(balance: BrokerageBalance): BigDecimal? {
        val raw = balance.availableDecimal.trim().replace(',', '.')
        if (raw.isEmpty()) return null
        return runCatching { BigDecimal(raw) }.getOrNull()
    }

    private fun estimateMarketBuyUpperBoundRub(detail: SecurityDetail, lots: Int): BigDecimal? {
        val priceRaw =
            detail.orderBook?.bestAskPriceDecimal?.trim()?.replace(',', '.').orEmpty()
        if (priceRaw.isEmpty()) return null
        val perUnit = runCatching { BigDecimal(priceRaw) }.getOrNull() ?: return null
        val qty = lots * detail.lotSize
        return perUnit.multiply(BigDecimal.valueOf(qty.toLong()))
    }
}
