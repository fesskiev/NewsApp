package org.news.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface GlobalRoute

@Serializable
data object Splash : GlobalRoute

@Serializable
data object Auth : GlobalRoute

@Serializable
data object Home : GlobalRoute

@Serializable
sealed interface Route