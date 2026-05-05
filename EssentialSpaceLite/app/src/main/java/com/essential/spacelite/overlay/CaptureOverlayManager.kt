package com.essential.spacelite.overlay

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.ContextThemeWrapper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import android.view.animation.PathInterpolator
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.view.WindowManager.BadTokenException
import com.essential.spacelite.R
import com.essential.spacelite.data.AppDatabase
import com.essential.spacelite.data.entity.CaptureEntry
import com.essential.spacelite.utils.FileUtils
import com.essential.spacelite.utils.GlassUi
import com.essential.spacelite.utils.VoiceRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class CaptureOverlayManager(private val context: Context) {

    companion object {
        private const val NO_ACTIVE_CAPTURE = -1L
    }

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private val wm: WindowManager = context.getSystemService(WindowManager::class.java)

    private lateinit var rootView: View
    private lateinit var windowParams: WindowManager.LayoutParams
    private var isAttached = false
    private var isVisible = false
    private var activeCaptureToken = NO_ACTIVE_CAPTURE
    private var isScreenshotReady = false
    private var isSaving = false
    private var pendingShowToken = NO_ACTIVE_CAPTURE

    private lateinit var flashOverlay: View
    private lateinit var cardContainer: View
    private lateinit var thumbnailImage: ImageView
    private lateinit var thumbnailPlaceholder: View
    private lateinit var textInput: EditText
    private lateinit var btnVoice: ImageView
    private lateinit var btnSave: TextView
    private lateinit var btnDiscard: ImageView
    private lateinit var voiceTimerText: TextView
    private lateinit var voiceWaveform: View
    private lateinit var recordingDot: View
    private lateinit var screenshotLoadingIndicator: View

    private var currentScreenshotPath: String? = null
    private var currentThumbnailPath: String? = null
    private val voiceRecorder = VoiceRecorder(context)
    private var voiceNotePath = ""
    private var voiceNoteDuration = 0L
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private var recordingTicker: Runnable? = null
    private var pendingAttachRetry = false

    private val easeOut = PathInterpolator(0.0f, 0.0f, 0.2f, 1.0f)
    private val linear = LinearInterpolator()

    fun init() {
        buildView()
        attachToWindow()
        hide(animate = false)
    }

    fun beginCapture(captureToken: Long) {
        if (isVisible) {
            hide(animate = false)
        }
        activeCaptureToken = captureToken
        pendingShowToken = NO_ACTIVE_CAPTURE
        isScreenshotReady = false
    }

    private fun buildView() {
        val themed = ContextThemeWrapper(context, R.style.Theme_EssentialSpaceLite)
        rootView = LayoutInflater.from(themed).inflate(R.layout.overlay_capture, null)

        flashOverlay = rootView.findViewById(R.id.flash_overlay)
        cardContainer = rootView.findViewById(R.id.card_container)
        thumbnailImage = rootView.findViewById(R.id.thumbnail_image)
        thumbnailPlaceholder = rootView.findViewById(R.id.thumbnail_placeholder)
        textInput = rootView.findViewById(R.id.text_note_input)
        btnVoice = rootView.findViewById(R.id.btn_voice)
        btnSave = rootView.findViewById(R.id.btn_save)
        btnDiscard = rootView.findViewById(R.id.btn_discard)
        voiceTimerText = rootView.findViewById(R.id.voice_timer)
        voiceWaveform = rootView.findViewById(R.id.voice_waveform)
        recordingDot = rootView.findViewById(R.id.recording_dot)
        screenshotLoadingIndicator = rootView.findViewById(R.id.screenshot_loading)

        GlassUi.applyDepth(cardContainer, 24f)
        GlassUi.attachLiquidPress(btnSave, 0.985f, 0.97f)
        GlassUi.attachLiquidPress(btnVoice, 0.92f, 0.88f)
        GlassUi.attachLiquidPress(btnDiscard, 0.92f, 0.88f)

        btnSave.setOnClickListener { saveAndDismiss() }
        btnDiscard.setOnClickListener { discardAndDismiss() }
        btnVoice.setOnClickListener { toggleVoice() }
        textInput.setOnClickListener { enableFocusForInput() }
        textInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) disableFocusAfterInput()
        }
    }

    private fun attachToWindow() {
        if (isAttached) return
        windowParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).also { it.gravity = Gravity.BOTTOM }

        try {
            wm.addView(rootView, windowParams)
            isAttached = true
            pendingAttachRetry = false
            flushPendingShowIfNeeded()
        } catch (_: BadTokenException) {
            scheduleAttachRetry()
        } catch (_: SecurityException) {
            scheduleAttachRetry()
        } catch (_: IllegalStateException) {
            scheduleAttachRetry()
        }
    }

    private fun enableFocusForInput() {
        windowParams.flags = windowParams.flags and WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE.inv()
        wm.updateViewLayout(rootView, windowParams)

        handler.postDelayed({
            textInput.requestFocus()
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                rootView.windowInsetsController?.show(WindowInsets.Type.ime())
            } else {
                val imm = context.getSystemService(InputMethodManager::class.java)
                imm.showSoftInput(textInput, InputMethodManager.SHOW_FORCED)
            }
        }, 160)
    }

    private fun disableFocusAfterInput() {
        handler.postDelayed({
            if (isVisible) {
                windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                wm.updateViewLayout(rootView, windowParams)
                val imm = context.getSystemService(InputMethodManager::class.java)
                imm.hideSoftInputFromWindow(textInput.windowToken, 0)
            }
        }, 100)
    }

    fun show(captureToken: Long) {
        if (!ensureAttached()) {
            pendingShowToken = captureToken
            activeCaptureToken = captureToken
            return
        }

        cardContainer.animate().setListener(null)
        cardContainer.animate().cancel()
        activeCaptureToken = captureToken
        if (isVisible) {
            resetState()
            rootView.visibility = View.VISIBLE
            rootView.alpha = 1f
            flashIn()
            triggerHaptic()
            cardContainer.alpha = 1f
            cardContainer.translationY = 0f
            cardContainer.scaleX = 1f
            cardContainer.scaleY = 1f
            return
        }

        isVisible = true
        resetState()
        rootView.visibility = View.VISIBLE
        rootView.alpha = 0f
        flashIn()
        triggerHaptic()

        cardContainer.alpha = 0f
        cardContainer.translationY = dpToPx(14f)
        cardContainer.scaleX = 0.985f
        cardContainer.scaleY = 0.985f
        rootView.animate()
            .alpha(1f)
            .setDuration(180L)
            .setInterpolator(easeOut)
            .start()
        cardContainer.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .translationY(0f)
            .setDuration(220)
            .setInterpolator(easeOut)
            .setStartDelay(20)
            .start()
    }

    fun hide(animate: Boolean = true) {
        invalidateCaptureSession()
        cardContainer.animate().setListener(null)
        cardContainer.animate().cancel()
        if (!animate) {
            rootView.visibility = View.GONE
            isVisible = false
            return
        }

        val imm = context.getSystemService(InputMethodManager::class.java)
        imm.hideSoftInputFromWindow(textInput.windowToken, 0)
        disableFocusAfterInput()

        cardContainer.animate()
            .scaleX(0.988f)
            .scaleY(0.988f)
            .alpha(0f)
            .translationY(dpToPx(10f))
            .setDuration(150)
            .setInterpolator(linear)
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    rootView.visibility = View.GONE
                    rootView.alpha = 1f
                    isVisible = false
                }
            })
            .start()
    }

    private fun flashIn() {
        flashOverlay.alpha = 0f
        flashOverlay.visibility = View.VISIBLE
        ObjectAnimator.ofFloat(flashOverlay, "alpha", 0f, 0.18f, 0f).also {
            it.duration = 80
            it.interpolator = linear
            it.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    flashOverlay.visibility = View.GONE
                }
            })
            it.start()
        }
    }

    fun onScreenshotDetected(captureToken: Long, uri: Uri) {
        scope.launch(Dispatchers.IO) {
            try {
                val bitmap = decodeBitmapWithRetries(uri)
                if (bitmap == null) {
                    withContext(Dispatchers.Main) { onScreenshotFailed(captureToken) }
                    return@launch
                }

                val (ssPath, thumbPath) = FileUtils.saveCaptureFiles(context, bitmap)
                bitmap.recycle()

                if (!isCaptureStillActive(captureToken)) {
                    File(ssPath).delete()
                    File(thumbPath).delete()
                    return@launch
                }

                currentScreenshotPath = ssPath
                currentThumbnailPath = thumbPath

                withContext(Dispatchers.Main) { revealScreenshot(captureToken, thumbPath) }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { onScreenshotFailed(captureToken) }
            }
        }
    }

    fun onScreenshotBitmap(captureToken: Long, bitmap: Bitmap) {
        scope.launch(Dispatchers.IO) {
            try {
                val (ssPath, thumbPath) = FileUtils.saveCaptureFiles(context, bitmap)
                bitmap.recycle()

                if (!isCaptureStillActive(captureToken)) {
                    File(ssPath).delete()
                    File(thumbPath).delete()
                    return@launch
                }

                currentScreenshotPath = ssPath
                currentThumbnailPath = thumbPath

                withContext(Dispatchers.Main) { revealScreenshot(captureToken, thumbPath) }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) { onScreenshotFailed(captureToken) }
            }
        }
    }

    fun onScreenshotFailed(captureToken: Long) {
        if (activeCaptureToken != captureToken) return
        if (!isVisible) {
            invalidateCaptureSession()
            Toast.makeText(context, "Could not read the screenshot. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        screenshotLoadingIndicator.visibility = View.GONE
        thumbnailPlaceholder.visibility = View.VISIBLE
        thumbnailImage.visibility = View.GONE
        setSaveEnabled(false)
        Toast.makeText(context, "Could not read the screenshot. Please try again.", Toast.LENGTH_SHORT).show()
    }

    private fun revealScreenshot(captureToken: Long, path: String) {
        if (!isCaptureStillActive(captureToken)) return
        if (!File(path).exists()) {
            onScreenshotFailed(captureToken)
            return
        }
        screenshotLoadingIndicator.visibility = View.GONE
        thumbnailImage.setImageBitmap(BitmapFactory.decodeFile(path))
        thumbnailPlaceholder.visibility = View.GONE
        thumbnailImage.visibility = View.VISIBLE
        isScreenshotReady = true
        setSaveEnabled(true)

        thumbnailImage.alpha = 0f
        thumbnailImage.scaleX = 0.96f
        thumbnailImage.scaleY = 0.96f
        thumbnailImage.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(120)
            .setInterpolator(easeOut)
            .start()
    }

    private fun toggleVoice() {
        if (isRecording) stopRecording() else startRecording()
    }

    private fun startRecording() {
        val path = FileUtils.newVoiceNotePath(context)
        if (!voiceRecorder.start(path)) return
        voiceNotePath = path
        isRecording = true
        btnVoice.setImageResource(R.drawable.ic_mic_stop)
        recordingDot.visibility = View.VISIBLE
        voiceWaveform.visibility = View.VISIBLE
        voiceTimerText.visibility = View.VISIBLE
        startRecordingTicker()
    }

    private fun stopRecording() {
        val dur = voiceRecorder.stop() ?: 0L
        isRecording = false
        voiceNoteDuration = dur
        stopRecordingTicker()
        btnVoice.setImageResource(R.drawable.ic_mic)
        recordingDot.visibility = View.GONE
        voiceWaveform.visibility = View.GONE
        if (dur < 500) {
            voiceNotePath = ""
            voiceTimerText.visibility = View.GONE
        } else {
            voiceTimerText.text = "* ${FileUtils.formatDuration(dur)}"
        }
    }

    private fun startRecordingTicker() {
        recordingTicker = object : Runnable {
            override fun run() {
                if (!isRecording) return
                val ms = voiceRecorder.durationMs
                voiceTimerText.text = formatMs(ms)
                if (ms >= 59_500) {
                    stopRecording()
                    return
                }
                handler.postDelayed(this, 100)
            }
        }
        handler.post(recordingTicker!!)
    }

    private fun stopRecordingTicker() {
        recordingTicker?.let { handler.removeCallbacks(it) }
        recordingTicker = null
    }

    private fun saveAndDismiss() {
        if (isSaving) return
        if (isRecording) stopRecording()
        val ssPath = currentScreenshotPath
        if (ssPath.isNullOrEmpty() || !isScreenshotReady) {
            Toast.makeText(context, "Screenshot is still loading. Please wait a moment.", Toast.LENGTH_SHORT).show()
            return
        }

        isSaving = true
        setSaveEnabled(false)
        btnDiscard.isEnabled = false
        btnVoice.isEnabled = false
        textInput.isEnabled = false

        val thumbPath = currentThumbnailPath ?: ""
        val note = textInput.text.toString().trim().takeIf { it.isNotEmpty() }
        val vPath = voiceNotePath.takeIf { it.isNotEmpty() }

        scope.launch(Dispatchers.IO) {
            try {
                AppDatabase.getDatabase(context).captureEntryDao().insert(
                    CaptureEntry(
                        screenshotPath = ssPath,
                        thumbnailPath = thumbPath,
                        textNote = note,
                        voiceNotePath = vPath,
                        voiceNoteDurationMs = voiceNoteDuration,
                        timestamp = System.currentTimeMillis()
                    )
                )
                withContext(Dispatchers.Main) { hide() }
            } catch (_: Exception) {
                withContext(Dispatchers.Main) {
                    isSaving = false
                    btnDiscard.isEnabled = true
                    btnVoice.isEnabled = true
                    textInput.isEnabled = true
                    setSaveEnabled(isScreenshotReady)
                    Toast.makeText(context, "Could not save capture. Please try again.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun discardAndDismiss() {
        if (isRecording) voiceRecorder.cancel()
        if (voiceNotePath.isNotEmpty()) File(voiceNotePath).delete()
        currentScreenshotPath?.let { File(it).delete() }
        currentThumbnailPath?.let { File(it).delete() }
        resetPaths()
        hide()
    }

    private fun resetState() {
        isScreenshotReady = false
        isSaving = false
        textInput.text.clear()
        textInput.isEnabled = true
        thumbnailImage.setImageDrawable(null)
        thumbnailImage.visibility = View.GONE
        thumbnailPlaceholder.visibility = View.VISIBLE
        screenshotLoadingIndicator.visibility = View.VISIBLE
        voiceTimerText.visibility = View.GONE
        voiceTimerText.text = ""
        voiceWaveform.visibility = View.GONE
        recordingDot.visibility = View.GONE
        btnVoice.setImageResource(R.drawable.ic_mic)
        btnVoice.isEnabled = true
        btnDiscard.isEnabled = true
        cardContainer.alpha = 0f
        cardContainer.translationY = 0f
        windowParams.flags = windowParams.flags or WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        wm.updateViewLayout(rootView, windowParams)
        setSaveEnabled(false)
        resetPaths()
    }

    private fun resetPaths() {
        currentScreenshotPath = null
        currentThumbnailPath = null
        voiceNotePath = ""
        voiceNoteDuration = 0L
    }

    private suspend fun decodeBitmapWithRetries(uri: Uri): android.graphics.Bitmap? {
        repeat(8) { attempt ->
            val bitmap = try {
                context.contentResolver.openInputStream(uri)?.use {
                    BitmapFactory.decodeStream(it)
                }
            } catch (_: Exception) {
                null
            }
            if (bitmap != null) return bitmap
            if (attempt < 7) delay(180)
        }
        return null
    }

    private fun setSaveEnabled(enabled: Boolean) {
        btnSave.isEnabled = enabled
        btnSave.alpha = if (enabled) 1f else 0.55f
    }

    private fun invalidateCaptureSession() {
        activeCaptureToken = NO_ACTIVE_CAPTURE
        pendingShowToken = NO_ACTIVE_CAPTURE
        isScreenshotReady = false
    }

    private fun ensureAttached(): Boolean {
        if (!isAttached) {
            attachToWindow()
        }
        return isAttached
    }

    private fun scheduleAttachRetry() {
        if (pendingAttachRetry) return
        pendingAttachRetry = true
        handler.postDelayed({
            pendingAttachRetry = false
            attachToWindow()
        }, 450)
    }

    private fun flushPendingShowIfNeeded() {
        val token = pendingShowToken
        if (token == NO_ACTIVE_CAPTURE || !isAttached) return
        pendingShowToken = NO_ACTIVE_CAPTURE
        show(token)
    }

    private fun isCaptureStillActive(captureToken: Long): Boolean {
        return isVisible && activeCaptureToken == captureToken
    }

    private fun triggerHaptic() {
        try {
            val vibrator = context.getSystemService(Vibrator::class.java)
            vibrator.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {
        }
    }

    private fun dpToPx(dp: Float) = dp * context.resources.displayMetrics.density

    private fun formatMs(ms: Long): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        return "%d:%02d".format(minutes, seconds % 60)
    }

    fun destroy() {
        stopRecordingTicker()
        voiceRecorder.release()
        scope.cancel()
        handler.removeCallbacksAndMessages(null)
        if (isAttached) {
            try {
                wm.removeView(rootView)
            } catch (_: Exception) {
            }
            isAttached = false
        }
    }
}
