package com.betterglyph;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Custom slider that looks like the reference image:
 *   left dots ········  [====thumb====]  ········ right value
 *
 * Dots to the left of thumb = filled/bright
 * Dots to the right = dim
 * Thumb = solid rounded rect with twin vertical bars
 */
public class DotBarSlider extends View {

    public interface OnChangeListener {
        void onProgressChanged(DotBarSlider slider, int progress, boolean fromUser);
    }

    private int   mMax      = 100;
    private int   mProgress = 0;
    private OnChangeListener mListener;

    // Paint objects
    private final Paint mDotOnPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mDotOffPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mThumbPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mThumbBarPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private static final int DOT_COUNT  = 28;   // dots on each side
    private static final float DOT_R    = 3.5f; // dot radius dp
    private static final float DOT_GAP  = 5.5f; // gap between dot centres dp
    private static final float THUMB_W  = 48f;  // thumb width dp
    private static final float THUMB_H  = 28f;  // thumb height dp

    public DotBarSlider(Context c) { super(c); init(); }
    public DotBarSlider(Context c, AttributeSet a) { super(c,a); init(); }
    public DotBarSlider(Context c, AttributeSet a, int d) { super(c,a,d); init(); }

    private void init() {
        mDotOnPaint.setColor(0xFFD4C94A);   // yellow-green active dots
        mDotOffPaint.setColor(0x44D4C94A);  // dim inactive dots
        mThumbPaint.setColor(0xFFD4C94A);
        mThumbBarPaint.setColor(0xFF0D0D0D);
        setClickable(true);
    }

    public void setMax(int max) { mMax = Math.max(1, max); invalidate(); }
    public void setProgress(int p) { mProgress = clamp(p); invalidate(); }
    public int getProgress() { return mProgress; }
    public void setOnChangeListener(OnChangeListener l) { mListener = l; }

    private float dp(float v) { return v * getResources().getDisplayMetrics().density; }
    private int clamp(int v) { return Math.max(0, Math.min(mMax, v)); }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        int h = Math.round(dp(THUMB_H) + dp(8));
        setMeasuredDimension(MeasureSpec.getSize(wSpec), h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float w = getWidth(), h = getHeight();
        float cy = h / 2f;
        float frac = mMax > 0 ? (float) mProgress / mMax : 0f;

        float thumbW = dp(THUMB_W), thumbH = dp(THUMB_H);
        float thumbR = dp(5);
        // Thumb centre x tracks progress across usable width (with padding)
        float pad = thumbW / 2f + dp(4);
        float trackW = w - pad * 2f;
        float thumbCx = pad + frac * trackW;

        // ── Draw dots left of thumb ────────────────────────────────────────
        float dotR   = dp(DOT_R);
        float dotGap = dp(DOT_GAP);
        float dotAreaEnd = thumbCx - thumbW / 2f - dp(6);
        float dotStartLeft = pad - dotR;
        // Pack as many dots as fit left of thumb
        float x = dotStartLeft;
        while (x + dotGap <= dotAreaEnd) {
            canvas.drawCircle(x, cy, dotR, mDotOnPaint);
            x += dotGap;
        }
        // Remaining dots (dim) from right of thumb to right edge
        float dotStartRight = thumbCx + thumbW / 2f + dp(6);
        x = dotStartRight;
        float dotAreaRight = w - pad + dotR;
        while (x + dotR <= dotAreaRight) {
            canvas.drawCircle(x, cy, dotR, mDotOffPaint);
            x += dotGap;
        }

        // ── Draw thumb ─────────────────────────────────────────────────────
        RectF thumbRect = new RectF(thumbCx - thumbW/2, cy - thumbH/2,
                                    thumbCx + thumbW/2, cy + thumbH/2);
        canvas.drawRoundRect(thumbRect, thumbR, thumbR, mThumbPaint);

        // Twin vertical bars inside thumb
        float barW  = dp(2.5f), barH = dp(12), barGap = dp(7);
        float barTop = cy - barH / 2f, barBot = cy + barH / 2f;
        RectF barL = new RectF(thumbCx - barGap/2 - barW, barTop,
                               thumbCx - barGap/2,         barBot);
        RectF barR = new RectF(thumbCx + barGap/2,         barTop,
                               thumbCx + barGap/2 + barW,  barBot);
        canvas.drawRoundRect(barL, dp(1.5f), dp(1.5f), mThumbBarPaint);
        canvas.drawRoundRect(barR, dp(1.5f), dp(1.5f), mThumbBarPaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        float pad = dp(THUMB_W)/2f + dp(4);
        float trackW = getWidth() - pad * 2f;
        float frac = clamp01((e.getX() - pad) / trackW);
        int newProg = clamp(Math.round(frac * mMax));
        if (e.getAction() == MotionEvent.ACTION_DOWN
                || e.getAction() == MotionEvent.ACTION_MOVE) {
            if (newProg != mProgress) {
                mProgress = newProg;
                invalidate();
                if (mListener != null) mListener.onProgressChanged(this, mProgress, true);
            }
            return true;
        }
        return super.onTouchEvent(e);
    }

    private float clamp01(float v) { return Math.max(0f, Math.min(1f, v)); }
}
