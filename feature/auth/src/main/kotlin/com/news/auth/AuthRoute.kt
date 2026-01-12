package com.news.auth

import androidx.compose.runtime.Composable

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

@Composable
fun AuthRoute() {
    SplashScreen()
    //LoginScreen()
    //RegistrationScreen()
}
