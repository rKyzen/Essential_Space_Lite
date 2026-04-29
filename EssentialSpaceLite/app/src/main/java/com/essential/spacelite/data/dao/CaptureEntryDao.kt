package com.essential.spacelite.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.essential.spacelite.data.entity.CaptureEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureEntryDao {

    @Query("SELECT * FROM capture_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<CaptureEntry>>

    @Query("SELECT * FROM capture_entries ORDER BY timestamp DESC")
    suspend fun getAllEntriesSnapshot(): List<CaptureEntry>

    @Query("SELECT * FROM capture_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): CaptureEntry?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: CaptureEntry): Long

    @Update
    suspend fun update(entry: CaptureEntry)

    @Delete
    suspend fun delete(entry: CaptureEntry)

    @Query("DELETE FROM capture_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM capture_entries")
    suspend fun getCount(): Int

    @Query("DELETE FROM capture_entries")
    suspend fun deleteAll()
}
