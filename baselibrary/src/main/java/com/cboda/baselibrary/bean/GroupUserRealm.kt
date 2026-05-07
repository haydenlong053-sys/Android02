package com.cboda.baselibrary.bean

import io.realm.RealmObject
import io.realm.annotations.Index
import io.realm.annotations.PrimaryKey

open class GroupUserRealm : RealmObject() {
    @Index
    @PrimaryKey
    var id = ""
    var groupNickName = ""//用户在群内的昵称
    var joinTime = 0L//入群时间

    var extraJson = ""
}