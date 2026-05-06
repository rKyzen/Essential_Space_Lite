package com.essential.spacelite.timeline

import android.Manifest
import android.content.Intent
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.GestureDetector
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
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
import com.essential.spacelite.utils.FileUtils
import com.essential.spacelite.utils.GlassUi
import com.essential.spacelite.utils.PrefsManager
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
    private val swipeThresholdPx by lazy { px(72) }
    private val swipeVelocityThresholdPx by lazy { px(72) }

    private val cardTimeFormat = SimpleDateFormat("H:mm", Locale.US)
    private val nDotTypeface by lazy { ResourcesCompat.getFont(this, R.font.ndot_47_inspired_by_nothing) }
    private val tabInterpolator = PathInterpolator(0.2f, 0f, 0f, 1f)
    private val swipeDetector by lazy {
        GestureDetector(
            this,
            object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = false

                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    val start = e1 ?: return false
                    val deltaX = e2.x - start.x
                    val deltaY = e2.y - start.y
                    if (kotlin.math.abs(deltaX) < swipeThresholdPx ||
                        kotlin.math.abs(velocityX) < swipeVelocityThresholdPx ||
                        kotlin.math.abs(deltaX) <= kotlin.math.abs(deltaY)
                    ) {
                        return false
                    }

                    if (deltaX < 0) {
                        swipeToNextTab()
                    } else {
                        swipeToPreviousTab()
                    }
                    return true
                }
            }
        )
    }

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.prepareActivity(this)
        super.onCreate(savedInstanceState)
        if (!PrefsManager.isDisclosureAccepted(this)) {
            startActivity(Intent(this, AccessibilityDisclosureActivity::class.java))
            finish()
            return
        }
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        currentTab = parseOriginTab(intent.getStringExtra(EXTRA_INITIAL_TAB))

        applyGlassSystem()
        setupInteractions()
        applyHeadingTypography()
        observeEntries()
        requestAllPermissions()
        applyTab(currentTab, focusSearch = currentTab == OriginTab.SEARCH)
        animateScreenEntrance()
    }

    private fun setupInteractions() {
        binding.searchInput.doAfterTextChanged {
            viewModel.setQuery(it?.toString().orEmpty())
        }

        binding.btnSettingsBottom.setOnClickListener {
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
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subtitle.collect { subtitle ->
                    binding.subtitleText.text = subtitle
                }
            }
        }
    }

    private fun applyTab(tab: OriginTab, focusSearch: Boolean = false) {
        val shouldAnimate = hasAnimatedTabState && currentTab != tab
        val previousTab = currentTab
        currentTab = tab
        when (tab) {
            OriginTab.NOTES -> {
                viewModel.setFilter(TimelineViewModel.FilterMode.ALL)
                binding.titleText.text = getString(R.string.main_title)
                setVisible(binding.titleText, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.subtitleText, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.searchBar, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.statsRow, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.notesContent, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.voiceContent, false, shouldAnimate, tabDirection(previousTab, tab))
            }

            OriginTab.VOICE -> {
                viewModel.setFilter(TimelineViewModel.FilterMode.VOICE)
                setVisible(binding.titleText, false, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.subtitleText, false, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.searchBar, false, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.statsRow, false, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.notesContent, false, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.voiceContent, true, shouldAnimate, tabDirection(previousTab, tab))
            }

            OriginTab.SEARCH -> {
                viewModel.setFilter(TimelineViewModel.FilterMode.ALL)
                binding.titleText.text = getString(R.string.main_title)
                setVisible(binding.titleText, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.subtitleText, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.searchBar, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.statsRow, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.notesContent, true, shouldAnimate, tabDirection(previousTab, tab))
                setVisible(binding.voiceContent, false, shouldAnimate, tabDirection(previousTab, tab))
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

        // #region agent log
        Log.d(
            "NavBarDebug",
            "H_UI1 updateNavState tab=$currentTab animate=$animate selected=${selectedView.id}"
        )
        // #endregion

        listOf(binding.navNotes, binding.navVoice, binding.navSearch).forEach { nav ->
            val iconView = (nav as LinearLayout).getChildAt(0) as ImageView
            // Reference navbar: static icons; selection is a subtle alpha emphasis only.
            nav.animate().cancel()
            iconView.animate().cancel()
            nav.scaleX = 1f
            nav.scaleY = 1f
            nav.translationY = 0f
            iconView.scaleX = 1f
            iconView.scaleY = 1f
            iconView.rotation = 0f
            iconView.alpha = if (nav === selectedView) 1f else 0.78f
        }
    }

    private fun renderCurrentTab() {
        updateStats(latestEntries)
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
        GlassUi.applyDepth(binding.root, 12f)
        GlassUi.attachLiquidPress(binding.root)

        val displayText = entry.textNote?.takeIf { it.isNotBlank() }
            ?: if (!entry.voiceNotePath.isNullOrBlank()) "Voice memo saved for later playback." else "Saved capture ready for your next action."

        binding.cardText.text = displayText
        binding.cardTime.text = cardTimeFormat.format(Date(entry.timestamp))
        val reminderText = ReminderUtils.formatOverviewReminder(binding.root.context, entry.reminderAt)
        binding.cardReminder.text = reminderText
        binding.cardReminder.visibility = if (entry.reminderAt != null) View.VISIBLE else View.GONE
        binding.cardKind.text = when {
            !entry.voiceNotePath.isNullOrBlank() && !entry.textNote.isNullOrBlank() -> getString(R.string.card_kind_hybrid)
            !entry.voiceNotePath.isNullOrBlank() -> getString(R.string.card_kind_voice)
            else -> getString(R.string.card_kind_note)
        }

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
        GlassUi.animateEntrance(binding.root, (index * 36L).coerceAtMost(180L), 10f)
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
        GlassUi.applyDepth(binding.root, 10f)
        GlassUi.attachLiquidPress(binding.root)
        GlassUi.attachLiquidPress(binding.btnPlay, 0.94f, 0.9f)
        binding.voiceMeta.text = FileUtils.formatDuration(entry.voiceNoteDurationMs)

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
        GlassUi.animateEntrance(binding.root, (index * 44L).coerceAtMost(160L), 10f)
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

    private fun px(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()

    private fun setVisible(view: View, visible: Boolean, animate: Boolean, direction: Int = 1) {
        if (!animate) {
            view.clearAnimation()
            view.animate().cancel()
            view.visibility = if (visible) View.VISIBLE else View.GONE
            view.alpha = 1f
            view.scaleX = 1f
            view.scaleY = 1f
            view.translationY = 0f
            view.translationX = 0f
            return
        }

        if (visible) {
            view.visibility = View.VISIBLE
            view.alpha = 0f
            view.scaleX = 0.985f
            view.scaleY = 0.985f
            view.translationY = px(6).toFloat()
            view.translationX = (px(14) * direction).toFloat()
            view.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .translationX(0f)
                .setInterpolator(OvershootInterpolator(0.55f))
                .setDuration(360L)
                .start()
        } else if (view.visibility == View.VISIBLE) {
            view.animate()
                .alpha(0f)
                .scaleX(0.99f)
                .scaleY(0.99f)
                .translationY(px(2).toFloat())
                .translationX((-px(10) * direction).toFloat())
                .setDuration(220L)
                .setInterpolator(tabInterpolator)
                .withEndAction {
                    view.visibility = View.GONE
                    view.alpha = 1f
                    view.scaleX = 1f
                    view.scaleY = 1f
                    view.translationY = 0f
                    view.translationX = 0f
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

        binding.titleText.typeface = nDotTypeface
        binding.titleText.letterSpacing = 0.02f
        binding.voiceTitleText.typeface = nDotTypeface
        binding.voiceTitleText.letterSpacing = 0.02f
    }

    private fun applyGlassSystem() {
        GlassUi.applyBlur(binding.backdropBlobTop, 54f)
        GlassUi.applyBlur(binding.backdropBlobBottom, 68f)

        listOf(
            binding.searchBar,
            binding.statsRow,
            binding.bottomNav,
            binding.btnSettingsBottom
        ).forEach { view ->
            GlassUi.applyDepth(view, 16f)
            GlassUi.attachLiquidPress(view)
        }

        listOf(binding.statCaptures, binding.statVoice, binding.statReminders).forEach { view ->
            GlassUi.applyDepth(view, 8f)
            GlassUi.attachLiquidPress(view, 0.97f, 0.96f)
        }

        listOf(binding.navNotes, binding.navVoice, binding.navSearch).forEach { nav ->
            GlassUi.attachLiquidPress(nav, 0.95f, 0.92f)
        }
    }

    private fun animateScreenEntrance() {
        GlassUi.animateEntrance(binding.titleText, delayMs = 10L, offsetDp = 18f)
        GlassUi.animateEntrance(binding.subtitleText, delayMs = 38L, offsetDp = 12f)
        GlassUi.animateEntrance(binding.searchBar, delayMs = 70L, offsetDp = 14f)
        GlassUi.animateEntrance(binding.statsRow, delayMs = 110L, offsetDp = 12f)
        GlassUi.animateEntrance(binding.bottomNav, delayMs = 180L, offsetDp = 16f)
        GlassUi.animateEntrance(binding.btnSettingsBottom, delayMs = 220L, offsetDp = 18f)
    }

    private fun updateStats(entries: List<CaptureEntry>) {
        binding.statCapturesValue.text = entries.size.toString()
        binding.statVoiceValue.text = entries.count { !it.voiceNotePath.isNullOrBlank() }.toString()
        binding.statRemindersValue.text = entries.count { it.reminderAt != null }.toString()
    }

    private fun tabDirection(from: OriginTab, to: OriginTab): Int {
        val fromIndex = when (from) {
            OriginTab.NOTES -> 0
            OriginTab.VOICE -> 1
            OriginTab.SEARCH -> 2
        }
        val toIndex = when (to) {
            OriginTab.NOTES -> 0
            OriginTab.VOICE -> 1
            OriginTab.SEARCH -> 2
        }
        return if (toIndex >= fromIndex) 1 else -1
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

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        swipeDetector.onTouchEvent(ev)
        return super.dispatchTouchEvent(ev)
    }

    private fun swipeToNextTab() {
        when (currentTab) {
            OriginTab.NOTES -> applyTab(OriginTab.VOICE)
            OriginTab.VOICE -> applyTab(OriginTab.SEARCH)
            OriginTab.SEARCH -> Unit
        }
    }

    private fun swipeToPreviousTab() {
        when (currentTab) {
            OriginTab.NOTES -> Unit
            OriginTab.VOICE -> applyTab(OriginTab.NOTES)
            OriginTab.SEARCH -> applyTab(OriginTab.VOICE)
        }
    }

    companion object {
        const val EXTRA_INITIAL_TAB = "extra_initial_tab"

        fun createIntent(context: android.content.Context, tab: String): Intent {
            return Intent(context, MainActivity::class.java).putExtra(EXTRA_INITIAL_TAB, tab)
        }
    }
}
