package org.news

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.news.auth.AuthRoute
import org.koin.compose.koinInject
import org.news.design.NewsAppTheme
import org.news.navigation.Home
import org.news.navigation.GlobalNavigationEventBus
import org.news.navigation.Splash
import org.news.navigation.Auth
import org.news.navigation.GlobalRoute
import org.news.splash.SplashRoute

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val navigationEventBus: GlobalNavigationEventBus = koinInject()
            NewsAppTheme {
                val globalRoute by navigationEventBus.globalNavigation.collectAsState(initial = Splash)
                NavigationAnimation(globalRoute) { route ->
                    when (route) {
                        is Splash -> SplashRoute()
                        is Auth -> AuthRoute()
                        is Home -> ArticleListRoute(onArticleClick = { })
                    }
                }
            }
        }
    }
}

@Composable
private fun NavigationAnimation(
    state: GlobalRoute,
    onStateChange: @Composable (GlobalRoute) -> Unit
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            val enterTransition = slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(
                animationSpec = tween(800, delayMillis = 200)
            ) + scaleIn(
                initialScale = 0.8f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
            val exitTransition = slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(600, easing = FastOutLinearInEasing)
            ) + fadeOut(
                animationSpec = tween(400)
            ) + scaleOut(
                targetScale = 1.2f,
                animationSpec = tween(600, easing = LinearOutSlowInEasing)
            )

            enterTransition togetherWith exitTransition
        },
        modifier = Modifier.fillMaxSize()
    ) { state ->
        onStateChange(state)
    }
}