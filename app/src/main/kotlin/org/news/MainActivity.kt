package org.news

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import com.news.auth.AuthRoute
import org.koin.compose.koinInject
import org.news.design.NewsAppTheme
import org.news.navigation.Home
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Splash
import org.news.navigation.Auth

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigationEventBus: GlobalNavigationEventBus = koinInject()
            NewsAppTheme {
                val globalRoute by navigationEventBus.globalNavigation.collectAsState(initial = Splash)
                when (globalRoute) {
                    is Splash -> AuthRoute()
                    is Auth -> AuthRoute()
                    is Home -> ArticleListRoute(onArticleClick = { })
                }
            }
        }
    }
}