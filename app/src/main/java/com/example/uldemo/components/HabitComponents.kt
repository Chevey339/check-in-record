package com.example.uldemo.components

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uldemo.R
import com.example.uldemo.managers.CheckInManager
import com.example.uldemo.managers.HabitManager
import com.example.uldemo.models.DailyHabit
import com.example.uldemo.ui.theme.CustomFontFamily
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("UnrememberedMutableState")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HabitManagerComponent(selectedDate: LocalDate) {
    var refreshTrigger by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
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
                        refreshTrigger++
                    }
                )
            }
        }
        
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