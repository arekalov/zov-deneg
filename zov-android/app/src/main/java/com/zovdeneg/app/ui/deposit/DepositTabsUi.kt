package com.zovdeneg.app.ui.deposit

import com.zovdeneg.app.R
import com.zovdeneg.app.domain.balance.BrokerageBalance
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.components.ZovFilterChip
import com.zovdeneg.app.ui.components.ZovRubWholeAmountDigitsField
import com.zovdeneg.app.ui.theme.ZovTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

internal data class DepositTabModel(
    val chipResIds: List<Int>,
    val amountChip: Int,
    val balance: BrokerageBalance?,
    val isWorking: Boolean,
    val actionFailed: Boolean,
    val depositAmountDigits: String,
    val canSubmitDepositAmount: Boolean,
)

internal data class WithdrawTabModel(
    val balance: BrokerageBalance?,
    val withdrawAmountDigits: String,
    val canSubmitWithdrawAmount: Boolean,
    val isWorking: Boolean,
    val actionFailed: Boolean,
)

@Composable
internal fun DepositTabContent(
    model: DepositTabModel,
    onAmountChip: (Int) -> Unit,
    onDepositAmountDigitsChange: (String) -> Unit,
    onConfirmDeposit: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val balance = model.balance
    if (balance != null) {
        Text(
            stringResource(R.string.balance_available, balance.availableText),
            style = t.bodyReg14,
            color = c.onSurface,
        )
        Text(
            stringResource(R.string.balance_blocked, balance.blockedText),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
        Text(
            stringResource(R.string.balance_total, balance.totalText),
            style = t.subtitleReg13,
            color = c.onSurfaceVariant,
        )
    }
    ZovRubWholeAmountDigitsField(
        value = model.depositAmountDigits,
        onValueChange = onDepositAmountDigitsChange,
        label = stringResource(R.string.deposit_amount_label),
    )
    Spacer(Modifier.height(ZovItemSpacing))
    Row(horizontalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
        model.chipResIds.forEachIndexed { i, resId ->
            ZovFilterChip(
                label = stringResource(resId),
                selected = model.amountChip == i,
                onClick = { onAmountChip(i) },
            )
        }
    }
    if (model.actionFailed) {
        Text(stringResource(R.string.error_submit_deposit), style = t.bodyReg14, color = c.negative)
    }
    Button(
        onClick = onConfirmDeposit,
        enabled = !model.isWorking && model.canSubmitDepositAmount,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.deposit_action))
    }
}

@Composable
internal fun WithdrawTabContent(
    model: WithdrawTabModel,
    onWithdrawAmountDigitsChange: (String) -> Unit,
    onWithdraw: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val balance = model.balance
    if (balance != null) {
        Text(
            stringResource(R.string.balance_available, balance.availableText),
            style = t.bodyReg14,
            color = c.onSurface,
        )
        Spacer(Modifier.height(ZovItemSpacing))
    }
    ZovRubWholeAmountDigitsField(
        value = model.withdrawAmountDigits,
        onValueChange = onWithdrawAmountDigitsChange,
        label = stringResource(R.string.withdraw_amount_label),
    )
    if (model.actionFailed) {
        Text(
            stringResource(R.string.error_submit_withdraw),
            style = t.bodyReg14,
            color = c.negative,
        )
    }
    Button(
        onClick = onWithdraw,
        enabled = !model.isWorking && model.canSubmitWithdrawAmount,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text(stringResource(R.string.withdraw_action))
    }
}

@Composable
internal fun DepositScreenLoadedContent(
    state: DepositUiState,
    tab: Int,
    chipResIds: List<Int>,
    viewModel: DepositViewModel,
    onBack: () -> Unit,
) {
    if (tab == 0) {
        DepositTabContent(
            model = DepositTabModel(
                chipResIds = chipResIds,
                amountChip = state.selectedDepositChipIndex,
                balance = state.balance,
                isWorking = state.isWorking,
                actionFailed = state.actionFailed,
                depositAmountDigits = state.depositAmountDigits,
                canSubmitDepositAmount = state.canSubmitDepositAmount,
            ),
            onAmountChip = viewModel::selectDepositChip,
            onDepositAmountDigitsChange = viewModel::setDepositAmountDigits,
            onConfirmDeposit = viewModel::depositWithEnteredAmount,
        )
    } else {
        WithdrawTabContent(
            model = WithdrawTabModel(
                balance = state.balance,
                withdrawAmountDigits = state.withdrawAmountDigits,
                canSubmitWithdrawAmount = state.canSubmitWithdrawAmount,
                isWorking = state.isWorking,
                actionFailed = state.actionFailed,
            ),
            onWithdrawAmountDigitsChange = viewModel::setWithdrawAmountDigits,
            onWithdraw = viewModel::withdrawWithEnteredAmount,
        )
    }
    TextButton(onClick = onBack, modifier = Modifier.fillMaxWidth()) {
        Text(stringResource(R.string.action_back))
    }
}
