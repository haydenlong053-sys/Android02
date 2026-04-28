package com.cboda.baselibrary.utils

import android.app.Application
import android.text.TextUtils
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.SocialContact
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.bean.UserInfoBroadModel
import com.cboda.baselibrary.bean.UserRealm
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.coroutines.CoroutineScope

object RealmUtil {

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
                ThreadUtils.getMainHandler().post { callBack.invoke(friendBean) }
//                ThreadUtils.runOnUiThread { callBack.invoke(friendBean) }
//            } ?: ThreadUtils.runOnUiThread { callBack.invoke(null) }
            } ?: ThreadUtils.getMainHandler().post { callBack.invoke(null) }
//            } ?: callBack.invoke(null)
        }
    }
    fun addUserData(list: List<SocialContact>?, viewModelScope: CoroutineScope? = null) {
        list?.let {
            if (list.isNotEmpty()) {
                list.forEach { item ->
                    getRealm()?.executeTransactionAsync { realm ->
                        val data = realm.where(UserRealm::class.java)?.equalTo("id", item.userId)?.findFirst()
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
                            list.add(getUserRealm(item))
                        }
                        realm.insertOrUpdate(list)
                    }
                }
            }
        }
    }

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

    fun addUserData(friendBean: FriendBean?) {
        friendBean?.let {
            if (!TextUtils.isEmpty(friendBean.id)) {
//                if (canFind) {
                findUserDataAndUpdateOrAdd(friendBean.id, friendBean)
//                } else {
//                    getRealm()?.executeTransactionAsync { realm ->
//                        val createUserRealm = realm.createObject(UserRealm::class.java)
//                        addUserData2(friendBean, createUserRealm)
//                    }
//                }
            }
        }
    }

    fun addUserData(data: UserInfoBroadModel?) {
        data?.let {
            val bean = FriendBean()
            bean.id = data.id ?: ""
            bean.userImg = data.icon
            bean.nickName = data.name
            bean.grade = data.grade.toString()
            bean.registerDay = data.registerDayBean?.registDay?.toString() ?: ""
            bean.isVip = data.isvip == "1"
            bean.isNiceNum = !TextUtils.isEmpty(data.lianghao)
            bean.sex = data.sex
            addUserData(bean)
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

//    fun addUserData(list: List<FriendBean>?, viewModelScope: CoroutineScope? = null) {
//        list?.let {
//            list.forEach { item ->
//                getRealm()?.executeTransactionAsync { realm ->
//                    val data = realm.where(UserRealm::class.java)?.equalTo("id", item.id)?.findFirst()
////                    viewModelScope?.launch {
////                        withContext(Dispatchers.IO) {
//////                            withContext(Dispatchers.Main) {
//////                            }
////                        }
////                    } ?: let {
////                        GlobalScope.launch(Dispatchers.IO) {
//////                            GlobalScope.launch(Dispatchers.Main) {
//////
//////                            }
////                        }
////                    }
//                    val list = arrayListOf<UserRealm>()
//                    data?.let {
//                        list.add(data)
//                    } ?: let {
//                        list.add(getUserRealm(item, null))
//                    }
//                    realm.insertOrUpdate(list)
//                }
//            }
//        }
//    }

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


    private fun findUserDataAndUpdateOrAdd(id: String?, friendBean: FriendBean) {
        if (!TextUtils.isEmpty(id)) {
            getRealm()?.executeTransactionAsync { realm ->
                val userRealm = realm.where(UserRealm::class.java)?.equalTo("id", id)?.findFirst()
                userRealm?.let {
                    friendBean.isVip?.let {
                        userRealm.isVip = it
                    }
                    if (!TextUtils.isEmpty(friendBean.grade)) {
                        userRealm.grade = friendBean.grade ?: ""
                    }
                    if (!TextUtils.isEmpty(friendBean.userImg)) {
                        userRealm.userImg = friendBean.userImg ?: ""
                    }
                    if (!TextUtils.isEmpty(friendBean.registerDay)) {
                        userRealm.registerDay = friendBean.registerDay ?: ""
                    }
                    if (!TextUtils.isEmpty(friendBean.nickName)) {
                        userRealm.nickName = friendBean.nickName ?: ""
                    }
                    if (!TextUtils.isEmpty(friendBean.remarkName)) {
                        userRealm.remarkName = friendBean.remarkName ?: ""
                    }
                    friendBean.isNiceNum?.let {
                        userRealm.isNiceNum = it
                    }
                    if (!TextUtils.isEmpty(friendBean.sex)) {
                        userRealm.sex = friendBean.sex ?: ""
                    }
                } ?: let {
                    val createUserRealm = realm.createObject(UserRealm::class.java, friendBean.id)
                    getUserRealm(friendBean, createUserRealm)
//                    realm.copyToRealmOrUpdate(getUserRealm(friendBean, createUserRealm))
                }
            }
        }
    }


    private fun getUserRealm(friendBean: SocialContact?): UserRealm {
        val createUserRealm = UserRealm()
        friendBean?.let {
            createUserRealm.id = friendBean.userId
            createUserRealm.isVip = "1" == friendBean.isvip
            if (!TextUtils.isEmpty(friendBean.grade)) {
                createUserRealm.grade = friendBean.grade ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.icon)) {
                createUserRealm.userImg = friendBean.icon ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.registerDay)) {
                createUserRealm.registerDay = friendBean.registerDay ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.nickName)) {
                createUserRealm.nickName = friendBean.nickName ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.remarkName)) {
                createUserRealm.remarkName = friendBean.remarkName ?: ""
            }
            createUserRealm.isNiceNum = !TextUtils.isEmpty(friendBean.lianghao)
            if (!TextUtils.isEmpty(friendBean.sex)) {
                createUserRealm.sex = friendBean.sex ?: ""
            }
        }
        return createUserRealm
    }

    private fun getUserRealm(friendBean: FriendBean?, userRealm: UserRealm?): UserRealm {
        val createUserRealm = userRealm ?: UserRealm()
        friendBean?.let {
            if (null == userRealm) {
                createUserRealm.id = friendBean.id
            }
            friendBean.isVip?.let {
                createUserRealm.isVip = it
            }
            if (!TextUtils.isEmpty(friendBean.grade)) {
                createUserRealm.grade = friendBean.grade ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.userImg)) {
                createUserRealm.userImg = friendBean.userImg ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.registerDay)) {
                createUserRealm.registerDay = friendBean.registerDay ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.nickName)) {
                createUserRealm.nickName = friendBean.nickName ?: ""
            }
            if (!TextUtils.isEmpty(friendBean.remarkName)) {
                createUserRealm.remarkName = friendBean.remarkName ?: ""
            }
            friendBean.isNiceNum?.let {
                createUserRealm.isNiceNum = it
            }
            if (!TextUtils.isEmpty(friendBean.sex)) {
                createUserRealm.sex = friendBean.sex ?: ""
            }
        }
        return createUserRealm
    }

    //    private var mRealm: Realm? = null
    private fun getRealm(): Realm? {
//        return if (null != mRealm) {
//            mRealm
//        } else {
//            mRealm = Realm.getDefaultInstance()
//            mRealm
//        }
        return Realm.getDefaultInstance()
    }

    fun initRealm(application: Application) {
        Realm.init(application)
        val config = RealmConfiguration.Builder().name("seek_web_four.realm").schemaVersion(1L).build()
        Realm.setDefaultConfiguration(config)
    }

}