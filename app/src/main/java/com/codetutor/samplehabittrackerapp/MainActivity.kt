package com.codetutor.samplehabittrackerapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.codetutor.samplehabittrackerapp.ui.theme.SampleHabitTrackerAppTheme

private val Ink = Color(0xFF20233A)
private val MutedInk = Color(0xFF777B91)
private val Page = Color(0xFFF8F8FC)
private val Violet = Color(0xFF6750D9)
private val SoftViolet = Color(0xFFF0EDFF)
private val Mint = Color(0xFF4EBD9D)
private val SoftMint = Color(0xFFE7F8F2)
private val Orange = Color(0xFFF6A847)
private val SoftOrange = Color(0xFFFFF3DD)

data class Habit(
    val id: Int,
    val emoji: String,
    val title: String,
    val detail: String,
    val streak: Int,
    val accent: Color,
    val tint: Color,
    val complete: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SampleHabitTrackerAppTheme(darkTheme = false, dynamicColor = false) {
                HabitTrackerApp()
            }
        }
    }
}

@Composable
private fun HabitTrackerApp() {
    var habits by remember {
        mutableStateOf(
            listOf(
                Habit(1, "💧", "Drink water", "8 glasses", 12, Violet, SoftViolet, true),
                Habit(2, "🧘", "Meditate", "10 minutes", 5, Mint, SoftMint, true),
                Habit(3, "📖", "Read", "20 pages", 8, Orange, SoftOrange),
                Habit(4, "🚶", "Evening walk", "30 minutes", 3, Color(0xFFE26C83), Color(0xFFFFEDF1))
            )
        )
    }
    var section by remember { mutableStateOf("Today") }
    var showAddHabit by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf("Today") }

    val completed = habits.count { it.complete }
    val progress = if (habits.isEmpty()) 0f else completed.toFloat() / habits.size

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Page,
        bottomBar = {
            BottomNavigation(selectedTab = selectedTab, onSelect = { selectedTab = it })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item { ProgressCard(completed, habits.size, progress) }
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Your habits", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Spacer(Modifier.weight(1f))
                    SegmentControl(selected = section, onSelect = { section = it })
                }
            }
            items(habits, key = { it.id }) { habit ->
                HabitCard(
                    habit = habit,
                    onToggle = {
                        habits = habits.map { current ->
                            if (current.id == habit.id) current.copy(complete = !current.complete) else current
                        }
                    }
                )
            }
            item {
                AddHabitButton { showAddHabit = true }
            }
            item { WeeklySummary(habits) }
        }
    }

    if (showAddHabit) {
        AddHabitDialog(
            onDismiss = { showAddHabit = false },
            onAdd = { name, goal ->
                if (name.isNotBlank()) {
                    habits = habits + Habit(
                        id = (habits.maxOfOrNull { it.id } ?: 0) + 1,
                        emoji = "✨",
                        title = name.trim(),
                        detail = goal.ifBlank { "Daily" },
                        streak = 0,
                        accent = Violet,
                        tint = SoftViolet
                    )
                    showAddHabit = false
                }
            }
        )
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text("Good morning, Alex", color = Ink, fontWeight = FontWeight.Bold, fontSize = 26.sp)
            Spacer(Modifier.height(4.dp))
            Text("Tuesday, August 11", color = MutedInk, fontSize = 14.sp)
        }
        Spacer(Modifier.weight(1f))
        Surface(shape = CircleShape, color = SoftViolet, modifier = Modifier.size(48.dp)) {
            Box(contentAlignment = Alignment.Center) { Text("A", color = Violet, fontWeight = FontWeight.Bold, fontSize = 18.sp) }
        }
    }
}

@Composable
private fun ProgressCard(completed: Int, total: Int, progress: Float) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Violet),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 22.dp, vertical = 21.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(86.dp)) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.25f),
                    strokeWidth = 8.dp
                )
                Text("${(progress * 100).toInt()}%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Today’s progress", color = Color.White.copy(alpha = 0.82f), fontSize = 14.sp)
                Spacer(Modifier.height(4.dp))
                Text("$completed of $total habits done", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(7.dp))
                Text(
                    text = if (completed == total && total > 0) "Amazing — you did it!" else "Small actions build big change.",
                    color = Color.White.copy(alpha = 0.82f),
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun SegmentControl(selected: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEEFF5))
            .padding(3.dp)
    ) {
        listOf("Today", "Week").forEach { label ->
            val active = selected == label
            Text(
                text = label,
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (active) Color.White else Color.Transparent)
                    .clickable { onSelect(label) }
                    .padding(horizontal = 11.dp, vertical = 7.dp),
                color = if (active) Ink else MutedInk,
                fontSize = 12.sp,
                fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun HabitCard(habit: Habit, onToggle: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(color = habit.tint, shape = RoundedCornerShape(14.dp), modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Text(habit.emoji, fontSize = 25.sp) }
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    habit.title,
                    color = if (habit.complete) MutedInk else Ink,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    textDecoration = if (habit.complete) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(Modifier.height(3.dp))
                Text(habit.detail, color = MutedInk, fontSize = 13.sp)
                Spacer(Modifier.height(7.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 13.sp)
                    Spacer(Modifier.width(3.dp))
                    Text("${habit.streak} day streak", color = habit.accent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            CompletionButton(complete = habit.complete, color = habit.accent, onClick = onToggle)
        }
    }
}

@Composable
private fun CompletionButton(complete: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        color = if (complete) color else Color.White,
        shape = CircleShape,
        border = if (complete) null else androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFD9DAE5))
    ) {
        Box(contentAlignment = Alignment.Center) {
            if (complete) Text("✓", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 19.sp)
        }
    }
}

@Composable
private fun AddHabitButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(17.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(17.dp),
        color = SoftViolet
    ) {
        Text(
            text = "+  Add a new habit",
            modifier = Modifier.padding(vertical = 17.dp),
            color = Violet,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
private fun WeeklySummary(habits: List<Habit>) {
    val bestStreak = habits.maxOfOrNull { it.streak } ?: 0
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("🏆", fontSize = 27.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Keep your momentum", color = Ink, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(Modifier.height(3.dp))
                Text("Your best streak is $bestStreak days. Keep it going!", color = MutedInk, fontSize = 12.sp)
            }
            Text("View", color = Violet, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun BottomNavigation(selectedTab: String, onSelect: (String) -> Unit) {
    Surface(color = Color.White, shadowElevation = 10.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 11.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            NavItem("◉", "Today", selectedTab == "Today") { onSelect("Today") }
            NavItem("▥", "Progress", selectedTab == "Progress") { onSelect("Progress") }
            NavItem("⚙", "Settings", selectedTab == "Settings") { onSelect("Settings") }
        }
    }
}

@Composable
private fun NavItem(icon: String, label: String, selected: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 3.dp)
    ) {
        Text(icon, color = if (selected) Violet else MutedInk, fontSize = 20.sp)
        Text(label, color = if (selected) Violet else MutedInk, fontSize = 11.sp, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
    }
}

@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create a habit", color = Ink, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Choose one small action you want to repeat daily.", color = MutedInk, fontSize = 14.sp)
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Habit name") }, singleLine = true)
                OutlinedTextField(value = goal, onValueChange = { goal = it }, label = { Text("Daily goal (optional)") }, singleLine = true)
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(name, goal) },
                colors = ButtonDefaults.buttonColors(containerColor = Violet)
            ) { Text("Add habit") }
        },
        dismissButton = { Text("Cancel", modifier = Modifier.clickable(onClick = onDismiss).padding(12.dp), color = MutedInk) }
    )
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun HabitTrackerPreview() {
    SampleHabitTrackerAppTheme(darkTheme = false, dynamicColor = false) {
        HabitTrackerApp()
    }
}
