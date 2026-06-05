package com.freddy.im.db;

import androidx.annotation.NonNull;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Realm 读写统一在单后台线程执行（避免主线程阻塞与 Realm 线程限制）。
 */
public final class ImDbExecutor {

    private static final ExecutorService REALM_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread thread = new Thread(r, "im-realm");
        thread.setDaemon(true);
        return thread;
    });

    private ImDbExecutor() {
    }

    public static void runWrite(@NonNull Runnable task) {
        runRealm(task);
    }

    /** 同步读库；与 {@link #runWrite} 共用同一线程，避免并发打开 Realm。 */
    public static void runRead(@NonNull Runnable task) {
        runRealm(task);
    }

    public static void runRealm(@NonNull Runnable task) {
        if (!ImRealm.isUserBound()) {
            return;
        }
        REALM_EXECUTOR.execute(task);
    }

    /** 等待已提交的 Realm 任务执行完毕（切库/登出前调用）。 */
    public static void drainPendingWrites() {
        if (!ImRealm.isInitialized()) {
            return;
        }
        try {
            Future<?> barrier = REALM_EXECUTOR.submit(() -> { });
            barrier.get(10L, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            android.util.Log.w("ImDbExecutor", "drainPendingWrites timeout", e);
        } catch (Exception e) {
            android.util.Log.w("ImDbExecutor", "drainPendingWrites failed", e);
        }
    }
}
