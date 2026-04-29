package com.zovdeneg.app.ui.orders

import com.zovdeneg.app.domain.orders.UserOrder
import com.zovdeneg.app.domain.usecase.LoadUserOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

data class OrdersListUiState(
    val orders: List<UserOrder> = emptyList(),
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
)

@HiltViewModel
class OrdersListViewModel @Inject constructor(
    private val loadUserOrders: LoadUserOrdersUseCase,
) : ViewModel() {

    private val _portfolioRefreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val portfolioRefreshRequests: SharedFlow<Unit> = _portfolioRefreshRequests.asSharedFlow()

    private val lastStatusByOrderId = mutableMapOf<String, String>()

    private var pollingJob: Job? = null

    private val _uiState = MutableStateFlow(OrdersListUiState())
    val uiState: StateFlow<OrdersListUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            loadUserOrders().fold(
                onSuccess = { list ->
                    rememberOrderStatuses(list)
                    _uiState.update {
                        OrdersListUiState(orders = list, isLoading = false, loadFailed = false)
                    }
                },
                onFailure = {
                    _uiState.update {
                        OrdersListUiState(orders = emptyList(), isLoading = false, loadFailed = true)
                    }
                },
            )
        }
    }

    fun startOrdersPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob =
            viewModelScope.launch {
                while (isActive) {
                    delay(ORDERS_POLL_INTERVAL_MS)
                    pollOrdersSilently()
                }
            }
    }

    fun stopOrdersPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    private suspend fun pollOrdersSilently() {
        val prev = lastStatusByOrderId.toMap()
        loadUserOrders().fold(
            onSuccess = { newList ->
                if (shouldNotifyPortfolioAfterExecution(prev, newList)) {
                    _portfolioRefreshRequests.tryEmit(Unit)
                }
                rememberOrderStatuses(newList)
                _uiState.update { it.copy(orders = newList, loadFailed = false) }
            },
            onFailure = { },
        )
    }

    private fun rememberOrderStatuses(list: List<UserOrder>) {
        lastStatusByOrderId.clear()
        lastStatusByOrderId.putAll(list.associate { it.id to it.status })
    }

    private companion object {
        const val ORDERS_POLL_INTERVAL_MS = 8_000L

        private val executedOrPartialStatuses = setOf("executed", "partial")

        private fun shouldNotifyPortfolioAfterExecution(
            prev: Map<String, String>,
            newList: List<UserOrder>,
        ): Boolean {
            if (prev.isEmpty()) return false
            return newList.any { order ->
                val was = prev[order.id] ?: return@any false
                val now = order.status
                was == "pending" && now in executedOrPartialStatuses
            }
        }
    }
}
