package org.news.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

interface GlobalNavigationEventBus {
    val globalNavigation: SharedFlow<GlobalRoute>
    suspend fun navigateTo(route: GlobalRoute)
}

internal class GlobalNavigationEventBusImpl : GlobalNavigationEventBus {

    override val globalNavigation = MutableSharedFlow<GlobalRoute>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override suspend fun navigateTo(route: GlobalRoute) {
        globalNavigation.emit(route)
    }

}