//  Copyright (C) 2016 Pivotal Software, Inc. All rights reserved.
//
//  This program and the accompanying materials are made available under
//  the terms of the under the Apache License, Version 2.0 (the "License”);
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.

package io.pivotal.android.push.heartbeatmonitor.service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Map;

import io.pivotal.android.push.fcm.FcmMessagingService;
import io.pivotal.android.push.heartbeatmonitor.Const;
import io.pivotal.android.push.heartbeatmonitor.Preferences;
import io.pivotal.android.push.heartbeatmonitor.activity.MainActivity;

public class PushService extends FcmMessagingService {

    @Override
    public void onReceiveHeartbeat(Map<String, String> heartbeat) {
        Log.i(Const.LOG_TAG, "Received heartbeat.");

        Preferences.tickHeartbeatCounter(this);
        Preferences.tickLastTimestamp(this);

        final Intent intent = new Intent(MainActivity.HEARTBEAT_RECEIVED);
        sendBroadcast(intent);
    }
}
