package com.example.uldemo.managers

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.mutableStateMapOf
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate

object CheckInManager {
    private val checkedDates = mutableStateMapOf<LocalDate, Set<Int>>()
    private lateinit var sharedPreferences: SharedPreferences
    private const val PREF_NAME = "habit_tracker_prefs"
    private const val KEY_CHECK_DATA = "check_data"
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadData()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadData() {
        try {
            val jsonString = sharedPreferences.getString(KEY_CHECK_DATA, null) ?: return
            val jsonObject = JSONObject(jsonString)
            
            jsonObject.keys().forEach { dateString ->
                val date = LocalDate.parse(dateString)
                val habitIdsArray = jsonObject.getJSONArray(dateString)
                val habitIds = mutableSetOf<Int>()
                
                for (i in 0 until habitIdsArray.length()) {
                    habitIds.add(habitIdsArray.getInt(i))
                }
                
                if (habitIds.isNotEmpty()) {
                    checkedDates[date] = habitIds
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveData() {
        try {
            val jsonObject = JSONObject()
            
            checkedDates.forEach { (date, habitIds) ->
                val jsonArray = JSONArray()
                habitIds.forEach { id ->
                    jsonArray.put(id)
                }
                jsonObject.put(date.toString(), jsonArray)
            }
            
            sharedPreferences.edit()
                .putString(KEY_CHECK_DATA, jsonObject.toString())
                .apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun isDateChecked(date: LocalDate): Boolean {
        return checkedDates[date]?.isNotEmpty() == true
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun isHabitChecked(date: LocalDate, habitId: Int): Boolean {
        return checkedDates[date]?.contains(habitId) == true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleHabitCheck(date: LocalDate, habitId: Int) {
        val currentSet = checkedDates[date] ?: emptySet()
        if (currentSet.contains(habitId)) {
            val newSet = currentSet - habitId
            if (newSet.isEmpty()) {
                checkedDates.remove(date)
            } else {
                checkedDates[date] = newSet
            }
        } else {
            checkedDates[date] = currentSet + habitId
        }
        saveData()
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCompletedCount(date: LocalDate): Int {
        return checkedDates[date]?.size ?: 0
    }
}