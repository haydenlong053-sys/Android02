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
# 保留 Kotlin 元数据
-keepattributes *Annotation*,InnerClasses
-dontwarn kotlin.**

# 保留 Agora SDK（音视频）
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# 保留 Gson（JSON 序列化）
-keep class com.google.gson.** { *; }
-keep class  com.google.gson.TypeAdapterFactory
-keep class  com.google.gson.JsonSerializer
-keep class  com.google.gson.JsonDeserializer

# 保留 Jetpack Compose
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# 保留自定义 View
-keep class com.lxkj.aiim.** { *; }

# 防止 R 资源被移除
-keep class com.lxkj.aiim.R { *; }
-keepclassmembers class **.R$* {
    public static <fields>;
}
-dontshrink
-keep class org.webrtc.**  { *; }
-keep class com.serenegiant.**  { *; }
-keepclasseswithmembernames class * { native <methods>; }

-keep class okhttp3.** {*;}
-keepclassmembers class okhttp3.** {
  *;
}

-keep class com.tencent.**{*;}
-keepclassmembers class com.tenncent.mars.** {
  *;
}
-keep class com.cboda.chatui.net.base.** {*;}
-keepclassmembers class com.cboda.chatui.net.base.** {
  *;
}
-keep class cn.wildfirechat.model.** {*;}
-keepclassmembers class cn.wildfirechat.model.** {
  *;
}

-keep class com.cboda.chatui.organization.model.** {*;}
-keepclassmembers class com.cboda.chatui.organization.model.** {
  *;
}

-keepclassmembers class cn.wildfirechat.** {
    <init>(...);
}

-keepclassmembers class cn.wildfire.** {
    <init>(...);
}

-keepclassmembers class cn.wildfirechat.message.MessageContent {
    encode();
}

-keep class net.sourceforge.pinyin4j.** { *;}

-dontwarn razerdp.basepopup.**
-keep class razerdp.basepopup.**{*;}

-keep class com.luck.picture.lib.** { *; }

-keep class com.luck.lib.camerax.** { *; }

-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop** { *; }
-keep interface com.yalantis.ucrop** { *; }
-dontwarn com.serenegiant.common.R$string
-dontwarn com.serenegiant.dialog.MessageDialogFragment$MessageDialogListener
-dontwarn com.serenegiant.dialog.MessageDialogFragment
-dontwarn com.serenegiant.utils.BuildCheck
-dontwarn com.serenegiant.utils.PermissionCheck
-dontwarn java.awt.BorderLayout
-dontwarn java.awt.Component
-dontwarn java.awt.Container
-dontwarn java.awt.Dimension
-dontwarn java.awt.Font
-dontwarn java.awt.GridLayout
-dontwarn java.awt.LayoutManager
-dontwarn java.awt.event.ActionEvent
-dontwarn java.awt.event.ActionListener
-dontwarn java.awt.event.WindowAdapter
-dontwarn java.awt.event.WindowEvent
-dontwarn java.awt.event.WindowListener
-dontwarn javax.swing.Icon
-dontwarn javax.swing.JApplet
-dontwarn javax.swing.JButton
-dontwarn javax.swing.JComboBox
-dontwarn javax.swing.JFrame
-dontwarn javax.swing.JLabel
-dontwarn javax.swing.JPanel
-dontwarn javax.swing.JScrollPane
-dontwarn javax.swing.JTabbedPane
-dontwarn javax.swing.JTextArea
-dontwarn javax.swing.JTextField
-dontwarn org.joda.convert.FromString
-dontwarn org.joda.convert.ToString

# DoraemonKit 相关 - 忽略 Hummer 相关类的警告（可选依赖）
-dontwarn com.didi.hummer.context.HummerContext
-dontwarn com.didi.hummer.**
