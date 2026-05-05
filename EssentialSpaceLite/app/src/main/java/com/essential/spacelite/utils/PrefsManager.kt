package com.essential.spacelite.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsManager {
    private const val PREFS_NAME = "essential_space_prefs"
    private const val KEY_ACCESSIBILITY_RUNNING = "accessibility_running"
    private const val KEY_ACCESSIBILITY_ENABLED = "accessibility_feature_enabled"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_DISCLOSURE_ACCEPTED = "disclosure_accepted"
    private const val KEY_THEME_OPTION = "theme_option"
    private const val KEY_USE_NDOT = "use_ndot_headings"

    enum class ThemeOption(val storageValue: String) {
        NOTHING_DARK("nothing_dark");

        companion object {
            fun fromStorage(value: String?): ThemeOption {
                if (value == "material_you_dark") return NOTHING_DARK
                if (value == "nothing_light") return NOTHING_DARK
                if (value == "material_you_light") return NOTHING_DARK
                return entries.firstOrNull { it.storageValue == value } ?: NOTHING_DARK
            }
        }
    }

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun isAccessibilityServiceRunning(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ACCESSIBILITY_RUNNING, false)

    fun setAccessibilityServiceRunning(context: Context, running: Boolean) =
        prefs(context).edit().putBoolean(KEY_ACCESSIBILITY_RUNNING, running).apply()

    fun isAccessibilityEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ACCESSIBILITY_ENABLED, true)

    fun setAccessibilityEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_ACCESSIBILITY_ENABLED, enabled).apply()

    fun isOnboardingDone(context: Context): Boolean =
        prefs(context).getBoolean(KEY_ONBOARDING_DONE, false)

    fun setOnboardingDone(context: Context, done: Boolean) =
        prefs(context).edit().putBoolean(KEY_ONBOARDING_DONE, done).apply()

    fun isDisclosureAccepted(context: Context): Boolean =
        prefs(context).getBoolean(KEY_DISCLOSURE_ACCEPTED, false)

    fun setDisclosureAccepted(context: Context, accepted: Boolean) =
        prefs(context).edit().putBoolean(KEY_DISCLOSURE_ACCEPTED, accepted).apply()

    fun getThemeOption(context: Context): ThemeOption =
        ThemeOption.fromStorage(prefs(context).getString(KEY_THEME_OPTION, null))

    fun setThemeOption(context: Context, option: ThemeOption) =
        prefs(context).edit().putString(KEY_THEME_OPTION, option.storageValue).apply()

    fun useNdotHeadings(context: Context): Boolean =
        prefs(context).getBoolean(KEY_USE_NDOT, true)

    fun setUseNdotHeadings(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_USE_NDOT, enabled).apply()
}
