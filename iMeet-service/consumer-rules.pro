# 保留 Agora SDK（音视频）
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# 保留所有 public 类和方法
-keep public class com.example.mylib.** {
    public <methods>;
    public <fields>;
}