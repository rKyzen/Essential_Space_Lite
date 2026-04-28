# Keep Glyph SDK classes
-keep class com.nothing.ketchum.** { *; }

# Keep Room entities
-keep class com.essential.spacelite.data.entity.** { *; }

# Keep data binding generated classes
-keep class com.essential.spacelite.databinding.** { *; }

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { *; }

# Kotlin coroutines
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
