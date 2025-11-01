package org.news

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import org.news.design.NewsAppTheme
import org.news.navigation.Authenticated
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Unauthenticated

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigationEventBus: GlobalNavigationEventBus = koinInject()
            NewsAppTheme {
                val globalRoute by
                    navigationEventBus.globalNavigation.collectAsState(initial = Authenticated)
                when (globalRoute) {
                    is Unauthenticated -> {

                    }
                    is Authenticated -> {
                        ArticleListRoute(
                            onArticleClick = { }
                        )
                    }
                }
            }
        }
    }
}