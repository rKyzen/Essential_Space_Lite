package com.essential.spacelite.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.essential.spacelite.R

object ThemeHelper {

    fun applySavedNightMode(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    fun prepareActivity(activity: Activity) {
        activity.setTheme(R.style.Theme_EssentialSpaceLite_Nothing)
    }

    fun setThemeOption(context: Context, option: PrefsManager.ThemeOption) {
        PrefsManager.setThemeOption(context, option)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    fun displayLabel(option: PrefsManager.ThemeOption): String {
        return "Dark"
    }
}
