package com.essential.spacelite.timeline

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import com.essential.spacelite.databinding.ActivityAccessibilityDisclosureBinding
import com.essential.spacelite.utils.PrefsManager
import com.essential.spacelite.utils.ThemeHelper

class AccessibilityDisclosureActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAccessibilityDisclosureBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        ThemeHelper.prepareActivity(this)
        super.onCreate(savedInstanceState)
        binding = ActivityAccessibilityDisclosureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnContinue.setOnClickListener {
            PrefsManager.setDisclosureAccepted(this, true)
            PrefsManager.setOnboardingDone(this, true)
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            finish()
        }

        binding.btnPermissions.setOnClickListener {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
            }
            startActivity(intent)
        }

        binding.btnLater.setOnClickListener {
            PrefsManager.setDisclosureAccepted(this, true)
            PrefsManager.setOnboardingDone(this, true)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}
