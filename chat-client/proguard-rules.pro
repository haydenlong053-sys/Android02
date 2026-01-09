#-keepattributes InnerClasses,EnclosingMethod,Signature
#-keep class com.tencent.mars.** { *; }
#-keepclassmembers class com.tencent.mars.** {
#  *;
#}
#-keep class com.tencent.mars.sdt.SdtLogic { *; }
#-keep class com.tencent.mars.sdt.SdtLogic$* { *; }
#-keep interface com.tencent.mars.sdt.SdtLogic$* { *; }
#-keepnames class com.tencent.mars.sdt.SdtLogic$*
#-keepnames interface com.tencent.mars.sdt.SdtLogic$*
#-keepclassmembers class com.tencent.mars.sdt.SdtLogic {
#    public static interface *;
#    public static class *;
#}