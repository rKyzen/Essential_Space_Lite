package com.betterglyph;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.service.quicksettings.TileService;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    // ── Pages ──────────────────────────────────────────────────────────────
    private static final int PAGE_AUDIO  = 0;
    private static final int PAGE_GLYPHS = 1;
    private static final int PAGE_ABOUT  = 2;
    private int mCurrentPage = PAGE_AUDIO;

    // ── Root views ─────────────────────────────────────────────────────────
    private FrameLayout   pageContainer;
    private LinearLayout  navAudio, navGlyphs, navAbout;
    private ImageView     navAudioIcon, navGlyphsIcon, navAboutIcon;
    private TextView      navAudioLabel, navGlyphsLabel, navAboutLabel;

    // ── Audio page views ───────────────────────────────────────────────────
    private View          audioPage;
    private MaterialButton btnToggle;
    private View          statusDot;
    private TextView      tvStatus;
    private LinearLayout  latencySection;
    private DotSeekBar    seekLatency;
    private TextView      tvMarker0, tvMarker1, tvMarker2;

    // ── Glyphs page views ──────────────────────────────────────────────────
    private View          glyphsPage;
    private DotSeekBar    seekGamma;
    private TextView      tvGammaLabel;
    private LinearLayout  presetCarousel;
    private TextView      tvPresetName, tvPresetDesc;
    private TextView      tvDeviceLabel;
    private Spinner       spinnerDevice;

    // ── About page ─────────────────────────────────────────────────────────
    private View          aboutPage;

    // ── State ──────────────────────────────────────────────────────────────
    private boolean mIsRunning      = false;
    private String  mSelectedPreset = "";
    private int     mSelectedDevice = DeviceProfile.DEVICE_UNKNOWN;
    private float   mGamma          = AudioCaptureService.DEFAULT_GAMMA;
    private int     mLatencyMs      = 0;

    private final List<AudioCaptureService.PresetInfo> mPresetInfos   = new ArrayList<>();
    private final List<TextView>                       mPresetChips   = new ArrayList<>();

    // ── Beat ───────────────────────────────────────────────────────────────
    private final Handler mBeatHandler = new Handler(Looper.getMainLooper());
    private long          mLastBeat    = 0;

    // ── Service ────────────────────────────────────────────────────────────
    private AudioCaptureService mService = null;
    private boolean mBound   = false;
    private int     mPending = 0;
    private Intent  mPendData = null;
    private boolean mHasTok  = false;

    private final ServiceConnection mConn = new ServiceConnection() {
        @Override public void onServiceConnected(ComponentName n, IBinder b) {
            mService = ((AudioCaptureService.LocalBinder) b).getService();
            mBound = true;
            mService.setDevice(mSelectedDevice);
            if (!mSelectedPreset.isEmpty()) mService.setPreset(mSelectedPreset);
            mService.setLatencyCompensationMs(mLatencyMs);
            mService.setGamma(mGamma);
            mService.setBeatCallback(() -> mBeatHandler.post(MainActivity.this::onBeat));
            if (mHasTok && mPendData != null) {
                mService.startCapture(mPending, mPendData);
                mPending = 0; mPendData = null; mHasTok = false;
            }
        }
        @Override public void onServiceDisconnected(ComponentName n) {
            mService = null; mBound = false;
        }
    };

    private MediaProjectionManager mProjMgr;

    private final ActivityResultLauncher<Intent> mProjLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    deliverToken(result.getResultCode(), result.getData());
                } else {
                    Toast.makeText(this, "Audio capture permission denied", Toast.LENGTH_SHORT).show();
                    setRunning(false);
                }
            });

    private final ActivityResultLauncher<String> mNotifLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                    granted -> launchProjection());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Root
        pageContainer = findViewById(R.id.pageContainer);
        navAudio      = findViewById(R.id.navAudio);
        navGlyphs     = findViewById(R.id.navGlyphs);
        navAbout      = findViewById(R.id.navAbout);
        navAudioIcon  = findViewById(R.id.navAudioIcon);
        navGlyphsIcon = findViewById(R.id.navGlyphsIcon);
        navAboutIcon  = findViewById(R.id.navAboutIcon);
        navAudioLabel = findViewById(R.id.navAudioLabel);
        navGlyphsLabel= findViewById(R.id.navGlyphsLabel);
        navAboutLabel = findViewById(R.id.navAboutLabel);

        mProjMgr = (MediaProjectionManager) getSystemService(MEDIA_PROJECTION_SERVICE);

        inflatePages();
        setupGlyphsPage();
        setupNavigation();
        showPage(PAGE_AUDIO, false);
        reloadPresets();
    }

    @Override protected void onDestroy() {
        if (mBound) { unbindService(mConn); mBound = false; }
        mBeatHandler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Page inflation
    // ══════════════════════════════════════════════════════════════════════
    private void inflatePages() {
        LayoutInflater inf = LayoutInflater.from(this);

        // Audio page
        audioPage = inf.inflate(R.layout.page_audio, pageContainer, false);
        btnToggle     = audioPage.findViewById(R.id.btnToggle);
        statusDot     = audioPage.findViewById(R.id.statusDot);
        tvStatus      = audioPage.findViewById(R.id.tvStatus);
        latencySection= audioPage.findViewById(R.id.latencySection);
        seekLatency   = audioPage.findViewById(R.id.seekLatency);
        tvMarker0     = audioPage.findViewById(R.id.tvMarker0);
        tvMarker1     = audioPage.findViewById(R.id.tvMarker1);
        tvMarker2     = audioPage.findViewById(R.id.tvMarker2);
        pageContainer.addView(audioPage);

        btnToggle.setOnClickListener(v -> onToggleClicked());

        seekLatency.setMax(300);
        seekLatency.setOnProgressChangedListener((bar, p, fromUser) -> {
            if (!fromUser) return;
            mLatencyMs = AudioCaptureService.clampLatencyCompensationMs(p);
            AudioCaptureService.saveLatencyCompensationMs(this, mSelectedDevice, mLatencyMs);
            if (mBound && mService != null) mService.setLatencyCompensationMs(mLatencyMs);
            updateMarkers(mLatencyMs);
        });

        // Glyphs page
        glyphsPage  = inf.inflate(R.layout.page_glyphs, pageContainer, false);
        seekGamma   = glyphsPage.findViewById(R.id.seekGamma);
        tvGammaLabel= glyphsPage.findViewById(R.id.tvGammaLabel);
        presetCarousel= glyphsPage.findViewById(R.id.presetCarousel);
        tvPresetName= glyphsPage.findViewById(R.id.tvPresetName);
        tvPresetDesc= glyphsPage.findViewById(R.id.tvPresetDesc);
        tvDeviceLabel= glyphsPage.findViewById(R.id.tvDeviceLabel);
        spinnerDevice= glyphsPage.findViewById(R.id.spinnerDevice);
        pageContainer.addView(glyphsPage);
        glyphsPage.setVisibility(View.GONE);

        // About page
        aboutPage = inf.inflate(R.layout.page_about, pageContainer, false);
        pageContainer.addView(aboutPage);
        aboutPage.setVisibility(View.GONE);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Glyphs page setup
    // ══════════════════════════════════════════════════════════════════════
    private final int[] DEVICE_IDS = {
        DeviceProfile.DEVICE_NP1, DeviceProfile.DEVICE_NP2,
        DeviceProfile.DEVICE_NP2A, DeviceProfile.DEVICE_NP3A, DeviceProfile.DEVICE_NP4A
    };

    private void setupGlyphsPage() {
        // Gamma slider
        mGamma = AudioCaptureService.loadGamma(this);
        seekGamma.setMax(AudioCaptureService.GAMMA_SLIDER_STEPS);
        seekGamma.setProgress(AudioCaptureService.gammaToSliderProgress(mGamma));
        updateGammaLabel(mGamma);
        seekGamma.setOnProgressChangedListener((bar, p, fromUser) -> {
            float g = AudioCaptureService.gammaFromSliderProgress(p);
            mGamma = AudioCaptureService.clampGamma(g);
            updateGammaLabel(mGamma);
            AudioCaptureService.saveGamma(this, mGamma);
            if (mBound && mService != null) mService.setGamma(mGamma);
        });

        // Device spinner
        int detected = DeviceProfile.detectDevice();
        mSelectedDevice = detected != DeviceProfile.DEVICE_UNKNOWN ? detected : DeviceProfile.DEVICE_NP2;

        String[] names = {"Phone (1)", "Phone (2)", "Phone (2a) / 2a+",
                          "Phone (3a) / 3a Pro", "Phone (4a)"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDevice.setAdapter(adapter);

        for (int i = 0; i < DEVICE_IDS.length; i++)
            if (DEVICE_IDS[i] == mSelectedDevice) { spinnerDevice.setSelection(i); break; }

        tvDeviceLabel.setText(detected != DeviceProfile.DEVICE_UNKNOWN
                ? "Auto-detected: " + DeviceProfile.deviceName(detected)
                : "Not detected — select manually");

        spinnerDevice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int pos, long id) {
                int dev = DEVICE_IDS[pos];
                boolean changed = dev != mSelectedDevice;
                mSelectedDevice = dev;
                if (changed) {
                    if (mIsRunning) {
                        stopEverything(); setRunning(false);
                        Toast.makeText(MainActivity.this,
                                "Device changed — restart to apply", Toast.LENGTH_SHORT).show();
                    }
                    mLatencyMs = AudioCaptureService.loadLatencyCompensationMs(
                            MainActivity.this, mSelectedDevice);
                    seekLatency.setProgress(mLatencyMs);
                    reloadPresets();
                    if (mBound && mService != null) mService.setDevice(mSelectedDevice);
                }
            }
            @Override public void onNothingSelected(AdapterView<?> p) {}
        });

        // Load saved latency
        mLatencyMs = AudioCaptureService.loadLatencyCompensationMs(this, mSelectedDevice);
        seekLatency.setProgress(mLatencyMs);
        updateMarkers(mLatencyMs);
    }

    private void updateGammaLabel(float g) {
        tvGammaLabel.setText(String.format(Locale.US, "Light Gamma: %.2f", g));
    }

    private void updateMarkers(int ms) {
        // Show current value on middle marker
        tvMarker1.setText(ms + "ms");
    }

    // ══════════════════════════════════════════════════════════════════════
    // Preset chips
    // ══════════════════════════════════════════════════════════════════════
    private void reloadPresets() {
        List<AudioCaptureService.PresetInfo> list =
                AudioCaptureService.loadPresetInfos(this, mSelectedDevice);
        mPresetInfos.clear(); mPresetInfos.addAll(list);
        if (findPreset(mSelectedPreset) == null && !mPresetInfos.isEmpty())
            mSelectedPreset = mPresetInfos.get(0).key;
        buildChips();
        updatePresetCard();
    }

    private void buildChips() {
        presetCarousel.removeAllViews();
        mPresetChips.clear();
        int gapDp = dp(8);
        for (int i = 0; i < mPresetInfos.size(); i++) {
            AudioCaptureService.PresetInfo info = mPresetInfos.get(i);
            TextView chip = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, dp(36));
            lp.setMarginEnd(gapDp);
            chip.setLayoutParams(lp);
            chip.setText(info.key);
            chip.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
            chip.setPaddingRelative(dp(16), 0, dp(16), 0);
            chip.setGravity(android.view.Gravity.CENTER);
            chip.setSingleLine(true);
            chip.setTag(info.key);
            chip.setOnClickListener(v -> selectPreset(info.key));
            presetCarousel.addView(chip);
            mPresetChips.add(chip);
        }
        refreshChipStyles();
    }

    private void selectPreset(String key) {
        if (key == null || key.isEmpty()) return;
        mSelectedPreset = key;
        refreshChipStyles();
        updatePresetCard();
        if (mBound && mService != null) mService.setPreset(key);
    }

    private void refreshChipStyles() {
        for (TextView chip : mPresetChips) {
            boolean sel = mSelectedPreset.equals(chip.getTag());
            chip.setBackgroundResource(sel ? R.drawable.bg_preset_selected : R.drawable.bg_preset_unselected);
            chip.setTextColor(sel ? 0xFF0A0A0A : 0xFF888888);
        }
    }

    private void updatePresetCard() {
        AudioCaptureService.PresetInfo info = findPreset(mSelectedPreset);
        if (info != null) {
            tvPresetName.setText(info.key);
            tvPresetDesc.setText(info.description);
        }
    }

    private AudioCaptureService.PresetInfo findPreset(String key) {
        for (AudioCaptureService.PresetInfo i : mPresetInfos)
            if (i.key.equals(key)) return i;
        return null;
    }

    // ══════════════════════════════════════════════════════════════════════
    // Navigation
    // ══════════════════════════════════════════════════════════════════════
    private void setupNavigation() {
        navAudio.setOnClickListener(v  -> showPage(PAGE_AUDIO,  true));
        navGlyphs.setOnClickListener(v -> showPage(PAGE_GLYPHS, true));
        navAbout.setOnClickListener(v  -> showPage(PAGE_ABOUT,  true));
    }

    private void showPage(int page, boolean animate) {
        if (page == mCurrentPage && animate) return;
        mCurrentPage = page;

        // Animate out current, animate in new
        View[] pages = {audioPage, glyphsPage, aboutPage};
        for (int i = 0; i < pages.length; i++) {
            if (pages[i] == null) continue;
            if (i == page) {
                pages[i].setVisibility(View.VISIBLE);
                if (animate) {
                    pages[i].setAlpha(0f);
                    pages[i].setTranslationY(12f);
                    pages[i].animate().alpha(1f).translationY(0f)
                        .setDuration(220).setInterpolator(new DecelerateInterpolator(2f)).start();
                }
            } else {
                pages[i].setVisibility(View.GONE);
            }
        }

        // Nav highlight
        styleNav(navAudioIcon,  navAudioLabel,  page == PAGE_AUDIO);
        styleNav(navGlyphsIcon, navGlyphsLabel, page == PAGE_GLYPHS);
        styleNav(navAboutIcon,  navAboutLabel,  page == PAGE_ABOUT);
    }

    private void styleNav(ImageView icon, TextView label, boolean active) {
        float alpha = active ? 1.0f : 0.38f;
        icon.animate().alpha(alpha).setDuration(180).start();
        label.animate().alpha(active ? 1.0f : 0.5f).setDuration(180).start();
        label.setTextColor(active ? 0xFFFFFFFF : 0xFF666666);
    }

    // ══════════════════════════════════════════════════════════════════════
    // Toggle start/stop
    // ══════════════════════════════════════════════════════════════════════
    private void onToggleClicked() {
        btnToggle.animate().scaleX(0.96f).scaleY(0.96f).setDuration(60)
            .withEndAction(() -> btnToggle.animate().scaleX(1f).scaleY(1f)
                .setDuration(150).setInterpolator(new DecelerateInterpolator(2f)).start())
            .start();

        if (mIsRunning) {
            stopEverything();
            setRunning(false);
        } else {
            if (mSelectedPreset.isEmpty() && !mPresetInfos.isEmpty())
                mSelectedPreset = mPresetInfos.get(0).key;
            requestPermissionsAndStart();
        }
    }

    private void requestPermissionsAndStart() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            mNotifLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            return;
        }
        launchProjection();
    }

    private void launchProjection() {
        mProjLauncher.launch(mProjMgr.createScreenCaptureIntent());
    }

    private void deliverToken(int code, Intent data) {
        Intent svc = new Intent(this, AudioCaptureService.class);
        svc.putExtra(AudioCaptureService.EXTRA_PRESET_KEY, mSelectedPreset);
        ContextCompat.startForegroundService(this, svc);
        if (mBound && mService != null) {
            applyServiceSettings();
            mService.startCapture(code, data);
        } else {
            mPending = code; mPendData = data; mHasTok = true;
            bindService(svc, mConn, BIND_AUTO_CREATE);
        }
        setRunning(true);
    }

    private void applyServiceSettings() {
        mService.setDevice(mSelectedDevice);
        if (!mSelectedPreset.isEmpty()) mService.setPreset(mSelectedPreset);
        mService.setLatencyCompensationMs(mLatencyMs);
        mService.setGamma(mGamma);
        mService.setBeatCallback(() -> mBeatHandler.post(this::onBeat));
    }

    private void stopEverything() {
        if (mBound && mService != null) mService.stopCapture();
        if (mBound) { unbindService(mConn); mBound = false; mService = null; }
        mPending = 0; mPendData = null; mHasTok = false;
        stopService(new Intent(this, AudioCaptureService.class));
    }

    private void setRunning(boolean running) {
        mIsRunning = running;
        TileService.requestListeningState(this,
                new ComponentName(this, VisualizerTileService.class));
        runOnUiThread(() -> {
            if (running) {
                btnToggle.setText("Stop  ■");
                btnToggle.setBackgroundTintList(
                        ColorStateList.valueOf(0xFFF87171)); // red
                btnToggle.setTextColor(0xFFFFFFFF);
                latencySection.setVisibility(View.VISIBLE);
                statusDot.setBackgroundResource(R.drawable.dot_active);
                tvStatus.setText("Listening — Glyphs active");
                tvStatus.setTextColor(0xFF4ADE80);
            } else {
                btnToggle.setText("Start  ▶");
                btnToggle.setBackgroundTintList(
                        ColorStateList.valueOf(0xFF4ADE80)); // green
                btnToggle.setTextColor(0xFF000000);
                latencySection.setVisibility(View.GONE);
                statusDot.setBackgroundResource(R.drawable.dot_inactive);
                tvStatus.setText("Not running");
                tvStatus.setTextColor(0xFF555555);
            }
        });
    }

    // ══════════════════════════════════════════════════════════════════════
    // Beat callback
    // ══════════════════════════════════════════════════════════════════════
    private void onBeat() {
        long now = System.currentTimeMillis();
        if (now - mLastBeat < 120) return;
        mLastBeat = now;
        // Status dot pulse
        statusDot.animate().scaleX(1.8f).scaleY(1.8f).setDuration(80)
            .withEndAction(() -> statusDot.animate().scaleX(1f).scaleY(1f)
                .setDuration(180).setInterpolator(new DecelerateInterpolator()).start())
            .setInterpolator(new AccelerateDecelerateInterpolator()).start();
    }

    // ══════════════════════════════════════════════════════════════════════
    // Helpers
    // ══════════════════════════════════════════════════════════════════════
    private int dp(int v) {
        return Math.round(v * getResources().getDisplayMetrics().density);
    }
}
