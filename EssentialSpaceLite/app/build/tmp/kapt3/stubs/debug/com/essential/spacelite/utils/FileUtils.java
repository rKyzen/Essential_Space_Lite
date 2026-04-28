package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000J\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\n\u0002\u0010\t\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\nH\u0002J\u0010\u0010\f\u001a\u00020\r2\b\u0010\u000e\u001a\u0004\u0018\u00010\u0004J\u000e\u0010\u000f\u001a\u00020\u00042\u0006\u0010\u0010\u001a\u00020\u0011J\u0016\u0010\u0012\u001a\u00020\u00042\u0006\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u0004J\u000e\u0010\u0015\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018J\u000e\u0010\u0019\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018J\u000e\u0010\u001a\u001a\u00020\u00162\u0006\u0010\u0017\u001a\u00020\u0018J\u000e\u0010\u001b\u001a\u00020\u00042\u0006\u0010\u0017\u001a\u00020\u0018J\"\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0004\u0012\u0004\u0012\u00020\u00040\u001d2\u0006\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u001e\u001a\u00020\nR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0007X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001f"}, d2 = {"Lcom/essential/spacelite/utils/FileUtils;", "", "()V", "SCREENSHOT_DIR", "", "THUMBNAIL_DIR", "THUMBNAIL_MAX_SIZE", "", "VOICE_DIR", "createThumbnail", "Landroid/graphics/Bitmap;", "source", "deleteFile", "", "path", "formatDuration", "ms", "", "generateFileName", "prefix", "ext", "getScreenshotDir", "Ljava/io/File;", "context", "Landroid/content/Context;", "getThumbnailDir", "getVoiceDir", "newVoiceNotePath", "saveCaptureFiles", "Lkotlin/Pair;", "bitmap", "app_debug"})
public final class FileUtils {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String SCREENSHOT_DIR = "screenshots";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String THUMBNAIL_DIR = "thumbnails";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String VOICE_DIR = "voice_notes";
    private static final int THUMBNAIL_MAX_SIZE = 300;
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.utils.FileUtils INSTANCE = null;
    
    private FileUtils() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.File getScreenshotDir(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.File getThumbnailDir(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.io.File getVoiceDir(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String generateFileName(@org.jetbrains.annotations.NotNull()
    java.lang.String prefix, @org.jetbrains.annotations.NotNull()
    java.lang.String ext) {
        return null;
    }
    
    /**
     * Saves full screenshot and compressed thumbnail.
     * Returns pair of (screenshotPath, thumbnailPath)
     */
    @org.jetbrains.annotations.NotNull()
    public final kotlin.Pair<java.lang.String, java.lang.String> saveCaptureFiles(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    android.graphics.Bitmap bitmap) {
        return null;
    }
    
    private final android.graphics.Bitmap createThumbnail(android.graphics.Bitmap source) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String newVoiceNotePath(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void deleteFile(@org.jetbrains.annotations.Nullable()
    java.lang.String path) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String formatDuration(long ms) {
        return null;
    }
}