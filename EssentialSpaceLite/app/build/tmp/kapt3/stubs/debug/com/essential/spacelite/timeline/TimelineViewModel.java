package com.essential.spacelite.timeline;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000R\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\t\n\u0002\b\b\u0018\u00002\u00020\u0001:\u0001$B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0017\u001a\u00020\u00182\u0006\u0010\u0019\u001a\u00020\bJ\u0010\u0010\u001a\u001a\u00020\u001b2\u0006\u0010\u001c\u001a\u00020\u001dH\u0002J\u0018\u0010\u001e\u001a\u00020\u001b2\u0006\u0010\u0019\u001a\u00020\b2\u0006\u0010\u001f\u001a\u00020\nH\u0002J\u0018\u0010 \u001a\u00020\u001b2\u0006\u0010\u0019\u001a\u00020\b2\u0006\u0010\u0011\u001a\u00020\u0012H\u0002J\u000e\u0010!\u001a\u00020\u00182\u0006\u0010\u001f\u001a\u00020\nJ\u000e\u0010\"\u001a\u00020\u00182\u0006\u0010#\u001a\u00020\u0012R\u001a\u0010\u0005\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\t\u001a\b\u0012\u0004\u0012\u00020\n0\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000b\u0010\fR\u001d\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\b0\u00070\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\fR\u0014\u0010\u000f\u001a\b\u0012\u0004\u0012\u00020\n0\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00120\u0010X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0013\u001a\u00020\u0014X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00120\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0016\u0010\f\u00a8\u0006%"}, d2 = {"Lcom/essential/spacelite/timeline/TimelineViewModel;", "Landroidx/lifecycle/AndroidViewModel;", "application", "Landroid/app/Application;", "(Landroid/app/Application;)V", "allEntries", "Lkotlinx/coroutines/flow/StateFlow;", "", "Lcom/essential/spacelite/data/entity/CaptureEntry;", "currentFilter", "Lcom/essential/spacelite/timeline/TimelineViewModel$FilterMode;", "getCurrentFilter", "()Lkotlinx/coroutines/flow/StateFlow;", "entries", "getEntries", "filterMode", "Lkotlinx/coroutines/flow/MutableStateFlow;", "query", "", "repo", "Lcom/essential/spacelite/data/CaptureRepository;", "subtitle", "getSubtitle", "deleteEntry", "", "entry", "isToday", "", "timestamp", "", "matchesFilter", "mode", "matchesQuery", "setFilter", "setQuery", "value", "FilterMode", "app_debug"})
public final class TimelineViewModel extends androidx.lifecycle.AndroidViewModel {
    @org.jetbrains.annotations.NotNull()
    private final com.essential.spacelite.data.CaptureRepository repo = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> query = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.MutableStateFlow<com.essential.spacelite.timeline.TimelineViewModel.FilterMode> filterMode = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.essential.spacelite.data.entity.CaptureEntry>> allEntries = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<com.essential.spacelite.data.entity.CaptureEntry>> entries = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<com.essential.spacelite.timeline.TimelineViewModel.FilterMode> currentFilter = null;
    @org.jetbrains.annotations.NotNull()
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> subtitle = null;
    
    public TimelineViewModel(@org.jetbrains.annotations.NotNull()
    android.app.Application application) {
        super(null);
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<com.essential.spacelite.data.entity.CaptureEntry>> getEntries() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<com.essential.spacelite.timeline.TimelineViewModel.FilterMode> getCurrentFilter() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getSubtitle() {
        return null;
    }
    
    public final void setQuery(@org.jetbrains.annotations.NotNull()
    java.lang.String value) {
    }
    
    public final void setFilter(@org.jetbrains.annotations.NotNull()
    com.essential.spacelite.timeline.TimelineViewModel.FilterMode mode) {
    }
    
    public final void deleteEntry(@org.jetbrains.annotations.NotNull()
    com.essential.spacelite.data.entity.CaptureEntry entry) {
    }
    
    private final boolean matchesQuery(com.essential.spacelite.data.entity.CaptureEntry entry, java.lang.String query) {
        return false;
    }
    
    private final boolean matchesFilter(com.essential.spacelite.data.entity.CaptureEntry entry, com.essential.spacelite.timeline.TimelineViewModel.FilterMode mode) {
        return false;
    }
    
    private final boolean isToday(long timestamp) {
        return false;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0006\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004j\u0002\b\u0005j\u0002\b\u0006\u00a8\u0006\u0007"}, d2 = {"Lcom/essential/spacelite/timeline/TimelineViewModel$FilterMode;", "", "(Ljava/lang/String;I)V", "ALL", "TODAY", "NOTES", "VOICE", "app_debug"})
    public static enum FilterMode {
        /*public static final*/ ALL /* = new ALL() */,
        /*public static final*/ TODAY /* = new TODAY() */,
        /*public static final*/ NOTES /* = new NOTES() */,
        /*public static final*/ VOICE /* = new VOICE() */;
        
        FilterMode() {
        }
        
        @org.jetbrains.annotations.NotNull()
        public static kotlin.enums.EnumEntries<com.essential.spacelite.timeline.TimelineViewModel.FilterMode> getEntries() {
            return null;
        }
    }
}