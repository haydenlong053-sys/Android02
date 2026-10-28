package com.freddy.im.db.mapper;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.entity.ConvFolderEntity;
import com.freddy.im.http.ImGsonHelper;
import com.freddy.im.http.model.im.ConvFolderItem;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ConvFolderEntity ↔ ConvFolderItem。
 */
public final class ConvFolderRealmMapper {

    private static final Type STRING_LIST = new TypeToken<List<String>>() {
    }.getType();

    private ConvFolderRealmMapper() {
    }

    @NonNull
    public static String sessionIdsToJson(@Nullable List<String> sessionIds) {
        if (sessionIds == null || sessionIds.isEmpty()) {
            return "[]";
        }
        return ImGsonHelper.getGson().toJson(sessionIds);
    }

    @NonNull
    public static List<String> sessionIdsFromJson(@Nullable String json) {
        if (TextUtils.isEmpty(json)) {
            return new ArrayList<>();
        }
        try {
            List<String> list = ImGsonHelper.getGson().fromJson(json, STRING_LIST);
            return list != null ? new ArrayList<>(list) : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public static void fillEntity(@NonNull ConvFolderEntity entity, @NonNull ConvFolderItem item, int sortIndex) {
        entity.setName(item.getName() != null ? item.getName() : "");
        entity.setIconType(item.getIconType());
        entity.setSortIndex(sortIndex);
        entity.setSessionIdsJson(sessionIdsToJson(item.getSessionIds()));
    }

    @NonNull
    public static ConvFolderItem toItem(@NonNull ConvFolderEntity entity) {
        ConvFolderItem item = new ConvFolderItem();
        item.setId(entity.getFolderId());
        item.setName(entity.getName());
        item.setIconType(entity.getIconType());
        item.setSortIndex(entity.getSortIndex());
        item.setSessionIds(sessionIdsFromJson(entity.getSessionIdsJson()));
        return item;
    }

    /** 提交服务端用：仅 id / name / sessionIds。 */
    @NonNull
    public static ConvFolderItem toServerItem(@NonNull ConvFolderItem src) {
        ConvFolderItem item = new ConvFolderItem();
        item.setId(src.getId());
        item.setName(src.getName());
        item.setSessionIds(new ArrayList<>(src.getSessionIds()));
        return item;
    }

    @NonNull
    public static List<ConvFolderItem> toServerList(@Nullable List<ConvFolderItem> list) {
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        List<ConvFolderItem> out = new ArrayList<>(list.size());
        for (ConvFolderItem item : list) {
            if (item != null && !TextUtils.isEmpty(item.getId())) {
                out.add(toServerItem(item));
            }
        }
        return out;
    }
}
