/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfirechat.remote;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class RecoverReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // do nothing
       eLogD("wfc", "main process crashed, to restart");
    }
}
