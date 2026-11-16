# Default ProGuard rules. R8 is enabled for release builds.
-keepattributes SourceFile,LineNumberTable

# DataStore preferences are accessed through generated serializers.
-keep class androidx.datastore.preferences.core.** { *; }
