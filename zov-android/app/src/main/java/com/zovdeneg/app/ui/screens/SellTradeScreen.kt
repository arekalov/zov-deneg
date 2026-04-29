package com.zovdeneg.app.ui.screens

import com.zovdeneg.app.R
import com.zovdeneg.app.ui.components.LocalZovSnackbarHostState
import com.zovdeneg.app.ui.components.LocalZovSnackbarScope
import com.zovdeneg.app.ui.components.ZovCenteredCircularProgress
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.theme.ZovTheme
import com.zovdeneg.app.ui.trade.SellSubmitHint
import com.zovdeneg.app.ui.trade.SellUiState
import com.zovdeneg.app.ui.trade.SellViewModel

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle

import kotlinx.coroutines.launch

@Composable
private fun SellSubmitHintBanner(hint: SellSubmitHint) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    when (val h = hint) {
        SellSubmitHint.None -> {}
        SellSubmitHint.CannotSellMinimalLot ->
            Text(
                stringResource(R.string.sell_precheck_cannot_lot),
                style = t.bodyReg14,
                color = c.negative,
            )

        is SellSubmitHint.InsufficientSecuritiesFromApi -> {
            val msg = h.serverMessage?.trim().orEmpty()
            Text(
                if (msg.isNotEmpty()) {
                    stringResource(R.string.sell_error_insufficient_securities_reason, msg)
                } else {
                    stringResource(R.string.sell_error_insufficient_securities)
                },
                style = t.bodyReg14,
                color = c.negative,
            )
        }

        SellSubmitHint.GenericFailure ->
            Text(stringResource(R.string.error_submit_order), style = t.bodyReg14, color = c.negative)
    }
}

@Composable
private fun SellScreenOrderContent(
    state: SellUiState,
    label: String,
    viewModel: SellViewModel,
    onBack: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    when {
        state.isLoading -> ZovCenteredCircularProgress()
        state.loadFailed -> {
            Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
            Button(onClick = onBack) { Text(stringResource(R.string.action_back)) }
        }
        state.detail != null -> {
            val detail = requireNotNull(state.detail)
            Text(
                stringResource(R.string.sell_title_format, label),
                style = t.sectionSemi16,
                color = c.onSurface,
            )
            Text(stringResource(R.string.buy_lots_label), style = t.labelMed12, color = c.onSurfaceVariant)
            MarketOrderLotsRow(
                lots = state.lots,
                isSubmitting = state.isSubmitting,
                maxLots = state.maxSellLots,
                onBump = viewModel::bumpLots,
            )
            Text(
                stringResource(
                    R.string.sell_order_hint,
                    state.lots * detail.lotSize,
                    detail.priceLine,
                ),
                style = t.subtitleReg14,
                color = c.onSurfaceVariant,
            )
            SellSubmitHintBanner(state.submitHint)
            OutlinedButton(
                onClick = { viewModel.submitOrder() },
                enabled = state.canSubmitMarketSell,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = c.negative),
            ) { Text(stringResource(R.string.action_place_order), style = t.bodyMed14) }
        }
    }
}

@Composable
fun SellScreen(
    viewModel: SellViewModel,
    onBack: () -> Unit,
    onPortfolioChanged: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalZovSnackbarHostState.current
    val snackbarScope = LocalZovSnackbarScope.current
    val successMsg = stringResource(R.string.order_submitted)
    LaunchedEffect(state.orderJustPlaced) {
        if (!state.orderJustPlaced) return@LaunchedEffect
        snackbarScope.launch { snackbarHostState.showSnackbar(successMsg) }
        onPortfolioChanged()
        viewModel.acknowledgeOrderPlaced()
        onBack()
    }
    val label =
        state.displayTicker.ifBlank { state.detail?.ticker.orEmpty() }.replace('_', '/')
    ZovScrollScreen {
        SellScreenOrderContent(
            state = state,
            label = label,
            viewModel = viewModel,
            onBack = onBack,
        )
    }
}
