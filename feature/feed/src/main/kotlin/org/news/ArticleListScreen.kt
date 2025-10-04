@file:OptIn(ExperimentalMaterial3Api::class)

package org.news

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
import androidx.compose.material3.SnackbarHost
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.news.feed.R
import org.news.model.Article

@Composable
fun ArticleListScreen(
    onArticleClick: (Article) -> Unit,
    viewModel: ArticleListViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val event by viewModel.uiEvent.collectAsState(null)

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(event) {
        val event = event?.event ?: return@LaunchedEffect
        when (event) {
            is ArticleListEvent.EmptyQuery -> snackbarHostState.showSnackbar(
                message = "Query field is empty",
                duration = SnackbarDuration.Short
            )
            is ArticleListEvent.Error -> snackbarHostState.showSnackbar(
                message = "Failed to load articles: ${event.message}",
                withDismissAction = true,
                duration = SnackbarDuration.Indefinite
            )
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = state.query,
                        onValueChange = { viewModel.onAction(ArticleListAction.UpdateQuery(it)) },
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
                        onClick = { viewModel.onAction(ArticleListAction.RefreshArticles) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_refresh),
                            contentDescription = "Refresh",
                            modifier = Modifier.size(42.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFD5CBCB),
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState)
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(state.articles) { article ->
                    ListItem(
                        headlineContent = {
                            Text(
                                text = article.title, fontWeight = FontWeight.Bold,
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

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(100.dp)
                )
            }
        }
    }
}