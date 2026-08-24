package com.init.space.utils

import android.content.Context
import android.content.SharedPreferences

object PrefsManager {
    private const val PREFS_NAME = "init_space_prefs"
    private const val KEY_ACCESSIBILITY_RUNNING = "accessibility_running"
    private const val KEY_ACCESSIBILITY_ENABLED = "accessibility_feature_enabled"
    private const val KEY_ONBOARDING_DONE = "onboarding_done"
    private const val KEY_DISCLOSURE_ACCEPTED = "disclosure_accepted"
    private const val KEY_AI_SUMMARY_ENABLED = "ai_summary_enabled"
    private const val KEY_LAST_SEEN_VERSION_CODE = "last_seen_version_code"

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

    fun isAiSummaryEnabled(context: Context): Boolean =
        prefs(context).getBoolean(KEY_AI_SUMMARY_ENABLED, false)

    fun setAiSummaryEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean(KEY_AI_SUMMARY_ENABLED, enabled).apply()

    fun getLastSeenVersionCode(context: Context): Long =
        prefs(context).getLong(KEY_LAST_SEEN_VERSION_CODE, 0L)

    fun setLastSeenVersionCode(context: Context, versionCode: Long) =
        prefs(context).edit().putLong(KEY_LAST_SEEN_VERSION_CODE, versionCode).apply()
}
