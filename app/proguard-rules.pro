# Release builds currently ship unminified (see app/build.gradle.kts), so these
# rules are not applied yet. They are kept ready for when R8 is switched on.

# Glance resolves ActionCallback implementations by class name at runtime.
-keep class * implements androidx.glance.appwidget.action.ActionCallback { *; }

# Room's generated implementations are looked up reflectively by name.
-keep class * extends androidx.room.RoomDatabase { *; }

# Keep line numbers so crash reports from the phone stay readable.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
