package com.cboda.intelligent.viewmodel

import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess

import com.cboda.intelligent.livedata.SingleLiveBroadData

import com.cboda.intelligent.configuration.CommonBroadConfig

import com.cboda.intelligent.utils.UrlBroadBroadConstants

import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.model.NoticeInfoBroadBean

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


 class NoticeViewBroadModel: BaseViewModel() {



     /**
      * 获取公告分页列表
      */
     suspend fun getNoticeInfoList(pageNo: Int =1,pageSize: Int=CommonBroadConfig.pageSize_20) :SingleLiveBroadData<NetworkListBroadResult<NoticeInfoBroadBean>>{
         val safeCenterLiveData = SingleLiveBroadData<NetworkListBroadResult<NoticeInfoBroadBean>>()
         val params = hashMapOf("pageNo" to pageNo,"pageSize" to pageSize)
         withContext(Dispatchers.IO) {
             try {
                 NetworkBroadRepository.postEncryptedList2<NoticeInfoBroadBean>(
                     UrlBroadBroadConstants.NOTICE_LIST,
                     NoticeInfoBroadBean::class.java,
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
     suspend fun goNoticeRead(id: String?):SingleLiveBroadData<Boolean>{
//         getTopBaseSimple2Activity()?.showLoading()
         val mLiveData = SingleLiveBroadData<Boolean>()
         withContext(Dispatchers.IO) {
             try {
                 NetworkBroadRepository.postEncrypted2(
                     UrlBroadBroadConstants.NOTICE_READ,
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