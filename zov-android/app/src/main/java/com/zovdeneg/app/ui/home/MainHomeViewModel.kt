package com.zovdeneg.app.ui.home

import com.zovdeneg.app.domain.portfolio.Holding
import com.zovdeneg.app.domain.usecase.LoadBrokerageBalanceUseCase
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
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class MainHomeViewModel @Inject constructor(
    private val refreshHomePortfolio: RefreshHomePortfolioUseCase,
    private val loadBrokerageBalance: LoadBrokerageBalanceUseCase,
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
            val snapshot = snapshotAsync.await()
            val balanceResult = balanceAsync.await()
            val summary = snapshot.summary
            val holdings = snapshot.holdings
            _uiState.update {
                MainHomeUiState(
                    portfolioAmountRub = summary.getOrNull()?.portfolioAmountRub,
                    totalGainText = summary.getOrNull()?.totalGainText,
                    brokerageTotalRub = balanceResult.getOrNull()?.totalText,
                    holdings = holdings.getOrNull().orEmpty(),
                    isLoading = false,
                    loadFailed = summary.isFailure,
                )
            }
        }
    }

    private suspend fun mergeLatestPortfolioAndBalanceQuietly() {
        supervisorScope {
            val snapshotAsync = async { refreshHomePortfolio() }
            val balanceAsync = async { loadBrokerageBalance() }
            val snapshot = snapshotAsync.await()
            val balanceResult = balanceAsync.await()
            val summary = snapshot.summary
            val holdings = snapshot.holdings
            _uiState.update { prev ->
                prev.copy(
                    portfolioAmountRub = summary.getOrNull()?.portfolioAmountRub
                        ?: prev.portfolioAmountRub,
                    totalGainText = summary.getOrNull()?.totalGainText
                        ?: prev.totalGainText,
                    brokerageTotalRub = balanceResult.getOrNull()?.totalText
                        ?: prev.brokerageTotalRub,
                    holdings = holdings.getOrNull() ?: prev.holdings,
                    loadFailed = prev.loadFailed,
                )
            }
        }
    }

    companion object {
        /** Интервал тихого обновления данных на экране «Главная», пока он в foreground. */
        const val QUIET_REFRESH_INTERVAL_MS = 12_000L
    }
}
