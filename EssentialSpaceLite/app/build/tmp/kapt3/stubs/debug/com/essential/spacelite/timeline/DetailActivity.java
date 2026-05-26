package com.essential.spacelite.timeline;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000L\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\f\n\u0002\u0018\u0002\n\u0002\b\b\n\u0002\u0010\t\n\u0002\b\b\u0018\u0000 32\u00020\u0001:\u00013B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0015\u001a\u00020\u0016H\u0002J\b\u0010\u0017\u001a\u00020\u0016H\u0002J\b\u0010\u0018\u001a\u00020\u0016H\u0002J\b\u0010\u0019\u001a\u00020\u0016H\u0002J\b\u0010\u001a\u001a\u00020\u0016H\u0002J\u0010\u0010\u001b\u001a\u00020\u00162\u0006\u0010\u001c\u001a\u00020\bH\u0002J\b\u0010\u001d\u001a\u00020\u0016H\u0002J\b\u0010\u001e\u001a\u00020\u0016H\u0002J\b\u0010\u001f\u001a\u00020\u0016H\u0002J\b\u0010 \u001a\u00020\u0016H\u0002J\u0012\u0010!\u001a\u00020\u00162\b\u0010\"\u001a\u0004\u0018\u00010#H\u0014J\b\u0010$\u001a\u00020\u0016H\u0014J\b\u0010%\u001a\u00020\u0016H\u0014J\b\u0010&\u001a\u00020\u0016H\u0002J\u0010\u0010\'\u001a\u00020\u00162\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0010\u0010(\u001a\u00020\u00162\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0010\u0010)\u001a\u00020\u00162\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0010\u0010*\u001a\u00020\u00162\u0006\u0010+\u001a\u00020,H\u0002J\b\u0010-\u001a\u00020\u0016H\u0002J\b\u0010.\u001a\u00020\u0016H\u0002J\b\u0010/\u001a\u00020\u0016H\u0002J\b\u00100\u001a\u00020\u0016H\u0002J\b\u00101\u001a\u00020\u0016H\u0002J\b\u00102\u001a\u00020\u0016H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\r\u001a\u0004\u0018\u00010\u000eX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u000f\u001a\u00020\u00108BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0013\u0010\u0014\u001a\u0004\b\u0011\u0010\u0012\u00a8\u00064"}, d2 = {"Lcom/essential/spacelite/timeline/DetailActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/essential/spacelite/databinding/ActivityDetailBinding;", "dateTimeFormat", "Ljava/text/SimpleDateFormat;", "entry", "Lcom/essential/spacelite/data/entity/CaptureEntry;", "isGeneratingSummary", "", "isPlaying", "isScreenshotExpanded", "mediaPlayer", "Landroid/media/MediaPlayer;", "screenshotDetector", "Landroid/view/GestureDetector;", "getScreenshotDetector", "()Landroid/view/GestureDetector;", "screenshotDetector$delegate", "Lkotlin/Lazy;", "addReminderToCalendar", "", "addReminderToClock", "applyGlassSystem", "applyScreenshotHeight", "applyThemeVisuals", "bindEntry", "e", "clearReminder", "copyAiSummary", "copyNote", "generateAiSummary", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "pickReminderDateTime", "renderAiSummary", "renderFavoriteState", "renderReminder", "saveReminder", "reminderAt", "", "saveTextNote", "setupActions", "shareCapture", "toggleFavorite", "togglePlayback", "toggleScreenshotExpansion", "Companion", "app_debug"})
public final class DetailActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.essential.spacelite.databinding.ActivityDetailBinding binding;
    @org.jetbrains.annotations.Nullable()
    private com.essential.spacelite.data.entity.CaptureEntry entry;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isGeneratingSummary = false;
    private boolean isScreenshotExpanded = false;
    @org.jetbrains.annotations.NotNull()
    private final java.text.SimpleDateFormat dateTimeFormat = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy screenshotDetector$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_ENTRY_ID = "extra_entry_id";
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.timeline.DetailActivity.Companion Companion = null;
    
    public DetailActivity() {
        super();
    }
    
    private final android.view.GestureDetector getScreenshotDetector() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void bindEntry(com.essential.spacelite.data.entity.CaptureEntry e) {
    }
    
    private final void setupActions() {
    }
    
    private final void saveTextNote() {
    }
    
    private final void renderReminder(com.essential.spacelite.data.entity.CaptureEntry entry) {
    }
    
    private final void renderFavoriteState(com.essential.spacelite.data.entity.CaptureEntry entry) {
    }
    
    private final void renderAiSummary(com.essential.spacelite.data.entity.CaptureEntry entry) {
    }
    
    private final void pickReminderDateTime() {
    }
    
    private final void saveReminder(long reminderAt) {
    }
    
    private final void clearReminder() {
    }
    
    private final void toggleFavorite() {
    }
    
    private final void generateAiSummary() {
    }
    
    private final void copyAiSummary() {
    }
    
    private final void toggleScreenshotExpansion() {
    }
    
    private final void applyScreenshotHeight() {
    }
    
    private final void addReminderToCalendar() {
    }
    
    private final void addReminderToClock() {
    }
    
    private final void togglePlayback() {
    }
    
    private final void shareCapture() {
    }
    
    private final void copyNote() {
    }
    
    private final void applyGlassSystem() {
    }
    
    private final void applyThemeVisuals() {
    }
    
    @java.lang.Override()
    protected void onPause() {
    }
    
    @java.lang.Override()
    protected void onDestroy() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0005"}, d2 = {"Lcom/essential/spacelite/timeline/DetailActivity$Companion;", "", "()V", "EXTRA_ENTRY_ID", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}