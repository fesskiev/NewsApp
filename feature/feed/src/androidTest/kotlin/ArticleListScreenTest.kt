import androidx.compose.ui.test.junit4.createComposeRule
import org.junit.Rule
import org.junit.Test
import org.news.ArticleListContent
import org.news.ArticleListState
import org.news.design.NewsAppTheme

class ArticleListScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun articleListContentInitTest() {
        composeTestRule.setContent {
            NewsAppTheme {
                ArticleListContent(
                    uiState = ArticleListState(),
                    uiEvent = null,
                    onArticleClick = { },
                    onAction = { }
                )
            }
        }
    }
}