package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.anyToInt
import com.blankj.utilcode.util.GsonUtils
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.bean.CommentListBean
import com.cboda.intelligent.bean.ReportConfigBean
import com.cboda.intelligent.bean.SquareHotConfigBean
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.bean.SquareFollowerUserBean
import com.cboda.intelligent.bean.SquareMessageBean
import com.cboda.intelligent.bean.SquareUserProfileBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.model.SignInfoViewBroadModel
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleverBodyViewBroadModel : BaseViewBroadModel() {

    fun getSignInfo(): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.SIGN_INFO,
                        SignInfoViewBroadModel::class.java, HashMap<String, Any>()
                    )
                    result.onSuccess { data ->
                        liveData.postValue(1 == data?.isSign)
                    }.onFailure { _, _ ->
                        liveData.postValue(false)
                    }.onError { _ ->
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

//    fun getUserInfo(): SingleLiveBroadData<UserInfoBroadModel?> {
//        val liveData = SingleLiveBroadData<UserInfoBroadModel?>()
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    val result = NetworkBroadRepository.postEncrypted(
//                        UrlBroadBroadConstants.GET_USER_INFO,
//                        UserInfoBroadModel::class.java, HashMap<String, Any>()
//                    )
//                    result.onSuccess { data ->
//                        data?.let {
//                            val bean = GlobalFriendBroadBean()
//                            bean.userId = data.id
//                            bean.icon = data.icon
//                            bean.nickName = data.name
//                            bean.grade = data.grade
//                            bean.registerDay = data.registerDayBean?.registDay
//                            bean.isVip = data.isvip == "1"
//                            bean.isNiceNum = !TextUtils.isEmpty(data.lianghao)
//                            MMKVBroadUtils.saveUserData(bean)
//                        }
//                        liveData.postValue(data)
//                    }.onFailure { _, _ ->
//                        liveData.postValue(null)
//                    }.onError { _ ->
//                        liveData.postValue(null)
//                    }
//                } catch (_: Exception) {
//                    liveData.postValue(null)
//                }
//            }
//        }
//        return liveData
//    }

    /**
     * 广场帖子列表（分页）
     * tabType: 0=世界 1=热门 2=我的关注
     */
    fun getSquarePostList(
        tabType: Int,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize_20,
    ): SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>>()
        val params = hashMapOf<String, Any>(
            "listType" to tabType.toString(),
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_POST_LIST,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, _ ->
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }.onError {
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }
                } catch (_: Exception) {
//                    liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                }
            }
        }
        return liveData
    }

    /**
     * 朋友圈列表（分页）
     * tabType: 0=世界 1=热门 2=我的关
     */
    fun getFriendPostList(
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize_20,
    ): SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>>()
        val params = hashMapOf<String, Any>(
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.FRIEND_POST_LIST,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, _ ->
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }.onError {
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }
                } catch (_: Exception) {
//                    liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                }
            }
        }
        return liveData
    }

    /** 广场帖子详情 */
    fun getSquarePostDetail(postId: String): SingleLiveBroadData<SquarePostBean?> {
        val liveData = SingleLiveBroadData<SquarePostBean?>()
        val params = hashMapOf<String, Any>("id" to anyToInt(postId).toString())
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.SQUARE_POST_DETAIL,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(result)
                    }.onFailure { _, _ ->
//                        liveData.postValue(buildMockSquarePostDetail(postId))
                    }.onError {
//                        liveData.postValue(buildMockSquarePostDetail(postId))
                    }
                } catch (_: Exception) {
//                    liveData.postValue(buildMockSquarePostDetail(postId))
                }
            }
        }
        return liveData
    }

    /** 广场帖子浏览上报（无感调用，不提示成功/失败） */
    fun recordSquarePostView(postId: String) {
        if (postId.isBlank()) return
        val params = hashMapOf<String, Any>("id" to anyToInt(postId).toString())
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_POST_VIEW,
                        Any::class.java,
                        params,
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    /** 朋友圈动态浏览上报（无感调用，不提示成功/失败） */
    fun recordMomentPostView(postId: String) {
        if (postId.isBlank()) return
        val params = hashMapOf<String, Any>("id" to anyToInt(postId.trim()).toString())
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.MOMENT_POST_VIEW,
                        Any::class.java,
                        params,
                    )
                } catch (_: Exception) {
                }
            }
        }
    }

    /** 朋友圈动态详情 */
    fun getMomentPostDetail(postId: String): SingleLiveBroadData<SquarePostBean?> {
        val liveData = SingleLiveBroadData<SquarePostBean?>()
        val params = hashMapOf<String, Any>("id" to anyToInt(postId.trim()).toString())
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.MOMENT_POST_DETAIL,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(result)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 广场帖子评论列表（分页） */
    fun getSquareCommentList(
        postId: String,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize_20,
    ): SingleLiveBroadData<NetworkListBroadResult<CommentListBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<CommentListBean>>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId).toString(),
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_POST_COMMENT_LIST,
                        CommentListBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, _ ->
//                        liveData.postValue(buildMockSquareCommentList(pageNo, pageSize))
                    }.onError {
//                        liveData.postValue(buildMockSquareCommentList(pageNo, pageSize))
                    }
                } catch (_: Exception) {
//                    liveData.postValue(buildMockSquareCommentList(pageNo, pageSize))
                }
            }
        }
        return liveData
    }

    /** 广场帖子发表评论 */
    fun postSquareComment(
        postId: String,
        content: String,
        parentId: String = "0",
    ): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId).toString(),
            "content" to content,
            "parentId" to if (parentId.isBlank() || parentId == "0") {
                "0"
            } else {
                anyToInt(parentId).toString()
            },
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_POST_COMMENT,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 朋友圈动态发表评论 */
    fun postMomentComment(
        postId: String,
        content: String,
        parentId: String = "0",
    ): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId.trim()).toString(),
            "content" to content,
            "parentId" to if (parentId.isBlank() || parentId == "0") {
                "0"
            } else {
                anyToInt(parentId.trim()).toString()
            },
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.MOMENT_POST_COMMENT,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 广场帖子点赞/取消点赞 */
    fun likeSquarePost(postId: String, like: Boolean): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId).toString(),
            "type" to if (like) "1" else "0",
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_POST_LIKE,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 删除动态（广场/朋友圈） */
    fun deleteMomentPost(postId: String): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId.trim()).toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.MOMENT_POST_DELETE,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** @see deleteMomentPost */
    fun deleteSquarePost(postId: String): SingleLiveBroadData<Boolean> = deleteMomentPost(postId)

    /** 朋友圈动态置顶/取消置顶 */
    fun pinMomentPost(postId: String, pin: Boolean): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId.trim()).toString(),
            "type" to if (pin) "1" else "0",
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.MOMENT_POST_PIN,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 广场帖子置顶/取消置顶 */
    fun topSquarePost(postId: String, top: Boolean): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId.trim()).toString(),
            "type" to if (top) "1" else "0",
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_POST_PIN,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 朋友圈动态点赞/取消点赞 */
    fun likeMomentPost(postId: String, like: Boolean): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "id" to anyToInt(postId).toString(),
            "type" to if (like) "1" else "0",
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.MOMENT_POST_LIKE,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 广场热门/发布配置 */
    fun getSquareHotConfig(): SingleLiveBroadData<SquareHotConfigBean?> {
        val liveData = SingleLiveBroadData<SquareHotConfigBean?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedFlat2(
                        UrlBroadBroadConstants.SQUARE_HOT_CONFIG,
                        hashMapOf<String, Any>(),
                    ).onSuccess { map ->
                        liveData.postValue(parseSquareHotConfig(map))
                    }.onFailure { _, _ ->
                        liveData.postValue(null)
                    }.onError {
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    private fun parseSquareHotConfig(map: Map<String, Any?>?): SquareHotConfigBean? {
        return try {
            GsonUtils.getGson().fromJson(
                GsonUtils.getGson().toJson(map),
                SquareHotConfigBean::class.java,
            )
        } catch (_: Exception) {
            null
        }
    }

    /** 用户主页信息 */
    /** 用户主页广场帖子列表（分页） */
    fun getSquareUserPostList(
        userId: String,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize,
    ): SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>>()
        val params = hashMapOf<String, Any>(
            "userId" to userId,
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_USER_LIST,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, _ ->
                    }.onError {
                    }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 用户主页朋友圈列表（分页） */
    fun getMomentUserPostList(
        userId: String,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize,
    ): SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>>()
        val params = hashMapOf<String, Any>(
            "userId" to userId,
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.MOMENT_USER_LIST,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, _ ->
                    }.onError {
                    }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 关注/取关用户，type：1=关注，0=取消 */
    fun followUser(userId: String, follow: Boolean): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "userId" to userId,
            "type" to if (follow) "1" else "0",
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_USER_FOLLOW,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(false)
                    }.onError {
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    /** 拉黑/解除拉黑用户，type：1=拉黑，0=解除 */
    fun blackUser(userId: String, black: Boolean): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "userId" to userId,
            "type" to if (black) "1" else "0",
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_USER_BLACK,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(false)
                    }.onError {
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    /** 用户粉丝列表（分页） */
    fun getFollowerList(
        userId: String?,
        keyword: String,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize,
    ): SingleLiveBroadData<NetworkListBroadResult<SquareFollowerUserBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquareFollowerUserBean>>()
        val params = hashMapOf<String, Any>(
            "keyword" to keyword,
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        if (!userId.isNullOrBlank()) {
            params["userId"] = userId
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_USER_FOLLOWER_LIST,
                        SquareFollowerUserBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError {
                    }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 用户关注列表（分页） */
    fun getFollowingList(
        userId: String?,
        keyword: String,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize,
    ): SingleLiveBroadData<NetworkListBroadResult<SquareFollowerUserBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquareFollowerUserBean>>()
        val params = hashMapOf<String, Any>(
            "keyword" to keyword,
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        if (!userId.isNullOrBlank()) {
            params["userId"] = userId
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_USER_FOLLOWING_LIST,
                        SquareFollowerUserBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError {
                    }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 粉丝黑名单列表（分页） */
    fun getBlacklistList(
        keyword: String,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize,
    ): SingleLiveBroadData<NetworkListBroadResult<SquareFollowerUserBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquareFollowerUserBean>>()
        val params = hashMapOf<String, Any>(
            "keyword" to keyword,
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_USER_BLACKLIST_LIST,
                        SquareFollowerUserBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError {
                    }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    fun getUserProfile(userId: String): SingleLiveBroadData<SquareUserProfileBean?> {
        val liveData = SingleLiveBroadData<SquareUserProfileBean?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_USER_PROFILE,
                        SquareUserProfileBean::class.java,
                        hashMapOf("userId" to userId),
                    ).onSuccess { profile ->
                        liveData.postValue(profile)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(null)
                    }.onError {
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }

    /** 是否已设置支付密码 */
    fun checkPayPasswordSet(): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.GET_USER_INFO,
                        UserInfoBean::class.java,
                        HashMap<String, Any>(),
                    ).onSuccess { userInfo ->
                        liveData.postValue(userInfo?.hasPayPwd == "1")
                    }.onFailure { _, _ ->
                        liveData.postValue(false)
                    }.onError {
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    /** 发布广场帖子 */
    fun publishSquarePost(
        content: String,
        imageUrls: String,
        odicStake: String,
        payPwd: String,
        video: String = "",
        commentGateTypes: String = "1",
        commentCreditMin: String = "",
        quoteRefType: String = "",
        quoteRefId: String = "",
    ): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "content" to content,
            "imageUrls" to imageUrls,
            "video" to video,
//            "odicStake" to anyToInt(odicStake).toString(),
            "payPwd" to payPwd,
            "commentGateTypes" to commentGateTypes,
            "commentCreditMin" to commentCreditMin,
            "quoteRefType" to quoteRefType,
            "quoteRefId" to anyToInt(quoteRefId).toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_POST_PUBLISH,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(false)
                    }.onError {
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    /** 发布朋友圈 */
    fun publishMomentPost(
        content: String,
        imageUrls: String,
        quoteRefType: String = "",
        quoteRefId: String = "",
    ): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "content" to content,
            "imageUrls" to imageUrls,
            "quoteRefType" to quoteRefType,
            "quoteRefId" to anyToInt(quoteRefId).toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.MOMENT_POST_PUBLISH,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(false)
                    }.onError {
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    /** 举报类型列表 */
    fun getReportConfigList(targetType: String): SingleLiveBroadData<List<ReportConfigBean>> {
        val liveData = SingleLiveBroadData<List<ReportConfigBean>>()
        val params = hashMapOf<String, Any>("targetType" to targetType)
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_REPORT_CONFIG_LIST,
                        ReportConfigBean::class.java,
                        params,
                    ).onSuccess { result ->
                        val list = result?.list.orEmpty().filter { it.displayTitle().isNotBlank() }
                        liveData.postValue(list)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(emptyList())
                    }.onError {
                        liveData.postValue(emptyList())
                    }
                } catch (_: Exception) {
                    liveData.postValue(emptyList())
                }
            }
        }
        return liveData
    }

    /** 提交举报 */
    fun submitReport(
        targetType: String,
        targetId: String,
        reportConfigId: String,
        reason: String,
        imageUrls: List<String>,
    ): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>(
            "targetType" to targetType,
            "targetId" to anyToInt(targetId).toString(),
            "reportConfigId" to anyToInt(reportConfigId).toString(),
            "reason" to reason,
            "imageUrls" to GsonUtils.getGson().toJson(imageUrls),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_REPORT_SUBMIT,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError { }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    private fun buildMockSquarePostDetail(postId: String): SquarePostBean {
        return SquarePostBean(
            id = postId,
            name = "JOY Follow",
            sign = "生活不止远方的苟且，还是今天和明天",
            icon = "",
            grade = "78",
            registDay = "365",
            time = "2026-09-12 10:30:00",
            content = "就是单纯的想发个朋友圈看看哈有多少人在关注我哈有多少人在关注我哈有多少人在关注我哈有多少人在关注我哈...",
            imgs = arrayOf(
                "https://picsum.photos/400/400?random=11",
                "https://picsum.photos/400/400?random=12",
                "https://picsum.photos/400/400?random=13",
            ),
            viewCount = "2",
            commentCount = "2",
            likeCount = "3",
            quoteCount = "0",
            odicStakeStatus = "1",
        )
    }

    private fun buildMockSquareCommentList(
        pageNo: Int,
        pageSize: Int,
    ): NetworkListBroadResult<CommentListBean> {
        val allComments = listOf(
            CommentListBean(
                id = 1001,
                name = "堂吉诃德的一生正气天气",
                content = "地方不错，我想去了",
                time = "2026-09-12 08:30:00",
                grade = "78",
                registDay = "120",
            ),
            CommentListBean(
                id = 1002,
                name = "阿梅",
                sign = "堂吉诃德的一生正气天气",
                content = "在大东山这边还可以用哦，在大东山这边还可以用哦在大东山这边还可以用哦",
                time = "2026-09-12 12:25:00",
                grade = "78",
                registDay = "88",
            ),
        )
        val fromIndex = (pageNo - 1) * pageSize
        if (fromIndex >= allComments.size) {
            return NetworkListBroadResult(emptyList(), allComments.size, 1)
        }
        val toIndex = (fromIndex + pageSize).coerceAtMost(allComments.size)
        val pageList = allComments.subList(fromIndex, toIndex)
        val totalPage = ((allComments.size + pageSize - 1) / pageSize).coerceAtLeast(1)
        return NetworkListBroadResult(pageList, allComments.size, totalPage)
    }

    /** 消息中心列表（分页） */
    fun getMessageList(
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize,
    ): SingleLiveBroadData<NetworkListBroadResult<SquareMessageBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquareMessageBean>>()
        val params = hashMapOf<String, Any>(
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_USER_MESSAGE_LIST,
                        SquareMessageBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, message ->
                        showToastMain(message)
                    }.onError {
                    }
                } catch (_: Exception) {
                }
            }
        }
        return liveData
    }

    /** 消息中心未读数（与 success 同级字段 unreadCount） */
    fun getMessageUnreadCount(): SingleLiveBroadData<Int> {
        val liveData = SingleLiveBroadData<Int>()
        val params = hashMapOf<String, Any>(
            "pageNo" to "1",
            "pageSize" to "1",
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedFlat2(
                        UrlBroadBroadConstants.SQUARE_USER_MESSAGE_LIST,
                        params,
                    ).onSuccess { map ->
                        liveData.postValue(anyToInt(map?.get("unreadCount")).coerceAtLeast(0))
                    }.onFailure { _, _ ->
                        liveData.postValue(0)
                    }.onError {
                        liveData.postValue(0)
                    }
                } catch (_: Exception) {
                    liveData.postValue(0)
                }
            }
        }
        return liveData
    }

    /** 消息中心标记已读，传 id 标记单条，不传则全部已读 */
    fun readMessage(messageId: String? = null): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>()
        messageId?.trim()?.takeIf { it.isNotEmpty() }?.let { params["id"] = anyToInt(it).toString() }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_USER_MESSAGE_READ_ALL,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(false)
                    }.onError {
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    /** 消息中心删除消息，传 id 删单条，不传则清空全部 */
    fun deleteMessage(messageId: String? = null): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any>()
        messageId?.trim()?.takeIf { it.isNotEmpty() }?.let { params["id"] = it }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncrypted2(
                        UrlBroadBroadConstants.SQUARE_USER_DELETE_MESSAGE,
                        Any::class.java,
                        params,
                    ).onSuccess {
                        liveData.postValue(true)
                    }.onFailure { _, message ->
                        showToastMain(message)
                        liveData.postValue(false)
                    }.onError {
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }
}