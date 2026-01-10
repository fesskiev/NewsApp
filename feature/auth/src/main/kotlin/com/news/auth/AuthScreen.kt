package com.news.auth

import androidx.compose.runtime.Composable

/**
 *  [App Launch]
 *     ↓
 * [Welcome/Splash] → [Check Local Auth State]
 *     ↓ (No session)
 * [Auth Entry Point] ──► [Registration Screen] ──(Success)──► [Enable Biometric Prompt?]
 *     ↓ (Has session)                                             ↓ (Yes)
 * [Login Screen] ──(Password/Biometric)──► [Home/Dashboard]       [Biometric Prompt] ──(Success)──► [Home]
 *     ↓ (Failed/Alt)                                                ↓ (No/Skip)
 * [Registration Screen] ──(Loop)                                 [Home] (Biometrics Disabled)
 */

@Composable
fun AuthRoute() {
    LoginScreen()
}