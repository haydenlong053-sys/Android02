package com.lxkj.aiim.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.lxkj.aiim.lj.bean.CommonSelectBean
import com.lxkj.aiim.model.OrderInfoBean

//flagType :0是转账，1是提现
class TransferListAdapter(
    layoutResId: Int = R.layout.item_rv_transfer_record,
    var flagType: Int = 0
) :
    BaseQuickAdapter<OrderInfoBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: OrderInfoBean) {
        if (flagType == 0) {
            when (item.paymentStatus) {
                0 -> {
                    holder.setText(
                        R.id.mTvTransferName,
                        context.getString(R.string.mine_transfer_ing)
                    )
                }

                1 -> {
                    holder.setText(R.id.mTvTransferName, context.getString(R.string.mine_transfer))
                }

                2 -> {
                    holder.setText(
                        R.id.mTvTransferName,
                        context.getString(R.string.mine_Transfer_fail)
                    )
                }
            }

        } else {
            //(0:发起提现 1:正在提现 2:提现成功 3:提现失败 4:申请中 5:被拒绝)
            when (item.status) {
                0, 1, 4 -> {
                    holder.setText(
                        R.id.mTvTransferName,
                        context.getString(R.string.mine_t_Withdrawal_ing)
                    )
                }

                2 -> {
                    holder.setText(R.id.mTvTransferName, context.getString(R.string.提现))
                }

                3, 5 -> {
                    holder.setText(
                        R.id.mTvTransferName,
                        context.getString(R.string.mine_t_Withdrawal_fail)
                    )
                }
            }

        }
        if (item.paymentStatus == 2 || item.status == 3 || item.status == 5) {
            holder.setTextColorRes(R.id.mTvTransferName, R.color.black_FF4D4F)
        } else {
            holder.setTextColorRes(R.id.mTvTransferName, R.color.black)
        }
        holder.setText(
            R.id.mTvTransferAddress,
            String.format(context.getString(R.string.mine_address), item.receivingAddress)
        )
        if (item.amount > 0) {
            holder.setText(R.id.mTvTransferMoney, "-${item.amount.toString()}")
        } else {
            holder.setText(R.id.mTvTransferMoney, item.amount.toString())
        }

        holder.setText(R.id.mTvTransferTime, item.createTime)
        holder.setGone(R.id.mViewLine, holder.layoutPosition == (this.data.size - 1))
    }
}