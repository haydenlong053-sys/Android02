package com.cboda.intelligent

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager.widget.ViewPager
import com.cboda.chatui.IMConnectionStatusViewBroadModel
import com.cboda.chatui.IMServiceStatusViewBroadModel
import com.cboda.chatui.bridge.TiXingBroadUtils
import com.cboda.chatui.bridge.MyCommonBroadBridge
import com.cboda.chatui.conversationlist.ConversationListViewBroadModel
import com.cboda.chatui.conversationlist.ConversationListViewModelBroadFactory
import com.cboda.chatui.user.UserViewBroadModel
import cn.wildfirechat.client.ConnectionStatus
import cn.wildfirechat.message.Message
import cn.wildfirechat.message.NoticeInformMessageContent
import cn.wildfirechat.message.UnknownMessageContent
import cn.wildfirechat.model.Conversation.ConversationType
import cn.wildfirechat.model.ModifyMyInfoEntry
import cn.wildfirechat.model.ModifyMyInfoType
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.OnReceiveMessageListener
import cn.wildfirechat.viewmodel.HomeUIViewModel
import com.blankj.utilcode.util.BarUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.SizeUtils.dp2px
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.event.DoubleMessageBroadEvent
import com.cboda.baselibrary.event.UnreadCountBroadEvent
import com.cboda.baselibrary.utils.AppPermissionBroadUtils
import com.cboda.chatemoji.EmotionBroadEvent
import com.cboda.intelligent.adapter.FragmentBroadAdapter
import com.cboda.intelligent.base.BaseSimpleBroadActivity
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.intelligent.compose.ExpandableBottomBar
import com.cboda.intelligent.compose.ExpandableBottomBarBroadItem
import com.cboda.intelligent.fragment.MineBroadFragment
import com.cboda.intelligent.fragment.TongXunLuBroadFragment
import com.cboda.intelligent.fragment.MessageBroadFragment
import com.cboda.intelligent.holder.SingleBtnBroadHolder
import com.cboda.intelligent.holder.XitonggaonggaoBroadHolder
import com.cboda.intelligent.holder.MyImManagerBroadHolder
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.utils.ToastBroadUtil
import com.cboda.intelligent.model.NoticeInfoBroadBean
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.AppBroadUtils.Companion.exit
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.intelligent.fragment.CleverBodyBroadFragment
import com.cboda.intelligent.fragment.NewMirrorBroadFragment
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.widge.CustomClickBroadView
import com.cboda.intelligent.widge.MyViewBroadPager
import com.cboda.intelligent.widge.MyUnreadCountTextBroadView
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.cboda.baselibrary.common.BridgeImEnum

class MainBroadActivity : BaseSimpleBroadActivity() {

    private var needShowAccount = false
    private var selectedIndex by mutableStateOf(0)
    private var isBottomBarExpanded by mutableStateOf(false)
    private var xiaoxiUnreadCount by mutableStateOf("0")
    private var tongxunluUnreadCount by mutableStateOf("0")

    private lateinit var bottomLayout: LinearLayoutCompat
    private lateinit var wodeLayout: ConstraintLayout
    private lateinit var tvwode: TextView
    private lateinit var ivwode: AppCompatImageView
    private lateinit var faxiaoLayout: ConstraintLayout
    private lateinit var tvfaxian: TextView
    private lateinit var ivfaxian: AppCompatImageView
    private lateinit var xiaochengxuLayout: ConstraintLayout
    private lateinit var tvxiaochengxu: TextView
    private lateinit var ivxiaochengxu: AppCompatImageView
    private lateinit var tongxunluLayout: ConstraintLayout
    private lateinit var tvtongxunlu: TextView
    private lateinit var tvTongxunLuNum: MyUnreadCountTextBroadView
    private lateinit var ivtongxunlu: AppCompatImageView
    private lateinit var xiaoxiLayout: ConstraintLayout
    private lateinit var clickView: CustomClickBroadView
    private lateinit var tvXiaoxi: TextView
    private lateinit var tvXiaoxiNum: MyUnreadCountTextBroadView
    private lateinit var ivXiaoxi: AppCompatImageView
    private lateinit var viewPager: MyViewBroadPager

    private val uIViewModel: HomeUIViewModel by viewModels()


    override fun getLayoutId(): Int {
        return R.layout.main_activity
    }

    override fun showOrHideTitleLayout(): Boolean {
        return false
    }

    override fun transparentStatus(): Boolean {
        return true
    }

    companion object {
        var ptConfig: CommonResultBroadBean.CommonResult? = null
        var currtentLoginUser: CommonResultBroadBean.CommonResult? = null
    }

    //    private val fragmentNameList = arrayListOf<String>()
    private var gson = GsonUtils.getGson()
    private var myImManagerHolder: MyImManagerBroadHolder? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        initViews()
        bottomLayout.isVisible = false
        val composeView = findViewById<ComposeView>(R.id.compose_bottom_bar)
        window.addFlags(WindowManager.LayoutParams.FLAG_BLUR_BEHIND)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            window.setBackgroundBlurRadius(20)
        }


        composeView.setContent {
            MaterialTheme {
                ExpandableBottomBar(
                    selectedIndex = selectedIndex,
                    onItemSelected = { index ->
                        viewPager.setCurrentItem(index, false)
                    },
                    expanded = isBottomBarExpanded,
                    onExpandedChange = { isExpanded ->
                        isBottomBarExpanded = isExpanded
                        findViewById<View>(R.id.touch_intercept_overlay).visibility =
                            if (isExpanded) View.VISIBLE else View.GONE
                    },
                    items = listOf(
                        ExpandableBottomBarBroadItem(
                            icon = painterResource(id = R.drawable.ic_home_tab_broad_mirror),
                            selectedIcon = painterResource(id = R.drawable.ic_home_tab_mirror_broad_pre),
                            text = stringResource(id = R.string.bottom_bar_mirror)
                        ),
                        ExpandableBottomBarBroadItem(
                            icon = painterResource(id = R.drawable.ic_ai_tab_broad_mirror),
                            selectedIcon = painterResource(id = R.drawable.ic_ai_tab_mirror_broad_pre),
                            text = stringResource(id = R.string.bottom_bar_intelligent)
                        ),
                        ExpandableBottomBarBroadItem(
                            icon = painterResource(id = R.drawable.ic_home_tab_broad_message),
                            selectedIcon = painterResource(id = R.drawable.ic_home_tab_message_broad_pre),
                            text = stringResource(id = R.string.bottom_bar_social),
                            badge = if (xiaoxiUnreadCount == "0" && tongxunluUnreadCount == "0") "" else ((xiaoxiUnreadCount.toInt()) + (tongxunluUnreadCount.toInt())).toString()
                        ),
//                        ExpandableBottomBarBroadItem(
//                            icon = painterResource(id = R.drawable.ic_home_tab_contacts),
//                            selectedIcon = painterResource(id = R.drawable.ic_home_tab_contacts_pre),
//                            text = "通讯录",
//                            badge = if (tongxunluUnreadCount == "0") "" else tongxunluUnreadCount
//                        ),
                        ExpandableBottomBarBroadItem(
                            icon = painterResource(id = R.drawable.ic_home_tab_broad_mine),
                            selectedIcon = painterResource(id = R.drawable.ic_home_tab_mine_broad_pre),
                            text = stringResource(id = R.string.bottom_bar_center)
                        )
                    )
                )
            }
        }

        findViewById<View>(R.id.touch_intercept_overlay).setOnClickListener {
            it.visibility = View.GONE
            isBottomBarExpanded = false
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
                selectedIndex = position
            }

            override fun onPageScrollStateChanged(state: Int) {
            }
        })


        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                uIViewModel.uiState.collect { state ->
                    composeView.visibility = if (state.showBottomUI) View.GONE else View.VISIBLE
                }

            }
        }


        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
        viewModel?.pintaiLiveData?.observe(this) {
            it.data?.apply {
                ptConfig = it.data
                SharePrefBroadBroadUtil.saveString(this@MainBroadActivity, "pingtaipeizhi", gson.toJson(it.data))
                SharePrefBroadBroadUtil.saveString(this@MainBroadActivity, "pingtaimgc", ptConfig!!.mgc)
            }
        }
        viewModel?.xitongaongaoListLiveData?.observe(this) {
            it.dataList?.apply {
                if (this.isNotEmpty()) {
                    val holder = XitonggaonggaoBroadHolder(this@MainBroadActivity, this)
                    holder.callBack = object : XitonggaonggaoBroadHolder.OnConfimCallBack {
                        override fun onClickConfim(isConfim: Boolean) {
                        }

                        override fun yidu(id: String) {
                            viewModel?.yidugonggao(this@MainBroadActivity, id)
                        }

                        override fun shanchu(id: String) {
                            viewModel?.shanchugonggao(this@MainBroadActivity, id)
                        }

                    }
                    holder.show()
                }
            }
        }
        viewModel?.userLiveData?.observe(this) {
//            if (needShowAccount) {
//                intent = Intent(this@MainBroadActivity, ZhaqBroadActivity::class.java)
//                startActivity(intent)
//                needShowAccount = false
//            }
            it.data?.apply {
                currtentLoginUser = this
                MMKVBroadUtils.saveKeFuId(it.data?.kfid!!)
                MMKVBroadUtils.saveUserName(it.data?.name!!)
                SharePrefBroadBroadUtil.saveString(this@MainBroadActivity, "autofanyi", it.data?.autofanyi!!)
                SharePrefBroadBroadUtil.saveString(this@MainBroadActivity, "isvip", it.data?.isvip!!)
                viewModel?.getKfInfo(this@MainBroadActivity, it.data?.kfid!!)

                val zongshu = this.fris + this.gros
                tongxunluUnreadCount = zongshu.toString()
//                ChatManager.Instance().haoyoushenqing = this.fris
                ChatManager.Instance().qunshenqing = this.gros
                eLogD("看看未读", "main$tongxunluUnreadCount")
                EventBus.getDefault().post(
                    UnreadCountBroadEvent(
                        null,
                        null,
                        null,
//                    (it?.unreadMention!! + it?.unreadMentionAll!!).toString()
                        null,
                        null,
                        null,
                        tongxunluUnreadCount
                    )
                )
//                EventBus.getDefault().post(AppBroadEvent(AppEventBroadCode.EVENT_REFRESH_CONTACT,true))


                val entry =
                    ModifyMyInfoEntry(ModifyMyInfoType.Modify_DisplayName, it.data?.name ?: "")
                val entry1 =
                    ModifyMyInfoEntry(ModifyMyInfoType.Modify_Portrait, it.data?.icon ?: "")
                userViewModel?.modifyMyInfo(listOf(entry, entry1))
                    ?.observe(this@MainBroadActivity) { it1 ->
                        AppBroadUtils.debug("野火", "修改用户信息---> ${it1.isSuccess}")
                    }

            }
        }

        viewModel?.keFuData?.observe(this) {
            it.data?.apply {
                this.name?.let { it1 -> MMKVBroadUtils.saveKeFuName(it1) }
            }
        }

        // The old click listeners are now replaced by the ExpandableBottomBar logic
        clickView.setOnClickAndDoubleClickListener(object :
            CustomClickBroadView.OnClickAndDoubleClickListener {
            override fun onSingleClick() {
                // single click is now handled by the bottom bar
            }

            override fun onDoubleClick() {
                EventBus.getDefault().post(DoubleMessageBroadEvent())
            }
        })

//        val imManagerHolder = ImManagerHolder(this)
//        imManagerHolder.tvXiaoxiNum = tvXiaoxiNum

        myImManagerHolder = MyImManagerBroadHolder(this)


        afterViews()

//        val perms = arrayOf(
//            Permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
//        )
//        if (XXPermissions.isGranted(this, *perms)) {
//        } else {
//            NormalBroadDialog(
//                this,
//                getString(R.string.温馨提示),
//                "为了保证用户体验和功能的正常运行，需要设置允许手机进入高耗电模式，是否请求权限？",
//                "取消",
//                "确定",
//                true
//            )
//                .setOnButtonClickListener(object : OnButtonClick {
//                    override fun OnRightClick() {
//                        val permission: XXPermissions =
//                            XXPermissions.with(this@MainBroadActivity).permission(*perms)
//                        permission.request(object : OnPermissionCallback {
//                            override fun onGranted(permissions: List<String>, allGranted: Boolean) {
//                            }
//
//                            override fun onDenied(
//                                permissions: List<String>,
//                                doNotAskAgain: Boolean
//                            ) {
//                                super.onDenied(permissions, doNotAskAgain)
//                                ToastBroadUtil.show(getString(R.string.权限被拒绝))
//                            }
//                        })
//                    }
//
//                    override fun OnLeftClick() {}
//                }).show()
//        }

        //外包的获取系统公告代码已废弃
//        getsitonggonggao()

//        viewModel?.versionUpdate(this)
        val hasNotificationPermission = AppPermissionBroadUtils.hasNotificationPermission(this)
        if (!hasNotificationPermission) {
            AppPermissionBroadUtils.requestNotificationPermission(this, 1001)

        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
//                != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(this,
//                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
//                    1001)
//            }
//        }
        setNavBarHeightListener {
            val bottomView = findViewById<View>(R.id.bottomView)
            val layoutParams = bottomView.layoutParams
            layoutParams.height = dp2px(12f) + it
            bottomView.layoutParams = layoutParams
            (messageFragment as MessageBroadFragment).setBottomHeight(it)
        }
    }


    private fun getsitonggonggao() {
        viewModel?.getxitonggaonggao(this, "1")
    }

    private fun clickItem(tag: String) {
//        val i = fragmentNameList.indexOf(tag)
//        if (i == -1 || i == viewPager.currentItem) return
//        viewPager.setCurrentItem(i, false)
    }

    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
    @SuppressLint("GestureBackNavigation", "MissingSuperCall")
    override fun onBackPressed() {
//        super.onBackPressed()
        moveTaskToBack(false)
    }

    private var viewModel: CommViewBroadModel? = null

    private var conversationListViewModel: ConversationListViewBroadModel? = null

    override fun onResume() {
        super.onResume()
        viewModel?.getconfig(this)
        viewModel?.getUserInfo(this, "")

        conversationListViewModel?.reloadConversationUnreadStatus()

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: AppBroadEvent<*>?) {
        event?.let {
            when (event.eventCode) {
                AppEventBroadCode.EVENT_LOGIN_SUCCESS -> {
                    needShowAccount = true
                }
            }
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMsgReceive(event: EmotionBroadEvent) {
        if (event.type == "addEmotion") {
            myImManagerHolder?.addCustomFace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private var isInitialized = false
    private var playHolder: SingleBtnBroadHolder? = null
    private fun afterViews() {
        eLogD("MainActivityClass", "野火开始初始化--->")
        val imServiceStatusViewModel = ViewModelProvider(this)[IMServiceStatusViewBroadModel::class.java]
        imServiceStatusViewModel.imServiceStatusLiveData().observe(this) {
            if (it && !isInitialized) {
                eLogD("MainActivityClass", "野火初始化成功--->")
                initMyIm()
                isInitialized = true
            }
//            else
//                ToastBroadUtil.show("初始化$it")
        }

        val connectionStatusViewModel =
            ViewModelProvider(this)[IMConnectionStatusViewBroadModel::class.java]
        connectionStatusViewModel.connectionStatusLiveData().observe(this) {
            AppBroadUtils.debug("野火", "连接状态--> $it")
            when (it) {
                ConnectionStatus.ConnectionStatusTokenIncorrect,
                ConnectionStatus.ConnectionStatusSecretKeyMismatch,
                ConnectionStatus.ConnectionStatusRejected,
                ConnectionStatus.ConnectionStatusLogout,
                ConnectionStatus.ConnectionStatusKickedoff -> {
                    exit(this@MainBroadActivity)
                    ToastBroadUtil.show(getString(R.string.dengluguoqi))
//                    if (playHolder == null) {
//                        playHolder = SingleBtnBroadHolder(
//                            AppBroadUtil.activityList[AppBroadUtil.activityList.size - 1],
//                            getString(R.string.wenxintishi),
//                            getString(R.string.dengluguoqi),
//                            comfimName = getString(R.string.queding),
//                            isCancle = false
//                        )
//                        playHolder?.callBack = object : SingleBtnBroadHolder.OnConfimCallBack {
//                            override fun onClickConfim(isConfim: Boolean) {
//
//                            }
//                        }
//
//                    }
//                    playHolder?.show()
                }

                ConnectionStatus.ConnectionStatusNotLicensed -> {
                    AppBroadUtils.debug("野火", " 专业版IM服务没有授权或者授权过期！！！-->")
                }

                ConnectionStatus.ConnectionStatusTimeInconsistent -> {
                    AppBroadUtils.debug("野火", " 服务器和客户端时间相差太大！！！-->")
                }
            }

        }

    }

    private var userViewModel: UserViewBroadModel? = null
    private val fragmentList = arrayListOf<Fragment>()

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        ivXiaoxi = findViewById(R.id.ivXiaoxi)
        tvXiaoxiNum = findViewById(R.id.tvXiaoxiNum)
        tvXiaoxi = findViewById(R.id.tvXiaoxi)
        clickView = findViewById(R.id.clickView)
        xiaoxiLayout = findViewById(R.id.xiaoxiLayout)
        ivtongxunlu = findViewById(R.id.ivtongxunlu)
        tvTongxunLuNum = findViewById(R.id.tvTongxunLuNum)
        tvtongxunlu = findViewById(R.id.tvtongxunlu)
        tongxunluLayout = findViewById(R.id.tongxunluLayout)
        ivxiaochengxu = findViewById(R.id.ivxiaochengxu)
        tvxiaochengxu = findViewById(R.id.tvxiaochengxu)
        xiaochengxuLayout = findViewById(R.id.xiaochengxuLayout)
        ivfaxian = findViewById(R.id.ivfaxian)
        tvfaxian = findViewById(R.id.tvfaxian)
        faxiaoLayout = findViewById(R.id.faxiaoLayout)
        ivwode = findViewById(R.id.ivwode)
        tvwode = findViewById(R.id.tvwode)
        wodeLayout = findViewById(R.id.wodeLayout)
        bottomLayout = findViewById(R.id.bottomLayout)
    }


    private val messageFragment = MessageBroadFragment.getInstance()
    private fun initMyIm() {

//        fragmentList.add(MirrorBroadFragment.getInstance())

        fragmentList.add(NewMirrorBroadFragment())
        fragmentList.add(CleverBodyBroadFragment())
//        fragmentNameList.add("faxian")
        fragmentList.add(messageFragment)
//        fragmentList.add(TongXunLuBroadFragment.getInstance())
//        fragmentNameList.add("xiaoxi")
//        fragmentNameList.add("tongxunlu")
//        if (SharePrefBroadBroadUtil.getString(this, AppBroadConsts.showXcx, "") == "1") {
//            fragmentList.add(XiaoChengXuBroadFragment.getInstance())
//            fragmentNameList.add("xiaochengxu")
//            xiaochengxuLayout?.isVisible = true
//        } else {
//            xiaochengxuLayout?.isVisible = false
//        }
//        if (SharePrefBroadBroadUtil.getString(this, AppBroadConsts.showFx, "") == "1") {

        faxiaoLayout?.isVisible = true
//        } else {
//            faxiaoLayout?.isVisible = false
//        }
        fragmentList.add(MineBroadFragment())
//        fragmentNameList.add("wode")


        ChatManager.Instance().addOnReceiveMessageListener(object : OnReceiveMessageListener {
            override fun onReceiveMessage(messages: MutableList<Message>?, hasMore: Boolean) {
                try {
                    messages?.forEachIndexed { index, message ->
                         eLogD("zhanglong","MessageContent==="+message.content+"--messageContentType=="+message.content.messageContentType)
                        if (message.sender == "admin" && message.content is NoticeInformMessageContent) {
                            val bean = message.content as NoticeInformMessageContent
//                            Log.d("zhanglong","NoticeInformMessageContent==$bean==${bean.isRead}---${bean.isForcePopup}---${bean.isStatus}")
                            EventBus.getDefault().post(
                                NoticeInfoBroadBean(
                                    id = bean.id,
                                    contentUrl = bean.contentUrl,
                                    forcePopup = bean.isForcePopup,
                                    imageUrl = bean.imageUrl,
                                    previewContent = bean.previewContent,
                                    noticeTitle = bean.noticeTitle,
                                    status = bean.isStatus
                                )
                            )
                        }
                        if (message.content is UnknownMessageContent) {
                            val unkown = message.content as UnknownMessageContent
                            AppBroadUtils.debug(
                                "野火",
                                "收到自定义通知消息---> ${unkown.orignalPayload.type}---> ${unkown.orignalPayload.content}"
                            )
                            AppBroadUtils.debug(
                                "野火",
                                "收到自定义通知消息---> ${System.currentTimeMillis()}---> ${message.serverTime}---> ${ChatManager.Instance().serverDeltaTime}---> ${(System.currentTimeMillis() - message.serverTime - ChatManager.Instance().serverDeltaTime)}"
                            )
                            if ((System.currentTimeMillis() - message.serverTime - ChatManager.Instance().serverDeltaTime) / 1000 > 120) {
                                return@forEachIndexed
                            }

//                            if (message.conversation.target == ChatManager.Instance().userId) {
                            when (unkown.orignalPayload.type) {
                                1002 -> {
                                    val zidingyi = unkown.orignalPayload.content
                                    val split = zidingyi.split("|")
                                    val xinepngy = split[0].toLong()
                                    val qunshu = split[1].toLong()
                                    val zongshu = xinepngy + qunshu
                                    AppBroadUtils.debug("野火", "收到自定义通知消息---> $xinepngy")
//                                    if (zongshu > 99) {
//                                        tongxunluUnreadCount = "99+"
//                                    } else {
//                                        tongxunluUnreadCount = zongshu.toString()
//                                    }
                                    ChatManager.Instance().qunshenqing = qunshu
                                    tongxunluUnreadCount = zongshu.toString()
                                    fragmentList.forEachIndexed { index, fragment ->
                                        if (fragment is TongXunLuBroadFragment) {
                                            fragment.updateShuLiang(xinepngy, qunshu)
                                        }
                                    }
                                    EventBus.getDefault().post(
                                        UnreadCountBroadEvent(
                                            null,
                                            null,
                                            null,
//                    (it?.unreadMention!! + it?.unreadMentionAll!!).toString()
                                            null,
                                            null,
                                            null,
                                            tongxunluUnreadCount
                                        )
                                    )
                                    EventBus.getDefault()
                                        .post(AppBroadEvent(AppEventBroadCode.EVENT_REFRESH_CONTACT, true))
                                    //非自己名义发送的消息才响铃
                                    if (message.sender != MMKVBroadUtils.getRealUserId()) {
                                        TiXingBroadUtils.tixing(this@MainBroadActivity)

                                    }
                                }

                                1003 -> {
                                    fragmentList.forEachIndexed { index, fragment ->
                                        if (fragment is TongXunLuBroadFragment) {
                                            fragment.reflushTongxunlu()
                                        }
                                    }
                                    TiXingBroadUtils.tixing(this@MainBroadActivity)
                                }

                                1005 -> {
                                    if (unkown.orignalPayload.content.equals(MMKVBroadUtils.getRealUserId())) {
                                        MyCommonBroadBridge.getInstance().setMyBridgeMethod(
                                            this@MainBroadActivity, BridgeImEnum.BANNED.value,
                                            "",
                                            "",
                                            null
                                        )
                                    }
                                }
//                                }
                            }


                        }
                    }
                } catch (e: Exception) {
                    AppBroadUtils.debug("野火", "收到自定义通知消息异常---> $e")
                }
            }
        })

        viewPager?.apply {
            adapter = FragmentBroadAdapter(fragmentList, supportFragmentManager)
            setScroll(true)
            offscreenPageLimit = fragmentList.size
        }


        userViewModel = ViewModelProvider(this)[UserViewBroadModel::class.java]
        viewModel?.getUserInfo(this, "")
        conversationListViewModel = ViewModelProvider(
            this, ConversationListViewModelBroadFactory(
                listOf(
                    ConversationType.Single,
                    ConversationType.Group,
                    ConversationType.Channel,
                    ConversationType.SecretChat
                ), listOf(0)
            )
        )[ConversationListViewBroadModel::class.java]

        conversationListViewModel?.unreadCountEventLiveData()?.observe(this) {
//            Log.d("zhanglong","unreadCountEventLiveData==="+it.toString())
            EventBus.getDefault().post(
                UnreadCountBroadEvent(
                    null,
                    it.singleCount?.toString(),
                    it.groupCount?.toString(),
//                    (it?.unreadMention!! + it?.unreadMentionAll!!).toString()
                    (it?.unreadMention!!).toString(),
                    it.myGroupCount.toString(),
                    it.otherGroupCount.toString(),
                    null
                )
            )
            val totalUnread = it.singleCount + it.groupCount
            totalUnread.apply {
                xiaoxiUnreadCount = this.toString()
            }
        }
        viewPager.postDelayed({
            conversationListViewModel?.reloadConversationUnreadStatus()
        }, 2000L)
//        conversationListViewModel?.unreadCountLiveData()?.observe(this) {
//            AppBroadUtil.debug("野火", "会话未读数---> ${it?.unread}")
//            it?.unread?.apply {
//                if (this >= 100) {
//                    xiaoxiUnreadCount = this.toString()
//                } else {
//                    xiaoxiUnreadCount = this.toString()
//                }
//            }
//        }
    }


    override fun fitsSystemWindows() = true
}