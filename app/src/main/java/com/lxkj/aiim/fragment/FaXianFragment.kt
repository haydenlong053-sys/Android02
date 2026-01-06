package com.lxkj.aiim.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseLazyFragment
import com.lxkj.aiim.lj.CiecelActivity
import com.lxkj.aiim.lj.ScanActivity
import com.lxkj.aiim.lj.SignInActivity
import com.lxkj.aiim.lj.adapter.FaxianAdapter
import com.lxkj.aiim.lj.bean.DataListBean
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.configuration.ScanJumpEnum
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.http.OkHttpHelper
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.utils.StringUtil
import com.lxkj.aiim.lj.view.BadgeView
import com.lxkj.aiim.utils.JumpUtils.Companion.startH5Activity
import okhttp3.Request
import okhttp3.Response

class FaXianFragment:BaseLazyFragment() {

    companion object {
        fun getInstance(): Fragment {
            return FaXianFragment()
        }
    }

    private var faxianAdapter: FaxianAdapter? = null
    private val listBeans = ArrayList<DataListBean>()
    private lateinit var ryFaxian: RecyclerView
    private lateinit var llQiandao: LinearLayout
    private lateinit var llSao: LinearLayout
    private lateinit var viLine: View
    private lateinit var llFenxaing: LinearLayout
    private lateinit var badgeView: BadgeView
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
            val intent = Intent(context, ScanActivity::class.java)
            intent.putExtra("jumpSource",ScanJumpEnum.Find.sourceType)
            context?.startActivity(intent)
        }
        llFenxaing?.setOnClickListener{//好友分享
            val intent = Intent(context, CiecelActivity::class.java)
            context?.startActivity(intent)
        }
        llQiandao?.setOnClickListener{//签到红包
            val intent = Intent(context, SignInActivity::class.java)
            context?.startActivity(intent)
        }

        faxianAdapter = FaxianAdapter(context, listBeans)
        val linearLayoutManager = LinearLayoutManager(context)
        ryFaxian?.setLayoutManager(linearLayoutManager)
        ryFaxian?.setAdapter(faxianAdapter)
        faxianAdapter!!.setOnItemClickListener(FaxianAdapter.OnItemClickListener { position ->
            startH5Activity(context,listBeans.get(position).url, listBeans.get(position).title)
        })

        urlList();
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_faxian
    }

    /**
     * 外链列表
     * */
    private fun urlList() {
        val param: MutableMap<String, Any> = HashMap()
        OkHttpHelper.getInstance().post_json(context, Url.urlList, param, object : BaseCallback<ResultBean>() {
            override fun onError(response: Response, code: Int, e: Exception) {}
            override fun onSuccess(response: Response?, t: ResultBean) {
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
        OkHttpHelper.getInstance().post_json(context, Url.userInfo, param, object : BaseCallback<ResultBean>() {
            override fun onError(response: Response, code: Int, e: Exception) {}
            override fun onSuccess(response: Response?, t: ResultBean) {
                if (StringUtil.isEmpty(t.data.quanCount)){
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