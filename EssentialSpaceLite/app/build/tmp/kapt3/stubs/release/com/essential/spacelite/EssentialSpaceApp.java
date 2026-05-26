package com.essential.spacelite;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0003\u0018\u0000 \f2\u00020\u0001:\u0001\fB\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\t\u001a\u00020\nH\u0002J\b\u0010\u000b\u001a\u00020\nH\u0016R\u001b\u0010\u0003\u001a\u00020\u00048FX\u0086\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0007\u0010\b\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\r"}, d2 = {"Lcom/essential/spacelite/EssentialSpaceApp;", "Landroid/app/Application;", "()V", "database", "Lcom/essential/spacelite/data/AppDatabase;", "getDatabase", "()Lcom/essential/spacelite/data/AppDatabase;", "database$delegate", "Lkotlin/Lazy;", "createNotificationChannels", "", "onCreate", "Companion", "app_release"})
public final class EssentialSpaceApp extends android.app.Application {
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy database$delegate = null;
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_GENERAL = "essential_general";
    @org.jetbrains.annotations.NotNull()
    public static final java.lang.String CHANNEL_REMINDERS = "origin_reminders_v2";
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.EssentialSpaceApp.Companion Companion = null;
    
    public EssentialSpaceApp() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.essential.spacelite.data.AppDatabase getDatabase() {
        return null;
    }
    
    @java.lang.Override()
    public void onCreate() {
    }
    
    private final void createNotificationChannels() {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0002\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0006"}, d2 = {"Lcom/essential/spacelite/EssentialSpaceApp$Companion;", "", "()V", "CHANNEL_GENERAL", "", "CHANNEL_REMINDERS", "app_release"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}