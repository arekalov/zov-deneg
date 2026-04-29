package com.zovdeneg.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.zovdeneg.app.R
import com.zovdeneg.app.domain.orders.UserOrder
import com.zovdeneg.app.ui.common.ZovItemSpacing
import com.zovdeneg.app.ui.common.ZovUnit
import com.zovdeneg.app.ui.components.LocalZovSnackbarHostState
import com.zovdeneg.app.ui.components.ZovCenteredCircularProgress
import com.zovdeneg.app.ui.components.ZovElevatedListCard
import com.zovdeneg.app.ui.components.ZovScrollScreen
import com.zovdeneg.app.ui.orders.OrderDetailViewModel
import com.zovdeneg.app.ui.orders.OrdersListViewModel
import com.zovdeneg.app.ui.theme.ZovTheme
import java.time.Instant
import kotlinx.coroutines.flow.collect
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun OrdersListScreen(
    viewModel: OrdersListViewModel = hiltViewModel(),
    onOpenOrder: (String) -> Unit,
    onPortfolioMayHaveChanged: () -> Unit = {},
) {
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    DisposableEffect(viewModel) {
        viewModel.startOrdersPolling()
        onDispose { viewModel.stopOrdersPolling() }
    }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val onPortfolioRefresh by rememberUpdatedState(onPortfolioMayHaveChanged)
    LaunchedEffect(viewModel, lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.portfolioRefreshRequests.collect {
                onPortfolioRefresh()
            }
        }
    }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovScrollScreen {
        when {
            state.isLoading -> ZovCenteredCircularProgress()
            state.loadFailed ->
                Column(verticalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
                    Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                    Button(onClick = { viewModel.refresh() }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }

            state.orders.isEmpty() ->
                Text(
                    stringResource(R.string.orders_empty),
                    style = t.subtitleReg14,
                    color = c.onSurfaceVariant,
                )

            else ->
                state.orders.forEachIndexed { index, order ->
                    if (index > 0) {
                        Spacer(Modifier.height(ZovItemSpacing))
                    }
                    OrderListRowCard(order = order, onClick = { onOpenOrder(order.id) })
                }
        }
    }
}

@Composable
private fun OrderListRowCard(
    order: UserOrder,
    onClick: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovElevatedListCard(modifier = Modifier.clickable(onClick = onClick)) {
        Text(
            orderLineTitle(order),
            style = t.bodyMed14,
            color = c.onSurface,
        )
        Text(
            formatOrderInstant(order.createdAtEpochSeconds),
            style = t.labelReg12,
            color = c.onSurfaceVariant,
        )
        Spacer(Modifier.height(ZovUnit))
        Text(
            orderStatusLabel(order.status),
            style = t.sectionSemi16,
            color = c.onSurface,
        )
    }
}

@Composable
fun OrderDetailScreen(
    viewModel: OrderDetailViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onCancelSuccess: (() -> Unit)? = null,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalZovSnackbarHostState.current
    val cancelledMsg = stringResource(R.string.order_cancelled_snackbar)
    LaunchedEffect(state.cancelSucceeded) {
        if (!state.cancelSucceeded) return@LaunchedEffect
        snackbarHostState.showSnackbar(cancelledMsg)
        viewModel.acknowledgeCancelSuccess()
        if (onCancelSuccess != null) {
            onCancelSuccess()
        } else {
            onBack()
        }
    }
    val c = ZovTheme.colors
    val t = ZovTheme.text
    ZovScrollScreen {
        when {
            state.isLoading -> ZovCenteredCircularProgress()
            state.loadFailed ->
                Column(verticalArrangement = Arrangement.spacedBy(ZovItemSpacing)) {
                    Text(stringResource(R.string.error_load), style = t.bodyReg14, color = c.negative)
                    Button(onClick = { viewModel.retry() }) {
                        Text(stringResource(R.string.action_retry))
                    }
                }

            state.order != null ->
                OrderDetailLoadedContent(
                    order = checkNotNull(state.order),
                    isCancelling = state.isCancelling,
                    cancelFailed = state.cancelFailed,
                    onCancel = viewModel::cancel,
                )
        }
    }
}

@Composable
private fun OrderDetailLoadedContent(
    order: UserOrder,
    isCancelling: Boolean,
    cancelFailed: Boolean,
    onCancel: () -> Unit,
) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    val createdLine = stringResource(
        R.string.order_detail_created_at,
        formatOrderInstant(order.createdAtEpochSeconds),
    )
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(ZovItemSpacing),
    ) {
        Text(orderLineTitle(order), style = t.titleSemi20, color = c.onSurface)
        Text(createdLine, style = t.subtitleReg14, color = c.onSurfaceVariant)
        detailLine(stringResource(R.string.order_detail_side), sideLabel(order.side))
        detailLine(stringResource(R.string.order_detail_quantity), order.quantity.toString())
        detailLine(
            stringResource(R.string.order_detail_status),
            orderStatusLabel(order.status),
        )
        order.executedPrice?.takeIf { it.isNotBlank() }?.let { price ->
            detailLine(stringResource(R.string.order_detail_executed_price), price)
        }
        order.totalAmount?.takeIf { it.isNotBlank() }?.let { amount ->
            detailLine(stringResource(R.string.order_detail_total), amount)
        }
        if (order.isCancellable()) {
            OutlinedButton(
                onClick = onCancel,
                enabled = !isCancelling,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    if (isCancelling) {
                        stringResource(R.string.order_cancelling)
                    } else {
                        stringResource(R.string.order_cancel)
                    },
                )
            }
        }
        if (cancelFailed) {
            Text(
                stringResource(R.string.order_cancel_failed),
                style = t.bodyReg14,
                color = c.negative,
            )
        }
    }
}

@Composable
private fun detailLine(label: String, value: String) {
    val c = ZovTheme.colors
    val t = ZovTheme.text
    Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(ZovUnit / 2)) {
        Text(label, style = t.labelReg12, color = c.onSurfaceVariant)
        Text(value, style = t.bodyReg14, color = c.onSurface)
    }
}

@Composable
private fun orderLineTitle(order: UserOrder): String {
    val ticker = order.ticker.ifBlank { "—" }
    val side = sideLabel(order.side)
    return stringResource(R.string.orders_row_title, ticker, side, order.quantity)
}

@Composable
private fun sideLabel(side: String): String =
    when (side.lowercase(Locale.getDefault())) {
        "buy" -> stringResource(R.string.order_side_buy)
        "sell" -> stringResource(R.string.order_side_sell)
        else -> side
    }

@Composable
private fun orderStatusLabel(status: String): String =
    when (status.lowercase(Locale.getDefault())) {
        "pending" -> stringResource(R.string.order_status_pending)
        "executed" -> stringResource(R.string.order_status_executed)
        "partial" -> stringResource(R.string.order_status_partial)
        "cancelled" -> stringResource(R.string.order_status_cancelled)
        "rejected" -> stringResource(R.string.order_status_rejected)
        else -> status
    }

private fun formatOrderInstant(epochSeconds: Long): String =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("ru-RU"))
        .format(Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault()))
