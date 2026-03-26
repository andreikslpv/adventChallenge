package com.ai.adventchallenge.navigation

sealed class Screen(val route: String) {
    object Chat : Screen("chat")
    object Settings : Screen("settings/{agentId}") {
        fun createRoute(agentId: String) = "settings/$agentId"
    }
}
