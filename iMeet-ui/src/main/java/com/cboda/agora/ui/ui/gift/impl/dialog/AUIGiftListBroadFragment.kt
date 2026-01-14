package com.cboda.agora.ui.ui.gift.impl.dialog

import android.content.res.TypedArray
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cboda.agora.ui.R
import com.cboda.agora.ui.databinding.AuiGiftListFragmentBroadLayoutBinding
import com.cboda.agora.ui.ui.gift.AUIGiftInfo
import com.cboda.agora.ui.ui.gift.AUIGiftTabInfo
import com.cboda.agora.ui.ui.gift.listener.AUIConfirmClickBroadListener
import com.cboda.agora.ui.ui.gift.listener.AUIGiftItemClickBroadListener
import com.cboda.agora.ui.ui.gift.selected
import com.cboda.agora.ui.utils.DeviceBroadTools

class AUIGiftListBroadFragment : Fragment,AUIGiftItemClickBroadListener{
    constructor():this(null,null)
    constructor(
        gift:List<AUIGiftTabInfo>?,
        tagId:Int?
    ){
        if (gift != null) {
            this.gift = gift
        }
        this.currentTag = tagId

        if (gift != null) {
            for (auiGiftTabEntity in gift) {
                map[auiGiftTabEntity.tabId] = auiGiftTabEntity.gifts as List<AUIGiftInfo>
            }
        }
    }
    private var adapter: AUIGiftListBroadAdapter? = null
    private var giftBean: AUIGiftInfo? = null
    private var listener: AUIConfirmClickBroadListener? = null
    private var gift:List<AUIGiftTabInfo> = mutableListOf()
    private val mColumns = 4
    private var currentTag:Int? = 0

    private lateinit var aGiftListBinding: AuiGiftListFragmentBroadLayoutBinding

    private var map:MutableMap<Int,List<AUIGiftInfo>> = mutableMapOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        aGiftListBinding = AuiGiftListFragmentBroadLayoutBinding.inflate(inflater)
        return aGiftListBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // 获取自定义样式的ID
        context?.let {
            val themeTa: TypedArray = it.theme.obtainStyledAttributes(R.styleable.AUIGiftBottomDialog)
            val appearanceId = themeTa.getResourceId(R.styleable.AUIGiftBottomDialog_aui_giftBottomDialog_appearance, 0)
            themeTa.recycle()
            initView(appearanceId)
        }
    }

    private fun initView(appearanceId:Int) {
        context?.let {
            val typedArray = it.obtainStyledAttributes(appearanceId, R.styleable.AUIGiftBottomDialog)
            aGiftListBinding.gridview.verticalSpacing = DeviceBroadTools.dp2px(requireContext(), 20F)
            aGiftListBinding.gridview.numColumns = mColumns
            aGiftListBinding.gridview.verticalSpacing = 40
            adapter = AUIGiftListBroadAdapter(it, 1,typedArray, map[currentTag] as List<AUIGiftInfo>)
            aGiftListBinding.gridview.adapter = adapter
            adapter?.setSelectedPosition(0)
            adapter?.setOnItemClickListener(this)
        }
    }

    fun setOnItemSelectClickListener(listener: AUIConfirmClickBroadListener?) {
        this.listener = listener
    }

    override fun sendGift(view:View,position:Int,gift: AUIGiftInfo) {
        giftBean = adapter?.getItem(position)
        listener?.sendGift(view,gift)
    }



    override fun selectGift(view: View, position: Int, gift: AUIGiftInfo) {
        adapter?.getItem(position)?.let {
            it.selected = !it.selected
            if (it.selected) {
                adapter?.setSelectedPosition(position)
            } else {
                adapter?.setSelectedPosition(-1)
            }
        }
    }

    override fun onDestroy() {
        adapter?.clearRun()
        super.onDestroy()
    }
}