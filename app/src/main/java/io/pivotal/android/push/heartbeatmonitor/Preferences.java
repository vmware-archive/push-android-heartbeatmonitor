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

package io.pivotal.android.push.heartbeatmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.Date;

class Preferences {

    private static final String HEARTBEAT_COUNTER = "heartbeat_counter";
    private static final String HEARTBEAT_LAST_TIMESTAMP = "heartbeat_last_timestamp";

    private static final String MONITOR_SERVICE_URL = "monitor_service_url";
    private static final String MONITOR_PLATFORM_UUID = "monitor_platform_uuid";
    private static final String MONITOR_PLATFORM_SECRET = "monitor_platform_secret";

    static int getHeartbeatCounter(final Context context) {
        return getSharedPreferences(context).getInt(HEARTBEAT_COUNTER, 0);
    }

    private static void setHeartbeatCounter(final Context context, final int counter) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putInt(HEARTBEAT_COUNTER, counter);
        editor.apply();
    }

    static void tickHeartbeatCounter(final Context context) {
        final int heartbeatCounter = getHeartbeatCounter(context);
        setHeartbeatCounter(context, heartbeatCounter + 1);
    }

    static long getLastTimestamp(final Context context) {
        return getSharedPreferences(context).getLong(HEARTBEAT_LAST_TIMESTAMP, 0);
    }

    private static void setLastTimestamp(final Context context, final Date timestamp) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putLong(HEARTBEAT_LAST_TIMESTAMP, timestamp.getTime());
        editor.apply();
    }

    static void tickLastTimestamp(final Context context) {
        setLastTimestamp(context, new Date());
    }

    private static SharedPreferences getSharedPreferences(final Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    static String getServiceUrl(final Context context) {
        return getSharedPreferences(context).getString(MONITOR_SERVICE_URL, "");
    }

    static String getPlatformUuid(final Context context) {
        return getSharedPreferences(context).getString(MONITOR_PLATFORM_UUID, "");
    }

    static String getPlatformSecret(final Context context) {
        return getSharedPreferences(context).getString(MONITOR_PLATFORM_SECRET, "");
    }

    static void setServiceUrl(final Context context, final String serviceUrl) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(MONITOR_SERVICE_URL, serviceUrl);
        editor.apply();
    }

    static void setPlatformUuid(final Context context, final String platformUuid) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(MONITOR_PLATFORM_UUID, platformUuid);
        editor.apply();
    }

    static void setPlatformSecret(final Context context, final String platformSecret) {
        final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
        editor.putString(MONITOR_PLATFORM_SECRET, platformSecret);
        editor.apply();
    }
}
