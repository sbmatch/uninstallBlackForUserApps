# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-allowaccessmodification

# 包名不混合大小写
#-dontusemixedcaseclassnames
# 禁用预验证
-dontpreverify
# 忽略警告
-dontwarn com.**
#====================抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable
#====================代码混淆压缩比，在0~7之间
-optimizationpasses 5