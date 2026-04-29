package com.zovdeneg.app.ui.tabs

import com.zovdeneg.app.domain.market.SecurityListItem
import com.zovdeneg.app.domain.transactions.Transaction
import com.zovdeneg.app.domain.usecase.LoadSecuritiesPageUseCase
import com.zovdeneg.app.domain.usecase.LoadTransactionsPageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

private const val SECURITIES_LIST_PAGE_SIZE = 20

private const val TRANSACTIONS_LIST_PAGE_SIZE = 4
private const val SEARCH_DEBOUNCE_MS = 400L

data class SearchTabUiState(
    val query: String = "",
    val filterIndex: Int = 0,
    val securities: List<SecurityListItem> = emptyList(),
    val lastLoadedPage: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class ZovSearchTabViewModel @Inject constructor(
    private val loadSecuritiesPage: LoadSecuritiesPageUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(SearchTabUiState())
    val uiState: StateFlow<SearchTabUiState> = _uiState.asStateFlow()

    private var searchDebounceJob: Job? = null

    init {
        viewModelScope.launch { fetchSecuritiesPage(page = 1, append = false) }
    }

    fun setQuery(value: String) {
        _uiState.update { it.copy(query = value) }
        searchDebounceJob?.cancel()
        searchDebounceJob =
            viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_MS)
                _uiState.update {
                    it.copy(
                        securities = emptyList(),
                        lastLoadedPage = 0,
                        hasMore = false,
                        isLoading = true,
                        loadFailed = false,
                    )
                }
                fetchSecuritiesPage(page = 1, append = false)
            }
    }

    fun setFilterIndex(index: Int) {
        if (index == _uiState.value.filterIndex) return
        searchDebounceJob?.cancel()
        _uiState.update {
            it.copy(
                filterIndex = index,
                securities = emptyList(),
                lastLoadedPage = 0,
                hasMore = false,
                isLoading = true,
                loadFailed = false,
            )
        }
        viewModelScope.launch { fetchSecuritiesPage(page = 1, append = false) }
    }

    fun loadMore() {
        val s = _uiState.value
        if (!s.hasMore || s.isLoadingMore || s.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            fetchSecuritiesPage(page = s.lastLoadedPage + 1, append = true)
        }
    }

    private fun searchApiType(filterIndex: Int): String? =
        when (filterIndex) {
            1 -> "stock"
            2 -> "bond"
            3 -> "etf"
            else -> null
        }

    private suspend fun fetchSecuritiesPage(page: Int, append: Boolean) {
        val snapshot = _uiState.value
        val type = searchApiType(snapshot.filterIndex)
        val query = snapshot.query.trim()
        loadSecuritiesPage(query, type, page, SECURITIES_LIST_PAGE_SIZE).fold(
            onSuccess = { env ->
                _uiState.update {
                    val merged = if (append) it.securities + env.items else env.items
                    it.copy(
                        securities = merged,
                        lastLoadedPage = env.page,
                        hasMore = env.hasNextPage,
                        isLoading = false,
                        isLoadingMore = false,
                        loadFailed = false,
                    )
                }
            },
            onFailure = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        loadFailed = true,
                    )
                }
            },
        )
    }
}

data class HistoryTabUiState(
    val filterIndex: Int = 0,
    val transactions: List<Transaction> = emptyList(),
    val lastLoadedPage: Int = 0,
    val hasMore: Boolean = false,
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class ZovHistoryTabViewModel @Inject constructor(
    private val loadTransactionsPage: LoadTransactionsPageUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HistoryTabUiState())
    val uiState: StateFlow<HistoryTabUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { fetchTransactionsPage(page = 1, append = false) }
    }

    fun setFilterIndex(index: Int) {
        if (index == _uiState.value.filterIndex) return
        _uiState.update {
            it.copy(
                filterIndex = index,
                transactions = emptyList(),
                lastLoadedPage = 0,
                hasMore = false,
                isLoading = true,
                loadFailed = false,
            )
        }
        viewModelScope.launch { fetchTransactionsPage(page = 1, append = false) }
    }

    fun loadMore() {
        val s = _uiState.value
        if (!s.hasMore || s.isLoadingMore || s.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingMore = true) }
            fetchTransactionsPage(page = s.lastLoadedPage + 1, append = true)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    loadFailed = false,
                    transactions = emptyList(),
                    lastLoadedPage = 0,
                    hasMore = false,
                )
            }
            fetchTransactionsPage(page = 1, append = false)
        }
    }

    private fun historyApiType(): String? =
        when (_uiState.value.filterIndex) {
            1 -> "buy"
            2 -> "sell"
            3 -> "deposit"
            4 -> "withdrawal"
            else -> null
        }

    private suspend fun fetchTransactionsPage(page: Int, append: Boolean) {
        val type = historyApiType()
        loadTransactionsPage(page, TRANSACTIONS_LIST_PAGE_SIZE, type).fold(
            onSuccess = { env ->
                _uiState.update {
                    val merged = if (append) it.transactions + env.items else env.items
                    it.copy(
                        transactions = merged,
                        lastLoadedPage = env.page,
                        hasMore = env.hasNextPage,
                        isLoading = false,
                        isLoadingMore = false,
                        loadFailed = false,
                    )
                }
            },
            onFailure = {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        loadFailed = true,
                    )
                }
            },
        )
    }
}
