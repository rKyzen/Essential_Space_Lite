package com.essential.spacelite.utils

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.essential.spacelite.R

object ThemeHelper {

    data class ThemePalette(
        val backgroundColor: Int,
        val surfaceTint: Int,
        val surfaceStrongTint: Int,
        val navShellTint: Int,
        val navActiveTint: Int,
        val accentTint: Int,
        val primaryText: Int
    )

    fun applySavedNightMode(context: Context) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    fun prepareActivity(activity: Activity) {
        val style = when (PrefsManager.getThemeOption(activity)) {
            PrefsManager.ThemeOption.NOTHING_DARK -> R.style.Theme_EssentialSpaceLite_Nothing
            PrefsManager.ThemeOption.NOTHING_CARBON -> R.style.Theme_EssentialSpaceLite_Carbon
            PrefsManager.ThemeOption.NOTHING_GLASS -> R.style.Theme_EssentialSpaceLite_Glass
        }
        activity.setTheme(style)
    }

    fun setThemeOption(context: Context, option: PrefsManager.ThemeOption) {
        PrefsManager.setThemeOption(context, option)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }

    fun displayLabel(option: PrefsManager.ThemeOption): String {
        return when (option) {
            PrefsManager.ThemeOption.NOTHING_DARK -> "Pure Black"
            PrefsManager.ThemeOption.NOTHING_CARBON -> "Carbon"
            PrefsManager.ThemeOption.NOTHING_GLASS -> "Glass"
        }
    }

    fun palette(context: Context, option: PrefsManager.ThemeOption = PrefsManager.getThemeOption(context)): ThemePalette {
        return when (option) {
            PrefsManager.ThemeOption.NOTHING_DARK -> ThemePalette(
                backgroundColor = 0xFF050505.toInt(),
                surfaceTint = 0xCC101010.toInt(),
                surfaceStrongTint = 0xE0181818.toInt(),
                navShellTint = 0xE0141414.toInt(),
                navActiveTint = 0xF0222222.toInt(),
                accentTint = ContextCompat.getColor(context, R.color.accent_white),
                primaryText = ContextCompat.getColor(context, R.color.text_primary)
            )

            PrefsManager.ThemeOption.NOTHING_CARBON -> ThemePalette(
                backgroundColor = 0xFF0D1014.toInt(),
                surfaceTint = 0xCC1C242D.toInt(),
                surfaceStrongTint = 0xE029333D.toInt(),
                navShellTint = 0xE0171D24.toInt(),
                navActiveTint = 0xF02A3138.toInt(),
                accentTint = 0xFFE5E7EB.toInt(),
                primaryText = 0xFFF4F6F8.toInt()
            )

            PrefsManager.ThemeOption.NOTHING_GLASS -> ThemePalette(
                backgroundColor = 0xFF08111B.toInt(),
                surfaceTint = 0xB8152B42.toInt(),
                surfaceStrongTint = 0xD8223A54.toInt(),
                navShellTint = 0xD8122335.toInt(),
                navActiveTint = 0xF0294664.toInt(),
                accentTint = ContextCompat.getColor(context, R.color.accent_yellow),
                primaryText = 0xFFF5F7FA.toInt()
            )
        }
    }

    fun applyRootBackground(root: View, palette: ThemePalette) {
        root.setBackgroundColor(palette.backgroundColor)
    }

    fun tintSurface(view: View, color: Int) {
        val background = view.background?.mutate() ?: return
        DrawableCompat.setTint(background, color)
        view.background = background
    }

    fun styleCard(card: MaterialCardView, palette: ThemePalette, strong: Boolean = false) {
        card.setCardBackgroundColor(if (strong) palette.surfaceStrongTint else palette.surfaceTint)
        card.strokeColor = palette.accentTint and 0x55FFFFFF
    }

    fun styleNavActive(view: View, palette: ThemePalette, active: Boolean) {
        if (active) {
            view.setBackgroundResource(R.drawable.bg_origin_nav_active)
            tintSurface(view, palette.navActiveTint)
            view.alpha = 1f
        } else {
            view.background = null
            view.alpha = 0.82f
        }
    }

    fun stylePrimaryButton(button: View, palette: ThemePalette) {
        when (button) {
            is MaterialButton -> {
                button.backgroundTintList = ColorStateList.valueOf(palette.accentTint)
                button.setTextColor(palette.backgroundColor)
            }

            is TextView -> {
                tintSurface(button, palette.accentTint)
                button.setTextColor(palette.backgroundColor)
            }
        }
    }

    fun styleOutlineButton(button: View, palette: ThemePalette) {
        when (button) {
            is MaterialButton -> {
                button.strokeColor = ColorStateList.valueOf(palette.accentTint and 0x66FFFFFF)
                button.setTextColor(palette.primaryText)
            }

            is TextView -> {
                tintSurface(button, palette.surfaceStrongTint)
                button.setTextColor(palette.primaryText)
            }
        }
    }

    fun applyAmbientMode(option: PrefsManager.ThemeOption, topBlob: View?, bottomBlob: View? = null) {
        when (option) {
            PrefsManager.ThemeOption.NOTHING_DARK -> {
                topBlob?.alpha = 0.62f
                bottomBlob?.alpha = 0.48f
            }

            PrefsManager.ThemeOption.NOTHING_CARBON -> {
                topBlob?.alpha = 0.42f
                bottomBlob?.alpha = 0.32f
                topBlob?.scaleX = 0.92f
                topBlob?.scaleY = 0.92f
                bottomBlob?.scaleX = 0.94f
                bottomBlob?.scaleY = 0.94f
            }

            PrefsManager.ThemeOption.NOTHING_GLASS -> {
                topBlob?.alpha = 0.86f
                bottomBlob?.alpha = 0.7f
                topBlob?.scaleX = 1.06f
                topBlob?.scaleY = 1.06f
                bottomBlob?.scaleX = 1.04f
                bottomBlob?.scaleY = 1.04f
            }
        }
    }
}
