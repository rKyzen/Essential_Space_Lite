package com.essential.spacelite.timeline;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000`\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0011\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\t\n\u0002\b\t\u0018\u00002\u00020\u0001B\u0005\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u0016\u001a\u00020\u0017H\u0002J\b\u0010\u0018\u001a\u00020\u000fH\u0002J\b\u0010\u0019\u001a\u00020\u001aH\u0002J\b\u0010\u001b\u001a\u00020\u0017H\u0002J\u0012\u0010\u001c\u001a\u00020\u00172\b\u0010\u001d\u001a\u0004\u0018\u00010\u001eH\u0014J\b\u0010\u001f\u001a\u00020\u0017H\u0014J\b\u0010 \u001a\u00020\u0017H\u0002J\b\u0010!\u001a\u00020\u0017H\u0002J\b\u0010\"\u001a\u00020\u0017H\u0002J\b\u0010#\u001a\u00020\u0017H\u0002J\b\u0010$\u001a\u00020\u0017H\u0002J\b\u0010%\u001a\u00020\u0017H\u0002J\u0010\u0010&\u001a\u00020\u00172\u0006\u0010\'\u001a\u00020(H\u0002J\b\u0010)\u001a\u00020\u0017H\u0002J\b\u0010*\u001a\u00020\u0017H\u0002J\b\u0010+\u001a\u00020\u0017H\u0002J\b\u0010,\u001a\u00020\u0017H\u0002J\b\u0010-\u001a\u00020\u0017H\u0002J\u0010\u0010.\u001a\u00020\u00172\u0006\u0010/\u001a\u00020\u000bH\u0002J\b\u00100\u001a\u00020\u0017H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R \u0010\u0007\u001a\u0014\u0012\u0010\u0012\u000e\u0012\u0004\u0012\u00020\n\u0012\u0004\u0012\u00020\u000b0\t0\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u001a\u0010\f\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000f0\u000e0\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001b\u0010\u0010\u001a\u00020\u00118BX\u0082\u0084\u0002\u00a2\u0006\f\n\u0004\b\u0014\u0010\u0015\u001a\u0004\b\u0012\u0010\u0013\u00a8\u00061"}, d2 = {"Lcom/essential/spacelite/timeline/MainActivity;", "Landroidx/appcompat/app/AppCompatActivity;", "()V", "adapter", "Lcom/essential/spacelite/timeline/TimelineAdapter;", "binding", "Lcom/essential/spacelite/databinding/ActivityMainBinding;", "filterChips", "", "Lkotlin/Pair;", "Landroid/widget/TextView;", "Lcom/essential/spacelite/timeline/TimelineViewModel$FilterMode;", "permissionsLauncher", "Landroidx/activity/result/ActivityResultLauncher;", "", "", "viewModel", "Lcom/essential/spacelite/timeline/TimelineViewModel;", "getViewModel", "()Lcom/essential/spacelite/timeline/TimelineViewModel;", "viewModel$delegate", "Lkotlin/Lazy;", "checkAndPromptAccessibility", "", "getAppVersion", "isAccessibilityServiceEnabled", "", "observeEntries", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "onResume", "openAccessibilitySettings", "requestAllPermissions", "setupRecyclerView", "setupSearchAndFilters", "setupServiceButton", "showCredits", "showDeleteDialog", "entryId", "", "showHowToUse", "showPrivacyNote", "showSettingsMenu", "showThemeDialog", "showVersionDialog", "updateChipSelection", "selectedMode", "updateEmptyState", "app_release"})
public final class MainActivity extends androidx.appcompat.app.AppCompatActivity {
    private com.essential.spacelite.databinding.ActivityMainBinding binding;
    @org.jetbrains.annotations.NotNull()
    private final kotlin.Lazy viewModel$delegate = null;
    private com.essential.spacelite.timeline.TimelineAdapter adapter;
    private java.util.List<? extends kotlin.Pair<? extends android.widget.TextView, ? extends com.essential.spacelite.timeline.TimelineViewModel.FilterMode>> filterChips;
    @org.jetbrains.annotations.NotNull()
    private final androidx.activity.result.ActivityResultLauncher<java.lang.String[]> permissionsLauncher = null;
    
    public MainActivity() {
        super();
    }
    
    private final com.essential.spacelite.timeline.TimelineViewModel getViewModel() {
        return null;
    }
    
    @java.lang.Override()
    protected void onCreate(@org.jetbrains.annotations.Nullable()
    android.os.Bundle savedInstanceState) {
    }
    
    private final void setupRecyclerView() {
    }
    
    private final void observeEntries() {
    }
    
    private final void setupSearchAndFilters() {
    }
    
    private final void updateChipSelection(com.essential.spacelite.timeline.TimelineViewModel.FilterMode selectedMode) {
    }
    
    private final void updateEmptyState() {
    }
    
    private final void setupServiceButton() {
    }
    
    private final void showSettingsMenu() {
    }
    
    private final void showThemeDialog() {
    }
    
    private final void showPrivacyNote() {
    }
    
    private final void showCredits() {
    }
    
    private final void showVersionDialog() {
    }
    
    private final java.lang.String getAppVersion() {
        return null;
    }
    
    private final void checkAndPromptAccessibility() {
    }
    
    private final boolean isAccessibilityServiceEnabled() {
        return false;
    }
    
    private final void openAccessibilitySettings() {
    }
    
    private final void showHowToUse() {
    }
    
    private final void showDeleteDialog(long entryId) {
    }
    
    private final void requestAllPermissions() {
    }
    
    @java.lang.Override()
    protected void onResume() {
    }
}