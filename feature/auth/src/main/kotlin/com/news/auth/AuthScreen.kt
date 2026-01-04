@file:OptIn(ExperimentalUuidApi::class)

package com.news.auth

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.news.auth.R
import org.news.common.mvi.UiEvent
import org.news.common.test.TestTag.LOADING_INDICATOR
import org.news.design.components.Snackbar
import org.news.design.components.SnackbarParams
import java.security.Signature
import androidx.compose.ui.tooling.preview.Preview
import com.news.auth.AuthAction.*
import org.news.design.NewsAppTheme
import kotlin.uuid.ExperimentalUuidApi

@Composable
fun AuthRoute() {
    AuthScreen()
}

@Composable
private fun AuthScreen(
    viewModel: AuthViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState(null)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onAction(CheckBiometricEnable)
    }

    val biometricAuthenticator = rememberBiometricAuthenticator(
        activity = LocalActivity.current as FragmentActivity,
        onSuccess = {
            viewModel.onAction(BiometricAuthenticated(it))
        },
        onError = { error ->
            viewModel.onAction(BiometricAuthenticatorError(error))
        }
    )

    LaunchedEffect(uiEvent) {
        val event = uiEvent?.event ?: return@LaunchedEffect
        when (event) {
            AuthEvent.EnrollBiometric -> launcher.launch(createSettingIntent())
            is AuthEvent.LaunchBiometricAuthenticator -> biometricAuthenticator(event.signature)
            else -> {}
        }
    }

    AuthScaffold(uiEvent) {
        AuthContent(
            uiState,
            onAction = { viewModel.onAction(it) }
        )
    }
}

@Composable
private fun AuthScaffold(
    uiEvent: UiEvent<AuthEvent>?,
    content: @Composable (() -> Unit)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent) {
        val event = uiEvent?.event ?: return@LaunchedEffect
        when (event) {
            AuthEvent.SnackbarEvent.BiometricDisable -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = "Please enable biometrics to continue",
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )

            is AuthEvent.SnackbarEvent.BiometricAuthenticatorError -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = event.error,
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { Snackbar(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}


@Composable
private fun AuthContent(
    uiState: AuthState,
    onAction: (AuthAction) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Welcome", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = { onAction(EmailChange(it)) },
                label = { Text("Email (used as User ID)") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isEmailTextFiledEnable,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.password,
                onValueChange = { onAction(PasswordChange(it)) },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isPasswordTextFiledEnable,
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.isLoginButtonEnable,
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val biometricState = uiState.biometricState) {
                is BiometricState.Enable -> {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fingerprint),
                        contentDescription = null,
                        modifier = Modifier
                            .size(52.dp)
                            .clickable(
                                enabled = uiState.isLoginButtonEnable,
                                onClick = { onAction(BiometricAuthenticateClick(biometricState.signature)) }
                            )
                    )
                }

                BiometricState.Disable -> {
                    Row(
                        modifier = Modifier
                            .clickable { onAction(EnableBiometricClick) }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_fingerprint),
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Enable Biometrics?",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                BiometricState.Unavailable -> {}
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(48.dp)
                        .testTag(LOADING_INDICATOR)
                )
            }
        }
    }
}

@Composable
private fun rememberBiometricAuthenticator(
    activity: FragmentActivity,
    onSuccess: (Signature) -> Unit,
    onError: (String) -> Unit
): (Signature) -> Unit {
    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    result.cryptoObject?.signature?.let {
                        onSuccess(it)
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    onError("Authentication failed")
                }
            }
        )
    }

    val promptInfo = remember {
        BiometricPrompt.PromptInfo.Builder()
            .setTitle("Biometric Authentication")
            .setSubtitle("Log in using your biometric credential")
            .setNegativeButtonText("Use password")
            .build()
    }

    return remember {
        { signature ->
            val crypto = BiometricPrompt.CryptoObject(signature)
            biometricPrompt.authenticate(promptInfo, crypto)
        }
    }
}

private fun createSettingIntent(): Intent = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
    Intent(Settings.ACTION_SECURITY_SETTINGS)
} else {
    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
        putExtra(
            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
    }
}

@Preview(name = "Biometric Enable")
@Composable
private fun ContentEnable() {
    NewsAppTheme {
        AuthContent(
            uiState = AuthState(
                email = "test@example.com",
                password = "qwerty",
                biometricState = BiometricState.Enable(Signature.getInstance("SHA256withRSA")),
                isLoading = false
            ),
            onAction = { }
        )
    }
}

@Preview(name = "Biometric Disable")
@Composable
private fun ContentDisable() {
    NewsAppTheme {
        AuthContent(
            uiState = AuthState(
                email = "test@example.com",
                password = "qwerty",
                biometricState = BiometricState.Disable,
                isLoading = false
            ),
            onAction = { }
        )
    }
}

@Preview(name = "Biometric Unavailable")
@Composable
private fun ContentUnavailable() {
    NewsAppTheme {
        AuthContent(
            uiState = AuthState(
                email = "test@example.com",
                password = "qwerty",
                biometricState = BiometricState.Unavailable,
                isLoading = false
            ),
            onAction = { }
        )
    }
}

@Preview(name = "Loading")
@Composable
private fun ContentLoading() {
    NewsAppTheme {
        AuthContent(
            uiState = AuthState(
                email = "test@example.com",
                biometricState = BiometricState.Enable(Signature.getInstance("SHA256withRSA")),
                isLoading = true
            ),
            onAction = { }
        )
    }
}

@Preview(name = "Error Event")
@Composable
private fun ScaffoldErrorEvent() {
    val event = AuthEvent.SnackbarEvent.BiometricAuthenticatorError("Sample Error Message")
    NewsAppTheme {
        AuthScaffold(
            uiEvent = UiEvent(event)
        ) {

        }
    }
}