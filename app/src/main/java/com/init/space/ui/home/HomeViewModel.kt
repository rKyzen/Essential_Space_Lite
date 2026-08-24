package com.init.space.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.init.space.data.AppDatabase
import com.init.space.data.CaptureRepository
import com.init.space.data.entity.CaptureEntry
import com.init.space.utils.FileUtils
import com.init.space.utils.ReminderScheduler
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    enum class FilterMode {
        ALL, NOTES, VOICE, STARRED, REMINDERS
    }

    private val repo = CaptureRepository(
        AppDatabase.getDatabase(application)
    )

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _filterMode = MutableStateFlow(FilterMode.ALL)
    val filterMode: StateFlow<FilterMode> = _filterMode.asStateFlow()

    val allEntries: StateFlow<List<CaptureEntry>> = repo.allEntries.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val filteredEntries: StateFlow<List<CaptureEntry>> = combine(allEntries, _query, _filterMode) { entries, rawQuery, mode ->
        val queryText = rawQuery.trim()
        entries.filter { entry ->
            matchesFilter(entry, mode) && matchesQuery(entry, queryText)
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        emptyList()
    )

    val recentEntry: StateFlow<CaptureEntry?> = allEntries.map { entries ->
        entries.sortedWith(
            compareByDescending<CaptureEntry> { it.reminderAt != null }
                .thenByDescending { it.isFavorite }
                .thenByDescending { it.timestamp }
        ).firstOrNull()
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        null
    )

    val totalCount: StateFlow<Int> = allEntries.map { it.size }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val reminderCount: StateFlow<Int> = allEntries.map { list -> list.count { it.reminderAt != null } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val starredCount: StateFlow<Int> = allEntries.map { list -> list.count { it.isFavorite } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val voiceCount: StateFlow<Int> = allEntries.map { list -> list.count { !it.voiceNotePath.isNullOrBlank() } }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setQuery(value: String) {
        _query.value = value
    }

    fun setFilter(mode: FilterMode) {
        _filterMode.value = mode
    }

    fun toggleFavorite(entry: CaptureEntry) {
        viewModelScope.launch {
            val updated = entry.copy(isFavorite = !entry.isFavorite)
            repo.update(updated)
        }
    }

    fun deleteEntry(entry: CaptureEntry) {
        viewModelScope.launch {
            FileUtils.deleteFile(entry.screenshotPath)
            FileUtils.deleteFile(entry.thumbnailPath)
            FileUtils.deleteFile(entry.voiceNotePath)
            ReminderScheduler.cancel(getApplication(), entry.id)
            repo.delete(entry)
        }
    }

    private fun matchesQuery(entry: CaptureEntry, query: String): Boolean {
        if (query.isBlank()) return true
        return listOfNotNull(
            entry.textNote,
            entry.aiSummary,
            entry.appName
        ).any { it.contains(query, ignoreCase = true) } ||
            (!entry.voiceNotePath.isNullOrBlank() && "voice".contains(query, ignoreCase = true)) ||
            (entry.reminderAt != null && "reminder".contains(query, ignoreCase = true)) ||
            (entry.isFavorite && listOf("star", "starred", "favorite", "fav").any { it.contains(query, ignoreCase = true) })
    }

    private fun matchesFilter(entry: CaptureEntry, mode: FilterMode): Boolean {
        return when (mode) {
            FilterMode.ALL -> true
            FilterMode.NOTES -> !entry.textNote.isNullOrBlank()
            FilterMode.VOICE -> !entry.voiceNotePath.isNullOrBlank()
            FilterMode.STARRED -> entry.isFavorite
            FilterMode.REMINDERS -> entry.reminderAt != null
        }
    }
}
