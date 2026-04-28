package com.essential.spacelite.utils

import android.app.Activity
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import com.essential.spacelite.R
import com.google.android.material.color.DynamicColors

object ThemeHelper {

    fun applySavedNightMode(context: Context) {
        AppCompatDelegate.setDefaultNightMode(toNightMode(PrefsManager.getThemeOption(context)))
    }

    fun prepareActivity(activity: Activity) {
        val option = PrefsManager.getThemeOption(activity)
        activity.setTheme(
            when (option) {
                PrefsManager.ThemeOption.MATERIAL_YOU_LIGHT,
                PrefsManager.ThemeOption.MATERIAL_YOU_DARK -> R.style.Theme_EssentialSpaceLite_MaterialYou
                PrefsManager.ThemeOption.NOTHING_LIGHT,
                PrefsManager.ThemeOption.NOTHING_DARK -> R.style.Theme_EssentialSpaceLite_Nothing
            }
        )
        if (option == PrefsManager.ThemeOption.MATERIAL_YOU_LIGHT ||
            option == PrefsManager.ThemeOption.MATERIAL_YOU_DARK
        ) {
            DynamicColors.applyToActivityIfAvailable(activity)
        }
    }

    fun setThemeOption(context: Context, option: PrefsManager.ThemeOption) {
        PrefsManager.setThemeOption(context, option)
        AppCompatDelegate.setDefaultNightMode(toNightMode(option))
    }

    fun displayLabel(option: PrefsManager.ThemeOption): String {
        return when (option) {
            PrefsManager.ThemeOption.MATERIAL_YOU_LIGHT -> "Material You Light"
            PrefsManager.ThemeOption.MATERIAL_YOU_DARK -> "Material You Dark"
            PrefsManager.ThemeOption.NOTHING_LIGHT -> "Nothing Light"
            PrefsManager.ThemeOption.NOTHING_DARK -> "Nothing Dark"
        }
    }

    private fun toNightMode(option: PrefsManager.ThemeOption): Int {
        return when (option) {
            PrefsManager.ThemeOption.MATERIAL_YOU_LIGHT,
            PrefsManager.ThemeOption.NOTHING_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            PrefsManager.ThemeOption.MATERIAL_YOU_DARK,
            PrefsManager.ThemeOption.NOTHING_DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
    }
}
