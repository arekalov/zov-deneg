package com.zovdeneg.app.ui.tabs

import com.zovdeneg.app.domain.market.SecurityKind
import com.zovdeneg.app.domain.market.SecurityListItem
import com.zovdeneg.app.domain.transactions.Transaction
import com.zovdeneg.app.domain.transactions.TransactionSide
import com.zovdeneg.app.domain.usecase.LoadPopularSecuritiesUseCase
import com.zovdeneg.app.domain.usecase.LoadTransactionsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

data class SearchTabUiState(
    val query: String = "",
    val filterIndex: Int = 0,
    val allSecurities: List<SecurityListItem> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
) {
    fun visibleSecurities(): List<SecurityListItem> {
        val byKind =
            allSecurities.filter { item ->
                when (filterIndex) {
                    1 -> item.kind == SecurityKind.STOCK
                    2 -> item.kind == SecurityKind.BOND
                    3 -> item.kind == SecurityKind.ETF
                    else -> true
                }
            }
        val q = query.trim().lowercase()
        if (q.isEmpty()) return byKind
        return byKind.filter { item ->
            item.ticker.lowercase().contains(q) || item.subtitle.lowercase().contains(q)
        }
    }
}

@HiltViewModel
class ZovSearchTabViewModel @Inject constructor(
    private val loadPopularSecurities: LoadPopularSecuritiesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchTabUiState())
    val uiState: StateFlow<SearchTabUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadPopularSecurities().fold(
                onSuccess = { list ->
                    _uiState.update {
                        it.copy(allSecurities = list, isLoading = false, loadFailed = false)
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, loadFailed = true) }
                },
            )
        }
    }

    fun setQuery(value: String) {
        _uiState.update { it.copy(query = value) }
    }

    fun setFilterIndex(index: Int) {
        _uiState.update { it.copy(filterIndex = index) }
    }
}

data class HistoryTabUiState(
    val filterIndex: Int = 0,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
) {
    fun visibleTransactions(): List<Transaction> =
        when (filterIndex) {
            1 -> transactions.filter { it.side == TransactionSide.PURCHASE }
            2 -> transactions.filter { it.side == TransactionSide.SALE }
            else -> transactions
        }
}

@HiltViewModel
class ZovHistoryTabViewModel @Inject constructor(
    private val loadTransactions: LoadTransactionsUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryTabUiState())
    val uiState: StateFlow<HistoryTabUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            loadTransactions().fold(
                onSuccess = { list ->
                    _uiState.update {
                        it.copy(transactions = list, isLoading = false, loadFailed = false)
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isLoading = false, loadFailed = true) }
                },
            )
        }
    }

    fun setFilterIndex(index: Int) {
        _uiState.update { it.copy(filterIndex = index) }
    }
}
