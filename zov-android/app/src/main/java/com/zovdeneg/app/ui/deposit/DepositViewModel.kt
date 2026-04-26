package com.zovdeneg.app.ui.deposit

import com.zovdeneg.app.data.format.ZovRubDisplay
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

internal val depositAmountDecimals = listOf("1000.00", "5000.00", "10000.00", "50000.00")

/** Сумма вывода в одном запросе (decimal string для API). */
internal const val WITHDRAW_SELECTED_AMOUNT_DECIMAL: String = "1000.00"

enum class DepositSheetSuccess {
    DEPOSIT,
    WITHDRAW,
}

private fun depositLineForChipIndex(index: Int): String {
    val i = index.coerceIn(0, depositAmountDecimals.lastIndex)
    return ZovRubDisplay.formatApiDecimalToRubLine(depositAmountDecimals[i])
}

data class DepositUiState(
    val balance: BrokerageBalance? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isWorking: Boolean = false,
    val actionFailed: Boolean = false,
    val selectedDepositChipIndex: Int = 2,
    /** Отображаемая сумма выбранного чипа пополнения (из API не приходит отдельно). */
    val selectedDepositAmountLine: String = depositLineForChipIndex(2),
    /** Сумма запроса вывода (см. [WITHDRAW_SELECTED_AMOUNT_DECIMAL]). */
    val withdrawAmountLine: String = ZovRubDisplay.formatApiDecimalToRubLine(WITHDRAW_SELECTED_AMOUNT_DECIMAL),
    val pendingSuccess: DepositSheetSuccess? = null,
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
        _uiState.update {
            it.copy(
                selectedDepositChipIndex = index,
                selectedDepositAmountLine = depositLineForChipIndex(index),
                actionFailed = false,
            )
        }
    }

    fun depositSelectedChipAmount() {
        val amount =
            depositAmountDecimals[
                _uiState.value.selectedDepositChipIndex.coerceIn(
                    0,
                    depositAmountDecimals.lastIndex,
                ),
            ]
        depositSelectedAmount(amount)
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
                            pendingSuccess = DepositSheetSuccess.DEPOSIT,
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
            submitBrokerageWithdraw(WITHDRAW_SELECTED_AMOUNT_DECIMAL).fold(
                onSuccess = { bal ->
                    _uiState.update {
                        it.copy(
                            balance = bal,
                            isWorking = false,
                            pendingSuccess = DepositSheetSuccess.WITHDRAW,
                        )
                    }
                },
                onFailure = { _uiState.update { it.copy(isWorking = false, actionFailed = true) } },
            )
        }
    }

    fun acknowledgePendingSuccess() {
        _uiState.update { it.copy(pendingSuccess = null) }
    }
}
