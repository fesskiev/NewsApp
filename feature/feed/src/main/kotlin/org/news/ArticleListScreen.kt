@file:OptIn(ExperimentalMaterial3Api::class)

package org.news

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.news.common.mvi.UiEvent
import org.news.common.test.TestTag.ARTICLE_LIST
import org.news.common.test.TestTag.DATE_PICKER_ICON
import org.news.common.test.TestTag.LOADING_INDICATOR
import org.news.common.test.TestTag.REFRESH_ICON
import org.news.common.test.TestTag.SEARCH_FIELD
import org.news.design.NewsAppTheme
import org.news.design.components.DateRangePickerDialog
import org.news.design.components.Snackbar
import org.news.design.components.SnackbarParams
import org.news.feed.R
import org.news.model.Article

@Composable
fun ArticleListRoute(
    onArticleClick: (Article) -> Unit
) {
    ArticleListScreen(
        onArticleClick = onArticleClick
    )
}

@Composable
private fun ArticleListScreen(
    onArticleClick: (Article) -> Unit,
    viewModel: ArticleListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState(null)

    ArticleListContent(
        uiState = uiState,
        uiEvent = uiEvent,
        onArticleClick = onArticleClick,
        onAction = { viewModel.onAction(it) }
    )
}

@Composable
internal fun ArticleListContent(
    uiState: ArticleListState,
    uiEvent: UiEvent<ArticleListEvent>?,
    onArticleClick: (Article) -> Unit,
    onAction: (ArticleListAction) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }

    ArticleListScaffold(
        uiEvent = uiEvent,
        onRefreshClick = { onAction(ArticleListAction.RefreshArticles) },
        onDatePickerClick = { showDatePicker = !showDatePicker },
        onQueryChange = { onAction(ArticleListAction.UpdateQuery(it)) },
        query = uiState.query,
        isLoading = uiState.isLoading
    ) {
        val articles = uiState.articles
        if (articles.isNotEmpty()) {
            ArticleList(
                articles = articles,
                onArticleClick = onArticleClick
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(80.dp)
                    .testTag(LOADING_INDICATOR)
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

@Composable
internal fun ArticleListScaffold(
    uiEvent: UiEvent<ArticleListEvent>?,
    onRefreshClick: () -> Unit,
    onDatePickerClick: () -> Unit,
    onQueryChange: (String) -> Unit,
    query: String,
    isLoading: Boolean,
    content: @Composable (() -> Unit)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent) {
        val currentEvent = uiEvent?.event ?: return@LaunchedEffect
        when (currentEvent) {
            ArticleListEvent.EmptyQuery -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = "Search field is empty",
                    duration = SnackbarDuration.Short,
                    isError = false
                )
            )
            ArticleListEvent.EmptyArticlesResponse -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = "No articles founded",
                    duration = SnackbarDuration.Short,
                    isError = false
                )
            )
            is ArticleListEvent.Error -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = "Failed to load articles: ${currentEvent.message}",
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { onQueryChange(it) },
                        modifier = Modifier
                            .testTag(SEARCH_FIELD)
                            .fillMaxWidth(),
                        enabled = !isLoading,
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
                        onClick = { onDatePickerClick() },
                        modifier = Modifier
                            .testTag(DATE_PICKER_ICON),
                        enabled = !isLoading
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_date_range),
                            contentDescription = "Date picker",
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(
                        onClick = { onRefreshClick() },
                        modifier = Modifier
                            .testTag(REFRESH_ICON),
                        enabled = !isLoading
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
        snackbarHost = { Snackbar(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
private fun ArticleList(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .testTag(ARTICLE_LIST)
            .fillMaxSize()
    ) {
        items(articles) { article ->
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

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Composable
fun ArticleListContentInitState() {
    NewsAppTheme {
        ArticleListContent(
            uiState = ArticleListState(),
            uiEvent = null,
            onArticleClick = { },
            onAction = { }
        )
    }
}

