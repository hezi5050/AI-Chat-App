package com.hezi.aichatapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.hezi.aichatapp.ui.chat.ChatScreen
import com.hezi.aichatapp.ui.chat.ChatViewModel
import com.hezi.aichatapp.ui.conversations.ConversationsScreen
import com.hezi.aichatapp.ui.diagnostics.DiagnosticsScreen
import com.hezi.aichatapp.ui.settings.SettingsScreen

sealed class Screen(val route: String) {
    data object Chat : Screen("chat")
    data object Settings : Screen("settings")
    data object Diagnostics : Screen("diagnostics")
    data object Conversations : Screen("conversations")
}

@Composable
fun AppNavGraph(
    navController: NavHostController
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Chat.route
    ) {
        composable(Screen.Chat.route) {
            val chatViewModel: ChatViewModel = hiltViewModel()
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                onNavigateToDiagnostics = { navController.navigate(Screen.Diagnostics.route) },
                onNavigateToConversations = { navController.navigate(Screen.Conversations.route) }
            )
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Diagnostics.route) {
            DiagnosticsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Conversations.route) { backStackEntry ->
            val chatBackStackEntry = remember(backStackEntry) {
                navController.getBackStackEntry(Screen.Chat.route)
            }
            val chatViewModel: ChatViewModel = hiltViewModel(chatBackStackEntry)
            
            ConversationsScreen(
                onNavigateBack = { navController.popBackStack() },
                onConversationClick = { conversationId ->
                    chatViewModel.loadConversation(conversationId)
                    // Navigate back to chat
                    navController.popBackStack()
                }
            )
        }
    }
}

