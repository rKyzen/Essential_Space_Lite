package com.essential.spacelite.utils

import android.graphics.Color
import android.graphics.RenderEffect
import android.graphics.Shader
import android.os.Build
import android.view.MotionEvent
import android.view.View
import android.view.animation.OvershootInterpolator
import android.view.animation.PathInterpolator
import kotlin.math.roundToInt

object GlassUi {

    private val liquidInterpolator = PathInterpolator(0.22f, 0.0f, 0.12f, 1.0f)
    private val settleInterpolator = OvershootInterpolator(0.5f)

    fun applyDepth(view: View, elevationDp: Float = 14f) {
        view.elevation = view.dp(elevationDp)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            view.outlineAmbientShadowColor = Color.argb(46, 0, 0, 0)
            view.outlineSpotShadowColor = Color.argb(62, 0, 0, 0)
        }
    }

    fun applyBlur(view: View, radiusDp: Float = 42f) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val radius = view.dp(radiusDp)
            view.setRenderEffect(RenderEffect.createBlurEffect(radius, radius, Shader.TileMode.CLAMP))
        }
    }

    fun attachLiquidPress(
        view: View,
        pressedScale: Float = 0.975f,
        pressedAlpha: Float = 0.94f
    ) {
        view.setOnTouchListener { v, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    v.animate()
                        .scaleX(pressedScale)
                        .scaleY(pressedScale)
                        .alpha(pressedAlpha)
                        .translationY(v.dp(1.5f))
                        .setDuration(120L)
                        .setInterpolator(liquidInterpolator)
                        .start()
                }

                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    v.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(220L)
                        .setInterpolator(settleInterpolator)
                        .start()
                }
            }
            false
        }
    }

    fun animateEntrance(
        view: View,
        delayMs: Long = 0L,
        offsetDp: Float = 14f,
        scaleFrom: Float = 0.985f
    ) {
        view.alpha = 0f
        view.scaleX = scaleFrom
        view.scaleY = scaleFrom
        view.translationY = view.dp(offsetDp)
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setStartDelay(delayMs)
            .setDuration(380L)
            .setInterpolator(settleInterpolator)
            .start()
    }

    private fun View.dp(value: Float): Float = value * resources.displayMetrics.density
}
