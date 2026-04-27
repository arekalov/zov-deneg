package com.zovdeneg.app.ui.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zovdeneg.app.domain.orders.UserOrder
import com.zovdeneg.app.domain.usecase.LoadUserOrdersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
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
    private val _uiState = MutableStateFlow(OrdersListUiState())
    val uiState: StateFlow<OrdersListUiState> = _uiState.asStateFlow()

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            loadUserOrders().fold(
                onSuccess = { list ->
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
}
