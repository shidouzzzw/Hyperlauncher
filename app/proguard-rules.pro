# Keep line numbers for release crash reports.
-keepattributes SourceFile,LineNumberTable,InnerClasses,EnclosingMethod

# Entry points referenced from AndroidManifest.xml must survive obfuscation.
-keep public class com.gafure.hyperlauncher.HyperLauncherApp {
    public <init>();
    *;
}
-keep public class com.gafure.hyperlauncher.MainActivity {
    public <init>();
    *;
}

# ViewModels are instantiated reflectively by the lifecycle library.
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
    *;
}

# DataStore preferences are accessed through generated serializers.
-keep class androidx.datastore.preferences.core.** { *; }

# Startup initializers are discovered by manifest meta-data class names.
-keep class androidx.startup.** { *; }
-keep class * extends androidx.startup.Initializer { *; }

# Baseline-profile installer runs on first launch in release builds.
-keep class androidx.profileinstaller.** { *; }

# Immutable collections are built via static factories at runtime.
-keep class kotlinx.collections.immutable.** { *; }
