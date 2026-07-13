# ========== 自研 IM SDK (im_lib) — 随 consumerProguardFiles 合并到宿主 release ==========

-keepattributes Signature,InnerClasses,EnclosingMethod,*Annotation*

# ---------- Gson / JSON 反序列化（字段名不可混淆） ----------
-keep class com.freddy.im.http.model.** { <fields>; }
-keep class com.freddy.im.bean.** { <fields>; }

# EasyHttp 请求体：private 字段名即 JSON 键名
-keep class com.freddy.im.http.api.** { <fields>; }
-keep interface com.hjq.http.config.IRequestApi
-keep class * implements com.hjq.http.config.IRequestApi { <fields>; }
-keep class com.hjq.http.** { *; }
-dontwarn com.hjq.http.**

# ---------- Protobuf 信令（Netty 编解码） ----------
-keep class com.google.protobuf.** { *; }
-dontwarn com.google.protobuf.**
-keep class com.freddy.im.signal.ImSignalProtos { *; }
-keep class com.freddy.im.signal.ImSignalProtos$* { *; }

# ---------- Netty 长连接 ----------
-keep class com.freddy.im.netty.** { *; }
-keep class io.netty.** { *; }
-dontwarn io.netty.**
-keepclassmembers class io.netty.** { *; }

# ---------- Realm 本地库 ----------
-keep @io.realm.annotations.RealmClass class * { *; }
-keep class com.freddy.im.db.entity.** { <fields>; }
-keep class com.freddy.im.db.mapper.** { *; }
-keep class io.realm.** { *; }
-dontwarn io.realm.**

# ---------- SDK 核心（连接、消息分发、群成员同步等） ----------
-keep class com.freddy.im.** { *; }
-keep interface com.freddy.im.** { *; }

# OthBusiSig 按 code 注册的 Handler
-keep class * implements com.freddy.im.signal.othbusi.OthBusiSigHandler { *; }

# 加解密
-keep class com.freddy.im.crypto.** { *; }
