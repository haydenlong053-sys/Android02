package com.cboda.intelligent.fragment

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import com.cboda.chatui.conversationlist.ConversationGroupBroadFragment
import com.cboda.chatui.conversationlist.ConversationOtherGroupBroadFragment
import com.cboda.chatui.conversationlist.ConversationSingleBroadFragment
import com.cboda.chatui.widget.ProgressBroadFragment
import cn.wildfirechat.viewmodel.HomeUIViewModel
import com.blankj.utilcode.util.ConvertUtils.dp2px
import com.blankj.utilcode.util.SizeUtils
import com.cboda.baselibrary.bean.GlobalNoticeBroadBean
import com.cboda.baselibrary.event.UnreadCountBroadEvent
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.MyPagerBroadAdapter
import com.cboda.intelligent.base.BaseLazyBroadFragment
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.bean.GlobalBroadInfo
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.intelligent.compose.BlurButton
import com.cboda.intelligent.compose.BlurFabButton
import com.cboda.intelligent.compose.FloatManagerBar
import com.cboda.intelligent.compose.MorePopUp
import com.cboda.intelligent.dialog.pop.SysNoticeBroadPop
import com.cboda.intelligent.holder.CommanConfimBroadHolder
import com.cboda.intelligent.holder.XiaoXiMoreBroadHolder
import com.cboda.intelligent.activity.NoticeDetailBroadActivity
import com.cboda.intelligent.activity.NoticeListBroadActivity
import com.cboda.intelligent.activity.ScanBroadActivity
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.model.NoticeInfoBroadBean
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.buildins.UIBroadUtil
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.CommonNavigatorBroadAdapter
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.abs.IPagerTitleView
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.indicators.LinePagerBroadIndicator
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.SimplePagerTitleBroadView
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.badge.BadgeAnchor
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.badge.BadgeBroadRule
import com.cboda.baselibrary.view.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleBroadView
import com.cboda.intelligent.viewmodel.CommViewBroadModel
import com.cboda.intelligent.viewmodel.FriendViewBroadModel
import com.cboda.intelligent.viewmodel.NoticeViewBroadModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.cboda.baselibrary.view.magicindicator.MagicBroadIndicator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.widge.BadgeIndicatorAdapter
import com.cboda.intelligent.widge.ImgIndicatorBroadAdapter
import com.cboda.select.picture.utils.LanguageUtil
import net.lucode.hackware.magicindicator.buildins.commonnavigator.titles.badge.BadgePagerTitleView
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class MessageBroadFragment : BaseLazyBroadFragment() {
    companion object {
        fun getInstance(): Fragment {
            return MessageBroadFragment()
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

    //    var titles: ArrayList<String> = ArrayList()
    private val noticeViewModel: NoticeViewBroadModel by viewModels()
    private val friendViewModel: FriendViewBroadModel by viewModels()
    private var conversationSingleFragment: ConversationSingleBroadFragment? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
        setBottomViewHeight()
        var result = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height", "dimen", "android"
        )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = hometool?.layoutParams
        linearParams?.height = result
        hometool?.layoutParams = linearParams

        EventBus.getDefault().register(this)

        viewModel = ViewModelProvider(this)[CommViewBroadModel::class.java]
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
            val holder = XiaoXiMoreBroadHolder(context, ivAdd)
            holder.itemListener = object : XiaoXiMoreBroadHolder.OnItemClickListener {
                override fun onItemClick(content: String) {
                    when (content) {
                        "faqiqun" -> {
                            JumpBroadUtils.startCreateQunActivity(context)
                        }

                        "tianjia" -> {
                            JumpBroadUtils.startSearchUserActivity(context)
                        }

                        "saoyisao" -> {
                            val isGranted = XXPermissions.isGranted(activity!!, Permission.CAMERA)
                            if (isGranted) {
                                saoyisao()
                            } else {
                                val holder3 = CommanConfimBroadHolder(
                                    activity, getString(R.string.温馨提示), getString(R.string.camera_permission_request, AppBroadUtils.getAppName(activity!!)), getString(R.string.agree), getString(R.string.jujuele)
                                )
                                holder3.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                                    override fun onClickConfim(isConfim: Boolean) {
                                        if (isConfim) {
                                            XXPermissions.with(activity!!).permission(Permission.CAMERA).request { _, _ ->
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
            JumpBroadUtils.startSearchActivity(context)
        }

        ivsudu?.setOnClickListener {
            JumpBroadUtils.startH5Activity(context, bean?.keUrl ?: "", "")
        }

        ivkefu?.setOnClickListener {
            MMKVBroadUtils.getKeFuId()?.let { it1 -> AppBroadUtils.goChat(context, it1, "") }
        }
//        val fragment = ConversationListBroadFragment()
//        addFragment(fragment)

//        fragments.add(ConversationListBroadFragment())
        conversationSingleFragment = ConversationSingleBroadFragment()
        fragments.add(conversationSingleFragment!!)
        fragments.add(ConversationGroupBroadFragment())
        fragments.add(ConversationOtherGroupBroadFragment())
        fragments.add(SocialFragment())

        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.setData(fragments)
        viewPager.adapter = vp2FragmentAdapter
        viewPager.offscreenPageLimit = fragments.size
        viewPager.isUserInputEnabled = true
        initMagicIndicator()


        initNoticeMsg()

    }

    private fun initFriendList() {
        eLogD(APP_TAG, "initFriendList-----------")
        lifecycleScope.launch {
            friendViewModel.fetchFriendShipList()
//                .observe(viewLifecycleOwner) { mList ->
//                eLogD("getGroupListDataClass", "fetchFriendShipList-----------${mList.first.size}")
//                if (mList.first.isNotEmpty()) {
//                    MMKVBroadUtils.saveUserListData(mList.first, lifecycleScope)
//                }
//                if (mList.second.isNotEmpty()) {
//                    RealmUtil.addUserData(mList.second, lifecycleScope)
//                }
//            }
            friendViewModel.fetchGroupList().observe(viewLifecycleOwner) { mList ->
                eLogD("getGroupListDataClass", "fetchGroupList-----------${mList.size}")
                if (mList.isNotEmpty()) {
                    MMKVBroadUtils.saveGroupList(mList, lifecycleScope)
                }
            }
        }

    }

    private fun initNoticeMsg() {
        lifecycleScope.launch {
            noticeViewModel.getNoticeInfoList(1, CommonBroadConfig.pageSize_100).observe(viewLifecycleOwner) { mList ->
                if (mList.list?.isNotEmpty() == true) {
                    eLogD(
                        APP_TAG, "mList.list!![0].noticeTitle==" + mList.list!![0].noticeTitle
                    )
                    val noticeBean = GlobalNoticeBroadBean(
                        mList.list!![0].id, mList.list!![0].createTime, mList.list!![0].noticeTitle
                    )

                    eLogD(
                        APP_TAG, "mNoticeBean==" + noticeBean.noticeTitle
                    )
                    conversationSingleFragment?.setGlobalNoticeBean(noticeBean)
                    val infoBeans = mList.list?.filter { bean -> !bean.isRead && bean.forcePopup && bean.status }
//                            mList.list?.filter { bean ->  bean.status }
                    showNoticeMsgDialog(infoBeans)
                }
            }
        }
    }

    private var sysNoticePop: SysNoticeBroadPop? = null
    private fun showNoticeMsgDialog(infoBeans: List<NoticeInfoBroadBean>?) {
//        if (infoBeans?.isNotEmpty() == true){
//            Log.d(APP_TAG,"===${infoBeans[0].isRead}---${infoBeans[0].forcePopup}---${infoBeans[0].status}")
//        }
        if (infoBeans?.isNotEmpty() == true && sysNoticePop?.isShowing != true && !infoBeans[0].isRead && infoBeans[0].forcePopup && infoBeans[0].status) {
            sysNoticePop = SysNoticeBroadPop(requireContext(), infoBeans[0])
            sysNoticePop?.showPopupWindow()
            tempNoticeList.clear()
        }
    }

    private val badgeCounts = intArrayOf(anyToInt(singleCount), anyToInt(myGroupCount), anyToInt(otherGroupCount), anyToInt(socialCount))
    private var indicatorAdapt: BadgeIndicatorAdapter? = null
    private fun initMagicIndicator() {
        val mResIds = intArrayOf(R.string.conversation_tab, R.string.private_group_tab, R.string.public_group_tab, R.string.friend_tab)
        magicIndicator.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonBroadNavigator(requireContext())
        commonNavigator.isAdjustMode = LanguageUtil.getLanList(MMKVBroadUtils.getAppLanguageKey(requireContext())).startsWith("zh")
        commonNavigator.isSkimOver = true
        indicatorAdapt = BadgeIndicatorAdapter(requireContext(), mResIds, badgeCounts)
        commonNavigator.adapter = indicatorAdapt
        magicIndicator.navigator = commonNavigator
        bindIndicatorAndViewPager(
            magicIndicator, viewPager, indicatorAdapt, magicIndicatorNeedOnPageScrolled = true
        )
    }

    private fun updateBadges() {
        badgeCounts[0] = anyToInt(singleCount)
        badgeCounts[1] = anyToInt(myGroupCount)
        badgeCounts[2] = anyToInt(otherGroupCount)
        badgeCounts[3] = anyToInt(socialCount)
        indicatorAdapt?.notifyDataSetChanged()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEvent(event: UnreadCountBroadEvent?) {
//        if (null != event?.count0)
//            count0 = event?.count0.toString()
        if (null != event?.singleCount) {
            event.singleCount.toIntOrNull()?.let {
                singleCount = event.singleCount.toString()
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
                myGroupCount = event.myGroupCount.toString()
            }
        }
        if (null != event?.otherGroupCount) {
            event.otherGroupCount.toIntOrNull()?.let {
                otherGroupCount = event.otherGroupCount.toString()

            }
        }
        if (null != event?.socialCount) {
            event.socialCount.toIntOrNull()?.let {
                socialCount = event.socialCount.toString()
            }
        }

        updateBadges()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveEvent(event: GlobalNoticeBroadBean?) {
        if (event?.jumpAppAction == 1) {
            val intent = Intent(requireContext(), NoticeDetailBroadActivity::class.java)
            intent.putExtra("data", NoticeInfoBroadBean().apply {
                this.id = event.id
                this.noticeTitle = event.noticeTitle
                this.createTime = event.createTime
                this.previewContent = event.previewContent
                this.contentUrl = event.contentUrl

            })
            startActivity(intent)
        } else {
            startActivity(Intent(requireContext(), NoticeListBroadActivity::class.java))
        }

    }

    private val tempNoticeList = mutableListOf<NoticeInfoBroadBean>()

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onReceiveEvent(event: NoticeInfoBroadBean?) {
        event?.let { bean ->
            tempNoticeList.clear()
            tempNoticeList.add(bean)
            if (isResumed && !managerBar.isVisible) {
                showNoticeMsgDialog(tempNoticeList)
            }
        }
    }

    private var viewModel: CommViewBroadModel? = null
    private var bottomView: View? = null
    private lateinit var ivkefu: AppCompatImageView
    private lateinit var ivsudu: AppCompatImageView
    private lateinit var viewPager: ViewPager2
    private lateinit var magicIndicator: MagicBroadIndicator
    private lateinit var ivsousuo: AppCompatImageView
    private lateinit var ivAdd: AppCompatImageView
    private lateinit var hometool: View
    private lateinit var ivMore: ComposeView
    private lateinit var ivManager: ComposeView
    private lateinit var managerBar: ComposeView
    private var isManagerClick by mutableStateOf(false)
    private var commonNavigator: CommonBroadNavigator? = null
//    private val badgeViews = mutableMapOf<Int, TextView>()

    private var marginBottom = 0

    fun setBottomHeight(height: Int) {
        marginBottom = height
        setBottomViewHeight()
    }


    private fun setBottomViewHeight() {
        bottomView?.let {
            val layoutParams = it.layoutParams
            layoutParams.height = marginBottom
            it.layoutParams = layoutParams
        }

    }

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
        bottomView = view.findViewById(R.id.bottomView3)
        magicIndicator.visibility = View.VISIBLE

        //        titles.add("全部")
//        titles.add(getString(R.string.conversation_tab))
//        titles.add(getString(R.string.private_group_tab))
//        titles.add(getString(R.string.public_group_tab))
//        titles.add(getString(R.string.friend_tab))
        managerBar.setContent {
            val asState = uiViewModel.uiState.collectAsState()
            FloatManagerBar(showAllSelect = !asState.value.isAllSelect, showAllRead = asState.value.isAllRead, enableDelete = asState.value.enableDelete, toSelectAll = {
                if (it) {
                    uiViewModel.selectAll()
                } else {
                    uiViewModel.cancelSelectAll()
                }
            }, toSelectRead = {
                if (it) {
                    uiViewModel.markAllAsRead()

                } else {
                    uiViewModel.markSingleAsRead()
                }
                isManagerClick = false
                uiViewModel.updateBottomState(false)
            }, toDelete = {
                uiViewModel.deleteSelected()
                isManagerClick = false
                uiViewModel.updateBottomState(false)
            })
        }
        ivManager.setContent {
            BlurButton(
                size = 50.dp, iconPainter = painterResource(R.mipmap.ic_manager_broad_black), expanded = isManagerClick, onExpandedChange = {
                    val currentIndex = viewPager.currentItem
                    if (currentIndex == 3) {
                        return@BlurButton
                    }

                    isManagerClick = it
                    uiViewModel.updateBottomState(isManagerClick)


                    (fragments[currentIndex] as? ProgressBroadFragment)?.let { fragment ->
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
//            XiaoXiMoreBroadHolder(context, ivMore)
            BlurFabButton(
                size = 50.dp, addIconPainter = painterResource(R.mipmap.ic_add_broad_black)
            ) { dismiss ->
                MorePopUp(onItemClick = { index ->
                    when (index) {
                        0 -> { // 搜索
                            JumpBroadUtils.startSearchActivity(context)
                        }

                        1 -> { // 建群聊
                            JumpBroadUtils.startCreateQunActivity(context)
                        }

                        2 -> { // 加好友
                            JumpBroadUtils.startSearchUserActivity(context)
                        }

                        3 -> { // 扫一扫
                            val isGranted = XXPermissions.isGranted(requireContext(), Permission.CAMERA)
                            if (isGranted) {
                                saoyisao()
                            } else {
                                val holder3 = CommanConfimBroadHolder(
                                    activity, getString(R.string.温馨提示), getString(R.string.camera_permission_request, AppBroadUtils.getAppName(requireContext())), getString(R.string.agree), getString(R.string.jujuele)
                                )
                                holder3.callBack = object : CommanConfimBroadHolder.OnConfimCallBack {
                                    override fun onClickConfim(isConfim: Boolean) {
                                        if (isConfim) {
                                            XXPermissions.with(activity!!).permission(Permission.CAMERA).request { _, _ ->
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
        val intent = Intent(context, ScanBroadActivity::class.java)
        intent.putExtra("jumpSource", ScanJumpBroadEnum.Message.sourceType)
        startActivity(intent)
    }

//    private fun addFragment(targetFragment: Fragment) {
//        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
//        transaction.add(R.id.fl, targetFragment, targetFragment.javaClass.name)
//            .commitAllowingStateLoss()
//    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_xiao_broad_xi
    }

//    private fun bindTab(viewPager: ViewPager2) {
//        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
//            override fun onPageScrolled(
//                position: Int,
//                positionOffset: Float,
//                positionOffsetPixels: Int
//            ) {
//                magicIndicator.onPageScrolled(position, positionOffset, positionOffsetPixels)
//            }
//
//            override fun onPageSelected(position: Int) {
//                magicIndicator.onPageSelected(position)
//            }
//
//            override fun onPageScrollStateChanged(state: Int) {
//                magicIndicator.onPageScrollStateChanged(state)
//            }
//        })
//    }
}