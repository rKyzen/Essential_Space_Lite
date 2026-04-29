package com.essential.spacelite.capture

import android.accessibilityservice.AccessibilityService
import android.graphics.Bitmap
import android.graphics.ColorSpace
import android.hardware.HardwareBuffer
import android.view.Display
import android.content.Context
import android.os.Handler
import android.os.Looper
import com.essential.spacelite.overlay.CaptureOverlayManager
import java.util.concurrent.Executor

object CaptureOrchestrator {
    private const val DIRECT_CAPTURE_GRACE_MS = 1200L

    private var overlay: CaptureOverlayManager? = null
    private var currentObserver: ScreenshotObserver? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var captureTimeout: Runnable? = null
    private var fallbackStart: Runnable? = null
    private var captureToken = 0L
    private var hasDeliveredCapture = false
    private var fallbackStarted = false
    private val mainExecutor = Executor { runnable -> mainHandler.post(runnable) }

    fun init(serviceContext: Context) {
        if (overlay != null) return
        overlay = CaptureOverlayManager(serviceContext).also { it.init() }
    }

    fun onTrigger(service: AccessibilityService) {
        init(service)
        val triggerTime = System.currentTimeMillis()
        val token = ++captureToken
        hasDeliveredCapture = false
        fallbackStarted = false
        overlay?.beginCapture(token)

        currentObserver?.stop()
        currentObserver = null
        captureTimeout?.let { mainHandler.removeCallbacks(it) }
        captureTimeout = null
        fallbackStart?.let { mainHandler.removeCallbacks(it) }
        fallbackStart = null

        captureTimeout = Runnable {
            currentObserver?.stop()
            currentObserver = null
            overlay?.onScreenshotFailed(token)
        }.also {
            mainHandler.postDelayed(it, 12_000L)
        }

        requestDirectScreenshot(service, token)
        fallbackStart = Runnable {
            startFallbackCapture(service, token, triggerTime)
        }.also {
            mainHandler.postDelayed(it, DIRECT_CAPTURE_GRACE_MS)
        }
    }

    private fun requestDirectScreenshot(
        service: AccessibilityService,
        token: Long
    ) {
        try {
            service.takeScreenshot(
                Display.DEFAULT_DISPLAY,
                mainExecutor,
                object : AccessibilityService.TakeScreenshotCallback {
                    override fun onSuccess(screenshot: AccessibilityService.ScreenshotResult) {
                        val bitmap = screenshot.toSoftwareBitmap()
                        if (bitmap != null) {
                            deliverBitmap(token, bitmap)
                        }
                    }

                    override fun onFailure(errorCode: Int) = Unit
                }
            )
        } catch (_: Exception) {
        }
    }

    private fun startFallbackCapture(
        service: AccessibilityService,
        token: Long,
        triggerTime: Long
    ) {
        if (token != captureToken || hasDeliveredCapture || fallbackStarted) return
        fallbackStarted = true
        startMediaStoreObserver(service, token, triggerTime)
        service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_TAKE_SCREENSHOT)
    }

    private fun startMediaStoreObserver(
        serviceContext: Context,
        token: Long,
        triggerTime: Long
    ) {
        currentObserver?.stop()
        currentObserver = null

        val obs = ScreenshotObserver(
            context = serviceContext.applicationContext,
            triggerTimeMs = triggerTime,
            onDetected = { uri ->
                deliverUri(token, uri)
            }
        )
        currentObserver = obs
        obs.start()
    }

    private fun deliverBitmap(token: Long, bitmap: Bitmap) {
        if (!tryResolveCapture(token)) {
            bitmap.recycle()
            return
        }
        mainHandler.post {
            overlay?.show(token)
            overlay?.onScreenshotBitmap(token, bitmap)
        }
    }

    private fun deliverUri(token: Long, uri: android.net.Uri) {
        if (!tryResolveCapture(token)) return
        mainHandler.post {
            overlay?.show(token)
            overlay?.onScreenshotDetected(token, uri)
        }
    }

    private fun tryResolveCapture(token: Long): Boolean {
        if (token != captureToken || hasDeliveredCapture) {
            return false
        }
        hasDeliveredCapture = true
        fallbackStart?.let { mainHandler.removeCallbacks(it) }
        fallbackStart = null
        captureTimeout?.let { mainHandler.removeCallbacks(it) }
        captureTimeout = null
        currentObserver?.stop()
        currentObserver = null
        return true
    }

    fun destroy() {
        hasDeliveredCapture = false
        fallbackStarted = false
        fallbackStart?.let { mainHandler.removeCallbacks(it) }
        fallbackStart = null
        captureTimeout?.let { mainHandler.removeCallbacks(it) }
        captureTimeout = null
        currentObserver?.stop()
        currentObserver = null
        overlay?.destroy()
        overlay = null
    }

    private fun AccessibilityService.ScreenshotResult.toSoftwareBitmap(): Bitmap? {
        val buffer: HardwareBuffer = try {
            hardwareBuffer
        } catch (_: Exception) {
            return null
        }

        return try {
            val wrapped = Bitmap.wrapHardwareBuffer(buffer, colorSpace ?: ColorSpace.get(ColorSpace.Named.SRGB))
                ?: return null
            wrapped.copy(Bitmap.Config.ARGB_8888, false).also {
                wrapped.recycle()
            }
        } catch (_: Exception) {
            null
        } finally {
            try {
                buffer.close()
            } catch (_: Exception) {
            }
        }
    }

}
