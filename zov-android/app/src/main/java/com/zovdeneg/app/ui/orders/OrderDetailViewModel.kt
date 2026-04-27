package com.zovdeneg.app.ui.orders

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.orders.UserOrder
import com.zovdeneg.app.domain.usecase.CancelOrderUseCase
import com.zovdeneg.app.domain.usecase.LoadOrderDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OrderDetailUiState(
    val order: UserOrder? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isCancelling: Boolean = false,
    val cancelFailed: Boolean = false,
    val cancelSucceeded: Boolean = false,
)

@HiltViewModel
class OrderDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val loadOrderDetail: LoadOrderDetailUseCase,
    private val cancelOrder: CancelOrderUseCase,
) : ViewModel() {
    private val orderId = savedStateHandle.get<String>("orderId").orEmpty().replace('_', '/')

    private val _uiState = MutableStateFlow(OrderDetailUiState())
    val uiState: StateFlow<OrderDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch { load() }
    }

    fun retry() {
        viewModelScope.launch { load() }
    }

    fun cancel() {
        val id = _uiState.value.order?.id ?: orderId
        if (id.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCancelling = true, cancelFailed = false) }
            cancelOrder(id).fold(
                onSuccess = {
                    _uiState.update {
                        it.copy(
                            isCancelling = false,
                            cancelSucceeded = true,
                            order = it.order?.copy(status = "cancelled"),
                        )
                    }
                },
                onFailure = {
                    _uiState.update { s -> s.copy(isCancelling = false, cancelFailed = true) }
                },
            )
        }
    }

    fun acknowledgeCancelSuccess() {
        _uiState.update { it.copy(cancelSucceeded = false) }
    }

    private suspend fun load() {
        _uiState.update {
            OrderDetailUiState(isLoading = true, loadFailed = false, cancelFailed = false)
        }
        loadOrderDetail(orderId).fold(
            onSuccess = { order ->
                _uiState.update {
                    OrderDetailUiState(order = order, isLoading = false, loadFailed = false)
                }
            },
            onFailure = {
                _uiState.update {
                    OrderDetailUiState(order = null, isLoading = false, loadFailed = true)
                }
            },
        )
    }
}
