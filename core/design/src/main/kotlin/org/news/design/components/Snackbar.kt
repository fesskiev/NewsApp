package org.news.design.components

import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class SnackbarParams(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val isError: Boolean = false
) : SnackbarVisuals

@Composable
fun Snackbar(
    snackbarHostState: SnackbarHostState
) {
    SnackbarHost(snackbarHostState) { snackbarData ->
        val isError = (snackbarData.visuals as? SnackbarParams)?.isError ?: false

        val containerColor = if (isError) Color.Red else SnackbarDefaults.color
        val contentColor = if (isError) Color.White else SnackbarDefaults.contentColor

        Snackbar(
            modifier = Modifier
                .padding(12.dp)
                .height(52.dp),
            containerColor = containerColor,
            contentColor = contentColor,
            actionContentColor = contentColor,
            dismissActionContentColor = contentColor,
            content = {
                Text(
                    text = snackbarData.visuals.message,
                    fontSize = 16.sp
                )
            }
        )
    }
}