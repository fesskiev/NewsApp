@file:OptIn(ExperimentalUuidApi::class)

package org.news.common.mvi

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class UiEvent<E>(
    val event: E,
    val id: String
)

abstract class MviViewModel<S, A, E>(
    initialState: S
) : ViewModel() {

    val uiState = MutableStateFlow(initialState)
    val uiEvent = MutableSharedFlow<UiEvent<E>>(extraBufferCapacity = 1)

    abstract fun onAction(action: A)

    protected fun emitUiEvent(event: E, id: String = Uuid.random().toString()) {
        uiEvent.tryEmit(UiEvent(event, id))
    }
}
