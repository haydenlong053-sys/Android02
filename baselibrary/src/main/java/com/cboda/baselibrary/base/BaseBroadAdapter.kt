package com.cboda.baselibrary.base

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.BaseLoadMoreModule
import com.chad.library.adapter.base.module.LoadMoreModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder

abstract class BaseBroadAdapter<T>(layoutResId: Int) :
    BaseQuickAdapter<T, BaseViewHolder>(layoutResId), LoadMoreModule {
    private var listener: ((Int, T) -> Unit)? = null
    private var viewListener: ((Int, Int, T) -> Unit)? = null
    private var longClickListener: ((Int, T) -> Unit)? = null
    protected open fun itemCanClick(): Boolean {
        return true
    }

    override fun convert(holder: BaseViewHolder, item: T) {
        onBindData(holder, item)
        if (itemCanClick()) {
            holder.itemView.setOnClickListener {
                if (holder is BaseViewHolder)
                    onItemClick(holder, holder.layoutPosition, item)
                listener?.invoke(holder.layoutPosition, item)
            }
            holder.itemView.setOnLongClickListener {
                if (holder is BaseViewHolder)
                    onItemClick(holder, holder.layoutPosition, item)
                longClickListener?.invoke(holder.layoutPosition, item)
                return@setOnLongClickListener false
            }
        }
    }

    open fun onItemClick(holder: BaseViewHolder, position: Int, data: T) {

    }

    open fun getViewListener() = viewListener
    open fun setOnViewItemClickListener(l: (Int, Int, T) -> Unit) {
        viewListener = l
    }

    open fun setOnItemClickListener(l: (Int, T) -> Unit) {
        listener = l
    }

    open fun setOnItemLongClickListener(l: (Int, T) -> Unit) {
        longClickListener = l
    }

    override fun convert(holder: BaseViewHolder, item: T, payloads: List<Any>) {
        val payloadsStr = if (payloads.isNotEmpty()) payloads[0] else ""
        if (payloadsStr == "noUpdateImg") {
            onBindDataPayLoads(holder, item)
        } else {
            super.convert(holder, item, payloads)
        }
    }

    protected abstract fun onBindData(holder: BaseViewHolder, bean: T)

    open fun onBindDataPayLoads(holder: BaseViewHolder, bean: T) {

    }

    open fun setData(l: List<T>?) {
        data.clear()
        l?.also {
            data.addAll(l)
        }
        notifyDataSetChanged()
    }


    open fun updateData3(l: List<T>?) {
        if (l?.size == data.size && data.isNotEmpty()) {
            l.forEachIndexed { index, t ->
                data[index] = t
            }
            updateListPartial(0)
        } else {
            setData(l)
        }
    }


    open fun updateIsAddData(newData: List<T>?) {
        newData?.also {
            data.addAll(newData)
            notifyItemRangeInserted(data.size - newData.size, newData.size)
        }
    }

    open fun updateIsAddData(newDataSize: Int) {
//        newData?.also {
//            data.addAll(newData)
//            notifyItemRangeInserted(data.size - newData.size, newData.size)
//        }
        if (newDataSize >= 1) {
            notifyItemRangeInserted(data.size - newDataSize, newDataSize)
        }
    }

//    open fun addData(l: List<T>?) {
//        val positionStart = data.size
//        l?.also {
//            data.addAll(l)
//        }
//        if (data.size > positionStart) {
//            notifyItemRangeInserted(positionStart, data.size - positionStart)
//        }
//    }

    open fun updateList2(l: List<T>?) {
        val positionStart = data.size
        data.clear()
        l?.also {
            data.addAll(it)
        }
        if (data.size > positionStart) {
            updateListInserted(positionStart, data.size - positionStart)
        } else if (data.size < positionStart) {
            notifyItemRangeRemoved(data.size, positionStart - data.size)
        }
    }

    open fun updateList() {
        notifyItemRangeChanged(0, data.size)
    }


    open fun updateList3(position: Int) {
        notifyItemRangeChanged(position, 1)
    }

    //插入数据并更新
    open fun updateListInserted(position: Int, itemCount: Int) {
        notifyItemRangeInserted(position, itemCount)
    }

    //插入数据并更新
    open fun updateListInserted(l: List<T>?) {
        l?.also {
            if (l.isNotEmpty()) {
                val positionStart = data.size
                data.addAll(l)
                if (data.size > positionStart) {
                    updateListInserted(positionStart, data.size - positionStart)
                }
            }
        }
    }

    //删除数据并更新
    open fun updateListRemove(position: Int) {
        if (position != -1 && data.size > position) {
            data.removeAt(position)
            notifyItemRangeRemoved(position, 1)
        }
    }

    //更新数据
    open fun updateListPart(item: T, position: Int) {
        if (data.size > position) {
            data[position] = item
            updateListPart(position)
        }
    }

    //更新数据
    open fun updateListItem(item: T, position: Int) {
        if (data.size > position) {
            data[position] = item
            updateList3(position)
        }
    }

    open fun updateList5(itemCount: Int) {
        notifyItemRangeRemoved(data.size, itemCount)
    }

    //局部刷新数据
    open fun updateListPart(position: Int) {
        notifyItemRangeChanged(position, 1, "noUpdateImg")
    }

    //局部刷新
    open fun updateListPartial(position: Int) {
        notifyItemRangeChanged(position, data.size, "noUpdateImg")
    }

    open fun updateListOne(position: Int) {
        notifyItemRangeChanged(position, 1)
    }

    open fun updateListPartial2(position: Int) {
        notifyItemChanged(position, "noUpdateImg")
    }


    override fun addLoadMoreModule(baseQuickAdapter: BaseQuickAdapter<*, *>): BaseLoadMoreModule {
        return BaseLoadMoreModule(baseQuickAdapter)
    }


}