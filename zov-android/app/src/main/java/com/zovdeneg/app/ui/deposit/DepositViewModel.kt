package com.zovdeneg.app.ui.deposit

import com.zovdeneg.app.domain.balance.BrokerageBalance
import com.zovdeneg.app.domain.usecase.LoadBrokerageBalanceUseCase
import com.zovdeneg.app.domain.usecase.SubmitBrokerageDepositUseCase
import com.zovdeneg.app.domain.usecase.SubmitBrokerageWithdrawUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import javax.inject.Inject

data class DepositUiState(
    val balance: BrokerageBalance? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isWorking: Boolean = false,
    val actionFailed: Boolean = false,
    val selectedDepositChipIndex: Int = 2,
)

@HiltViewModel
class DepositViewModel @Inject constructor(
    private val loadBrokerageBalance: LoadBrokerageBalanceUseCase,
    private val submitBrokerageDeposit: SubmitBrokerageDepositUseCase,
    private val submitBrokerageWithdraw: SubmitBrokerageWithdrawUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(DepositUiState())
    val uiState: StateFlow<DepositUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadFailed = false) }
            loadBrokerageBalance().fold(
                onSuccess = { bal ->
                    _uiState.update {
                        it.copy(
                            balance = bal,
                            isLoading = false,
                            loadFailed = false,
                        )
                    }
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false, loadFailed = true) }
                },
            )
        }
    }

    fun selectDepositChip(index: Int) {
        _uiState.update { it.copy(selectedDepositChipIndex = index, actionFailed = false) }
    }

    fun depositSelectedAmount(amountDecimal: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, actionFailed = false) }
            submitBrokerageDeposit(amountDecimal).fold(
                onSuccess = { bal ->
                    _uiState.update {
                        it.copy(
                            balance = bal,
                            isWorking = false,
                        )
                    }
                },
                onFailure = { _uiState.update { it.copy(isWorking = false, actionFailed = true) } },
            )
        }
    }

    fun withdrawDemoAmount() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, actionFailed = false) }
            submitBrokerageWithdraw("1000.00").fold(
                onSuccess = { bal ->
                    _uiState.update {
                        it.copy(
                            balance = bal,
                            isWorking = false,
                        )
                    }
                },
                onFailure = { _uiState.update { it.copy(isWorking = false, actionFailed = true) } },
            )
        }
    }
}
