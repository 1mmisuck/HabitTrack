package com.example.habittracker.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits ORDER BY isFavorite DESC, createdDate DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit)

    @Update
    suspend fun updateHabit(habit: Habit)

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHistory(history: HabitHistory)

    @Query("DELETE FROM habit_history WHERE habitId = :habitId AND dateCompleted = :date")
    suspend fun deleteHistory(habitId: Int, date: Long)

    @Query("SELECT EXISTS(SELECT * FROM habit_history WHERE habitId = :habitId AND dateCompleted = :date)")
    fun isHabitCompleted(habitId: Int, date: Long): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT * FROM habit_history WHERE habitId = :habitId AND dateCompleted = :date)")
    suspend fun isHabitCompletedSync(habitId: Int, date: Long): Boolean

    @Query("SELECT COUNT(*) FROM habit_history WHERE habitId = :habitId")
    fun getCompletionCount(habitId: Int): Flow<Int>

    @Query("SELECT dateCompleted FROM habit_history WHERE habitId = :habitId")
    fun getHistoryDates(habitId: Int): Flow<List<Long>>

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category)

    @Delete
    suspend fun deleteCategory(category: Category)
}