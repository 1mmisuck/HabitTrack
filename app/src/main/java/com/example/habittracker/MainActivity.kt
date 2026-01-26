package com.example.habittracker

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.habittracker.data.Habit
import com.example.habittracker.data.HabitViewModel
import com.example.habittracker.data.Category
import java.util.*

enum class AppLanguage { RU, EN }

object Localization {
    val strings = mapOf(
        AppLanguage.RU to mapOf(
            "app_name" to "HabitTracker", "search" to "Поиск...", "all" to "Все", "create" to "Создать",
            "no_habits" to "Пусто", "completed" to "ГОТОВО", "new_habit" to "Новая привычка",
            "title_label" to "Название", "target_label" to "Цель (дни)", "category_label" to "Категория:",
            "save" to "Сохранить", "new_category" to "Новая категория", "name_label" to "Имя", "ok" to "ОК",
            "settings" to "Настройки", "dark_theme" to "Темная тема", "language" to "Язык",
            "history" to "История достижений", "note_label" to "Заметка", "delete" to "Удалить",
            "congrats" to "Поздравляем!", "target_reached" to "Цель выполнена!",
            "fill_all" to "Заполни все поля!", "manage_cat" to "Категории",
            "mon" to "П", "tue" to "В", "wed" to "С", "thu" to "Ч", "fri" to "П", "sat" to "С", "sun" to "В"
        ),
        AppLanguage.EN to mapOf(
            "app_name" to "HabitTracker", "search" to "Search...", "all" to "All", "create" to "Create",
            "no_habits" to "Empty", "completed" to "DONE", "new_habit" to "New Habit",
            "title_label" to "Title", "target_label" to "Goal Days", "category_label" to "Category:",
            "save" to "Save", "new_category" to "New Category", "name_label" to "Name", "ok" to "OK",
            "settings" to "Settings", "dark_theme" to "Dark Mode", "language" to "Language",
            "history" to "Achievement History", "note_label" to "Note", "delete" to "Delete",
            "congrats" to "Congrats!", "target_reached" to "Goal reached!",
            "fill_all" to "Fill all fields!", "manage_cat" to "Categories",
            "mon" to "M", "tue" to "T", "wed" to "W", "thu" to "T", "fri" to "F", "sat" to "S", "sun" to "S"
        )
    )
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val viewModel = ViewModelProvider.AndroidViewModelFactory.getInstance(application).create(HabitViewModel::class.java)
        val prefs = getSharedPreferences("app_config", Context.MODE_PRIVATE)

        setContent {
            var isDark by remember { mutableStateOf(prefs.getBoolean("dark", false)) }
            val savedLang = prefs.getString("lang", "RU") ?: "RU"
            var lang by remember { mutableStateOf(if (savedLang == "RU") AppLanguage.RU else AppLanguage.EN) }
            val t = Localization.strings[lang]!!

            val colorScheme = if (isDark) darkColorScheme(primary = Color(0xFF758BFD), surface = Color(0xFF1B1B1F), background = Color(0xFF121214))
            else lightColorScheme(primary = Color(0xFF4361EE), surface = Color.White, background = Color(0xFFF8F9FA))

            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") { HomeScreen(navController, viewModel, t) }
                        composable("add_habit") { AddHabitScreen(navController, viewModel, t) }
                        composable("manage_categories") { ManageCategoriesScreen(navController, viewModel, t) }
                        composable("settings") {
                            SettingsScreen(navController, isDark, {
                                isDark = it
                                prefs.edit().putBoolean("dark", it).apply()
                            }, lang, {
                                lang = it
                                prefs.edit().putString("lang", it.name).apply()
                            }, t)
                        }
                        composable("details/{id}", arguments = listOf(navArgument("id") { type = NavType.IntType })) {
                            val id = it.arguments?.getInt("id") ?: 0
                            DetailScreen(navController, viewModel, id, t)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdvancedColorPicker(onColorSelected: (Color) -> Unit) {
    var h by remember { mutableStateOf(0f) }
    var s by remember { mutableStateOf(1f) }
    var v by remember { mutableStateOf(1f) }
    val currentColor = Color.hsv(h, s, v)
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(50.dp).clip(CircleShape).background(currentColor).border(2.dp, Color.Gray, CircleShape))
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(12.dp).pointerInput(Unit) { detectDragGestures { change, _ -> h = (change.position.x / size.width).coerceIn(0f, 1f) * 360f } }) {
            drawRect(Brush.horizontalGradient((0..360 step 60).map { Color.hsv(it.toFloat(), 1f, 1f) }))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(12.dp).pointerInput(Unit) { detectDragGestures { change, _ -> s = (change.position.x / size.width).coerceIn(0f, 1f) } }) {
            drawRect(Brush.horizontalGradient(listOf(Color.White, Color.hsv(h, 1f, 1f))))
        }
        Spacer(modifier = Modifier.height(10.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(12.dp).pointerInput(Unit) { detectDragGestures { change, _ -> v = (change.position.x / size.width).coerceIn(0f, 1f) } }) {
            drawRect(Brush.horizontalGradient(listOf(Color.Black, Color.hsv(h, s, 1f))))
        }
        onColorSelected(currentColor)
    }
}

@Composable
fun FooterSection() {
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        HorizontalDivider(modifier = Modifier.width(40.dp), thickness = 2.dp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(12.dp))
        Text("made by 1mmisuck", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.LightGray)
        Text("(Пятигорский Данила)", fontSize = 10.sp, color = Color.LightGray)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: androidx.navigation.NavController, viewModel: HabitViewModel, t: Map<String, String>) {
    val habits by viewModel.habits.collectAsState()
    val categories by viewModel.categories.collectAsState()
    var selectedCat by remember { mutableStateOf(t["all"]!!) }
    var searchQuery by remember { mutableStateOf("") }
    val filtered by remember { derivedStateOf { habits.filter { (selectedCat == t["all"]!! || it.category == selectedCat) && it.title.contains(searchQuery, true) } } }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
                CenterAlignedTopAppBar(
                    navigationIcon = {
                        Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = null,
                            modifier = Modifier.padding(start = 12.dp).size(48.dp).clip(CircleShape)
                        )
                    },
                    title = { Text(t["app_name"]!!, fontWeight = FontWeight.ExtraBold) },
                    actions = { IconButton(onClick = { navController.navigate("settings") }) { Icon(Icons.Default.Settings, null) } }
                )
                OutlinedTextField(value = searchQuery, onValueChange = { searchQuery = it }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), placeholder = { Text(t["search"]!!) }, leadingIcon = { Icon(Icons.Default.Search, null) }, shape = RoundedCornerShape(12.dp), singleLine = true)
            }
        },
        floatingActionButton = { ExtendedFloatingActionButton(onClick = { navController.navigate("add_habit") }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White) { Icon(Icons.Default.Add, null); Spacer(modifier = Modifier.width(8.dp)); Text(t["create"]!!) } }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(horizontal = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                IconButton(onClick = { navController.navigate("manage_categories") }) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { CategoryChip(selectedCat == t["all"]!!, t["all"]!!, MaterialTheme.colorScheme.primary) { selectedCat = t["all"]!! } }
                    items(categories, key = { it.id }) { cat -> CategoryChip(selectedCat == cat.name, cat.name, Color(cat.color)) { selectedCat = cat.name } }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filtered, key = { it.id }) { habit ->
                    val color = categories.find { it.name == habit.category }?.color ?: Color.Gray.toArgb()
                    HabitCard(habit, Color(color), viewModel, t) { navController.navigate("details/${habit.id}") }
                }
                item { FooterSection() }
            }
        }
    }
}

@Composable
fun CategoryChip(selected: Boolean, text: String, color: Color, onClick: () -> Unit) {
    Surface(modifier = Modifier.clickable { onClick() }, color = if (selected) color else MaterialTheme.colorScheme.surfaceVariant.copy(0.5f), shape = RoundedCornerShape(12.dp)) {
        Text(text, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun HabitCard(habit: Habit, accentColor: Color, viewModel: HabitViewModel, t: Map<String, String>, onClick: () -> Unit) {
    val isDone by viewModel.isHabitCompletedToday(habit.id).collectAsState(false)
    val historyDates by viewModel.getHistoryDates(habit.id).collectAsState(emptyList())
    val totalCount = historyDates.size
    val progress = if (habit.targetDays > 0) totalCount.toFloat() / habit.targetDays else 0f
    val finished = totalCount >= habit.targetDays

    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }, shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(accentColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(habit.category.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = accentColor)
                    if (habit.isFavorite) { Spacer(modifier = Modifier.width(8.dp)); Icon(Icons.Default.Bookmark, null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB703)) }
                    if (finished) { Spacer(modifier = Modifier.width(8.dp)); Text(t["completed"]!! + " ✅", fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D6A4F)) }
                }
                Text(habit.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(progress = progress.coerceIn(0f, 1f), modifier = Modifier.fillMaxWidth(0.8f).height(6.dp).clip(CircleShape), color = if(finished) Color(0xFF2D6A4F) else accentColor)
            }
            IconButton(onClick = { viewModel.setHabitStatus(habit.id, !isDone) }, modifier = Modifier.background(if (isDone) Color(0xFFD8F3DC).copy(0.8f) else MaterialTheme.colorScheme.surfaceVariant, CircleShape)) {
                Icon(if (isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked, null, tint = if (isDone) Color(0xFF2D6A4F) else Color.LightGray)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddHabitScreen(navController: androidx.navigation.NavController, viewModel: HabitViewModel, t: Map<String, String>) {
    var title by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("21") }
    var selectedCat by remember { mutableStateOf("") }
    val categories by viewModel.categories.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newCatName by remember { mutableStateOf("") }
    var pickedColor by remember { mutableStateOf(Color.White) }
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text(t["new_habit"]!!) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.Close, null) } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp)) {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text(t["title_label"]!!) }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = target, onValueChange = { target = it }, label = { Text(t["target_label"]!!) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
            Text(t["category_label"]!!, fontWeight = FontWeight.Bold)
            FlowRow(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                categories.forEach { cat ->
                    FilterChip(selected = selectedCat == cat.name, onClick = { selectedCat = cat.name }, label = { Text(cat.name) }, trailingIcon = { Icon(Icons.Default.Cancel, null, modifier = Modifier.size(16.dp).clickable { viewModel.deleteCategory(cat) }) }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(cat.color), selectedLabelColor = if(Color(cat.color)==Color.White) Color.Black else Color.White))
                }
                AssistChip(onClick = { showDialog = true }, label = { Text("+") })
            }
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = {
                if (title.isBlank() || selectedCat.isBlank()) Toast.makeText(context, t["fill_all"]!!, Toast.LENGTH_SHORT).show()
                else { viewModel.addHabit(title, selectedCat, target.toIntOrNull() ?: 21); navController.popBackStack() }
            }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) { Text(t["save"]!!, fontWeight = FontWeight.Bold) }
        }
        if (showDialog) {
            AlertDialog(onDismissRequest = { showDialog = false }, title = { Text(t["new_category"]!!) }, text = {
                Column {
                    OutlinedTextField(value = newCatName, onValueChange = { newCatName = it }, label = { Text(t["name_label"]!!) })
                    Spacer(modifier = Modifier.height(16.dp)); AdvancedColorPicker { pickedColor = it }
                }
            }, confirmButton = { TextButton(onClick = { if (newCatName.isNotEmpty()) { viewModel.addCategory(newCatName, pickedColor.toArgb()); selectedCat = newCatName; newCatName = ""; showDialog = false } }) { Text(t["ok"]!!) } })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageCategoriesScreen(navController: androidx.navigation.NavController, viewModel: HabitViewModel, t: Map<String, String>) {
    val categories by viewModel.categories.collectAsState()
    Scaffold(topBar = { TopAppBar(title = { Text(t["manage_cat"]!!) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(categories, key = { it.id }) { cat ->
                val index = categories.indexOf(cat)
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(cat.color)))
                        Spacer(modifier = Modifier.width(16.dp)); Text(cat.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold)
                        IconButton(onClick = { if(index > 0) {
                            val list = categories.toMutableList()
                            java.util.Collections.swap(list, index, index - 1)
                            viewModel.updateCategoryOrder(list)
                        } }) { Icon(Icons.Default.ArrowUpward, null) }
                        IconButton(onClick = { if(index < categories.size - 1) {
                            val list = categories.toMutableList()
                            java.util.Collections.swap(list, index, index + 1)
                            viewModel.updateCategoryOrder(list)
                        } }) { Icon(Icons.Default.ArrowDownward, null) }
                        IconButton(onClick = { viewModel.deleteCategory(cat) }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(navController: androidx.navigation.NavController, isDark: Boolean, onThemeChange: (Boolean) -> Unit, lang: AppLanguage, onLangChange: (AppLanguage) -> Unit, t: Map<String, String>) {
    Scaffold(topBar = { @OptIn(ExperimentalMaterial3Api::class) TopAppBar(title = { Text(t["settings"]!!) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(painter = painterResource(id = R.drawable.logo), contentDescription = null, modifier = Modifier.size(150.dp).clip(CircleShape))
            Spacer(modifier = Modifier.height(24.dp))
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(t["dark_theme"]!!, fontWeight = FontWeight.Bold); Switch(checked = isDark, onCheckedChange = onThemeChange) }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                    Text(t["language"]!!, fontWeight = FontWeight.Bold); Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = lang == AppLanguage.RU, onClick = { onLangChange(AppLanguage.RU) }, label = { Text("Русский") })
                        FilterChip(selected = lang == AppLanguage.EN, onClick = { onLangChange(AppLanguage.EN) }, label = { Text("English") })
                    }
                }
            }
            Spacer(modifier = Modifier.weight(1f)); FooterSection()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(navController: androidx.navigation.NavController, viewModel: HabitViewModel, habitId: Int, t: Map<String, String>) {
    val habits by viewModel.habits.collectAsState()
    val habit = habits.find { it.id == habitId } ?: return
    val categories by viewModel.categories.collectAsState()
    val color = Color(categories.find { it.name == habit.category }?.color ?: Color.Gray.toArgb())
    val history by viewModel.getHistoryDates(habitId).collectAsState(emptyList())
    var viewDate by remember { mutableStateOf(Calendar.getInstance()) }
    val months = if (t["mon"] == "П") listOf("Январь", "Февраль", "Март", "Апрель", "Май", "Июнь", "Июль", "Август", "Сентябрь", "Октябрь", "Ноябрь", "Декабрь")
    else listOf("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December")

    Scaffold(topBar = { TopAppBar(title = { Text(habit.title) }, navigationIcon = { IconButton(onClick = { navController.popBackStack() }) { Icon(Icons.Default.ArrowBack, null) } }, actions = {
        IconButton(onClick = { viewModel.toggleFavorite(habit) }) { Icon(if(habit.isFavorite) Icons.Default.Bookmark else Icons.Outlined.StarOutline, null, tint = if(habit.isFavorite) Color(0xFFFFB703) else Color.Gray) }
        IconButton(onClick = { viewModel.deleteHabit(habit); navController.popBackStack() }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
    }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(20.dp).fillMaxSize(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            if (history.size >= habit.targetDays) item {
                Surface(color = Color(0xFFD8F3DC).copy(0.5f), shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.EmojiEvents, null, modifier = Modifier.size(40.dp), tint = Color(0xFF2D6A4F)); Spacer(modifier = Modifier.width(12.dp)); Column { Text(t["congrats"]!!, fontWeight = FontWeight.Bold, color = Color(0xFF2D6A4F)); Text(t["target_reached"]!!, fontSize = 12.sp, color = Color(0xFF2D6A4F)) }
                    }
                }
            }
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = color.copy(0.1f))) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { viewDate = (viewDate.clone() as Calendar).apply { add(Calendar.MONTH, -1) } }) { Icon(Icons.Default.ChevronLeft, null) }
                            Text("${months[viewDate.get(Calendar.MONTH)]} ${viewDate.get(Calendar.YEAR)}", fontWeight = FontWeight.Bold)
                            IconButton(onClick = { viewDate = (viewDate.clone() as Calendar).apply { add(Calendar.MONTH, 1) } }) { Icon(Icons.Default.ChevronRight, null) }
                        }
                        CalendarGrid(history, viewDate, color, t) { day -> viewModel.toggleDateCompletion(habitId, day, viewDate.get(Calendar.MONTH), viewDate.get(Calendar.YEAR)) }
                    }
                }
            }
            item { Text(t["note_label"]!!, fontWeight = FontWeight.Bold); OutlinedTextField(value = habit.description, onValueChange = { viewModel.updateHabitNote(habit, it) }, modifier = Modifier.fillMaxWidth().height(120.dp), shape = RoundedCornerShape(16.dp)) }
            item { FooterSection() }
        }
    }
}

@Composable
fun CalendarGrid(history: List<Long>, viewDate: Calendar, color: Color, t: Map<String, String>, onDayClick: (Int) -> Unit) {
    val days = viewDate.getActualMaximum(Calendar.DAY_OF_MONTH)
    val first = (viewDate.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.let { (it.get(Calendar.DAY_OF_WEEK) + 5) % 7 }
    val historySet by remember(history, viewDate) { derivedStateOf { history.map { val c = Calendar.getInstance().apply { timeInMillis = it }; if (c.get(Calendar.MONTH) == viewDate.get(Calendar.MONTH) && c.get(Calendar.YEAR) == viewDate.get(Calendar.YEAR)) c.get(Calendar.DAY_OF_MONTH) else -1 }.toSet() } }
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
            listOf(t["mon"]!!, t["tue"]!!, t["wed"]!!, t["thu"]!!, t["fri"]!!, t["sat"]!!, t["sun"]!!).forEach { Text(it, fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center, modifier = Modifier.width(36.dp)) }
        }
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(columns = GridCells.Fixed(7), modifier = Modifier.height(240.dp), userScrollEnabled = false) {
            items(List(first) { -1 } + (1..days).toList()) { day ->
                if (day != -1) {
                    val done = historySet.contains(day)
                    Box(modifier = Modifier.size(36.dp).padding(2.dp).clip(CircleShape).background(if (done) color else Color.Transparent).clickable { onDayClick(day) }, Alignment.Center) { Text(day.toString(), color = if (done) (if(color == Color.White) Color.Black else Color.White) else MaterialTheme.colorScheme.onSurface, fontSize = 14.sp) }
                }
            }
        }
    }
}