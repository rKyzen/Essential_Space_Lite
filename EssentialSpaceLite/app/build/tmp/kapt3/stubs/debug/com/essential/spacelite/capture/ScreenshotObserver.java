package com.essential.spacelite.capture;

/**
 * Detects the image that was just created by the system screenshot action.
 *
 * Some OEM builds do not expose a stable "Screenshot" relative path or name,
 * so we bias toward "latest recent readable image after trigger" instead of
 * relying on one folder naming convention.
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\b\u0018\u00002\u00020\u0001:\u0001\"B)\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0012\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007\u00a2\u0006\u0002\u0010\nJ\u0016\u0010\u0014\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0016J\u0016\u0010\u0017\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0016J\u0016\u0010\u0018\u001a\u00020\t2\u0006\u0010\u0015\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0016J\u0016\u0010\u0019\u001a\u00020\f2\u0006\u0010\u001a\u001a\u00020\u001bH\u0082@\u00a2\u0006\u0002\u0010\u001cJ\u0018\u0010\u001d\u001a\u0004\u0018\u00010\u001b2\u0006\u0010\u0015\u001a\u00020\bH\u0082@\u00a2\u0006\u0002\u0010\u0016J\u000e\u0010\u001e\u001a\u00020\tH\u0082@\u00a2\u0006\u0002\u0010\u001fJ\u0006\u0010 \u001a\u00020\tJ\u0006\u0010!\u001a\u00020\tR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000f\u001a\u00020\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0006\u001a\u000e\u0012\u0004\u0012\u00020\b\u0012\u0004\u0012\u00020\t0\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u0013X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006#"}, d2 = {"Lcom/essential/spacelite/capture/ScreenshotObserver;", "", "context", "Landroid/content/Context;", "triggerTimeMs", "", "onDetected", "Lkotlin/Function1;", "Landroid/net/Uri;", "", "(Landroid/content/Context;JLkotlin/jvm/functions/Function1;)V", "delivered", "", "mainHandler", "Landroid/os/Handler;", "observer", "Landroid/database/ContentObserver;", "registered", "scope", "Lkotlinx/coroutines/CoroutineScope;", "canOpen", "uri", "(Landroid/net/Uri;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "checkUri", "deliver", "isValidCandidate", "info", "Lcom/essential/spacelite/capture/ScreenshotObserver$MediaInfo;", "(Lcom/essential/spacelite/capture/ScreenshotObserver$MediaInfo;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadInfo", "pollScan", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "start", "stop", "MediaInfo", "app_debug"})
public final class ScreenshotObserver {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    private final long triggerTimeMs = 0L;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.jvm.functions.Function1<android.net.Uri, kotlin.Unit> onDetected = null;
    @org.jetbrains.annotations.NotNull()
    private final android.os.Handler mainHandler = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.CoroutineScope scope = null;
    private boolean registered = false;
    private boolean delivered = false;
    @org.jetbrains.annotations.NotNull()
    private final android.database.ContentObserver observer = null;
    
    public ScreenshotObserver(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long triggerTimeMs, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super android.net.Uri, kotlin.Unit> onDetected) {
        super();
    }
    
    public final void start() {
    }
    
    public final void stop() {
    }
    
    private final java.lang.Object checkUri(android.net.Uri uri, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object pollScan(kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    private final java.lang.Object loadInfo(android.net.Uri uri, kotlin.coroutines.Continuation<? super com.essential.spacelite.capture.ScreenshotObserver.MediaInfo> $completion) {
        return null;
    }
    
    private final java.lang.Object isValidCandidate(com.essential.spacelite.capture.ScreenshotObserver.MediaInfo info, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.Object canOpen(android.net.Uri uri, kotlin.coroutines.Continuation<? super java.lang.Boolean> $completion) {
        return null;
    }
    
    private final java.lang.Object deliver(android.net.Uri uri, kotlin.coroutines.Continuation<? super kotlin.Unit> $completion) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0014\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\b\u0082\b\u0018\u00002\u00020\u0001B5\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0005\u0012\u0006\u0010\u0007\u001a\u00020\b\u0012\u0006\u0010\t\u001a\u00020\b\u0012\u0006\u0010\n\u001a\u00020\b\u00a2\u0006\u0002\u0010\u000bJ\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0005H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\bH\u00c6\u0003J\t\u0010\u0019\u001a\u00020\bH\u00c6\u0003J\t\u0010\u001a\u001a\u00020\bH\u00c6\u0003JE\u0010\u001b\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00052\b\b\u0002\u0010\u0007\u001a\u00020\b2\b\b\u0002\u0010\t\u001a\u00020\b2\b\b\u0002\u0010\n\u001a\u00020\bH\u00c6\u0001J\u0013\u0010\u001c\u001a\u00020\u001d2\b\u0010\u001e\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001J\t\u0010!\u001a\u00020\bH\u00d6\u0001R\u0011\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\rR\u0011\u0010\u0007\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u0011\u0010\n\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\u000fR\u0011\u0010\t\u001a\u00020\b\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\u000fR\u0011\u0010\u0006\u001a\u00020\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\rR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0013\u0010\u0014\u00a8\u0006\""}, d2 = {"Lcom/essential/spacelite/capture/ScreenshotObserver$MediaInfo;", "", "uri", "Landroid/net/Uri;", "dateModifiedSec", "", "size", "displayName", "", "relativePath", "mimeType", "(Landroid/net/Uri;JJLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V", "getDateModifiedSec", "()J", "getDisplayName", "()Ljava/lang/String;", "getMimeType", "getRelativePath", "getSize", "getUri", "()Landroid/net/Uri;", "component1", "component2", "component3", "component4", "component5", "component6", "copy", "equals", "", "other", "hashCode", "", "toString", "app_debug"})
    static final class MediaInfo {
        @org.jetbrains.annotations.NotNull()
        private final android.net.Uri uri = null;
        private final long dateModifiedSec = 0L;
        private final long size = 0L;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String displayName = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String relativePath = null;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String mimeType = null;
        
        public MediaInfo(@org.jetbrains.annotations.NotNull()
        android.net.Uri uri, long dateModifiedSec, long size, @org.jetbrains.annotations.NotNull()
        java.lang.String displayName, @org.jetbrains.annotations.NotNull()
        java.lang.String relativePath, @org.jetbrains.annotations.NotNull()
        java.lang.String mimeType) {
            super();
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.net.Uri getUri() {
            return null;
        }
        
        public final long getDateModifiedSec() {
            return 0L;
        }
        
        public final long getSize() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getDisplayName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getRelativePath() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getMimeType() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final android.net.Uri component1() {
            return null;
        }
        
        public final long component2() {
            return 0L;
        }
        
        public final long component3() {
            return 0L;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component4() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component5() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String component6() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.essential.spacelite.capture.ScreenshotObserver.MediaInfo copy(@org.jetbrains.annotations.NotNull()
        android.net.Uri uri, long dateModifiedSec, long size, @org.jetbrains.annotations.NotNull()
        java.lang.String displayName, @org.jetbrains.annotations.NotNull()
        java.lang.String relativePath, @org.jetbrains.annotations.NotNull()
        java.lang.String mimeType) {
            return null;
        }
        
        @java.lang.Override()
        public boolean equals(@org.jetbrains.annotations.Nullable()
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override()
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override()
        @org.jetbrains.annotations.NotNull()
        public java.lang.String toString() {
            return null;
        }
    }
}