package com.init.space.accessibility

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.init.space.capture.CaptureOrchestrator
import com.init.space.utils.PrefsManager
import kotlin.math.abs

/**
 * Detects simultaneous Volume Up + Volume Down key combinations.
 * Configures FLAG_REQUEST_FILTER_KEY_EVENTS both in XML and programmatically at runtime.
 * Consumes both ACTION_DOWN and ACTION_UP for the combo synchronously before any capture dispatch.
 */
class VolumeAccessibilityService : AccessibilityService() {

    private var volUpTime = 0L
    private var volDownTime = 0L
    private var isVolUpPressed = false
    private var isVolDownPressed = false
    private var lastTrigger = 0L
    private var isComboActive = false

    private val windowMs = 150L
    private val debounceMs = 1500L

    private val mainHandler = Handler(Looper.getMainLooper())

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (!PrefsManager.isAccessibilityEnabled(this)) return false

        val keyCode = event.keyCode
        if (keyCode != KeyEvent.KEYCODE_VOLUME_UP && keyCode != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return false
        }

        val now = SystemClock.elapsedRealtime()

        // If inside the debounce window following a trigger, consume all volume events
        if (now - lastTrigger < debounceMs) {
            return true
        }

        if (event.action == KeyEvent.ACTION_DOWN) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                isVolUpPressed = true
                volUpTime = now
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                isVolDownPressed = true
                volDownTime = now
            }

            val isSimultaneous = (isVolUpPressed && isVolDownPressed) ||
                    (volUpTime != 0L && volDownTime != 0L && abs(volUpTime - volDownTime) <= windowMs)

            if (isSimultaneous) {
                lastTrigger = now
                volUpTime = 0L
                volDownTime = 0L
                isComboActive = true

                // Suppress any OS volume UI
                suppressVolumeUi()

                // Post capture dispatch asynchronously so onKeyEvent returns true immediately
                mainHandler.post {
                    fire()
                }

                // Synchronously consume key-down before the OS can route to volume slider
                return true
            }
        } else if (event.action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                isVolUpPressed = false
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                isVolDownPressed = false
            }

            if (isComboActive || (now - lastTrigger < debounceMs)) {
                if (!isVolUpPressed && !isVolDownPressed) {
                    isComboActive = false
                }
                // Synchronously consume key-up so OS doesn't adjust volume on release
                return true
            }
        }

        return false // Allow normal volume adjustment for standard single key presses
    }

    private fun suppressVolumeUi() {
        try {
            val audioManager = getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.adjustSuggestedStreamVolume(
                AudioManager.ADJUST_SAME,
                AudioManager.USE_DEFAULT_STREAM_TYPE,
                0
            )
        } catch (_: Exception) {
        }
    }

    private fun fire() {
        CaptureOrchestrator.onTrigger(this)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    override fun onServiceConnected() {
        super.onServiceConnected()

        // Ensure FLAG_REQUEST_FILTER_KEY_EVENTS is explicitly active at runtime
        val info = serviceInfo ?: AccessibilityServiceInfo()
        info.flags = info.flags or AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
        serviceInfo = info

        PrefsManager.setAccessibilityServiceRunning(this, true)
        CaptureOrchestrator.init(this)
    }

    override fun onDestroy() {
        CaptureOrchestrator.destroy()
        PrefsManager.setAccessibilityServiceRunning(this, false)
        super.onDestroy()
    }
}
