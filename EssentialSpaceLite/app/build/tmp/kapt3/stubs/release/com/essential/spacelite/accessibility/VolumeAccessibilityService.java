package com.essential.spacelite.accessibility;

/**
 * Detects simultaneous Vol Up + Vol Down (within 100ms window).
 * Uses the accessibility screenshot API on trigger and falls back to the
 * legacy global screenshot path only if needed.
 *
 * Debounce: 1500ms between triggers.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u0004H\u0002J\b\u0010\u000e\u001a\u00020\fH\u0002J\u0012\u0010\u000f\u001a\u00020\f2\b\u0010\u0010\u001a\u0004\u0018\u00010\u0011H\u0016J\b\u0010\u0012\u001a\u00020\fH\u0016J\b\u0010\u0013\u001a\u00020\fH\u0016J\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0010\u001a\u00020\u0016H\u0014J\b\u0010\u0017\u001a\u00020\fH\u0014R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082D\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0018"}, d2 = {"Lcom/essential/spacelite/accessibility/VolumeAccessibilityService;", "Landroid/accessibilityservice/AccessibilityService;", "()V", "DEBOUNCE_MS", "", "WINDOW_MS", "lastTrigger", "mainHandler", "Landroid/os/Handler;", "volDownTime", "volUpTime", "checkTrigger", "", "now", "fire", "onAccessibilityEvent", "event", "Landroid/view/accessibility/AccessibilityEvent;", "onDestroy", "onInterrupt", "onKeyEvent", "", "Landroid/view/KeyEvent;", "onServiceConnected", "app_release"})
public final class VolumeAccessibilityService extends android.accessibilityservice.AccessibilityService {
    private long volUpTime = 0L;
    private long volDownTime = 0L;
    private long lastTrigger = 0L;
    private final long WINDOW_MS = 100L;
    private final long DEBOUNCE_MS = 1500L;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler mainHandler = null;
    
    public VolumeAccessibilityService() {
        super();
    }
    
    @java.lang.Override()
    protected boolean onKeyEvent(@org.jetbrains.annotations.NotNull()
    android.view.KeyEvent event) {
        return false;
    }
    
    private final void checkTrigger(long now) {
    }
    
    private final void fire() {
    }
    
    @java.lang.Override()
    public void onAccessibilityEvent(@org.jetbrains.annotations.Nullable()
    android.view.accessibility.AccessibilityEvent event) {
    }
    
    @java.lang.Override()
    public void onInterrupt() {
    }
    
    @java.lang.Override()
    protected void onServiceConnected() {
    }
    
    @java.lang.Override()
    public void onDestroy() {
    }
}