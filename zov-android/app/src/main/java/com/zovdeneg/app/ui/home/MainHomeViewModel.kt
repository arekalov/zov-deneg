package com.zovdeneg.app.ui.home

import com.zovdeneg.app.domain.portfolio.Holding
import com.zovdeneg.app.domain.usecase.LoadBrokerageBalanceUseCase
import com.zovdeneg.app.domain.usecase.LoadUserOrdersUseCase
import com.zovdeneg.app.domain.usecase.RefreshHomePortfolioUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

data class MainHomeUiState(
    val portfolioAmountRub: String? = null,
    val totalGainText: String? = null,
    /** Итого по брокерскому счёту с `GET /balance` (форматированная строка). */
    val brokerageTotalRub: String? = null,
    val holdings: List<Holding> = emptyList(),
    val activeOrdersCount: Int = 0,
    val isLoading: Boolean = true,
    /** Жест «потянуть вниз» на главной: обновление котировок и портфеля. */
    val isPullRefreshing: Boolean = false,
    val loadFailed: Boolean = false,
    /**
     * Монотонно растёт при каждом тихом опросе портфеля, чтобы [MutableStateFlow] не отбрасывал
     * обновление при тех же строках/списке (сравнение [equals] у data class).
     */
    val quietRefreshStamp: Long = 0L,
)

@HiltViewModel
class MainHomeViewModel @Inject constructor(
    private val refreshHomePortfolio: RefreshHomePortfolioUseCase,
    private val loadBrokerageBalance: LoadBrokerageBalanceUseCase,
    private val loadUserOrders: LoadUserOrdersUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MainHomeUiState())
    val uiState: StateFlow<MainHomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { loadPortfolioAndBalance() }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            loadPortfolioAndBalance()
        }
    }

    fun pullToRefresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isPullRefreshing = true, loadFailed = false) }
            try {
                loadPortfolioAndBalance()
            } finally {
                _uiState.update { it.copy(isPullRefreshing = false) }
            }
        }
    }

    /** Обновить портфель и баланс без полноэкранной загрузки (периодический опрос на главной). */
    fun refreshQuietly() {
        viewModelScope.launch {
            mergeLatestPortfolioAndBalanceQuietly()
        }
    }

    private suspend fun loadPortfolioAndBalance() {
        supervisorScope {
            val snapshotAsync = async { refreshHomePortfolio() }
            val balanceAsync = async { loadBrokerageBalance() }
            val ordersAsync = async { loadUserOrders() }
            val snapshot = snapshotAsync.await()
            val balanceResult = balanceAsync.await()
            val ordersResult = ordersAsync.await()
            val summary = snapshot.summary
            val holdings = snapshot.holdings
            val activeOrdersCount =
                ordersResult.getOrNull()?.count { it.isActive() } ?: 0
            _uiState.update {
                MainHomeUiState(
                    portfolioAmountRub = summary.getOrNull()?.portfolioAmountRub,
                    totalGainText = summary.getOrNull()?.totalGainText,
                    brokerageTotalRub = balanceResult.getOrNull()?.totalText,
                    holdings = holdings.getOrNull().orEmpty(),
                    activeOrdersCount = activeOrdersCount,
                    isLoading = false,
                    isPullRefreshing = false,
                    loadFailed = summary.isFailure,
                    quietRefreshStamp = 0L,
                )
            }
        }
    }

    private suspend fun mergeLatestPortfolioAndBalanceQuietly() {
        supervisorScope {
            val snapshotAsync = async { refreshHomePortfolio() }
            val balanceAsync = async { loadBrokerageBalance() }
            val ordersAsync = async { loadUserOrders() }
            val snapshot = snapshotAsync.await()
            val balanceResult = balanceAsync.await()
            val ordersResult = ordersAsync.await()
            val summary = snapshot.summary
            val holdings = snapshot.holdings
            _uiState.update { prev ->
                val nextActiveOrders =
                    ordersResult.getOrNull()?.count { it.isActive() }
                prev.copy(
                    portfolioAmountRub = summary.getOrNull()?.portfolioAmountRub
                        ?: prev.portfolioAmountRub,
                    totalGainText = summary.getOrNull()?.totalGainText
                        ?: prev.totalGainText,
                    brokerageTotalRub = balanceResult.getOrNull()?.totalText
                        ?: prev.brokerageTotalRub,
                    holdings = holdings.getOrNull() ?: prev.holdings,
                    activeOrdersCount = nextActiveOrders ?: prev.activeOrdersCount,
                    loadFailed = prev.loadFailed,
                    quietRefreshStamp = prev.quietRefreshStamp + 1L,
                )
            }
        }
    }

    companion object {
        /** Интервал тихого обновления данных на экране «Главная», пока он в foreground. */
        const val QUIET_REFRESH_INTERVAL_MS = 12_000L
    }
}
