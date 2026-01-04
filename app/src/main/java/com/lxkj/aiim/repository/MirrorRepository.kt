package com.lxkj.aiim.repository

import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.NetworkBroadResult
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.lxkj.aiim.lj.http.UrlBroadConstants
import com.lxkj.aiim.model.PaymentPasswordStatusResponse
import com.lxkj.aiim.model.SignInfoViewModel
import com.lxkj.aiim.model.UserInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MirrorRepository {

    suspend fun getUserMirrorInfo(): NetworkBroadResult<UserInfoModel> {
        val params = hashMapOf("code" to "")

        return withContext(Dispatchers.IO) {
                NetworkBroadRepository.postEncrypted(
                    UrlBroadConstants.GET_USER_INFO,
                    UserInfoModel::class.java,
                    params
                )
        }
    }

    suspend fun  getUserSignInfo(): NetworkBroadResult<SignInfoViewModel>{
        val params = emptyMap<String,String>()
         return  withContext(Dispatchers.IO){
             NetworkBroadRepository.postEncrypted(UrlBroadConstants.SIGN_INFO,SignInfoViewModel::class.java,params)

         }
    }
}