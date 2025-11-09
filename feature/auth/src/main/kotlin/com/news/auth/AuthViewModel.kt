package com.news.auth

import kotlinx.coroutines.flow.update
import org.news.common.mvi.MviViewModel

data class AuthState(
    val isLoading: Boolean = false,
    val email: String = "",
)

internal sealed interface AuthAction {
    data class EmailChange(val email: String) : AuthAction
}

internal sealed interface AuthEvent {

}

internal class AuthViewModel(
) : MviViewModel<AuthState, AuthAction, AuthEvent>(
    initialState = AuthState()
) {
    override fun onAction(action: AuthAction) {
        when (action) {
            is AuthAction.EmailChange -> uiState.update { it.copy(email = action.email) }
        }
    }

}