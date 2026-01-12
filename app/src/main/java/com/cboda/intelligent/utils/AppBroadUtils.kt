package com.cboda.intelligent.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.telephony.TelephonyManager
import android.util.Log
import com.cboda.chatui.ChatManagerBroadHolder
import com.cboda.chatui.conversation.ConversationBroadActivity
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.network.config.NetworkBroadConfig
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.gyf.immersionbar.ImmersionBar
import com.cboda.intelligent.BuildConfig
import com.cboda.intelligent.MainBroadActivity
import com.cboda.intelligent.app.LiXinBroadApp
import com.cboda.intelligent.activity.NewEquipmentBroadActivity
import com.cboda.intelligent.configuration.AppBroadConsts
import com.cboda.intelligent.service.MeetingFloatBroadService
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus


class AppBroadUtils {
    companion object {
        var activityList = ArrayList<Activity>()

        fun getAppName(context: Context): String {
            val packageManager = context.packageManager
            var appName = ""
            try {
                val applicationInfo: ApplicationInfo =
                    packageManager.getApplicationInfo(context.packageName, 0)
                appName = packageManager.getApplicationLabel(applicationInfo) as String
            } catch (e: Exception) {

            }
            return appName
        }

        fun getVersion(context: Context?): String {
            val manager: PackageManager? = context?.packageManager
            var info: PackageInfo? = null
            try {
                info = manager?.getPackageInfo(
                    context.packageName, 0
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return info?.versionName ?: ""
        }

        @SuppressLint("HardwareIds")
        fun getDeviceIMEI(context: Context): String {
//            var deviceId: String
//            val telephony: TelephonyManager = context
//                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//            deviceId = telephony.getDeviceId()
//            if (deviceId == null) {
//                deviceId = Settings.Secure.getString(
//                    context.contentResolver,
//                    Settings.Secure.ANDROID_ID
//                )
//            }
            return try {
                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                tm?.deviceId ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }
        }

        fun getVersionCode(context: Context?): Int {
            val manager: PackageManager? = context?.packageManager
            var info: PackageInfo? = null
            try {
                info = manager?.getPackageInfo(
                    context.packageName, 0
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return info?.versionCode ?: 1
        }


        /**
         * 状态栏是否使用深色 true黑色 false 白色
         */
        fun useDarkFont(context: Activity?, isDark: Boolean) {
            context?.apply {
                ImmersionBar.with(this).statusBarDarkFont(isDark).init()
            }
        }

        /**
         * show toast
         */
        fun showToast(context: Context, str: String?) {
            str?.apply {
//                Toast.makeText(context, str, Toast.LENGTH_SHORT).show()
                ToastBroadUtils.show(str)
            }
        }

        fun copy(context: Context?, str: String) {
            val cm = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.text = str
            showToast(context, "已复制到剪切板")
        }

        fun debug(tag: String, exception: String) {
            if (BuildConfig.DEBUG) {
                Log.e(tag, exception)
            }
        }

        fun createQRImage(
            content: String?, widthPix: Int = 1000, heightPix: Int = 1000
        ): Bitmap? {
            try {
//            if (content == null || "".equals(content)) {
//                return false;
//            }

                //配置参数
                val hints: MutableMap<EncodeHintType, Any?> = HashMap()
                hints[EncodeHintType.CHARACTER_SET] = "utf-8"
                //容错级别
                hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
                //设置空白边距的宽度
                hints[EncodeHintType.MARGIN] = 1 //default is 4

                // 图像数据转换，使用了矩阵转换
                var bitMatrix: BitMatrix? = null
                try {
                    bitMatrix = QRCodeWriter().encode(
                        content, BarcodeFormat.QR_CODE, widthPix,
                        heightPix, hints
                    )
                } catch (e: WriterException) {
                    e.printStackTrace()
                }
                val pixels = IntArray(widthPix * heightPix)
                // 下面这里按照二维码的算法，逐个生成二维码的图片，
                // 两个for循环是图片横列扫描的结果
                for (y in 0 until heightPix) {
                    for (x in 0 until widthPix) {
                        if (bitMatrix!![x, y]) {
                            pixels[y * widthPix + x] = -0x1000000
                        } else {
                            pixels[y * widthPix + x] = -0x1
                        }
                    }
                }

                // 生成二维码图片的格式，使用ARGB_8888
                val bitmap = Bitmap.createBitmap(widthPix, heightPix, Bitmap.Config.ARGB_8888)
                bitmap.setPixels(pixels, 0, widthPix, 0, 0, widthPix, heightPix)

//            if (logoBm != null) {
//                bitmap = addLogo(bitmap, logoBm);
//            }

                //必须使用compress方法将bitmap保存到文件中再进行读取。直接返回的bitmap是没有任何压缩的，
                // 内存消耗巨大！
                return bitmap
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
            return null
        }


        fun isFastDoubleClick(): Boolean {
            val time = System.currentTimeMillis()
            if (time - lastClickTime < 500) {
                return true
            }
            lastClickTime = time
            return false
        }

        var lastClickTime = 0L


        fun imLogin(
            context: Context?,
            apptoken: String?,
            rytoken: String?,
            uid: String?,
            yehuoUserToken: String?
        ) {
            debug("imLogin", "--> $uid $rytoken $yehuoUserToken")
            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.APPTOKEN, apptoken)
            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.UID, uid)

            MMKVBroadUtils.saveUserId(apptoken ?: "")
            MMKVBroadUtils.saveTxUserSign(rytoken ?: "")
            MMKVBroadUtils.saveRealUserId(uid ?: "")
            MMKVBroadUtils.saveMyImToken(yehuoUserToken ?: "")

            NetworkBroadConfig.updateAppToken(apptoken)


            //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
            val connextStatus =
                ChatManagerBroadHolder.gChatManager.connect(uid ?: "", yehuoUserToken ?: "")
            debug("野火", "connect----> $connextStatus --> }")
//            txLogin()
            LiXinBroadApp.initOkGo()
            MainScope().launch {
                delay(200)
                EventBus.getDefault().post(AppBroadEvent(AppEventBroadCode.EVENT_LOGIN_SUCCESS, null))
            }
            JumpBroadUtils.startMainActivity(context)
            activityList.forEachIndexed { _, activity ->
                if (activity !is MainBroadActivity) {
                    activity.finish()
                }
            }


            MMKVBroadUtils.getRealUserId()?.apply {

            }

        }


        fun goChat(context: Context?, target: String, isGroup: String) {
//            val intent =
//                Intent(context, LiXinTUIC2CChatActivity::class.java)
//            intent.putExtra(TUIConstants.TUIChat.CHAT_TYPE, V2TIMConversation.V2TIM_C2C)
//            intent.putExtra(TUIConstants.TUIChat.CHAT_ID, uid)
//            context?.startActivity(intent)

            val conversation = Conversation()
            conversation.type =
                if (isGroup == "1") Conversation.ConversationType.Group else Conversation.ConversationType.Single
            conversation.line = 0
            conversation.target = target
            // 点击会话
            val intent = Intent(
                context,
                ConversationBroadActivity::class.java
            )
            intent.putExtra("conversation", conversation)
            context?.startActivity(intent)
        }


        fun exit(context: Context?) {
            clearUserData(context)
            JumpBroadUtils.startLoginActivity(context)
            activityList.forEachIndexed { _, activity ->
                if (activity !is NewEquipmentBroadActivity) {
                    activity.finish()
                }
            }
        }


        fun clearUserData(context: Context?) {
            ChatManager.Instance().disconnect(true, false)
            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.UID, "")
            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.APPTOKEN, "")
            NetworkBroadConfig.updateAppToken(null)
            EventBus.getDefault().post(
                MeetingFloatBroadService.MeetingEvent(
                    MeetingFloatBroadService.EVENT_BACKGROUND_QUITE,
                    ""
                )
            )
            BroadConstant.userId = null
            BroadConstant.realUserId = ""
            MMKVBroadUtils.clearUserId()
            MMKVBroadUtils.clearRealUserId()
            MMKVBroadUtils.clearMyImToken()
        }

    }


}