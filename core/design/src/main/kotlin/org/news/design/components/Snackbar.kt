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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.news.common.test.TestTag.SNACKBAR
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.tooling.preview.Preview
import org.news.design.NewsAppTheme


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
                .testTag(SNACKBAR)
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

@Preview(name = "Default Snackbar")
@Composable
private fun DefaultSnackbarPreview() {
    NewsAppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(SnackbarParams(message = "Operation successful!"))
        }
        Snackbar(snackbarHostState = snackbarHostState)
    }
}

@Preview(name = "Snackbar with Action")
@Composable
private fun SnackbarWithActionPreview() {
    NewsAppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(
                SnackbarParams(
                    message = "Item deleted.",
                    actionLabel = "Undo"
                )
            )
        }
        Snackbar(snackbarHostState = snackbarHostState)
    }
}

@Preview(name = "Dismissible Snackbar")
@Composable
private fun DismissibleSnackbarPreview() {
    NewsAppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(
                SnackbarParams(
                    message = "New update available.",
                    withDismissAction = true
                )
            )
        }
        Snackbar(snackbarHostState = snackbarHostState)
    }
}

@Preview(name = "Error Snackbar")
@Composable
private fun ErrorSnackbarPreview() {
    NewsAppTheme {
        val snackbarHostState = remember { SnackbarHostState() }
        LaunchedEffect(snackbarHostState) {
            snackbarHostState.showSnackbar(
                SnackbarParams(
                    message = "Failed to load data.",
                    isError = true
                )
            )
        }
        Snackbar(snackbarHostState = snackbarHostState)
    }
}
