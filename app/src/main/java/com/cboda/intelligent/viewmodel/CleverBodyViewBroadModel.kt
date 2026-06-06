package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.bean.CommentListBean
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.model.SignInfoViewBroadModel
import com.cboda.intelligent.utils.UrlBroadBroadConstants
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
        val params = hashMapOf<String, Any>("id" to postId)
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

    /** 广场帖子评论列表（分页） */
    fun getSquareCommentList(
        postId: String,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize_20,
    ): SingleLiveBroadData<NetworkListBroadResult<CommentListBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<CommentListBean>>()
        val params = hashMapOf<String, Any>(
            "postId" to postId,
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

//    private fun buildMockSquarePostDetail(postId: String): SquarePostBean {
//        return SquarePostBean(
//            id = postId,
//            name = "JOY Follow",
//            sign = "生活不止远方的苟且，还是今天和明天",
//            icon = "",
//            grade = "78",
//            registDay = "365",
//            time = "2026-09-12 10:30:00",
//            content = "就是单纯的想发个朋友圈看看哈有多少人在关注我",
//            imgs = arrayOf(
//                "https://picsum.photos/400/400?random=11",
//                "https://picsum.photos/400/400?random=12",
//                "https://picsum.photos/400/400?random=13",
//            ),
//            viewCount = "2",
//            commentCount = "2",
//            likeCount = "3",
//            quoteCount = "0",
//            odicStakeStatus = "1",
//        )
//    }

//    private fun buildMockSquareCommentList(
//        pageNo: Int,
//        pageSize: Int,
//    ): NetworkListBroadResult<CommentListBean> {
//        val allComments = listOf(
//            CommentListBean(
//                name = "堂吉诃德的一生正气天气",
//                content = "我也要去看看",
//                time = "2026-09-12 08:30:00",
//                grade = "78",
//                registDay = "120",
//            ),
//            CommentListBean(
//                name = "阿梅",
//                sign = "堂吉诃德的一生正气天气",
//                content = "在大东山这边还可以用哦，在大东山这边还可以用哦在大东山这边还可以用哦",
//                time = "2026-09-12 12:25:00",
//                grade = "78",
//                registDay = "88",
//            ),
//            CommentListBean(
//                name = "王德",
//                content = "我也要去看看",
//                time = "2026-09-12 11:10:00",
//                grade = "65",
//                registDay = "30",
//            ),
//            CommentListBean(
//                name = "陈冉",
//                content = "这是真的吗",
//                time = "2026-09-12 12:58:00",
//                grade = "72",
//                registDay = "66",
//            ),
//        )
//        val fromIndex = (pageNo - 1) * pageSize
//        if (fromIndex >= allComments.size) {
//            return NetworkListBroadResult(emptyList(), allComments.size, 1)
//        }
//        val toIndex = (fromIndex + pageSize).coerceAtMost(allComments.size)
//        val pageList = allComments.subList(fromIndex, toIndex)
//        val totalPage = ((allComments.size + pageSize - 1) / pageSize).coerceAtLeast(1)
//        return NetworkListBroadResult(pageList, allComments.size, totalPage)
//    }
}