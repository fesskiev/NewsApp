package org.news

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.news.common.mvi.MviViewModel
import org.news.common.utils.executeWithTryCatch
import org.news.data.NewsRepository
import org.news.model.Article

data class ArticleListState(
    val articles: List<Article> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class ArticleListAction {
    data object FetchArticles : ArticleListAction()
}

sealed class ArticleListEvent

class ArticleListViewModel(
    private val repository: NewsRepository
) : MviViewModel<ArticleListState, ArticleListAction, ArticleListEvent>(
    initialState = ArticleListState()
) {

    override fun onAction(action: ArticleListAction) {
        when (action) {
            is ArticleListAction.FetchArticles -> fetchArticles()
        }
    }

    private fun fetchArticles() {
        viewModelScope.launch {
            executeWithTryCatch {
                uiState.update {
                    it.copy(isLoading = true, error = null)
                }
                val articles = repository.searchNews(
                    query = "android",
                    from = "2025-09-29",
                    to = "2025-09-30"
                )
                uiState.update {
                    it.copy(articles = articles, isLoading = false, error = null)
                }

            }.onFailure { failure ->
                uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load articles: ${failure.message}"
                    )
                }
            }
        }
    }
}