package com.essential.spacelite.timeline

import android.Manifest
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import com.essential.spacelite.R
import com.essential.spacelite.data.entity.CaptureEntry
import com.essential.spacelite.databinding.ActivityMainBinding
import com.essential.spacelite.databinding.ItemOriginCaptureCardBinding
import com.essential.spacelite.databinding.ItemVoiceNoteBinding
import com.essential.spacelite.utils.PrefsManager
import com.essential.spacelite.utils.ThemeHelper
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private enum class OriginTab {
        NOTES,
        VOICE,
        SEARCH
    }

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TimelineViewModel by viewModels()
    private var currentTab = OriginTab.NOTES
    private var latestEntries: List<CaptureEntry> = emptyList()
    private var mediaPlayer: MediaPlayer? = null
    private var playingButton: ImageButton? = null
    private var hasAnimatedTabState = false

    private val cardTimeFormat = SimpleDateFormat("H:mm", Locale.US)
    private val nDotTypeface by lazy { ResourcesCompat.getFont(this, R.font.ndot_47_inspired_by_nothing) }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.prepareActivity(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentTab = parseOriginTab(intent.getStringExtra(EXTRA_INITIAL_TAB))

        setupInteractions()
        applyHeadingTypography()
        observeEntries()
        requestAllPermissions()
        if (!isAccessibilityServiceEnabled() && !PrefsManager.isOnboardingDone(this)) {
            openAccessibilitySettings()
            PrefsManager.setOnboardingDone(this, true)
        }
        applyTab(currentTab, focusSearch = currentTab == OriginTab.SEARCH)
    }

    private fun setupInteractions() {
        binding.searchInput.doAfterTextChanged {
            viewModel.setQuery(it?.toString().orEmpty())
        }

        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.navNotes.setOnClickListener {
            applyTab(OriginTab.NOTES)
        }
        binding.navVoice.setOnClickListener {
            applyTab(OriginTab.VOICE)
        }
        binding.navSearch.setOnClickListener {
            applyTab(OriginTab.SEARCH, focusSearch = true)
        }
    }

    private fun observeEntries() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entries.collect { entries ->
                    latestEntries = entries
                    renderCurrentTab()
                }
            }
        }
    }

    private fun applyTab(tab: OriginTab, focusSearch: Boolean = false) {
        val shouldAnimate = hasAnimatedTabState && currentTab != tab
        currentTab = tab
        when (tab) {
            OriginTab.NOTES -> {
                viewModel.setFilter(TimelineViewModel.FilterMode.ALL)
                binding.titleText.text = getString(R.string.main_title)
                setVisible(binding.titleText, true, shouldAnimate)
                setVisible(binding.searchBar, true, shouldAnimate)
                setVisible(binding.notesContent, true, shouldAnimate)
                setVisible(binding.voiceContent, false, shouldAnimate)
            }

            OriginTab.VOICE -> {
                viewModel.setFilter(TimelineViewModel.FilterMode.VOICE)
                setVisible(binding.titleText, false, shouldAnimate)
                setVisible(binding.searchBar, false, shouldAnimate)
                setVisible(binding.notesContent, false, shouldAnimate)
                setVisible(binding.voiceContent, true, shouldAnimate)
            }

            OriginTab.SEARCH -> {
                viewModel.setFilter(TimelineViewModel.FilterMode.ALL)
                binding.titleText.text = getString(R.string.main_title)
                setVisible(binding.titleText, true, shouldAnimate)
                setVisible(binding.searchBar, true, shouldAnimate)
                setVisible(binding.notesContent, true, shouldAnimate)
                setVisible(binding.voiceContent, false, shouldAnimate)
                if (focusSearch) {
                    binding.searchInput.post {
                        binding.searchInput.requestFocus()
                    }
                }
            }
        }

        updateNavState(shouldAnimate)
        hasAnimatedTabState = true
        renderCurrentTab()
    }

    private fun updateNavState(animate: Boolean) {
        val selectedView = when (currentTab) {
            OriginTab.NOTES -> binding.navNotes
            OriginTab.VOICE -> binding.navVoice
            OriginTab.SEARCH -> binding.navSearch
        }

        listOf(binding.navNotes, binding.navVoice, binding.navSearch).forEach { nav ->
            nav.setBackgroundResource(
                if (nav === selectedView) R.drawable.bg_origin_nav_active else android.R.color.transparent
            )
            val iconView = (nav as LinearLayout).getChildAt(0) as ImageView
            iconView.alpha = if (nav === selectedView) 1f else 0.86f
            val targetScale = if (nav === selectedView) 1.03f else 1f
            if (animate) {
                nav.animate()
                    .scaleX(targetScale)
                    .scaleY(targetScale)
                    .setDuration(280L)
                    .setInterpolator(OvershootInterpolator(0.85f))
                    .start()
                iconView.animate()
                    .alpha(if (nav === selectedView) 1f else 0.78f)
                    .scaleX(if (nav === selectedView) 1.02f else 0.98f)
                    .scaleY(if (nav === selectedView) 1.02f else 0.98f)
                    .setDuration(220L)
                    .start()
            } else {
                nav.scaleX = targetScale
                nav.scaleY = targetScale
                iconView.scaleX = if (nav === selectedView) 1.02f else 0.98f
                iconView.scaleY = if (nav === selectedView) 1.02f else 0.98f
            }
        }
    }

    private fun renderCurrentTab() {
        when (currentTab) {
            OriginTab.NOTES,
            OriginTab.SEARCH -> renderNoteSections(latestEntries)

            OriginTab.VOICE -> renderVoiceSections(latestEntries.filter { !it.voiceNotePath.isNullOrBlank() })
        }
    }

    private fun renderNoteSections(entries: List<CaptureEntry>) {
        val isSearching = binding.searchInput.text?.isNotBlank() == true
        val recentEntries = if (isSearching) entries.take(4) else entries.take(2)
        val todayEssentials = if (isSearching) emptyList() else entries.drop(2).take(2)

        binding.recentLabel.visibility = if (recentEntries.isEmpty()) View.GONE else View.VISIBLE
        binding.recentRows.visibility = if (recentEntries.isEmpty()) View.GONE else View.VISIBLE
        binding.todayLabel.visibility = if (todayEssentials.isEmpty()) View.GONE else View.VISIBLE
        binding.todayRows.visibility = if (todayEssentials.isEmpty()) View.GONE else View.VISIBLE

        populateCaptureRows(binding.recentRows, recentEntries)
        populateCaptureRows(binding.todayRows, todayEssentials)

        val showEmpty = entries.isEmpty()
        binding.emptyState.visibility = if (showEmpty) View.VISIBLE else View.GONE
        binding.emptyStateTitle.text = getString(
            if (isSearching) R.string.empty_state_filtered_title else R.string.empty_state_title
        )
        binding.emptyStateSubtitle.text = getString(
            if (isSearching) R.string.empty_state_filtered_subtitle else R.string.empty_state_subtitle
        )
    }

    private fun populateCaptureRows(container: LinearLayout, entries: List<CaptureEntry>) {
        container.removeAllViews()
        if (entries.isEmpty()) return

        val columns = if (resources.configuration.screenWidthDp <= 360) 1 else 2
        entries.chunked(columns).forEachIndexed { rowIndex, rowEntries ->
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.TOP
            }

            rowEntries.forEachIndexed { index, entry ->
                val cardBinding = ItemOriginCaptureCardBinding.inflate(LayoutInflater.from(this), row, false)
                val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                if (index == 0) {
                    params.marginEnd = if (rowEntries.size > 1) px(8) else 0
                } else {
                    params.marginStart = px(8)
                }
                cardBinding.root.layoutParams = params
                bindCaptureCard(cardBinding, entry, rowIndex * columns + index)
                row.addView(cardBinding.root)
            }

            if (columns == 2 && rowEntries.size == 1) {
                row.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f).also {
                        it.marginStart = px(8)
                    }
                })
            }

            container.addView(row)
        }
    }

    private fun bindCaptureCard(binding: ItemOriginCaptureCardBinding, entry: CaptureEntry, index: Int) {
        val displayText = entry.textNote?.takeIf { it.isNotBlank() }
            ?: if (!entry.voiceNotePath.isNullOrBlank()) "Voice memo saved for later playback." else "Saved capture ready for your next action."

        binding.cardText.text = displayText
        binding.cardTime.text = cardTimeFormat.format(Date(entry.timestamp))

        val showPreview = File(entry.thumbnailPath).exists() && (index % 2 == 0 || entry.textNote.isNullOrBlank())
        binding.cardPreview.visibility = if (showPreview) View.VISIBLE else View.GONE
        if (showPreview) {
            Glide.with(binding.root)
                .load(File(entry.thumbnailPath))
                .centerCrop()
                .into(binding.cardPreview)
        } else {
            binding.cardPreview.setImageDrawable(null)
        }

        binding.cardVoice.visibility = if (!entry.voiceNotePath.isNullOrBlank() && !showPreview) View.VISIBLE else View.GONE
        binding.cardTimeGroup.alpha = if (!entry.voiceNotePath.isNullOrBlank()) 0.96f else 0.72f

        binding.root.setOnClickListener { openEntry(entry) }
        binding.root.setOnLongClickListener {
            showDeleteDialog(entry)
            true
        }
    }

    private fun renderVoiceSections(entries: List<CaptureEntry>) {
        val today = entries.filter { isToday(it.timestamp) }
        val yesterday = entries.filter { isYesterday(it.timestamp) }
        val older = entries.filterNot { isToday(it.timestamp) || isYesterday(it.timestamp) }

        populateVoiceRows(binding.voiceTodayRows, if (today.isNotEmpty()) today else older.take(2))
        populateVoiceRows(binding.voiceYesterdayRows, if (yesterday.isNotEmpty()) yesterday else older.drop(2))
        binding.voiceYesterdayLabel.visibility =
            if (binding.voiceYesterdayRows.childCount > 0) View.VISIBLE else View.GONE
    }

    private fun populateVoiceRows(container: LinearLayout, entries: List<CaptureEntry>) {
        container.removeAllViews()
        entries.forEachIndexed { index, entry ->
            val rowBinding = ItemVoiceNoteBinding.inflate(LayoutInflater.from(this), container, false)
            bindVoiceRow(rowBinding, entry, index)
            container.addView(rowBinding.root)
        }
    }

    private fun bindVoiceRow(binding: ItemVoiceNoteBinding, entry: CaptureEntry, index: Int) {
        binding.markerContainer.removeAllViews()
        val markerSeeds = markerSeeds(entry, index)
        markerSeeds.forEachIndexed { markerIndex, weight ->
            binding.markerContainer.addView(View(this).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, weight)
            })
            binding.markerContainer.addView(View(this).apply {
                background = getDrawable(R.drawable.bg_wave_marker)
                layoutParams = LinearLayout.LayoutParams(px(8), px(8)).apply {
                    gravity = Gravity.CENTER_VERTICAL
                }
            })
            if (markerIndex == markerSeeds.lastIndex) {
                binding.markerContainer.addView(View(this).apply {
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
                })
            }
        }

        binding.root.setOnClickListener { openEntry(entry) }
        binding.btnPlay.setOnClickListener { toggleVoicePlayback(entry, binding.btnPlay) }
    }

    private fun markerSeeds(entry: CaptureEntry, index: Int): List<Float> {
        val base = ((entry.timestamp / 1000L) % 7).toInt()
        return listOf(
            0.22f + (base * 0.02f),
            0.14f + ((index % 3) * 0.04f),
            0.36f,
            0.18f,
            0.12f + (base % 2) * 0.03f
        )
    }

    private fun toggleVoicePlayback(entry: CaptureEntry, button: ImageButton) {
        val path = entry.voiceNotePath ?: return openEntry(entry)
        if (!File(path).exists()) return openEntry(entry)

        if (playingButton === button) {
            stopPlayback()
            return
        }

        stopPlayback()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(path)
                prepare()
                setOnCompletionListener { stopPlayback() }
                start()
            }
            playingButton = button
            button.setImageResource(R.drawable.ic_stop)
        } catch (_: Exception) {
            stopPlayback()
            openEntry(entry)
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.release()
        mediaPlayer = null
        playingButton?.setImageResource(R.drawable.ic_play)
        playingButton = null
    }

    private fun openEntry(entry: CaptureEntry) {
        startActivity(Intent(this, DetailActivity::class.java).apply {
            putExtra(DetailActivity.EXTRA_ENTRY_ID, entry.id)
        })
    }

    private fun showDeleteDialog(entry: CaptureEntry) {
        com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
            .setTitle("Delete capture?")
            .setMessage("This removes the screenshot, note, and voice memo from Origin Space.")
            .setPositiveButton("Delete") { _, _ ->
                viewModel.deleteEntry(entry)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun requestAllPermissions() {
        val perms = mutableListOf(Manifest.permission.RECORD_AUDIO)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            perms.add(Manifest.permission.POST_NOTIFICATIONS)
            perms.add(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            @Suppress("DEPRECATION")
            perms.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionsLauncher.launch(perms.toTypedArray())
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val service = "$packageName/${com.essential.spacelite.accessibility.VolumeAccessibilityService::class.java.canonicalName}"
        val enabledServices = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains(service)
    }

    private fun openAccessibilitySettings() {
        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    }

    private fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun setVisible(view: View, visible: Boolean, animate: Boolean) {
        if (!animate) {
            view.clearAnimation()
            view.animate().cancel()
            view.visibility = if (visible) View.VISIBLE else View.GONE
            view.alpha = 1f
            view.scaleX = 1f
            view.scaleY = 1f
            view.translationY = 0f
            return
        }

        if (visible) {
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.scaleX = 0.985f
            view.scaleY = 0.985f
            view.translationY = px(6).toFloat()
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setInterpolator(OvershootInterpolator(0.65f))
                .setDuration(260L)
                .start()
        } else if (view.visibility == View.VISIBLE) {
            view.animate()
                .alpha(0f)
                .scaleX(0.985f)
                .scaleY(0.985f)
                .translationY(px(4).toFloat())
                .setDuration(170L)
                .withEndAction {
                    view.visibility = View.GONE
                    view.alpha = 1f
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.translationY = 0f
                }
                .start()
        } else {
            view.visibility = View.GONE
        }
    }

    private fun applyHeadingTypography() {
        val useNdot = PrefsManager.useNdotHeadings(this)
        val headingViews = listOf(
            binding.recentLabel,
            binding.todayLabel,
            binding.voiceTodayLabel,
            binding.voiceYesterdayLabel
        )

        headingViews.forEach { textView ->
            textView.typeface = if (useNdot) nDotTypeface else Typeface.MONOSPACE
            textView.letterSpacing = if (useNdot) 0.08f else 0.16f
            textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (useNdot) 11f else 12f)
        }
    }

    private fun parseOriginTab(value: String?): OriginTab {
        return when (value?.lowercase(Locale.US)) {
            "voice" -> OriginTab.VOICE
            "search" -> OriginTab.SEARCH
            else -> OriginTab.NOTES
        }
    }

    private fun isToday(timestamp: Long): Boolean {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(timestamp: Long): Boolean {
        val now = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    override fun onResume() {
        super.onResume()
        applyHeadingTypography()
        applyTab(currentTab)
    }

    override fun onPause() {
        super.onPause()
        stopPlayback()
    }

    companion object {
        const val EXTRA_INITIAL_TAB = "extra_initial_tab"

        fun createIntent(context: android.content.Context, tab: String): Intent {
            return Intent(context, MainActivity::class.java).putExtra(EXTRA_INITIAL_TAB, tab)
        }
    }
}
