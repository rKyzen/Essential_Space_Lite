package com.better.nothing.music.vizualizer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.util.Log;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;

import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Real-time Nothing glyph visualizer driven by zones.config.
 * The Java port now follows the same high-level pipeline as musicViz.py:
 *   FFT -> unique frequency peaks -> per-frequency decay ->
 *   overlapping zone mapping -> quadratic normalization ->
 *   optional percent slice mapping -> glyph output
 * The only intentional runtime difference is normalization: the Python script
 * can normalize against the whole track, while the live service uses a rolling
 * per-zone peak because future frames are not known yet.
 */

public class AudioCaptureService extends Service {

    private static final String TAG = "GlyphViz:Service";
    private static final String CHANNEL_ID = "glyph_viz_channel";
    private static final int NOTIF_ID = 1;

    public static final String EXTRA_PRESET_KEY = "preset_key";
    public static final float DEFAULT_GAMMA = 2f;
    private static final String PREFS_NAME = "glyph_visualizer_prefs";
    private static final String PREF_GAMMA = "gamma";
    private static final String PREF_LATENCY_PREFIX = "latency_device_";
    private static final String PREF_LATENCY_PRESETS = "latency_presets";
    private static final String DEFAULT_PRESET_KEY = "np1s";
    private static final String PHONE_MODEL_UNKNOWN = "UNKNOWN";
    private static final String PHONE_MODEL_PHONE1 = "PHONE1";
    private static final String PHONE_MODEL_PHONE2 = "PHONE2";
    private static final String PHONE_MODEL_PHONE2A = "PHONE2A";
    private static final String PHONE_MODEL_PHONE3A = "PHONE3A";
    private static final String PHONE_MODEL_PHONE3 = "PHONE3";
    private static final String PHONE_MODEL_PHONE4A = "PHONE4A";

    private static final int SAMPLE_RATE = 44100;
    private static final int FPS = 60;
    private static final int HOP = Math.round(SAMPLE_RATE / (float) FPS);
    private static final int ANALYSIS_WINDOW = roundHalfEvenToInt(SAMPLE_RATE * 0.025d);
    private static final int FFT_SIZE = nextPowerOfTwo(ANALYSIS_WINDOW);
    private static final float HZ_PER_BIN = (float) SAMPLE_RATE / FFT_SIZE;

    private static final float PEAK_FALLOFF = 0.9995f;
    private static final float PYTHON_FREQ_MULTIPLIER = 4f;
    private static final float EPSILON = 0.000001f;
    private static final long MIN_SEND_MS = 16L;
    private static volatile boolean sIsRunning = false;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private final IBinder mBinder = new LocalBinder();

    private GlyphManager mGM;
    private boolean mSessionOpen = false;

    private MediaProjection mProjection;
    private AudioRecord mAudioRecord;
    private ExecutorService mExecutor;
    private volatile boolean mCapturing = false;

    private volatile VisualizerConfig mVisualizerConfig;
    private String mPresetKey = DEFAULT_PRESET_KEY;
    private String mDetectedPhoneModel = PHONE_MODEL_UNKNOWN;
    private List<String> mAvailablePresetKeys = Collections.emptyList();
    private int mSelectedDevice = DeviceProfile.DEVICE_UNKNOWN;
    private volatile int mLatencyCompensationMs = 0;
    private volatile int mLatencySettingsVersion = 0;
    private volatile int mPresetConfigVersion = 0;
    private volatile float mGamma = DEFAULT_GAMMA;

    private float[] mCurrentLightState = new float[0];
    private float[] mZonePeaks = new float[0];
    private float[] mDecayedFrequencyState = new float[0];
    private int mLastHash = -999;
    private long mLastSendMs = 0L;

    private final GlyphManager.Callback mGlyphCallback = new GlyphManager.Callback() {
        @Override
        public void onServiceConnected(ComponentName cn) {
            Log.d(TAG, "Glyph connected");
            if (Common.is22111()) {
                mGM.register(com.nothing.ketchum.Glyph.DEVICE_22111);
            } else if (Common.is20111()) {
                mGM.register(com.nothing.ketchum.Glyph.DEVICE_20111);
            } else if (Common.is23111()) {
                mGM.register(com.nothing.ketchum.Glyph.DEVICE_23111);
            } else if (Common.is23113()) {
                mGM.register(com.nothing.ketchum.Glyph.DEVICE_23113);
            } else if (Common.is24111()) {
                mGM.register(com.nothing.ketchum.Glyph.DEVICE_24111);
            } else {
                mGM.register(com.nothing.ketchum.Glyph.DEVICE_25111);  // Phone 4a noice
            }
            try {
                mGM.openSession();
                mSessionOpen = true;
                Log.d(TAG, "Session open");
            } catch (GlyphException e) {
                Log.e(TAG, "Session fail: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName cn) {
            mSessionOpen = false;
        }
    };

    public class LocalBinder extends Binder {
        public AudioCaptureService getService() {
            return AudioCaptureService.this;
        }
    }

    private record ZoneSpec(float lowHz, float highHz, float lowPercent, float highPercent) {

        boolean hasPercentSlice() {
                return !Float.isNaN(lowPercent) && !Float.isNaN(highPercent);
            }
        }

    private static final class FrequencyRange {
        final float lowHz;
        final float highHz;
        final int binLo;
        final int binHi;

        FrequencyRange(float lowHz, float highHz) {
            this.lowHz = lowHz;
            this.highHz = highHz;
            this.binLo = Math.max(0, (int) Math.ceil(lowHz / HZ_PER_BIN));
            this.binHi = Math.max(binLo, Math.min(FFT_SIZE / 2, (int) Math.floor(highHz / HZ_PER_BIN)));
        }
    }

    private record VisualizerConfig(String presetKey, String description, float decay,
                                    ZoneSpec[] zones, FrequencyRange[] uniqueRanges,
                                    int[][] zoneToRangeIndices) {
    }

    private record PendingFrame(float[] uniquePeaks, VisualizerConfig config, int configVersion,
                                long dueAtMs) {
    }

    public static final class PresetInfo {
        public final String key;
        public final String description;

        PresetInfo(String key, String description) {
            this.key = key;
            this.description = description;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind() called");
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Service onCreate()");
        mSelectedDevice = DeviceProfile.detectDevice();
        mLatencyCompensationMs = loadLatencyCompensationMs(this, mSelectedDevice);
        mGamma = loadGamma(this);
        Log.d(TAG, "Detected device: " + mSelectedDevice + ", loaded settings: latency=" + mLatencyCompensationMs + "ms, gamma=" + mGamma);
        try {
            refreshPresetCatalog();
            if (mAvailablePresetKeys.isEmpty()) {
                throw new JSONException("No presets available in zones.config");
            }
            if (!mAvailablePresetKeys.contains(mPresetKey)) {
                mPresetKey = chooseDefaultPresetKey(mDetectedPhoneModel, mAvailablePresetKeys);
            }
            mVisualizerConfig = loadVisualizerConfig(mPresetKey);
            resetVisualizerState();
            Log.d(TAG, "Detected " + mDetectedPhoneModel + ", loaded preset " + mPresetKey + " with " + mVisualizerConfig.zones.length + " zones");
        } catch (Exception e) {
            Log.e(TAG, "Failed to load zones.config: " + e.getMessage(), e);
        }

        mGM = GlyphManager.getInstance(getApplicationContext());
        mGM.init(mGlyphCallback);
        Log.d(TAG, "GlyphManager initialized");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand() flags=" + flags + " startId=" + startId);
        String requestedPreset = intent != null ? intent.getStringExtra(EXTRA_PRESET_KEY) : null;
        if (requestedPreset != null && !requestedPreset.trim().isEmpty()) {
            Log.d(TAG, "Preset requested: " + requestedPreset);
            setPreset(requestedPreset.trim());
        }
        startForeground(NOTIF_ID, buildNotification());
        Log.d(TAG, "Started as foreground service");
        return START_NOT_STICKY;
    }

    public static List<PresetInfo> loadPresetInfos(Context context, int device) {
        String detectedPhoneModel = detectPhoneModel();
        String selectedPhoneModel = phoneModelForDevice(device);
        String phoneModelForCatalog = PHONE_MODEL_UNKNOWN.equals(selectedPhoneModel)
                ? detectedPhoneModel
                : selectedPhoneModel;

        try {
            JSONObject root = loadZonesConfigRoot(context);
            List<String> matching = getPresetKeysForPhoneModel(root, phoneModelForCatalog);
            if (matching.isEmpty() && !PHONE_MODEL_UNKNOWN.equals(detectedPhoneModel)) {
                matching = getPresetKeysForPhoneModel(root, detectedPhoneModel);
            }
            if (matching.isEmpty()) {
                matching = getAllPresetKeys(root);
            }
            return buildPresetInfos(root, matching);
        } catch (FileNotFoundException e) {
            List<PresetInfo> builtIn = getBuiltInPresetInfosForPhoneModel(phoneModelForCatalog);
            if (builtIn.isEmpty() && !PHONE_MODEL_UNKNOWN.equals(detectedPhoneModel)) {
                builtIn = getBuiltInPresetInfosForPhoneModel(detectedPhoneModel);
            }
            if (builtIn.isEmpty()) {
                builtIn = getAllBuiltInPresetInfos();
            }
            return builtIn;
        } catch (Exception e) {
            Log.e(TAG, "Failed to load preset catalog", e);
            return getAllBuiltInPresetInfos();
        }
    }

    public static boolean isRunning() {
        return sIsRunning;
    }

    public static int loadLatencyCompensationMs(Context context, int device) {
        return  getPreferences(context).getInt(latencyPreferenceKey(device), 0);
    }

    public static void saveLatencyCompensationMs(Context context, int device, int latencyMs) {
        getPreferences(context)
                .edit()
                .putInt(latencyPreferenceKey(device), latencyMs)
                .apply();
    }

    public static float loadGamma(Context context) {
        return getPreferences(context).getFloat(PREF_GAMMA, DEFAULT_GAMMA);
    }

    public static void saveGamma(Context context, float gamma) {
        getPreferences(context)
                .edit()
                .putFloat(PREF_GAMMA, gamma)
                .apply();
    }

    public static List<Integer> loadLatencyPresets(Context context) {
        String saved = getPreferences(context).getString(PREF_LATENCY_PRESETS, null);
        if (saved == null || saved.isEmpty()) {
            return new ArrayList<>(Arrays.asList(10, 154, 300));
        }
        ArrayList<Integer> presets = new ArrayList<>();
        try {
            String[] parts = saved.split(",");
            for (String part : parts) {
                presets.add(Integer.parseInt(part.trim()));
            }
        } catch (Exception e) {
            return new ArrayList<>(Arrays.asList(10, 154, 300));
        }
        return presets;
    }

    public static void saveLatencyPresets(Context context, List<Integer> presets) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < presets.size(); i++) {
            if (i > 0) sb.append(",");
            sb.append(presets.get(i));
        }
        getPreferences(context)
                .edit()
                .putString(PREF_LATENCY_PRESETS, sb.toString())
                .apply();
    }

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static String latencyPreferenceKey(int device) {
        return PREF_LATENCY_PREFIX + Math.max(DeviceProfile.DEVICE_UNKNOWN, device);
    }

    public void setPreset(String presetSelection) {
        if (presetSelection == null || presetSelection.trim().isEmpty()) {
            return;
        }
        String trimmedSelection = presetSelection.trim();
        applyPresetSelection(trimmedSelection);
    }

    public void setDevice(int device) {
        Log.d(TAG, "setDevice: " + device);
        mSelectedDevice = device;
        int newLatency = loadLatencyCompensationMs(this, device);
        Log.d(TAG, "Device " + device + " loaded with latency=" + newLatency + "ms");
        setLatencyCompensationMs(newLatency);
    }

    public void setLatencyCompensationMs(int latencyMs) {
        if (mLatencyCompensationMs != latencyMs) {
            mLatencyCompensationMs = latencyMs;
            mLatencySettingsVersion++;
        }
    }

    public void setGamma(float gamma) {
        if (mGamma != gamma)  mGamma = gamma;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy() called");
        sIsRunning = false;
        stopCapture();
        if (mSessionOpen) {
            try {
                Log.d(TAG, "Closing GlyphManager session");
                mGM.closeSession();
                Log.d(TAG, "GlyphManager session closed");
            } catch (GlyphException ignored) {
                Log.w(TAG, "Exception closing session: " + ignored.getMessage());
            }
        }
        if (mGM != null) {
            Log.d(TAG, "Un-initializing GlyphManager");
            mGM.unInit();
            Log.d(TAG, "GlyphManager uninitialized");
        }
        Log.d(TAG, "Service onDestroy() complete");
        super.onDestroy();
    }

    public void startCapture(int resultCode, Intent data) {
        if (mCapturing) {
            stopCapture();
        }
        if (mVisualizerConfig == null) {
            Log.e(TAG, "Cannot start capture without a parsed zones.config preset");
            sIsRunning = false;
            return;
        }

        MediaProjectionManager pm =
                (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);
        if (pm == null) {
            Log.e(TAG, "MediaProjectionManager is null");
            sIsRunning = false;
            return;
        }
        
        mProjection = pm.getMediaProjection(resultCode, data);
        if (mProjection == null) {
            Log.e(TAG, "Null projection - user likely denied screen capture permission");
            sIsRunning = false;
            return;
        }
        
        mProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                Log.d(TAG, "MediaProjection stopped");
                stopCapture();
                stopSelf();
            }
        }, mHandler);

        mCapturing = true;
        mExecutor = Executors.newSingleThreadExecutor();
        mExecutor.submit(this::captureLoop);
        sIsRunning = true;
        Log.d(TAG, "startCapture OK - capture loop started");
    }

    public void stopCapture() {
        Log.d(TAG, "stopCapture() called, mCapturing=" + mCapturing);
        mCapturing = false;
        sIsRunning = false;
        Log.d(TAG, "Stopped recording flags");
        
        if (mAudioRecord != null) {
            Log.d(TAG, "Releasing AudioRecord");
            AudioRecord audioRecord = mAudioRecord;
            mAudioRecord = null;
            try {
                audioRecord.stop();
                Log.d(TAG, "AudioRecord stopped");
            } catch (Exception ignored) {
                Log.w(TAG, "Exception stopping AudioRecord: " + ignored.getMessage());
            }
            audioRecord.release();
            Log.d(TAG, "AudioRecord released");
        }
        
        if (mProjection != null) {
            Log.d(TAG, "Stopping MediaProjection");
            MediaProjection projection = mProjection;
            mProjection = null;
            projection.stop();
            Log.d(TAG, "MediaProjection stopped");
        }
        
        if (mExecutor != null) {
            Log.d(TAG, "Shutting down executor");
            ExecutorService executor = mExecutor;
            mExecutor = null;
            executor.shutdownNow();
            Log.d(TAG, "Executor shut down");
        }
        
        Log.d(TAG, "Resetting visualizer state");
        resetVisualizerState();
        mHandler.post(() -> {
            Log.d(TAG, "Turning off glyphs");
            turnOffGlyphsManually();
        });
        Log.d(TAG, "stopCapture complete");
    }

    private void turnOffGlyphsManually() {
        if (mGM == null || !mSessionOpen) {
            return;
        }

        int glyphCount = resolveGlyphCount();
        if (glyphCount > 0) {
            try {
                mGM.setFrameColors(new int[glyphCount]);
            } catch (Exception e) {
                Log.w(TAG, "Failed to push zeroed frame colors while stopping", e);
            }
        }

        try {
            mGM.turnOff();
        } catch (Exception e) {
            Log.w(TAG, "turnOff failed while stopping", e);
        }
    }

    private int resolveGlyphCount() {
        if (mVisualizerConfig != null) {
            return mVisualizerConfig.zones.length;
        }
        if (mCurrentLightState.length > 0) {
            return mCurrentLightState.length;
        }
        return switch (mSelectedDevice) {
            case DeviceProfile.DEVICE_NP1 -> 15;
            case DeviceProfile.DEVICE_NP2 -> 33;
            case DeviceProfile.DEVICE_NP2A -> 26;
            case DeviceProfile.DEVICE_NP3A -> 36;
            case DeviceProfile.DEVICE_NP4A -> 6;
            default -> 0;
        };
    }

    private void captureLoop() {
        if (mVisualizerConfig == null) {
            Log.e(TAG, "captureLoop aborted: config missing");
            stopSelf();
            return;
        }

        AudioPlaybackCaptureConfiguration cfg =
                new AudioPlaybackCaptureConfiguration.Builder(mProjection)
                        .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                        .addMatchingUsage(AudioAttributes.USAGE_GAME)
                        .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                        .build();

        int minBuf = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
        );

        mAudioRecord = new AudioRecord.Builder()
                .setAudioPlaybackCaptureConfig(cfg)
                .setAudioFormat(new AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setChannelMask(AudioFormat.CHANNEL_IN_MONO)
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .build())
                .setBufferSizeInBytes(Math.max(minBuf, FFT_SIZE * 4))
                .build();

        if (mAudioRecord.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(TAG, "AudioRecord init failed");
            stopSelf();
            return;
        }

        mAudioRecord.startRecording();
        mCapturing = true;
        VisualizerConfig initialConfig = mVisualizerConfig;
        if (initialConfig != null) {
            Log.d(TAG, "Capture started using " + initialConfig.presetKey + " with " + initialConfig.zones.length + " zones");
        }

        float[] hann = buildHannWindow(ANALYSIS_WINDOW);
        float[] ring = new float[ANALYSIS_WINDOW];
        short[] hop = new short[HOP];
        float[] re = new float[FFT_SIZE];
        float[] im = new float[FFT_SIZE];
        float[] mag = new float[(FFT_SIZE / 2) + 1];
        ArrayDeque<PendingFrame> pendingFrames = new ArrayDeque<>();
        int appliedLatencyVersion = mLatencySettingsVersion;
        int appliedPresetVersion = mPresetConfigVersion;
        int rPos = 0;
        int filled = 0;

        while (mCapturing) {
            int currentPresetVersion = mPresetConfigVersion;
            VisualizerConfig config = mVisualizerConfig;
            if (config == null) {
                continue;
            }

            int read = mAudioRecord.read(hop, 0, HOP);
            if (read <= 0) {
                continue;
            }

            for (int i = 0; i < read; i++) {
                ring[rPos] = hop[i] / 32768f;
                rPos = (rPos + 1) % ANALYSIS_WINDOW;
            }
            filled = Math.min(filled + read, ANALYSIS_WINDOW);
            if (filled < ANALYSIS_WINDOW) {
                continue;
            }

            Arrays.fill(re, 0f);
            Arrays.fill(im, 0f);
            for (int i = 0; i < ANALYSIS_WINDOW; i++) {
                re[i] = ring[(rPos + i) % ANALYSIS_WINDOW] * hann[i];
            }
            fft(re, im, FFT_SIZE);

            for (int k = 0; k <= FFT_SIZE / 2; k++) {
                mag[k] = (float) Math.sqrt((re[k] * re[k]) + (im[k] * im[k]));
            }

            float[] uniquePeaks = new float[config.uniqueRanges.length];
            for (int fi = 0; fi < config.uniqueRanges.length; fi++) {
                FrequencyRange range = config.uniqueRanges[fi];
                float peak = 0f;
                for (int bin = range.binLo; bin <= range.binHi; bin++) {
                    if (mag[bin] > peak) {
                        peak = mag[bin];
                    }
                }
                uniquePeaks[fi] = peak;
            }

            if (currentPresetVersion != mPresetConfigVersion || config != mVisualizerConfig) {
                pendingFrames.clear();
                appliedPresetVersion = mPresetConfigVersion;
                continue;
            }

            if (appliedLatencyVersion != mLatencySettingsVersion
                    || appliedPresetVersion != currentPresetVersion) {
                pendingFrames.clear();
                appliedLatencyVersion = mLatencySettingsVersion;
                appliedPresetVersion = currentPresetVersion;
            }

            long dueAtMs = SystemClock.elapsedRealtime() + mLatencyCompensationMs;
            pendingFrames.addLast(new PendingFrame(uniquePeaks, config, currentPresetVersion, dueAtMs));
            dispatchDueFrames(pendingFrames);
        }
        Log.d(TAG, "Loop ended");
    }

    private void processFrame(float[] uniquePeaks, VisualizerConfig config, int configVersion) {
        if (!mSessionOpen || mGM == null || config == null || configVersion != mPresetConfigVersion) {
            return;
        }

        long now = System.currentTimeMillis();
        if (now - mLastSendMs < MIN_SEND_MS) {
            return;
        }
        mLastSendMs = now;

        ensureStateArrays(config.zones.length, config.uniqueRanges.length);

        float[] nextLightState = computeNextLightState(uniquePeaks, config);
        System.arraycopy(nextLightState, 0, mCurrentLightState, 0, nextLightState.length);

        int[] frameColors = buildFrameColors(nextLightState, config.zones.length);

        int hash = Arrays.hashCode(frameColors);
        if (hash == mLastHash) {
            return;
        }
        mLastHash = hash;

        try {
            mGM.setFrameColors(frameColors);
        } catch (Exception e) {
            Log.w(TAG, "setFrameColors failed", e);
        }
    }

    private int[] buildFrameColors(float[] normalizedLightState, int expectedLength) {
        int[] frameColors = new int[expectedLength];
        int count = Math.min(normalizedLightState.length, expectedLength);
        for (int i = 0; i < count; i++) {
            float gammaAdjusted = applyGamma(normalizedLightState[i]);
            frameColors[i] = Math.round(gammaAdjusted * 4095f);
        }
        return frameColors;
    }

    private void dispatchDueFrames(ArrayDeque<PendingFrame> pendingFrames) {
        long nowMs = SystemClock.elapsedRealtime();
        while (!pendingFrames.isEmpty()) {
            PendingFrame pendingFrame = pendingFrames.peekFirst();
            if (pendingFrame == null || pendingFrame.dueAtMs > nowMs) {
                return;
            }
            pendingFrames.removeFirst();
            final float[] peaksForFrame = pendingFrame.uniquePeaks;
            final VisualizerConfig configForFrame = pendingFrame.config;
            final int configVersionForFrame = pendingFrame.configVersion;
            mHandler.post(() -> processFrame(peaksForFrame, configForFrame, configVersionForFrame));
        }
    }

    private float applyGamma(float normalizedValue) {
        if (normalizedValue <= 0f) {
            return 0f;
        }
        return ((float) Math.pow(normalizedValue, mGamma));
    }

    private float[] computeNextLightState(float[] uniquePeaks, VisualizerConfig config) {
        float[] decayedFrequencyState = computeDecayedFrequencyState(uniquePeaks, config);
        float[] nextState = new float[config.zones.length];

        for (int zoneIndex = 0; zoneIndex < config.zones.length; zoneIndex++) {
            float rawZonePeak = 0f;
            int[] overlappingRanges = config.zoneToRangeIndices[zoneIndex];
            for (int rangeIndex : overlappingRanges) {
                if (rangeIndex >= 0 && rangeIndex < decayedFrequencyState.length) {
                    rawZonePeak = Math.max(rawZonePeak, decayedFrequencyState[rangeIndex]);
                }
            }

            mZonePeaks[zoneIndex] = Math.max(rawZonePeak, mZonePeaks[zoneIndex] * PEAK_FALLOFF);
            if (mZonePeaks[zoneIndex] < EPSILON) {
                mZonePeaks[zoneIndex] = EPSILON;
            }

            float normalized = (rawZonePeak / mZonePeaks[zoneIndex]);
            float quadratic = normalized * normalized;
            float mapped = applyPercentSlice(quadratic, config.zones[zoneIndex]);
            nextState[zoneIndex] = mapped < EPSILON ? 0f : mapped;
        }

        return nextState;
    }

    private float[] computeDecayedFrequencyState(float[] uniquePeaks, VisualizerConfig config) {
        float[] next = new float[mDecayedFrequencyState.length];
        for (int i = 0; i < next.length; i++) {
            float current = (i < uniquePeaks.length ? uniquePeaks[i] : 0f) * PYTHON_FREQ_MULTIPLIER;
            float risen = Math.max(mDecayedFrequencyState[i], current);
            float decayed = (config.decay * risen) + ((1f - config.decay) * current);
            next[i] = decayed < EPSILON ? 0f : decayed;
        }
        System.arraycopy(next, 0, mDecayedFrequencyState, 0, next.length);
        return next;
    }

    private void ensureStateArrays(int zoneCount, int uniqueRangeCount) {
        if (mCurrentLightState.length == zoneCount
                && mZonePeaks.length == zoneCount
                && mDecayedFrequencyState.length == uniqueRangeCount) {
            return;
        }
        mCurrentLightState = new float[zoneCount];
        mZonePeaks = new float[zoneCount];
        Arrays.fill(mZonePeaks, EPSILON);
        mDecayedFrequencyState = new float[uniqueRangeCount];
        mLastHash = -999;
    }

    private void resetVisualizerState() {
        if (mVisualizerConfig == null) {
            mCurrentLightState = new float[0];
            mZonePeaks = new float[0];
            mDecayedFrequencyState = new float[0];
        } else {
            mCurrentLightState = new float[mVisualizerConfig.zones.length];
            mZonePeaks = new float[mVisualizerConfig.zones.length];
            Arrays.fill(mZonePeaks, EPSILON);
            mDecayedFrequencyState = new float[mVisualizerConfig.uniqueRanges.length];
        }
        mLastHash = -999;
        mLastSendMs = 0L;
    }

    private static float applyPercentSlice(float normalizedValue, ZoneSpec zone) {
        if (!zone.hasPercentSlice()) {
            return normalizedValue;
        }

        float low = Math.min(zone.lowPercent, zone.highPercent);
        float high = Math.max(zone.lowPercent, zone.highPercent);
        float percent = normalizedValue * 100f;

        if (percent <= low) {
            return 0f;
        }
        if (percent >= high) {
            return 1f;
        }
        if (high == low) {
            return 1f;
        }
        return (percent - low) / (high - low);
    }

    private VisualizerConfig loadVisualizerConfig(String presetKey) throws IOException, JSONException {
        // Logic is now linear: if the JSON config fails, an exception is thrown
        // and handled by the caller (applyPresetSelection).
        return loadVisualizerConfigFromZonesConfig(presetKey);
    }

    private VisualizerConfig loadVisualizerConfigFromZonesConfig(String presetKey)
            throws IOException, JSONException {
        String rawJson = loadZonesConfigText(this);
        JSONObject root = new JSONObject(rawJson);
        JSONObject preset = root.optJSONObject(presetKey);

        if (preset == null) {
            throw new JSONException("Preset '" + presetKey + "' not found in zones.config");
        }

        JSONArray zonesArray = preset.optJSONArray("zones");
        if (zonesArray == null || zonesArray.length() == 0) {
            throw new JSONException("Preset '" + presetKey + "' has no zones");
        }

        double decayAlpha = preset.has("decay-alpha")
                ? preset.optDouble("decay-alpha", 0.8)
                : root.optDouble("decay-alpha", 0.8);

        ZoneSpec[] zones = parseZoneSpecs(zonesArray);
        return buildVisualizerConfig(
                presetKey,
                preset.optString("description", presetKey),
                decayAlpha,
                zones
        );
    }

    // 1. Changed to accept Context as a parameter
    private static String loadZonesConfigText(Context context) throws IOException {
        InputStream in = null;

        // 2. Removed "Context context = this;" because 'this' isn't allowed in static methods

        // 1. Try Assets (Primary location)
        try {
            in = context.getAssets().open("zones.config");
            return readFully(in);
        } catch (IOException ignored) {
            // Fall through to check filesystem
        } finally {
            closeQuietly(in);
        }

        // 2. Try Internal/External Filesystem locations
        File externalDir = context.getExternalFilesDir(null);
        File[] candidates = new File[]{
                new File(context.getFilesDir(), "zones.config"),
                externalDir == null ? null : new File(externalDir, "zones.config"),
                new File(context.getApplicationInfo().dataDir, "zones.config")
        };

        for (File candidate : candidates) {
            if (candidate != null && candidate.isFile()) {
                return readFile(candidate);
            }
        }

        // No fallback to internal presets. If it's not in the files, it's an error.
        throw new FileNotFoundException(
                "zones.config not found. Please bundle it as an asset or place it in the app's files directory."
        );
    }

    private void applyPresetSelection(String presetSelection) {
        try {
            // refreshPresetCatalog should now strictly parse the JSON file to populate mAvailablePresetKeys
            refreshPresetCatalog();

            String resolvedPresetKey = resolvePresetKey(presetSelection, mAvailablePresetKeys);

            if (!resolvedPresetKey.equals(mPresetKey) || mVisualizerConfig == null) {
                // This will throw an exception if the key doesn't exist in the JSON
                mVisualizerConfig = loadVisualizerConfig(resolvedPresetKey);
                mPresetKey = resolvedPresetKey;
                mPresetConfigVersion++;
                resetVisualizerState();
                refreshNotification();
                Log.d(TAG, "Applied preset '" + mPresetKey + "' from config file.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply preset selection: " + presetSelection + ". No fallback available.", e);
            // Set config to null to prevent the visualizer from running with invalid/missing data
            mVisualizerConfig = null;
        }
    }

    private String resolvePresetKey(String presetSelection, List<String> availablePresetKeys) {
        if (availablePresetKeys == null || availablePresetKeys.isEmpty()) {
            // Without a fallback catalog, this usually indicates the JSON failed to load
            return DEFAULT_PRESET_KEY;
        }

        if (availablePresetKeys.contains(presetSelection)) {
            return presetSelection;
        }

        String devicePresetKey = presetKeyForDevice(mSelectedDevice);
        if (devicePresetKey != null && availablePresetKeys.contains(devicePresetKey)) {
            return devicePresetKey;
        }

        String defaultPreset = chooseDefaultPresetKey(phoneModelForDevice(mSelectedDevice), availablePresetKeys);
        if (availablePresetKeys.contains(defaultPreset)) {
            return defaultPreset;
        }

        // If all else fails, pick the first one defined in the JSON
        return availablePresetKeys.get(0);
    }

    private VisualizerConfig buildVisualizerConfig(
            String presetKey,
            String description,
            double decayAlpha,
            ZoneSpec[] zones
    ) {
        float adjustedDecay = 0.86f + ((float) decayAlpha / 10f);
        List<float[]> uniquePairs = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();

        for (ZoneSpec zone : zones) {
            String key = String.format(Locale.US, "%.4f|%.4f", zone.lowHz, zone.highHz);
            if (seenPairs.add(key)) {
                uniquePairs.add(new float[]{zone.lowHz, zone.highHz});
            }
        }

        uniquePairs.sort(new Comparator<>() {
            @Override
            public int compare(float[] left, float[] right) {
                int lowCompare = Float.compare(left[0], right[0]);
                return lowCompare != 0 ? lowCompare : Float.compare(left[1], right[1]);
            }
        });

        FrequencyRange[] uniqueRanges = new FrequencyRange[uniquePairs.size()];
        for (int i = 0; i < uniquePairs.size(); i++) {
            float[] pair = uniquePairs.get(i);
            uniqueRanges[i] = new FrequencyRange(pair[0], pair[1]);
        }

        int[][] zoneToRangeIndices = new int[zones.length][];
        for (int zoneIndex = 0; zoneIndex < zones.length; zoneIndex++) {
            ZoneSpec zone = zones[zoneIndex];
            ArrayList<Integer> overlaps = new ArrayList<>();
            for (int rangeIndex = 0; rangeIndex < uniqueRanges.length; rangeIndex++) {
                FrequencyRange range = uniqueRanges[rangeIndex];
                if (!(range.highHz < zone.lowHz || range.lowHz > zone.highHz)) {
                    overlaps.add(rangeIndex);
                }
            }
            int[] mapping = new int[overlaps.size()];
            for (int j = 0; j < overlaps.size(); j++) {
                mapping[j] = overlaps.get(j);
            }
            zoneToRangeIndices[zoneIndex] = mapping;
        }

        return new VisualizerConfig(
                presetKey,
                description,
                adjustedDecay,
                zones,
                uniqueRanges,
                zoneToRangeIndices
        );
    }

    private ZoneSpec[] parseZoneSpecs(JSONArray zonesArray) throws JSONException {
        ZoneSpec[] zones = new ZoneSpec[zonesArray.length()];
        for (int i = 0; i < zonesArray.length(); i++) {
            JSONArray zoneArray = zonesArray.getJSONArray(i);
            float lowHz = (float) zoneArray.getDouble(0);
            float highHz = (float) zoneArray.getDouble(1);

            if (lowHz > highHz) {
                float tmp = lowHz;
                lowHz = highHz;
                highHz = tmp;
            }

            float lowPercent = parseOptionalPercent(zoneArray, 3);
            float highPercent = parseOptionalPercent(zoneArray, 4);
            zones[i] = new ZoneSpec(lowHz, highHz, lowPercent, highPercent);
        }
        return zones;
    }

    private static String presetKeyForDevice(int device) {
        return switch (device) {
            case DeviceProfile.DEVICE_NP1 -> "np1";
            case DeviceProfile.DEVICE_NP2 -> "np2";
            case DeviceProfile.DEVICE_NP2A -> "np2a";
            case DeviceProfile.DEVICE_NP3A -> "np3a";
            case DeviceProfile.DEVICE_NP4A -> "np4a";
            default -> null;
        };
    }

    private static String phoneModelForDevice(int device) {
        return switch (device) {
            case DeviceProfile.DEVICE_NP1 -> PHONE_MODEL_PHONE1;
            case DeviceProfile.DEVICE_NP2 -> PHONE_MODEL_PHONE2;
            case DeviceProfile.DEVICE_NP2A -> PHONE_MODEL_PHONE2A;
            case DeviceProfile.DEVICE_NP3A -> PHONE_MODEL_PHONE3A;
            case DeviceProfile.DEVICE_NP4A -> PHONE_MODEL_PHONE4A;
            default -> PHONE_MODEL_UNKNOWN;
        };
    }

    private static String readFile(File file) throws IOException {
        FileInputStream input = new FileInputStream(file);
        try {
            return readFully(input);
        } finally {
            closeQuietly(input);
        }
    }

    private static String readFully(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }
    private static void closeQuietly(Closeable closeable) {
        if (closeable == null) {
            return;
        }
        try {
            closeable.close();
        } catch (IOException ignored) {
        }
    }

    private static float parseOptionalPercent(JSONArray zoneArray, int index) {
        if (index >= zoneArray.length()) {
            return Float.NaN;
        }

        Object raw = zoneArray.opt(index);
        if (raw == null || raw == JSONObject.NULL) {
            return Float.NaN;
        }

        try {
            float value;
            if (raw instanceof Number) {
                value = ((Number) raw).floatValue();
            } else {
                String text = String.valueOf(raw).trim();
                if (text.endsWith("%")) {
                    text = text.substring(0, text.length() - 1).trim();
                }
                value = Float.parseFloat(text);
            }

            if (value >= 0f && value <= 1f) {
                value *= 100f;
            }
            return value;
        } catch (Exception ignored) {
            return Float.NaN;
        }
    }

    private static float[] buildHannWindow(int size) {
        float[] hann = new float[size];
        for (int i = 0; i < size; i++) {
            hann[i] = 0.5f * (1f - (float) Math.cos((2d * Math.PI * i) / size));
        }
        return hann;
    }

    private static int roundHalfEvenToInt(double value) {
        return (int) Math.rint(value);
    }

    private static int nextPowerOfTwo(int value) {
        int power = 1;
        while (power < value) {
            power <<= 1;
        }
        return power;
    }

    private static void fft(float[] re, float[] im, int n) {
        int j = 0;
        for (int i = 1; i < n; i++) {
            int bit = n >> 1;
            while ((j & bit) != 0) {
                j ^= bit;
                bit >>= 1;
            }
            j ^= bit;
            if (i < j) {
                float reTmp = re[i];
                re[i] = re[j];
                re[j] = reTmp;

                float imTmp = im[i];
                im[i] = im[j];
                im[j] = imTmp;
            }
        }

        for (int len = 2; len <= n; len <<= 1) {
            double angle = (-2d * Math.PI) / len;
            float wr = (float) Math.cos(angle);
            float wi = (float) Math.sin(angle);

            for (int i = 0; i < n; i += len) {
                float cr = 1f;
                float ci = 0f;
                for (int k = 0; k < len / 2; k++) {
                    float ur = re[i + k];
                    float ui = im[i + k];
                    float vr = (re[i + k + (len / 2)] * cr) - (im[i + k + (len / 2)] * ci);
                    float vi = (re[i + k + (len / 2)] * ci) + (im[i + k + (len / 2)] * cr);

                    re[i + k] = ur + vr;
                    im[i + k] = ui + vi;
                    re[i + k + (len / 2)] = ur - vr;
                    im[i + k + (len / 2)] = ui - vi;

                    float nextCr = (cr * wr) - (ci * wi);
                    ci = (cr * wi) + (ci * wr);
                    cr = nextCr;
                }
            }
        }
    }

    private Notification buildNotification() {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Glyph Visualizer",
                    NotificationManager.IMPORTANCE_LOW
            );
            nm.createNotificationChannel(channel);
        }

        PendingIntent pi = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        String content = mVisualizerConfig == null
                ? "zones.config missing"
                : (mDetectedPhoneModel + " - " + mVisualizerConfig.presetKey + " - " + mVisualizerConfig.description);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Glyph Visualizer")
                .setContentText(content)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentIntent(pi)
                .setOngoing(true)
                .setSilent(true)
                .build();
    }

    private void refreshNotification() {
        if (!sIsRunning && !mCapturing) {
            return;
        }
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (nm != null) {
            nm.notify(NOTIF_ID, buildNotification());
        }
    }

    private void refreshPresetCatalog() throws IOException, JSONException {
        mDetectedPhoneModel = detectPhoneModel();
        String selectedPhoneModel = phoneModelForDevice(mSelectedDevice);
        String phoneModelForCatalog = PHONE_MODEL_UNKNOWN.equals(selectedPhoneModel)
                ? mDetectedPhoneModel
                : selectedPhoneModel;

        try {
            JSONObject root = loadZonesConfigRoot();
            List<String> matching = getPresetKeysForPhoneModel(root, phoneModelForCatalog);
            if (matching.isEmpty() && !PHONE_MODEL_UNKNOWN.equals(mDetectedPhoneModel)) {
                matching = getPresetKeysForPhoneModel(root, mDetectedPhoneModel);
            }
            if (matching.isEmpty()) {
                matching = getAllPresetKeys(root);
            }
            mAvailablePresetKeys = matching;
        } catch (FileNotFoundException e) {
            List<String> matching = getBuiltInPresetKeysForPhoneModel(phoneModelForCatalog);
            if (matching.isEmpty() && !PHONE_MODEL_UNKNOWN.equals(mDetectedPhoneModel)) {
                matching = getBuiltInPresetKeysForPhoneModel(mDetectedPhoneModel);
            }
            if (matching.isEmpty()) {
                matching = getAllBuiltInPresetKeys();
            }
            mAvailablePresetKeys = matching;
        }
    }

    private JSONObject loadZonesConfigRoot() throws IOException, JSONException {
        return loadZonesConfigRoot(this);
    }

    private static JSONObject loadZonesConfigRoot(Context context) throws IOException, JSONException {
        // Explicitly call the static version using the Context provided
        return new JSONObject(loadZonesConfigText(context));
    }

    private static List<String> getAllPresetKeys(JSONObject root) {
        ArrayList<String> presets = new ArrayList<>();
        JSONArray names = root.names();
        if (names == null) {
            return presets;
        }
        for (int i = 0; i < names.length(); i++) {
            String key = names.optString(i, "");
            if (isPresetEntry(root, key)) {
                presets.add(key);
            }
        }
        Collections.sort(presets);
        return presets;
    }

    private static List<PresetInfo> buildPresetInfos(JSONObject root, List<String> keys) {
        ArrayList<PresetInfo> presets = new ArrayList<>();
        for (String key : keys) {
            JSONObject preset = root.optJSONObject(key);
            if (preset == null) {
                continue;
            }
            presets.add(new PresetInfo(key, preset.optString("description", key)));
        }
        return presets;
    }

    private static List<String> getPresetKeysForPhoneModel(JSONObject root, String phoneModel) {
        ArrayList<String> presets = new ArrayList<>();
        if (PHONE_MODEL_UNKNOWN.equals(phoneModel)) {
            return presets;
        }

        JSONArray names = root.names();
        if (names == null) {
            return presets;
        }
        for (int i = 0; i < names.length(); i++) {
            String key = names.optString(i, "");
            if (!isPresetEntry(root, key)) {
                continue;
            }
            JSONObject preset = root.optJSONObject(key);
            if (preset != null && phoneModel.equalsIgnoreCase(preset.optString("phone_model", ""))) {
                presets.add(key);
            }
        }
        Collections.sort(presets);
        return presets;
    }

    private static boolean isPresetEntry(JSONObject root, String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        if ("version".equals(key)
                || "amp".equals(key)
                || "decay-alpha".equals(key)
                || "decay_alpha".equals(key)
                || "what-is-decay-alpha".equals(key)
                || "what-is-decay".equals(key)) {
            return false;
        }
        JSONObject preset = root.optJSONObject(key);
        return preset != null && preset.optJSONArray("zones") != null;
    }

    private static List<String> getBuiltInPresetKeysForPhoneModel(String phoneModel) {
        if (PHONE_MODEL_PHONE1.equals(phoneModel)) {
            return new ArrayList<>(Arrays.asList("np1", "np1_bass"));
        }
        if (PHONE_MODEL_PHONE2.equals(phoneModel)) {
            return new ArrayList<>(Arrays.asList("np2", "np2_bass"));
        }
        if (PHONE_MODEL_PHONE2A.equals(phoneModel)) {
            return new ArrayList<>(Arrays.asList("np2a", "np2a_bass"));
        }
        if (PHONE_MODEL_PHONE3A.equals(phoneModel)) {
            return new ArrayList<>(Arrays.asList("np3a", "np3a_bass"));
        }
        if (PHONE_MODEL_PHONE4A.equals(phoneModel)) {
            return new ArrayList<>(Arrays.asList("np4a", "np4a_bass"));
        }
        return new ArrayList<>();
    }

    private static List<String> getAllBuiltInPresetKeys() {
        return new ArrayList<>(Arrays.asList(
                "np1", "np1_bass",
                "np2", "np2_bass",
                "np2a", "np2a_bass",
                "np3a", "np3a_bass",
                "np4a", "np4a_bass"
        ));
    }

    private static List<PresetInfo> getBuiltInPresetInfosForPhoneModel(String phoneModel) {
        ArrayList<PresetInfo> presets = new ArrayList<>();
        if (PHONE_MODEL_PHONE1.equals(phoneModel)) {
            presets.add(new PresetInfo("np1", "Built-in vocal preset for Phone (1)"));
            presets.add(new PresetInfo("np1_bass", "Built-in bass preset for Phone (1)"));
        } else if (PHONE_MODEL_PHONE2.equals(phoneModel)) {
            presets.add(new PresetInfo("np2", "Built-in vocal preset for Phone (2)"));
            presets.add(new PresetInfo("np2_bass", "Built-in bass preset for Phone (2)"));
        } else if (PHONE_MODEL_PHONE2A.equals(phoneModel)) {
            presets.add(new PresetInfo("np2a", "Built-in vocal preset for Phone (2a)"));
            presets.add(new PresetInfo("np2a_bass", "Built-in bass preset for Phone (2a)"));
        } else if (PHONE_MODEL_PHONE3A.equals(phoneModel)) {
            presets.add(new PresetInfo("np3a", "Built-in vocal preset for Phone (3a)"));
            presets.add(new PresetInfo("np3a_bass", "Built-in bass preset for Phone (3a)"));
        } else if (PHONE_MODEL_PHONE4A.equals(phoneModel)) {
            presets.add(new PresetInfo("np4a", "Vocal Heavy — full spectrum on 6 LEDs"));
            presets.add(new PresetInfo("np4a_bass", "Bass Heavy — beat-reactive cumulative fill"));
        }
        return presets;
    }

    private static List<PresetInfo> getAllBuiltInPresetInfos() {
        return new ArrayList<>(Arrays.asList(
                new PresetInfo("np1", "Built-in vocal preset for Phone (1)"),
                new PresetInfo("np1_bass", "Built-in bass preset for Phone (1)"),
                new PresetInfo("np2", "Built-in vocal preset for Phone (2)"),
                new PresetInfo("np2_bass", "Built-in bass preset for Phone (2)"),
                new PresetInfo("np2a", "Built-in vocal preset for Phone (2a)"),
                new PresetInfo("np2a_bass", "Built-in bass preset for Phone (2a)"),
                new PresetInfo("np3a", "Built-in vocal preset for Phone (3a)"),
                new PresetInfo("np3a_bass", "Built-in bass preset for Phone (3a)"),
                new PresetInfo("np4a", "Vocal Heavy — full spectrum on 6 LEDs"),
                new PresetInfo("np4a_bass", "Bass Heavy — beat-reactive cumulative fill")
        ));
    }

    private static String chooseDefaultPresetKey(String phoneModel, List<String> presetKeys) {
        if (presetKeys == null || presetKeys.isEmpty()) {
            return DEFAULT_PRESET_KEY;
        }

        String preferred = null;
        if (PHONE_MODEL_PHONE1.equals(phoneModel)) {
            preferred = "np1";
        } else if (PHONE_MODEL_PHONE2.equals(phoneModel)) {
            preferred = "np2";
        } else if (PHONE_MODEL_PHONE2A.equals(phoneModel)) {
            preferred = "np2a";
        } else if (PHONE_MODEL_PHONE3A.equals(phoneModel)) {
            preferred = "np3a";
        } else if (PHONE_MODEL_PHONE4A.equals(phoneModel)) {
            preferred = "np4a";
        } else if (PHONE_MODEL_PHONE3.equals(phoneModel)) {
            preferred = "np3test";
        }

        if (preferred != null && presetKeys.contains(preferred)) {
            return preferred;
        }
        return presetKeys.get(0);
    }

    private static String detectPhoneModel() {
        if (Common.is20111()) {
            return PHONE_MODEL_PHONE1;
        }
        if (Common.is22111()) {
            return PHONE_MODEL_PHONE2;
        }
        if (Common.is23111() || Common.is23113()) {
            return PHONE_MODEL_PHONE2A;
        }
        if (Common.is24111()) {
            return PHONE_MODEL_PHONE3A;
        }

        String buildText = (
                Build.MANUFACTURER + " "
                        + Build.BRAND + " "
                        + Build.MODEL + " "
                        + Build.DEVICE + " "
                        + Build.PRODUCT
        ).toLowerCase(Locale.US);

        if (buildText.contains("phone 3a")) {
            return PHONE_MODEL_PHONE3A;
        }
        if (buildText.contains("phone 3")) {
            return PHONE_MODEL_PHONE3;
        }
        if (buildText.contains("phone 2a")) {
            return PHONE_MODEL_PHONE2A;
        }
        if (buildText.contains("phone 2")) {
            return PHONE_MODEL_PHONE2;
        }
        if (buildText.contains("phone 1")) {
            return PHONE_MODEL_PHONE1;
        }

        return PHONE_MODEL_UNKNOWN;
    }
}
