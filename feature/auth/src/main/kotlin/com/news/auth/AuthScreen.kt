package com.news.auth

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
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
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.news.auth.R
import org.news.common.mvi.UiEvent
import org.news.common.test.TestTag.LOADING_INDICATOR
import org.news.design.components.Snackbar
import org.news.design.components.SnackbarParams
import java.security.Signature

@Composable
fun AuthRoute() {
    BiometricAuthScreen()
}

@Composable
private fun BiometricAuthScreen(
    viewModel: BiometricAuthViewModel = koinViewModel()
) {
    val activity = LocalContext.current as FragmentActivity

    val uiState by viewModel.uiState.collectAsState()
    val uiEvent by viewModel.uiEvent.collectAsState(null)

    BiometricAuthScaffold(uiEvent) {
        when (val biometricState = uiState.biometricState) {
            is BiometricState.Enable -> {
                val biometricAuthenticator = rememberBiometricAuthenticator(
                    activity,
                    signature = biometricState.signature,
                    onSuccess = {
                        viewModel.onAction(AuthAction.BiometricAuthenticated(it))
                    },
                    onError = { error ->
                        viewModel.onAction(AuthAction.BiometricAuthenticatorError(error))
                    }
                )

                BiometricAuthScreenContent(
                    state = uiState,
                    onEmailChange = { viewModel.onAction(AuthAction.EmailChange(it)) },
                    onBiometricClick = { biometricAuthenticator() }
                )
            }

            BiometricState.Disable -> {
                val launcher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.onAction(AuthAction.CheckBiometricEnable)
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Button(onClick = {
                        launcher.launch(createSettingIntent())
                    }) {
                        Text("Enable Biometrics")
                    }
                }
            }
        }
    }
}

@Composable
private fun BiometricAuthScaffold(
    uiEvent: UiEvent<AuthEvent>?,
    content: @Composable (() -> Unit)
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiEvent) {
        val currentEvent = uiEvent?.event ?: return@LaunchedEffect
        when (currentEvent) {
            AuthEvent.BiometricDisable -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = "Please enable biometrics to continue",
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )

            is AuthEvent.BiometricAuthenticatorError -> snackbarHostState.showSnackbar(
                visuals = SnackbarParams(
                    message = currentEvent.error,
                    duration = SnackbarDuration.Short,
                    isError = true
                )
            )
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
private fun BiometricAuthScreenContent(
    state: AuthState,
    onEmailChange: (String) -> Unit,
    onBiometricClick: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = "Welcome", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("Email (used as User ID)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(80.dp)
                        .testTag(LOADING_INDICATOR)
                )
            } else {
                Button(
                    onClick = onBiometricClick,
                    enabled = state.email.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_fingerprint),
                        contentDescription = "Biometric Login"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Login with Biometrics")
                }
            }
        }
    }
}

@Composable
private fun rememberBiometricAuthenticator(
    activity: FragmentActivity,
    signature: Signature,
    onSuccess: (Signature) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
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

    val crypto = BiometricPrompt.CryptoObject(signature)

    return remember { { biometricPrompt.authenticate(promptInfo, crypto) } }
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