package com.zovdeneg.app.ui.components

data class ZovAuthPasswordFieldState(
    val value: String,
    val onValueChange: (String) -> Unit,
    val enabled: Boolean = true,
)
