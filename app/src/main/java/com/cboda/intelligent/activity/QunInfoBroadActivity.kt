package com.cboda.intelligent.activity

import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.SwitchCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.chatui.WfcUIBroadKit
import com.cboda.chatui.conversation.ConversationBroadActivity
import com.cboda.chatui.conversation.ConversationViewBroadModel
import com.cboda.chatui.utils.MyIMActivityBroadUtils
import cn.wildfirechat.model.Conversation
import cn.wildfirechat.model.ModifyGroupInfoType
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.GeneralCallback
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.select.picture.entity.LocalMedia
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.QunUserBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.CompressionBroadUtil.imgCompression
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.holder.BottomDialogBroadHolder
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.holder.ImagePickerBroadHolder
import com.cboda.intelligent.utils.*
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.viewmodel.UploadFileViewBroadModel
import com.king.zxing.util.CodeUtils
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File


@Deprecated("废弃")
class QunInfoBroadActivity : BaseSimpleBroadActivity() {

    private var qunId = ""
    private var viewModel: CommViewBroadModel? = null
    private val contentList = arrayListOf<String>()
    private var mAdapter: QunUserBroadAdapter? = null
    private var mDataList = arrayListOf<CommonDataListBroadBean.CommonBean>()
    private var pageNo = 1
    private var uploadViewModel: UploadFileViewBroadModel? = null
    private var qunxiangqing: CommonResultBroadBean.CommonResult? = null
    private var jiangetime = 0
    private var conversationViewModel: ConversationViewBroadModel? = null
    private lateinit var tuichuquan: TextView
    private lateinit var jiesanqun: TextView
    private lateinit var qunguanlilay: LinearLayoutCompat
    private lateinit var kejianguanliView: View
    private lateinit var kejianguanli: SwitchCompat
    private lateinit var jiahao: AppCompatImageView
    private lateinit var tvjiangeshi: EditText
    private lateinit var jianhao: AppCompatImageView
    private lateinit var wufajiahaoyouView: View
    private lateinit var wufajiahaoyou: SwitchCompat
    private lateinit var ruqunyanzView: View
    private lateinit var ruqunyanz: SwitchCompat
    private lateinit var qunzhumingpianView: View
    private lateinit var qunzhumingpian: SwitchCompat
    private lateinit var kefahongboaView: View
    private lateinit var kefahongboa: SwitchCompat
    private lateinit var jinqunjiaoyanView: View
    private lateinit var jinqunjiaoyan: SwitchCompat
    private lateinit var yaoqinghaioyuView: View
    private lateinit var yaoqinghaioyu: SwitchCompat
    private lateinit var zhuanrangxian: View
    private lateinit var huanyingchengyuan: LinearLayoutCompat
    private lateinit var jiqiren: LinearLayoutCompat
    private lateinit var shengjiqun: LinearLayoutCompat
    private lateinit var zhuanrangqun: LinearLayoutCompat
    private lateinit var qunjinmyan: LinearLayoutCompat
    private lateinit var guanlixian: View
    private lateinit var qunguanliyuan: LinearLayoutCompat
    private lateinit var wodequnnicheng: LinearLayoutCompat
    private lateinit var wodequnbnicjheng: TextView
    private lateinit var qingkong: LinearLayoutCompat
    private lateinit var qungonggao: LinearLayoutCompat
    private lateinit var zidongfanyiView: View
    private lateinit var zidongfanyi: SwitchCompat
    private lateinit var miaodaraoView: View
    private lateinit var miaodarao: SwitchCompat
    private lateinit var zhidingView: View
    private lateinit var zhiding: SwitchCompat
    private lateinit var qunidlay: LinearLayoutCompat
    private lateinit var qunma: ImageView
    private lateinit var qunid: TextView
    private lateinit var shezhiqunnicheng: LinearLayoutCompat
    private lateinit var qunmingceng: TextView
    private lateinit var shezhiquntou: LinearLayoutCompat
    private lateinit var quntouxiang: ImageView
    private lateinit var chakangebngduo: TextView
    private lateinit var recyclerView:
            RecyclerView

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerView)
        chakangebngduo = findViewById(R.id.chakangebngduo)
        quntouxiang = findViewById(R.id.quntouxiang)
        shezhiquntou = findViewById(R.id.shezhiquntou)
        qunmingceng = findViewById(R.id.qunmingceng)
        shezhiqunnicheng = findViewById(R.id.shezhiqunnicheng)
        qunid = findViewById(R.id.qunid)
        qunma = findViewById(R.id.qunma)
        qunidlay = findViewById(R.id.qunidlay)
        zhiding = findViewById(R.id.zhiding)
        zhidingView = findViewById(R.id.zhidingView)
        miaodarao = findViewById(R.id.miaodarao)
        miaodaraoView = findViewById(R.id.miaodaraoView)
        zidongfanyi = findViewById(R.id.zidongfanyi)
        zidongfanyiView = findViewById(R.id.zidongfanyiView)
        qungonggao = findViewById(R.id.qungonggao)
        qingkong = findViewById(R.id.qingkong)
        wodequnbnicjheng = findViewById(R.id.wodequnbnicjheng)
        wodequnnicheng = findViewById(R.id.wodequnnicheng)
        qunguanliyuan = findViewById(R.id.qunguanliyuan)
        guanlixian = findViewById(R.id.guanlixian)
        qunjinmyan = findViewById(R.id.qunjinmyan)
        zhuanrangqun = findViewById(R.id.zhuanrangqun)
        shengjiqun = findViewById(R.id.shengjiqun)
        jiqiren = findViewById(R.id.jiqiren)
        huanyingchengyuan = findViewById(R.id.huanyingchengyuan)
        zhuanrangxian = findViewById(R.id.zhuanrangxian)
        yaoqinghaioyu = findViewById(R.id.yaoqinghaioyu)
        yaoqinghaioyuView = findViewById(R.id.yaoqinghaioyuView)
        jinqunjiaoyan = findViewById(R.id.jinqunjiaoyan)
        jinqunjiaoyanView = findViewById(R.id.jinqunjiaoyanView)
        kefahongboa = findViewById(R.id.kefahongboa)
        kefahongboaView = findViewById(R.id.kefahongboaView)
        qunzhumingpian = findViewById(R.id.qunzhumingpian)
        qunzhumingpianView = findViewById(R.id.qunzhumingpianView)
        ruqunyanz = findViewById(R.id.ruqunyanz)
        ruqunyanzView = findViewById(R.id.ruqunyanzView)
        wufajiahaoyou = findViewById(R.id.wufajiahaoyou)
        wufajiahaoyouView = findViewById(R.id.wufajiahaoyouView)
        jianhao = findViewById(R.id.jianhao)
        tvjiangeshi = findViewById(R.id.tvjiangeshi)
        jiahao = findViewById(R.id.jiahao)
        kejianguanli = findViewById(R.id.kejianguanli)
        kejianguanliView = findViewById(R.id.kejianguanliView)
        qunguanlilay = findViewById(R.id.qunguanlilay)
        jiesanqun = findViewById(R.id.jiesanqun)
        tuichuquan = findViewById(R.id.tuichuquan)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_qun_broad_info
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initViews()
        findViewById<View>(R.id.shengjiqunView).isGone = HttpsBroadConfig.isReviewEnv
        shengjiqun.isGone = HttpsBroadConfig.isReviewEnv
        qunId = intent?.getStringExtra("qunid") ?: ""
        getRightImg()?.setImageResource(R.mipmap.ic_gengduo_broad_white)
        getRightImg()?.setOnClickListener {
            contentList.clear()
            contentList.add(getString(R.string.tousuqun))
            val holder = BottomDialogBroadHolder(this, contentList)
            holder.callBack = object : BottomDialogBroadHolder.BottomDialogClickCallBack {
                override fun clickItem(position: Int) {
                    JumpBroadUtils.startQunTouSuActivity(this@QunInfoBroadActivity, qunId, "")
                }
            }
            holder.show()
        }
        conversationViewModel = WfcUIBroadKit.getAppScopeViewModel(
            ConversationViewBroadModel::class.java
        )
        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.errorLiveData?.observe(this) {
            when (it.type) {
                "zhidingsuc" -> {
                    zhiding?.isChecked = !zhiding!!.isChecked
                }

                "jiesanqunsuc" -> {
                    MyIMActivityBroadUtils.activities.forEachIndexed { index, activity ->
                        if (activity is ConversationBroadActivity) {
                            activity.finish()
                        }
                    }
                    finish()
                }

                "xiugaiqunxinxisuc" -> {
                    viewModel?.gonggaoxiangqing(this, qunId)
                }

                "zidongfanyisuc" -> {
                    zidongfanyi?.isChecked = !zidongfanyi!!.isChecked
                }
            }
        }
        viewModel?.gonggaoLiveData?.observe(this) {
            it.data?.apply {
                qunxiangqing = this
                MMKVBroadUtils.updateGroupNameAndRenameCount(id, name, renameCount, icon)
                if (!TextUtils.isEmpty(this.tQrcode)) {
                    GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                        this@QunInfoBroadActivity,
                        CodeUtils.createQRCode(this.tQrcode, dp2px(260f)),
                        qunma,
                        PixelBroadBroadUtils.dip2px(this@QunInfoBroadActivity, 10f)
                    )
                } else if (this.qrcode?.isNotEmpty() == true) {
                    GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                        this@QunInfoBroadActivity,
                        this.qrcode ?: "",
                        qunma,
                        PixelBroadBroadUtils.dip2px(this@QunInfoBroadActivity, 10f)
                    )
                }
                if (this.icon?.isNotEmpty() == true) {
                    GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                        this@QunInfoBroadActivity,
                        this.icon ?: "",
                        quntouxiang,
                        PixelBroadBroadUtils.dip2px(this@QunInfoBroadActivity, 5f)
                    )
                }


                qunmingceng?.text = this.name ?: ""
                qunid?.text = this.idno ?: ""
                zhiding?.isChecked = this.isZd == "1"
                wodequnbnicjheng?.text = this.nickname ?: ""

                jiesanqun?.isVisible = this.userType == "1"
                tuichuquan?.isVisible = this.userType != "1"

                qunguanlilay?.isVisible = this.userType == "1" || this.userType == "2"
                qunguanliyuan?.isVisible = this.userType == "1"
                guanlixian?.isVisible = this.userType == "1"
                zhuanrangqun?.isVisible = this.userType == "1"
                zhuanrangxian?.isVisible = this.userType == "1"

                yaoqinghaioyu?.isChecked = this.openQyyq == "1"
                jinqunjiaoyan?.isChecked = this.openQyyz == "1"
                kefahongboa?.isChecked = this.openQzhb == "1"
                qunzhumingpian?.isChecked = this.openQzts == "1"
                ruqunyanz?.isChecked = this.openQygl == "1"
                wufajiahaoyou?.isChecked = this.openQysl == "1"
                kejianguanli?.isChecked = this.openQykj == "1"
                zidongfanyi?.isChecked = this.isfanyi == "1"


                jiangetime = if (this.jiange.isNullOrEmpty()) {
                    1
                } else {
                    this.jiange!!.toInt()
                }
                tvjiangeshi?.setText(jiangetime.toString())

                mDataList.clear()
                when (this.userType) {
                    "1" -> {
                        //群主
                        val bean = CommonDataListBroadBean.CommonBean()
                        bean.type = "add"
                        mDataList.add(bean)

                        val bean1 = CommonDataListBroadBean.CommonBean()
                        bean1.type = "remove"
                        mDataList.add(bean1)

                        this.userList?.apply {
                            mDataList.addAll(this)
                        }
                    }

                    "2" -> {
                        //管理
                        val bean = CommonDataListBroadBean.CommonBean()
                        bean.type = "add"
                        mDataList.add(bean)

                        val bean1 = CommonDataListBroadBean.CommonBean()
                        bean1.type = "remove"
                        mDataList.add(bean1)

                        this.userList?.apply {
                            mDataList.addAll(this)
                        }
                    }

                    else -> {
                        if (this.openQyyq == "1") {
                            val bean = CommonDataListBroadBean.CommonBean()
                            bean.type = "add"
                            mDataList.add(bean)
                        }

                        if (this.openQykj == "1") {
                            this.userList?.forEachIndexed { index, commonBean ->
                                if (commonBean.type == "1" || commonBean.type == "2") {
                                    mDataList.add(commonBean)
                                }
                            }
                        } else {
                            this.userList?.apply {
                                mDataList.addAll(this)
                            }
                        }

                    }
                }


                mAdapter?.setList(mDataList)
            }

        }

        mAdapter = QunUserBroadAdapter(R.layout.item_qun_user_broad_layout)
        recyclerView?.apply {
            layoutManager = GridLayoutManager(
                this@QunInfoBroadActivity,
                4
            )
            adapter = mAdapter
        }
        mAdapter?.setList(mDataList)
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            when (mDataList[position].type) {
                "add" -> {
                    JumpBroadUtils.startQunMemberListActivity(
                        this,
                        qunId,
                        "add",
                        qunxiangqing?.userType ?: ""
                    )
                }

                "remove" -> {
                    JumpBroadUtils.startQunMemberListActivity(
                        this,
                        qunId,
                        "remove",
                        qunxiangqing?.userType ?: ""
                    )
                }

                else -> {
                    JumpBroadUtils.startQunGerenPageActivity(
                        this,
                        qunId,
                        mDataList[position].userId ?: "",
                        "1",
                        mDataList[position].type ?: "",
                    )
                }
            }
        }

        chakangebngduo?.setOnClickListener {
            JumpBroadUtils.startQunUserListActivity(this, qunId)
        }

        jianhao?.setOnClickListener {
            jiangetime--
            if (jiangetime <= 0) {
                jiangetime = 0
            }
            tvjiangeshi?.setText(jiangetime.toString())
            viewModel?.xiugaiqunxinxi(this, qunId, "jiange", jiangetime.toString())
        }

        jiahao?.setOnClickListener {
            jiangetime++
            tvjiangeshi?.setText(jiangetime.toString())
            viewModel?.xiugaiqunxinxi(this, qunId, "jiange", jiangetime.toString())
        }
        tvjiangeshi.setOnEditorActionListener { _, actionId, event ->
            val isDoneAction = actionId == EditorInfo.IME_ACTION_DONE
            val isEnterDown =
                event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN
            if (!isDoneAction && !isEnterDown) {
                return@setOnEditorActionListener false
            }

            val inputVal = tvjiangeshi.text?.toString()?.trim()?.toIntOrNull()
            jiangetime = if (inputVal == null || inputVal <= 0) {
                0
            } else {
                inputVal
            }
            tvjiangeshi.setText(jiangetime.toString())
            tvjiangeshi.setSelection(tvjiangeshi.text?.length ?: 0)
            viewModel?.xiugaiqunxinxi(this, qunId, "jiange", jiangetime.toString())
            tvjiangeshi.clearFocus()
            val imm = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(tvjiangeshi.windowToken, 0)
            true
        }

        yaoqinghaioyuView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(
                this,
                qunId,
                "openQyyq",
                if (yaoqinghaioyu?.isChecked == true) "0" else "1"
            )
        }

        jinqunjiaoyanView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(
                this,
                qunId,
                "openQyyz",
                if (jinqunjiaoyan?.isChecked == true) "0" else "1"
            )
        }

        kefahongboaView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(
                this,
                qunId,
                "openQzhb",
                if (kefahongboa?.isChecked == true) "0" else "1"
            )
        }

        qunzhumingpianView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(
                this,
                qunId,
                "openQzts",
                if (qunzhumingpian?.isChecked == true) "0" else "1"
            )
        }

        ruqunyanzView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(
                this,
                qunId,
                "openQygl",
                if (ruqunyanz?.isChecked == true) "0" else "1"
            )
        }

        wufajiahaoyouView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(
                this,
                qunId,
                "openQysl",
                if (wufajiahaoyou?.isChecked == true) "0" else "1"
            )
        }

        kejianguanliView?.setOnClickListener {
            viewModel?.xiugaiqunxinxi(
                this,
                qunId,
                "openQykj",
                if (kejianguanli?.isChecked == true) "0" else "1"
            )
        }
        zidongfanyiView?.setOnClickListener {
            viewModel?.setGroupFanyi(this, qunId, if (zidongfanyi?.isChecked == true) "0" else "1")
        }


        shezhiquntou?.setOnClickListener {
            if (qunxiangqing?.userType == "1" || qunxiangqing?.userType == "2") {
                val holder = ImagePickerBroadHolder(this)
                holder.callBack = object : ImagePickerBroadHolder.OnResultCallback {
                    override fun onResult(result: ArrayList<LocalMedia>?) {
                        val resultList: MutableList<LocalMedia>? = result
                        if (resultList?.isNotEmpty() == true) {
                            val card1File = File(resultList[0].realPath)
                            GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                                this@QunInfoBroadActivity,
                                "file://${resultList[0].realPath}",
                                quntouxiang,
                                PixelBroadBroadUtils.dip2px(this@QunInfoBroadActivity, 5f)
                            )
                            imgCompression(this@QunInfoBroadActivity, card1File) { _, compressFile ->
                                compressFile?.apply {
                                    if (uploadViewModel == null) {
                                        uploadViewModel =
                                            ViewModelProvider(this@QunInfoBroadActivity)[UploadFileViewBroadModel::class.java]
                                    }
                                    MainScope().launch {
                                        val urlBeans =
                                            uploadViewModel?.uploadSyn(compressFile)
                                        if (urlBeans?.url.isNullOrEmpty()) {
                                            dismissLoading()
                                            AppBroadUtils.showToast(
                                                this@QunInfoBroadActivity,
                                                urlBeans?.erroToaastInfo ?: ""
                                            )
                                            return@launch
                                        }
                                        AppBroadUtils.debug("长传结果-->", "${urlBeans?.url}")

                                        viewModel?.shangchuanquntouxiang(
                                            this@QunInfoBroadActivity,
                                            qunId,
                                            urlBeans?.url ?: ""
                                        )

                                        ChatManager.Instance().modifyGroupInfo(
                                            qunId,
                                            ModifyGroupInfoType.Modify_Group_Portrait,
                                            urlBeans?.url ?: "",
                                            arrayListOf<Int>(),
                                            null,
                                            object : GeneralCallback {
                                                override fun onSuccess() {
                                                    AppBroadUtils.debug(
                                                        "野火",
                                                        "修改群头像onSuccess ---> "
                                                    )
                                                }

                                                override fun onFail(errorCode: Int) {
                                                    AppBroadUtils.debug(
                                                        "野火",
                                                        "修改群头像onFail ---> $errorCode"
                                                    )
                                                }

                                            }
                                        )
                                    }
                                }
                            }


                        }
                    }

                    override fun onCancel() {
                    }
                }
                holder.show()
            } else {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanli))
            }
        }

        shezhiqunnicheng?.setOnClickListener {
            if (qunxiangqing?.userType == "1" || qunxiangqing?.userType == "2") {
                JumpBroadUtils.startQunNameActivity(this, qunId, qunmingceng?.text?.toString() ?: "")
            } else {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanlimingcheng))
            }
        }

        qungonggao?.setOnClickListener {
            if (qunxiangqing?.userType == "1" || qunxiangqing?.userType == "2") {
                JumpBroadUtils.startQunGongGaoActivity(this, qunId, qunxiangqing?.gonggao ?: "")
            } else {
                ToastBroadUtils.show(getString(R.string.zhiyouqunzhuguanlimgongao))
            }
        }

        zhidingView?.setOnClickListener {
            viewModel?.zhidingqun(this, qunId, if (zhiding?.isChecked == true) "0" else "1")
            val conversation = Conversation()
            conversation.type = Conversation.ConversationType.Group
            conversation.line = 0
            conversation.target = qunId
            ChatManager.Instance()
                .setConversationTop(conversation, if (zhiding?.isChecked == true) 0 else 1)
        }

        jiesanqun?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.wenxintishi),
                getString(R.string.jiesanquan),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        viewModel?.destoryGroup(this@QunInfoBroadActivity, qunId)
                    }
                }
            }
            holder.show()
        }

        tuichuquan?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.wenxintishi),
                getString(R.string.querdingjiesanqunshanchu),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        viewModel?.outGroup(this@QunInfoBroadActivity, qunId)
                    }
                }
            }
            holder.show()
        }

        qingkong?.setOnClickListener {
            val holder = CommanConfimBroadHolder(
                this,
                getString(R.string.qingkongliaotian),
                StringBroadUtils.format(getString(R.string.qingkongquntishi, qunxiangqing?.name ?: "")),
                getString(R.string.queding),
                getString(R.string.quxiao)
            )
            holder.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                override fun onClickConfim(isConfim: Boolean) {
                    if (isConfim) {
                        val conversation = Conversation()
                        conversation.type = Conversation.ConversationType.Group
                        conversation.line = 0
                        conversation.target = qunId
                        val groupConversation = ChatManager.Instance().getConversation(conversation)
//                        conversationViewModel?.clearRemoteConversationMessage(groupConversation!!.conversation)
                        conversationViewModel?.clearConversationMessage(groupConversation!!.conversation)
                        ToastBroadUtils.show(getString(R.string.qincghujikusuc))
                    }
                }
            }
            holder.show()
        }

        wodequnnicheng?.setOnClickListener {
            JumpBroadUtils.startQunNichengActivity(this, qunId, wodequnbnicjheng?.text?.toString() ?: "")
        }

        qunguanliyuan?.setOnClickListener {
            JumpBroadUtils.startQunManagerActivity(this, qunId)
        }

        qunjinmyan?.setOnClickListener {
            JumpBroadUtils.startQunJinYanActivity(this, qunId)
        }



        qunidlay?.setOnClickListener {
            JumpBroadUtils.startQunCodeActivity(this, qunId)
        }

        zhuanrangqun?.setOnClickListener {
            JumpBroadUtils.startZhuanRangQunActivity(this, qunId)
        }

        shengjiqun?.setOnClickListener {
            JumpBroadUtils.startQunShengJiActivity(this, qunId)
        }
        huanyingchengyuan?.setOnClickListener {
            JumpBroadUtils.startHyyActivity(this, qunId)
        }
        jiqiren?.setOnClickListener {
            JumpBroadUtils.startJiQiRenActivity(this, qunId)
        }

        miandarao()
    }

    private fun miandarao() {
        miaodaraoView?.setOnClickListener {
            val conversation = Conversation()
            conversation.type = Conversation.ConversationType.Group
            conversation.line = 0
            conversation.target = qunId
            ChatManager.Instance().setConversationSilent(conversation, miaodarao?.isChecked != true)
            miaodarao?.isChecked = !miaodarao!!.isChecked
        }

        val conversation = Conversation()
        conversation.type = Conversation.ConversationType.Group
        conversation.line = 0
        conversation.target = qunId
        val groupConversation = ChatManager.Instance().getConversation(conversation)
        miaodarao?.isChecked = groupConversation?.isSilent == true
    }


    override fun onResume() {
        super.onResume()
        viewModel?.gonggaoxiangqing(this, qunId)
    }

}