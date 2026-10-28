package com.freddy.im.message;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.mapper.ConvFolderRealmMapper;
import com.freddy.im.http.ImConversationHttpExample;
import com.freddy.im.http.model.im.ConvFolderItem;
import com.freddy.im.http.model.im.ConvFoldersContent;
import com.freddy.im.listener.OnHttpImListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 会话分组同步：拉取落库；整份保存 + 3048 乐观锁自动重试一次。
 */
public final class ConvFolderSyncHelper {

    private static final String TAG = "ConvFolderSync";
    public static final int MAX_FOLDERS = 20;
    public static final int MAX_SESSIONS_PER_FOLDER = 1000;
    public static final int MAX_NAME_LEN = 12;
    /** 服务端业务码：分组已在其他设备修改。 */
    public static final int ERR_VERSION_CONFLICT = 3048;

    public interface OnSynced {
        void onSynced(long version, @NonNull List<ConvFolderItem> folders);
    }

    public interface OnSaveResult {
        void onSuccess(long version, @NonNull List<ConvFolderItem> folders);

        void onFail(int errorCode, @Nullable String errorMsg);
    }

    /**
     * 在当前全量 folders 上应用修改，返回新列表（勿改入参）。
     */
    public interface FolderMutator {
        @NonNull
        List<ConvFolderItem> mutate(@NonNull List<ConvFolderItem> current);
    }

    private ConvFolderSyncHelper() {
    }

    @NonNull
    public static String newFolderId() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static void syncFromServer(@Nullable OnSynced onSynced) {
        ImConversationHttpExample.requestGetFolders(new OnHttpImListener<ConvFoldersContent>() {
            @Override
            public void onHttpImSuccess(@NonNull ConvFoldersContent result) {
                applyServerContent(result, onSynced);
            }
        }, () -> ImLocalStore.loadConvFolders((version, list) -> {
            if (onSynced != null) {
                onSynced.onSynced(version, list);
            }
        }));
    }

    /**
     * 本地加载 → mutator → POST saveFolders；遇 3048 则重拉后重放 mutator 再存一次。
     */
    public static void saveWithRetry(@NonNull FolderMutator mutator, @Nullable OnSaveResult onResult) {
        ImLocalStore.loadConvFolders((version, current) ->
                doSave(version, mutator.mutate(copyList(current)), mutator, true, onResult));
    }

    private static void doSave(long version,
                               @NonNull List<ConvFolderItem> next,
                               @NonNull FolderMutator mutator,
                               boolean allowRetry,
                               @Nullable OnSaveResult onResult) {
        if (next.size() > MAX_FOLDERS) {
            if (onResult != null) {
                onResult.onFail(3051, "最多创建 20 个分组");
            }
            return;
        }
        for (ConvFolderItem f : next) {
            if (f == null) {
                continue;
            }
            if (TextUtils.isEmpty(f.getId()) || f.getId().length() > 64) {
                if (onResult != null) {
                    onResult.onFail(3049, "分组数据不合法");
                }
                return;
            }
            String name = f.getName() != null ? f.getName().trim() : "";
            if (TextUtils.isEmpty(name) || name.length() > MAX_NAME_LEN) {
                if (onResult != null) {
                    onResult.onFail(3050, "分组名称不能为空，且不超过 " + MAX_NAME_LEN + " 个字");
                }
                return;
            }
            f.setName(name);
            if (f.getSessionIds().size() > MAX_SESSIONS_PER_FOLDER) {
                if (onResult != null) {
                    onResult.onFail(3052, "每个分组最多 1000 个会话");
                }
                return;
            }
        }

        List<ConvFolderItem> serverBody = ConvFolderRealmMapper.toServerList(next);
        // 保留本地 icon，落库用 next
        final List<ConvFolderItem> localSnapshot = copyList(next);

        ImConversationHttpExample.requestSaveFolders(version, serverBody,
                new OnHttpImListener<ConvFoldersContent>() {
                    @Override
                    public void onHttpImSuccess(@NonNull ConvFoldersContent result) {
                        // 用服务端返回 + 本地 icon 合并
                        mergeIcons(result.getFolders(), localSnapshot);
                        applyServerContent(result, (v, list) -> {
                            if (onResult != null) {
                                onResult.onSuccess(v, list);
                            }
                        });
                    }
                },
                (errorCode, errorMsg) -> {
                    if (errorCode == ERR_VERSION_CONFLICT && allowRetry) {
                        Log.w(TAG, "version conflict 3048, reload and retry once");
                        ImConversationHttpExample.requestGetFolders(
                                new OnHttpImListener<ConvFoldersContent>() {
                                    @Override
                                    public void onHttpImSuccess(@NonNull ConvFoldersContent result) {
                                        applyServerContent(result, (v, list) ->
                                                doSave(v, mutator.mutate(copyList(list)),
                                                        mutator, false, onResult));
                                    }
                                },
                                () -> {
                                    if (onResult != null) {
                                        onResult.onFail(errorCode, errorMsg);
                                    }
                                });
                        return;
                    }
                    if (onResult != null) {
                        onResult.onFail(errorCode, errorMsg);
                    }
                });
    }

    private static void applyServerContent(@NonNull ConvFoldersContent content,
                                           @Nullable OnSynced onSynced) {
        ImLocalStore.replaceConvFolders(content.getVersion(), content.getFolders(), (v, list) -> {
            if (onSynced != null) {
                onSynced.onSynced(v, list);
            }
        });
    }

    private static void mergeIcons(@Nullable List<ConvFolderItem> server,
                                   @NonNull List<ConvFolderItem> local) {
        if (server == null || server.isEmpty()) {
            return;
        }
        for (ConvFolderItem s : server) {
            if (s == null || TextUtils.isEmpty(s.getId())) {
                continue;
            }
            for (ConvFolderItem l : local) {
                if (l != null && s.getId().equals(l.getId())) {
                    s.setIconType(l.getIconType());
                    break;
                }
            }
        }
    }

    @NonNull
    private static List<ConvFolderItem> copyList(@NonNull List<ConvFolderItem> src) {
        List<ConvFolderItem> out = new ArrayList<>(src.size());
        for (ConvFolderItem item : src) {
            if (item != null) {
                out.add(item.copy());
            }
        }
        return out;
    }
}
