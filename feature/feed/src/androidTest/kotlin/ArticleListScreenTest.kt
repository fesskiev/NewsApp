import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import org.news.ArticleListContent
import org.news.ArticleListEvent
import org.news.ArticleListScaffold
import org.news.ArticleListState
import org.news.common.mvi.UiEvent
import org.news.common.test.TestTag.ARTICLE_LIST
import org.news.common.test.TestTag.DATE_PICKER_DIALOG
import org.news.common.test.TestTag.DATE_PICKER_ICON
import org.news.common.test.TestTag.LOADING_INDICATOR
import org.news.common.test.TestTag.REFRESH_ICON
import org.news.common.test.TestTag.SEARCH_FIELD
import org.news.common.test.TestTag.SNACKBAR
import org.news.design.NewsAppTheme

class ArticleListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(block: @Composable () -> Unit) {
        composeTestRule.setContent {
            NewsAppTheme {
                block()
            }
        }
    }

    private fun launchScaffold(
        uiEvent: UiEvent<ArticleListEvent>? = null,
        query: String = "",
        isLoading: Boolean = false,
        content: @Composable () -> Unit = {}
    ) {
        setContent {
            ArticleListScaffold(
                uiEvent = uiEvent,
                onRefreshClick = { },
                onDatePickerClick = { },
                onQueryChange = { },
                query = query,
                isLoading = isLoading
            ) {
                content()
            }
        }
    }

    private fun launchContent(
        uiState: ArticleListState = ArticleListState(),
        uiEvent: UiEvent<ArticleListEvent>? = null
    ) {
        setContent {
            ArticleListContent(
                uiState = uiState,
                uiEvent = uiEvent,
                onArticleClick = { },
                onAction = { }
            )
        }
    }

    @Test
    fun initialStateShowsEmptySearchFieldActionIconsWithoutBodyLoadingOrSnackbars() {
        launchContent()

        composeTestRule
            .onNodeWithTag(SEARCH_FIELD)
            .assertTextEquals("")

        composeTestRule
            .onNodeWithTag(DATE_PICKER_ICON)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(REFRESH_ICON)
            .assertIsDisplayed()

        composeTestRule
            .onNodeWithTag(LOADING_INDICATOR)
            .assertIsNotDisplayed()

        composeTestRule
            .onNodeWithTag(ARTICLE_LIST)
            .assertIsNotDisplayed()

        composeTestRule
            .onNodeWithTag(SNACKBAR)
            .assertIsNotDisplayed()

        composeTestRule
            .onNodeWithTag(DATE_PICKER_DIALOG)
            .assertIsNotDisplayed()
    }

    @Test
    fun searchFieldAndActionButtonsAreDisabledWhenLoading() {
        launchScaffold(isLoading = true)

        composeTestRule
            .onNodeWithTag(SEARCH_FIELD)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(DATE_PICKER_ICON)
            .assertIsNotEnabled()

        composeTestRule
            .onNodeWithTag(REFRESH_ICON)
            .assertIsNotEnabled()
    }

    @Test
    fun searchFieldAndActionButtonsAreEnabledWhenNotLoading() {
        launchScaffold(isLoading = false)

        composeTestRule
            .onNodeWithTag(SEARCH_FIELD)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(DATE_PICKER_ICON)
            .assertIsEnabled()

        composeTestRule
            .onNodeWithTag(REFRESH_ICON)
            .assertIsEnabled()
    }

    @Test
    fun searchFieldDisplaysProvidedQuery() {
        launchScaffold(query = "Android")

        composeTestRule
            .onNodeWithTag(SEARCH_FIELD)
            .assertTextContains("Android")
    }

    @Test
    fun searchFieldIsEmptyWhenNoQueryProvided() {
        launchScaffold(query = "")

        composeTestRule
            .onNodeWithTag(SEARCH_FIELD)
            .assertTextContains("")
    }

    @Test
    fun displaysEmptyQueryErrorSnackbarOnEmptyQueryEvent() {
        launchScaffold(uiEvent = UiEvent(ArticleListEvent.EmptyQuery))

        composeTestRule
            .onNodeWithTag(SNACKBAR)
            .assertIsDisplayed()
            .onChildAt(0)
            .assertTextContains("Search field is empty")
    }

    @Test
    fun displaysNoArticlesSnackbarOnEmptyArticlesEvent() {
        launchScaffold(uiEvent = UiEvent(ArticleListEvent.EmptyArticlesResponse))

        composeTestRule
            .onNodeWithTag(SNACKBAR)
            .assertIsDisplayed()
            .onChildAt(0)
            .assertTextContains("No articles founded")
    }

    @Test
    fun displaysErrorSnackbarOnErrorEvent() {
        launchScaffold(uiEvent = UiEvent(ArticleListEvent.Error(message = "Unknown error")))

        composeTestRule
            .onNodeWithTag(SNACKBAR)
            .assertIsDisplayed()
    }

    @Test
    fun displaysDatePickerDialogWhenDateIconClicked() {
        launchContent()

        composeTestRule
            .onNodeWithTag(DATE_PICKER_ICON)
            .performClick()

        composeTestRule
            .onNodeWithTag(DATE_PICKER_DIALOG)
            .assertIsDisplayed()
    }

    @Test
    fun displaysArticleListWhenArticlesPresentInState() {
        launchContent(uiState = ArticleListState(articles = mockArticles))

        composeTestRule
            .onNodeWithTag(ARTICLE_LIST)
            .assertIsDisplayed()
            .onChildren()
            .assertCountEquals(mockArticles.size)
    }

    @Test
    fun hidesArticleListWhenNoArticlesInState() {
        launchContent()

        composeTestRule
            .onNodeWithTag(ARTICLE_LIST)
            .assertIsNotDisplayed()
    }
}