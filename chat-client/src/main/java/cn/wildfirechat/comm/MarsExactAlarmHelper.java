package cn.wildfirechat.comm;

import android.app.AlarmManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

import cn.wildfirechat.client.ConnectionStatus;
import cn.wildfirechat.remote.ChatManager;

/**
 * 精确闹钟权限变化时尝试恢复 IM 连接（不依赖 USE_EXACT_ALARM）。
 */
public final class MarsExactAlarmHelper {

    private static volatile boolean receiverRegistered;

    private MarsExactAlarmHelper() {
    }

    public static boolean canScheduleExactAlarms(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true;
        }
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        return am != null && am.canScheduleExactAlarms();
    }

    public static void openExactAlarmSettings(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return;
        }
        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
        intent.setData(Uri.parse("package:" + context.getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            context.startActivity(intent);
        } catch (Exception ignored) {
        }
    }

    public static void registerPermissionListener(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S || receiverRegistered) {
            return;
        }
        Context app = context.getApplicationContext();
        IntentFilter filter = new IntentFilter(AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED);
        ContextCompat.registerReceiver(
                app,
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context ctx, Intent intent) {
                        if (canScheduleExactAlarms(ctx)) {
                            tryReconnectIm(ctx);
                        }
                    }
                },
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED);
        receiverRegistered = true;
    }

    private static void tryReconnectIm(Context context) {
        try {
            ChatManager mgr = ChatManager.Instance();
            String userId = mgr.getUserId();
            if (TextUtils.isEmpty(userId)) {
                return;
            }
            int status = mgr.getConnectionStatus();
            if (status == ConnectionStatus.ConnectionStatusUnconnected
                    || status == ConnectionStatus.ConnectionStatusLogout) {
                mgr.forceConnect();
            }
        } catch (Exception ignored) {
        }
    }
}
