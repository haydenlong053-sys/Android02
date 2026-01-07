package com.cboda.intelligent.repository

import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.NetworkBroadResult
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.model.SignInfoViewBroadModel
import com.cboda.intelligent.model.UserInfoBroadModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MirrorBroadRepository {

    suspend fun getUserMirrorInfo(): NetworkBroadResult<UserInfoBroadModel> {
        val params = hashMapOf("code" to "")

        return withContext(Dispatchers.IO) {
                NetworkBroadRepository.postEncrypted(
                    UrlBroadBroadConstants.GET_USER_INFO,
                    UserInfoBroadModel::class.java,
                    params
                )
        }
    }

    suspend fun  getUserSignInfo(): NetworkBroadResult<SignInfoViewBroadModel>{
        val params = emptyMap<String,String>()
         return  withContext(Dispatchers.IO){
             NetworkBroadRepository.postEncrypted(UrlBroadBroadConstants.SIGN_INFO,SignInfoViewBroadModel::class.java,params)

         }
    }
}