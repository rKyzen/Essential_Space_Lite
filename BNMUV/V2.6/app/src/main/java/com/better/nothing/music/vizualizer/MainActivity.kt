package com.better.nothing.music.vizualizer

import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.IBinder
import android.service.quicksettings.TileService
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.Font
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Shapes
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.pow

// ─── Stable wrappers ─────────────────────────────────────────────────────────

@Stable
data class PresetInfo(val key: String, val description: String)

// ─── Tab ─────────────────────────────────────────────────────────────────────
// Promoted to internal so MainViewModel can reference it without reflection.

internal enum class Tab(val label: String) {
    Audio("Audio"), Glyphs("Glyphs"), About("About");

    companion object {
        // Allocated once at class-load time; never re-allocated during recomposition.
        val all: List<Tab> = entries
    }
}

// ─── ViewModel ────────────────────────────────────────────────────────────────
//
// All mutable state lives here as MutableStateFlow so that:
//   • State survives configuration changes — no full UI rebuild on rotation.
//   • Collectors only recompose the subtree that reads a particular flow.
//   • All IO / CPU work is dispatched off the main thread.

internal class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val ctx = application

    // ── Tab ───────────────────────────────────────────────────────────────────
    private val _selectedTab = MutableStateFlow(Tab.Audio)
    val selectedTab = _selectedTab.asStateFlow()
    fun selectTab(tab: Tab) { _selectedTab.value = tab }

    // ── Device ────────────────────────────────────────────────────────────────
    // Exposed as MutableStateFlow (not just a val) so the Activity can always
    // read the latest device synchronously when binding the service.
    val selectedDevice = MutableStateFlow(DeviceProfile.DEVICE_NP2)

    // ── Latency ───────────────────────────────────────────────────────────────
    private val _latencyMs = MutableStateFlow(0)
    val latencyMs = _latencyMs.asStateFlow()
    fun setLatencyMs(value: Int) { _latencyMs.value = value }

    private val _latencyPresets = MutableStateFlow(listOf(10, 154, 300))
    val latencyPresets = _latencyPresets.asStateFlow()

    // ── Gamma ─────────────────────────────────────────────────────────────────
    private val _gammaValue = MutableStateFlow(AudioCaptureService.DEFAULT_GAMMA)
    val gammaValue = _gammaValue.asStateFlow()
    fun setGammaValue(value: Float) { _gammaValue.value = value }

    // ── Running state ─────────────────────────────────────────────────────────
    private val _runningState = MutableStateFlow(false)
    val runningState = _runningState.asStateFlow()
    fun setRunning(running: Boolean) { _runningState.value = running }

    // ── Presets ───────────────────────────────────────────────────────────────
    private val _selectedPreset = MutableStateFlow("")
    val selectedPreset = _selectedPreset.asStateFlow()
    fun currentPreset(): String = _selectedPreset.value
    fun setSelectedPreset(key: String) { if (key.isNotBlank()) _selectedPreset.value = key }

    private val _presetInfos = MutableStateFlow<List<AudioCaptureService.PresetInfo>>(emptyList())
    val presetInfos = _presetInfos.asStateFlow()

    // ── Init: all IO in parallel ──────────────────────────────────────────────

    init {
        viewModelScope.launch {
            // Device detection is CPU work — run on Default.
            val device = withContext(Dispatchers.Default) {
                DeviceProfile.detectDevice()
                    .takeIf { it != DeviceProfile.DEVICE_UNKNOWN }
                    ?: DeviceProfile.DEVICE_NP2
            }
            selectedDevice.value = device

            // Four independent SharedPreferences reads — run concurrently on IO.
            val gamma:   Float
            val latency: Int
            val presets: List<Int>
            val infos:   List<AudioCaptureService.PresetInfo>

            withContext(Dispatchers.IO) {
                val gammaD   = async { AudioCaptureService.loadGamma(ctx) }
                val latencyD = async { AudioCaptureService.loadLatencyCompensationMs(ctx, device) }
                val presetsD = async { AudioCaptureService.loadLatencyPresets(ctx) }
                val infosD   = async { AudioCaptureService.loadPresetInfos(ctx, device) }
                // All four execute in parallel; awaitAll is implicit via individual awaits.
                gamma   = gammaD.await()
                latency = latencyD.await()
                presets = presetsD.await()
                infos   = infosD.await()
            }
            // withContext resumes on the parent dispatcher (Main) — safe to write flows.
            _gammaValue.value     = gamma
            _latencyMs.value      = latency
            _latencyPresets.value = presets
            commitPresetInfos(infos)

            startRunningStatePoller()
        }
    }

    // ─── Off-thread service state polling ─────────────────────────────────────
    //
    // Previous code ran this on Dispatchers.Main (LaunchedEffect default), waking
    // the UI thread 2× per second for a comparison + potential state write.
    // Moving to Dispatchers.Default keeps the main thread completely idle when
    // nothing has changed.

    private fun startRunningStatePoller() {
        viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                delay(500L)
                val running = AudioCaptureService.isRunning()
                // Only emit when the value actually changes — prevents spurious
                // recompositions from a StateFlow that emits the same value.
                if (running != _runningState.value) _runningState.value = running
            }
        }
    }

    // ── Public helpers called by the Activity ─────────────────────────────────

    /** Reloads preset list from disk; safe to call from the main thread. */
    fun refreshPresets() {
        viewModelScope.launch(Dispatchers.IO) {
            val infos = AudioCaptureService.loadPresetInfos(ctx, selectedDevice.value)
            withContext(Dispatchers.Main.immediate) { commitPresetInfos(infos) }
        }
    }

    /** Persists latency compensation to SharedPreferences without blocking the main thread. */
    fun persistLatency(clamped: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            AudioCaptureService.saveLatencyCompensationMs(ctx, selectedDevice.value, clamped)
        }
    }

    /** Updates preset list in state and persists it; save is off main thread. */
    fun updateLatencyPresets(presets: List<Int>) {
        _latencyPresets.value = presets
        viewModelScope.launch(Dispatchers.IO) {
            AudioCaptureService.saveLatencyPresets(ctx, presets)
        }
    }

    /** Persists gamma to SharedPreferences without blocking the main thread. */
    fun persistGamma(clamped: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            AudioCaptureService.saveGamma(ctx, clamped)
        }
    }

    private fun commitPresetInfos(infos: List<AudioCaptureService.PresetInfo>) {
        _presetInfos.value = infos
        if (infos.none { it.key == _selectedPreset.value }) {
            _selectedPreset.value = infos.firstOrNull()?.key.orEmpty()
        }
    }
}

// ─── Activity ─────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    // viewModels() returns the same instance across configuration changes.
    private val viewModel: MainViewModel by viewModels()

    private val projectionManager by lazy {
        getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    }

    private var service: AudioCaptureService? = null
    private var bound = false
    private var pendingResultCode = 0
    private var pendingData: Intent? = null
    private var hasPendingToken = false

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, binder: IBinder) {
            Log.d("BetterViz", "Service connected: $name")
            service = (binder as AudioCaptureService.LocalBinder).service
            bound = true
            applyServiceSettings()
            if (hasPendingToken && pendingData != null) {
                val data = pendingData ?: return
                service?.startCapture(pendingResultCode, data)
                pendingResultCode = 0
                pendingData = null
                hasPendingToken = false
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            service = null
            bound = false
            // Use the lightweight static check; the poller will also catch any change.
            viewModel.setRunning(AudioCaptureService.isRunning())
        }
    }

    private val projectionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val data = result.data
            if (result.resultCode == Activity.RESULT_OK && data != null) {
                deliverProjectionToken(result.resultCode, data)
            } else {
                viewModel.setRunning(false)
                Toast.makeText(this, "Screen capture permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private val notificationLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) launchProjection()
            else {
                viewModel.setRunning(false)
                Toast.makeText(
                    this,
                    "Notifications are required while the visualizer is active",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            BetterVizTheme {
                // Collect each StateFlow independently. Compose only recomposes the
                // subtree(s) that actually read a value when it changes — collecting
                // them as separate `by` delegates achieves this granularity.
                val tab            by viewModel.selectedTab.collectAsStateWithLifecycle()
                val isRunning      by viewModel.runningState.collectAsStateWithLifecycle()
                val latencyMs      by viewModel.latencyMs.collectAsStateWithLifecycle()
                val latencyPresets by viewModel.latencyPresets.collectAsStateWithLifecycle()
                val gammaValue     by viewModel.gammaValue.collectAsStateWithLifecycle()
                val presets        by viewModel.presetInfos.collectAsStateWithLifecycle()
                val selectedPreset by viewModel.selectedPreset.collectAsStateWithLifecycle()

                BetterVizApp(
                    tab                     = tab,
                    onTabSelected           = viewModel::selectTab,
                    isRunning               = isRunning,
                    latencyMs               = latencyMs,
                    onLatencyChanged        = ::onLatencyChanged,
                    latencyPresets          = latencyPresets,
                    onLatencyPresetsChanged = viewModel::updateLatencyPresets,
                    gammaValue              = gammaValue,
                    onGammaChanged          = ::onGammaChanged,
                    presets                 = presets,
                    selectedPreset          = selectedPreset,
                    onPresetSelected        = ::onPresetSelected,
                    onToggleVisualizer      = ::toggleVisualizer,
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Single source of truth: push the real state into the ViewModel.
        // The poller will keep it in sync while the app is in the foreground.
        viewModel.setRunning(AudioCaptureService.isRunning())
    }

    override fun onDestroy() {
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
        super.onDestroy()
    }

    // ── Settings delegates ────────────────────────────────────────────────────
    // Each function: (1) clamps, (2) updates ViewModel state (triggers UI),
    // (3) persists to disk off main thread via ViewModel, (4) forwards to service.

    private fun onLatencyChanged(value: Int) {
        viewModel.setLatencyMs(value)
        viewModel.persistLatency(value)          // Dispatchers.IO — never blocks main
        service?.setLatencyCompensationMs(value)
    }

    private fun onGammaChanged(value: Float) {
        viewModel.setGammaValue(value)
        viewModel.persistGamma(value)            // Dispatchers.IO — never blocks main
        service?.setGamma(value)
    }

    private fun onPresetSelected(key: String) {
        viewModel.setSelectedPreset(key)
        service?.setPreset(key)
    }

    // ── Visualizer lifecycle ──────────────────────────────────────────────────

    private fun toggleVisualizer() {
        if (viewModel.runningState.value) {
            stopEverything()
            viewModel.setRunning(false)
            return
        }
        // refreshPresets is now async/IO — safe to call from main thread.
        if (viewModel.currentPreset().isBlank()) viewModel.refreshPresets()
        requestProjection()
    }

    private fun requestProjection() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }
        launchProjection()
    }

    private fun launchProjection() {
        projectionLauncher.launch(projectionManager.createScreenCaptureIntent())
    }

    private fun deliverProjectionToken(resultCode: Int, data: Intent) {
        val serviceIntent = Intent(this, AudioCaptureService::class.java).apply {
            putExtra(AudioCaptureService.EXTRA_PRESET_KEY, viewModel.currentPreset())
        }
        if (!bound) bindService(serviceIntent, serviceConnection, BIND_AUTO_CREATE)
        ContextCompat.startForegroundService(this, serviceIntent)
        if (bound && service != null) {
            applyServiceSettings()
            service?.startCapture(resultCode, data)
        } else {
            pendingResultCode = resultCode
            pendingData       = data
            hasPendingToken   = true
        }
        viewModel.setRunning(true)
        TileService.requestListeningState(
            this,
            ComponentName(this, VisualizerTileService::class.java)
        )
    }

    /** Reads latest values directly from ViewModel StateFlows — always current. */
    private fun applyServiceSettings() {
        service?.setDevice(viewModel.selectedDevice.value)
        service?.setLatencyCompensationMs(viewModel.latencyMs.value)
        service?.setGamma(viewModel.gammaValue.value)
        val preset = viewModel.currentPreset()
        if (preset.isNotBlank()) service?.setPreset(preset)
    }

    private fun stopEverything() {
        service?.stopCapture()
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
        service           = null
        pendingResultCode = 0
        pendingData       = null
        hasPendingToken   = false
        stopService(Intent(this, AudioCaptureService::class.java))
        TileService.requestListeningState(
            this,
            ComponentName(this, VisualizerTileService::class.java)
        )
    }
}

// ─── Root app composable ──────────────────────────────────────────────────────

@Composable
private fun BetterVizApp(
    tab: Tab,
    onTabSelected: (Tab) -> Unit,
    isRunning: Boolean,
    latencyMs: Int,
    onLatencyChanged: (Int) -> Unit,
    latencyPresets: List<Int>,
    onLatencyPresetsChanged: (List<Int>) -> Unit,
    gammaValue: Float,
    onGammaChanged: (Float) -> Unit,
    presets: List<AudioCaptureService.PresetInfo>,
    selectedPreset: String,
    onPresetSelected: (String) -> Unit,
    onToggleVisualizer: () -> Unit,
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        containerColor = Color.Black,
        floatingActionButton = {
            StartStopButton(
                running = isRunning,
                onClick = onToggleVisualizer,
                modifier = Modifier.padding(bottom = 8.dp, end = 8.dp),
            )
        },
        bottomBar = {
            NativeBottomBar(
                selectedTab = tab,
                onTabSelected = onTabSelected,
            )
        },
    ) { innerPadding ->
        // We use a Box here to "consume" the Scaffold padding once, 
        // preventing the AnimatedContent from jittering.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.Black)
        ) {
            AnimatedContent(
                targetState = tab,
                label = "tab_content",
                transitionSpec = {
                    val isMovingRight = targetState.ordinal > initialState.ordinal
                    val animationDuration = 400
                    val easing = EaseOut

                    if (isMovingRight) {
                        (slideInHorizontally(
                            initialOffsetX = { it },
                            animationSpec = tween(animationDuration, easing = easing)
                        ) + fadeIn(tween(animationDuration))) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { -it },
                                    animationSpec = tween(animationDuration, easing = easing)
                                ) + fadeOut(tween(animationDuration))
                    } else {
                        (slideInHorizontally(
                            initialOffsetX = { -it },
                            animationSpec = tween(animationDuration, easing = easing)
                        ) + fadeIn(tween(animationDuration))) togetherWith
                                slideOutHorizontally(
                                    targetOffsetX = { it },
                                    animationSpec = tween(animationDuration, easing = easing)
                                ) + fadeOut(tween(animationDuration))
                    }.using(SizeTransform(clip = false))
                },
                modifier = Modifier.fillMaxSize()
            ) { currentTab ->
                // Inside here, we no longer pass innerPadding down, 
                // as the parent Box is already handling it.
                when (currentTab) {
                    Tab.Audio -> AudioScreen(
                        isRunning = isRunning,
                        latencyMs = latencyMs,
                        onLatencyChanged = onLatencyChanged,
                        latencyPresets = latencyPresets,
                        onLatencyPresetsChanged = onLatencyPresetsChanged,
                    )

                    Tab.Glyphs -> GlyphsScreen(
                        gammaValue = gammaValue,
                        onGammaChanged = onGammaChanged,
                        presets = presets,
                        selectedPreset = selectedPreset,
                        onPresetSelected = onPresetSelected,
                    )

                    Tab.About -> AboutScreen()
                }
            }
        }
    }
}

// ─── Screens ──────────────────────────────────────────────────────────────────

@Composable
private fun AudioScreen(
    isRunning: Boolean,
    latencyMs: Int,
    onLatencyChanged: (Int) -> Unit,
    latencyPresets: List<Int>,
    onLatencyPresetsChanged: (List<Int>) -> Unit,
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 28.dp, vertical = 28.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ScreenTitle(text = "Better Nothing\nMusic Visualizer")
        BodyText(
            text = "To synchronize the Glyph Interface with your music, this app needs to capture " +
                    "process the device's real-time audio output. We use the Media Projection API " +
                    "to ensure a high-fidelity audio capture for the best visualization.\n\n" +
                    "Privacy Note: Don't be scared, we only utilize the audio stream. This app does " +
                    "not record or even view your screen content. Because we bypass video processing " +
                    "entirely, the app remains lightweight and avoids the battery drain associated " +
                    "with traditional screen recording."
        )
        AnimatedVisibility(visible = isRunning) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                BodyText(
                    text = "This latency compensation slider is for when you're using Bluetooth " +
                            "audio devices for example."
                )
                LatencyCard(
                    latencyMs               = latencyMs,
                    onLatencyChanged        = onLatencyChanged,
                    latencyPresets          = latencyPresets,
                    onLatencyPresetsChanged = onLatencyPresetsChanged,
                )
            }
        }
    }
}

@Composable
private fun GlyphsScreen(
    gammaValue: Float,
    onGammaChanged: (Float) -> Unit,
    presets: List<AudioCaptureService.PresetInfo>,
    selectedPreset: String,
    onPresetSelected: (String) -> Unit,
) {
    val scrollState = rememberScrollState()
    // derivedStateOf: re-computes only when selectedPreset or presets change,
    // not on every frame of a slider drag.
    val selectedInfo = remember(selectedPreset, presets) {
        presets.firstOrNull { it.key == selectedPreset } ?: presets.firstOrNull()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 28.dp, vertical = 28.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        ScreenTitle(text = "Glyph controls")
        Text(
            text = "Gamma control",
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFD2D2D2),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            GammaPreviewCard(gammaValue = gammaValue)
            BodyText(
                text = "A higher gamma value gives a more punchy look, but with less subtle details " +
                        "and overall brightness. A lower one is brighter but less punchy.",
                modifier = Modifier.weight(1f),
                size = 14.sp,
                lineHeight = 22.sp,
            )
        }
        GammaCard(gammaValue = gammaValue, onGammaChanged = onGammaChanged)
        Text(
            text = "Visualizer presets",
            modifier = Modifier.padding(top = 20.dp), // Adds space above the text
            style = MaterialTheme.typography.headlineMedium,
            color = Color(0xFFD2D2D2),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            presets.forEach { preset ->
                NativeFilterChip(
                    label    = preset.key,
                    selected = preset.key == selectedPreset,
                    onClick  = { onPresetSelected(preset.key) },
                )
            }
        }
        Card(
            shape  = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF242222)),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text     = selectedInfo?.description ?: "Text describing the preset in a nice way.",
                style    = MaterialTheme.typography.bodyLarge.copy(lineHeight = 22.sp),
                color    = Color(0xFFBABABA),
                modifier = Modifier.padding(20.dp),
            )
        }
        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
private fun AboutScreen() {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 28.dp, vertical = 28.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        ScreenTitle(text = "About & other")
        BodyText(
            text = "WE NEED A PROPER FUKCING ABOUT SCREEN!! PLACEHOLDER; Aleks Levet is honestly on another level when it comes to UI design, like the " +
                    "way he captures that clean, futuristic aesthetic inspired by NothingOS is " +
                    "actually insane. Every interface he touches feels intentional, minimal but " +
                    "never empty, detailed but never overwhelming."
        )
        Spacer(modifier = Modifier.height(28.dp))
    }
}

// ─── Reusable components ──────────────────────────────────────────────────────

@Composable
private fun ScreenTitle(text: String) {
    Text(
        text  = text,
        style = MaterialTheme.typography.displayLarge,
        color = Color.White,
    )
}

@Composable
private fun BodyText(
    text: String,
    modifier: Modifier = Modifier,
    size: TextUnit = 16.sp,
    lineHeight: TextUnit = 24.sp,
) {
    Text(
        text  = text,
        // Hoist TextStyle out of every recomposition; only reallocated when
        // size or lineHeight actually changes.
        style = remember(size, lineHeight) {
            TextStyle(
                fontSize   = size,
                lineHeight = lineHeight,
                fontWeight = FontWeight.Normal,
            )
        },
        color    = Color(0xFFB8B8B8),
        modifier = modifier,
    )
}

@Composable
private fun LatencyCard(
    latencyMs: Int,
    onLatencyChanged: (Int) -> Unit,
    latencyPresets: List<Int>,
    onLatencyPresetsChanged: (List<Int>) -> Unit,
) {
    var isEditingPresets by remember { mutableStateOf(false) }

    // SnapshotStateList: zero allocations during editing; mutations are tracked
    // by Compose without replacing the whole list reference.
    val editingPresets = remember { SnapshotStateList<String>() }

    Card(
        shape    = RoundedCornerShape(28.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF242222)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text  = "Latency adjust :",
                    color = Color(0xFFE6E1E3),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!isEditingPresets) {
                        latencyPresets.forEach { preset ->
                            NativeFilterChip(
                                label    = "${preset}ms",
                                selected = latencyMs == preset,
                                onClick  = { onLatencyChanged(preset) },
                            )
                        }
                        Button(
                            onClick = {
                                editingPresets.clear()
                                editingPresets.addAll(latencyPresets.map { it.toString() })
                                isEditingPresets = true
                            },
                            modifier        = Modifier.height(36.dp),
                            colors          = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF403F44),
                                contentColor   = Color(0xFFE6E0EB)
                            ),
                            contentPadding  = PaddingValues(4.dp),
                        ) {
                            Text("Edit", style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        editingPresets.forEachIndexed { index, value ->
                            OutlinedTextField(
                                value         = value,
                                onValueChange = { editingPresets[index] = it },
                                modifier      = Modifier.width(60.dp).height(40.dp),
                                textStyle     = TextStyle(fontSize = 12.sp),
                                singleLine    = true,
                            )
                        }
                        Button(
                            onClick = {
                                try {
                                    val newPresets = editingPresets
                                        .mapNotNull { it.toIntOrNull() }
                                        .filter { it in 0..300 }
                                    if (newPresets.isNotEmpty()) onLatencyPresetsChanged(newPresets)
                                } finally {
                                    isEditingPresets = false
                                }
                            },
                            modifier       = Modifier.height(36.dp),
                            colors         = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFB5F2B6),
                                contentColor   = Color(0xFF1C5A21)
                            ),
                            contentPadding = PaddingValues(8.dp),
                        ) {
                            Text("Save", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            ExpressiveSlider(
                modifier   = Modifier.fillMaxWidth(),
                value      = latencyMs.toFloat(),
                onValueChange = { onLatencyChanged(it.toInt()) },
                valueRange = 0f..300f,
            )
        }
    }
}

@Composable
private fun GammaCard(
    gammaValue: Float,
    onGammaChanged: (Float) -> Unit,
) {
    // Format only when gammaValue changes, not every recomposition.
    val gammaLabel = remember(gammaValue) {
        "Light Gamma: ${"%.2f".format(gammaValue)}"
    }

    Card(
        shape    = RoundedCornerShape(28.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF242222)),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(17.dp),
        ) {
            Text(
                text     = gammaLabel,
                color    = Color(0xFFE8E0EC),
                style    = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
            ExpressiveSlider(
                modifier      = Modifier.fillMaxWidth(),
                value         = gammaValue,
                onValueChange = onGammaChanged,
                valueRange    = 0.4f..3.5f,
            )
        }
    }
}

@Composable
private fun GammaPreviewCard(gammaValue: Float) {
    val animatedGamma by animateFloatAsState(
        targetValue  = gammaValue,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow,
        ),
        label = "gamma_curve",
    )

    // Allocate the Path once; reset() and refill it on each draw call.
    val curvePath = remember { Path() }

    Card(
        shape    = RoundedCornerShape(28.dp),
        colors   = CardDefaults.cardColors(containerColor = Color(0xFF242222)),
        modifier = Modifier.size(130.dp, 130.dp),
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(18.dp)) {
            val gridColor = Color(0xFF4C494C)
            val accent    = Color(0xFFE6E0EB)
            val pad       = 8f
            val left   = pad
            val top    = pad
            val right  = size.width - pad
            val bottom = size.height - pad
            val w = right - left
            val h = bottom - top

            drawLine(gridColor, Offset(left, bottom), Offset(right, bottom), strokeWidth = 4f, cap = StrokeCap.Round)
            drawLine(gridColor, Offset(left, bottom), Offset(left, top),    strokeWidth = 4f, cap = StrokeCap.Round)

            val hStep = h / 4f
            val vStep = w / 4f
            repeat(3) { i ->
                drawLine(gridColor, Offset(left,         bottom - hStep * (i + 1)), Offset(right, bottom - hStep * (i + 1)), strokeWidth = 1f)
                drawLine(gridColor, Offset(left + vStep * (i + 1), bottom),         Offset(left + vStep * (i + 1), top),     strokeWidth = 1f)
            }

            curvePath.reset()
            curvePath.moveTo(left, bottom)
            val steps = 20
            for (step in 1..steps) {
                val x = step / steps.toFloat()
                val y = x.pow(animatedGamma)
                curvePath.lineTo(left + x * w, bottom - y * h)
            }
            drawPath(curvePath, accent, style = Stroke(width = 8f, cap = StrokeCap.Round))
        }
    }
}

@Composable
private fun NativeFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = selected,
        onClick  = onClick,
        label    = { Text(text = label, style = MaterialTheme.typography.labelLarge) },
        colors   = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Color(0xFFD8D3DA),
            selectedLabelColor     = Color(0xFF1E1B20),
            containerColor         = Color(0xFF5A565A),
            labelColor             = Color(0xFFE7E0E7),
        ),
        border   = FilterChipDefaults.filterChipBorder(
            enabled             = true,
            selected            = selected,
            borderColor         = Color.Transparent,
            selectedBorderColor = Color.Transparent,
        ),
    )
}

@Composable
private fun StartStopButton(
    running: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed         by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.9f else 1.1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessLow
        ),
        label = "buttonScale"
    )

    val containerColor by animateColorAsState(
        targetValue   = if (running) Color(0xFFE53935) else Color(0xFFB5F2B6),
        animationSpec = tween(600, easing = EaseInOutCubic),
        label         = "containerColor"
    )

    val contentColor by animateColorAsState(
        targetValue   = if (running) Color.White else Color(0xFF1C5A21),
        animationSpec = tween(600, easing = EaseInOutCubic),
        label         = "contentColor"
    )

    FloatingActionButton(
        onClick           = onClick,
        interactionSource = interactionSource,
        shape             = RoundedCornerShape(15.dp),
        modifier          = modifier
            .graphicsLayer(scaleX = scale, scaleY = scale)
            .height(56.dp)
            .padding(5.dp),
        containerColor = containerColor,
        contentColor   = contentColor,
    ) {
        Row(
            modifier             = Modifier.padding(horizontal = 16.dp),
            verticalAlignment    = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnimatedContent(
                targetState  = running,
                transitionSpec = { (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut()) },
                label        = "iconTransition"
            ) { isRunning ->
                Icon(
                    imageVector     = if (isRunning) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    modifier        = Modifier.size(24.dp)
                )
            }
            Text(
                text  = if (running) "Stop visualizer" else "Start visualizer",
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight    = FontWeight.Medium,
                    letterSpacing = 0.5.sp
                )
            )
        }
    }
}

@Composable
private fun NativeBottomBar(
    selectedTab: Tab,
    onTabSelected: (Tab) -> Unit,
) {
    val tabs = Tab.all

    NavigationBar(
        modifier = Modifier.height(64.dp), // The magic number
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        windowInsets = WindowInsets(0) // Prevents extra padding on gesture-nav phones
    ) {
        tabs.forEach { tab ->
            val isSelected = tab == selectedTab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                label = {
                    Text(
                        text = tab.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                },
                icon = {
                    Icon(
                        imageVector = when (tab) {
                            Tab.Audio -> Icons.AutoMirrored.Filled.VolumeUp
                            Tab.Glyphs -> Icons.Filled.Settings
                            Tab.About -> Icons.Filled.Info
                        },
                        contentDescription = tab.label
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                    selectedIconColor = MaterialTheme.colorScheme.onBackground,
                    selectedTextColor = MaterialTheme.colorScheme.onBackground,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    modifier: Modifier = Modifier
) {
    // Must be remembered — a new object every recompose breaks press tracking.
    val interactionSource = remember { MutableInteractionSource() }

    Slider(
        value             = value,
        onValueChange     = onValueChange,
        valueRange        = valueRange,
        interactionSource = interactionSource,
        modifier          = modifier.height(48.dp),
        thumb = {
            Spacer(
                modifier = Modifier
                    .size(width = 4.dp, height = 44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onBackground,
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        },
        track = { sliderState ->
            SliderDefaults.Track(
                sliderState          = sliderState,
                modifier             = Modifier.height(16.dp),
                thumbTrackGapSize    = 4.dp,
                trackInsideCornerSize = 2.dp,
                colors               = SliderDefaults.colors(
                    activeTrackColor   = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    )
}

val NDotFontFamily = FontFamily(
    Font(resId = R.font.ndot57, weight = FontWeight.Normal)
    // If ndot55 is your lighter variant, you could add it here as FontWeight.Light
)

val NDot55FontFamily = FontFamily(
    Font(resId = R.font.ndot55, weight = FontWeight.Normal)
    // If ndot55 is your lighter variant, you could add it here as FontWeight.Light
)

@Composable
private fun BetterVizTheme(content: @Composable () -> Unit) {
    val typography = Typography(
        // HEADERS
        displayLarge = TextStyle(
            fontFamily = NDot55FontFamily,
            fontSize = 45.sp,
            lineHeight = 55.sp,
            fontWeight = FontWeight.Normal
        ),
        headlineMedium = TextStyle(
            fontFamily = NDotFontFamily,
            fontSize = 30.sp,
            lineHeight = 40.sp,
            fontWeight = FontWeight.Normal
        ),

        // SUB-HEADERS
        titleLarge = TextStyle(
            fontSize = 21.sp,
            lineHeight = 28.sp,
            fontWeight = FontWeight.Normal
        ),
        titleMedium = TextStyle(
            fontSize = 17.sp,
            lineHeight = 24.sp,
            fontWeight = FontWeight.Normal
        ),

        // BODY & LABELS (Keep system font for high legibility at small sizes)
        bodyLarge = TextStyle(fontSize = 16.sp, lineHeight = 24.sp, fontWeight = FontWeight.Normal),
        labelLarge = TextStyle(fontSize = 13.sp, lineHeight = 18.sp, fontWeight = FontWeight.Medium),
        labelMedium = TextStyle(fontSize = 12.sp, lineHeight = 16.sp, fontWeight = FontWeight.Medium),
    )

    MaterialTheme(
        colorScheme = darkColorScheme(
            background = Color.Black,
            surface = Color(0xFF242222),
            primary = Color(0xFFD8D3DA),
            secondary = Color(0xFFB5F2B6),
            onBackground = Color.White,
            onSurface = Color.White,
            onPrimary = Color(0xFF1C1A1D),
            surfaceVariant = Color(0xFF3D3C41),
        ),
        shapes = Shapes(
            extraLarge = RoundedCornerShape(32.dp),
            large = RoundedCornerShape(28.dp),
            medium = RoundedCornerShape(20.dp),
            small = RoundedCornerShape(14.dp),
        ),
        typography = typography,
        content = content,
    )
}