package com.init.space.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.init.space.data.entity.CaptureEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppDatabase private constructor(context: Context) : SQLiteOpenHelper(
    context.applicationContext,
    DATABASE_NAME,
    null,
    DATABASE_VERSION
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val _entriesFlow = MutableStateFlow<List<CaptureEntry>>(emptyList())
    val entriesFlow: StateFlow<List<CaptureEntry>> = _entriesFlow.asStateFlow()

    init {
        refreshEntries()
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS $TABLE_NAME (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                screenshot_path TEXT NOT NULL,
                thumbnail_path TEXT NOT NULL,
                text_note TEXT,
                voice_note_path TEXT,
                voice_note_duration_ms INTEGER NOT NULL DEFAULT 0,
                timestamp INTEGER NOT NULL,
                reminder_at INTEGER,
                ai_summary TEXT,
                app_name TEXT,
                is_favorite INTEGER NOT NULL DEFAULT 0
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN reminder_at INTEGER")
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN ai_summary TEXT")
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN is_favorite INTEGER NOT NULL DEFAULT 0")
        }
    }

    fun refreshEntries() {
        scope.launch {
            val list = queryAllEntriesSnapshot()
            _entriesFlow.value = list
        }
    }

    suspend fun queryAllEntriesSnapshot(): List<CaptureEntry> = withContext(Dispatchers.IO) {
        val result = mutableListOf<CaptureEntry>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            null,
            null,
            null,
            null,
            "timestamp DESC"
        )
        cursor.use { c ->
            while (c.moveToNext()) {
                result.add(cursorToEntry(c))
            }
        }
        result
    }

    suspend fun getEntryById(id: Long): CaptureEntry? = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "id = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        )
        cursor.use { c ->
            if (c.moveToFirst()) {
                cursorToEntry(c)
            } else {
                null
            }
        }
    }

    suspend fun insert(entry: CaptureEntry): Long = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = entryToContentValues(entry).apply {
            if (entry.id == 0L) {
                remove("id")
            }
        }
        val rowId = db.insertWithOnConflict(TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE)
        refreshEntries()
        rowId
    }

    suspend fun update(entry: CaptureEntry) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        val values = entryToContentValues(entry)
        db.update(TABLE_NAME, values, "id = ?", arrayOf(entry.id.toString()))
        refreshEntries()
    }

    suspend fun delete(entry: CaptureEntry) = withContext(Dispatchers.IO) {
        deleteById(entry.id)
    }

    suspend fun deleteById(id: Long) = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "id = ?", arrayOf(id.toString()))
        refreshEntries()
    }

    suspend fun deleteAll() = withContext(Dispatchers.IO) {
        val db = writableDatabase
        db.delete(TABLE_NAME, null, null)
        refreshEntries()
    }

    suspend fun getCount(): Int = withContext(Dispatchers.IO) {
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT COUNT(*) FROM $TABLE_NAME", null)
        cursor.use { c ->
            if (c.moveToFirst()) c.getInt(0) else 0
        }
    }

    private fun cursorToEntry(c: Cursor): CaptureEntry {
        val id = c.getLong(c.getColumnIndexOrThrow("id"))
        val screenshotPath = c.getString(c.getColumnIndexOrThrow("screenshot_path"))
        val thumbnailPath = c.getString(c.getColumnIndexOrThrow("thumbnail_path"))
        val textNote = c.getString(c.getColumnIndexOrThrow("text_note"))
        val voiceNotePath = c.getString(c.getColumnIndexOrThrow("voice_note_path"))
        val voiceNoteDurationMs = c.getLong(c.getColumnIndexOrThrow("voice_note_duration_ms"))
        val timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
        val reminderAt = if (c.isNull(c.getColumnIndexOrThrow("reminder_at"))) null else c.getLong(c.getColumnIndexOrThrow("reminder_at"))
        val aiSummary = c.getString(c.getColumnIndexOrThrow("ai_summary"))
        val appName = c.getString(c.getColumnIndexOrThrow("app_name"))
        val isFavorite = c.getInt(c.getColumnIndexOrThrow("is_favorite")) == 1

        return CaptureEntry(
            id = id,
            screenshotPath = screenshotPath,
            thumbnailPath = thumbnailPath,
            textNote = textNote,
            voiceNotePath = voiceNotePath,
            voiceNoteDurationMs = voiceNoteDurationMs,
            timestamp = timestamp,
            reminderAt = reminderAt,
            aiSummary = aiSummary,
            appName = appName,
            isFavorite = isFavorite
        )
    }

    private fun entryToContentValues(entry: CaptureEntry): ContentValues {
        return ContentValues().apply {
            if (entry.id != 0L) {
                put("id", entry.id)
            }
            put("screenshot_path", entry.screenshotPath)
            put("thumbnail_path", entry.thumbnailPath)
            put("text_note", entry.textNote)
            put("voice_note_path", entry.voiceNotePath)
            put("voice_note_duration_ms", entry.voiceNoteDurationMs)
            put("timestamp", entry.timestamp)
            put("reminder_at", entry.reminderAt)
            put("ai_summary", entry.aiSummary)
            put("app_name", entry.appName)
            put("is_favorite", if (entry.isFavorite) 1 else 0)
        }
    }

    companion object {
        const val DATABASE_NAME = "essential_space_db"
        const val DATABASE_VERSION = 4
        const val TABLE_NAME = "capture_entries"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = AppDatabase(context)
                INSTANCE = instance
                instance
            }
        }
    }
}
