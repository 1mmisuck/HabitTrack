package com.example.habittracker.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).habitDao()

    val habits = dao.getAllHabits().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val categories = dao.getAllCategories().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addHabit(title: String, category: String, targetDays: Int) {
        viewModelScope.launch {
            dao.insertHabit(Habit(title = title, category = category, targetDays = targetDays))
        }
    }

    fun toggleFavorite(habit: Habit) {
        viewModelScope.launch {
            dao.updateHabit(habit.copy(isFavorite = !habit.isFavorite))
        }
    }

    fun updateHabitNote(habit: Habit, newNote: String) {
        viewModelScope.launch { dao.updateHabit(habit.copy(description = newNote)) }
    }

    fun addCategory(name: String, color: Int) {
        viewModelScope.launch { dao.insertCategory(Category(name = name, color = color)) }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch { dao.deleteCategory(category) }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch { dao.deleteHabit(habit) }
    }

    fun setHabitStatus(habitId: Int, isCompleted: Boolean) {
        val today = getBaseDate(Calendar.getInstance())
        viewModelScope.launch {
            if (isCompleted) dao.insertHistory(HabitHistory(habitId = habitId, dateCompleted = today))
            else dao.deleteHistory(habitId, today)
        }
    }

    fun toggleDateCompletion(habitId: Int, day: Int, month: Int, year: Int) {
        viewModelScope.launch {
            val cal = Calendar.getInstance().apply {
                set(year, month, day, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val date = cal.timeInMillis
            if (dao.isHabitCompletedSync(habitId, date)) dao.deleteHistory(habitId, date)
            else dao.insertHistory(HabitHistory(habitId = habitId, dateCompleted = date))
        }
    }

    fun isHabitCompletedToday(habitId: Int) = dao.isHabitCompleted(habitId, getBaseDate(Calendar.getInstance()))
    fun getHabitStats(habitId: Int) = dao.getCompletionCount(habitId)
    fun getHistoryDates(habitId: Int) = dao.getHistoryDates(habitId)

    private fun getBaseDate(cal: Calendar): Long {
        return cal.apply {
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}