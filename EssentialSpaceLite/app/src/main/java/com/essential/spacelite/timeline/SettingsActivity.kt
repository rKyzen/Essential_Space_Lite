package com.essential.spacelite.timeline

import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.TypedValue
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.essential.spacelite.R
import com.essential.spacelite.data.AppDatabase
import com.essential.spacelite.databinding.ActivitySettingsBinding
import com.essential.spacelite.utils.FileUtils
import com.essential.spacelite.utils.GlassUi
import com.essential.spacelite.utils.PrefsManager
import com.essential.spacelite.utils.ReminderScheduler
import com.essential.spacelite.utils.ThemeHelper
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private val nDotTypeface by lazy { ResourcesCompat.getFont(this, R.font.ndot_47_inspired_by_nothing) }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.prepareActivity(this)
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setupHeading()
        setupRows()
        setupNav()
        setupInfoCard()
        applyGlassSystem()
        applyThemeVisuals()
        ThemeHelper.applyAmbientMode(
            PrefsManager.getThemeOption(this),
            binding.backdropBlobTop,
            binding.backdropBlobBottom
        )
    }

    private fun setupHeading() {
        binding.switchNdot.isChecked = PrefsManager.useNdotHeadings(this)
        binding.switchAiSummary.isChecked = PrefsManager.isAiSummaryEnabled(this)
        binding.themeValue.text = ThemeHelper.displayLabel(PrefsManager.getThemeOption(this))
        updateHeading()

        binding.switchNdot.setOnCheckedChangeListener { _, isChecked ->
            PrefsManager.setUseNdotHeadings(this, isChecked)
            updateHeading()
        }
        binding.switchAiSummary.setOnCheckedChangeListener { _, isChecked ->
            PrefsManager.setAiSummaryEnabled(this, isChecked)
        }
    }

    private fun updateHeading() {
        val useNdot = PrefsManager.useNdotHeadings(this)
        binding.settingsHeading.text = getString(R.string.settings_title)
        binding.settingsHeading.typeface = if (useNdot) nDotTypeface else Typeface.DEFAULT
        binding.settingsHeading.letterSpacing = if (useNdot) 0.08f else 0.04f
        binding.settingsHeading.setTextSize(TypedValue.COMPLEX_UNIT_SP, if (useNdot) 24f else 26f)
    }

    private fun setupRows() {
        binding.rowNdot.setOnClickListener {
            binding.switchNdot.toggle()
        }
        binding.rowAiSummary.setOnClickListener {
            binding.switchAiSummary.toggle()
        }
        binding.rowTheme.setOnClickListener {
            showThemePicker()
        }
        binding.rowKeyBinds.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_origin_key_binds)
                .setMessage("Origin Space currently captures with Volume Up + Volume Down. You can extend this later with a dedicated Origin Key action.")
                .setPositiveButton("Open Accessibility") { _, _ ->
                    startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                }
                .setNegativeButton("Close", null)
                .show()
        }
        binding.rowAbout.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.credits_title)
                .setMessage(R.string.credits_message)
                .setPositiveButton("OK", null)
                .show()
        }
        binding.rowPrivacy.setOnClickListener {
            openPrivacyPolicy()
        }
        binding.rowTerms.setOnClickListener {
            MaterialAlertDialogBuilder(this)
                .setTitle(R.string.settings_terms)
                .setMessage(R.string.terms_message)
                .setPositiveButton("OK", null)
                .show()
        }
        binding.rowDeleteAll.setOnClickListener {
            confirmDeleteAll()
        }
    }

    private fun openPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://esl-pap.netlify.app/"))
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, R.string.privacy_policy_error, Toast.LENGTH_SHORT).show()
        }
    }

    private fun showThemePicker() {
        val options = PrefsManager.ThemeOption.entries.toTypedArray()
        val labels = options.map { ThemeHelper.displayLabel(it) }.toTypedArray()
        val current = options.indexOf(PrefsManager.getThemeOption(this)).coerceAtLeast(0)
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_theme_dialog_title)
            .setSingleChoiceItems(labels, current) { dialog, which ->
                val selected = options[which]
                ThemeHelper.setThemeOption(this, selected)
                binding.themeValue.text = ThemeHelper.displayLabel(selected)
                dialog.dismiss()
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteAll() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_delete_all)
            .setMessage(R.string.settings_delete_all_message)
            .setPositiveButton(R.string.settings_delete_all_confirm) { _, _ ->
                lifecycleScope.launch(Dispatchers.IO) {
                    val dao = AppDatabase.getDatabase(applicationContext).captureEntryDao()
                    dao.getAllEntriesSnapshot().forEach { entry ->
                        FileUtils.deleteFile(entry.screenshotPath)
                        FileUtils.deleteFile(entry.thumbnailPath)
                        FileUtils.deleteFile(entry.voiceNotePath)
                        ReminderScheduler.cancel(applicationContext, entry.id)
                    }
                    dao.deleteAll()
                    runOnUiThread {
                        Toast.makeText(this@SettingsActivity, R.string.settings_delete_all_success, Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupNav() {
        applySettingsNavState()
        binding.navNotes.setOnClickListener {
            startActivity(MainActivity.createIntent(this, "notes"))
            finish()
        }
        binding.navVoice.setOnClickListener {
            startActivity(MainActivity.createIntent(this, "voice"))
            finish()
        }
        binding.navSearch.setOnClickListener {
            startActivity(MainActivity.createIntent(this, "search"))
            finish()
        }
        binding.btnSettingsActive.setOnClickListener {
            finish()
        }
    }

    private fun setupInfoCard() {
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0"
        lifecycleScope.launch(Dispatchers.IO) {
            val entries = AppDatabase.getDatabase(applicationContext).captureEntryDao().getAllEntriesSnapshot()
            val favorites = entries.count { it.isFavorite }
            val reminders = entries.count { it.reminderAt != null }
            val withVoice = entries.count { !it.voiceNotePath.isNullOrBlank() }
            runOnUiThread {
                binding.settingsInfoBody.text =
                    "Version $versionName\n${entries.size} captures, $favorites favorites, $withVoice voice memos, $reminders active reminders.\nLocal-first capture workspace with note, voice, reminder, and accessibility shortcut support."
            }
        }
    }

    private fun applyGlassSystem() {
        GlassUi.applyBlur(binding.backdropBlobTop, 56f)
        GlassUi.applyBlur(binding.backdropBlobBottom, 62f)

        listOf(
            binding.rowNdot,
            binding.rowAiSummary,
            binding.rowTheme,
            binding.rowKeyBinds,
            binding.rowAbout,
            binding.rowPrivacy,
            binding.rowTerms,
            binding.rowDeleteAll,
            binding.settingsInfoCard,
            binding.btnSettingsActive
        ).forEach { view ->
            GlassUi.applyDepth(view, 14f)
            GlassUi.attachLiquidPress(view)
        }

        GlassUi.applyDepth(binding.settingsBottomNav, 16f)

        listOf(binding.navNotes, binding.navVoice, binding.navSearch).forEach { nav ->
            GlassUi.attachLiquidPress(nav, 0.95f, 0.92f)
        }

        GlassUi.animateEntrance(binding.settingsHeading, 20L, 16f)
        GlassUi.animateEntrance(binding.rowNdot, 70L, 12f)
        GlassUi.animateEntrance(binding.rowAiSummary, 92L, 12f)
        GlassUi.animateEntrance(binding.rowKeyBinds, 114L, 12f)
        GlassUi.animateEntrance(binding.rowAbout, 138L, 12f)
        GlassUi.animateEntrance(binding.rowPrivacy, 162L, 12f)
        GlassUi.animateEntrance(binding.rowTerms, 186L, 12f)
        GlassUi.animateEntrance(binding.rowDeleteAll, 228L, 12f)
        GlassUi.animateEntrance(binding.settingsInfoCard, 254L, 12f)
        GlassUi.animateEntrance(binding.btnSettingsActive, 270L, 16f)
    }

    private fun applyThemeVisuals() {
        val palette = ThemeHelper.palette(this)
        ThemeHelper.applyRootBackground(binding.root, palette)
        listOf(
            binding.rowNdot,
            binding.rowAiSummary,
            binding.rowTheme,
            binding.rowKeyBinds,
            binding.rowAbout,
            binding.rowPrivacy,
            binding.rowTerms,
            binding.rowDeleteAll,
            binding.settingsInfoCard,
            binding.settingsBottomNav,
            binding.btnSettingsActive
        ).forEachIndexed { index, view ->
            ThemeHelper.tintSurface(view, if (index < 8) palette.surfaceStrongTint else palette.navShellTint)
        }
        applySettingsNavState()
    }

    private fun applySettingsNavState() {
        val palette = ThemeHelper.palette(this)
        ThemeHelper.styleNavActive(binding.navNotes, palette, false)
        ThemeHelper.styleNavActive(binding.navVoice, palette, false)
        ThemeHelper.styleNavActive(binding.navSearch, palette, false)
        ThemeHelper.tintSurface(binding.btnSettingsActive, palette.navActiveTint)
    }
}
