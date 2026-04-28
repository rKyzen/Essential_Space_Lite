package com.essential.spacelite.data

import com.essential.spacelite.data.dao.CaptureEntryDao
import com.essential.spacelite.data.entity.CaptureEntry
import kotlinx.coroutines.flow.Flow

class CaptureRepository(private val dao: CaptureEntryDao) {

    val allEntries: Flow<List<CaptureEntry>> = dao.getAllEntries()

    suspend fun insert(entry: CaptureEntry): Long = dao.insert(entry)

    suspend fun update(entry: CaptureEntry) = dao.update(entry)

    suspend fun delete(entry: CaptureEntry) = dao.delete(entry)

    suspend fun deleteById(id: Long) = dao.deleteById(id)

    suspend fun getEntryById(id: Long): CaptureEntry? = dao.getEntryById(id)
}
