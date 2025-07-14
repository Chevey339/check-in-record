package com.example.uldemo.models

enum class Screen {
    MAIN, EDIT_HABITS, ABOUT
}

data class DailyHabit(
    val id: Int,
    val title: String,
    val description: String = "",
    val icon: String = "✓"
)