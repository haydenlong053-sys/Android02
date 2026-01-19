#-keepattributes InnerClasses,EnclosingMethod,Signature

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
-keep class com.cboda.intelligent.** { *; }

# 防止 R 资源被移除
-keep class com.cboda.intelligent.R { *; }
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

-keep class com.cboda.select.picture.** { *; }

-keep class com.cboda.lib.camerax.** { *; }

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
-dontwarn com.amap.api.**
-dontwarn com.baidu.location.**
-dontwarn com.facebook.cache.**
-dontwarn com.facebook.common.**
-dontwarn com.facebook.imagepipeline.**

-dontwarn com.loc.**
-dontwarn com.nostra13.universalimageloader.core.**
-dontwarn com.squareup.javapoet.**
-dontwarn com.tencent.map.geolocation.**
-dontwarn com.tencent.smtt.export.external.interfaces.**
-dontwarn com.tencent.smtt.sdk.**
-dontwarn java.awt.image.**
-dontwarn javax.imageio.**
-dontwarn javax.lang.model.**
-dontwarn javax.tools.**






-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontpreverify           # 混淆时是否做预校验
-dontskipnonpubliclibraryclassmembers # 指定不去忽略非公共库的类成员
-verbose                # 混淆时是否记录日志
#-dontshrink #不压缩输入的类文件
#-dontoptimize #不优化输入的类文件
# 避免混淆泛型
-keepattributes Signature
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法
-keep public class * extends android.app.Activity      # 保持哪些类不被混淆
-keep public class * extends android.app.Fragment      # 保持哪些类不被混淆
-keep public class * extends android.app.Application   # 保持哪些类不被混淆
-keep public class * extends android.app.Service       # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver  # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider    # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference        # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService    # 保持哪些类不被混淆
-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}


-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

-keep class android.support.** { *; }
# 保留继承的
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**
# 保留R下面的资源
-keep class **.R$* {*;}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclassmembers class * implements android.os.Parcelable {
    static android.os.Parcelable$Creator CREATOR;
}

-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}

-keepnames class * implements java.io.Serializable

-keepclassmembers class * implements java.io.Serializable {
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers enum * {     # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable { # 保持 Parcelable 不被混淆
    public static final android.os.Parcelable$Creator *;
}

















-keep class com.cboda.chatemoji.MoonBroadUtils { *; }
-keep class com.cboda.chatemoji.LQREmotionBroadKit { *; }
-keep class com.cboda.chatemoji.EmotionBroadLayout { *; }
-keep class com.cboda.chatemoji.IEmotionExtClickBroadListener { *; }
-keep class com.cboda.chatemoji.IEmotionSelectedBroadListener { *; }
-keep class com.cboda.chatemoji.EmotionBroadEvent { *; }
-keep class com.cboda.chatemoji.IImageBroadLoader { *; }


-keep class com.cboda.baselibrary.network.**{*;}
-keep class com.cboda.baselibrary.bean.**{*;}
-keep class com.cboda.baselibrary.event.**{*;}
-keep class com.cboda.intelligent.bean.**{*;}
-keep class com.cboda.baselibrary.app.UtilBroadApplication { *; }
-keep class com.cboda.baselibrary.utils.CrashUtilKt { *; }
-keep class com.cboda.baselibrary.utils.MMKVBroadUtils { *; }
-keep class com.cboda.baselibrary.utils.MMKVBroadUtils$Companion { *; }
-keep class com.cboda.baselibrary.utils.HttpsBroadConfig { *; }
-keep class com.cboda.baselibrary.toast.ToastBroadUtils { *; }
-keep class com.cboda.baselibrary.utils.OnSingleClickBroadListener { *; }
-keep class com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter { *; }
-keep class com.cboda.baselibrary.utils.ViewExpandUtilKt { *; }
-keep class com.cboda.baselibrary.utils.DeviceBroadUtil { *; }
-keep class com.cboda.baselibrary.utils.AppPermissionBroadUtils { *; }
-keep class com.cboda.baselibrary.utils.LauncherFragmentBroadHelper { *; }
-keep class cn.wildfirechat.remote.**{*;}
-keep class cn.wildfirechat.message.core.**{*;}
-keep class cn.wildfirechat.client.**{*;}
-keep class cn.wildfirechat.utils.**{*;}
-keep class cn.wildfirechat.message.**{*;}
-keep class cn.wildfirechat.viewmodel.**{*;}
-keep class com.tencent.mars.sdt.SdtLogic$ICallBack { *; }


-keep class cn.wildfirechat.message.Message { *; }
-keep class cn.wildfirechat.viewmodel.SocialAction { *; }
-keep class cn.wildfirechat.uistate.MessagePageUiState { *; }
-keep class cn.wildfirechat.event.MsgUnReadCountEvent { *; }
-keep class com.cboda.intelligent.adapter.AIImgBroadAdapter { *; }
-keep class com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorBroadAdapter { *; }
-keep class com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorBroadAdapter$OnIndicatorItemClickListener { *; }



























-keep class com.tencent.mars.** { *; }
-keepclassmembers class com.tencent.mars.** {
  *;
}
-keep class com.tencent.mars.sdt.SdtLogic { *; }
-keep class com.tencent.mars.sdt.SdtLogic$* { *; }
-keep interface com.tencent.mars.sdt.SdtLogic$* { *; }
-keepnames class com.tencent.mars.sdt.SdtLogic$*
-keepnames interface com.tencent.mars.sdt.SdtLogic$*
-keepclassmembers class com.tencent.mars.sdt.SdtLogic {
    public static interface *;
    public static class *;
}

-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.**{*;}

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

#-keep class !cn.wildfire.chat.moment.**,!cn.wildfirechat.moment.**, **{ *; }
-keep class cn.wildfirechat.moment.MomentClient {
    public void init(***);
}

-keep class com.cboda.intelligent.myim.model.** {*;}
-keepclassmembers class com.cboda.intelligent.myim.model.** {
  *;
}

-keep class com.cboda.chatui.net.base.** {*;}
-keepclassmembers class com.cboda.chatui.net.base.** {
  *;
}

-keep class  com.cboda.chatui.voip.conference.model.** {*;}
-keepclassmembers class  com.cboda.chatui.voip.conference.model.** {
  *;
}

-keep class com.cboda.chatui.group.GroupAnnouncement {*;}
-keepclassmembers class com.cboda.chatui.group.GroupAnnouncement {
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

#-keepclassmembers class cn.wildfire.** {
#    <init>(...);
#}

-keepclassmembers class cn.wildfirechat.message.MessageContent {
    encode();
}

-keep class net.sourceforge.pinyin4j.** { *;}


#huawei push
-ignorewarnings
-keepattributes *Annotation*
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes Signature
-keepattributes SourceFile,LineNumberTable
-keep class com.hianalytics.android.**{*;}
-keep class com.huawei.updatesdk.**{*;}
-keep class com.huawei.hms.**{*;}




#===============================ucrop start==============================
-dontwarn com.yalantis.ucrop**
-keep class com.yalantis.ucrop.** { *; }
-keep interface com.yalantis.ucrop.** { *; }
#===============================ucrop end==============================

#===============================androidx start==============================
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep class androidx.core.**{*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**
-keep class androidx.core.app.CoreComponentFactory {*;}
-keep class android.app.AppComponentFactory {*;}
#===============================androidx end==============================

#===============================Fresco  start=============================
-keep,allowobfuscation @interface com.facebook.common.internal.DoNotStrip
-keep,allowobfuscation @interface com.facebook.soloader.DoNotOptimize

-keep class com.facebook.imagepipeline.animated.** { *; }
-keep class com.facebook.imagepipeline.gif.** { *; }
-keep class com.facebook.imagepipeline.webp.** { *; }
-keep class com.facebook.animated.** { *; }


# Do not strip any method/class that is annotated with @DoNotStrip
-keep @com.facebook.common.internal.DoNotStrip class *
-keepclassmembers class * {
    @com.facebook.common.internal.DoNotStrip *;
}

# Do not strip any method/class that is annotated with @DoNotOptimize
-keep @com.facebook.soloader.DoNotOptimize class *
-keepclassmembers class * {
    @com.facebook.soloader.DoNotOptimize *;
}
#===============================Fresco  end=============================

#===============================app更新  start=============================
-dontwarn com.king.app.updater.**
-keep class com.king.app.updater.**{ *;}
-keep class * extends com.king.app.updater.**{ *;}
-keep class * implements com.king.app.updater.**{ *;}
-keepattributes InnerClasses

-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

-dontwarn com.king.app.dialog.**
-keep class com.king.app.dialog.**{ *;}

-keep class * extends android.app.Dialog

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#===============================app更新  end=============================

#===============================ViewBinding  start=============================
-keep class * implements androidx.viewbinding.ViewBinding {
    *;
}
-keepclassmembers public class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
#===============================ViewBinding  end=============================

#===============================声网  start=============================
-keep class io.agora.**{*;}
#===============================声网  end=============================

#===============================retrofit start=============================
# Retrofit does reflection on generic parameters. InnerClasses is required to use Signature and
# EnclosingMethod is required to use InnerClasses.
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# Keep annotation default values (e.g., retrofit2.http.Field.encoded).
-keepattributes AnnotationDefault

# Retain service method parameters when optimizing.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Ignore annotation used for build tooling.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Guarded by a NoClassDefFoundError try/catch and only used when on the classpath.
-dontwarn kotlin.Unit

# Top-level functions that can only be used by Kotlin.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# With R8 full mode, it sees no subtypes of Retrofit interfaces since they are created with a Proxy
# and replaces all potential values with null. Explicitly keeping the interfaces prevents this.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation


#===============================retrofit end=============================


-keep class com.ishumei.** {*;}

#===============================ARouter start============================
-keep public class com.alibaba.android.arouter.routes.**{*;}
-keep public class com.alibaba.android.arouter.facade.**{*;}
-keep class * implements com.alibaba.android.arouter.facade.template.ISyringe{*;}

# 如果使用了 byType 的方式获取 Service，需添加下面规则，保护接口
-keep interface * implements com.alibaba.android.arouter.facade.template.IProvider

# 如果使用了 单类注入，即不定义接口实现 IProvider，需添加下面规则，保护实现
-keep class * implements com.alibaba.android.arouter.facade.template.IProvider
#===============================ARouter end============================


-keep class com.shuyu.gsyvideoplayer.video.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.**
-keep class com.shuyu.gsyvideoplayer.video.base.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.video.base.**
-keep class com.shuyu.gsyvideoplayer.utils.** { *; }
-dontwarn com.shuyu.gsyvideoplayer.utils.**
-keep class tv.danmaku.ijk.** { *; }
-dontwarn tv.danmaku.ijk.**
-keep class com.google.android.exoplayer2.** {*;}
-keep interface com.google.android.exoplayer2.**

-keep public class * extends android.view.View{
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, java.lang.Boolean);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-dontwarn io.microshow.rxffmpeg.**
-keep class io.microshow.rxffmpeg.**{*;}

#------------------------------------------adapter start-------------------------------
-keep public class * extends com.chad.library.adapter.base.viewholder.BaseViewHolder
-keepclassmembers  class **$** extends com.chad.library.adapter.base.viewholder.BaseViewHolder {
     <init>(...);
}
#------------------------------------------adapter end-------------------------------

#------------------------------------------loadsir start-------------------------------
-dontwarn com.kingja.loadsir.**
-keep class com.kingja.loadsir.** {*;}
#------------------------------------------loadsir end-------------------------------

-keep class com.zackratos.ultimatebarx.ultimatebarx.** { *; }
-keep public class * extends androidx.fragment.app.Fragment { *; }

#------------------------------------------xpopup start-------------------------------
-dontwarn com.lxj.xpopup.widget.**
-keep class com.lxj.xpopup.widget.**{*;}
#------------------------------------------xpopup end-------------------------------

#===============================CalenderView start============================
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
#===============================CalenderView end============================


#===============================svgaplayer start=============================
-keep class com.squareup.wire.** { *; }
-keep class com.opensource.svgaplayer.proto.** { *; }
#===============================svgaplayer end=============================

#===============================eventbus start=============================
-keepattributes *Annotation*
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}
#===============================eventbus end=============================


#===============================PictureSelector  start=============================
-keep class com.luck.picture.lib.** { *; }
-keep class com.luck.lib.camerax.** { *; }
#===============================PictureSelector  end=============================

#===============================阿里云 start=============================
-keep class com.alibaba.sdk.android.oss.** { *; }
-dontwarn org.apache.commons.codec.binary.**
#===============================阿里云 end=============================

#===============================gson start==============================
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# 使用Gson时需要配置Gson的解析对象及变量都不混淆。不然Gson会找不到变量。

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
#===============================gson end==============================

#===============================fastjson start==============================
-dontwarn com.alibaba.fastjson.**
-keep class com.alibaba.fastjson.** { *; }
#===============================fastjson end==============================

#===============================okhttp3 start==============================
-dontwarn com.squareup.okhttp3.**
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }
-keep class com.squareup.okhttp3.** { *;}
-dontwarn okio.**
#===============================okhttp3 end==============================

-dontwarn com.cmic.gen.sdk.**
-keep class com.cmic.gen.sdk.**{*;}
-dontwarn com.sdk.**
-keep class com.sdk.** { *;}
-dontwarn com.unikuwei.mianmi.account.shield.**
-keep class com.unikuwei.mianmi.account.shield.** {*;}
-keep class cn.com.chinatelecom.account.api.**{*;}

#===============================okio start==============================
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }
#===============================okio end==============================

#===============================RxJava RxAndroid start==============================
-dontwarn sun.misc.**
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
 long producerIndex;
 long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
 rx.internal.util.atomic.LinkedQueueNode consumerNode;
}
#-keep io.reactivex.rxjava2:rxjava:2.2.19
#===============================RxJava RxAndroid end==============================

#===============================webview start==============================
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
 public *;
}
-keepclassmembers class * extends android.webkit.webViewClient {
  public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
  public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.webViewClient {
  public void *(android.webkit.webView, jav.lang.String);
}
#===============================webview end==============================
#===============================glide start==============================
-dontwarn com.bumptech.glide.load.resource.bitmap.VideoDecoder
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#===============================glide end==============================










# 保留 Agora SDK（音视频）
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# 保留所有 public 类和方法
-keep public class com.example.mylib.** {
    public <methods>;
    public <fields>;
}

-keep class com.cboda.agora.service.model.** { *; }
-keep class com.cboda.agora.service.service.** { *; }
-keep class com.cboda.agora.service.utils.** { *; }
-keep class com.orhanobut.logger.** { *; }












































#===============================app模块核心类 start==============================
# 保留 app 模块的所有类
-keep class com.cboda.intelligent.** { *; }
-keepclassmembers class com.cboda.intelligent.** {
    *;
}

# 保留 app 模块的所有 ViewModel
-keep class com.cboda.intelligent.viewmodel.** { *; }
-keepclassmembers class com.cboda.intelligent.viewmodel.** {
    *;
}

# 保留 app 模块的所有 Repository
-keep class com.cboda.intelligent.repository.** { *; }
-keepclassmembers class com.cboda.intelligent.repository.** {
    *;
}

# 保留 app 模块的所有 Model/Bean
-keep class com.cboda.intelligent.model.** { *; }
-keep class com.cboda.intelligent.bean.** { *; }
-keepclassmembers class com.cboda.intelligent.model.** {
    *;
}
-keepclassmembers class com.cboda.intelligent.bean.** {
    *;
}

# 保留 app 模块的所有 data class（Kotlin）
-keep class com.cboda.intelligent.**$$serializer { *; }
-keepclassmembers class com.cboda.intelligent.** {
    <init>(...);
}

# 保留 app 模块的所有 enum class
-keepclassmembers enum com.cboda.intelligent.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留 app 模块的所有 Compose 相关类
-keep class com.cboda.intelligent.compose.** { *; }
-keepclassmembers class com.cboda.intelligent.compose.** {
    *;
}

# 保留 app 模块的所有 UI State
-keep class com.cboda.intelligent.uistate.** { *; }
-keepclassmembers class com.cboda.intelligent.uistate.** {
    *;
}

# 保留 app 模块的所有 Adapter
-keep class com.cboda.intelligent.adapter.** { *; }
-keepclassmembers class com.cboda.intelligent.adapter.** {
    *;
}

# 保留 app 模块的所有 Holder
-keep class com.cboda.intelligent.holder.** { *; }
-keepclassmembers class com.cboda.intelligent.holder.** {
    *;
}

# 保留 app 模块的所有 Dialog
-keep class com.cboda.intelligent.dialog.** { *; }
-keepclassmembers class com.cboda.intelligent.dialog.** {
    *;
}

# 保留 app 模块的所有自定义 View
-keep class com.cboda.intelligent.widge.** { *; }
-keepclassmembers class com.cboda.intelligent.widge.** {
    *;
}

# 保留 app 模块的所有 Service
-keep class com.cboda.intelligent.service.** { *; }
-keepclassmembers class com.cboda.intelligent.service.** {
    *;
}

# 保留 app 模块的所有 Fragment
-keep class com.cboda.intelligent.fragment.** { *; }
-keep class com.cboda.intelligent.base.** { *; }
-keepclassmembers class com.cboda.intelligent.fragment.** {
    *;
}
-keepclassmembers class com.cboda.intelligent.base.** {
    *;
}

# 保留 app 模块的所有 Activity
-keep class com.cboda.intelligent.activity.** { *; }
-keepclassmembers class com.cboda.intelligent.activity.** {
    *;
}

# 保留 app 模块的所有工具类
-keep class com.cboda.intelligent.utils.** { *; }
-keepclassmembers class com.cboda.intelligent.utils.** {
    *;
}

# 保留 app 模块的配置类
-keep class com.cboda.intelligent.configuration.** { *; }
-keepclassmembers class com.cboda.intelligent.configuration.** {
    *;
}

# 保留 app 模块的网络相关类
-keep class com.cboda.intelligent.net.** { *; }
-keepclassmembers class com.cboda.intelligent.net.** {
    *;
}

# 保留 app 模块的 WebView 相关类
-keep class com.cboda.intelligent.webview.** { *; }
-keepclassmembers class com.cboda.intelligent.webview.** {
    *;
}

# 保留 app 模块的 MyIM 相关类
-keep class com.cboda.intelligent.myim.** { *; }
-keepclassmembers class com.cboda.intelligent.myim.** {
    *;
}
#===============================app模块核心类 end==============================

#===============================baselibrary模块 start==============================
# 保留 baselibrary 模块的所有类
-keep class com.cboda.baselibrary.** { *; }
-keepclassmembers class com.cboda.baselibrary.** {
    *;
}

# 保留 baselibrary 模块的 bean 类
-keep class com.cboda.baselibrary.bean.** { *; }
-keepclassmembers class com.cboda.baselibrary.bean.** {
    *;
}

# 保留 baselibrary 模块的网络类
-keep class com.cboda.baselibrary.network.** { *; }
-keepclassmembers class com.cboda.baselibrary.network.** {
    *;
}

# 保留 baselibrary 模块的工具类
-keep class com.cboda.baselibrary.utils.** { *; }
-keepclassmembers class com.cboda.baselibrary.utils.** {
    *;
}

# 保留 baselibrary 模块的自定义 View
-keep class com.cboda.baselibrary.view.** { *; }
-keepclassmembers class com.cboda.baselibrary.view.** {
    *;
}
#===============================baselibrary模块 end==============================

#===============================chat-ui模块补充 start==============================
# 保留 chat-ui 模块的所有类（补充）
-keep class com.cboda.chatui.** { *; }
-keepclassmembers class com.cboda.chatui.** {
    *;
}

# 保留 chat-ui 模块的 ViewModel
-keep class com.cboda.chatui.viewmodel.** { *; }
-keepclassmembers class com.cboda.chatui.viewmodel.** {
    *;
}

# 保留 chat-ui 模块的 ViewHolder
-keep class com.cboda.chatui.conversation.message.viewholder.** { *; }
-keepclassmembers class com.cboda.chatui.conversation.message.viewholder.** {
    *;
}

# 保留 chat-ui 模块的扩展功能
-keep class com.cboda.chatui.conversation.ext.** { *; }
-keepclassmembers class com.cboda.chatui.conversation.ext.** {
    *;
}

# 保留 chat-ui 模块的 VOIP 相关类
-keep class com.cboda.chatui.voip.** { *; }
-keepclassmembers class com.cboda.chatui.voip.** {
    *;
}

# 保留 chat-ui 模块的群组相关类
-keep class com.cboda.chatui.group.** { *; }
-keepclassmembers class com.cboda.chatui.group.** {
    *;
}

# 保留 chat-ui 模块的联系人相关类
-keep class com.cboda.chatui.contact.** { *; }
-keepclassmembers class com.cboda.chatui.contact.** {
    *;
}

# 保留 chat-ui 模块的搜索相关类
-keep class com.cboda.chatui.search.** { *; }
-keepclassmembers class com.cboda.chatui.search.** {
    *;
}

# 保留 chat-ui 模块的音频相关类
-keep class com.cboda.chatui.audio.** { *; }
-keepclassmembers class com.cboda.chatui.audio.** {
    *;
}

# 保留 chat-ui 模块的第三方集成类
-keep class com.cboda.chatui.third.** { *; }
-keepclassmembers class com.cboda.chatui.third.** {
    *;
}
#===============================chat-ui模块补充 end==============================

#===============================Kotlin相关 start==============================
# Kotlin 数据类
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin 协程
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

# Kotlin 序列化（如果使用）
-keepattributes *Annotation*, InnerClasses
-keepclassmembers class kotlinx.serialization.** {
    *;
}
-keep,includedescriptorclasses class com.cboda.intelligent.**$$serializer { *; }
-keepclassmembers class com.cboda.intelligent.** {
    *** Companion;
}
-keepclasseswithmembers class com.cboda.intelligent.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Kotlin 反射
-keep class kotlin.reflect.** { *; }
-keepclassmembers class kotlin.reflect.** {
    *;
}

# Kotlin 伴生对象
-keepclassmembers class ** {
    public static ** Companion;
}

# Kotlin 密封类
-keep class * extends kotlin.Enum { *; }
-keepclassmembers class * extends kotlin.Enum {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
#===============================Kotlin相关 end==============================

#===============================Jetpack Compose补充 start==============================
# Compose 运行时
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.foundation.** { *; }
-keep class androidx.compose.material.** { *; }
-keep class androidx.compose.material3.** { *; }
-keep class androidx.compose.animation.** { *; }

# Compose 状态管理
-keep class androidx.compose.runtime.State { *; }
-keep class androidx.compose.runtime.MutableState { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable <methods>;
}

# Compose 导航
-keep class androidx.navigation.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Stable <methods>;
}
#===============================Jetpack Compose补充 end==============================

#===============================LiveData和ViewModel补充 start==============================
# 保留所有 ViewModel
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(...);
}

# 保留所有 LiveData
-keep class * extends androidx.lifecycle.LiveData { *; }
-keep class * extends androidx.lifecycle.MutableLiveData { *; }
-keepclassmembers class * extends androidx.lifecycle.LiveData {
    <init>(...);
}
#===============================LiveData和ViewModel补充 end==============================

#===============================数据类补充 start==============================
# 保留所有 data class 的成员
-keepclassmembers class * {
    @kotlin.jvm.JvmField <fields>;
}

# 保留所有包含 companion object 的类
-keepclassmembers class * {
    public static ** Companion;
}

# 保留所有枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# 保留所有 Parcelable 实现类
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
    public static final ** CREATOR;
}

# 保留所有 Serializable 实现类
-keepnames class * implements java.io.Serializable
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}
#===============================数据类补充 end==============================

#===============================反射相关 start==============================
# 保留通过反射调用的类和方法
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault
-keepattributes EnclosingMethod
-keepattributes Exceptions

# 保留所有注解
-keepattributes *Annotation*
-keep class * extends java.lang.annotation.Annotation { *; }

# 保留通过反射访问的字段
-keepclassmembers class * {
    @androidx.annotation.Keep <methods>;
    @androidx.annotation.Keep <fields>;
}
#===============================反射相关 end==============================

#===============================自定义View补充 start==============================
# 保留所有自定义 View 的构造函数
-keepclasseswithmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留所有自定义 ViewGroup
-keepclasseswithmembers class * extends android.view.ViewGroup {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 保留所有自定义 Dialog
-keep class * extends android.app.Dialog {
    public <init>(...);
}

# 保留所有自定义 Fragment
-keep class * extends androidx.fragment.app.Fragment {
    public <init>(...);
}
#===============================自定义View补充 end==============================

#===============================R类资源补充 start==============================
# 保留所有模块的 R 类
-keep class **.R
-keep class **.R$* {
    *;
}

# 保留所有 BuildConfig
-keep class **.BuildConfig {
    *;
}
#===============================R类资源补充 end==============================

