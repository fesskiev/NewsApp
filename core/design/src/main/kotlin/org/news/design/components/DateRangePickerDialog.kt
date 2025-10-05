package org.news.design.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.news.common.test.TestTag.DATE_PICKER_DIALOG

@Composable
fun DateRangePickerDialog(
    currentFrom: Long,
    currentTo: Long,
    onDismiss: () -> Unit,
    onConfirm: (from: Long, to: Long) -> Unit
) {
    val state = rememberDateRangePickerState(
        initialSelectedStartDateMillis = currentFrom,
        initialSelectedEndDateMillis = currentTo
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val start = state.selectedStartDateMillis
                    val end = state.selectedEndDateMillis
                    if (start != null && end != null) {
                        onConfirm(start, end)
                    }
                },
                enabled = state.selectedStartDateMillis != null &&
                        state.selectedEndDateMillis != null
            ) {
                Text("OK", fontSize = 16.sp)
            }
        },
        modifier = Modifier.testTag(DATE_PICKER_DIALOG),
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", fontSize = 16.sp)
            }
        }
    ) {
        DateRangePicker(
            state = state,
            title = {},
            showModeToggle = false,
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
        )
    }
}