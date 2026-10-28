package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.message.ConvFolderSyncHelper;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.freddy.im.signal.othbusi.OthBusiSigJson;

/**
 * 会话分组变更：code=1011，
 * data={"kind":"conv_folder","version":8}。
 * 本地 version ≥ 推送值则忽略，否则重拉。
 */
public final class ConvFolderOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.ConvFolder";

    @Override
    public int code() {
        return OthBusiSigCodes.CONV_FOLDER;
    }

    @Override
    public boolean handle(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        String kind = OthBusiSigJson.readString(data, "kind");
        if (!TextUtils.isEmpty(kind) && !"conv_folder".equals(kind)) {
            ELogUtilKt.eLogD(TAG, "ignore kind=" + kind);
            return false;
        }
        long remoteVersion = OthBusiSigJson.readInt(data, "version", 0);
        ImLocalStore.loadConvFolders((localVersion, folders) -> {
            if (localVersion >= remoteVersion && remoteVersion > 0) {
                ELogUtilKt.eLogD(TAG, "skip sync local=" + localVersion + " remote=" + remoteVersion);
                return;
            }
            ELogUtilKt.eLogD(TAG, "sync folders remoteVersion=" + remoteVersion);
            ConvFolderSyncHelper.syncFromServer((v, list) ->
                    MessageProcessor.getInstance().notifyConvFoldersUpdated());
        });
        return true;
    }
}
