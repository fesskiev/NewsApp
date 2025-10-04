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

private val today = dateFormatter.format(Date(nowMillis))
private val yesterday = dateFormatter.format(Date(nowMillis - 1.days.inWholeMilliseconds))

data class ArticleListState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = "",
    val from: String = yesterday,
    val to: String = today
)

sealed interface ArticleListAction {
    data object RefreshArticles : ArticleListAction
    data class UpdateQuery(val query: String) : ArticleListAction
}

sealed interface ArticleListEvent {
    data object EmptyQuery : ArticleListEvent
    data class Error(val message: String?) : ArticleListEvent
}

class ArticleListViewModel(
    private val repository: NewsRepository
) : MviViewModel<ArticleListState, ArticleListAction, ArticleListEvent>(
    initialState = ArticleListState()
) {

    private var queryJob: Job? = null

    override fun onAction(action: ArticleListAction) {
        when (action) {
            is ArticleListAction.RefreshArticles -> refreshArticles()
            is ArticleListAction.UpdateQuery -> updateQuery(action.query)
        }
    }

    private fun refreshArticles() {
        with(uiState.value) {
            if (query.isEmpty()) {
                emitUiEvent(ArticleListEvent.EmptyQuery)
                return
            }
            repository.clearQueryCache(query, from, to)
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
                    from = value.from,
                    to = value.to
                )
                update { it.copy(articles = articles, isLoading = false) }

            }.onFailure { failure ->
                update { it.copy(isLoading = false) }
                emitUiEvent(ArticleListEvent.Error(message = failure.message))
            }
        }
    }
}