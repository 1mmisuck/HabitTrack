package com.example.habittracker

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habittracker.data.Habit
import com.example.habittracker.data.HabitViewModel
import java.util.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(HabitViewModel::class.java)
        setContent {
            val navController = rememberNavController()
            MaterialTheme(colorScheme = lightColorScheme(primary = Color(0xFF4361EE), surface = Color.White, background = Color(0xFFF8F9FA))) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController, viewModel) }
                        composable("add_habit") { AddHabitScreen(navController, viewModel) }
                        composable("details/{id}", listOf(navArgument("id") { type = NavType.IntType })) {
                            val id = it.arguments?.getInt("id") ?: 0
                            DetailScreen(navController, viewModel, id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FooterSection() {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Divider(modifier = Modifier.width(40.dp), thickness = 2.dp, color = Color.LightGray)
        Spacer(Modifier.height(12.dp))
        Text("made by 1mmisuck", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        Text("(Пятигорский Данила)", fontSize = 10.sp, color = Color.LightGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: HabitViewModel) {
    val habits by viewModel.habits.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var selectedCat by remember { mutableStateOf("Все") }
    var searchQuery by remember { mutableStateOf("") }

    val filteredHabits by remember {
        derivedStateOf {
            habits.filter {
                (selectedCat == "Все" || it.category == selectedCat) &&
                        it.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            Column(Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(title = { Text("HabitTracker", fontWeight = FontWeight.ExtraBold) })
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
                    placeholder = { Text("Поиск...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color(0xFFF1F3F5))
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { navController.navigate("add_habit") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Создать")
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(horizontal = 20.dp)) {
            LazyRow(Modifier.padding(vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item { CategoryChip(selectedCat == "Все", "Все", Color(0xFF4361EE)) { selectedCat = "Все" } }
                items(categories, key = { it.id }) { cat ->
                    CategoryChip(selectedCat == cat.name, cat.name, Color(cat.color)) { selectedCat = cat.name }
                }
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(filteredHabits, key = { it.id }) { habit ->
                    val catColor = categories.find { it.name == habit.category }?.color ?: Color.Gray.toArgb()
                    HabitCard(habit, Color(catColor), viewModel) { navController.navigate("details/${habit.id}") }
                }
                item { FooterSection() }
            }
        }
    }
}

@Composable
fun CategoryChip(selected: Boolean, text: String, color: Color, onClick: () -> Unit) {
    Surface(
        Modifier.clickable { onClick() },
        color = if (selected) color else Color(0xFFE9ECEF),
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text, Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (selected) Color.White else Color.DarkGray, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun HabitCard(habit: Habit, accentColor: Color, viewModel: HabitViewModel, onClick: () -> Unit) {
    val isDone by viewModel.isHabitCompletedToday(habit.id).collectAsState(false)
    val count by viewModel.getHabitStats(habit.id).collectAsState(0)
    val isFinished = count >= habit.targetDays
    val progress = if (habit.targetDays > 0) count.toFloat() / habit.targetDays else 0f

    Card(Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = Color.White), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Spacer(Modifier.width(6.dp))
                    Text(habit.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    if (habit.isFavorite) {
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.Bookmark, null, Modifier.size(14.dp), Color(0xFFFFB703))
                    }
                }
                Text(habit.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(progress.coerceIn(0f, 1f), Modifier.fillMaxWidth(0.8f).height(6.dp).clip(CircleShape), color = if(isFinished) Color(0xFF2D6A4F) else accentColor)
            }
            IconButton(onClick = { viewModel.setHabitStatus(habit.id, !isDone) }, Modifier.background(if (isDone) Color(0xFFD8F3DC) else Color(0xFFF1F3F5), CircleShape)) {
                Icon(if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (isDone) Color(0xFF2D6A4F) else Color.LightGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(navController: NavController, viewModel: HabitViewModel) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("21") }
    var selectedCat by remember { mutableStateOf("") }
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newCat by remember { mutableStateOf("") }
    var newCatColor by remember { mutableStateOf(Color(0xFF4361EE)) }
    val context = LocalContext.current
    val colors = listOf(Color(0xFF4361EE), Color(0xFFE91E63), Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFF9C27B0), Color(0xFF00BCD4))

    Scaffold(topBar = { TopAppBar(title = { Text("Новая привычка") }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } }) }) { padding ->
        Column(Modifier.padding(padding).padding(20.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text("Цель (дней)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(24.dp))
            Text("Категория:", fontWeight = FontWeight.Bold)
            FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(selected = selectedCat == cat.name, onClick = { selectedCat = cat.name }, label = { Text(cat.name) }, trailingIcon = { Icon(Icons.Default.Cancel, null, Modifier.size(16.dp).clickable { viewModel.deleteCategory(cat) }) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(cat.color), selectedLabelColor = Color.White))
                }
                AssistChip(onClick = { showDialog = true }, label = { Text("+ Категория") })
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = {
                if (title.isBlank() || selectedCat.isBlank()) Toast.makeText(context, "Заполни все поля!", Toast.LENGTH_SHORT).show()
                else { viewModel.addHabit(title, selectedCat, target.toIntOrNull() ?: 21); navController.popBackStack() }
            }, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text("Сохранить", fontWeight = FontWeight.Bold) }
        }
        if (showDialog) {
            AlertDialog(onDismissRequest = { showDialog = false }, title = { Text("Новая категория") }, text = {
                Column {
                    OutlinedTextField(value = newCat, onValueChange = { newCat = it }, label = { Text("Имя") })
                    Spacer(Modifier.height(16.dp))
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                        colors.forEach { color ->
                            Box(Modifier.size(32.dp).clip(CircleShape).background(color).border(if(newCatColor == color) 2.dp else 0.dp, Color.Black, CircleShape).clickable { newCatColor = color })
                        }
                    }
                }
            }, confirmButton = { TextButton(onClick = { if (newCat.isNotEmpty()) { viewModel.addCategory(newCat, newCatColor.toArgb()); selectedCat = newCat; newCat = ""; showDialog = false } }) { Text("ОК") } })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: NavController, viewModel: HabitViewModel, habitId: Int) {
    val habits by viewModel.habits.collectAsState()
    val habit = habits.find { it.id == habitId } ?: return
    val categories by viewModel.categories.collectAsState()
    val accentColor = Color(categories.find { it.name == habit.category }?.color ?: Color.Gray.toArgb())
    val history by viewModel.getHistoryDates(habitId).collectAsState(emptyList())
    var viewDate by remember { mutableStateOf(Calendar.getInstance()) }
    val monthNames = listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")

    Scaffold(topBar = {
        TopAppBar(title = { Text(habit.title) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }, actions = {
            IconButton(onClick = { viewModel.toggleFavorite(habit) }) { Icon(if(habit.isFavorite) Icons.Default.Bookmark else Icons.Outlined.StarOutline, null, tint = if(habit.isFavorite) Color(0xFFFFB703) else Color.Gray) }
            IconButton(onClick = { viewModel.deleteHabit(habit); navController.popBackStack() }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
        })
    }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val isFinished = history.size >= habit.targetDays
            if (isFinished) {
                item {
                    Surface(color = Color(0xFFD8F3DC), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.EmojiEvents, null, tint = Color(0xFF2D6A4F), modifier = Modifier.size(40.dp))
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text("Поздравляем!", fontWeight = FontWeight.Bold, color = Color(0xFF2D6A4F))
                                Text("Цель достигнута! Привычка закреплена.", fontSize = 12.sp, color = Color(0xFF2D6A4F))
                            }
                        }
                    }
                }
            }
            item {
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = accentColor.copy(0.1f))) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            IconButton(onClick = { viewDate = (viewDate.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) { Icon(Icons.Default.ChevronLeft, null) }
                            Text("${monthNames[viewDate.get(Calendar.MONTH)]} ${viewDate.get(Calendar.YEAR)}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewDate = (viewDate.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) { Icon(Icons.Default.ChevronRight, null) }
                        }
                        CalendarGrid(history, viewDate, accentColor) { day -> viewModel.toggleDateCompletion(habitId, day, viewDate.get(Calendar.MONTH), viewDate.get(Calendar.YEAR)) }
                    }
                }
            }
            item {
                Text("Заметка", fontWeight = FontWeight.Bold)
                OutlinedTextField(value = habit.description, onValueChange = { viewModel.updateHabitNote(habit, it) }, Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(16.dp))
            }
            item { FooterSection() }
        }
    }
}

@Composable
fun CalendarGrid(history: List<Long>, viewDate: Calendar, accentColor: Color, onDayClick: (Int) -> Unit) {
    val daysInMonth = viewDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDay = (viewDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.let { (it.get(Calendar.DAY_OF_WEEK) + 5) % 7 }
    val historySet by remember(history, viewDate) { derivedStateOf { history.map { val c = Calendar.getInstance().apply { timeInMillis = it }; if (c.get(Calendar.MONTH) == viewDate.get(Calendar.MONTH) && c.get(Calendar.YEAR) == viewDate.get(Calendar.YEAR)) c.get(Calendar.DAY_OF_MONTH) else -1 }.toSet() } }

    Column {
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceAround) {
            listOf("П", "В", "С", "Ч", "П", "С", "В").forEach { Text(it, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp)) }
        }
        Spacer(Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.height(240.dp),
            userScrollEnabled = false
        ) {
            items(List(firstDay) { -1 } + (1..daysInMonth).toList()) { day ->
                if (day != -1) {
                    val done = historySet.contains(day)
                    Box(Modifier.size(36.dp).padding(2.dp).clip(CircleShape).background(if (done) accentColor else Color.Transparent).clickable { onDayClick(day) }, Alignment.Center) { Text(day.toString(), color = if (done) Color.White else Color.Black, fontSize = 14.sp) }
                }
            }
        }
    }
}