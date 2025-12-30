package com.lxkj.aiim.holder

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.PopupWindow
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lxkj.aiim.R
import com.lxkj.aiim.adapter.ShuZiAdapter

class ZhuanZhangKeyBoardHolder(
    private val context: Context?,
    private val view: View?,
    private val editText:EditText?
) {
    var pop: PopupWindow? = null
    private var recyclerView:RecyclerView? = null
    private var shanchulay:View? = null
    private var zhuanzhangview:View? = null
    private var lingshuzi:View? = null
    private var xiaoshudian:View? = null
    private var mDataList = arrayListOf<String>()
    private var shuziAdapter: ShuZiAdapter? = null


    fun show() {
        context?.apply {
            if (pop == null) {
                val popView = View.inflate(context, R.layout.zhuanzhang_keyboard_pop_layout, null)

                recyclerView = popView?.findViewById(R.id.recyclerView)
                shanchulay = popView?.findViewById(R.id.shanchulay)
                zhuanzhangview = popView?.findViewById(R.id.zhuanzhangview)
                lingshuzi = popView?.findViewById(R.id.lingshuzi)
                xiaoshudian = popView?.findViewById(R.id.xiaoshudian)


                pop = PopupWindow(
                    popView, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true
                )
                pop?.isTouchable = true
                pop?.isOutsideTouchable = true
                pop?.animationStyle = R.style.main_menu_animstyle
                pop?.setBackgroundDrawable(BitmapDrawable())
            }

            shuziAdapter = ShuZiAdapter(R.layout.list_shuzi_layout)
            pop?.apply {
                view?.apply {
                    showAtLocation(view,  Gravity.BOTTOM, 0, 0)
                }
            }
            mDataList.clear()
            for (i in 1 until 10){
                mDataList.add(i.toString())
            }

            recyclerView?.apply {
                layoutManager = GridLayoutManager(context,3)
                adapter = shuziAdapter
            }
            shuziAdapter?.setList(mDataList)
            shuziAdapter?.setOnItemClickListener{_,_,position->
                updateContent(mDataList[position])
            }

            shanchulay?.setOnClickListener {
                val length = editText?.getText()?.toString()?.length?:0
                val curSelection = editText?.selectionStart?:0
                if (length > 0 && curSelection > 0 && curSelection <= length) {
                    val content = editText?.getText().toString()
                    editText?.setText(content.substring(0, curSelection - 1) + content.subSequence(curSelection, length))
                    editText?.setSelection(curSelection - 1)
                }
            }

            zhuanzhangview?.setOnClickListener {
                if (TextUtils.isEmpty(editText?.text.toString())) {
                    return@setOnClickListener
                }
                pop?.dismiss()
                zhuanZhangLisetener?.zhuanzhang()
            }

            lingshuzi?.setOnClickListener {
                updateContent("0")
            }

            xiaoshudian?.setOnClickListener {

                if (editText?.text.isNullOrEmpty()){
                    return@setOnClickListener
                }

                if (editText?.text?.toString()?.contains(".") == true){
                    return@setOnClickListener
                }

                val curSelection = editText?.selectionStart ?:0
                val length = editText?.getText()?.toString()?.length?:0
                if (curSelection < length) {
                    val content = editText?.getText()?.toString()
                    editText?.setText(content?.substring(0, curSelection) + "." + content?.subSequence(curSelection, length))
                    editText?.setSelection(curSelection + 1)
                } else {
                    editText?.setText(editText.getText().toString() + ".")
                    editText?.setSelection(editText.getText().toString().length)
                }
            }
        }

    }

    fun refreshKeyboardOutSideTouchable(isTouchable:Boolean){
//        pop?.isOutsideTouchable = isTouchable
//        pop?.isTouchable = isTouchable
        if (!isTouchable) {
            pop?.showAtLocation(view, Gravity.BOTTOM, 0, 0)
        } else {
            pop?.dismiss()
        }
    }

    private fun updateContent(shuru:String){
        val curSelection = editText?.selectionStart?:0
        val length = editText?.getText()?.toString()?.length?:0
        if (curSelection < length) {
            val content = editText?.getText()?.toString()
            editText?.setText(content?.substring(0, curSelection) + shuru + content?.subSequence(curSelection, length))
            editText?.setSelection(curSelection + 1)
        } else {
            editText?.setText(editText.getText().toString() + shuru)
            editText?.setSelection(editText.getText().toString().length)
        }
    }
    var zhuanZhangLisetener:ZhuanZhangLisetener? = null

    interface ZhuanZhangLisetener{
        fun zhuanzhang()
    }
}