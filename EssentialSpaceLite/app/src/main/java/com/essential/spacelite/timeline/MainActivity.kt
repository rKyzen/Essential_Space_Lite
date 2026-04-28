package com.essential.spacelite.timeline

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.essential.spacelite.R
import com.essential.spacelite.databinding.ActivityMainBinding
import com.essential.spacelite.utils.PrefsManager
import com.essential.spacelite.utils.ThemeHelper
import com.google.android.material.color.MaterialColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: TimelineViewModel by viewModels()
    private lateinit var adapter: TimelineAdapter
    private lateinit var filterChips: List<Pair<TextView, TimelineViewModel.FilterMode>>

    private val permissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.prepareActivity(this)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeEntries()
        setupSearchAndFilters()
        setupServiceButton()
        requestAllPermissions()
        checkAndPromptAccessibility()
    }

    private fun setupRecyclerView() {
        adapter = TimelineAdapter(
            onItemClick = { entry ->
                startActivity(Intent(this, DetailActivity::class.java).apply {
                    putExtra(DetailActivity.EXTRA_ENTRY_ID, entry.id)
                })
            },
            onItemLongClick = { entry ->
                showDeleteDialog(entry.id)
                true
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = this@MainActivity.adapter
            setHasFixedSize(false)
        }
    }

    private fun observeEntries() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.entries.collect { entries ->
                    adapter.submitList(entries)
                    val showEmpty = entries.isEmpty()
                    binding.emptyState.visibility = if (showEmpty) View.VISIBLE else View.GONE
                    binding.recyclerView.visibility = if (showEmpty) View.GONE else View.VISIBLE
                    updateEmptyState()
                }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.subtitle.collect { binding.subtitle.text = it }
            }
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.currentFilter.collect { mode ->
                    updateChipSelection(mode)
                    updateEmptyState()
                }
            }
        }
    }

    private fun setupSearchAndFilters() {
        binding.searchInput.doAfterTextChanged {
            viewModel.setQuery(it?.toString().orEmpty())
            updateEmptyState()
        }

        filterChips = listOf(
            binding.chipAll to TimelineViewModel.FilterMode.ALL,
            binding.chipToday to TimelineViewModel.FilterMode.TODAY,
            binding.chipNotes to TimelineViewModel.FilterMode.NOTES,
            binding.chipVoice to TimelineViewModel.FilterMode.VOICE
        )

        filterChips.forEach { (chip, mode) ->
            chip.setOnClickListener { viewModel.setFilter(mode) }
        }
        updateChipSelection(TimelineViewModel.FilterMode.ALL)
    }

    private fun updateChipSelection(selectedMode: TimelineViewModel.FilterMode) {
        filterChips.forEach { (chip, mode) ->
            val selected = mode == selectedMode
            chip.setBackgroundResource(
                if (selected) R.drawable.bg_filter_chip_active else R.drawable.bg_filter_chip
            )
            chip.setTextColor(
                if (selected) {
                    MaterialColors.getColor(chip, com.google.android.material.R.attr.colorOnPrimary)
                } else {
                    getColor(R.color.text_secondary)
                }
            )
        }
    }

    private fun updateEmptyState() {
        val isFiltering = binding.searchInput.text?.isNotBlank() == true ||
            viewModel.currentFilter.value != TimelineViewModel.FilterMode.ALL

        if (isFiltering) {
            binding.emptyStateTitle.setText(R.string.empty_state_filtered_title)
            binding.emptyStateSubtitle.setText(R.string.empty_state_filtered_subtitle)
        } else {
            binding.emptyStateTitle.setText(R.string.empty_state_title)
            binding.emptyStateSubtitle.setText(R.string.empty_state_subtitle)
        }
    }

    private fun setupServiceButton() {
        binding.fabCapture.setOnClickListener {
            if (!isAccessibilityServiceEnabled()) {
                openAccessibilitySettings()
            } else {
                android.widget.Toast.makeText(
                    this,
                    "Press Vol Up + Down to capture",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }

        binding.btnSettings.setOnClickListener { showSettingsMenu() }
    }

    private fun showSettingsMenu() {
        val options = arrayOf(
            getString(R.string.settings_open_accessibility),
            getString(R.string.settings_how_to_use),
            "${getString(R.string.settings_theme)}: ${ThemeHelper.displayLabel(PrefsManager.getThemeOption(this))}",
            getString(R.string.settings_privacy),
            getString(R.string.settings_credits),
            "${getString(R.string.settings_version)}: v${getAppVersion()}"
        )

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_title)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> openAccessibilitySettings()
                    1 -> showHowToUse()
                    2 -> showThemeDialog()
                    3 -> showPrivacyNote()
                    4 -> showCredits()
                    5 -> showVersionDialog()
                }
            }
            .show()
    }

    private fun showThemeDialog() {
        val options = listOf(
            PrefsManager.ThemeOption.MATERIAL_YOU_LIGHT,
            PrefsManager.ThemeOption.MATERIAL_YOU_DARK,
            PrefsManager.ThemeOption.NOTHING_LIGHT,
            PrefsManager.ThemeOption.NOTHING_DARK
        )
        val labels = options.map { ThemeHelper.displayLabel(it) }.toTypedArray()
        val current = PrefsManager.getThemeOption(this)
        var selectedIndex = options.indexOf(current).coerceAtLeast(0)

        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_theme)
            .setSingleChoiceItems(labels, selectedIndex) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("Apply") { _, _ ->
                ThemeHelper.setThemeOption(this, options[selectedIndex])
                recreate()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPrivacyNote() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.privacy_note_title)
            .setMessage(R.string.privacy_note_message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showCredits() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.credits_title)
            .setMessage(R.string.credits_message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showVersionDialog() {
        MaterialAlertDialogBuilder(this)
            .setTitle(R.string.settings_version)
            .setMessage("Essential Space Lite v${getAppVersion()}")
            .setPositiveButton("OK", null)
            .show()
    }

    private fun getAppVersion(): String {
        @Suppress("DEPRECATION")
        return packageManager.getPackageInfo(packageName, 0).versionName ?: "2.1"
    }

    private fun checkAndPromptAccessibility() {
        if (!isAccessibilityServiceEnabled()) {
            binding.accessibilityBanner.visibility = View.VISIBLE
            binding.accessibilityBanner.setOnClickListener { openAccessibilitySettings() }
        } else {
            binding.accessibilityBanner.visibility = View.GONE
        }
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

    private fun showHowToUse() {
        MaterialAlertDialogBuilder(this)
            .setTitle("How to Use")
            .setMessage(
                "1. Enable Essential Space in Accessibility Settings\n\n" +
                    "2. Press Volume Up + Volume Down simultaneously to capture\n\n" +
                    "3. The screen is captured instantly via the accessibility API\n\n" +
                    "4. Add a text note or voice note in the overlay\n\n" +
                    "5. Tap Save and your capture is stored instantly."
            )
            .setPositiveButton("Got it", null)
            .show()
    }

    private fun showDeleteDialog(entryId: Long) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete entry?")
            .setMessage("This will permanently delete the screenshot, text note, and voice note.")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    val entry = com.essential.spacelite.data.AppDatabase
                        .getDatabase(applicationContext)
                        .captureEntryDao()
                        .getEntryById(entryId)
                    entry?.let { viewModel.deleteEntry(it) }
                }
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
            perms.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionsLauncher.launch(perms.toTypedArray())
    }

    override fun onResume() {
        super.onResume()
        checkAndPromptAccessibility()
    }
}
