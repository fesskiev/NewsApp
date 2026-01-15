package com.news.auth

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

/**
 * App Launch Flow
 *
 * [App Launch]
 * ↓
 * [Check Biometric Capability]
 * ↓
 * [Check Stored Auth Data]
 * ↓
 * ├─── (No tokens) ──► [Login Screen]
 * │
 * └─── (Has tokens) ──► [Check Expiration]
 *     ↓
 *     ├─── (Access valid) ──► [Auth Gate] ──► [Home]
 *     │
 *     ├─── (Access expired, Refresh valid) ──► [Refresh Tokens]
 *     │     ↓
 *     │     [Auth Gate] ──► [Home]
 *     │
 *     └─── (Both expired) ──► [Clear Data] ──► [Login Screen]
 *
 * [Auth Gate]
 * ├─── (Biometric Available) ──► [Biometric Prompt]
 * │     ├─── (Success) ──► [Decrypt] ──► Continue
 * │     └─── (Fail) ──► [Password Prompt] ──► [Decrypt] ──► Continue
 * │
 * └─── (Biometric Unavailable) ──► [Password Prompt] ──► [Decrypt] ──► Continue
 *
 * [Login Screen] has option: [New User? Register]
 * ↓
 * [Registration Screen] ──(Success)──► [Auto-login] ──► [Store Token] ──► [Enable Biometric Prompt?] ──► [Home]
 */

@Serializable
private data object Splash : NavKey

@Serializable
private data object Login : NavKey

@Serializable
private data object Registration : NavKey

@Composable
fun AuthRoute() {
    val backStack = rememberNavBackStack(Splash)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryProvider = entryProvider {
            entry<Splash> {
                SplashScreen(
                    onNavigateToAuth = {
                        backStack.clear() // Clear Splash from stack
                        backStack.add(Login) // Add Login as the new root
                    }
                )
            }
            entry<Login> {
                LoginScreen(
                    onNavigateToRegistration = { backStack.add(Registration) }
                )
            }
            entry<Registration> {
                RegistrationScreen(
                    onNavigateToLogin = { backStack.removeLastOrNull() }
                )
            }
        }
    )
}
