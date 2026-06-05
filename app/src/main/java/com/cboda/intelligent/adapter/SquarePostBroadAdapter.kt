package com.cboda.intelligent.adapter

import android.R.attr.text
import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.TextUtils
import android.text.style.ForegroundColorSpan
import android.widget.TextView
import androidx.core.view.isGone
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.setTxtColor
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.utils.TimeBroadUtils
import com.cboda.intelligent.widge.SquarePostImageLayoutHelper
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import androidx.core.graphics.toColorInt

class SquarePostBroadAdapter(val type: String) :
    BaseQuickAdapter<SquarePostBean, BaseViewHolder>(R.layout.item_square_post) {

    override fun onViewRecycled(holder: BaseViewHolder) {
        super.onViewRecycled(holder)
        SquarePostImageLayoutHelper.unbind(holder.getView(R.id.squareImageContainer))
    }

    override fun convert(holder: BaseViewHolder, item: SquarePostBean) {
        holder.apply {
            setText(R.id.squareUserName, item.name)
            getView<VpGradeView>(R.id.squareAvatarWrap).setData(
                item.icon,
                anyToInt(item.isLiang) == 1,
                anyToInt(item.isvip) == 1,
                item.grade,
                item.registDay,
            )
            setText(R.id.squarePublishTime, TimeBroadUtils.formatSquarePublishTime(item.time))
            setText(R.id.squareContent, item.content)
            setText(R.id.squareViewCount, anyToInt(item.viewCount).toString())
            setText(R.id.squareCommentCount, anyToInt(item.commentCount).toString())
            setText(R.id.squareLikeCount, anyToInt(item.likeCount).toString())
            setText(R.id.squareShareCount, anyToInt(item.quoteCount).toString())
            setGone(R.id.squarePledgeBadge, anyToInt(item.odicStakeStatus) != 0 || type != "square")
            setGone(R.id.squareShareLayout, type == "square")
            setGone(R.id.squarePlLayout, type == "square")
            if (anyToInt(item.odicStakeStatus) == 0) {
                setText(R.id.squarePledgeText, item.odicStakeLabel)
            }
            SquarePostImageLayoutHelper.bind(
                getView(R.id.squareImageContainer),
                item.imgs,
            )
            setGone(R.id.squareYyLayout, item.quote == null)
            if (item.quote != null) {
                setYyTxtContent(getView(R.id.squareYyContent), item.quote.name, item.quote.content)
                SquarePostImageLayoutHelper.bind(
                    getView(R.id.squareYyImageList),
                    item.quote.imageUrls,
                )
            }
        }
    }


    private fun setYyTxtContent(textView: TextView, name: String, content: String) {
        textView.isGone = TextUtils.isEmpty(content)
        if (!TextUtils.isEmpty(content)) {
            val textBefore = "$name："
            val spannable = SpannableString(textBefore + content)

// 设置前 5 个字符为红色
            val redColor = "#FF110F67".toColorInt() // 或者 Color.parseColor("#FF0000")
            spannable.setSpan(
                ForegroundColorSpan(redColor),
                0,      // 开始索引（包含）
                textBefore.length,      // 结束索引（不包含）
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )

// 给 TextView 设置
            textView.text = spannable
        }

    }
}
