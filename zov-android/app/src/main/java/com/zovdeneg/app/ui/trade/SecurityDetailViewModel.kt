package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.domain.market.PriceHistoryPoint
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.usecase.LoadSecurityDetailUseCase
import com.zovdeneg.app.domain.usecase.LoadSecurityOrderBookUseCase
import com.zovdeneg.app.domain.usecase.LoadSecurityPriceHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import java.time.Instant

import javax.inject.Inject

data class SecurityDetailUiState(
    val detail: SecurityDetail? = null,
    /** Не удалось загрузить карточку бумаги (стакан и график могут жить отдельно). */
    val detailFailed: Boolean = false,
    val priceHistory: List<PriceHistoryPoint> = emptyList(),
    val chartRange: SecurityChartRange = SecurityChartRange.ONE_DAY,
    val chartLoading: Boolean = false,
    val chartFailed: Boolean = false,
    val orderBookLoading: Boolean = false,
    val orderBookLoadFailed: Boolean = false,
    val isLoading: Boolean = true,
)

@HiltViewModel
class SecurityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSecurityDetail: LoadSecurityDetailUseCase,
    private val loadSecurityOrderBook: LoadSecurityOrderBookUseCase,
    private val loadSecurityPriceHistory: LoadSecurityPriceHistoryUseCase,
) : ViewModel() {
    private val securityNavId =
        savedStateHandle.get<String>("securityId").orEmpty().replace('_', '/')

    private var orderBookFetchStarted: Boolean = false

    private val _uiState = MutableStateFlow(SecurityDetailUiState())
    val uiState: StateFlow<SecurityDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        orderBookFetchStarted = false
        load()
    }

    fun selectChartRange(range: SecurityChartRange) {
        val s = _uiState.value
        if (s.chartRange == range) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    chartRange = range,
                    chartLoading = true,
                    chartFailed = false,
                    priceHistory = emptyList(),
                )
            }
            fetchPriceHistory(range)
        }
    }

    fun loadOrderBookIfNeeded() {
        val d = _uiState.value.detail ?: return
        if (d.orderBook != null || orderBookFetchStarted) return
        orderBookFetchStarted = true
        viewModelScope.launch {
            _uiState.update { it.copy(orderBookLoading = true, orderBookLoadFailed = false) }
            loadSecurityOrderBook(d.securityId).fold(
                onSuccess = { book ->
                    _uiState.update { s ->
                        val cur = s.detail ?: return@update s
                        s.copy(
                            detail = cur.copy(orderBook = book),
                            orderBookLoading = false,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(orderBookLoading = false, orderBookLoadFailed = true) }
                },
            )
        }
    }

    private fun load() {
        viewModelScope.launch {
            orderBookFetchStarted = false
            _uiState.update {
                SecurityDetailUiState(
                    isLoading = true,
                    detailFailed = false,
                    chartLoading = false,
                    chartFailed = false,
                    orderBookLoading = false,
                    orderBookLoadFailed = false,
                    chartRange = SecurityChartRange.ONE_DAY,
                )
            }
            loadSecurityDetail(securityNavId).fold(
                onSuccess = { detail ->
                    _uiState.update {
                        it.copy(
                            detail = detail,
                            isLoading = false,
                            detailFailed = false,
                            chartLoading = true,
                            chartFailed = false,
                            priceHistory = emptyList(),
                        )
                    }
                    fetchPriceHistory(_uiState.value.chartRange)
                },
                onFailure = {
                    _uiState.update {
                        SecurityDetailUiState(
                            detail = null,
                            detailFailed = true,
                            isLoading = false,
                            chartLoading = true,
                            chartFailed = false,
                            priceHistory = emptyList(),
                            chartRange = SecurityChartRange.ONE_DAY,
                        )
                    }
                    fetchPriceHistory(SecurityChartRange.ONE_DAY)
                },
            )
        }
    }

    private suspend fun fetchPriceHistory(range: SecurityChartRange) {
        val to = Instant.now().epochSecond
        val from = to - range.rangeSeconds()
        loadSecurityPriceHistory(securityNavId, from, to).fold(
            onSuccess = { history ->
                _uiState.update {
                    it.copy(
                        priceHistory = history.points,
                        chartLoading = false,
                        chartFailed = false,
                    )
                }
            },
            onFailure = {
                _uiState.update {
                    it.copy(
                        priceHistory = emptyList(),
                        chartLoading = false,
                        chartFailed = true,
                    )
                }
            },
        )
    }
}
