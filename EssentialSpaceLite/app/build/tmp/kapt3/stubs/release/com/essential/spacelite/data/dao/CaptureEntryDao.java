package com.essential.spacelite.data.dao;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00004\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010 \n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\bg\u0018\u00002\u00020\u0001J\u0016\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u000e\u0010\u0007\u001a\u00020\u0003H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0016\u0010\t\u001a\u00020\u00032\u0006\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0014\u0010\r\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00050\u000f0\u000eH\'J\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00050\u000fH\u00a7@\u00a2\u0006\u0002\u0010\bJ\u000e\u0010\u0011\u001a\u00020\u0012H\u00a7@\u00a2\u0006\u0002\u0010\bJ\u0018\u0010\u0013\u001a\u0004\u0018\u00010\u00052\u0006\u0010\n\u001a\u00020\u000bH\u00a7@\u00a2\u0006\u0002\u0010\fJ\u0016\u0010\u0014\u001a\u00020\u000b2\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006J\u0016\u0010\u0015\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u0005H\u00a7@\u00a2\u0006\u0002\u0010\u0006\u00a8\u0006\u0016"}, d2 = {"Lcom/essential/spacelite/data/dao/CaptureEntryDao;", "", "delete", "", "entry", "Lcom/essential/spacelite/data/entity/CaptureEntry;", "(Lcom/essential/spacelite/data/entity/CaptureEntry;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteAll", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "deleteById", "id", "", "(JLkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getAllEntries", "Lkotlinx/coroutines/flow/Flow;", "", "getAllEntriesSnapshot", "getCount", "", "getEntryById", "insert", "update", "app_release"})
@androidx.room.Dao()
public abstract interface CaptureEntryDao {
    
    @androidx.room.Query(value = "SELECT * FROM capture_entries ORDER BY timestamp DESC")
    @org.jetbrains.annotations.NotNull()
    public abstract kotlinx.coroutines.flow.Flow<java.util.List<com.essential.spacelite.data.entity.CaptureEntry>> getAllEntries();
    
    @androidx.room.Query(value = "SELECT * FROM capture_entries ORDER BY timestamp DESC")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getAllEntriesSnapshot(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.util.List<com.essential.spacelite.data.entity.CaptureEntry>> $completion);
    
    @androidx.room.Query(value = "SELECT * FROM capture_entries WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getEntryById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super com.essential.spacelite.data.entity.CaptureEntry> $completion);
    
    @androidx.room.Insert(onConflict = 1)
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object insert(@org.jetbrains.annotations.NotNull()
    com.essential.spacelite.data.entity.CaptureEntry entry, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Long> $completion);
    
    @androidx.room.Update()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object update(@org.jetbrains.annotations.NotNull()
    com.essential.spacelite.data.entity.CaptureEntry entry, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Delete()
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object delete(@org.jetbrains.annotations.NotNull()
    com.essential.spacelite.data.entity.CaptureEntry entry, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "DELETE FROM capture_entries WHERE id = :id")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteById(long id, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
    
    @androidx.room.Query(value = "SELECT COUNT(*) FROM capture_entries")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object getCount(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super java.lang.Integer> $completion);
    
    @androidx.room.Query(value = "DELETE FROM capture_entries")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object deleteAll(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}