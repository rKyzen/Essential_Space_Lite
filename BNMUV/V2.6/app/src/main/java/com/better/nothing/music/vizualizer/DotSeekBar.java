package com.better.nothing.music.vizualizer;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;

/**
 * Nothing-style slider:
 *   ● Thin dark track spanning full width
 *   ● White/light filled portion from left to thumb
 *   ● Small circular thumb knob (white, slightly larger)
 *   ● Clean, flat, minimal — matches Nothing OS aesthetic
 */
public class DotSeekBar extends View {

    public interface OnProgressChangedListener {
        void onProgressChanged(DotSeekBar bar, int progress, boolean fromUser);
    }

    // Nothing aesthetic: near-white active, dark inactive
    private static final int COLOR_TRACK    = 0xFF1C1C1C;  // very dark grey track
    private static final int COLOR_ACTIVE   = 0xFFEEEEEE;  // near-white fill
    private static final int COLOR_THUMB    = 0xFFFFFFFF;  // pure white thumb
    private static final int COLOR_THUMB_BG = 0xFF111111;  // thumb shadow/border

    private final Paint mTrackPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mActivePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mThumbBgPaint= new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint mThumbPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int   mMax              = 100;
    private int   mProgress         = 0;
    private float mAnimFrac         = 0f;
    private ValueAnimator mAnimator;

    private OnProgressChangedListener mListener;

    private static final float TRACK_H_DP  = 3f;   // track height
    private static final float THUMB_R_DP  = 9f;   // thumb radius
    private static final float THUMB_BG_DP = 11f;  // thumb shadow ring

    public DotSeekBar(Context ctx) { super(ctx); init(); }
    public DotSeekBar(Context ctx, AttributeSet a) { super(ctx, a); init(); }
    public DotSeekBar(Context ctx, AttributeSet a, int s) { super(ctx, a, s); init(); }

    private void init() {
        mTrackPaint.setColor(COLOR_TRACK);
        mTrackPaint.setStyle(Paint.Style.FILL);

        mActivePaint.setColor(COLOR_ACTIVE);
        mActivePaint.setStyle(Paint.Style.FILL);

        mThumbBgPaint.setColor(COLOR_THUMB_BG);
        mThumbBgPaint.setStyle(Paint.Style.FILL);

        mThumbPaint.setColor(COLOR_THUMB);
        mThumbPaint.setStyle(Paint.Style.FILL);
    }

    public void setMax(int max) { mMax = Math.max(1, max); invalidate(); }
    public int  getMax()        { return mMax; }
    public int  getProgress()   { return mProgress; }

    public void setProgress(int p) {
        int clamped = Math.max(0, Math.min(mMax, p));
        if (clamped == mProgress) return;
        mProgress = clamped;
        float target = mProgress / (float) mMax;
        if (mAnimator != null) mAnimator.cancel();
        mAnimator = ValueAnimator.ofFloat(mAnimFrac, target);
        mAnimator.setDuration(90);
        mAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        mAnimator.addUpdateListener(a -> { mAnimFrac = (float) a.getAnimatedValue(); invalidate(); });
        mAnimator.start();
    }

    public void setOnProgressChangedListener(OnProgressChangedListener l) { mListener = l; }

    @Override
    protected void onDraw(Canvas canvas) {
        float w   = getWidth();
        float h   = getHeight();
        float dp  = getContext().getResources().getDisplayMetrics().density;

        float trackH   = TRACK_H_DP  * dp;
        float thumbR   = THUMB_R_DP  * dp;
        float thumbBgR = THUMB_BG_DP * dp;

        // Track occupies full width with thumb-radius padding on each side
        float pad    = thumbBgR;
        float trackL = pad;
        float trackR = w - pad;
        float trackW = trackR - trackL;
        float trackY = h / 2f;

        // Thumb X
        float thumbX = trackL + mAnimFrac * trackW;
        float thumbX_clamped = Math.max(trackL, Math.min(trackR, thumbX));

        // Full inactive track
        RectF fullTrack = new RectF(trackL, trackY - trackH / 2f, trackR, trackY + trackH / 2f);
        canvas.drawRoundRect(fullTrack, trackH / 2f, trackH / 2f, mTrackPaint);

        // Active portion (left of thumb)
        if (thumbX_clamped > trackL) {
            RectF activeTrack = new RectF(trackL, trackY - trackH / 2f,
                    thumbX_clamped, trackY + trackH / 2f);
            canvas.drawRoundRect(activeTrack, trackH / 2f, trackH / 2f, mActivePaint);
        }

        // Thumb shadow/border circle
        canvas.drawCircle(thumbX_clamped, trackY, thumbBgR, mThumbBgPaint);
        // Thumb main circle
        canvas.drawCircle(thumbX_clamped, trackY, thumbR, mThumbPaint);
    }

    @Override
    protected void onMeasure(int wSpec, int hSpec) {
        float dp = getContext().getResources().getDisplayMetrics().density;
        int desiredH = (int)(40 * dp);
        int h = resolveSize(desiredH, hSpec);
        super.onMeasure(wSpec, MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY));
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        if (!isEnabled()) return false;
        float dp    = getContext().getResources().getDisplayMetrics().density;
        float pad   = THUMB_BG_DP * dp;
        float trackL= pad;
        float trackW= getWidth() - pad * 2f;
        float rx    = Math.max(0, Math.min(trackW, e.getX() - trackL));
        int   p     = Math.max(0, Math.min(mMax, Math.round(rx / trackW * mMax)));
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                mProgress = p;
                mAnimFrac = rx / trackW;
                invalidate();
                if (mListener != null) mListener.onProgressChanged(this, mProgress, true);
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_UP:
                getParent().requestDisallowInterceptTouchEvent(false);
                return true;
        }
        return super.onTouchEvent(e);
    }
}
