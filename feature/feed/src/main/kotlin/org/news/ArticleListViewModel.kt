package org.news

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.news.common.utils.toDateString
import org.news.common.mvi.MviViewModel
import org.news.data.NewsRepository
import org.news.model.Article
import kotlin.time.Duration.Companion.days
import org.news.common.utils.Result

private val nowMillis = System.currentTimeMillis()
private val today = nowMillis
private val yesterday = nowMillis - 1.days.inWholeMilliseconds

internal data class ArticleListState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = "",
    val from: Long = yesterday,
    val to: Long = today
)

internal sealed interface ArticleListAction {
    data object RefreshArticles : ArticleListAction
    data class UpdateQuery(val query: String) : ArticleListAction
    data class UpdateDateRange(val from: Long, val to: Long) : ArticleListAction
}

internal sealed interface ArticleListEvent {
    data object EmptyQuery : ArticleListEvent
    data class Error(val message: String?) : ArticleListEvent
    data object EmptyArticlesResponse : ArticleListEvent
}

internal class ArticleListViewModel(
    private val repository: NewsRepository
) : MviViewModel<ArticleListState, ArticleListAction, ArticleListEvent>(
    initialState = ArticleListState()
) {

    private var queryJob: Job? = null

    override fun onAction(action: ArticleListAction) {
        when (action) {
            is ArticleListAction.RefreshArticles -> refreshArticles()
            is ArticleListAction.UpdateQuery -> updateQuery(action.query)
            is ArticleListAction.UpdateDateRange -> updateDateRange(action.from, action.to)
        }
    }

    private fun updateDateRange(from: Long, to: Long) {
        uiState.update {
            it.copy(
                from = from,
                to = to
            )
        }
        refreshArticles()
    }

    private fun refreshArticles() {
        with(uiState.value) {
            if (query.isEmpty()) {
                emitUiEvent(ArticleListEvent.EmptyQuery)
                return
            }
            repository.clearQueryCache(
                query = query,
                from = from.toDateString(),
                to = to.toDateString()
            )
            viewModelScope.launch {
                fetchArticles(query)
            }
        }
    }

    private fun updateQuery(query: String) {
        uiState.update { it.copy(query = query) }
        queryJob?.cancel()
        queryJob = viewModelScope.launch {
            delay(500)
            fetchArticles(query)
        }
    }

    private suspend fun fetchArticles(query: String) {
        with(uiState) {
            update { it.copy(isLoading = true) }
            val result = repository.searchNews(
                query = query,
                from = value.from.toDateString(),
                to = value.to.toDateString()
            )
            when (result) {
                is Result.Success -> {
                    val articles = result.data
                    if (articles.isEmpty()) {
                        emitUiEvent(ArticleListEvent.EmptyArticlesResponse)
                    }
                    update { it.copy(articles = articles, isLoading = false) }
                }
                is Result.Failure -> {
                    update { it.copy(isLoading = false) }
                    emitUiEvent(ArticleListEvent.Error(message = result.error.message))
                }
            }
        }
    }
}