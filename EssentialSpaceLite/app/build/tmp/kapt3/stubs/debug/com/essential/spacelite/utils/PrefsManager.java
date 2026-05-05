package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00006\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\r\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001#B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u000e\u0010\u000b\u001a\u00020\f2\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u000f\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0011\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0012\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u000e\u0010\u0013\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eJ\u0010\u0010\u0014\u001a\u00020\u00152\u0006\u0010\r\u001a\u00020\u000eH\u0002J\u0016\u0010\u0016\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u0010J\u0016\u0010\u0019\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001a\u001a\u00020\u0010J\u0016\u0010\u001b\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001c\u001a\u00020\u0010J\u0016\u0010\u001d\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u001e\u001a\u00020\u0010J\u0016\u0010\u001f\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010 \u001a\u00020\fJ\u0016\u0010!\u001a\u00020\u00172\u0006\u0010\r\u001a\u00020\u000e2\u0006\u0010\u0018\u001a\u00020\u0010J\u000e\u0010\"\u001a\u00020\u00102\u0006\u0010\r\u001a\u00020\u000eR\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006$"}, d2 = {"Lcom/essential/spacelite/utils/PrefsManager;", "", "()V", "KEY_ACCESSIBILITY_ENABLED", "", "KEY_ACCESSIBILITY_RUNNING", "KEY_DISCLOSURE_ACCEPTED", "KEY_ONBOARDING_DONE", "KEY_THEME_OPTION", "KEY_USE_NDOT", "PREFS_NAME", "getThemeOption", "Lcom/essential/spacelite/utils/PrefsManager$ThemeOption;", "context", "Landroid/content/Context;", "isAccessibilityEnabled", "", "isAccessibilityServiceRunning", "isDisclosureAccepted", "isOnboardingDone", "prefs", "Landroid/content/SharedPreferences;", "setAccessibilityEnabled", "", "enabled", "setAccessibilityServiceRunning", "running", "setDisclosureAccepted", "accepted", "setOnboardingDone", "done", "setThemeOption", "option", "setUseNdotHeadings", "useNdotHeadings", "ThemeOption", "app_debug"})
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
    private static final java.lang.String KEY_DISCLOSURE_ACCEPTED = "disclosure_accepted";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_THEME_OPTION = "theme_option";
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String KEY_USE_NDOT = "use_ndot_headings";
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
    
    public final boolean isDisclosureAccepted(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setDisclosureAccepted(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean accepted) {
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
    
    public final boolean useNdotHeadings(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
        return false;
    }
    
    public final void setUseNdotHeadings(@org.jetbrains.annotations.NotNull()
    android.content.Context context, boolean enabled) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0012\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u0000 \b2\b\u0012\u0004\u0012\u00020\u00000\u0001:\u0001\bB\u000f\b\u0002\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0005\u0010\u0006j\u0002\b\u0007\u00a8\u0006\t"}, d2 = {"Lcom/essential/spacelite/utils/PrefsManager$ThemeOption;", "", "storageValue", "", "(Ljava/lang/String;ILjava/lang/String;)V", "getStorageValue", "()Ljava/lang/String;", "NOTHING_DARK", "Companion", "app_debug"})
    public static enum ThemeOption {
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
        
        @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0010\u0010\u0003\u001a\u00020\u00042\b\u0010\u0005\u001a\u0004\u0018\u00010\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/essential/spacelite/utils/PrefsManager$ThemeOption$Companion;", "", "()V", "fromStorage", "Lcom/essential/spacelite/utils/PrefsManager$ThemeOption;", "value", "", "app_debug"})
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