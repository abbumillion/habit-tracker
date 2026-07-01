package com.app.habit.ui


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.app.habit.data.CompletionRecord
import com.app.habit.data.Habit
import com.app.habit.data.HabitDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val habitDao = HabitDatabase.getInstance(application).habitDao()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val completionRecords: StateFlow<List<CompletionRecord>> = habitDao.getAllCompletionRecords()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val habits: StateFlow<List<Habit>> = habitDao.getAllHabits()
        .onStart { 
            _isLoading.value = true
            delay(1000) // Artificial delay to ensure visibility of loading state
        }
        .onEach { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Data loading is handled by the flows
    }

    fun addHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.insertHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            habitDao.deleteHabit(habit)
        }
    }

    fun completeHabit(habit: Habit) {
        viewModelScope.launch {
            val lastDate = habit.lastCompletedDate
            if (lastDate != null && isToday(lastDate)) {
                return@launch
            }

            if (lastDate != null && !isYesterday(lastDate) && !isToday(lastDate)) {
                habitDao.resetStreak(habit.id)
            }

            habitDao.incrementStreak(habit.id, Date())
            habitDao.insertCompletionRecord(CompletionRecord(habitId = habit.id, date = Date()))
        }
    }

    fun resetStreak(habit: Habit) {
        viewModelScope.launch {
            habitDao.resetStreak(habit.id)
        }
    }

    private fun isToday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val other = Calendar.getInstance().apply { time = date }
        return today.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(date: Date): Boolean {
        val today = Calendar.getInstance()
        val other = Calendar.getInstance().apply { time = date }
        today.add(Calendar.DAY_OF_YEAR, -1)
        return today.get(Calendar.YEAR) == other.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == other.get(Calendar.DAY_OF_YEAR)
    }
}