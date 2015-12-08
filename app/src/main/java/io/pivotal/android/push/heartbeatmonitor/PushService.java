package io.pivotal.android.push.heartbeatmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import io.pivotal.android.push.service.GcmService;

public class PushService extends GcmService {

    @Override
    public void onReceiveMessage(Bundle payload) {
        Log.i(Const.LOG_TAG, "Received heartbeat.");

        Preferences.tickHeartbeatCounter(this);

        final Intent intent = new Intent(MainActivity.HEARTBEAT_RECEIVED);
        sendBroadcast(intent);
    }
}
