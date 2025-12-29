package com.cboda.baselibrary.utils


import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class Vp2FragmentAdapter : FragmentStateAdapter {

    constructor(fragmentActivity: FragmentActivity) : super(fragmentActivity)

    constructor(fragment: Fragment) : super(fragment)

    private val fragments = ArrayList<Fragment>()

    fun addFragment(fragment: Fragment) {
        fragments.add(fragment)
        notifyDataSetChanged()
    }

    fun appendData(list: ArrayList<Fragment>) {
        val start = fragments.size
        fragments.addAll(list)
        notifyItemRangeInserted(start, list.size)
        notifyItemRangeChanged(start, list.size)
    }

    fun setData(list: ArrayList<Fragment>) {
        fragments.clear()
        fragments.addAll(list)
        notifyDataSetChanged()
    }

    fun clearData() {
        fragments.clear()
        notifyDataSetChanged()
    }

    fun getData() = fragments

    override fun getItemCount() = fragments.size

    override fun createFragment(position: Int) = fragments[position]

}