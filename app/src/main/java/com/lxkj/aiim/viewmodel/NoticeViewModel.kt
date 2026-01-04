package com.lxkj.aiim.viewmodel

import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess

import com.lxkj.aiim.livedata.SingleLiveData

import com.lxkj.aiim.lj.configuration.CommonConfig

import com.lxkj.aiim.lj.http.UrlBroadConstants
import com.lxkj.aiim.lj.utils.getTopBaseSimple2Activity

import com.lxkj.aiim.lj.utils.showToastMain
import com.lxkj.aiim.model.NoticeInfoBean

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


 class NoticeViewModel: ViewModel() {



     /**
      * 获取公告分页列表
      */
     suspend fun getNoticeInfoList(pageNo: Int =1,pageSize: Int=CommonConfig.pageSize_20) :SingleLiveData<NetworkListBroadResult<NoticeInfoBean>>{
         val safeCenterLiveData = SingleLiveData<NetworkListBroadResult<NoticeInfoBean>>()
         val params = hashMapOf("pageNo" to pageNo,"pageSize" to pageSize)
         withContext(Dispatchers.IO) {
             try {
                 NetworkBroadRepository.postEncryptedList2<NoticeInfoBean>(
                     UrlBroadConstants.NOTICE_LIST,
                     NoticeInfoBean::class.java,
                     params
                 ).onSuccess { bean ->
                     bean?.let {
                         safeCenterLiveData.postValue(it)
                     }

                 }.onFailure { code, message ->
                     showToastMain(message)
                 }.onError {

                 }

             } catch (e: Exception) {

             }
         }
         return safeCenterLiveData
     }

     /**
      * 上报已阅读
      */
     suspend fun goNoticeRead(id: String?):SingleLiveData<Boolean>{
//         getTopBaseSimple2Activity()?.showLoading()
         val mLiveData = SingleLiveData<Boolean>()
         withContext(Dispatchers.IO) {
             try {
                 NetworkBroadRepository.postEncrypted2(
                     UrlBroadConstants.NOTICE_READ,
                     Any::class.java,
                     hashMapOf<String, Any?>("id" to id)
                 ).onSuccess { userInfo ->
//                     getTopBaseSimple2Activity()?.dismissLoading()
                     mLiveData.postValue(true)

                 }.onFailure { code, message ->
//                     getTopBaseSimple2Activity()?.dismissLoading()
                     showToastMain(message)
                 }.onError {
//                     getTopBaseSimple2Activity()?.dismissLoading()
                 }

             } catch (e: Exception) {
//                 getTopBaseSimple2Activity()?.dismissLoading()
             }
         }
         return mLiveData
     }
 }