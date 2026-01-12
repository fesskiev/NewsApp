package com.news.auth

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.news.auth.LoginAction.BiometricAuthenticateClick
import com.news.auth.LoginAction.BiometricAuthenticated
import com.news.auth.LoginAction.BiometricAuthenticatorError
import com.news.auth.LoginAction.CheckBiometricEnable
import com.news.auth.LoginAction.EmailChange
import com.news.auth.LoginAction.EnableBiometricClick
import com.news.auth.LoginAction.LoginClick
import com.news.auth.LoginAction.PasswordChange
import org.koin.compose.viewmodel.koinViewModel
import org.news.auth.R
import org.news.common.mvi.UiEvent
import org.news.common.test.TestTag.LOADING_INDICATOR
import org.news.design.NewsAppTheme
import org.news.design.components.Snackbar
import org.news.design.components.SnackbarParams
import java.security.Signature

@Composable
internal fun LoginScreen(
    viewModel: LoginViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState(null)

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        viewModel.onAction(CheckBiometricEnable)
    }

    val biometricAuthenticator = rememberBiometricAuthenticator(
        onSuccess = {
            viewModel.onAction(BiometricAuthenticated(it))
        },
        onError = {
            viewModel.onAction(BiometricAuthenticatorError(it))
        }
    )

    LaunchedEffect(uiEvent) {
        val event = uiEvent?.event ?: return@LaunchedEffect
        when (event) {
            LoginEvent.EnrollBiometric -> launcher.launch(createSettingIntent())
            is LoginEvent.LaunchBiometricAuthenticator -> biometricAuthenticator(event.signature)
            else -> Unit
        }
    }

    LoginScaffold(uiEvent) {
        LoginContent(
            uiState,
            onAction = { viewModel.onAction(it) }
        )
    }
}

@Composable
private fun LoginScaffold(
    uiEvent: UiEvent<LoginEvent>?,
    content: @Composable (() -> Unit)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent) {
        val event = uiEvent?.event ?: return@LaunchedEffect
        when (event) {
            LoginEvent.SnackbarEvent.BiometricDisable -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = "Please enable biometrics to continue",
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )

            is LoginEvent.SnackbarEvent.BiometricAuthenticatorError -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = event.error,
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )
            else -> Unit
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
private fun LoginContent(
    uiState: LoginState,
    onAction: (LoginAction) -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Login", style = MaterialTheme.typography.headlineMedium)
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
                onClick = { onAction(LoginClick)},
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

                BiometricState.Unavailable -> Unit
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
    onSuccess: (Signature) -> Unit,
    onError: (String) -> Unit
): (Signature) -> Unit {
    val activity = LocalActivity.current as FragmentActivity
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

private fun createSettingIntent(): Intent =
    Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
        putExtra(
            Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )
    }

@Preview(name = "Biometric Enable")
@Composable
private fun ContentEnable() {
    NewsAppTheme {
        LoginContent(
            uiState = LoginState(
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
        LoginContent(
            uiState = LoginState(
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
        LoginContent(
            uiState = LoginState(
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
        LoginContent(
            uiState = LoginState(
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
    val event = LoginEvent.SnackbarEvent.BiometricAuthenticatorError("Sample Error Message")
    NewsAppTheme {
        LoginScaffold(
            uiEvent = UiEvent(event)
        ) {

        }
    }
}