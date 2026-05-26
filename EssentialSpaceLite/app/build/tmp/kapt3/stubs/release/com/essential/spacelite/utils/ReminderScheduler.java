package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\t\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH\u0002J\u0016\u0010\t\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bJ\u0016\u0010\u000b\u001a\u00020\n2\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\f\u001a\u00020\r\u00a8\u0006\u000e"}, d2 = {"Lcom/essential/spacelite/utils/ReminderScheduler;", "", "()V", "buildPendingIntent", "Landroid/app/PendingIntent;", "context", "Landroid/content/Context;", "entryId", "", "cancel", "", "schedule", "entry", "Lcom/essential/spacelite/data/entity/CaptureEntry;", "app_release"})
public final class ReminderScheduler {
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.utils.ReminderScheduler INSTANCE = null;
    
    private ReminderScheduler() {
        super();
    }
    
    public final void schedule(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.data.entity.CaptureEntry entry) {
    }
    
    public final void cancel(@org.jetbrains.annotations.NotNull()
    android.content.Context context, long entryId) {
    }
    
    private final android.app.PendingIntent buildPendingIntent(android.content.Context context, long entryId) {
        return null;
    }
}