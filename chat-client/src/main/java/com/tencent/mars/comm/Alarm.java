//package com.tencent.mars.comm;
//
//import android.app.AlarmManager;
//import android.app.PendingIntent;
//import android.content.BroadcastReceiver;
//import android.content.Context;
//import android.content.Intent;
//import android.content.IntentFilter;
//import android.os.Build;
//import android.os.Process;
//import android.os.SystemClock;
//
//import com.tencent.mars.xlog.Log;
//
//import java.util.Comparator;
//import java.util.Iterator;
//import java.util.TreeSet;
//
///**
// * Mars 长连接心跳闹钟。无精确闹钟权限时回退为 {@link AlarmManager#setAndAllowWhileIdle}，
// * 避免移除 USE_EXACT_ALARM 后 setExact 抛 SecurityException 导致 IM 掉线。
// */
//public class Alarm extends BroadcastReceiver {
//    private static final String TAG = "MicroMsg.Alarm";
//    private static final String KEXTRA_ID = "ID";
//    private static final String KEXTRA_PID = "PID";
//
//    private static WakerLock wakerlock;
//    private static Alarm bc_alarm;
//    private static final TreeSet<Object[]> alarm_waiting_set = new TreeSet<>(new ComparatorAlarm());
//
//    private native void onAlarm(long id);
//
//    public static void resetAlarm(Context context) {
//        synchronized (alarm_waiting_set) {
//            Iterator<Object[]> it = alarm_waiting_set.iterator();
//            while (it.hasNext()) {
//                cancelAlarmMgr(context, (PendingIntent) it.next()[TSetData.PENDINGINTENT.ordinal()]);
//            }
//            alarm_waiting_set.clear();
//            if (bc_alarm != null) {
//                context.unregisterReceiver(bc_alarm);
//                bc_alarm = null;
//            }
//        }
//    }
//
//    public static boolean start(long id, int after, Context context) {
//        long curtime = SystemClock.elapsedRealtime();
//        if (after < 0) {
//            Log.e(TAG, "id:%d, after:%d", id, after);
//            return false;
//        }
//        if (context == null) {
//            Log.e(TAG, "null==context, id:%d, after:%d", id, after);
//            return false;
//        }
//        synchronized (alarm_waiting_set) {
//            if (wakerlock == null) {
//                wakerlock = new WakerLock(context);
//                Log.i(TAG, "start new wakerlock");
//            }
//            if (bc_alarm == null) {
//                bc_alarm = new Alarm();
//                context.registerReceiver(
//                        bc_alarm,
//                        new IntentFilter("ALARM_ACTION(" + Process.myPid() + ")"),
//                        null,
//                        null);
//            }
//            for (Object[] entry : alarm_waiting_set) {
//                if ((Long) entry[TSetData.ID.ordinal()] == id) {
//                    Log.e(TAG, "id exist=%d", id);
//                    return false;
//                }
//            }
//            long waittime = after >= 0 ? curtime + after : curtime;
//            PendingIntent pendingIntent = setAlarmMgr(id, waittime, context);
//            if (pendingIntent == null) {
//                return false;
//            }
//            alarm_waiting_set.add(new Object[]{id, waittime, pendingIntent});
//        }
//        return true;
//    }
//
//    public static boolean stop(long id, Context context) {
//        if (context == null) {
//            Log.e(TAG, "context==null");
//            return false;
//        }
//        synchronized (alarm_waiting_set) {
//            if (wakerlock == null) {
//                wakerlock = new WakerLock(context);
//                Log.i(TAG, "stop new wakerlock");
//            }
//            if (bc_alarm == null) {
//                bc_alarm = new Alarm();
//                context.registerReceiver(bc_alarm, new IntentFilter());
//                Log.i(TAG, "stop new Alarm");
//            }
//            Iterator<Object[]> it = alarm_waiting_set.iterator();
//            while (it.hasNext()) {
//                Object[] next = it.next();
//                if ((Long) next[TSetData.ID.ordinal()] != id) {
//                    continue;
//                }
//                cancelAlarmMgr(context, (PendingIntent) next[TSetData.PENDINGINTENT.ordinal()]);
//                it.remove();
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private static PendingIntent setAlarmMgr(long id, long time, Context context) {
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (am == null) {
//            Log.e(TAG, "am == null");
//            return null;
//        }
//        Intent intent = new Intent();
//        intent.setAction("ALARM_ACTION(" + Process.myPid() + ")");
//        intent.putExtra(KEXTRA_ID, id);
//        intent.putExtra(KEXTRA_PID, Process.myPid());
//        int flags = Build.VERSION.SDK_INT < Build.VERSION_CODES.M
//                ? PendingIntent.FLAG_UPDATE_CURRENT
//                : PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE;
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, (int) id, intent, flags);
//        scheduleAlarm(am, time, pendingIntent);
//        return pendingIntent;
//    }
//
//    private static void scheduleAlarm(AlarmManager am, long time, PendingIntent pendingIntent) {
//        final int type = AlarmManager.RTC_WAKEUP;
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
//            am.set(type, time, pendingIntent);
//            return;
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            if (am.canScheduleExactAlarms()) {
//                try {
//                    am.setExact(type, time, pendingIntent);
//                    return;
//                } catch (SecurityException ignored) {
//                    Log.w(TAG, "setExact denied, fallback to setAndAllowWhileIdle");
//                }
//            } else {
//                Log.w(TAG, "canScheduleExactAlarms=false, use setAndAllowWhileIdle");
//            }
//            am.setAndAllowWhileIdle(type, time, pendingIntent);
//            return;
//        }
//        try {
//            am.setExact(type, time, pendingIntent);
//        } catch (SecurityException e) {
//            Log.w(TAG, "setExact SecurityException, fallback to set");
//            am.set(type, time, pendingIntent);
//        }
//    }
//
//    private static boolean cancelAlarmMgr(Context context, PendingIntent pendingIntent) {
//        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
//        if (am == null) {
//            Log.e(TAG, "am == null");
//            return false;
//        }
//        if (pendingIntent == null) {
//            Log.e(TAG, "pendingIntent == null");
//            return false;
//        }
//        am.cancel(pendingIntent);
//        pendingIntent.cancel();
//        return true;
//    }
//
//    @Override
//    public void onReceive(Context context, Intent intent) {
//        if (context == null || intent == null) {
//            return;
//        }
//        long id = intent.getLongExtra(KEXTRA_ID, 0L);
//        int pid = intent.getIntExtra(KEXTRA_PID, 0);
//        if (id == 0L || pid == 0) {
//            return;
//        }
//        if (pid != Process.myPid()) {
//            Log.w(TAG, "onReceive id:%d, pid:%d, mypid:%d", id, pid, Process.myPid());
//            return;
//        }
//        boolean found = false;
//        synchronized (alarm_waiting_set) {
//            Iterator<Object[]> it = alarm_waiting_set.iterator();
//            while (it.hasNext()) {
//                Object[] next = it.next();
//                Long curId = (Long) next[TSetData.ID.ordinal()];
//                Log.i(TAG, "onReceive id=%d, curId=%d", id, curId);
//                if (!curId.equals(id)) {
//                    continue;
//                }
//                Log.i(TAG, "onReceive find alarm id:%d, pid:%d, delta miss time:%d",
//                        id, pid, SystemClock.elapsedRealtime() - (Long) next[TSetData.WAITTIME.ordinal()]);
//                it.remove();
//                found = true;
//                break;
//            }
//            if (!found) {
//                Log.e(TAG, "onReceive not found id:%d, pid:%d, alarm_waiting_set.size:%d",
//                        id, pid, alarm_waiting_set.size());
//            }
//        }
//        if (wakerlock != null) {
//            wakerlock.lock(200L);
//        }
//        if (found) {
//            onAlarm(id);
//        }
//    }
//
//    private static class ComparatorAlarm implements Comparator<Object[]> {
//        @Override
//        public int compare(Object[] lhs, Object[] rhs) {
//            return (int) ((Long) lhs[TSetData.ID.ordinal()] - (Long) rhs[TSetData.ID.ordinal()]);
//        }
//    }
//
//    private enum TSetData {
//        ID,
//        WAITTIME,
//        PENDINGINTENT
//    }
//}
