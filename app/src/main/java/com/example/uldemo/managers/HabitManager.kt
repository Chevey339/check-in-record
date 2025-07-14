package com.example.uldemo.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.uldemo.models.DailyHabit
import org.json.JSONArray
import org.json.JSONObject

object HabitManager {
    private val _habits = mutableStateListOf<DailyHabit>()
    val habits: List<DailyHabit> get() = _habits.toList()
    
    private lateinit var sharedPreferences: SharedPreferences
    private const val PREF_NAME = "habit_tracker_prefs"
    private const val KEY_HABITS_DATA = "habits_data"
    private var nextId = 1
    
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadHabits()
    }
    
    private fun loadHabits() {
        try {
            val jsonString = sharedPreferences.getString(KEY_HABITS_DATA, null)
            if (jsonString != null) {
                val jsonArray = JSONArray(jsonString)
                _habits.clear()
                for (i in 0 until jsonArray.length()) {
                    val habitJson = jsonArray.getJSONObject(i)
                    val habit = DailyHabit(
                        id = habitJson.getInt("id"),
                        title = habitJson.getString("title"),
                        description = habitJson.optString("description", ""),
                        icon = habitJson.optString("icon", "✓")
                    )
                    _habits.add(habit)
                    nextId = maxOf(nextId, habit.id + 1)
                }
            } else {
                _habits.addAll(getDefaultHabits())
                saveHabits()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _habits.clear()
            _habits.addAll(getDefaultHabits())
        }
    }
    
    private fun getDefaultHabits(): List<DailyHabit> {
        return listOf(
            DailyHabit(1, "喝水", "每天8杯水", "💧"),
            DailyHabit(2, "运动", "30分钟运动", "🏃"),
            DailyHabit(3, "冥想", "10分钟冥想", "🧘"),
            DailyHabit(4, "阅读", "读书30分钟", "📖"),
            DailyHabit(5, "早睡", "晚上11点前睡觉", "😴")
        )
    }
    
    private fun saveHabits() {
        try {
            val jsonArray = JSONArray()
            _habits.forEach { habit ->
                val habitJson = JSONObject().apply {
                    put("id", habit.id)
                    put("title", habit.title)
                    put("description", habit.description)
                    put("icon", habit.icon)
                }
                jsonArray.put(habitJson)
            }
            sharedPreferences.edit()
                .putString(KEY_HABITS_DATA, jsonArray.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun addHabit(title: String, description: String, icon: String) {
        val newHabit = DailyHabit(nextId++, title, description, icon)
        _habits.add(newHabit)
        saveHabits()
    }
    
    fun updateHabit(id: Int, title: String, description: String, icon: String) {
        val index = _habits.indexOfFirst { it.id == id }
        if (index != -1) {
            _habits[index] = _habits[index].copy(
                title = title,
                description = description,
                icon = icon
            )
            saveHabits()
        }
    }
    
    fun deleteHabit(id: Int) {
        _habits.removeAll { it.id == id }
        saveHabits()
    }
}