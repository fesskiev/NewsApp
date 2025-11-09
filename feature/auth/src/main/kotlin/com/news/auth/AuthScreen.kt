package com.news.auth

import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import org.news.auth.R

@Composable
fun AuthRoute() {
    BiometricAuthScreen()
}

@Composable
private fun BiometricAuthScreen(
    viewModel: AuthViewModel = koinViewModel()
) {
    val activity = LocalContext.current as FragmentActivity
    var isBiometricAvailable by remember { mutableStateOf(activity.isBiometricAvailable()) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        isBiometricAvailable = activity.isBiometricAvailable()
    }

    if (isBiometricAvailable) {
        val biometricAuthenticator = rememberBiometricAuthenticator(
            activity,
            onSuccess = { cryptoObject ->
                println("AUTH SUCCESS")
            },
            onError = { error ->

            }
        )
        val uiState by viewModel.uiState.collectAsState()
        val uiEvent by viewModel.uiEvent.collectAsState(null)

        AuthScreenContent(
            state = uiState,
            onEmailChange = { viewModel.onAction(AuthAction.EmailChange(it)) },
            onBiometricClick = { biometricAuthenticator() }
        )
    } else {
        LaunchedEffect(Unit) {
            Toast.makeText(activity, "Please enable biometrics to continue", Toast.LENGTH_LONG)
                .show()
        }
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Button(onClick = {
                val intent =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                        Intent(Settings.ACTION_SECURITY_SETTINGS)
                    } else {
                        Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                            putExtra(
                                Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                BiometricManager.Authenticators.BIOMETRIC_STRONG
                            )
                        }
                    }
                launcher.launch(intent)
            }) {
                Text("Enable Biometrics")
            }
        }
    }
}

@Composable
fun AuthScreenContent(
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
                CircularProgressIndicator()
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
    onSuccess: (BiometricPrompt.CryptoObject?) -> Unit,
    onError: (String) -> Unit
): () -> Unit {
    val biometricPrompt = remember {
        BiometricPrompt(
            activity,
            ContextCompat.getMainExecutor(activity),
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    onSuccess(result.cryptoObject)
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

    return remember { { biometricPrompt.authenticate(promptInfo) } }
}

private fun Context.isBiometricAvailable(): Boolean {
    val biometricManager = BiometricManager.from(this)
    return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
        BiometricManager.BIOMETRIC_SUCCESS -> true
        else -> false
    }
}