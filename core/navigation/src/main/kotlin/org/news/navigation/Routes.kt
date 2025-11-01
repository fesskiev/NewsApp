package org.news.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface GlobalRoute

@Serializable
data object Unauthenticated : GlobalRoute

@Serializable
data object Authenticated : GlobalRoute

@Serializable
sealed interface Route