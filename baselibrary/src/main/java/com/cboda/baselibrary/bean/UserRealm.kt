package com.cboda.baselibrary.bean

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey


open class UserRealm : RealmObject() {
    @Index
    @PrimaryKey
    var id = ""
    var registerDay = ""
    var grade = ""
    var userImg = ""
    var nickName = ""
    var remarkName = ""
    var isVip: Boolean = false
    var isNiceNum: Boolean = false
    var sex = ""
    var extraJson = ""
}