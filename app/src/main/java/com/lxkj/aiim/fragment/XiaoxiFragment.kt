package com.lxkj.aiim.fragment

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.viewpager2.widget.ViewPager2
import com.cboda.chatui.conversationlist.ConversationGroupFragment
import com.cboda.chatui.conversationlist.ConversationOtherGroupFragment
import com.cboda.chatui.conversationlist.ConversationSingleFragment
import com.cboda.chatui.widget.ProgressFragment
import cn.wildfirechat.viewmodel.HomeUIViewModel
import com.cboda.baselibrary.bean.GlobalNoticeBroadBean
import com.cboda.baselibrary.event.UnreadCountBroadEvent
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.MyPagerAdapter
import com.lxkj.aiim.base.BaseLazyFragment
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.bean.GlobalBroadInfo
import com.lxkj.aiim.compose.BlurButton
import com.lxkj.aiim.compose.BlurFabButton
import com.lxkj.aiim.compose.DynamicFloatTabBar
import com.lxkj.aiim.compose.FloatManagerBar
import com.lxkj.aiim.compose.MorePopUp
import com.lxkj.aiim.dialog.pop.SysNoticePop
import com.lxkj.aiim.holder.CommanConfimHolder
import com.lxkj.aiim.holder.XiaoXiMoreHolder
import com.lxkj.aiim.lj.NoticeDetailActivity
import com.lxkj.aiim.lj.NoticeListActivity
import com.lxkj.aiim.lj.ScanActivity
import com.lxkj.aiim.lj.configuration.CommonConfig
import com.lxkj.aiim.lj.configuration.ScanJumpEnum
import com.lxkj.aiim.lj.utils.ScreenBroadUtil
import com.lxkj.aiim.model.NoticeInfoBean
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.utils.JumpUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.lxkj.aiim.viewmodel.CommViewModel
import com.lxkj.aiim.viewmodel.FriendViewModel
import com.lxkj.aiim.viewmodel.NoticeViewModel
import com.lxkj.aiim.widge.ColorFlipPagerTitleView
import kotlinx.coroutines.launch
import net.lucode.hackware.magicindicator.MagicIndicator
import net.lucode.hackware.magicindicator.buildins.UIUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.CommonNavigator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.CommonNavigatorAdapter
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.indicators.LinePagerIndicator
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeAnchor
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleView
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgeRule
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class XiaoxiFragment : BaseLazyFragment() {
    companion object {
        fun getInstance(): Fragment {
            return XiaoxiFragment()
        }
    }

    private val uiViewModel: HomeUIViewModel by activityViewModels()

    private var allCount = "0"
    private var singleCount = "0"
    private var allGroupCount = "0"
    private var atMeCount = "0"
    private var myGroupCount = "0"
    private var otherGroupCount = "0"
    private var socialCount = "0"
    private var bean: CommonResultBroadBean.CommonResult? = null
    private val fragments = ArrayList<Fragment>()
    var titles: ArrayList<String> = ArrayList()
    private val noticeViewModel: NoticeViewModel by viewModels()
    private val friendViewModel: FriendViewModel by viewModels()
    private var conversationSingleFragment: ConversationSingleFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        var result = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen", "android"
        )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = hometool?.layoutParams
        linearParams?.height = result
        hometool?.layoutParams = linearParams

        EventBus.getDefault().register(this)

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.pintaiLiveData?.observe(viewLifecycleOwner) {
            it.data?.apply {
                bean = this
                ivkefu?.isVisible = this.showKf == "1"
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                uiViewModel.uiState.collect { state ->
                    if (!state.showBottomUI) {
                        managerBar.visibility = View.GONE
                    } else {
                        if (!managerBar.isVisible) {
                            managerBar.visibility = View.VISIBLE
                        }
                    }
                    viewPager.isUserInputEnabled = !managerBar.isVisible


                }
            }
        }

    }



    override fun loadData() {
        initFriendList()
        ivAdd?.setOnClickListener {
            val holder = XiaoXiMoreHolder(context, ivAdd)
            holder.itemListener = object : XiaoXiMoreHolder.OnItemClickListener {
                override fun onItemClick(content: String) {
                    when (content) {
                        "faqiqun" -> {
                            JumpUtils.startCreateQunActivity(context)
                        }

                        "tianjia" -> {
                            JumpUtils.startSearchUserActivity(context)
                        }

                        "saoyisao" -> {
                            val isGranted = XXPermissions.isGranted(activity!!, Permission.CAMERA)
                            if (isGranted) {
                                saoyisao()
                            } else {
                                val holder3 = CommanConfimHolder(
                                    activity,
                                    "温馨提示",
                                    "${AppUtil.getAppName(activity!!)}需要相机权限,是否请求权限?",
                                    "同意",
                                    "拒绝"
                                )
                                holder3.callBack = object : CommanConfimHolder.OnConfimCallBack {
                                    override fun onClickConfim(isConfim: Boolean) {
                                        if (isConfim) {
                                            XXPermissions.with(activity!!)
                                                .permission(Permission.CAMERA).request { _, _ ->
                                                    saoyisao()
                                                }
                                        }
                                    }
                                }
                                holder3.show()

                            }
                        }
                    }
                }
            }
            holder.show()
        }
        ivsousuo?.setOnClickListener {
//            TUICore.startActivity("SearchMainActivity", null)
            JumpUtils.startSearchActivity(context)
        }

        ivsudu?.setOnClickListener {
            JumpUtils.startH5Activity(context, bean?.keUrl ?: "", "")
        }

        ivkefu?.setOnClickListener {
            MMKVBroadUtils.getKeFuId()?.let { it1 -> AppUtil.goChat(context, it1, "") }
        }
//        val fragment = ConversationListFragment()
//        addFragment(fragment)

//        fragments.add(ConversationListFragment())
        conversationSingleFragment = ConversationSingleFragment()
        fragments.add(conversationSingleFragment!!)
        fragments.add(ConversationGroupFragment())
        fragments.add(ConversationOtherGroupFragment())
        fragments.add(SocialFragment())


        initMagicIndicator()
        initNoticeMsg()

    }

    private fun initFriendList() {
        eLogD("zhanglong", "initFriendList-----------")
        lifecycleScope.launch {
            friendViewModel.fetchFriendShipList().observe(viewLifecycleOwner) { mList ->
                eLogD("getGroupListDataClass", "fetchFriendShipList-----------${mList.size}")
                if (mList.isNotEmpty()) {
                    GlobalBroadInfo.myFriendListBean = MMKVBroadUtils.saveUserListData(mList)
                }
            }
            friendViewModel.fetchGroupList().observe(viewLifecycleOwner) { mList ->
                eLogD("getGroupListDataClass", "fetchGroupList-----------${mList.size}")
                if (mList.isNotEmpty()) {
                    GlobalBroadInfo.myGroupListData = MMKVBroadUtils.saveGroupList(mList)
                }
            }
        }

    }

    private fun initNoticeMsg() {
        lifecycleScope.launch {
            noticeViewModel.getNoticeInfoList(1, CommonConfig.pageSize_100)
                .observe(viewLifecycleOwner) { mList ->
                    if (mList.list?.isNotEmpty() == true) {
                        Log.d(
                            "zhanglong",
                            "mList.list!![0].noticeTitle==" + mList.list!![0].noticeTitle
                        )
                        val noticeBean = GlobalNoticeBroadBean(
                            mList.list!![0].id,
                            mList.list!![0].createTime,
                            mList.list!![0].noticeTitle
                        )

                        Log.d(
                            "zhanglong",
                            "mNoticeBean==" + noticeBean.noticeTitle
                        )
                        conversationSingleFragment?.setGlobalNoticeBean(noticeBean)
                        val infoBeans =
                            mList.list?.filter { bean -> !bean.isRead && bean.forcePopup && bean.status }
//                            mList.list?.filter { bean ->  bean.status }
                        showNoticeMsgDialog(infoBeans)
                    }
                }
        }
    }

    private var sysNoticePop: SysNoticePop? = null
    private fun showNoticeMsgDialog(infoBeans: List<NoticeInfoBean>?) {
//        if (infoBeans?.isNotEmpty() == true){
//            Log.d("zhanglong","===${infoBeans[0].isRead}---${infoBeans[0].forcePopup}---${infoBeans[0].status}")
//        }
        if (infoBeans?.isNotEmpty() == true && sysNoticePop?.isShowing != true && !infoBeans[0].isRead && infoBeans[0].forcePopup && infoBeans[0].status) {
            sysNoticePop = SysNoticePop(requireContext(), infoBeans[0])
            sysNoticePop?.showPopupWindow()
            tempNoticeList.clear()
        }
    }

    var commonNavigator: CommonNavigator? = null

    private fun initMagicIndicator() {
        val pagerAdapter: MyPagerAdapter = MyPagerAdapter(this)
        pagerAdapter.setFragments(fragments)
        viewPager.adapter = pagerAdapter
        commonNavigator = CommonNavigator(context)
        commonNavigator?.isAdjustMode = true
        commonNavigator!!.scrollPivotX = 0.65f
//        commonNavigator!!.isAdjustMode = true
        commonNavigator!!.adapter = object : CommonNavigatorAdapter() {
            override fun getCount(): Int {
                return if (titles == null) 0 else titles.size
            }

            override fun getTitleView(context: Context, index: Int): IPagerTitleView {
                val badgePagerTitleView = BadgePagerTitleView(context)

                val simplePagerTitleView: SimplePagerTitleView = ColorFlipPagerTitleView(context)
                simplePagerTitleView.setText(titles.get(index))
                simplePagerTitleView.normalColor = resources.getColor(R.color.color_text0)
                simplePagerTitleView.selectedColor = resources.getColor(R.color.color_indicator)
                simplePagerTitleView.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL))
                simplePagerTitleView.setOnClickListener {
                    // 管理模式下拦截标签点击，不切换页面
                    if (isManagerClick) return@setOnClickListener
                    viewPager.currentItem = index
                }
                badgePagerTitleView.innerPagerTitleView = simplePagerTitleView

                // set badge position
                badgePagerTitleView.xBadgeRule =
                    BadgeRule(BadgeAnchor.CONTENT_RIGHT, 0)
                badgePagerTitleView.yBadgeRule = BadgeRule(BadgeAnchor.CONTENT_TOP, 0)
                // don't cancel badge when tab selected
                badgePagerTitleView.isAutoCancelBadge = false
                // setup badge
                val badgeTextView = LayoutInflater.from(context)
                    .inflate(R.layout.simple_count_badge_layout, null) as TextView
//                when (index) {
//                    0 -> if (count0 != "0") {
//                        badgeTextView.text = count0
//                        badgePagerTitleView.badgeView = badgeTextView
//                    }
//
//                    1 -> if (count1 != "0") {
//                        badgeTextView.text = count1
//                        badgePagerTitleView.badgeView = badgeTextView
//                    }
//
//                    2 -> if (count2 != "0") {
//                        badgeTextView.text = count2
//                        badgePagerTitleView.badgeView = badgeTextView
//                    }
//
//                    3 -> if (count3 != "0") {
//                        badgeTextView.text = count3
//                        badgePagerTitleView.badgeView = badgeTextView
//                    }
//                }
                when (index) {
                    0 -> if (singleCount != "0") {
                        badgeTextView.text = singleCount
                        badgePagerTitleView.badgeView = badgeTextView
                    }

//                    1 -> if (atMeCount != "0") {
//                        badgeTextView.text = atMeCount
//                        badgePagerTitleView.badgeView = badgeTextView
//                    }

                    1 -> if (myGroupCount != "0") {
                        badgeTextView.text = myGroupCount
                        badgePagerTitleView.badgeView = badgeTextView
                    }

                    2 -> if (otherGroupCount != "0") {
                        badgeTextView.text = otherGroupCount
                        badgePagerTitleView.badgeView = badgeTextView
                    }

                    3 -> {
                        Log.e("看看未读", "$socialCount")
                        if (socialCount != "0") {
                            badgeTextView.text = socialCount
                            badgePagerTitleView.badgeView = badgeTextView
                        } else {
                            badgeTextView.text = socialCount
                            badgeTextView.visibility = View.INVISIBLE
                            badgePagerTitleView.badgeView = badgeTextView
                        }
                    }

                }
                simplePagerTitleView.setWidth(ScreenBroadUtil.getScreenWidth(context) / 6);
                return badgePagerTitleView
            }

            override fun getIndicator(context: Context): IPagerIndicator {
                val indicator = LinePagerIndicator(context)
                indicator.mode = LinePagerIndicator.MODE_EXACTLY
                indicator.lineHeight = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.lineWidth = UIUtil.dip2px(context, 16.0).toFloat()
                indicator.roundRadius = UIUtil.dip2px(context, 2.0).toFloat()
                indicator.startInterpolator = AccelerateInterpolator()
                indicator.endInterpolator = DecelerateInterpolator(2.0f)
                indicator.setColors(resources.getColor(R.color.color_indicator))
                return indicator
            }
        }
        magicIndicator.setNavigator(commonNavigator)

        bindTab(viewPager)
        viewPager.offscreenPageLimit = fragments.size
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UnreadCountBroadEvent?) {
//        if (null != event?.count0)
//            count0 = event?.count0.toString()
        if (null != event?.singleCount) {
            event.singleCount.toIntOrNull()?.let {
                singleCount = if (it > 99) {
                    "99+"
                } else {
                    event.singleCount.toString()
                }

            }

        }

        if (null != event?.allGroupCount) {
            event.allGroupCount.toIntOrNull()?.let {
                allGroupCount = if (it > 99) {
                    "99+"
                } else {
                    event.allGroupCount.toString()
                }

            }

        }
        if (null != event?.atMeCount) {
            event.atMeCount.toIntOrNull()?.let {
                atMeCount = if (it > 99) {
                    "99+"
                } else {
                    event.atMeCount.toString()
                }

            }
        }
        if (null != event?.myGroupCount) {
            event.myGroupCount.toIntOrNull()?.let {
                myGroupCount = if (it > 99) {
                    "99+"
                } else {
                    event.myGroupCount.toString()
                }

            }
        }
        if (null != event?.otherGroupCount) {
            event.otherGroupCount.toIntOrNull()?.let {
                otherGroupCount = if (it > 99) {
                    "99+"
                } else {
                    event.otherGroupCount.toString()
                }

            }
        }
        if (null != event?.socialCount) {
            event.socialCount.toIntOrNull()?.let {
                socialCount = if (it > 99) {
                    "99+"
                } else {
                    event.socialCount.toString()
                }

            }
        }

        tabUnreadCounts = listOf(singleCount, myGroupCount, otherGroupCount, socialCount)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveEvent(event: GlobalNoticeBroadBean?) {
        if (event?.jumpAppAction == 1) {
            val intent = Intent(requireContext(), NoticeDetailActivity::class.java)
            intent.putExtra("data", NoticeInfoBean().apply {
                this.id = event.id
                this.noticeTitle = event.noticeTitle
                this.createTime = event.createTime
                this.previewContent = event.previewContent
                this.contentUrl = event.contentUrl

            })
            startActivity(intent)
        } else {
            startActivity(Intent(requireContext(), NoticeListActivity::class.java))
        }

    }

    private val tempNoticeList = mutableListOf<NoticeInfoBean>()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveEvent(event: NoticeInfoBean?) {
        event?.let { bean ->
            tempNoticeList.clear()
            tempNoticeList.add(bean)
            if (isResumed && !managerBar.isVisible) {
                showNoticeMsgDialog(tempNoticeList)
            }
        }
    }

    private var viewModel: CommViewModel? = null
    private lateinit var ivkefu: AppCompatImageView
    private lateinit var ivsudu: AppCompatImageView
    private lateinit var viewPager: ViewPager2
    private lateinit var magicIndicator: MagicIndicator
    private lateinit var ivsousuo: AppCompatImageView
    private lateinit var ivAdd: AppCompatImageView
    private lateinit var hometool: View
    private lateinit var ivMore: ComposeView
    private lateinit var cvTab: ComposeView
    private lateinit var ivManager: ComposeView
    private lateinit var managerBar: ComposeView
    private var isManagerClick by mutableStateOf(false)
    private var tabSelectedIndex by mutableStateOf(0)
    private var tabUnreadCounts by mutableStateOf(listOf("0", "0", "0", "0"))

    private fun initViews(view: View) {
        hometool = view.findViewById(R.id.hometool)
        ivAdd = view.findViewById(R.id.ivAdd)
        ivsousuo = view.findViewById(R.id.ivsousuo)
        magicIndicator = view.findViewById(R.id.magicIndicator)
        viewPager = view.findViewById(R.id.viewPager)
        ivsudu = view.findViewById(R.id.ivsudu)
        ivkefu = view.findViewById(R.id.ivkefu)
        ivMore = view.findViewById(R.id.ivMore)
        ivManager = view.findViewById(R.id.ivManager)
        managerBar = view.findViewById(R.id.compose_manager_bar)
        cvTab = view.findViewById(R.id.cv_bg_tab)
        magicIndicator.visibility = View.INVISIBLE

        //        titles.add("全部")
        titles.add("对话")
        titles.add("私群")
        titles.add("公群")
        titles.add("好友")

        cvTab.setContent {
//            FloatShadowBar(){}
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                DynamicFloatTabBar(
                    tabs = titles,
                    selectedIndex = tabSelectedIndex,
                    indicatorFixedWidth = 16.dp,
                    unreadCounts = tabUnreadCounts,
                    contentPaddingStart = 0.dp,
                    contentPaddingEnd = 0.dp,
                    onTabSelected = { index ->
                        viewPager.currentItem = index
                    }
                )
            }

        }
        managerBar.setContent {
            val asState = uiViewModel.uiState.collectAsState()
            FloatManagerBar(
                showAllSelect = !asState.value.isAllSelect,
                showAllRead = asState.value.isAllRead,
                enableDelete = asState.value.enableDelete,
                toSelectAll = {
                    if (it) {
                        uiViewModel.selectAll()
                    } else {
                        uiViewModel.cancelSelectAll()
                    }
                },
                toSelectRead = {
                    if (it) {
                        uiViewModel.markAllAsRead()

                    } else {
                        uiViewModel.markSingleAsRead()
                    }
                    isManagerClick = false
                    uiViewModel.updateBottomState(false)
                },
                toDelete = {
                    uiViewModel.deleteSelected()
                    isManagerClick = false
                    uiViewModel.updateBottomState(false)
                })
        }
        ivManager.setContent {
            BlurButton(
                size = 50.dp, iconPainter = painterResource(R.mipmap.ic_manager_black),
                expanded = isManagerClick, onExpandedChange = {
                    val currentIndex = viewPager.currentItem
                    if (currentIndex == 3) {
                        return@BlurButton
                    }

                    isManagerClick = it
                    uiViewModel.updateBottomState(isManagerClick)


                    (fragments[currentIndex] as? ProgressFragment)?.let { fragment ->
                        val tag = when (currentIndex) {
                            0 -> "single"
                            1 -> "myGroup"
                            2 -> "otherGroup"
                            else -> "social"
                        }
                        fragment.updateUIMode(tag, isManagerClick)

                    }
                    if (!isManagerClick) {
                        uiViewModel.closeSelect()
                    }
                    viewPager.isUserInputEnabled = !isManagerClick

                })
        }
        ivMore.setContent {
//            XiaoXiMoreHolder(context, ivMore)
            BlurFabButton(
                size = 50.dp,
                addIconPainter = painterResource(R.mipmap.ic_add_black)
            ) { dismiss ->
                MorePopUp(onItemClick = { index ->
                    when (index) {
                        0 -> { // 搜索
                            JumpUtils.startSearchActivity(context)
                        }

                        1 -> { // 建群聊
                            JumpUtils.startCreateQunActivity(context)
                        }

                        2 -> { // 加好友
                            JumpUtils.startSearchUserActivity(context)
                        }

                        3 -> { // 扫一扫
                            val isGranted = XXPermissions.isGranted(requireContext(), Permission.CAMERA)
                            if (isGranted) {
                                saoyisao()
                            } else {
                                val holder3 = CommanConfimHolder(
                                    activity,
                                    "温馨提示",
                                    "${AppUtil.getAppName(requireContext())}需要相机权限,是否请求权限?",
                                    "同意",
                                    "拒绝"
                                )
                                holder3.callBack = object : CommanConfimHolder.OnConfimCallBack {
                                    override fun onClickConfim(isConfim: Boolean) {
                                        if (isConfim) {
                                            XXPermissions.with(activity!!)
                                                .permission(Permission.CAMERA).request { _, _ ->
                                                    saoyisao()
                                                }
                                        }
                                    }
                                }
                                holder3.show()
                            }
                        }
                    }
                    // 点击后关闭弹窗
                    dismiss()
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.getconfig(context)
        showNoticeMsgDialog(tempNoticeList)
        eLogD("getGroupListDataClass", "onResume-----------")
    }

    private fun saoyisao() {
        val intent = Intent(context, ScanActivity::class.java)
        intent.putExtra("jumpSource", ScanJumpEnum.Message.sourceType)
        startActivity(intent)
    }

//    private fun addFragment(targetFragment: Fragment) {
//        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
//        transaction.add(R.id.fl, targetFragment, targetFragment.javaClass.name)
//            .commitAllowingStateLoss()
//    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_xiaoxi
    }

    private fun bindTab(viewPager: ViewPager2) {
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
//                super.onPageScrolled(position, positionOffset, positionOffsetPixels)

            }

            override fun onPageSelected(position: Int) {
//                super.onPageSelected(position)
                tabSelectedIndex = position
            }

            override fun onPageScrollStateChanged(state: Int) {
//                super.onPageScrollStateChanged(state)

            }
        });

    }
}