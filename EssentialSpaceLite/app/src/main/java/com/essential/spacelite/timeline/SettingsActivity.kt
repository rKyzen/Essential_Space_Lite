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
    }

    private fun setupHeading() {
        binding.switchNdot.isChecked = PrefsManager.useNdotHeadings(this)
        updateHeading()

        binding.switchNdot.setOnCheckedChangeListener { _, isChecked ->
            PrefsManager.setUseNdotHeadings(this, isChecked)
            updateHeading()
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
        binding.settingsInfoBody.text =
            "Version $versionName\nLocal-first capture workspace with note, voice, reminder, and accessibility shortcut support."
    }

    private fun applyGlassSystem() {
        GlassUi.applyBlur(binding.backdropBlobTop, 56f)
        GlassUi.applyBlur(binding.backdropBlobBottom, 62f)

        listOf(
            binding.rowNdot,
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
        GlassUi.animateEntrance(binding.rowKeyBinds, 95L, 12f)
        GlassUi.animateEntrance(binding.rowAbout, 120L, 12f)
        GlassUi.animateEntrance(binding.rowPrivacy, 145L, 12f)
        GlassUi.animateEntrance(binding.rowTerms, 170L, 12f)
        GlassUi.animateEntrance(binding.rowDeleteAll, 220L, 12f)
        GlassUi.animateEntrance(binding.settingsInfoCard, 250L, 12f)
        GlassUi.animateEntrance(binding.btnSettingsActive, 260L, 16f)
    }
}
