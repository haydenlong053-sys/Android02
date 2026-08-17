package com.cboda.intelligent.net;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppContext;

import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.select.picture.utils.LanguageUtil;

public class BroadUrl {

    //2025/9/22  后端说全部从82改成84端口  数据自己建,然后再迁移，所以后期有数据问题就找他。
    public static String IP = HttpsBroadConfig.INSTANCE.getAppBaseUrl() + "/";
//    public static String WebIP = HttpsBroadConfig.appWebHost;

    private static String YHXY = "/user-agreement.html";//用户协议
    private static String YSXY = "/privacy-policy.html";//隐私协议
    private static String QDGZ = "/checkin-rewards.html";//签到规则
    private static String GYWM = "/about-seekweb4.html";//关于我们
    private static String ZXXY = "/account-deletion.html";//注销协议
    private static String thirdContent = "/third-party-sharing.html"; //第三方共享清单

    //获取验证码
    public static String sendMsg = IP + "sendMsg";
    //平台参数
//    public static String ptConfig = IP + "ptConfig";
    //设备号登录
    public static String eqLogin = IP + "eqLogin";
    //设备号注册
    public static String regist = IP + "im/user/register";
    //文件上传
    public static String uploadFile = IP + "uploadFile";
    //多文件上传
    public static String uploadFiles = IP + "uploadFiles";
    //修改个人资料 POST /im/user/updateProfile（nickname / avatar / sex / sign）
    public static String editUserinfo = IP + "im/user/updateProfile";
    //账号密码登陆
    public static String zhLogin = IP + "zhLogin";
    //手机号密码登陆
    public static String pwdLogin = IP + "pwdLogin";
    //手机区号列表
    public static String quhaoList = IP + "quhaoList";
    //支付密码找回密码
    public static String zhaohui2 = IP + "zhaohui2";
    //验证码找回密码
    public static String zhaohui = IP + "zhaohui";
    //获取图形验证码
    public static String getCode = IP + "getCode";
    //用户信息
    public static String userInfo = IP + "im/user/getUser";
    //外链列表
    public static String urlList = IP + "urlList";
    //完善资料
    public static String wanshan = IP + "wanshan";
    //我的朋友动态
    public static String myQuanList = IP + "myQuanList";
    //发布动态
    public static String pushDy = IP + "pushDy";
    //点赞朋友圈
    public static String quanLike = IP + "quanLike";
    //删除朋友圈
    public static String delQuan = IP + "delQuan";
    //我的互动消息
    public static String hudongList = IP + "hudongList";
    //评论朋友圈
    public static String quanComm = IP + "quanComm";
    //清空互动消息
    public static String delHd = IP + "delHd";
    //签到页
    public static String signInfo = IP + "signInfo";
    //签到
    public static String sign = IP + "sign";
    //签到金额明细
    public static String signLogList = IP + "signLogList";
    //密保问题列表
    public static String mbFaqList = IP + "mbFaqList";
    //设置密保问题
    public static String setMibao = IP + "setMibao";
    //设置支付密码
    public static String setPaypwd = IP + "setPaypwd";
    //验证支付密码
    public static String checkPayPwd = IP + "checkPayPwd";
    //验证短信验证码
    public static String checkCode = IP + "checkCode";
    //验证登录密码
    public static String checkPwd = IP + "checkPwd";
    //账户记录
    public static String balanceLogList = IP + "balanceLogList";
    //提现标题
    public static String txInfo = IP + "txInfo";
    //提现
    public static String tixian = IP + "tixian";
    //提现记录
    public static String txLogList = IP + "txLogList";
    //充值
    public static String rechage = IP + "rechage";
    //充值记录
    public static String rechLogList = IP + "rechLogList";
    //对方朋友圈
    public static String duiQuanList = IP + "duiQuanList";
    //朋友圈详情
    public static String quanInfo = IP + "quanInfo";
    //我的表情
    public static String emojiList = IP + "emojiList";
    //删除表情包
    public static String delEmoji = IP + "delEmoji";
    //我的收藏
    public static String collList = IP + "collList";
    //常见问题
    public static String qaList = IP + "qaList";
    //更换手机号
    public static String changePhone = IP + "changePhone";
    //更换登录密码
    public static String changePwd = IP + "changePwd";
    //验证密保问题
    public static String changeMibao = IP + "changeMibao";
    //黑名单列表
    public static String blackList = IP + "blackList";
    //拉黑、解除拉黑好友
    public static String black = IP + "black";
    //意见反馈
    public static String fankui = IP + "fankui";
    //兑换会员
    public static String duivip = IP + "duivip";
    //群组详情
    public static String groupInfo = IP + "groupInfo";
    //升级群聊
    public static String upGroup = IP + "upGroup";
    //群欢迎语
    public static String groupHyList = IP + "groupHyList";
    //添加、修改群欢迎语
    public static String addHy = IP + "addHy";
    //删除群欢迎语
    public static String delHy = IP + "delHy";
    //群定时消息
    public static String groupDsList = IP + "groupDsList";
    //删除群定时消息
    public static String delDs = IP + "delDs";
    //添加、修改群定时消息
    public static String addDs = IP + "addDs";
    //账号密码登录
    public static String accLogin = IP + "im/user/loginByPassword";
    //获取设备验证码
    public static String getAccCode = IP + "getAccCode";
    //申请注销
    public static String zhuxiao = IP + "zhuxiao";

    //获取当前的授权码
    public static String DEVICE_GET_AUTH = IP + "device/get/auth";
    //绑定设备
    public static String DEVICE_BIND = IP + "device/bind";
    //申请兑换靓号
    public static String EXCHANGE_APPLY = IP + "exchange/apply";
    //删除收藏
    public static String DEL_COLL = IP + "delColl";
    public static String getWebLanguageType() {
        if (LanguageUtil.INSTANCE.getSystemLocal(getAppContext(null)).getLanguage().startsWith("zh")) {
            //简体中文
            return "/zh";
        } else {
            return "/en";
        }
    }

    public static String getUserAgreementUrl() {
        return HttpsBroadConfig.appWebHost + getWebLanguageType() + YHXY;
    }

    public static String getPrivacyPolicyUrl() {
        return HttpsBroadConfig.appWebHost + getWebLanguageType() + YSXY;
    }

    public static String getCheckinRewardsUrl() {
        return HttpsBroadConfig.appWebHost + getWebLanguageType() + QDGZ;
    }

    public static String getAboutUrl() {
        return HttpsBroadConfig.appWebHost + getWebLanguageType() + GYWM;
    }

    public static String getAccountDeletionUrl() {
        return HttpsBroadConfig.appWebHost + getWebLanguageType() + ZXXY;
    }

    public static String getThirdContentUrl() {
        return HttpsBroadConfig.appWebHost + getWebLanguageType() + thirdContent;
    }

}
