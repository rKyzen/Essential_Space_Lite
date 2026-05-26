package com.essential.spacelite.capture;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000j\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0014\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u0018H\u0002J\u0018\u0010\u0019\u001a\u00020\u00152\u0006\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u001a\u001a\u00020\u001bH\u0002J\u0006\u0010\u001c\u001a\u00020\u0015J\u000e\u0010\u001d\u001a\u00020\u00152\u0006\u0010\u001e\u001a\u00020\u001fJ\u000e\u0010 \u001a\u00020\u00152\u0006\u0010!\u001a\u00020\"J\u0018\u0010#\u001a\u00020\u00152\u0006\u0010!\u001a\u00020\"2\u0006\u0010\u0016\u001a\u00020\u0004H\u0002J \u0010$\u001a\u00020\u00152\u0006\u0010!\u001a\u00020\"2\u0006\u0010\u0016\u001a\u00020\u00042\u0006\u0010%\u001a\u00020\u0004H\u0002J \u0010&\u001a\u00020\u00152\u0006\u0010\u001e\u001a\u00020\u001f2\u0006\u0010\u0016\u001a\u00020\u00042\u0006\u0010%\u001a\u00020\u0004H\u0002J\u0010\u0010\'\u001a\u00020\f2\u0006\u0010\u0016\u001a\u00020\u0004H\u0002J\u000e\u0010(\u001a\u0004\u0018\u00010\u0018*\u00020)H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0005\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\b\u001a\u0004\u0018\u00010\tX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006*"}, d2 = {"Lcom/essential/spacelite/capture/CaptureOrchestrator;", "", "()V", "DIRECT_CAPTURE_GRACE_MS", "", "captureTimeout", "Ljava/lang/Runnable;", "captureToken", "currentObserver", "Lcom/essential/spacelite/capture/ScreenshotObserver;", "fallbackStart", "fallbackStarted", "", "hasDeliveredCapture", "mainExecutor", "Ljava/util/concurrent/Executor;", "mainHandler", "Landroid/os/Handler;", "overlay", "Lcom/essential/spacelite/overlay/CaptureOverlayManager;", "deliverBitmap", "", "token", "bitmap", "Landroid/graphics/Bitmap;", "deliverUri", "uri", "Landroid/net/Uri;", "destroy", "init", "serviceContext", "Landroid/content/Context;", "onTrigger", "service", "Landroid/accessibilityservice/AccessibilityService;", "requestDirectScreenshot", "startFallbackCapture", "triggerTime", "startMediaStoreObserver", "tryResolveCapture", "toSoftwareBitmap", "Landroid/accessibilityservice/AccessibilityService$ScreenshotResult;", "app_release"})
public final class CaptureOrchestrator {
    private static final long DIRECT_CAPTURE_GRACE_MS = 1200L;
    @org.jetbrains.annotations.Nullable()
    private static com.essential.spacelite.overlay.CaptureOverlayManager overlay;
    @org.jetbrains.annotations.Nullable()
    private static com.essential.spacelite.capture.ScreenshotObserver currentObserver;
    @org.jetbrains.annotations.NotNull()
    private static final android.os.Handler mainHandler = null;
    @org.jetbrains.annotations.Nullable()
    private static java.lang.Runnable captureTimeout;
    @org.jetbrains.annotations.Nullable()
    private static java.lang.Runnable fallbackStart;
    private static long captureToken = 0L;
    private static boolean hasDeliveredCapture = false;
    private static boolean fallbackStarted = false;
    @org.jetbrains.annotations.NotNull()
    private static final java.util.concurrent.Executor mainExecutor = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.capture.CaptureOrchestrator INSTANCE = null;
    
    private CaptureOrchestrator() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context serviceContext) {
    }
    
    public final void onTrigger(@org.jetbrains.annotations.NotNull()
    android.accessibilityservice.AccessibilityService service) {
    }
    
    private final void requestDirectScreenshot(android.accessibilityservice.AccessibilityService service, long token) {
    }
    
    private final void startFallbackCapture(android.accessibilityservice.AccessibilityService service, long token, long triggerTime) {
    }
    
    private final void startMediaStoreObserver(android.content.Context serviceContext, long token, long triggerTime) {
    }
    
    private final void deliverBitmap(long token, android.graphics.Bitmap bitmap) {
    }
    
    private final void deliverUri(long token, android.net.Uri uri) {
    }
    
    private final boolean tryResolveCapture(long token) {
        return false;
    }
    
    public final void destroy() {
    }
    
    private final android.graphics.Bitmap toSoftwareBitmap(android.accessibilityservice.AccessibilityService.ScreenshotResult $this$toSoftwareBitmap) {
        return null;
    }
}