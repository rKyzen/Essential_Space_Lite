package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000@\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0016\u001a\u00020\u0017J\u0006\u0010\u0018\u001a\u00020\u0017J\u000e\u0010\u0019\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\u0012J\r\u0010\u001b\u001a\u0004\u0018\u00010\n\u00a2\u0006\u0002\u0010\u001cR\u0011\u0010\u0005\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\t\u001a\u00020\n8F\u00a2\u0006\u0006\u001a\u0004\b\u000b\u0010\fR\u001e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\r\u001a\u00020\u000e@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\u0010R\u0010\u0010\u0011\u001a\u0004\u0018\u00010\u0012X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0013\u001a\u0004\u0018\u00010\u0014X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0015\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/essential/spacelite/utils/VoiceRecorder;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "amplitude", "", "getAmplitude", "()I", "durationMs", "", "getDurationMs", "()J", "<set-?>", "", "isRecording", "()Z", "outputPath", "", "recorder", "Landroid/media/MediaRecorder;", "startTime", "cancel", "", "release", "start", "path", "stop", "()Ljava/lang/Long;", "app_release"})
public final class VoiceRecorder {
    @org.jetbrains.annotations.NotNull()
    private final android.content.Context context = null;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaRecorder recorder;
    @org.jetbrains.annotations.Nullable()
    private java.lang.String outputPath;
    private long startTime = 0L;
    private boolean isRecording = false;
    
    public VoiceRecorder(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        super();
    }
    
    public final boolean isRecording() {
        return false;
    }
    
    public final long getDurationMs() {
        return 0L;
    }
    
    public final int getAmplitude() {
        return 0;
    }
    
    public final boolean start(@org.jetbrains.annotations.NotNull()
    java.lang.String path) {
        return false;
    }
    
    /**
     * Stop and return duration ms, or null on failure.
     */
    @org.jetbrains.annotations.Nullable()
    public final java.lang.Long stop() {
        return null;
    }
    
    public final void cancel() {
    }
    
    public final void release() {
    }
}