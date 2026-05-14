# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\intel\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools-proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Remove all Android Logs from the release build
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
}

# Remove System.out.println and print
-assumenosideeffects class java.io.PrintStream {
    public static *** println(...);
    public static *** print(...);
}
