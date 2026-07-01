package com.app.habit.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date

@Database(
    entities = [Habit::class, User::class, CompletionRecord::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: HabitDatabase? = null

        private val databaseWriteExecutor = CoroutineScope(Dispatchers.IO)

        fun getInstance(context: Context): HabitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    HabitDatabase::class.java,
                    "habit_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class SeedDatabaseCallback : Callback() {
            override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onCreate(db)
                seedData()
            }

            override fun onDestructiveMigration(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                seedData()
            }

            private fun seedData() {
                databaseWriteExecutor.launch {
                    val database = INSTANCE ?: return@launch
                    val habitDao = database.habitDao()
                    
                    // Only seed if empty
                    database.runInTransaction {
                        // We can't easily check count here without a direct query, 
                        // but since it's onCreate/onDestructiveMigration, it should be empty.
                    }

                    val sampleHabits = listOf(
                        Habit(
                            name = "Exercise",
                            description = "30 minutes of physical activity",
                            frequency = "Daily",
                            category = "Health",
                            createdAt = Date()
                        ),
                        Habit(
                            name = "Read",
                            description = "Read 20 pages of a book",
                            frequency = "Daily",
                            category = "Personal",
                            createdAt = Date()
                        ),
                        Habit(
                            name = "Meditate",
                            description = "10 minutes of meditation",
                            frequency = "Daily",
                            category = "Health",
                            createdAt = Date()
                        )
                    )

                    sampleHabits.forEach { habit ->
                        habitDao.insertHabit(habit)
                    }
                }
            }
        }
    }
}