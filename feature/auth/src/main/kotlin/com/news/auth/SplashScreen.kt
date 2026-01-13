package com.news.auth

import androidx.activity.compose.LocalActivity
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import org.koin.compose.viewmodel.koinViewModel
import com.news.auth.SplashAction.BiometricAuthenticated
import com.news.auth.SplashAction.BiometricAuthenticatorError
import kotlinx.coroutines.launch
import org.news.security.biometric.PromptConfig
import org.news.security.biometric.launchBiometricAuthenticator

@Composable
internal fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState(null)

    val activity = LocalActivity.current as FragmentActivity
    LaunchedEffect(uiEvent) {
        val event = uiEvent?.event ?: return@LaunchedEffect
        when (event) {
            is SplashEvent.LaunchBiometricAuthenticator -> {
                launch {
                    activity.launchBiometricAuthenticator(
                        config = PromptConfig(
                            title = "Biometric Authentication",
                            subtitle = "Log in using your biometric credential"
                        )
                    ).fold(
                        onSuccess = { viewModel.onAction(BiometricAuthenticated(event.authData)) },
                        onFailure = { viewModel.onAction(BiometricAuthenticatorError(it.message)) }
                    )
                }
            }
        }
    }

    SplashContent()
}

@Composable
private fun SplashContent() {
    val infiniteTransition = rememberInfiniteTransition()

    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF4facfe),
                        Color(0xFF1A3A3A)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "🚀",
            fontSize = 100.sp,
            modifier = Modifier.scale(scale)
        )
    }
}

@Preview
@Composable
private fun SplashScreenPreview() {
    MaterialTheme {
        SplashContent()
    }
}