package com.essential.spacelite.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.essential.spacelite.data.dao.CaptureEntryDao
import com.essential.spacelite.data.entity.CaptureEntry

@Database(
    entities = [CaptureEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun captureEntryDao(): CaptureEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "essential_space_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
