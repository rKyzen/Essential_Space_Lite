package com.essential.spacelite.timeline

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.essential.spacelite.data.AppDatabase
import com.essential.spacelite.data.CaptureRepository
import com.essential.spacelite.data.entity.CaptureEntry
import com.essential.spacelite.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class TimelineViewModel(application: Application) : AndroidViewModel(application) {

    enum class FilterMode {
        ALL, TODAY, NOTES, VOICE
    }

    private val repo = CaptureRepository(
        AppDatabase.getDatabase(application).captureEntryDao()
    )

    private val query = MutableStateFlow("")
    private val filterMode = MutableStateFlow(FilterMode.ALL)

    private val allEntries: StateFlow<List<CaptureEntry>> = repo.allEntries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val entries: StateFlow<List<CaptureEntry>> = combine(allEntries, query, filterMode) { entries, rawQuery, mode ->
        val normalized = rawQuery.trim()
        entries.filter { entry ->
            matchesFilter(entry, mode) && matchesQuery(entry, normalized)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val currentFilter: StateFlow<FilterMode> = filterMode

    val subtitle: StateFlow<String> = combine(allEntries, entries, filterMode) { all, visible, mode ->
        val todayCount = all.count { isToday(it.timestamp) }
        val totalLabel = if (all.size == 1) "1 capture" else "${all.size} captures"
        val todayLabel = if (todayCount == 1) "1 today" else "$todayCount today"
        val filteredLabel = if (mode == FilterMode.ALL && visible.size == all.size) "" else " - ${visible.size} shown"
        "$totalLabel - $todayLabel$filteredLabel"
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        "0 captures - 0 today"
    )

    fun setQuery(value: String) {
        query.value = value
    }

    fun setFilter(mode: FilterMode) {
        filterMode.value = mode
    }

    fun deleteEntry(entry: CaptureEntry) {
        viewModelScope.launch {
            FileUtils.deleteFile(entry.screenshotPath)
            FileUtils.deleteFile(entry.thumbnailPath)
            FileUtils.deleteFile(entry.voiceNotePath)
            repo.delete(entry)
        }
    }

    private fun matchesQuery(entry: CaptureEntry, query: String): Boolean {
        if (query.isBlank()) return true
        return entry.textNote?.contains(query, ignoreCase = true) == true
    }

    private fun matchesFilter(entry: CaptureEntry, mode: FilterMode): Boolean {
        return when (mode) {
            FilterMode.ALL -> true
            FilterMode.TODAY -> isToday(entry.timestamp)
            FilterMode.NOTES -> !entry.textNote.isNullOrBlank()
            FilterMode.VOICE -> !entry.voiceNotePath.isNullOrBlank()
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }
}
