package com.cboda.intelligent.holder

import android.content.Context
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.CompressionBroadUtil.imgCompression
import com.cboda.select.picture.entity.LocalMedia
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SelectPicBroadAdapter
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.JumpBroadUtils
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MutliPicSelectBroadHolder(
    private val context: Context,
    private val recyclerView: RecyclerView?,
    private val maxPic: Int = 9,
    private val hasSubmitImageList: ArrayList<String> = ArrayList<String>(),
    private val isDrag: Boolean = false,
    private val isCompress: Boolean = true
) {
    var mDataList = arrayListOf<String>()
    private var mAdapter: SelectPicBroadAdapter? = null
    private var imgsList = arrayListOf<String>()

    init {
        if (isDrag) {
            val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.Callback() {
                override fun getMovementFlags(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder
                ): Int {
                    if (viewHolder.getAdapterPosition() == mDataList.size - 1) {
                        return makeMovementFlags(0, 0)
                    }
                    val dragFlags =
                        ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN // 支持上下拖动
                    val swipeFlags = 0 // 不支持滑动
                    return makeMovementFlags(dragFlags, swipeFlags)
                }

                override fun onMove(
                    recyclerView: RecyclerView,
                    source: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    val fromPosition = source.getAdapterPosition()
                    val toPosition = target.getAdapterPosition()
                    if (fromPosition == mDataList.size - 1 || toPosition == mDataList.size - 1) {
                        return false
                    }
                    Collections.swap(mDataList, fromPosition, toPosition)
                    recyclerView.adapter?.notifyItemMoved(fromPosition, toPosition)
                    return true
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                }

            })
            itemTouchHelper.attachToRecyclerView(recyclerView)
        }


        mAdapter = SelectPicBroadAdapter(R.layout.item_add_comment_pic_layout)
        recyclerView?.apply {
            layoutManager = GridLayoutManager(context, 3)
            adapter = mAdapter
        }
        mDataList.clear()
        if (hasSubmitImageList.isNotEmpty()) {
            mDataList.addAll(hasSubmitImageList)
        }
        mDataList.add("add")
        mAdapter?.setList(mDataList)
        mAdapter?.addChildClickViewIds(R.id.deleteIcon1)
        mAdapter?.setOnItemChildClickListener { _, view, position ->
            if (mDataList.isEmpty()) return@setOnItemChildClickListener
            when (view.id) {
                R.id.deleteIcon1 -> {
                    val index = mDataList.indexOf(view.tag as String)
                    mDataList.removeAt(index)
                    mAdapter?.setList(mDataList)
                }
            }
        }
        mAdapter?.setOnItemClickListener { _, _, position ->
            if (mDataList.isEmpty()) return@setOnItemClickListener
            imgsList.clear()
            for (i in 0 until mDataList.size) {
                if (mDataList[i] != "add") {
                    imgsList.add(mDataList[i])
                }
            }
            if (mDataList[position] == "add") {
                if (imgsList.size >= maxPic) {
                    ToastBroadUtils.show("最多添加${maxPic}张图片")
                    return@setOnItemClickListener
                }
                openGallery()
            } else {
                JumpBroadUtils.startZoomPicActivity(context, imgsList, position = position)
            }
        }

    }

    private var locaList = arrayListOf<String>()
    private var imagePickerHolder2: ImagePickerBroadHolder? = null

    private fun openGallery() {
        if (imagePickerHolder2 == null) {
            imagePickerHolder2 = ImagePickerBroadHolder(context)
            imagePickerHolder2?.callBack = object : ImagePickerBroadHolder.OnResultCallback {
                override fun onResult(result: java.util.ArrayList<LocalMedia>?) {
                    result?.apply {
                        locaList.clear()
                        var leftCount = maxPic - mDataList.size + 1
                        leftCount = if (result.size > leftCount) leftCount else result.size
                        AppBroadUtils.debug(
                            "图片选择",
                            "--> leftCount= $leftCount  result= ${result.size}  maxPic= $maxPic"
                        )
                        for (i in 0 until leftCount) {
                            locaList.add(this[i].realPath)
                        }
                        mDataList.addAll(0, locaList)
                        mAdapter?.setList(mDataList)

                        getRealFileList()
                    }
                }

                override fun onCancel() {
                }

            }
        }
        imagePickerHolder2?.show(isVideo = false, isSingle = false, maxPic - imgsList.size, false)
    }

    private val fileList = ArrayList<File>()

    var lubanFileList = ArrayList<File>()

    var tvtupuanshu: TextView? = null

    private fun getRealFileList() {
        fileList.clear()
        for (i in 0 until mDataList.size) {
            if (!mDataList[i].startsWith("http") && !mDataList[i].startsWith("https") && mDataList[i] != "add") {
                fileList.add(File(mDataList[i]))
            }
        }

        lubanFileList.clear()
        tvtupuanshu?.text = "${fileList.size}/$maxPic"
        if (isCompress) {
            fileList.forEach { file ->
                imgCompression(context, file) { _, compressFile ->
                    compressFile?.apply {
                        lubanFileList.add(this)
                    }
                }
            }
        } else {
            lubanFileList.addAll(fileList)
        }

    }

    private var indexList = arrayListOf<Int>()
    private var stringBuilder = StringBuilder()

    /**
     * 上传完调用
     */
    fun getUrls(urlList: List<String>?): String {
        indexList.clear()
        for (i in 0 until mDataList.size) {
            if (!mDataList[i].startsWith("http") && !mDataList[i].startsWith("https") && mDataList[i] != "add") {
                indexList.add(i)
            }
        }
        if (indexList.isNotEmpty()) {
            urlList?.forEachIndexed { index, s ->
                mDataList[indexList[index]] = s
            }
        }

        stringBuilder.clear()
        mDataList.forEachIndexed { index, s ->
            if (s != "add") {
                if (index == mDataList.size - 2) {
                    stringBuilder.append(s)
                } else {
                    stringBuilder.append(s)
                    stringBuilder.append("|")
                }
            }
        }

        return stringBuilder.toString()
    }
}