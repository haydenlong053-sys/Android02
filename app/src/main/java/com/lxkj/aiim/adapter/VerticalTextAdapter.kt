package com.lxkj.aiim.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.view.isGone
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.utils.setTextGradientColor
import com.hjq.shape.view.ShapeTextView
import com.cboda.intelligent.R


class VerticalTextAdapter(private val items: List<String>) :
    RecyclerView.Adapter<VerticalTextAdapter.ViewHolder>() {
    private var currentIndex = 0
    fun updateCurrentIndex(newIndex: Int) {
        if (newIndex == currentIndex) return

        // 记录旧索引和新索引（用于局部刷新）
        val oldIndex = currentIndex
        currentIndex = newIndex

        // 刷新旧位置、新位置、以及新位置的下一项（如果需要）
        notifyItemChanged(oldIndex)
        notifyItemChanged(newIndex)
        notifyItemChanged(newIndex + 1) // 下一项也要更新（渐变效果）
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: ShapeTextView = itemView.findViewById(R.id.text_view)
        val view: View = itemView.findViewById(R.id.view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.vertical_text_adapter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.textView.text = items[position % items.size] // 循环显示
        holder.view.isGone = position == 0
        when (position) {
            currentIndex -> {
                // 当前项：黑色，不透明
                holder.textView.setTextGradientColor(
                    com.cboda.baselibrary.R.color.color_94A3B8,
                    com.cboda.baselibrary.R.color.color_94A3B8
                )
            }


            else -> {
                // 其他项：更淡
                holder.textView.setTextGradientColor(
                    com.cboda.baselibrary.R.color.color_94A3B8_50,
                    com.cboda.baselibrary.R.color.transparent
                )

            }
        }
    }

    override fun getItemCount() = Int.MAX_VALUE // 无限循环
}