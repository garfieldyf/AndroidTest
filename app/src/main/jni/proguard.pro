# This is a configuration file for ProGuard.
# http://proguard.sourceforge.net/index.html#manual/usage.html
# AUTO-GENERATED FILE. DO NOT MODIFY.

# Keeps the R.styleable class fields.
-keepclasseswithmembers class **.R$styleable {
    public static <fields>;
}

# For native methods, including not being called methods.
-keep class **.util.FileUtils
-keep class **.util.ProcessUtils
-keep class **.util.MessageDigests
-keep class **.graphics.GIFImage
-keep class **.graphics.BitmapUtils

-keepclassmembers class * {
    native <methods>;
}
