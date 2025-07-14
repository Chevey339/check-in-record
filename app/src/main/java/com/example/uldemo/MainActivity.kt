package com.example.uldemo

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uldemo.ui.theme.UldemoTheme
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.*
import androidx.compose.ui.text.style.TextAlign
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.mutableStateListOf
import com.example.uldemo.ui.theme.CustomFontFamily
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.IconButton
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.FloatingActionButton
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalUriHandler

// 导航状态枚举
enum class Screen {
    MAIN, EDIT_HABITS, ABOUT
}

data class DailyHabit(
    val id: Int,
    val title: String,
    val description: String = "",
    val icon: String = "✓"
)

// 习惯管理器 - 管理习惯的增删改
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
                // 默认习惯
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

// 全局打卡状态管理
object CheckInManager {
    private val checkedDates = mutableStateMapOf<LocalDate, Set<Int>>()
    private lateinit var sharedPreferences: SharedPreferences
    private const val PREF_NAME = "habit_tracker_prefs"
    private const val KEY_CHECK_DATA = "check_data"
    
    // 初始化
    @RequiresApi(Build.VERSION_CODES.O)
    fun init(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        loadData()
    }
    
    // 加载保存的数据
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
    
    // 保存数据
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
    
    // 检查某个日期是否有任何打卡
    fun isDateChecked(date: LocalDate): Boolean {
        return checkedDates[date]?.isNotEmpty() == true
    }
    
    // 检查某个习惯在某天是否完成
    @RequiresApi(Build.VERSION_CODES.O)
    fun isHabitChecked(date: LocalDate, habitId: Int): Boolean {
        return checkedDates[date]?.contains(habitId) == true
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun toggleHabitCheck(date: LocalDate, habitId: Int) {
        val currentSet = checkedDates[date] ?: emptySet()
        if (currentSet.contains(habitId)) {
            // 取消打卡
            val newSet = currentSet - habitId
            if (newSet.isEmpty()) {
                checkedDates.remove(date)
            } else {
                checkedDates[date] = newSet
            }
        } else {
            // 添加打卡
            checkedDates[date] = currentSet + habitId
        }
        // 保存数据
        saveData()
    }
    
    // 获取某天完成的习惯数量
    @RequiresApi(Build.VERSION_CODES.O)
    fun getCompletedCount(date: LocalDate): Int {
        return checkedDates[date]?.size ?: 0
    }
}

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化管理器
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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainScreen(
    onNavigateToEditHabits: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showMenu by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9F9FB),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(20.dp))
            
            // 顶部栏，包含标题和菜单按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "打卡记录",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = Color.Black
                    )
                )
                
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "菜单",
                            tint = Color.Black
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("编辑习惯") },
                            onClick = {
                                showMenu = false
                                onNavigateToEditHabits()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("关于") },
                            onClick = {
                                showMenu = false
                                onNavigateToAbout()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(vertical = 16.dp, horizontal = 8.dp)
            ) {
                PhysicsBasedCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = { newDate -> selectedDate = newDate }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Text(
                    text = "每日打卡",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black.copy(alpha = 0.9f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                HabitManagerComponent(selectedDate = selectedDate)
            }

            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EditHabitsScreen(onNavigateBack: () -> Unit) {
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<DailyHabit?>(null) }
    var showDeleteDialog by remember { mutableStateOf<DailyHabit?>(null) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9F9FB),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(20.dp))
            
            // 顶部栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.Black
                    )
                }
                
                Text(
                    text = "编辑习惯",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 习惯列表
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                if (HabitManager.habits.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "暂无习惯，点击下方按钮添加",
                            color = Color.Gray,
                            fontFamily = CustomFontFamily
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(HabitManager.habits) { habit ->
                            EditableHabitItem(
                                habit = habit,
                                onEdit = { editingHabit = habit },
                                onDelete = { showDeleteDialog = habit }
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(10.dp))
        }
        
        // 添加按钮
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .navigationBarsPadding(),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "添加习惯",
                tint = Color.White
            )
        }
    }
    
    // 添加/编辑对话框
    if (showAddDialog || editingHabit != null) {
        HabitEditDialog(
            habit = editingHabit,
            onDismiss = {
                showAddDialog = false
                editingHabit = null
            },
            onSave = { title, description, icon ->
                if (editingHabit != null) {
                    HabitManager.updateHabit(editingHabit!!.id, title, description, icon)
                } else {
                    HabitManager.addHabit(title, description, icon)
                }
                showAddDialog = false
                editingHabit = null
            }
        )
    }
    
    // 删除确认对话框
    showDeleteDialog?.let { habit ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("删除习惯") },
            text = { Text("确定要删除「${habit.title}」吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        HabitManager.deleteHabit(habit.id)
                        showDeleteDialog = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
fun EditableHabitItem(
    habit: DailyHabit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.04f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = habit.icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                color = Color.Black,
                fontFamily = CustomFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = habit.description,
                color = Color.Gray,
                fontFamily = CustomFontFamily,
                fontSize = 12.sp
            )
        }
        
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "编辑",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "删除",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun HabitEditDialog(
    habit: DailyHabit?,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf(habit?.title ?: "") }
    var description by remember { mutableStateOf(habit?.description ?: "") }
    var icon by remember { mutableStateOf(habit?.icon ?: "✓") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (habit == null) "添加习惯" else "编辑习惯") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("习惯名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("描述") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                OutlinedTextField(
                    value = icon,
                    onValueChange = { icon = it },
                    label = { Text("图标 (emoji)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(title.trim(), description.trim(), icon.trim())
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF9F9FB),
                        Color.White
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.statusBarsPadding())
            Spacer(modifier = Modifier.height(20.dp))
            
            // 顶部栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回",
                        tint = Color.Black
                    )
                }
                
                Text(
                    text = "关于",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = Color.Black
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 关于内容
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f),
                        spotColor = Color.Black.copy(alpha = 0.2f)
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 应用图标
                Text(
                    text = "📊",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Text(
                    text = "习惯打卡",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Text(
                    text = "版本 1.0.0",
                    color = Color.Gray,
                    fontFamily = CustomFontFamily,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                
                // 应用介绍
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = "📝 应用介绍",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    Text(
                        text = "这是一个简洁优雅的习惯养成应用，帮助您建立和维持良好的日常习惯。通过日历视图和打卡功能，让习惯养成变得更加直观和有趣。",
                        color = Color.Black.copy(alpha = 0.8f),
                        fontFamily = CustomFontFamily,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Text(
                        text = "✨ 主要功能",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color.Black
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    
                    val features = listOf(
                        "📅 直观的日历视图",
                        "✅ 简单的打卡操作",
                        "📊 完成进度统计",
                        "🎯 自定义习惯管理",
                        "💾 本地数据存储"
                    )
                    
                    features.forEach { feature ->
                        Text(
                            text = feature,
                            color = Color.Black.copy(alpha = 0.8f),
                            fontFamily = CustomFontFamily,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // GitHub链接
                Button(
                    onClick = {
                        uriHandler.openUri("https://github.com/Chevey339/check-in-record")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = null,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("GitHub 开源地址")
                }
                
                Text(
                    text = "开发者：17",
                    color = Color.Gray,
                    fontFamily = CustomFontFamily,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            Spacer(modifier = Modifier.navigationBarsPadding())
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PhysicsBasedCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val currentMonth = YearMonth.now()
    val initialPage = Int.MAX_VALUE / 2
    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { Int.MAX_VALUE })

    val displayedMonth = currentMonth.plusMonths((pagerState.currentPage - initialPage).toLong())

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        val monthTitleFormatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
        Text(
            text = displayedMonth.format(monthTitleFormatter),
            fontSize = 22.sp,
            fontFamily = CustomFontFamily,
            fontWeight = FontWeight.Bold,
            color = Color.Black.copy(alpha = 0.9f), // [白天模式适应] 白色 -> 黑色
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            // DayOfWeek.values() 在某些环境中顺序不固定，这里明确指定顺序
            val daysOfWeek = listOf(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
            for (day in daysOfWeek) {
                Text(
                    text = day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    color = Color.Gray, // [白天模式适应] 白色 -> 灰色，用于次要信息
                    fontSize = 14.sp,
                    fontFamily = CustomFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.height(300.dp),
        ) { page ->
            val month = currentMonth.plusMonths((page - initialPage).toLong())
            MonthView(
                month = month,
                selectedDate = selectedDate,
                onDateSelected = onDateSelected
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DayCell(
    date: LocalDate,
    isSelected: Boolean,
    isCurrentMonth: Boolean,
    onDateSelected: (LocalDate) -> Unit
) {
    val isToday = date == LocalDate.now()
    val isChecked = CheckInManager.isDateChecked(date) // 检查是否已打卡
    val cellColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent // [风格优化] 选中时使用主题色，更醒目
    val textColor = when {
        isSelected -> Color.White // [白天模式适应] 在主题色背景上，用白色文字
        isToday -> MaterialTheme.colorScheme.primary // [风格优化] 今天日期也使用主题色强调
        else -> Color.Black // [关键修复] 默认文字颜色，从白色改为黑色
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(cellColor)
            .clickable { onDateSelected(date) },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color = textColor.copy(alpha = if (isCurrentMonth) 1.0f else 0.4f), // 非本月日期变淡
                fontFamily = CustomFontFamily,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                fontSize = 16.sp
            )
            
            // 打卡标记
            if (isChecked) {
                Spacer(modifier = Modifier.height(2.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Color.White
                            else MaterialTheme.colorScheme.primary
                        )
                )
            }
        }
    }
}

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitManagerComponent(selectedDate: LocalDate) {
    // 添加一个状态来触发重组
    var refreshTrigger by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 显示当前日期
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日", Locale.CHINESE)
        Text(
            text = selectedDate.format(dateFormatter),
            fontSize = 14.sp,
            fontFamily = CustomFontFamily,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(HabitManager.habits) { habit ->
                HabitItem(
                    habit = habit,
                    date = selectedDate,
                    isChecked = CheckInManager.isHabitChecked(selectedDate, habit.id),
                    onCheckChanged = {
                        CheckInManager.toggleHabitCheck(selectedDate, habit.id)
                        refreshTrigger++ // 触发重组
                    }
                )
            }
        }
        
        // 显示完成进度
        val completedCount = CheckInManager.getCompletedCount(selectedDate)
        val totalCount = HabitManager.habits.size
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black.copy(alpha = 0.04f))
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "今日完成：$completedCount / $totalCount",
                fontSize = 14.sp,
                fontFamily = CustomFontFamily,
                color = if (completedCount == totalCount) MaterialTheme.colorScheme.primary else Color.Gray,
                fontWeight = if (completedCount == totalCount) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun HabitItem(
    habit: DailyHabit,
    date: LocalDate,
    isChecked: Boolean,
    onCheckChanged: () -> Unit
) {
    val textAlpha by animateFloatAsState(targetValue = if (isChecked) 0.6f else 1f)
    val textColor = Color.Black
    val secondaryTextColor = Color.Gray

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black.copy(alpha = 0.04f))
            .clickable { onCheckChanged() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 习惯图标
        Text(
            text = habit.icon,
            fontSize = 24.sp,
            modifier = Modifier.padding(end = 12.dp)
        )
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = habit.title,
                color = textColor.copy(alpha = textAlpha),
                fontFamily = CustomFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                textDecoration = if (isChecked) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = habit.description,
                color = secondaryTextColor.copy(alpha = textAlpha),
                fontFamily = CustomFontFamily,
                fontSize = 12.sp
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // 打卡按钮
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    if (isChecked) MaterialTheme.colorScheme.primary
                    else Color.Black.copy(alpha = 0.08f)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isChecked) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_circle_solid),
                    contentDescription = "已完成",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MonthView(
    month: YearMonth,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    val daysInMonth = month.lengthOfMonth()
    val firstDayOfMonth = month.atDay(1).dayOfWeek
    // 计算网格开始前的空白天数 (假设周日是第一天)
    val paddingDays = (firstDayOfMonth.value % 7)

    val calendarDays = mutableListOf<LocalDate?>()
    // 添加上个月的尾巴
    for (i in 0 until paddingDays) {
        calendarDays.add(null) // 使用null作为占位符
    }
    // 添加本月的所有天
    for (i in 1..daysInMonth) {
        calendarDays.add(month.atDay(i))
    }

    // 日期网格
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        userScrollEnabled = false // 禁止网格内部滚动，由Pager控制
    ) {
        items(calendarDays) { date ->
            if (date != null) {
                DayCell(
                    date = date,
                    isSelected = date == selectedDate,
                    isCurrentMonth = true, // 在这个视图里都是当前月
                    onDateSelected = onDateSelected
                )
            } else {
                // 空白占位符
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UldemoTheme {
        AppNavigation()
    }
}