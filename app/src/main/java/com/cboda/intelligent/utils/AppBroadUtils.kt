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
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.network.config.NetworkBroadConfig
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.USER_SEND_LINK
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.chatui.ChatManagerBroadHolder
import com.cboda.chatui.conversation.ConversationBroadActivity
import com.cboda.intelligent.activity.NewEquipmentBroadActivity
import com.cboda.intelligent.app.MyBroadApp
import com.cboda.intelligent.configuration.AppBroadConsts
import com.freddy.chat.MessageListActivity
import com.freddy.chat.SessionItemIntent
import com.freddy.chat.im.IMSClientBootstrap
import com.freddy.im.IMSConfig
import com.freddy.im.http.ImHttpConfig
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.db.ImLocalStore
import com.freddy.im.signal.ImSignalProtos
import com.freddy.im.util.ImUtils
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.getAppVersion
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.gyf.immersionbar.ImmersionBar
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import kotlin.jvm.java


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


        fun getVersion(context: Context? = null): String {
            return getAppVersion(context)
        }

        @SuppressLint("HardwareIds")
//        fun getDeviceIMEI(context: Context): String {
////            var deviceId: String
////            val telephony: TelephonyManager = context
////                .getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
////            deviceId = telephony.getDeviceId()
////            if (deviceId == null) {
////            deviceId = DeviceBroadUtil.getAndroidId(context)
////            }
//            return try {
//                val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
//                tm?.deviceId ?: "Unknown"
//            } catch (e: Exception) {
//                "Unknown"
//            }
//        }

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
            eLogD(tag, exception)
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
            uid: String?,
            myImUserToken: String?,
            fromType: String = ""
        ) {
            eLogD("imLogin", "--> $uid  $myImUserToken")

            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.APPTOKEN, apptoken)
            SharePrefBroadBroadUtil.saveString(context, AppBroadConsts.UID, uid)
            NetworkBroadConfig
            MMKVBroadUtils.saveUserAppToken(apptoken ?: "")
//            MMKVBroadUtils.saveTxUserSign(rytoken ?: "")
            MMKVBroadUtils.saveRealUserId(uid ?: "")
            MMKVBroadUtils.saveMyImToken(myImUserToken ?: "")

            NetworkBroadConfig.updateAppToken(apptoken)
            goLoginMyIm(apptoken, uid)
            //自研隐藏此处野火im
            //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
            val connextStatus = ChatManagerBroadHolder.gChatManager.connect(uid ?: "", myImUserToken ?: "")
            eLogD("野火", "connect----> $connextStatus --> }")

            MyBroadApp.initOkGo()
            MainScope().launch {
                delay(200)
                EventBus.getDefault().post(AppBroadEvent(AppEventBroadCode.EVENT_LOGIN_SUCCESS, null))
            }
//            activityList.forEachIndexed { _, activity ->
//                if (activity !is MainBroadActivity) {
//                    activity.finish()
//                }
//            }
            JumpBroadUtils.startMainActivity(context, fromType)


//            MMKVBroadUtils.getRealUserId()?.apply {
//
//            }

        }

        fun goLoginMyIm(apptoken: String?, userId: String?) {
//            eLogD("EasyHttp", "setGlobalHeaders--> $userId  $apptoken")
            IMSClientBootstrap.getInstance().init(userId, apptoken, HttpsBroadConfig.MY_IM_HOST, IMSConfig.APP_STATUS_FOREGROUND)
            ImHttpConfig.setGlobalHeaders(apptoken, userId, ImSignalProtos.ClientType.ANDROID_VALUE)
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

        //自研-进入单聊1791854966a9485c8483ac3cce077ff6!85a581c1b0e74647a23324326ca055da
        fun goChatP2p(target: String, name: String?, context: Context? = null) {
            val mContext = getAppContext(context)
            val sessionItem = SessionItem()
            sessionItem.sessionId = ImUtils.getP2pSessionIdId(MMKVBroadUtils.getRealUserId(), target)
            sessionItem.conversation = ImSignalProtos.ConversationType.PAIR.number
            sessionItem.oppositeUserId = target
            sessionItem.sessionName = name
            ImLocalStore.saveSessions(listOf(sessionItem))
            val intent = Intent(mContext, MessageListActivity::class.java)
            SessionItemIntent.putSessionItem(intent, sessionItem)
            mContext.startActivity(intent)
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
            NetworkBroadConfig.updateAppToken("")
//            EventBus.getDefault().post(
//                MeetingFloatBroadService.MeetingEvent(
//                    MeetingFloatBroadService.EVENT_BACKGROUND_QUITE,
//                    ""
//                )
//            )
            BroadConstant.userAppToken = null
            BroadConstant.realUserId = ""
            MMKVBroadUtils.clearUserId()
            MMKVBroadUtils.clearRealUserId()
            MMKVBroadUtils.clearMyImToken()
            MMKVBroadUtils.saveSqlStrData(USER_SEND_LINK, "false")
        }

    }


}