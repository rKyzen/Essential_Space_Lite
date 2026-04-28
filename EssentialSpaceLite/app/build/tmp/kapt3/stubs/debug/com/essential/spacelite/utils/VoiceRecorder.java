package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00008\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\t\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0006\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u0006\u0010\u0012\u001a\u00020\u0013J\u0006\u0010\u0014\u001a\u00020\u0013J\u000e\u0010\u0015\u001a\u00020\n2\u0006\u0010\u0016\u001a\u00020\u000eJ\r\u0010\u0017\u001a\u0004\u0018\u00010\u0006\u00a2\u0006\u0002\u0010\u0018R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0005\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u001e\u0010\u000b\u001a\u00020\n2\u0006\u0010\t\u001a\u00020\n@BX\u0086\u000e\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u000f\u001a\u0004\u0018\u00010\u0010X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0011\u001a\u00020\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0019"}, d2 = {"Lcom/essential/spacelite/utils/VoiceRecorder;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "durationMs", "", "getDurationMs", "()J", "<set-?>", "", "isRecording", "()Z", "outputPath", "", "recorder", "Landroid/media/MediaRecorder;", "startTime", "cancel", "", "release", "start", "path", "stop", "()Ljava/lang/Long;", "app_debug"})
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