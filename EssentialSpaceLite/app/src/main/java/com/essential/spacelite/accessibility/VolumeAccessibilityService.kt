package com.essential.spacelite.accessibility

import android.accessibilityservice.AccessibilityService
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.essential.spacelite.capture.CaptureOrchestrator
import com.essential.spacelite.utils.PrefsManager

/**
 * Detects simultaneous Vol Up + Vol Down (within 100ms window).
 * Uses the accessibility screenshot API on trigger and falls back to the
 * legacy global screenshot path only if needed.
 *
 * Debounce: 1500ms between triggers.
 */
class VolumeAccessibilityService : AccessibilityService() {

    private var volUpTime   = 0L
    private var volDownTime = 0L
    private var lastTrigger = 0L

    private val WINDOW_MS   = 100L
    private val DEBOUNCE_MS = 1500L

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!PrefsManager.isAccessibilityEnabled(this)) return false
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val now = SystemClock.elapsedRealtime()
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP   -> { volUpTime   = now; checkTrigger(now) }
            KeyEvent.KEYCODE_VOLUME_DOWN -> { volDownTime = now; checkTrigger(now) }
        }
        return false // never consume — let volume still work
    }

    private fun checkTrigger(now: Long) {
        val up   = volUpTime
        val down = volDownTime
        if (up == 0L || down == 0L) return
        if (Math.abs(up - down) > WINDOW_MS) return
        if (now - lastTrigger < DEBOUNCE_MS) return

        lastTrigger = now
        volUpTime   = 0L
        volDownTime = 0L

        fire()
    }

    private fun fire() {
        // 1. Take screenshot via accessibility API — no MediaProjection needed
        CaptureOrchestrator.onTrigger(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()
        PrefsManager.setAccessibilityServiceRunning(this, true)
        // Defer overlay init by 500ms — the accessibility service window token
        // is not fully registered with WindowManager at the moment onServiceConnected
        // fires. Adding a view immediately causes "Operation not started: CREATE_ACCESSIBILITY_OVERLAY".
        mainHandler.postDelayed({
            CaptureOrchestrator.init(this)
        }, 500)
    }

    override fun onDestroy() {
        CaptureOrchestrator.destroy()
        PrefsManager.setAccessibilityServiceRunning(this, false)
        super.onDestroy()
    }
}
