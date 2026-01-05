package com.lxkj.aiim.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.AppCompatImageView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import com.cboda.chatui.contact.ContactListFragment
import com.google.gson.Gson
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseLazyFragment
import com.lxkj.aiim.compose.BlurFabButton
import com.lxkj.aiim.compose.MorePopUp
import com.lxkj.aiim.holder.CommanConfimHolder
import com.lxkj.aiim.holder.XiaoXiMoreHolder
import com.lxkj.aiim.lj.ScanActivity
import com.lxkj.aiim.lj.configuration.ScanJumpEnum
import com.lxkj.aiim.utils.AppUtil
import com.lxkj.aiim.utils.JumpUtils
import com.lxkj.aiim.viewmodel.CommViewModel

class TongXunLuFragment:BaseLazyFragment() {

    private lateinit var fl: FrameLayout
    private lateinit var ivsousuo: AppCompatImageView
    private lateinit var ivAdd: AppCompatImageView
    private lateinit var hometool:
            View
    private  lateinit var  ivMore: ComposeView


    companion object {
        fun getInstance(): Fragment {
            return TongXunLuFragment()
        }
    }
    private var viewModel: CommViewModel? = null

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

        viewModel = ViewModelProvider(this)[CommViewModel::class.java]
        viewModel?.haoyouListLiveData?.observe(viewLifecycleOwner){
            it.dataList?.apply {
                contactListFragment?.setFriendData(gson.toJson(it))
            }
        }
        contactListFragment = ContactListFragment()
        addFragment(contactListFragment!!)
    }

    private var gson = Gson()
    override fun loadData() {

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

//        val userSign = MMKVBroadUtils.getTxUserSign()
//        MMKVBroadUtils.getRealUserId()?.apply {
//            TUILogin.login(
//                LiXinApp.mContext!!,
//                BroadConstant.TX_IM_APP_ID,
//                this,
//                userSign,
//                object : TUICallback() {
//                    override fun onSuccess() {
//                        AppUtil.debug("通讯录IM LOGIN", "---> success")
//                        val fragment = TUIContactFragment()
//                        addFragment(fragment)
//                    }
//
//                    override fun onError(errorCode: Int, errorMessage: String?) {
//                        AppUtil.debug("通讯录IM LOGIN ", "---> error $errorCode $errorMessage ")
//                    }
//
//                })
//        }

    }
    private var contactListFragment: ContactListFragment? = null


    private fun initViews(view: View) {
        hometool = view.findViewById(R.id.hometool)
        ivAdd = view.findViewById(R.id.ivAdd)
        ivsousuo = view.findViewById(R.id.ivsousuo)
        fl = view.findViewById(R.id.fl)
        ivMore = view.findViewById(R.id.ivMore)
        ivMore.setContent {
//            XiaoXiMoreHolder(context, ivMore)
            BlurFabButton(size = 50.dp, addIconPainter = painterResource(R.mipmap.ic_add_black)) { dismiss ->
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
                    // 点击后关闭弹窗
                    dismiss()
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.gethaoyoulist(context,"")
    }

    fun reflushTongxunlu(){
        viewModel?.gethaoyoulist(context,"")
    }

    fun updateShuLiang(xinepngy:Long,qunshu:Long){
        contactListFragment?.updateShuLiang(xinepngy,qunshu)
    }

    private fun saoyisao() {
        val intent = Intent(context, ScanActivity::class.java)
        intent.putExtra("jumpSource",ScanJumpEnum.Contacts.sourceType)
        startActivity(intent)
    }

    private fun addFragment(targetFragment: Fragment) {
        val transaction: FragmentTransaction = childFragmentManager.beginTransaction()
        transaction.add(R.id.fl, targetFragment, targetFragment.javaClass.name)
            .commitAllowingStateLoss()
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_tongxunlu
    }
}