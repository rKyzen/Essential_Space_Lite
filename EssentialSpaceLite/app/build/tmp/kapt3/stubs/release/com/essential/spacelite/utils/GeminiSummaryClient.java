package com.essential.spacelite.utils;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000V\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010 \n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\t\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u0004\b\u00c6\u0002\u0018\u00002\u00020\u0001:\u0001+B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u0018\u0010\u0007\u001a\u00020\b2\u0006\u0010\t\u001a\u00020\u00042\u0006\u0010\n\u001a\u00020\u0004H\u0002J!\u0010\u000b\u001a\u00020\u00042\b\u0010\f\u001a\u0004\u0018\u00010\u00042\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u0002\u00a2\u0006\u0002\u0010\u000fJ5\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u0006\u0010\u0012\u001a\u00020\u00042\b\u0010\f\u001a\u0004\u0018\u00010\u00042\b\u0010\r\u001a\u0004\u0018\u00010\u000e\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b\u0013\u0010\u0014J\u0018\u0010\u0015\u001a\u00020\u00042\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u0018\u001a\u00020\u0004H\u0002J\u0006\u0010\u0019\u001a\u00020\u001aJ\u0018\u0010\u001b\u001a\u00020\u001c2\u0006\u0010\u0016\u001a\u00020\u00172\u0006\u0010\u001d\u001a\u00020\u0004H\u0002J\u0010\u0010\u001e\u001a\u00020\u00042\u0006\u0010\u001d\u001a\u00020\u0004H\u0002J\u0012\u0010\u001f\u001a\u0004\u0018\u00010\u00042\u0006\u0010 \u001a\u00020!H\u0002J+\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00040\u00112\u0006\u0010#\u001a\u00020\u00042\u0006\u0010$\u001a\u00020\bH\u0002\u00f8\u0001\u0000\u00f8\u0001\u0001\u00a2\u0006\u0004\b%\u0010&J\u0018\u0010\'\u001a\u00020(2\u0006\u0010)\u001a\u00020(2\u0006\u0010*\u001a\u00020\u0017H\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00040\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u000b\n\u0002\b!\n\u0005\b\u00a1\u001e0\u0001\u00a8\u0006,"}, d2 = {"Lcom/essential/spacelite/utils/GeminiSummaryClient;", "", "()V", "API_BASE", "", "fallbackModels", "", "buildPayload", "Lorg/json/JSONObject;", "prompt", "imageData", "buildPrompt", "note", "reminderAt", "", "(Ljava/lang/String;Ljava/lang/Long;)Ljava/lang/String;", "generateSummary", "Lkotlin/Result;", "screenshotPath", "generateSummary-0E7RQCE", "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/Long;)Ljava/lang/Object;", "humanizeApiMessage", "statusCode", "", "raw", "isConfigured", "", "parseApiError", "Lcom/essential/spacelite/utils/GeminiSummaryClient$GeminiRequestException;", "response", "parseSummary", "prepareImageData", "file", "Ljava/io/File;", "requestSummary", "model", "payload", "requestSummary-gIAlu-s", "(Ljava/lang/String;Lorg/json/JSONObject;)Ljava/lang/Object;", "scaleBitmap", "Landroid/graphics/Bitmap;", "source", "maxSide", "GeminiRequestException", "app_release"})
public final class GeminiSummaryClient {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String API_BASE = "https://generativelanguage.googleapis.com/v1beta/models/";
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<java.lang.String> fallbackModels = null;
    @org.jetbrains.annotations.NotNull()
    public static final com.essential.spacelite.utils.GeminiSummaryClient INSTANCE = null;
    
    private GeminiSummaryClient() {
        super();
    }
    
    public final boolean isConfigured() {
        return false;
    }
    
    private final org.json.JSONObject buildPayload(java.lang.String prompt, java.lang.String imageData) {
        return null;
    }
    
    private final java.lang.String buildPrompt(java.lang.String note, java.lang.Long reminderAt) {
        return null;
    }
    
    private final java.lang.String prepareImageData(java.io.File file) {
        return null;
    }
    
    private final android.graphics.Bitmap scaleBitmap(android.graphics.Bitmap source, int maxSide) {
        return null;
    }
    
    private final java.lang.String parseSummary(java.lang.String response) {
        return null;
    }
    
    private final com.essential.spacelite.utils.GeminiSummaryClient.GeminiRequestException parseApiError(int statusCode, java.lang.String response) {
        return null;
    }
    
    private final java.lang.String humanizeApiMessage(int statusCode, java.lang.String raw) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001c\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0006\b\u0002\u0018\u00002\u00060\u0001j\u0002`\u0002B\u0015\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\u0006\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007R\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\f"}, d2 = {"Lcom/essential/spacelite/utils/GeminiSummaryClient$GeminiRequestException;", "Ljava/lang/IllegalStateException;", "Lkotlin/IllegalStateException;", "statusCode", "", "uiMessage", "", "(ILjava/lang/String;)V", "getStatusCode", "()I", "getUiMessage", "()Ljava/lang/String;", "app_release"})
    static final class GeminiRequestException extends java.lang.IllegalStateException {
        private final int statusCode = 0;
        @org.jetbrains.annotations.NotNull()
        private final java.lang.String uiMessage = null;
        
        public GeminiRequestException(int statusCode, @org.jetbrains.annotations.NotNull()
        java.lang.String uiMessage) {
            super();
        }
        
        public final int getStatusCode() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull()
        public final java.lang.String getUiMessage() {
            return null;
        }
    }
}