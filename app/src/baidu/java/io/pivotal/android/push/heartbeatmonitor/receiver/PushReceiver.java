package io.pivotal.android.push.heartbeatmonitor.receiver;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import io.pivotal.android.push.baidu.BaiduPushReceiver;
import io.pivotal.android.push.heartbeatmonitor.Const;
import io.pivotal.android.push.heartbeatmonitor.Preferences;
import io.pivotal.android.push.heartbeatmonitor.activity.MainActivity;

public class PushReceiver extends BaiduPushReceiver {

    @Override
    public void onMessage(Context context, String message, String customContentString) {
        super.onMessage(context, message, customContentString);

        Log.i(Const.LOG_TAG, "Received heartbeat.");

        Preferences.tickHeartbeatCounter(context);
        Preferences.tickLastTimestamp(context);

        final Intent intent = new Intent(MainActivity.HEARTBEAT_RECEIVED);
        context.sendBroadcast(intent);
    }
}