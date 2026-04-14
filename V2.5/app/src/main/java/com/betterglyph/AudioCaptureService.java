package com.betterglyph;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioPlaybackCaptureConfiguration;
import android.media.AudioRecord;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.nothing.ketchum.Common;
import com.nothing.ketchum.GlyphException;
import com.nothing.ketchum.GlyphFrame;
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
 *
 * The Java port now follows the same high-level pipeline as musicViz.py:
 *   FFT -> unique frequency peaks -> per-frequency decay ->
 *   overlapping zone mapping -> quadratic normalization ->
 *   optional percent slice mapping -> glyph output
 *
 * The only intentional runtime difference is normalization: the Python script
 * can normalize against the whole track, while the live service uses a rolling
 * per-zone peak because future frames are not known yet.
 */
public class AudioCaptureService extends Service {

    private static final String TAG = "GlyphViz:Service";
    private static final String CHANNEL_ID = "glyph_viz_channel";
    private static final int NOTIF_ID = 1;

    public static final String EXTRA_PRESET_KEY = "preset_key";

    /** Called on the main thread with overall energy 0-1 every processed frame */
    public interface BeatListener {
        void onBeat(float energy);
    }
    private volatile BeatListener mBeatListener = null;
    public void setBeatListener(BeatListener l) { mBeatListener = l; }
    /** Convenience: accept a plain Runnable as beat callback */
    public void setBeatCallback(Runnable r) { mBeatListener = (energyd) -> { if (r != null) r.run(); }; }
    public static final String PRESET_VOCAL = "preset_vocal";
    public static final String PRESET_BASS = "preset_bass";
    public static final float DEFAULT_GAMMA = 1f;
    private static final String PREFS_NAME = "glyph_visualizer_prefs";
    private static final String PREF_GAMMA = "gamma";
    private static final String PREF_LATENCY_PREFIX = "latency_device_";
    private static final String DEFAULT_PRESET_KEY = "np2";
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

    private static final float THRESHOLD = 0.06f;
    private static final float PEAK_FALLOFF = 0.9995f;
    private static final float PYTHON_FREQ_MULTIPLIER = 4f;
    private static final float EPSILON = 0.000001f;
    private static final long MIN_SEND_MS = 16L;
    private static final int MAX_LATENCY_COMPENSATION_MS = 250;
    private static final float MIN_GAMMA = 0.40f;
    private static final float MAX_GAMMA = 3.00f;
    public  static final int GAMMA_SLIDER_STEPS = 260;
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
    private String mPresetMode = PRESET_VOCAL;
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
                mGM.register(com.nothing.ketchum.Glyph.DEVICE_25111);  // Phone 4a
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

    private static final class ZoneSpec {
        final float lowHz;
        final float highHz;
        final float lowPercent;
        final float highPercent;

        ZoneSpec(float lowHz, float highHz, float lowPercent, float highPercent) {
            this.lowHz = lowHz;
            this.highHz = highHz;
            this.lowPercent = lowPercent;
            this.highPercent = highPercent;
        }

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

    private static final class VisualizerConfig {
        final String presetKey;
        final String description;
        final float decay;
        final ZoneSpec[] zones;
        final FrequencyRange[] uniqueRanges;
        final int[][] zoneToRangeIndices;

        VisualizerConfig(
                String presetKey,
                String description,
                float decay,
                ZoneSpec[] zones,
                FrequencyRange[] uniqueRanges,
                int[][] zoneToRangeIndices
        ) {
            this.presetKey = presetKey;
            this.description = description;
            this.decay = decay;
            this.zones = zones;
            this.uniqueRanges = uniqueRanges;
            this.zoneToRangeIndices = zoneToRangeIndices;
        }
    }

    private static final class PendingFrame {
        final float[] uniquePeaks;
        final VisualizerConfig config;
        final int configVersion;
        final long dueAtMs;

        PendingFrame(float[] uniquePeaks, VisualizerConfig config, int configVersion, long dueAtMs) {
            this.uniquePeaks = uniquePeaks;
            this.config = config;
            this.configVersion = configVersion;
            this.dueAtMs = dueAtMs;
        }
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
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mSelectedDevice = DeviceProfile.detectDevice();
        mLatencyCompensationMs = loadLatencyCompensationMs(this, mSelectedDevice);
        mGamma = loadGamma(this);
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
            Log.d(TAG, "Detected " + mDetectedPhoneModel + ", loaded preset " + mPresetKey);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load zones.config: " + e.getMessage(), e);
        }

        mGM = GlyphManager.getInstance(getApplicationContext());
        mGM.init(mGlyphCallback);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String requestedPreset = intent != null ? intent.getStringExtra(EXTRA_PRESET_KEY) : null;
        if (requestedPreset != null && !requestedPreset.trim().isEmpty()) {
            setPreset(requestedPreset.trim());
        }
        startForeground(NOTIF_ID, buildNotification());
        return START_NOT_STICKY;
    }

    public String getDetectedPhoneModel() {
        return mDetectedPhoneModel;
    }

    public List<String> getAvailablePresetKeys() {
        return new ArrayList<>(mAvailablePresetKeys);
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

    public static int clampLatencyCompensationMs(int latencyMs) {
        return Math.max(0, Math.min(MAX_LATENCY_COMPENSATION_MS, latencyMs));
    }

    public static float clampGamma(float gamma) {
        return clamp(gamma, MIN_GAMMA, MAX_GAMMA);
    }

    public static int gammaToSliderProgress(float gamma) {
        return Math.round((clampGamma(gamma) - MIN_GAMMA) * 100f);
    }

    public static float gammaFromSliderProgress(int progress) {
        return clampGamma(MIN_GAMMA + (Math.max(0, Math.min(GAMMA_SLIDER_STEPS, progress)) / 100f));
    }

    public static int loadLatencyCompensationMs(Context context, int device) {
        return clampLatencyCompensationMs(
                getPreferences(context).getInt(latencyPreferenceKey(device), 0)
        );
    }

    public static void saveLatencyCompensationMs(Context context, int device, int latencyMs) {
        getPreferences(context)
                .edit()
                .putInt(latencyPreferenceKey(device), clampLatencyCompensationMs(latencyMs))
                .apply();
    }

    public static float loadGamma(Context context) {
        return clampGamma(getPreferences(context).getFloat(PREF_GAMMA, DEFAULT_GAMMA));
    }

    public static void saveGamma(Context context, float gamma) {
        getPreferences(context)
                .edit()
                .putFloat(PREF_GAMMA, clampGamma(gamma))
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
        if (PRESET_VOCAL.equals(trimmedSelection) || PRESET_BASS.equals(trimmedSelection)) {
            mPresetMode = trimmedSelection;
        }
        applyPresetSelection(trimmedSelection);
    }

    public void setDevice(int device) {
        mSelectedDevice = device;
        setLatencyCompensationMs(loadLatencyCompensationMs(this, device));
    }

    public void setLatencyCompensationMs(int latencyMs) {
        int clampedLatencyMs = clampLatencyCompensationMs(latencyMs);
        if (mLatencyCompensationMs != clampedLatencyMs) {
            mLatencyCompensationMs = clampedLatencyMs;
            mLatencySettingsVersion++;
        }
    }

    public void setGamma(float gamma) {
        mGamma = clampGamma(gamma);
    }

    @Override
    public void onDestroy() {
        sIsRunning = false;
        stopCapture();
        if (mSessionOpen) {
            try {
                mGM.closeSession();
            } catch (GlyphException ignored) {
            }
        }
        if (mGM != null) {
            mGM.unInit();
        }
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
        mProjection = pm.getMediaProjection(resultCode, data);
        if (mProjection == null) {
            Log.e(TAG, "Null projection");
            sIsRunning = false;
            return;
        }
        mProjection.registerCallback(new MediaProjection.Callback() {
            @Override
            public void onStop() {
                stopCapture();
                stopSelf();
            }
        }, mHandler);

        mExecutor = Executors.newSingleThreadExecutor();
        mExecutor.submit(this::captureLoop);
        sIsRunning = true;
        Log.d(TAG, "startCapture OK");
    }

    public void stopCapture() {
        mCapturing = false;
        sIsRunning = false;
        if (mAudioRecord != null) {
            AudioRecord audioRecord = mAudioRecord;
            mAudioRecord = null;
            try {
                audioRecord.stop();
            } catch (Exception ignored) {
            }
            audioRecord.release();
        }
        if (mProjection != null) {
            MediaProjection projection = mProjection;
            mProjection = null;
            projection.stop();
        }
        if (mExecutor != null) {
            ExecutorService executor = mExecutor;
            mExecutor = null;
            executor.shutdownNow();
        }
        resetVisualizerState();
        mHandler.post(() -> {
            turnOffGlyphsManually();
        });
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
        switch (mSelectedDevice) {
            case DeviceProfile.DEVICE_NP1:
                return 15;
            case DeviceProfile.DEVICE_NP2:
                return 33;
            case DeviceProfile.DEVICE_NP2A:
                return 26;
            case DeviceProfile.DEVICE_NP3A:
                return 36;
            case DeviceProfile.DEVICE_NP4A:
                return 6;
            default:
                return 0;
        }
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

        // Compute overall energy for beat UI callback
        float overallEnergy = 0f;
        for (float v : nextLightState) overallEnergy = Math.max(overallEnergy, v);
        final float beatEnergy = overallEnergy;
        final BeatListener bl = mBeatListener;
        if (bl != null) mHandler.post(() -> bl.onBeat(beatEnergy));

        int hash = Arrays.hashCode(frameColors);
        if (hash == mLastHash) {
            return;
        }
        mLastHash = hash;

        try {
            mGM.setFrameColors(frameColors); //this is the way to have proper smooth brightness levels.
            return;
        } catch (Exception e) {
            Log.w(TAG, "setFrameColors failed, falling back to channel toggles", e);
        }

        ArrayList<Integer> activeChannels = new ArrayList<>();
        for (int zoneIndex = 0; zoneIndex < nextLightState.length; zoneIndex++) {
            if (nextLightState[zoneIndex] > THRESHOLD) {
                activeChannels.add(zoneIndex);
            }
        }

        try {
            if (activeChannels.isEmpty()) {
                mGM.turnOff();
                return;
            }

            GlyphFrame.Builder builder = mGM.getGlyphFrameBuilder();
            for (int channel : activeChannels) {
                builder.buildChannel(channel);
            }
            mGM.toggle(builder.build());
        } catch (Exception e) {
            Log.e(TAG, "Glyph update failed: " + e.getMessage(), e);
        }
    }

    private int[] buildFrameColors(float[] normalizedLightState, int expectedLength) {
        int[] frameColors = new int[expectedLength];
        int count = Math.min(normalizedLightState.length, expectedLength);
        for (int i = 0; i < count; i++) {
            float gammaAdjusted = applyGamma(clamp01(normalizedLightState[i]));
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
        return clamp01((float) Math.pow(normalizedValue, mGamma));
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

            float normalized = clamp01(rawZonePeak / mZonePeaks[zoneIndex]);
            float quadratic = normalized * normalized;
            float mapped = applyPercentSlice(quadratic, config.zones[zoneIndex]);
            nextState[zoneIndex] = mapped < EPSILON ? 0f : clamp01(mapped);
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
        try {
            return loadVisualizerConfigFromZonesConfig(presetKey);
        } catch (FileNotFoundException e) {
            Log.w(TAG, "zones.config missing, using built-in preset catalog");
            return loadBuiltInVisualizerConfig(presetKey);
        }
    }

    private VisualizerConfig loadVisualizerConfigFromZonesConfig(String presetKey)
            throws IOException, JSONException {
        String rawJson = loadZonesConfigText();
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

    private String loadZonesConfigText() throws IOException {
        return loadZonesConfigText(this);
    }

    private static String loadZonesConfigText(Context context) throws IOException {
        InputStream in = null;
        try {
            in = context.getAssets().open("zones.config");
            return readFully(in);
        } catch (IOException ignored) {
            closeQuietly(in);
        }

        ClassLoader loader = AudioCaptureService.class.getClassLoader();
        if (loader != null) {
            in = loader.getResourceAsStream("zones.config");
            if (in != null) {
                try {
                    return readFully(in);
                } finally {
                    closeQuietly(in);
                }
            }
        }

        ApplicationInfo appInfo = context.getApplicationInfo();
        File externalDir = context.getExternalFilesDir(null);
        File[] candidates = new File[]{
                new File(context.getFilesDir(), "zones.config"),
                externalDir == null ? null : new File(externalDir, "zones.config"),
                new File(appInfo.dataDir, "zones.config"),
                new File("zones.config"),
                new File("BetterNothingMusicVisualizer-Android-app/zones.config")
        };

        for (File candidate : candidates) {
            if (candidate != null && candidate.isFile()) {
                return readFile(candidate);
            }
        }

        throw new FileNotFoundException(
                "zones.config not found. Bundle it as an asset or place it in app files."
        );
    }

    private void applyPresetSelection(String presetSelection) {
        try {
            refreshPresetCatalog();
            String resolvedPresetKey = resolvePresetKey(presetSelection, mAvailablePresetKeys);
            if (!resolvedPresetKey.equals(mPresetKey) || mVisualizerConfig == null) {
                mVisualizerConfig = loadVisualizerConfig(resolvedPresetKey);
                mPresetKey = resolvedPresetKey;
                mPresetConfigVersion++;
                resetVisualizerState();
                refreshNotification();
                Log.d(TAG, "Resolved preset '" + presetSelection + "' to '" + mPresetKey + "'");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to apply preset selection: " + presetSelection, e);
        }
    }

    private String resolvePresetKey(String presetSelection, List<String> availablePresetKeys) {
        if (availablePresetKeys == null || availablePresetKeys.isEmpty()) {
            return DEFAULT_PRESET_KEY;
        }

        if (availablePresetKeys.contains(presetSelection)) {
            return presetSelection;
        }

        String devicePresetKey = presetKeyForDevice(mSelectedDevice);
        String preferredMode = PRESET_BASS.equals(presetSelection) ? PRESET_BASS : PRESET_VOCAL;
        String[] candidates = buildPresetCandidates(devicePresetKey, preferredMode);
        for (String candidate : candidates) {
            if (availablePresetKeys.contains(candidate)) {
                return candidate;
            }
        }

        if (devicePresetKey != null && availablePresetKeys.contains(devicePresetKey)) {
            return devicePresetKey;
        }

        String defaultPreset = chooseDefaultPresetKey(phoneModelForDevice(mSelectedDevice), availablePresetKeys);
        if (availablePresetKeys.contains(defaultPreset)) {
            return defaultPreset;
        }

        return availablePresetKeys.get(0);
    }

    private VisualizerConfig loadBuiltInVisualizerConfig(String presetKey) throws JSONException {
        switch (presetKey) {
            case "np1":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in vocal preset for Phone (1)",
                        0.8d,
                        buildZoneSpecs(DeviceProfile.VOCAL_NP1)
                );
            case "np2":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in vocal preset for Phone (2)",
                        0.8d,
                        buildNp2VocalZones()
                );
            case "np2a":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in vocal preset for Phone (2a)",
                        0.8d,
                        buildZoneSpecs(DeviceProfile.VOCAL_NP2A)
                );
            case "np3a":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in vocal preset for Phone (3a)",
                        0.8d,
                        buildZoneSpecs(DeviceProfile.VOCAL_NP3A)
                );
            case "np1_bass":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in bass preset for Phone (1)",
                        0.8d,
                        buildNp1BassZones()
                );
            case "np2_bass":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in bass preset for Phone (2)",
                        0.8d,
                        buildNp2BassZones()
                );
            case "np2a_bass":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in bass preset for Phone (2a)",
                        0.8d,
                        buildNp2aBassZones()
                );
            case "np3a_bass":
                return buildVisualizerConfig(
                        presetKey,
                        "Built-in bass preset for Phone (3a)",
                        0.8d,
                        buildNp3aBassZones()
                );
            case "np4a":
                return buildVisualizerConfig(
                        presetKey,
                        "Vocal Heavy — full spectrum on 6 LEDs",
                        0.8d,
                        buildZoneSpecs(DeviceProfile.VOCAL_NP4A)
                );
            case "np4a_bass":
                return buildVisualizerConfig(
                        presetKey,
                        "Bass Heavy — beat-reactive cumulative fill",
                        0.8d,
                        buildNp4aBassZones()
                );
            default:
                throw new JSONException("Preset '" + presetKey + "' not found in built-in preset catalog");
        }
    }

    private VisualizerConfig buildVisualizerConfig(
            String presetKey,
            String description,
            double decayAlpha,
            ZoneSpec[] zones
    ) {
        float adjustedDecay = clamp(0.86f + ((float) decayAlpha / 10f), 0f, 0.9999f);
        List<float[]> uniquePairs = new ArrayList<>();
        Set<String> seenPairs = new HashSet<>();

        for (ZoneSpec zone : zones) {
            String key = String.format(Locale.US, "%.4f|%.4f", zone.lowHz, zone.highHz);
            if (seenPairs.add(key)) {
                uniquePairs.add(new float[]{zone.lowHz, zone.highHz});
            }
        }

        Collections.sort(uniquePairs, new Comparator<float[]>() {
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

    private ZoneSpec[] buildZoneSpecs(float[][] rows) {
        ZoneSpec[] zones = new ZoneSpec[rows.length];
        for (int i = 0; i < rows.length; i++) {
            zones[i] = zone(rows[i][0], rows[i][1]);
        }
        return zones;
    }

    private ZoneSpec[] buildNp2VocalZones() {
        ZoneSpec[] zones = buildZoneSpecs(DeviceProfile.VOCAL_NP2);
        zones[24] = zone(DeviceProfile.VOCAL_NP2[24][0], DeviceProfile.VOCAL_NP2[24][1], 0f, 70f);
        return zones;
    }

    private ZoneSpec[] buildNp1BassZones() {
        ZoneSpec[] zones = new ZoneSpec[15];
        zones[0] = zone(200f, 600f);
        zones[1] = zone(2000f, 8000f);
        zones[2] = zone(200f, 600f);
        zones[3] = zone(600f, 2000f);
        zones[4] = zone(20f, 90f);
        zones[5] = zone(90f, 200f);
        zones[6] = zone(8000f, 20000f);
        fillPercentBar(zones, 7, 8, 20f, 200f);
        return zones;
    }

    private ZoneSpec[] buildNp2BassZones() {
        ZoneSpec[] zones = new ZoneSpec[33];
        zones[0] = zone(400f, 1000f);
        zones[1] = zone(1000f, 3000f);
        zones[2] = zone(3000f, 6000f);
        fillPercentBar(zones, 3, 16, 20f, 200f);
        zones[19] = zone(6000f, 12000f);
        zones[20] = zone(12000f, 20000f);
        zones[21] = zone(180f, 400f);
        zones[22] = zone(80f, 180f);
        zones[23] = zone(20f, 80f);
        zones[24] = zone(4000f, 16000f, 0f, 70f);
        fillPercentBar(zones, 25, 8, 20f, 200f);
        return zones;
    }

    private ZoneSpec[] buildNp2aBassZones() {
        ZoneSpec[] zones = new ZoneSpec[26];
        fillPercentBar(zones, 0, 16, 20f, 200f);
        zones[16] = zone(250f, 400f);
        zones[17] = zone(400f, 700f);
        zones[18] = zone(700f, 1200f);
        zones[19] = zone(1000f, 3000f);
        zones[20] = zone(400f, 1000f);
        zones[21] = zone(180f, 400f);
        zones[22] = zone(80f, 180f);
        zones[23] = zone(20f, 80f);
        zones[24] = zone(8000f, 20000f);
        zones[25] = zone(3000f, 8000f);
        return zones;
    }

    private ZoneSpec[] buildNp3aBassZones() {
        ZoneSpec[] zones = new ZoneSpec[36];
        fillPercentBar(zones, 0, 16, 20f, 200f);
        zones[16] = zone(400f, 700f);
        zones[17] = zone(180f, 400f);
        zones[18] = zone(80f, 180f);
        zones[19] = zone(20f, 80f);
        fillPercentBar(zones, 20, 8, 20f, 200f);
        zones[28] = zone(700f, 1500f);
        zones[29] = zone(1000f, 3000f);
        zones[30] = zone(400f, 1000f);
        zones[31] = zone(2000f, 4000f);
        zones[32] = zone(4000f, 7000f);
        zones[33] = zone(12000f, 20000f);
        zones[34] = zone(6000f, 12000f);
        zones[35] = zone(3000f, 6000f);
        return zones;
    }


    /**
     * Phone (4a) — 6 LEDs (A1-A6 = indices 0-5)
     * Bass Heavy: cumulative percent-bar fill from bottom (A6=sub-bass) to top (A1=treble).
     * All 6 zones share the same bass frequency range (20-160 Hz), each covering a
     * successive 1/6 slice of the normalised level so they light up sequentially.
     * The result is a rising bar that visually fills upward as the beat hits.
     */
    private ZoneSpec[] buildNp4aBassZones() {
        ZoneSpec[] zones = new ZoneSpec[6];
        // A6 (idx 5) = lowest slice, fires first on any bass energy
        zones[5] = zone(20f, 160f,  0f,  17f);
        zones[4] = zone(20f, 160f, 17f,  33f);
        zones[3] = zone(20f, 160f, 33f,  50f);
        zones[2] = zone(20f, 160f, 50f,  67f);
        zones[1] = zone(20f, 160f, 67f,  83f);
        // A1 (idx 0) = top slice, only on peak bass hits
        zones[0] = zone(20f, 160f, 83f, 100f);
        return zones;
    }

    private void fillPercentBar(ZoneSpec[] zones, int startIndex, int segmentCount, float lowHz, float highHz) {
        float step = 100f / segmentCount;
        for (int i = 0; i < segmentCount; i++) {
            zones[startIndex + i] = zone(lowHz, highHz, step * i, step * (i + 1));
        }
    }

    private static ZoneSpec zone(float lowHz, float highHz) {
        return new ZoneSpec(lowHz, highHz, Float.NaN, Float.NaN);
    }

    private static ZoneSpec zone(float lowHz, float highHz, float lowPercent, float highPercent) {
        return new ZoneSpec(lowHz, highHz, lowPercent, highPercent);
    }

    private String[] buildPresetCandidates(String devicePresetKey, String presetMode) {
        if (devicePresetKey == null || devicePresetKey.isEmpty()) {
            return new String[0];
        }

        if (PRESET_BASS.equals(presetMode)) {
            return new String[]{
                    devicePresetKey + "_bass",
                    devicePresetKey + "-bass",
                    devicePresetKey + "_bass_heavy",
                    devicePresetKey + "-bass-heavy",
                    "bass_" + devicePresetKey,
                    "bass-" + devicePresetKey,
                    "bass_heavy_" + devicePresetKey,
                    "bass-heavy-" + devicePresetKey
            };
        }

        return new String[]{
                devicePresetKey,
                devicePresetKey + "_vocal",
                devicePresetKey + "-vocal",
                devicePresetKey + "_vocal_heavy",
                devicePresetKey + "-vocal-heavy",
                "vocal_" + devicePresetKey,
                "vocal-" + devicePresetKey,
                "vocal_heavy_" + devicePresetKey,
                "vocal-heavy-" + devicePresetKey
        };
    }

    private static String presetKeyForDevice(int device) {
        switch (device) {
            case DeviceProfile.DEVICE_NP1:
                return "np1";
            case DeviceProfile.DEVICE_NP2:
                return "np2";
            case DeviceProfile.DEVICE_NP2A:
                return "np2a";
            case DeviceProfile.DEVICE_NP3A:
                return "np3a";
            case DeviceProfile.DEVICE_NP4A:
                return "np4a";
            default:
                return null;
        }
    }

    private static String phoneModelForDevice(int device) {
        switch (device) {
            case DeviceProfile.DEVICE_NP1:
                return PHONE_MODEL_PHONE1;
            case DeviceProfile.DEVICE_NP2:
                return PHONE_MODEL_PHONE2;
            case DeviceProfile.DEVICE_NP2A:
                return PHONE_MODEL_PHONE2A;
            case DeviceProfile.DEVICE_NP3A:
                return PHONE_MODEL_PHONE3A;
            case DeviceProfile.DEVICE_NP4A:
                return PHONE_MODEL_PHONE4A;
            default:
                return PHONE_MODEL_UNKNOWN;
        }
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
            return clamp(value, 0f, 100f);
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

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private static float clamp01(float value) {
        return clamp(value, 0f, 1f);
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
                String.valueOf(Build.MANUFACTURER) + " "
                        + String.valueOf(Build.BRAND) + " "
                        + String.valueOf(Build.MODEL) + " "
                        + String.valueOf(Build.DEVICE) + " "
                        + String.valueOf(Build.PRODUCT)
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
