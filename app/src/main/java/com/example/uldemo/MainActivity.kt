package com.example.uldemo

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.*
import com.example.uldemo.managers.CheckInManager
import com.example.uldemo.managers.HabitManager
import com.example.uldemo.models.Screen
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
    var currentScreen by remember { mutableStateOf(Screen.MAIN) }
    
    when (currentScreen) {
        Screen.MAIN -> MainScreen(
            onNavigateToEditHabits = { currentScreen = Screen.EDIT_HABITS },
            onNavigateToAbout = { currentScreen = Screen.ABOUT }
        )
        Screen.EDIT_HABITS -> EditHabitsScreen(
            onNavigateBack = { currentScreen = Screen.MAIN }
        )
        Screen.ABOUT -> AboutScreen(
            onNavigateBack = { currentScreen = Screen.MAIN }
        )
    }
}


