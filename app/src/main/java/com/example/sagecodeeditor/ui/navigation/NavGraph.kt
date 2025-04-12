package com.example.sagecodeeditor.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sagecodeeditor.ui.screens.CodeEditorScreen
import com.example.sagecodeeditor.ui.screens.FileExplorerScreen
import com.example.sagecodeeditor.ui.screens.HomeScreen
import com.example.sagecodeeditor.ui.screens.LivePreviewScreen
import com.example.sagecodeeditor.ui.screens.TerminalScreen

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object FileExplorer : Screen("file_explorer")
    object CodeEditor : Screen("code_editor")
    object BlankEditor : Screen("blank_editor")
    object Terminal : Screen("terminal")
    object LivePreview : Screen("live_preview")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.BlankEditor.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        
        composable(Screen.FileExplorer.route) {
            FileExplorerScreen(navController = navController)
        }
        
        composable(Screen.BlankEditor.route) {
            // Open a blank editor without a file path
            CodeEditorScreen(
                navController = navController,
                filePath = ""
            )
        }
        
        composable("${Screen.CodeEditor.route}/{filePath}") { backStackEntry ->
            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
            CodeEditorScreen(
                navController = navController,
                filePath = filePath
            )
        }
        
        composable(Screen.Terminal.route) {
            TerminalScreen(navController = navController)
        }
        
        composable(Screen.LivePreview.route) {
            LivePreviewScreen(navController = navController)
        }
    }
}
