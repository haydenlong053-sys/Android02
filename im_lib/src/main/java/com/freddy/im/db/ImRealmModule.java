package com.freddy.im.db;

import com.freddy.im.db.entity.ConvFolderEntity;
import com.freddy.im.db.entity.ConvFolderMetaEntity;
import com.freddy.im.db.entity.GroupMemberEntity;
import com.freddy.im.db.entity.MessageEntity;
import com.freddy.im.db.entity.PinnedMessageEntity;
import com.freddy.im.db.entity.SessionEntity;

import io.realm.annotations.RealmModule;

@RealmModule(library = true, classes = {
        MessageEntity.class,
        SessionEntity.class,
        GroupMemberEntity.class,
        PinnedMessageEntity.class,
        ConvFolderEntity.class,
        ConvFolderMetaEntity.class
})
public class ImRealmModule {
}
