package com.zovdeneg.app.ui.trade

import com.zovdeneg.app.domain.market.PriceHistoryPoint
import com.zovdeneg.app.domain.market.SecurityDetail
import com.zovdeneg.app.domain.usecase.LoadSecurityDetailUseCase
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
    val priceHistory: List<PriceHistoryPoint> = emptyList(),
    val chartRange: SecurityChartRange = SecurityChartRange.ONE_DAY,
    val chartLoading: Boolean = false,
    val chartFailed: Boolean = false,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class SecurityDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadSecurityDetail: LoadSecurityDetailUseCase,
    private val loadSecurityPriceHistory: LoadSecurityPriceHistoryUseCase,
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

    fun selectChartRange(range: SecurityChartRange) {
        val s = _uiState.value
        if (s.chartRange == range || s.detail == null) return
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

    private fun load() {
        viewModelScope.launch {
            _uiState.update {
                SecurityDetailUiState(
                    isLoading = true,
                    loadFailed = false,
                    chartLoading = false,
                    chartFailed = false,
                    chartRange = SecurityChartRange.ONE_DAY,
                )
            }
            loadSecurityDetail(ticker).fold(
                onSuccess = { detail ->
                    _uiState.update {
                        it.copy(
                            detail = detail,
                            isLoading = false,
                            loadFailed = false,
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
                            isLoading = false,
                            loadFailed = true,
                        )
                    }
                },
            )
        }
    }

    private suspend fun fetchPriceHistory(range: SecurityChartRange) {
        val to = Instant.now().epochSecond
        val from = to - range.rangeSeconds()
        loadSecurityPriceHistory(ticker, from, to).fold(
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
