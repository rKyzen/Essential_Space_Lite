package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000T\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\u0002\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001%B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J$\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\b\u0010\u0007\u001a\u0004\u0018\u00010\b2\n\b\u0002\u0010\t\u001a\u0004\u0018\u00010\bJ\u0016\u0010\n\u001a\u00020\u00042\u0006\u0010\u000b\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rJ\u000e\u0010\u000e\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u0010J\u000e\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0005\u001a\u00020\u0006J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000f\u001a\u00020\u00102\b\b\u0002\u0010\u0005\u001a\u00020\u0006J\u000e\u0010\u0013\u001a\u00020\u00042\u0006\u0010\u0014\u001a\u00020\u0015J\u0016\u0010\u0016\u001a\u00020\u00042\u0006\u0010\u000f\u001a\u00020\u00102\u0006\u0010\u0005\u001a\u00020\u0006J \u0010\u0017\u001a\u00020\u00042\u0006\u0010\u0018\u001a\u00020\u00192\u0006\u0010\f\u001a\u00020\r2\b\b\u0002\u0010\u001a\u001a\u00020\u001bJ\u001e\u0010\u001c\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\b2\u0006\u0010\f\u001a\u00020\r2\u0006\u0010\u001e\u001a\u00020\u001bJ\u0016\u0010\u001f\u001a\u00020\u00042\u0006\u0010 \u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rJ\u0016\u0010!\u001a\u00020\u00042\u0006\u0010 \u001a\u00020\b2\u0006\u0010\f\u001a\u00020\rJ\u0016\u0010\"\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\b2\u0006\u0010#\u001a\u00020$\u00a8\u0006&"}, d2 = {"Lcom/essential/spacelite/utils/ThemeHelper;", "", "()V", "applyAmbientMode", "", "option", "Lcom/essential/spacelite/utils/PrefsManager$ThemeOption;", "topBlob", "Landroid/view/View;", "bottomBlob", "applyRootBackground", "root", "palette", "Lcom/essential/spacelite/utils/ThemeHelper$ThemePalette;", "applySavedNightMode", "context", "Landroid/content/Context;", "displayLabel", "", "prepareActivity", "activity", "Landroid/app/Activity;", "setThemeOption", "styleCard", "card", "Lcom/google/android/material/card/MaterialCardView;", "strong", "", "styleNavActive", "view", "active", "styleOutlineButton", "button", "stylePrimaryButton", "tintSurface", "color", "", "ThemePalette", "app_release"})
public final class ThemeHelper {
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.utils.ThemeHelper INSTANCE = null;
    
    private ThemeHelper() {
        super();
    }
    
    public final void applySavedNightMode(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    public final void prepareActivity(@org.jetbrains.annotations.NotNull()
    android.app.Activity activity) {
    }
    
    public final void setThemeOption(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.PrefsManager.ThemeOption option) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public final java.lang.String displayLabel(@org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.PrefsManager.ThemeOption option) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.essential.spacelite.utils.ThemeHelper.ThemePalette palette(@org.jetbrains.annotations.NotNull()
    android.content.Context context, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.PrefsManager.ThemeOption option) {
        return null;
    }
    
    public final void applyRootBackground(@org.jetbrains.annotations.NotNull()
    android.view.View root, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.ThemeHelper.ThemePalette palette) {
    }
    
    public final void tintSurface(@org.jetbrains.annotations.NotNull()
    android.view.View view, int color) {
    }
    
    public final void styleCard(@org.jetbrains.annotations.NotNull()
    com.google.android.material.card.MaterialCardView card, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.ThemeHelper.ThemePalette palette, boolean strong) {
    }
    
    public final void styleNavActive(@org.jetbrains.annotations.NotNull()
    android.view.View view, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.ThemeHelper.ThemePalette palette, boolean active) {
    }
    
    public final void stylePrimaryButton(@org.jetbrains.annotations.NotNull()
    android.view.View button, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.ThemeHelper.ThemePalette palette) {
    }
    
    public final void styleOutlineButton(@org.jetbrains.annotations.NotNull()
    android.view.View button, @org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.ThemeHelper.ThemePalette palette) {
    }
    
    public final void applyAmbientMode(@org.jetbrains.annotations.NotNull()
    com.essential.spacelite.utils.PrefsManager.ThemeOption option, @org.jetbrains.annotations.Nullable()
    android.view.View topBlob, @org.jetbrains.annotations.Nullable()
    android.view.View bottomBlob) {
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0002\b\u0018\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u00002\u00020\u0001B=\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0003\u0012\u0006\u0010\u0005\u001a\u00020\u0003\u0012\u0006\u0010\u0006\u001a\u00020\u0003\u0012\u0006\u0010\u0007\u001a\u00020\u0003\u0012\u0006\u0010\b\u001a\u00020\u0003\u0012\u0006\u0010\t\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\nJ\t\u0010\u0013\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0014\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0015\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0016\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0017\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0018\u001a\u00020\u0003H\u00c6\u0003J\t\u0010\u0019\u001a\u00020\u0003H\u00c6\u0003JO\u0010\u001a\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\b\b\u0002\u0010\u0004\u001a\u00020\u00032\b\b\u0002\u0010\u0005\u001a\u00020\u00032\b\b\u0002\u0010\u0006\u001a\u00020\u00032\b\b\u0002\u0010\u0007\u001a\u00020\u00032\b\b\u0002\u0010\b\u001a\u00020\u00032\b\b\u0002\u0010\t\u001a\u00020\u0003H\u00c6\u0001J\u0013\u0010\u001b\u001a\u00020\u001c2\b\u0010\u001d\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u001e\u001a\u00020\u0003H\u00d6\u0001J\t\u0010\u001f\u001a\u00020 H\u00d6\u0001R\u0011\u0010\b\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\r\u0010\fR\u0011\u0010\u0007\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\fR\u0011\u0010\u0006\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000f\u0010\fR\u0011\u0010\t\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\fR\u0011\u0010\u0005\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0011\u0010\fR\u0011\u0010\u0004\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0012\u0010\f\u00a8\u0006!"}, d2 = {"Lcom/essential/spacelite/utils/ThemeHelper$ThemePalette;", "", "backgroundColor", "", "surfaceTint", "surfaceStrongTint", "navShellTint", "navActiveTint", "accentTint", "primaryText", "(IIIIIII)V", "getAccentTint", "()I", "getBackgroundColor", "getNavActiveTint", "getNavShellTint", "getPrimaryText", "getSurfaceStrongTint", "getSurfaceTint", "component1", "component2", "component3", "component4", "component5", "component6", "component7", "copy", "equals", "", "other", "hashCode", "toString", "", "app_release"})
    public static final class ThemePalette {
        private final int backgroundColor = 0;
        private final int surfaceTint = 0;
        private final int surfaceStrongTint = 0;
        private final int navShellTint = 0;
        private final int navActiveTint = 0;
        private final int accentTint = 0;
        private final int primaryText = 0;
        
        public ThemePalette(int backgroundColor, int surfaceTint, int surfaceStrongTint, int navShellTint, int navActiveTint, int accentTint, int primaryText) {
            super();
        }
        
        public final int getBackgroundColor() {
            return 0;
        }
        
        public final int getSurfaceTint() {
            return 0;
        }
        
        public final int getSurfaceStrongTint() {
            return 0;
        }
        
        public final int getNavShellTint() {
            return 0;
        }
        
        public final int getNavActiveTint() {
            return 0;
        }
        
        public final int getAccentTint() {
            return 0;
        }
        
        public final int getPrimaryText() {
            return 0;
        }
        
        public final int component1() {
            return 0;
        }
        
        public final int component2() {
            return 0;
        }
        
        public final int component3() {
            return 0;
        }
        
        public final int component4() {
            return 0;
        }
        
        public final int component5() {
            return 0;
        }
        
        public final int component6() {
            return 0;
        }
        
        public final int component7() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final com.essential.spacelite.utils.ThemeHelper.ThemePalette copy(int backgroundColor, int surfaceTint, int surfaceStrongTint, int navShellTint, int navActiveTint, int accentTint, int primaryText) {
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