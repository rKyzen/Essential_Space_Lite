package com.better.nothing.music.vizualizer;

import com.nothing.ketchum.Common;

/**
 * Zone configuration for each Nothing Phone model.
 *
 * VOCAL HEAVY — ports musicViz.py np1/np2 signal chain exactly:
 *   60fps Hann FFT → peak per zone → decay(0.94) → per-zone quadratic norm → threshold
 *   Zone index maps directly to glyph channel index per README.
 *
 * BASS HEAVY — dual-layer adaptive engine (transient + sustain):
 *   Each device maps available glyphs to frequency bands as sensibly as possible.
 *
 * ── Phone 1 (20111) — 15 glyphs ──────────────────────────────────────────
 *   A1=0  B1=1  C1-C4=2-5  E1=6  D1_1-D1_8=7-14
 *
 * ── Phone 2 (22111) — 33 glyphs ──────────────────────────────────────────
 *   A1=0 A2=1 B1=2 C1_1-C1_16=3-18 C2-C6=19-23 E1=24 D1_1-D1_8=25-32
 *
 * ── Phone 2a / 2a Plus (23111/23113) — 26 glyphs ─────────────────────────
 *   C1-C24=0-23  B=24  A=25
 *
 * ── Phone 3a / 3a Pro (24111) — 36 glyphs ────────────────────────────────
 *   C1-C20=0-19  A1-A11=20-30  B1-B5=31-35
 */
public class DeviceProfile {

    public static final int DEVICE_NP1  = 1;
    public static final int DEVICE_NP2  = 2;
    public static final int DEVICE_NP2A = 3;
    public static final int DEVICE_NP3A = 4;
    public static final int DEVICE_NP4A = 5;
    public static final int DEVICE_UNKNOWN = 0;

    /** Auto-detect which device we're on */
    public static int detectDevice() {
        if (Common.is20111()) return DEVICE_NP1;
        if (Common.is22111()) return DEVICE_NP2;
        if (Common.is23111() || Common.is23113()) return DEVICE_NP2A;
        if (Common.is24111()) return DEVICE_NP3A;
        // Phone (4a) — model 25111
        if (Common.is25111()) return DEVICE_NP4A;
        return DEVICE_UNKNOWN;
    }

    public static String deviceName(int device) {
        switch (device) {
            case DEVICE_NP1:  return "Phone (1)";
            case DEVICE_NP2:  return "Phone (2)";
            case DEVICE_NP2A: return "Phone (2a) / 2a+";
            case DEVICE_NP3A: return "Phone (3a) / 3a Pro";
            case DEVICE_NP4A: return "Phone (4a)";
            default:          return "Unknown";
        }
    }

    public static String deviceCode(int device) {
        switch (device) {
            case DEVICE_NP1:  return "20111";
            case DEVICE_NP2:  return "22111";
            case DEVICE_NP2A: return "23111";
            case DEVICE_NP3A: return "24111";
            case DEVICE_NP4A: return "25111";
            default:          return "22111";
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // VOCAL HEAVY zone tables
    // Each row: {loHz, hiHz, glyphIndex}
    // ══════════════════════════════════════════════════════════════════════

    /** Phone 1 — np1 config port, 15 zones */
    public static final float[][] VOCAL_NP1 = {
        // {loHz, hiHz, glyphIndex}
        {200,  600,   0},   // A1   — camera glyph
        {2000, 6000,  1},   // B1   — slash glyph
        {120,  170,   2},   // C1_1 — center bottom-left
        {170,  250,   3},   // C1_2 — center bottom-right
        {20,    90,   4},   // C1_3 — center top-right
        {90,   120,   5},   // C1_4 — center top-left
        {6000,20000,  6},   // E1   — bottom dot
        {1700, 3000,  14},  // D1_8 — battery bottom (nearest dot)
        {1500, 1700,  13},  // D1_7
        {1250, 1500,  12},  // D1_6
        {1060, 1250,  11},  // D1_5
        {875,  1060,  10},  // D1_4
        {687,   875,   9},  // D1_3
        {500,   687,   8},  // D1_2
        {300,   500,   7},  // D1_1 — battery top
    };

    /** Phone 2 — np2 config port, 33 zones */
    public static final float[][] VOCAL_NP2 = {
        {100,   520,   0},   // A1
        {520,  1250,   1},   // A2
        {1500,15000,   2},   // B1
        {60,    85,    3},   // C1_1
        {85,   121,    4},   // C1_2
        {121,  172,    5},   // C1_3
        {172,  243,    6},   // C1_4
        {243,  345,    7},   // C1_5
        {345,  489,    8},   // C1_6
        {489,  693,    9},   // C1_7
        {693,  982,   10},   // C1_8
        {982, 1391,   11},   // C1_9
        {1391,1970,   12},   // C1_10
        {1970,2791,   13},   // C1_11
        {2791,3953,   14},   // C1_12
        {3953,5600,   15},   // C1_13
        {5600,7931,   16},   // C1_14
        {7931,11234,  17},   // C1_15
        {11234,16000, 18},   // C1_16
        {60,   184,   19},   // C2
        {184,  568,   20},   // C3
        {568, 1746,   21},   // C4
        {1746,5371,   22},   // C5
        {5371,16000,  23},   // C6
        {4000,20000,  24},   // E1  (pct clamp 0-70 handled in code)
        {50,   160,   25},   // D1_1 (cumulative bass fill)
        {50,   160,   26},   // D1_2
        {50,   160,   27},   // D1_3
        {50,   160,   28},   // D1_4
        {50,   160,   29},   // D1_5
        {50,   160,   30},   // D1_6
        {50,   160,   31},   // D1_7
        {50,   160,   32},   // D1_8
    };

    /**
     * Phone 2a — 26 glyphs: C1-C24=0-23, B=24, A=25
     * Design: C1-C24 as 24-band spectrum (log-spaced 20Hz-20kHz)
     *         A (25) = bass accent, B (24) = treble accent
     */
    public static final float[][] VOCAL_NP2A;
    static {
        // 24 log-spaced bands from 20Hz to 20000Hz across C1-C24 (indices 0-23)
        // Plus A (idx 25) for bass, B (idx 24) for treble
        double logMin = Math.log10(20);
        double logMax = Math.log10(20000);
        VOCAL_NP2A = new float[26][3];
        for (int i = 0; i < 24; i++) {
            double lo = Math.pow(10, logMin + (logMax - logMin) * i / 24.0);
            double hi = Math.pow(10, logMin + (logMax - logMin) * (i + 1) / 24.0);
            VOCAL_NP2A[i][0] = (float) lo;
            VOCAL_NP2A[i][1] = (float) hi;
            VOCAL_NP2A[i][2] = i; // C1-C24 = index 0-23
        }
        // A (25) = bass accent 30-120Hz
        VOCAL_NP2A[24][0] = 30; VOCAL_NP2A[24][1] = 120; VOCAL_NP2A[24][2] = 25;
        // B (24) = treble accent 8000-20000Hz
        VOCAL_NP2A[25][0] = 8000; VOCAL_NP2A[25][1] = 20000; VOCAL_NP2A[25][2] = 24;
    }

    /**
     * Phone 3a/3a Pro — 36 glyphs: C1-C20=0-19, A1-A11=20-30, B1-B5=31-35
     * Design:
     *   C1-C20 (0-19): 20-band spectrum arc (main visual bar)
     *   A1-A11 (20-30): 11-band mid/upper spectrum
     *   B1-B5  (31-35): 5-band high freq
     */
    public static final float[][] VOCAL_NP3A;
    static {
        VOCAL_NP3A = new float[36][3];
        double logMin = Math.log10(20);
        double logMax = Math.log10(20000);

        // C1-C20 (0-19): full spectrum 20Hz-20kHz, 20 bands
        for (int i = 0; i < 20; i++) {
            double lo = Math.pow(10, logMin + (logMax - logMin) * i / 20.0);
            double hi = Math.pow(10, logMin + (logMax - logMin) * (i + 1) / 20.0);
            VOCAL_NP3A[i][0] = (float) lo;
            VOCAL_NP3A[i][1] = (float) hi;
            VOCAL_NP3A[i][2] = i;
        }
        // A1-A11 (20-30): 11 bands 80Hz-8kHz (mid emphasis)
        double aLogMin = Math.log10(80), aLogMax = Math.log10(8000);
        for (int i = 0; i < 11; i++) {
            double lo = Math.pow(10, aLogMin + (aLogMax - aLogMin) * i / 11.0);
            double hi = Math.pow(10, aLogMin + (aLogMax - aLogMin) * (i + 1) / 11.0);
            VOCAL_NP3A[20 + i][0] = (float) lo;
            VOCAL_NP3A[20 + i][1] = (float) hi;
            VOCAL_NP3A[20 + i][2] = 20 + i;
        }
        // B1-B5 (31-35): 5 bands 3kHz-20kHz (treble)
        double bLogMin = Math.log10(3000), bLogMax = Math.log10(20000);
        for (int i = 0; i < 5; i++) {
            double lo = Math.pow(10, bLogMin + (bLogMax - bLogMin) * i / 5.0);
            double hi = Math.pow(10, bLogMin + (bLogMax - bLogMin) * (i + 1) / 5.0);
            VOCAL_NP3A[31 + i][0] = (float) lo;
            VOCAL_NP3A[31 + i][1] = (float) hi;
            VOCAL_NP3A[31 + i][2] = 31 + i;
        }
    }


    /**
     * Phone (4a) — 6 LEDs only: A1-A6 = indices 0-5
     * 6 log-spaced frequency bands mapped to each LED.
     * Bass → A6 (bottom), Treble → A1 (top)
     */
    public static final float[][] VOCAL_NP4A = {
        {4000, 20000,  0},  // A1 — treble / air
        {1500,  4000,  1},  // A2 — upper mids
        { 500,  1500,  2},  // A3 — mids
        { 180,   500,  3},  // A4 — upper bass
        {  80,   180,  4},  // A5 — bass
        {  20,    80,  5},  // A6 — sub-bass
    };

    // Bass heavy: same 6 bands
    public static final float[][] BASS_NP4A_BANDS = {
        {20,    80,  5},   // sub-bass → A6
        {80,   180,  4},   // bass     → A5
        {180,  500,  3},   // upper bass→ A4
        {500, 1500,  2},   // mids     → A3
        {1500,4000,  1},   // hi-mids  → A2
        {4000,20000, 0},   // treble   → A1
    };
    public static final int[] BASS_NP4A_VU_INDICES = {0, 1, 2, 3, 4, 5};
    public static final int   BASS_NP4A_E1   = -1;
    public static final int   BASS_NP4A_RING = -1;

    public static float[][] getVocalZones(int device) {
        switch (device) {
            case DEVICE_NP1:  return VOCAL_NP1;
            case DEVICE_NP2:  return VOCAL_NP2;
            case DEVICE_NP2A: return VOCAL_NP2A;
            case DEVICE_NP3A: return VOCAL_NP3A;
            case DEVICE_NP4A: return VOCAL_NP4A;
            default:          return VOCAL_NP2;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // BASS HEAVY band→channel mappings
    // Each row: {loHz, hiHz, glyphIndex}
    // Plus special handling for VU meters and B1 ring in code
    // ══════════════════════════════════════════════════════════════════════

    /** Phone 1 bass bands — 6 perceptual bands */
    public static final float[][] BASS_NP1_BANDS = {
        {20,    90,  4},   // sub-bass → C1_3
        {90,   200,  5},   // bass     → C1_4
        {200,  600,  2},   // upper bass→ C1_1
        {600, 2000,  3},   // low mids → C1_2
        {200,  600,  0},   // mid-bass → A1
        {2000, 8000, 1},   // hi-mids  → B1
    };
    // D1_1–D1_8 = 7–14, used as RMS VU bar
    public static final int[] BASS_NP1_VU_INDICES = {7,8,9,10,11,12,13,14};
    public static final int   BASS_NP1_E1 = 6;
    public static final int   BASS_NP1_RING = -1; // no ring on NP1

    /** Phone 2 bass bands */
    public static final float[][] BASS_NP2_BANDS = {
        {20,    80,  23},  // sub-bass → C6
        {80,   180,  22},  // bass     → C5
        {180,  400,  21},  // upper bass→ C4
        {400, 1000,   0},  // low mids → A1
        {1000,3000,   1},  // center mids→ A2
        {3000,6000,   2},  // hi mids  → B1 (also ring)
        {6000,12000, 19},  // air      → C2
        {12000,20000,20},  // sparkle  → C3
    };
    // C1 arc 3-18 = VU meter, D1 25-32 = peak bar
    public static final int[] BASS_NP2_C1_INDICES = {3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18};
    public static final int[] BASS_NP2_D1_INDICES = {25,26,27,28,29,30,31,32};
    public static final int   BASS_NP2_E1   = 24;
    public static final int   BASS_NP2_RING = 2; // B1

    /** Phone 2a bass bands — C1-C24=0-23, B=24, A=25 */
    public static final float[][] BASS_NP2A_BANDS = {
        {20,    80,  23},  // sub-bass → C24 (top-right)
        {80,   180,  22},  // bass     → C23
        {180,  400,  21},  // upper bass→ C22
        {400, 1000,  20},  // low mids → C21
        {1000,3000,  19},  // mids     → C20
        {3000,8000,  25},  // hi mids  → A (large glyph)
        {8000,20000, 24},  // treble   → B (straight glyph)
    };
    // C1-C16 (0-15) = VU arc
    public static final int[] BASS_NP2A_VU_INDICES = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    public static final int   BASS_NP2A_E1   = -1; // no E1 on 2a
    public static final int   BASS_NP2A_RING = 25; // A glyph as "ring"

    /** Phone 3a bass bands — C1-C20=0-19, A1-A11=20-30, B1-B5=31-35 */
    public static final float[][] BASS_NP3A_BANDS = {
        {20,    80,  19},  // sub-bass → C20
        {80,   180,  18},  // bass     → C19
        {180,  400,  17},  // upper bass→ C18
        {400, 1000,  30},  // low mids → A11
        {1000,3000,  29},  // mids     → A10
        {3000,6000,  35},  // hi-mids  → B5
        {6000,12000, 34},  // air      → B4
        {12000,20000,33},  // sparkle  → B3
    };
    // C1-C16 (0-15) = VU arc, A1-A8 (20-27) = peak bar
    public static final int[] BASS_NP3A_C_INDICES = {0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15};
    public static final int[] BASS_NP3A_A_INDICES = {20,21,22,23,24,25,26,27};
    public static final int   BASS_NP3A_E1   = -1; // no E1 on 3a
    public static final int   BASS_NP3A_RING = -1; // use B1 (31) as ring
    public static final int   BASS_NP3A_B1   = 31;
}
