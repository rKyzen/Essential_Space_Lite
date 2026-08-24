package com.init.space.ui.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.init.space.data.AppDatabase
import com.init.space.data.CaptureRepository
import com.init.space.data.entity.CaptureEntry
import com.init.space.utils.FileUtils
import com.init.space.utils.OpenRouterSummaryClient
import com.init.space.utils.ReminderScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repo = CaptureRepository(
        AppDatabase.getDatabase(application)
    )

    private val _entry = MutableStateFlow<CaptureEntry?>(null)
    val entry: StateFlow<CaptureEntry?> = _entry.asStateFlow()

    private val _isGeneratingSummary = MutableStateFlow(false)
    val isGeneratingSummary: StateFlow<Boolean> = _isGeneratingSummary.asStateFlow()

    private val _summaryStatus = MutableStateFlow<String?>(null)
    val summaryStatus: StateFlow<String?> = _summaryStatus.asStateFlow()

    fun loadEntry(id: Long) {
        viewModelScope.launch {
            val loaded = repo.getEntryById(id)
            _entry.value = loaded
        }
    }

    fun saveTextNote(newText: String) {
        val current = _entry.value ?: return
        val updated = current.copy(textNote = newText.trim())
        viewModelScope.launch {
            repo.update(updated)
            _entry.value = updated
        }
    }

    fun toggleFavorite() {
        val current = _entry.value ?: return
        val updated = current.copy(isFavorite = !current.isFavorite)
        viewModelScope.launch {
            repo.update(updated)
            _entry.value = updated
        }
    }

    fun setReminder(reminderAt: Long) {
        val current = _entry.value ?: return
        val updated = current.copy(reminderAt = reminderAt)
        viewModelScope.launch {
            repo.update(updated)
            ReminderScheduler.schedule(getApplication(), updated)
            _entry.value = updated
        }
    }

    fun clearReminder() {
        val current = _entry.value ?: return
        val updated = current.copy(reminderAt = null)
        viewModelScope.launch {
            repo.update(updated)
            ReminderScheduler.cancel(getApplication(), current.id)
            _entry.value = updated
        }
    }

    fun generateAiSummary() {
        val current = _entry.value ?: return
        if (_isGeneratingSummary.value) return

        if (!OpenRouterSummaryClient.isConfigured()) {
            _summaryStatus.value = "openrouter api key is not configured"
            return
        }

        _isGeneratingSummary.value = true
        _summaryStatus.value = "processing..."

        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                OpenRouterSummaryClient.generateSummary(
                    screenshotPath = current.screenshotPath,
                    note = current.textNote,
                    reminderAt = current.reminderAt
                )
            }

            _isGeneratingSummary.value = false
            result.fold(
                onSuccess = { summary ->
                    val updated = current.copy(aiSummary = summary)
                    repo.update(updated)
                    _entry.value = updated
                    _summaryStatus.value = null
                },
                onFailure = { error ->
                    _summaryStatus.value = error.message ?: "unable to generate summary"
                }
            )
        }
    }

    fun deleteCapture(onComplete: () -> Unit) {
        val current = _entry.value ?: return
        viewModelScope.launch {
            FileUtils.deleteFile(current.screenshotPath)
            FileUtils.deleteFile(current.thumbnailPath)
            FileUtils.deleteFile(current.voiceNotePath)
            ReminderScheduler.cancel(getApplication(), current.id)
            repo.delete(current)
            onComplete()
        }
    }
}
