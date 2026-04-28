package com.essential.spacelite.timeline

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.essential.spacelite.R
import com.essential.spacelite.data.AppDatabase
import com.essential.spacelite.data.entity.CaptureEntry
import com.essential.spacelite.databinding.ActivityDetailBinding
import com.essential.spacelite.utils.FileUtils
import com.essential.spacelite.utils.ThemeHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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

        val hasVoice = e.voiceNotePath != null && File(e.voiceNotePath).exists()
        binding.voiceSection.visibility = if (hasVoice) android.view.View.VISIBLE else android.view.View.GONE
        if (hasVoice) {
            binding.voiceDuration.text = FileUtils.formatDuration(e.voiceNoteDurationMs)
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }
        binding.btnSaveNote.setOnClickListener { saveTextNote() }
        binding.btnShareCapture.setOnClickListener { shareCapture() }
        binding.btnCopyNote.setOnClickListener { copyNote() }
        binding.btnPlayVoice.setOnClickListener { togglePlayback() }
    }

    private fun saveTextNote() {
        val e = entry ?: return
        val updated = e.copy(textNote = binding.editTextNote.text.toString().trim())
        lifecycleScope.launch {
            AppDatabase.getDatabase(applicationContext).captureEntryDao().update(updated)
            entry = updated
            Toast.makeText(this@DetailActivity, "Note saved", Toast.LENGTH_SHORT).show()
        }
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
        startActivity(Intent.createChooser(shareIntent, "Share capture"))
    }

    private fun copyNote() {
        val note = binding.editTextNote.text?.toString()?.trim().orEmpty()
        if (note.isEmpty()) {
            Toast.makeText(this, "No note to copy", Toast.LENGTH_SHORT).show()
            return
        }

        val clipboard = getSystemService(ClipboardManager::class.java)
        clipboard.setPrimaryClip(ClipData.newPlainText("Essential Space note", note))
        Toast.makeText(this, "Note copied", Toast.LENGTH_SHORT).show()
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
