package com.cboda.intelligent.bean;

import com.cboda.select.picture.entity.CircleCommListBroadBean;
import com.cboda.select.picture.entity.DataListBroadBean;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

/**
 * Created by kxn on 2019/1/21 0021.
 */
public class ResultBroadBean extends BaseBroadBean implements Serializable {
    public String tQrcode2;
    public String imtoken;
    public String idno;
    public String qrcode;
    public String eqno;//设备唯一标识
    public String uuid;
    public String userId;
    public String userSign;
    public String apptoken;
    public String isws;
    public String url;
    public String yanzm;
    public String dzNames;
    public String groupId;
    public String isFirst;
    public String totalCount;
    public String totalPage;
    public String icon;
    public String name;

    public String backImg;
    public List<String> urls;
    public List<DataListBroadBean> dataList;
    public List<CircleCommListBroadBean> likeList;
    public Data data;

    public class Data {
        public int vipRedeemEnabled;
        public int vipPurchaseEnabled;
        public String loginType;
        public String hasInvit;
        public String showFx;
        public String showXcx;
        public String openBj;
        public String openCh;
        public String sname;
        public String sno;
        public String skm;
        public String openMibao;
        public String tx1;
        public String tx2;
        public String cz;
        public String id;
        public String icon;
        public String img;
        public String name;
        public String sex;
        public String idno;
        public String phone;
        public String mbName;//密保问题
        public String mbAnswer;//密保答案
        public String twoFactorCode;//谷歌二次验证码
        public String paypwd;//支付密码
        public String eqno;//设备唯一标识
        public String acount;
        public String qrcode;
        public String tQrcode;

        public String qrcode2;
        public String sign;
        public String showQuan;
        public String balalce;
        public String pointTotal;
        public String hasPaypwd;
        public String regType;
        public String mbid;//密保id
        public String mb;
        public String isAddYz;
        public String isQuantx;
        public String isMsgtx;
        public String noadd;
        public String accSerch;
        public String phoneSerch;
        public String idnoSerch;
        public String groAdd;
        public String mpAdd;
        public String qrAdd;
        public String ziti;
        public String chatImg;
        public String kfid;
        public String quanCount;
        public String xcxUrl;
        public String hudong;
        public String hdIcon;
        public String info;
        public String huifu;
        public String time;
        public String regImg;
        public String fileType;
        public String regInfo;
        public String signMoney;
        public String days;
        public String isSign;
        public String isFri;
        public String type;
        public String address;
        public String lon;
        public String lat;
        public String userId;
        public String isDz;
        public String dzNames;
        public String video;
        public String vimg;
        public String regInfo2;
        public String groupinfo;
        public String appinfo;
        public String isvip;//是否会员
        public String vipTime;//会员到期时间
        public String vipinfo;
        public String gsize;
        public String autofanyi;
        public String biandabianyi;
        public String pwd;//密码
        public String zxState;//注销申请状态 1：审核中 2：通过 3：驳回 4：未申请
        public String zxReson;//注销驳回原因
        public String authCode;//注销驳回原因
        public List<String> imgs;
        public List<CircleCommListBroadBean> commList;
        public List<CircleCommListBroadBean> likeList;
        public List<String> memberInterestList;
        public List<CircleCommListBroadBean> userList;
        public String city;
        public String profession;
        public String mId;
        public String isLiang;
        public String lianghao;
        public String prettyNumber;

        // 只需要比较部分字段
        public boolean partialEquals(ResultBroadBean.Data other) {
            if (this == other) return true;
            if (other == null) return false;

            return Objects.equals(this.name, other.name)          // String 可 null
                    && Objects.equals(this.sex, other.sex)           // Integer 可 null
                    && Objects.equals(this.icon, other.icon)           // Integer 可 null
                    && Objects.equals(this.city, other.city)           // Integer 可 null
                    && Objects.equals(this.profession, other.profession)           // Integer 可 null
                    && Objects.equals(this.sign, other.sign)           // Integer 可 null
                    && Objects.equals(this.memberInterestList, other.memberInterestList);      // List<String> 可 null
            // && Objects.equals(this.address, other.address) ... 继续加字段
        }
    }
}
