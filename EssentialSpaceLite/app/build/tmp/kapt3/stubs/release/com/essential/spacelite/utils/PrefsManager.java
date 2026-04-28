package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\t\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001\u001cB\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\t\u001a\u00020\n2\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\u000f\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fJ\u000e\u0010\u0010\u001a\u00020\u000e2\u0006\u0010\u000b\u001a\u00020\fJ\u0010\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u000b\u001a\u00020\fH\u0002J\u0016\u0010\u0013\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0015\u001a\u00020\u000eJ\u0016\u0010\u0016\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0017\u001a\u00020\u000eJ\u0016\u0010\u0018\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u0019\u001a\u00020\u000eJ\u0016\u0010\u001a\u001a\u00020\u00142\u0006\u0010\u000b\u001a\u00020\f2\u0006\u0010\u001b\u001a\u00020\nR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001d"}, d2 = {"Lcom/essential/spacelite/utils/PrefsManager;", "", "()V", "KEY_ACCESSIBILITY_ENABLED", "", "KEY_ACCESSIBILITY_RUNNING", "KEY_ONBOARDING_DONE", "KEY_THEME_OPTION", "PREFS_NAME", "getThemeOption", "Lcom/essential/spacelite/utils/PrefsManager$ThemeOption;", "context", "Landroid/content/Context;", "isAccessibilityEnabled", "", "isAccessibilityServiceRunning", "isOnboardingDone", "prefs", "Landroid/content/SharedPreferences;", "setAccessibilityEnabled", "", "enabled", "setAccessibilityServiceRunning", "running", "setOnboardingDone", "done", "setThemeOption", "option", "ThemeOption", "app_release"})
public final class PrefsManager {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String PREFS_NAME = "essential_space_prefs";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ACCESSIBILITY_RUNNING = "accessibility_running";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ACCESSIBILITY_ENABLED = "accessibility_feature_enabled";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_ONBOARDING_DONE = "onboarding_done";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_THEME_OPTION = "theme_option";
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.utils.PrefsManager INSTANCE = null;
    
    private PrefsManager() {
        super();
    }
    
    private final android.content.SharedPreferences prefs(android.content.Context context) {
        return null;
    }
    
    public final boolean isAccessibilityServiceRunning(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setAccessibilityServiceRunning(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean running) {
    }
    
    public final boolean isAccessibilityEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setAccessibilityEnabled(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean enabled) {
    }
    
    public final boolean isOnboardingDone(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setOnboardingDone(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean done) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.essential.spacelite.utils.PrefsManager.ThemeOption getThemeOption(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return null;
    }
    
    public final void setThemeOption(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.PrefsManager.ThemeOption option) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\t\b\u0086\u0081\u0002\u0018\u0000 \u000b2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\u000bB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007j\u0002\b\bj\u0002\b\tj\u0002\b\n\u00a8\u0006\f"}, d2 = {"Lcom/essential/spacelite/utils/PrefsManager$ThemeOption;", "", "storageValue", "", "(Ljava/lang/String;ILjava/lang/String;)V", "getStorageValue", "()Ljava/lang/String;", "MATERIAL_YOU_LIGHT", "MATERIAL_YOU_DARK", "NOTHING_LIGHT", "NOTHING_DARK", "Companion", "app_release"})
    public static enum ThemeOption {
        /*public static final*/ MATERIAL_YOU_LIGHT /* = new MATERIAL_YOU_LIGHT(null) */,
        /*public static final*/ MATERIAL_YOU_DARK /* = new MATERIAL_YOU_DARK(null) */,
        /*public static final*/ NOTHING_LIGHT /* = new NOTHING_LIGHT(null) */,
        /*public static final*/ NOTHING_DARK /* = new NOTHING_DARK(null) */;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String storageValue = null;
        @org.jetbrains.annotations.NotNull()
        public static final com.essential.spacelite.utils.PrefsManager.ThemeOption.Companion Companion = null;
        
        ThemeOption(java.lang.String storageValue) {
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getStorageValue() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.essential.spacelite.utils.PrefsManager.ThemeOption> getEntries() {
            return null;
        }
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/essential/spacelite/utils/PrefsManager$ThemeOption$Companion;", "", "()V", "fromStorage", "Lcom/essential/spacelite/utils/PrefsManager$ThemeOption;", "value", "", "app_release"})
        public static final class Companion {
            
            private Companion() {
                super();
            }
            
            @org.jetbrains.annotations.NotNull()
            public final com.essential.spacelite.utils.PrefsManager.ThemeOption fromStorage(@org.jetbrains.annotations.Nullable()
            java.lang.String value) {
                return null;
            }
        }
    }
}