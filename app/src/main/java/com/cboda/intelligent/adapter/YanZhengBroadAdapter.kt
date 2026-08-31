package com.cboda.intelligent.adapter

import android.text.TextUtils
import cn.wildfirechat.remote.ChatManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import androidx.core.graphics.toColorInt
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.activity.VerifyBroadActivity.Companion.GROUP_INVITED

class YanZhengBroadAdapter(layoutResId: Int, private val fromPage: String) :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
        val ivTouxiang = holder.getView<VpGradeView>(R.id.ivTouxiang)
        if (fromPage == "xindepengyou" || item.icon?.isNotEmpty() == true) {
            ivTouxiang.setData2(item.icon, item.isLiang == "1", item.isvip == "1", item.grade, item.registDay)
        } else if (fromPage == GROUP_INVITED && HttpsBroadConfig.useSelfImOnly) {
            ivTouxiang.loadUserImg(item.icon ?: "")
        } else {
            val groupInfo = ChatManager.Instance().getGroupInfo(item.id, false)
            ivTouxiang.loadUserImg(groupInfo?.portrait ?: "")
        }

        holder.setText(R.id.tvname, item.name ?: "")
        if (fromPage == GROUP_INVITED) {
            holder.setText(R.id.tvnicheng, (if (!TextUtils.isEmpty(item.applicantName)) item.applicantName else "") + item.info)
        } else {
            holder.setText(R.id.tvnicheng, item.sinfo ?: "")
        }

        if (fromPage == GROUP_INVITED && HttpsBroadConfig.useSelfImOnly) {
            bindSelfImGroupState(holder, item)
        } else {
            bindLegacyState(holder, item)
        }
    }

    /**
     * 自研入群申请 status：0 申请中 / 1 已同意 / 2 已拒绝。
     * type=approve 待我审批时可操作；type=apply 为我发起的申请。
     */
    private fun bindSelfImGroupState(
        holder: BaseViewHolder,
        item: CommonDataListBroadBean.CommonBean
    ) {
        when (item.state) {
            "0" -> {
                holder.setGone(R.id.tvNotAgree, false)
                holder.setGone(R.id.tvAgree, false)
                holder.setGone(R.id.yitianjia, true)
                holder.setText(R.id.yitianjia, "")
            }

            "1" -> {
                holder.setGone(R.id.tvNotAgree, true)
                holder.setGone(R.id.tvAgree, true)
                holder.setGone(R.id.yitianjia, false)
                holder.setText(R.id.yitianjia, context.getString(R.string.yitianjia))
                holder.setTextColor(R.id.yitianjia, "#59000000".toColorInt())
            }

            else -> {
                // "2" 已拒绝及其它
                holder.setGone(R.id.tvNotAgree, true)
                holder.setGone(R.id.tvAgree, true)
                holder.setGone(R.id.yitianjia, false)
                holder.setText(R.id.yitianjia, context.getString(R.string.yijujue))
                holder.setTextColor(R.id.yitianjia, "#EB534B".toColorInt())
            }
        }
    }

    /** 好友申请 / 野火入群：1 待处理 / 2 已添加 / 其它 已拒绝 */
    private fun bindLegacyState(
        holder: BaseViewHolder,
        item: CommonDataListBroadBean.CommonBean
    ) {
        when (item.state) {
            "1" -> {
                holder.setGone(R.id.tvNotAgree, false)
                holder.setGone(R.id.tvAgree, false)
                holder.setGone(R.id.yitianjia, true)
                holder.setText(R.id.yitianjia, "")
            }

            "2" -> {
                holder.setGone(R.id.tvNotAgree, true)
                holder.setGone(R.id.tvAgree, true)
                holder.setGone(R.id.yitianjia, false)
                holder.setText(R.id.yitianjia, context.getString(R.string.yitianjia))
                holder.setTextColor(R.id.yitianjia, "#59000000".toColorInt())
            }

            else -> {
                holder.setGone(R.id.tvNotAgree, true)
                holder.setGone(R.id.tvAgree, true)
                holder.setGone(R.id.yitianjia, false)
                holder.setText(R.id.yitianjia, context.getString(R.string.yijujue))
                holder.setTextColor(R.id.yitianjia, "#EB534B".toColorInt())
            }
        }
    }
}
