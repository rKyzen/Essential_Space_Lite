package com.essential.spacelite.timeline;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0007\n\u0002\u0010\t\n\u0002\b\u0006\u0018\u0000 &2\u00020\u0001:\u0001&B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u000e\u001a\u00020\u000fH\u0002J\b\u0010\u0010\u001a\u00020\u000fH\u0002J\u0010\u0010\u0011\u001a\u00020\u000f2\u0006\u0010\u0012\u001a\u00020\bH\u0002J\b\u0010\u0013\u001a\u00020\u000fH\u0002J\b\u0010\u0014\u001a\u00020\u000fH\u0002J\b\u0010\u0015\u001a\u00020\u000fH\u0002J\b\u0010\u0016\u001a\u00020\u000fH\u0002J\u0012\u0010\u0017\u001a\u00020\u000f2\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0014J\b\u0010\u001a\u001a\u00020\u000fH\u0014J\b\u0010\u001b\u001a\u00020\u000fH\u0014J\b\u0010\u001c\u001a\u00020\u000fH\u0002J\u0010\u0010\u001d\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0010\u0010\u001e\u001a\u00020\u000f2\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0010\u0010\u001f\u001a\u00020\u000f2\u0006\u0010 \u001a\u00020!H\u0002J\b\u0010\"\u001a\u00020\u000fH\u0002J\b\u0010#\u001a\u00020\u000fH\u0002J\b\u0010$\u001a\u00020\u000fH\u0002J\b\u0010%\u001a\u00020\u000fH\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0007\u001a\u0004\u0018\u00010\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\nX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\f\u001a\u0004\u0018\u00010\rX\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\'"}, d2 = {"Lcom/essential/spacelite/timeline/DetailActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "binding", "Lcom/essential/spacelite/databinding/ActivityDetailBinding;", "dateTimeFormat", "Ljava/text/SimpleDateFormat;", "entry", "Lcom/essential/spacelite/data/entity/CaptureEntry;", "isGeneratingSummary", "", "isPlaying", "mediaPlayer", "Landroid/media/MediaPlayer;", "addReminderToCalendar", "", "applyGlassSystem", "bindEntry", "e", "clearReminder", "copyAiSummary", "copyNote", "generateAiSummary", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onDestroy", "onPause", "pickReminderDateTime", "renderAiSummary", "renderReminder", "saveReminder", "reminderAt", "", "saveTextNote", "setupActions", "shareCapture", "togglePlayback", "Companion", "app_debug"})
public final class DetailActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.essential.spacelite.databinding.ActivityDetailBinding binding;
    @org.jetbrains.annotations.Nullable()
    private com.essential.spacelite.data.entity.CaptureEntry entry;
    @org.jetbrains.annotations.Nullable()
    private android.media.MediaPlayer mediaPlayer;
    private boolean isPlaying = false;
    private boolean isGeneratingSummary = false;
    @org.jetbrains.annotations.NotNull()
    private final java.text.SimpleDateFormat dateTimeFormat = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String EXTRA_ENTRY_ID = "extra_entry_id";
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.timeline.DetailActivity.Companion Companion = null;
    
    public DetailActivity() {
        super();
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
    
    private final void renderAiSummary(com.essential.spacelite.data.entity.CaptureEntry entry) {
    }
    
    private final void pickReminderDateTime() {
    }
    
    private final void saveReminder(long reminderAt) {
    }
    
    private final void clearReminder() {
    }
    
    private final void generateAiSummary() {
    }
    
    private final void copyAiSummary() {
    }
    
    private final void addReminderToCalendar() {
    }
    
    private final void togglePlayback() {
    }
    
    private final void shareCapture() {
    }
    
    private final void copyNote() {
    }
    
    private final void applyGlassSystem() {
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