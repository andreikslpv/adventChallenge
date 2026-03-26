package com.ai.adventchallenge.navigation

import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.ai.adventchallenge.ui.ChatScreen
import com.ai.adventchallenge.ui.SettingsScreen
import com.ai.adventchallenge.viewmodel.ChatViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModel: ChatViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showError by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            showError = true
        }
    }

    LaunchedEffect(showError) {
        if (showError && uiState.error != null) {
            val result = snackbarHostState.showSnackbar(
                message = uiState.error ?: "Ошибка",
                actionLabel = "ОК"
            )
            if (result == SnackbarResult.ActionPerformed || result == SnackbarResult.Dismissed) {
                viewModel.clearError()
                showError = false
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route,
    ) {
        composable(Screen.Chat.route) {
            ChatScreen(
                agents = uiState.agents,
                selectedAgentId = uiState.selectedAgentId,
                messages = uiState.messages,
                isLoading = uiState.isLoading,
                isSendingToAll = uiState.isSendingToAll,
                snackbarHostState = snackbarHostState,
                onSelectAgent = { viewModel.selectAgent(it) },
                onAddAgent = { viewModel.addAgent() },
                onRemoveAgent = { viewModel.removeAgent(it) },
                onOpenSettings = { agentId ->
                    navController.navigate(Screen.Settings.createRoute(agentId))
                },
                onSendMessage = { viewModel.sendMessage(it) },
                onSendToAll = { viewModel.sendMessageToAll(it) },
                onClearSession = { viewModel.clearSession() }
            )
        }

        composable(
            route = Screen.Settings.route,
            arguments = listOf(
                navArgument("agentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val agentId = backStackEntry.arguments?.getString("agentId") ?: return@composable
            val agent = uiState.agents.find { it.id == agentId }

            if (agent != null) {
                SettingsScreen(
                    settings = agent.settings,
                    onBack = { navController.popBackStack() },
                    onSave = { settings ->
                        viewModel.updateAgentSettings(agentId, settings)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
