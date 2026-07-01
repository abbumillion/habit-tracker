package com.app.habit.data


import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.util.Date
import kotlin.jvm.JvmSuppressWildcards

@Dao
@JvmSuppressWildcards
interface HabitDao {

    @Query("SELECT * FROM habits ORDER BY id DESC")
    fun getAllHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits WHERE isActive = 1 ORDER BY id DESC")
    fun getActiveHabits(): Flow<List<Habit>>

    @Insert
    suspend fun insertHabit(habit: Habit): Long

    @Update
    suspend fun updateHabit(habit: Habit): Int

    @Delete
    suspend fun deleteHabit(habit: Habit): Int

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun getHabitById(id: Int): Habit?

    @Query("UPDATE habits SET streak = streak + 1, lastCompletedDate = :date WHERE id = :id")
    suspend fun incrementStreak(id: Int, date: Date): Int

    @Query("UPDATE habits SET streak = 0 WHERE id = :id")
    suspend fun resetStreak(id: Int): Int

    @Query("SELECT COUNT(*) FROM habits WHERE isActive = 1")
    suspend fun getActiveHabitsCount(): Int

    @Insert
    suspend fun insertCompletionRecord(record: CompletionRecord)

    @Query("SELECT * FROM completion_records ORDER BY date ASC")
    fun getAllCompletionRecords(): Flow<List<CompletionRecord>>

    @Query("SELECT * FROM completion_records WHERE habitId = :habitId ORDER BY date ASC")
    fun getCompletionRecordsForHabit(habitId: Int): Flow<List<CompletionRecord>>
}