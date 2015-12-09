package io.pivotal.android.push.heartbeatmonitor;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import io.pivotal.android.push.service.GcmService;

public class PushService extends GcmService {

    private static final String PCF_PUSH_HEARTBEAT = "pcf.push.heartbeat";

    @Override
    public void onReceiveMessage(Bundle payload) {
        if (isHeartbeat(payload)) {
            Log.i(Const.LOG_TAG, "Received heartbeat.");

            Preferences.tickHeartbeatCounter(this);
            Preferences.tickLastTimestamp(this);

            final Intent intent = new Intent(MainActivity.HEARTBEAT_RECEIVED);
            sendBroadcast(intent);
        }
    }

    private boolean isHeartbeat(final Bundle payload) {
        if (payload == null) { return false; }

        for (final String key : payload.keySet()) {
            if (key.startsWith(PCF_PUSH_HEARTBEAT)) {
                return true;
            }
        }

        return false;
    }
}
