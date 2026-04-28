package com.essential.spacelite.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsManager {
    private const val PREFS_NAME = "essential_space_prefs"
    private const val KEY_ACCESSIBILITY_RUNNING = "accessibility_running"
    private const val KEY_ACCESSIBILITY_ENABLED = "accessibility_feature_enabled"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_THEME_OPTION = "theme_option"

    enum class ThemeOption(val storageValue: String) {
        MATERIAL_YOU_LIGHT("material_you_light"),
        MATERIAL_YOU_DARK("material_you_dark"),
        NOTHING_LIGHT("nothing_light"),
        NOTHING_DARK("nothing_dark");

        companion object {
            fun fromStorage(value: String?): ThemeOption {
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

    fun getThemeOption(context: Context): ThemeOption =
        ThemeOption.fromStorage(prefs(context).getString(KEY_THEME_OPTION, null))

    fun setThemeOption(context: Context, option: ThemeOption) =
        prefs(context).edit().putString(KEY_THEME_OPTION, option.storageValue).apply()
}
