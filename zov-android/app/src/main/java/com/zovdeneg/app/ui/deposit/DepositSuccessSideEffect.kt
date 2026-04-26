package com.zovdeneg.app.ui.deposit

import com.zovdeneg.app.R

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Composable
fun DepositSuccessSideEffect(
    pendingSuccess: DepositSheetSuccess?,
    snackbarHostState: SnackbarHostState,
    /** Область из [com.zovdeneg.app.ui.components.LocalZovSnackbarScope], чтобы плашка жила после pop. */
    snackbarScope: CoroutineScope,
    onAcknowledge: () -> Unit,
    onAfterBalanceChanged: () -> Unit,
) {
    val depositOk = stringResource(R.string.deposit_success_snackbar)
    val withdrawOk = stringResource(R.string.withdraw_success_snackbar)
    LaunchedEffect(pendingSuccess) {
        when (pendingSuccess) {
            null -> return@LaunchedEffect
            DepositSheetSuccess.DEPOSIT ->
                snackbarScope.launch { snackbarHostState.showSnackbar(depositOk) }
            DepositSheetSuccess.WITHDRAW ->
                snackbarScope.launch { snackbarHostState.showSnackbar(withdrawOk) }
        }
        onAcknowledge()
        onAfterBalanceChanged()
    }
}
