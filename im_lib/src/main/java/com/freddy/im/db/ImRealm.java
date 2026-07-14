package com.freddy.im.db;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Realm 多用户分库：每个登录 {@code userId} 对应独立 {@code .realm} 文件（方案 A）。
 * <p>
 * 调用顺序：{@link #init(Context)}（Application 一次）→ {@link #openForUser(String)}（登录/切号）→ {@link #getRealm()}。
 */
public final class ImRealm {

    private static final String DB_NAME_PREFIX = "im_sdk_";
    private static final String DB_NAME_SUFFIX = ".realm";
    private static final long SCHEMA_VERSION = 5L;

    private static volatile Context appContext;
    private static volatile boolean sdkInitialized;
    @Nullable
    private static volatile String currentUserId;
    @Nullable
    private static volatile RealmConfiguration activeUserConfig;

    private ImRealm() {
    }

    /**
     * 仅初始化 Realm SDK，不打开任何用户库。
     */
    public static void init(@NonNull Context context) {
        if (sdkInitialized) {
            return;
        }
        synchronized (ImRealm.class) {
            if (sdkInitialized) {
                return;
            }
            appContext = context.getApplicationContext();
            Realm.init(appContext);
            sdkInitialized = true;
        }
    }

    public static boolean isInitialized() {
        return sdkInitialized;
    }

    /**
     * 已 {@link #openForUser(String)} 且可读写。
     */
    public static boolean isUserBound() {
        return sdkInitialized && !TextUtils.isEmpty(currentUserId);
    }

    @Nullable
    public static String getCurrentUserId() {
        return currentUserId;
    }

    /**
     * 切换/登录用户：排空写队列后关闭已有 Realm 实例，再绑定该用户的 {@link RealmConfiguration}。
     */
    public static void openForUser(@NonNull String userId) {
        if (!sdkInitialized || appContext == null) {
            throw new IllegalStateException("ImRealm not initialized. Call ImRealm.init(Application) first.");
        }
        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException("userId is empty");
        }
        synchronized (ImRealm.class) {
            if (userId.equals(currentUserId)) {
                return;
            }
            ImDbExecutor.drainPendingWrites();
            closeRealmInstancesLocked(activeUserConfig);
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .name(realmFileNameForUser(userId))
                    .schemaVersion(SCHEMA_VERSION)
                    .deleteRealmIfMigrationNeeded()
                    .modules(new ImRealmModule())
                    .build();
            // 勿调用 Realm.setDefaultConfiguration：会覆盖 baselibrary RealmUtil 的用户库（UserRealm 等）
            activeUserConfig = config;
            currentUserId = userId;
        }
    }

    /**
     * 登出/切号前关闭当前用户库（不删除文件）。
     */
    public static void closeForUser() {
        synchronized (ImRealm.class) {
            ImDbExecutor.drainPendingWrites();
            closeRealmInstancesLocked(activeUserConfig);
            activeUserConfig = null;
            currentUserId = null;
        }
    }

    public static Realm getRealm() {
        if (!isUserBound() || activeUserConfig == null) {
            throw new IllegalStateException(
                    "No user Realm open. Call ImRealm.openForUser(userId) after login.");
        }
        return Realm.getInstance(activeUserConfig);
    }

    private static void closeRealmInstancesLocked(@Nullable RealmConfiguration config) {
        if (!sdkInitialized || config == null) {
            return;
        }
        try {
            Realm realm = Realm.getInstance(config);
            if (!realm.isClosed()) {
                realm.close();
            }
        } catch (IllegalStateException ignored) {
            // 无打开中的实例
        }
        int guard = 0;
        while (Realm.getGlobalInstanceCount(config) > 0 && guard++ < 50) {
            try {
                Thread.sleep(10L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
    }

    /**
     * 库文件名：{@code im_sdk_<userId>.realm}，仅将非法文件名字符替换为 {@code _}，不做哈希。
     */
    @NonNull
    private static String realmFileNameForUser(@NonNull String userId) {
        String safe = userId.replaceAll("[^a-zA-Z0-9_-]", "_");
        return DB_NAME_PREFIX + safe + DB_NAME_SUFFIX;
    }
}
