# EasyHttp + Gson：保留 HTTP 模型字段名与 JSON 一致
-keep class com.freddy.im.http.model.** {
    <fields>;
}

# SDK 消息载体（Gson / 反射场景）
-keep class com.freddy.im.bean.AppMessage {
    <fields>;
}

# Realm 实体
-keep class com.freddy.im.db.entity.** {
    <fields>;
}
-keep @io.realm.annotations.RealmClass class * { *; }
