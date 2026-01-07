package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.DialogPopSureTransferBinding
import com.cboda.intelligent.utils.GlideBroadUtil
import com.cboda.intelligent.model.PersonInfoBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import razerdp.basepopup.BasePopupWindow

//确认转账提示pop
class TransferSureTipBroadPop(val context: Context,val actualAmount: String="") : BasePopupWindow(context) {
    //    private val scope = MainScope() // 创建主线程的协程作用域
//    private var viewModel: TransferViewBroadModel? = null
    var sureInvoke:()-> Unit={}
    private var binding = DialogPopSureTransferBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        EventBus.getDefault().register(this)
        binding.mTvCancel.setOnClickListener {
            dismiss()
        }
        binding.mTvSure.setOnClickListener {
            sureInvoke.invoke()
            dismiss()
        }
        initData()
    }

    private fun initData() {
//        if (context is AppCompatActivity) {
//            val activity = context
//            viewModel =
//                ViewModelProvider(activity).get<TransferViewBroadModel>(TransferViewBroadModel::class.java)
//        }

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: PersonInfoBean?) {
        event?.let {
            GlideBroadUtil.setImag(context,R.mipmap.ic_icon_avatar,event.icon,binding.mIvAvatar)
            binding.mTvName.text =it.userName
            binding.mTvMoney.text =actualAmount
            binding.mTvUID.text = String.format(context.getString(R.string.mine_user_uid),it.uid)
        }

    }

    override fun onDismiss() {
        EventBus.getDefault().unregister(this)
//        scope.cancel()
        super.onDismiss()
    }
}