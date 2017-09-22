/*  Copyright (C) 2015-Present Pivotal Software, Inc. All rights reserved. 
 *
 *  This program and the accompanying materials are made available under 
 *  the terms of the under the Apache License, Version 2.0 (the "License”); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at 
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
  *  Unless required by applicable law or agreed to in writing, software 
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and 
 * limitations under the License.
  */

package io.pivotal.android.push.heartbeatmonitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import java.util.Date;

public class Preferences {

  public static final String HEARTBEAT_COUNTER = "heartbeat_counter";
  public static final String HEARTBEAT_LAST_TIMESTAMP = "heartbeat_last_timestamp";

  public static int getHeartbeatCounter(final Context context) {
    return getSharedPreferences(context).getInt(HEARTBEAT_COUNTER, 0);
  }

  public static void setHeartbeatCounter(final Context context, final int counter) {
    final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
    editor.putInt(HEARTBEAT_COUNTER, counter);
    editor.apply();
  }

  public static void tickHeartbeatCounter(final Context context) {
    final int heartbeatCounter = getHeartbeatCounter(context);
    setHeartbeatCounter(context, heartbeatCounter + 1);
  }

  public static long getLastTimestamp(final Context context) {
    return getSharedPreferences(context).getLong(HEARTBEAT_LAST_TIMESTAMP, 0);
  }

  public static void setLastTimestamp(final Context context, final Date timestamp) {
    final SharedPreferences.Editor editor = getSharedPreferences(context).edit();
    editor.putLong(HEARTBEAT_LAST_TIMESTAMP, timestamp.getTime());
    editor.apply();
  }

  public static void tickLastTimestamp(final Context context) {
    setLastTimestamp(context, new Date());
  }

  private static SharedPreferences getSharedPreferences(final Context context) {
    return PreferenceManager.getDefaultSharedPreferences(context);
  }
}
