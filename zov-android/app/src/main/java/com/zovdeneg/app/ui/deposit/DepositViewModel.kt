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

import java.math.BigDecimal
import java.math.RoundingMode

import javax.inject.Inject

internal val depositAmountDecimals = listOf("1000.00", "5000.00", "10000.00", "50000.00")

private const val MAX_AMOUNT_DIGITS = 15

private fun chipIndexToAmountDigits(index: Int): String =
    depositAmountDecimals[index.coerceIn(0, depositAmountDecimals.lastIndex)].substringBefore('.')

private fun digitsOnlyFiltered(input: String): String =
    input.filter { it.isDigit() }.take(MAX_AMOUNT_DIGITS)

/** Целые рубли (строка из цифр) → строка для API (`1234.00`). `null`, если пусто или не положительно. */
private fun wholeRublesDigitsToApiDecimal(digits: String): String? {
    val d = digits.filter { it.isDigit() }
    if (d.isEmpty()) return null
    val bd = runCatching { BigDecimal(d) }.getOrNull() ?: return null
    if (bd <= BigDecimal.ZERO) return null
    return bd.setScale(2, RoundingMode.HALF_UP).toPlainString()
}

enum class DepositSheetSuccess {
    DEPOSIT,
    WITHDRAW,
}

data class DepositUiState(
    val balance: BrokerageBalance? = null,
    val isLoading: Boolean = true,
    val loadFailed: Boolean = false,
    val isWorking: Boolean = false,
    val actionFailed: Boolean = false,
    val selectedDepositChipIndex: Int = 2,
    /** Сумма пополнения: только цифры, целые рубли (например `10000`). */
    val depositAmountDigits: String = chipIndexToAmountDigits(2),
    /** Сумма вывода: только цифры, целые рубли. */
    val withdrawAmountDigits: String = "1000",
    val pendingSuccess: DepositSheetSuccess? = null,
) {
    val canSubmitDepositAmount: Boolean get() = wholeRublesDigitsToApiDecimal(depositAmountDigits) != null

    val canSubmitWithdrawAmount: Boolean get() = wholeRublesDigitsToApiDecimal(withdrawAmountDigits) != null
}

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
                depositAmountDigits = chipIndexToAmountDigits(index),
                actionFailed = false,
            )
        }
    }

    fun setDepositAmountDigits(value: String) {
        _uiState.update {
            it.copy(
                depositAmountDigits = digitsOnlyFiltered(value),
                actionFailed = false,
            )
        }
    }

    fun setWithdrawAmountDigits(value: String) {
        _uiState.update {
            it.copy(
                withdrawAmountDigits = digitsOnlyFiltered(value),
                actionFailed = false,
            )
        }
    }

    fun depositWithEnteredAmount() {
        val amount = wholeRublesDigitsToApiDecimal(_uiState.value.depositAmountDigits) ?: return
        depositSelectedAmount(amount)
    }

    private fun depositSelectedAmount(amountDecimal: String) {
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

    fun withdrawWithEnteredAmount() {
        val amount = wholeRublesDigitsToApiDecimal(_uiState.value.withdrawAmountDigits) ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, actionFailed = false) }
            submitBrokerageWithdraw(amount).fold(
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
