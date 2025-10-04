package org.news

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.news.common.mvi.MviViewModel
import org.news.common.utils.executeWithTryCatch
import org.news.data.NewsRepository
import org.news.model.Article
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Duration.Companion.days

private val dateFormatter = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)

private val nowMillis = System.currentTimeMillis()
private val today = nowMillis
private val yesterday = nowMillis - 1.days.inWholeMilliseconds

private fun Long.toDateString(): String = dateFormatter.format(Date(this))

data class ArticleListState(
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
            is ArticleListAction.UpdateDateRange -> uiState.update {
                it.copy(
                    from = action.from,
                    to = action.to
                )
            }
        }
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
            executeWithTryCatch {
                update { it.copy(isLoading = true) }
                val articles = repository.searchNews(
                    query = query,
                    from = value.from.toDateString(),
                    to = value.to.toDateString()
                )
                if (articles.isEmpty()) {
                    emitUiEvent(ArticleListEvent.EmptyArticlesResponse)
                    update { it.copy(isLoading = false) }
                } else {
                    update { it.copy(articles = articles, isLoading = false) }
                }

            }.onFailure { failure ->
                update { it.copy(isLoading = false) }
                emitUiEvent(ArticleListEvent.Error(message = failure.message))
            }
        }
    }
}