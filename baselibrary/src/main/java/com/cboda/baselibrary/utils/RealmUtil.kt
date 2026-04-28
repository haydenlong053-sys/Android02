package com.cboda.baselibrary.utils

import android.app.Application
import android.text.TextUtils
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.bean.UserRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.CoroutineScope

object RealmUtil {

    fun addUserData(data: CommonResultBroadBean.CommonResult?) {
        data?.also {
            val bean = FriendBean()
            bean.id = data.id ?: ""
            bean.nickName = data.name
            bean.remarkName = data.remarkName
            bean.registerDay = data.registerDayBean?.registDay?.toString() ?: ""
            bean.grade = data.grade?.toString()
            bean.userImg = data.icon
            bean.isVip = data.isvip == "1"
            bean.isNiceNum = !TextUtils.isEmpty(data.lianghao)
            bean.sex = data.sex
            addUserData(bean)
        }
    }

    fun addUserData(userInfoBean: FriendBean?) {
        userInfoBean?.let {
            if (!TextUtils.isEmpty(userInfoBean.id)) {
//                if (canFind) {
                findUserDataAndUpdateOrAdd(userInfoBean.id, userInfoBean)
//                } else {
//                    getRealm()?.executeTransactionAsync { realm ->
//                        val createUserRealm = realm.createObject(UserRealm::class.java)
//                        addUserData2(userInfoBean, createUserRealm)
//                    }
//                }
            }
        }
    }


    fun addUserData(data: UserInfoBean?) {
        data?.let {
            val bean = FriendBean()
            bean.id = data.id
            bean.userImg = data.userImg
            bean.nickName = data.name
            bean.grade = data.grade
            bean.registerDay = data.registerDayBean?.registrationDays ?: ""
            bean.isVip = data.isVip == "1"
            bean.isNiceNum = !TextUtils.isEmpty(data.niceNumberId)
            bean.sex = data.sex
            addUserData(bean)
        }
    }

    fun addUserData(list: List<FriendBean>?, viewModelScope: CoroutineScope? = null) {
        list?.let {
            list.forEach { item ->
                getRealm()?.executeTransactionAsync { realm ->
                    val data = realm.where(UserRealm::class.java)?.equalTo("id", item.id)?.findFirst()
//                    viewModelScope?.launch {
//                        withContext(Dispatchers.IO) {
////                            withContext(Dispatchers.Main) {
////                            }
//                        }
//                    } ?: let {
//                        GlobalScope.launch(Dispatchers.IO) {
////                            GlobalScope.launch(Dispatchers.Main) {
////
////                            }
//                        }
//                    }
                    val list = arrayListOf<UserRealm>()
                    data?.let {
                        list.add(data)
                    } ?: let {
                        list.add(getUserRealm(item, null))
                    }
                    realm.insertOrUpdate(list)
                }
            }
        }
    }


    fun findUserData(id: String?, callBack: (FriendBean?) -> Unit = {}) {
        findUserData2(id) { data ->
            data?.let {
                val friendBean = FriendBean()
                friendBean.id = it.id
                friendBean.userImg = it.userImg
                friendBean.nickName = it.nickName
                friendBean.isNiceNum = it.isNiceNum
                friendBean.sex = it.sex
                friendBean.isVip = it.isVip
                friendBean.registerDay = it.registerDay
                friendBean.remarkName = it.remarkName
                friendBean.grade = it.grade
                callBack.invoke(friendBean)
                ThreadUtils.runOnUiThread { callBack.invoke(friendBean) }
            } ?: ThreadUtils.runOnUiThread { callBack.invoke(null) }
//            } ?: callBack.invoke(null)
        }
    }


    private fun findUserData2(id: String?, callBack: (UserRealm?) -> Unit = {}) {
        if (!TextUtils.isEmpty(id)) {
            getRealm()?.executeTransactionAsync { realm ->
                val data = realm.where(UserRealm::class.java)?.equalTo("id", id)?.findFirst()
                callBack.invoke(data)
            }
//            val data = getRealm()?.where(UserRealm::class.java)?.equalTo("id", id)?.findFirst()
//            callBack.invoke(data)
        } else {
            callBack.invoke(null)
        }
    }


    private fun findUserDataAndUpdateOrAdd(id: String?, userInfoBean: FriendBean) {
        if (!TextUtils.isEmpty(id)) {
            getRealm()?.executeTransactionAsync { realm ->
                val userRealm = realm.where(UserRealm::class.java)?.equalTo("id", id)?.findFirst()
                userRealm?.let {
                    userInfoBean.isVip?.let {
                        userRealm.isVip = it
                    }
                    if (!TextUtils.isEmpty(userInfoBean.grade)) {
                        userRealm.grade = userInfoBean.grade ?: ""
                    }
                    if (!TextUtils.isEmpty(userInfoBean.userImg)) {
                        userRealm.userImg = userInfoBean.userImg ?: ""
                    }
                    if (!TextUtils.isEmpty(userInfoBean.registerDay)) {
                        userRealm.registerDay = userInfoBean.registerDay ?: ""
                    }
                    if (!TextUtils.isEmpty(userInfoBean.nickName)) {
                        userRealm.nickName = userInfoBean.nickName ?: ""
                    }
                    if (!TextUtils.isEmpty(userInfoBean.remarkName)) {
                        userRealm.remarkName = userInfoBean.remarkName ?: ""
                    }
                    userInfoBean.isNiceNum?.let {
                        userRealm.isNiceNum = it
                    }
                    if (!TextUtils.isEmpty(userInfoBean.sex)) {
                        userRealm.sex = userInfoBean.sex ?: ""
                    }
                } ?: let {
                    val createUserRealm = realm.createObject(UserRealm::class.java, userInfoBean.id)
                    getUserRealm(userInfoBean, createUserRealm)
//                    realm.copyToRealmOrUpdate(getUserRealm(userInfoBean, createUserRealm))
                }
            }
        }
    }

    private fun getUserRealm(userInfoBean: FriendBean?, userRealm: UserRealm?): UserRealm {
        val createUserRealm = userRealm ?: UserRealm()
        userInfoBean?.let {
            if (null == userRealm) {
                createUserRealm.id = userInfoBean.id
            }
            userInfoBean.isVip?.let {
                createUserRealm.isVip = it
            }
            if (!TextUtils.isEmpty(userInfoBean.grade)) {
                createUserRealm.grade = userInfoBean.grade ?: ""
            }
            if (!TextUtils.isEmpty(userInfoBean.userImg)) {
                createUserRealm.userImg = userInfoBean.userImg ?: ""
            }
            if (!TextUtils.isEmpty(userInfoBean.registerDay)) {
                createUserRealm.registerDay = userInfoBean.registerDay ?: ""
            }
            if (!TextUtils.isEmpty(userInfoBean.nickName)) {
                createUserRealm.nickName = userInfoBean.nickName ?: ""
            }
            if (!TextUtils.isEmpty(userInfoBean.remarkName)) {
                createUserRealm.remarkName = userInfoBean.remarkName ?: ""
            }
            userInfoBean.isNiceNum?.let {
                createUserRealm.isNiceNum = it
            }
            if (!TextUtils.isEmpty(userInfoBean.sex)) {
                createUserRealm.sex = userInfoBean.sex ?: ""
            }
        }
        return createUserRealm
    }

    private var mRealm: Realm? = null
    private fun getRealm(): Realm? {
        return if (null != mRealm) {
            mRealm
        } else {
            mRealm = Realm.getDefaultInstance()
            mRealm
        }
    }

    fun initRealm(application: Application) {
        Realm.init(application)
        val config = RealmConfiguration.Builder().name("seek_web_four.realm").schemaVersion(1L).build()
        Realm.setDefaultConfiguration(config)
    }

}