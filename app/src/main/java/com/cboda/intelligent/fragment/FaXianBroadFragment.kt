package com.cboda.intelligent.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseLazyBroadFragment
import com.cboda.intelligent.activity.CiecelBroadActivity
import com.cboda.intelligent.activity.ScanBroadActivity
import com.cboda.intelligent.activity.SignInBroadActivity
import com.cboda.intelligent.adapter.FaxianBroadAdapter
import com.cboda.baselibrary.bean.DataListBroadBean
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.StringBroadUtil
import com.cboda.intelligent.widge.BadgeBroadView
import com.cboda.intelligent.utils.JumpBroadUtils.Companion.startH5Activity
import okhttp3.Request
import okhttp3.Response

class FaXianBroadFragment:BaseLazyBroadFragment() {

    companion object {
        fun getInstance(): Fragment {
            return FaXianBroadFragment()
        }
    }

    private var faxianAdapter: FaxianBroadAdapter? = null
    private val listBeans = ArrayList<DataListBroadBean>()
    private lateinit var ryFaxian: RecyclerView
    private lateinit var llQiandao: LinearLayout
    private lateinit var llSao: LinearLayout
    private lateinit var viLine: View
    private lateinit var llFenxaing: LinearLayout
    private lateinit var badgeView: BadgeBroadView
    private lateinit var viHead:
            View

    private fun initViews(view: View) {
        viHead = view.findViewById(R.id.viHead)
        badgeView = view.findViewById(R.id.badgeView)
        llFenxaing = view.findViewById(R.id.llFenxaing)
        viLine = view.findViewById(R.id.viLine)
        llSao = view.findViewById(R.id.llSao)
        llQiandao = view.findViewById(R.id.llQiandao)
        ryFaxian = view.findViewById(R.id.ryFaxian)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews(view)
    }

    override fun loadData() {

        var result = 0
        val resourceId = resources.getIdentifier(
            "status_bar_height",
            "dimen", "android"
        )
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId)
        }
        val linearParams = viHead?.layoutParams
        linearParams?.height = result
        viHead?.layoutParams = linearParams


        llSao?.setOnClickListener {//扫一扫
            val intent = Intent(context, ScanBroadActivity::class.java)
            intent.putExtra("jumpSource",ScanJumpBroadEnum.Find.sourceType)
            context?.startActivity(intent)
        }
        llFenxaing?.setOnClickListener{//好友分享
            val intent = Intent(context, CiecelBroadActivity::class.java)
            context?.startActivity(intent)
        }
        llQiandao?.setOnClickListener{//签到红包
            val intent = Intent(context, SignInBroadActivity::class.java)
            context?.startActivity(intent)
        }

        faxianAdapter = FaxianBroadAdapter(context, listBeans)
        val linearLayoutManager = LinearLayoutManager(context)
        ryFaxian?.setLayoutManager(linearLayoutManager)
        ryFaxian?.setAdapter(faxianAdapter)
        faxianAdapter!!.setOnItemClickListener(FaxianBroadAdapter.OnItemClickListener { position ->
            startH5Activity(context,listBeans.get(position).url, listBeans.get(position).title)
        })

        urlList();
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_fa_broad_xian
    }

    /**
     * 外链列表
     * */
    private fun urlList() {
        val param: MutableMap<String, Any> = HashMap()
        OkHttpBroadHelper.getInstance().post_json(context, BroadUrl.urlList, param, object : BaseBroadCallback<ResultBroadBean>() {
            override fun onError(response: Response, code: Int, e: Exception) {}
            override fun onSuccess(response: Response?, t: ResultBroadBean) {
                listBeans.clear()
                listBeans.addAll(t.dataList)
                faxianAdapter?.notifyDataSetChanged()
            }

            override fun onBeforeRequest(request: Request?) {
            }

            override fun onFailure(request: Request?, e: java.lang.Exception?) {
            }

            override fun onResponse(response: Response?) {
            }
        })
    }

    /**
     * 用户信息
     * */
    private fun userInfo() {
        val param: MutableMap<String, Any> = HashMap()
        OkHttpBroadHelper.getInstance().post_json(context, BroadUrl.userInfo, param, object : BaseBroadCallback<ResultBroadBean>() {
            override fun onError(response: Response, code: Int, e: Exception) {}
            override fun onSuccess(response: Response?, t: ResultBroadBean) {
                if (StringBroadUtil.isEmpty(t.data.quanCount)){
                    badgeView.visibility = View.GONE
                }else{
                    if (t.data.quanCount.equals("0")){
                        badgeView.visibility = View.GONE
                    }else{
                        badgeView.visibility = View.VISIBLE
                        badgeView.setCount(t.data.quanCount.toInt())
                    }
                }

            }

            override fun onBeforeRequest(request: Request?) {
            }

            override fun onFailure(request: Request?, e: java.lang.Exception?) {
            }

            override fun onResponse(response: Response?) {
            }
        })
    }

    override fun onResume() {
        super.onResume()
        userInfo();
    }
}