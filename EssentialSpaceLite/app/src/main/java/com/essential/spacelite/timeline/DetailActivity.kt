package com.essential.spacelite.timeline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.provider.CalendarContract
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.essential.spacelite.R
import com.essential.spacelite.data.AppDatabase
import com.essential.spacelite.data.entity.CaptureEntry
import com.essential.spacelite.databinding.ActivityDetailBinding
import com.essential.spacelite.utils.FileUtils
import com.essential.spacelite.utils.GlassUi
import com.essential.spacelite.utils.ReminderScheduler
import com.essential.spacelite.utils.ReminderUtils
import com.essential.spacelite.utils.ThemeHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailBinding
    private var entry: CaptureEntry? = null
    private var mediaPlayer: MediaPlayer? = null
    private var isPlaying = false

    private val dateTimeFormat = SimpleDateFormat("EEEE, MMMM d - h:mm a", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.prepareActivity(this)
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        applyGlassSystem()

        val entryId = intent.getLongExtra(EXTRA_ENTRY_ID, -1L)
        if (entryId == -1L) {
            finish()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(applicationContext)
            entry = db.captureEntryDao().getEntryById(entryId)
            entry?.let { bindEntry(it) } ?: finish()
        }

        setupActions()
    }

    private fun bindEntry(e: CaptureEntry) {
        binding.screenshotFull.maxHeight = (resources.displayMetrics.heightPixels * 0.42f).toInt()
        Glide.with(this).load(File(e.screenshotPath)).into(binding.screenshotFull)
        binding.entryDateTime.text = dateTimeFormat.format(Date(e.timestamp))
        binding.editTextNote.setText(e.textNote ?: "")
        binding.noteCount.text = getString(R.string.detail_note_count, binding.editTextNote.text?.length ?: 0)

        val hasVoice = e.voiceNotePath != null && File(e.voiceNotePath).exists()
        binding.voiceSection.visibility = if (hasVoice) android.view.View.VISIBLE else android.view.View.GONE
        binding.chipVoice.visibility = if (hasVoice) android.view.View.VISIBLE else android.view.View.GONE
        if (hasVoice) {
            binding.voiceDuration.text = FileUtils.formatDuration(e.voiceNoteDurationMs)
        }

        renderReminder(e)
        GlassUi.animateEntrance(binding.metaGroup, 30L, 12f)
        GlassUi.animateEntrance(binding.notesSection, 80L, 12f)
        if (hasVoice) {
            GlassUi.animateEntrance(binding.voiceSection, 120L, 12f)
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveNote.setOnClickListener { saveTextNote() }
        binding.btnShareCapture.setOnClickListener { shareCapture() }
        binding.btnCopyNote.setOnClickListener { copyNote() }
        binding.btnPlayVoice.setOnClickListener { togglePlayback() }
        binding.btnSetReminder.setOnClickListener { pickReminderDateTime() }
        binding.btnClearReminder.setOnClickListener { clearReminder() }
        binding.btnAddToCalendar.setOnClickListener { addReminderToCalendar() }
        binding.editTextNote.doAfterTextChanged {
            binding.noteCount.text = getString(R.string.detail_note_count, it?.length ?: 0)
        }
    }

    private fun saveTextNote() {
        val e = entry ?: return
        val updated = e.copy(textNote = binding.editTextNote.text.toString().trim())
        lifecycleScope.launch {
            AppDatabase.getDatabase(applicationContext).captureEntryDao().update(updated)
            entry = updated
            renderReminder(updated)
            Toast.makeText(this@DetailActivity, "Note saved", Toast.LENGTH_SHORT).show()
        }
    }

    private fun renderReminder(entry: CaptureEntry) {
        binding.reminderValue.text = ReminderUtils.formatDetailReminder(this, entry.reminderAt)
        val hasReminder = entry.reminderAt != null
        binding.btnClearReminder.visibility = if (hasReminder) android.view.View.VISIBLE else android.view.View.GONE
        binding.btnAddToCalendar.visibility = if (hasReminder) android.view.View.VISIBLE else android.view.View.GONE
        binding.chipReminder.visibility = if (hasReminder) android.view.View.VISIBLE else android.view.View.GONE
    }

    private fun pickReminderDateTime() {
        val now = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        val reminder = Calendar.getInstance().apply {
                            set(Calendar.YEAR, year)
                            set(Calendar.MONTH, month)
                            set(Calendar.DAY_OF_MONTH, dayOfMonth)
                            set(Calendar.HOUR_OF_DAY, hourOfDay)
                            set(Calendar.MINUTE, minute)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }.timeInMillis

                        if (reminder <= System.currentTimeMillis()) {
                            Toast.makeText(this, "Choose a future time", Toast.LENGTH_SHORT).show()
                        } else {
                            saveReminder(reminder)
                        }
                    },
                    now.get(Calendar.HOUR_OF_DAY),
                    now.get(Calendar.MINUTE),
                    false
                ).show()
            },
            now.get(Calendar.YEAR),
            now.get(Calendar.MONTH),
            now.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveReminder(reminderAt: Long) {
        val current = entry ?: return
        val updated = current.copy(reminderAt = reminderAt)
        lifecycleScope.launch {
            AppDatabase.getDatabase(applicationContext).captureEntryDao().update(updated)
            ReminderScheduler.schedule(applicationContext, updated)
            entry = updated
            renderReminder(updated)
            Toast.makeText(this@DetailActivity, "Reminder set", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clearReminder() {
        val current = entry ?: return
        val updated = current.copy(reminderAt = null)
        lifecycleScope.launch {
            AppDatabase.getDatabase(applicationContext).captureEntryDao().update(updated)
            ReminderScheduler.cancel(applicationContext, current.id)
            entry = updated
            renderReminder(updated)
            Toast.makeText(this@DetailActivity, "Reminder cleared", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addReminderToCalendar() {
        val current = entry ?: return
        val reminderAt = current.reminderAt ?: return
        val title = current.textNote?.takeIf { it.isNotBlank() } ?: "Origin Space reminder"
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.Events.DESCRIPTION, "Reminder from Origin Space")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, reminderAt)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, reminderAt + 30 * 60 * 1000L)
        }
        startActivity(intent)
    }

    private fun togglePlayback() {
        val e = entry ?: return
        val path = e.voiceNotePath ?: return

        if (isPlaying) {
            mediaPlayer?.pause()
            isPlaying = false
            binding.btnPlayVoice.setImageResource(R.drawable.ic_play)
            return
        }

        try {
            if (mediaPlayer == null) {
                val mp = MediaPlayer()
                mp.setDataSource(path)
                mp.prepare()
                mp.setOnCompletionListener {
                    isPlaying = false
                    binding.btnPlayVoice.setImageResource(R.drawable.ic_play)
                }
                mediaPlayer = mp
            }
            mediaPlayer?.start()
            isPlaying = true
            binding.btnPlayVoice.setImageResource(R.drawable.ic_stop)
        } catch (_: Exception) {
            Toast.makeText(this, "Could not play recording", Toast.LENGTH_SHORT).show()
        }
    }

    private fun shareCapture() {
        val e = entry ?: return
        val file = File(e.screenshotPath)
        if (!file.exists()) {
            Toast.makeText(this, "Screenshot file is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val uri: Uri = FileProvider.getUriForFile(this, "$packageName.fileprovider", file)
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, e.textNote.orEmpty())
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        startActivity(Intent.createChooser(shareIntent, "Share origin"))
    }

    private fun copyNote() {
        val note = binding.editTextNote.text?.toString()?.trim().orEmpty()
        if (note.isEmpty()) {
            Toast.makeText(this, "No note to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("Origin Space note", note))
        Toast.makeText(this, "Note copied", Toast.LENGTH_SHORT).show()
    }

    private fun applyGlassSystem() {
        GlassUi.applyBlur(binding.backdropBlobTop, 54f)
        GlassUi.applyDepth(binding.toolbar, 18f)
        GlassUi.applyDepth(binding.metaGroup, 14f)
        GlassUi.applyDepth(binding.notesSection, 14f)
        GlassUi.applyDepth(binding.voiceSection, 14f)
        GlassUi.applyDepth(binding.voicePreviewCard, 10f)

        listOf(
            binding.btnBack,
            binding.btnSaveNote,
            binding.btnShareCapture,
            binding.btnCopyNote,
            binding.btnSetReminder,
            binding.btnClearReminder,
            binding.btnAddToCalendar,
            binding.btnPlayVoice
        ).forEach { view ->
            GlassUi.attachLiquidPress(view)
        }

        GlassUi.animateEntrance(binding.toolbar, 10L, 10f)
    }

    override fun onPause() {
        super.onPause()
        mediaPlayer?.pause()
        isPlaying = false
        binding.btnPlayVoice.setImageResource(R.drawable.ic_play)
    }

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_ENTRY_ID = "extra_entry_id"
    }
}
