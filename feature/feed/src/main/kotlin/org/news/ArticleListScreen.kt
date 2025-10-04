@file:OptIn(ExperimentalMaterial3Api::class)

package org.news

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DateRangePicker
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDateRangePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.koin.compose.viewmodel.koinViewModel
import org.news.common.mvi.UiEvent
import org.news.feed.R
import org.news.model.Article

data class CustomSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = false,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val isError: Boolean = false
) : SnackbarVisuals

@Composable
fun ArticleListScreen(
    onArticleClick: (Article) -> Unit,
    viewModel: ArticleListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState(null)

    ArticleListScaffold(
        uiState = uiState,
        uiEvent = uiEvent,
        onArticleClick = onArticleClick,
        onAction = { viewModel.onAction(it) }
    )
}

@Composable
fun ArticleListScaffold(
    uiState: ArticleListState,
    uiEvent: UiEvent<ArticleListEvent>?,
    onArticleClick: (Article) -> Unit,
    onAction: (ArticleListAction) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiEvent) {
        val currentEvent = uiEvent?.event ?: return@LaunchedEffect
        when (currentEvent) {
            ArticleListEvent.EmptyQuery -> snackbarHostState.showSnackbar(
                CustomSnackbarVisuals(
                    message = "Query field is empty",
                    duration = SnackbarDuration.Short,
                    isError = false
                )
            )

            is ArticleListEvent.Error -> snackbarHostState.showSnackbar(
                CustomSnackbarVisuals(
                    message = "Failed to load articles: ${currentEvent.message}",
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )

            ArticleListEvent.EmptyArticlesResponse -> snackbarHostState.showSnackbar(
                CustomSnackbarVisuals(
                    message = "No articles founded",
                    duration = SnackbarDuration.Short,
                    isError = false
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = uiState.query,
                        onValueChange = { onAction(ArticleListAction.UpdateQuery(it)) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                },
                actions = {
                    IconButton(
                        onClick = { showDatePicker = !showDatePicker }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_date_range),
                            contentDescription = "Refresh",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(
                        onClick = { onAction(ArticleListAction.RefreshArticles) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFE5DDDD),
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                val isError = (snackbarData.visuals as? CustomSnackbarVisuals)?.isError ?: false

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
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            ArticleList(
                uiState = uiState,
                onArticleClick = onArticleClick
            )

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(80.dp)
                )
            }

            if (showDatePicker) {
                DateRangePickerDialog(
                    currentFrom = uiState.from,
                    currentTo = uiState.to,
                    onDismiss = { showDatePicker = false },
                    onConfirm = { from, to ->
                        showDatePicker = false
                        onAction(ArticleListAction.UpdateDateRange(from, to))
                    }
                )
            }
        }
    }
}

@Composable
private fun ArticleList(
    uiState: ArticleListState,
    onArticleClick: (Article) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.articles) { article ->
            ListItem(
                headlineContent = {
                    Text(
                        text = article.title,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2
                    )
                },
                supportingContent = {
                    Text(
                        text = article.description,
                        maxLines = 5
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onArticleClick(article) }
                    .padding(vertical = 8.dp)
            )
            HorizontalDivider(
                thickness = DividerDefaults.Thickness,
                color = DividerDefaults.color
            )
        }
    }
}

@Composable
private fun DateRangePickerDialog(
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