package com.init.space.data

import com.init.space.data.entity.CaptureEntry
import kotlinx.coroutines.flow.Flow

class CaptureRepository(private val db: AppDatabase) {

    val allEntries: Flow<List<CaptureEntry>> = db.entriesFlow

    suspend fun getAllEntriesSnapshot(): List<CaptureEntry> = db.queryAllEntriesSnapshot()

    suspend fun insert(entry: CaptureEntry): Long = db.insert(entry)

    suspend fun update(entry: CaptureEntry) = db.update(entry)

    suspend fun delete(entry: CaptureEntry) = db.delete(entry)

    suspend fun deleteById(id: Long) = db.deleteById(id)

    suspend fun getEntryById(id: Long): CaptureEntry? = db.getEntryById(id)

    suspend fun getCount(): Int = db.getCount()

    suspend fun deleteAll() = db.deleteAll()
}
