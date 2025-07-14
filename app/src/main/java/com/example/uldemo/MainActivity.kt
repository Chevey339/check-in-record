package com.example.uldemo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.uldemo.managers.CheckInManager
import com.example.uldemo.managers.HabitManager
import com.example.uldemo.screens.AboutScreen
import com.example.uldemo.screens.EditHabitsScreen
import com.example.uldemo.screens.MainScreen
import com.example.uldemo.ui.theme.UldemoTheme


class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        CheckInManager.init(this)
        HabitManager.init(this)
        
        enableEdgeToEdge()
        setContent {
            UldemoTheme {
                AppNavigation()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    
    NavHost(
        navController = navController,
        startDestination = "main",
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { it },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { -it / 3 },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { -it / 3 },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + fadeIn(animationSpec = androidx.compose.animation.core.tween(300))
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { it },
                animationSpec = androidx.compose.animation.core.tween(300)
            ) + fadeOut(animationSpec = androidx.compose.animation.core.tween(300))
        }
    ) {
        composable("main") {
            MainScreen(
                onNavigateToEditHabits = { navController.navigate("edit_habits") },
                onNavigateToAbout = { navController.navigate("about") }
            )
        }
        
        composable("edit_habits") {
            EditHabitsScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable("about") {
            AboutScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}


