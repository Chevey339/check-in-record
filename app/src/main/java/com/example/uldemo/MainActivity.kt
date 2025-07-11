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
import com.example.uldemo.ui.theme.CustomFontFamily
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.json.JSONArray

data class DailyHabit(
    val id: Int,
    val title: String,
    val description: String = "",
    val icon: String = "✓"
)

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

// 预定义的每日习惯
val dailyHabits = listOf(
    DailyHabit(1, "喝水", "每天8杯水", "💧"),
//    DailyHabit(2, "运动", "30分钟运动", "🏃"),
//    DailyHabit(3, "冥想", "10分钟冥想", "🧘"),
//    DailyHabit(4, "阅读", "读书30分钟", "📖"),
//    DailyHabit(5, "早睡", "晚上11点前睡觉", "😴")
)

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化打卡管理器
        CheckInManager.init(this)
        
        enableEdgeToEdge()
        setContent {
            UldemoTheme {
                Greeting()
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun Greeting() {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
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
            Text(
                text = "打卡记录",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.height(24.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(
                        elevation = 16.dp,
                        shape = RoundedCornerShape(22.dp),
                        ambientColor = Color.Black.copy(alpha = 0.08f), // 环境光阴影，更柔和
                        spotColor = Color.Black.copy(alpha = 0.2f)       // 主光源阴影
                    )
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color.White)
                    .padding(vertical = 16.dp, horizontal = 8.dp) // 内部留白
            ) {
                PhysicsBasedCalendar(
                    selectedDate = selectedDate,
                    onDateSelected = { newDate -> selectedDate = newDate }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 将日程管理也视为一个独立的模块，遵循相同的设计语言
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f) // 占据剩余空间
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
                // 模块标题
                Text(
                    text = "每日打卡",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = Color.Black.copy(alpha = 0.9f)
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))

                HabitManager(selectedDate = selectedDate)
            }

            // 为系统导航栏预留空间
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
fun HabitManager(selectedDate: LocalDate) {
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
            items(dailyHabits) { habit ->
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
        val totalCount = dailyHabits.size
        
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
        Greeting()
    }
}