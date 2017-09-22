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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.common.collect.ImmutableSet;
import io.pivotal.android.push.Push;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.registration.RegistrationListener;
import java.net.URI;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {

  public static final String HEARTBEAT_RECEIVED = "io.pivotal.android.push.heartbeatmonitor.HEARTBEAT_RECEIVED";
  public static final int TIMESTAMP_UPDATE_INTERVAL = 20000;

  @Bind(R.id.heart)
  ImageView heartView;

  @Bind(R.id.toolbar)
  Toolbar toolbar;

  @Bind({R.id.text1, R.id.text2, R.id.text3})
  List<TextView> textViews;

  private BroadcastReceiver receiver;
  private boolean isRegistered = false;
  private Handler handler;
  private Runnable updateTask = new Runnable() {

    @Override
    public void run() {
      updateHeartbeatCounter();
      handler.postDelayed(updateTask, TIMESTAMP_UPDATE_INTERVAL);
    }
  };

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    ButterKnife.bind(this);

    setSupportActionBar(toolbar);
  }

  @Override
  protected void onResume() {
    super.onResume();

    startPushRegistration();
    startUpdateTask();

    setTitle(R.string.app_name);

    final IntentFilter intentFilter = new IntentFilter(HEARTBEAT_RECEIVED);
    receiver = new BroadcastReceiver() {

      @Override
      public void onReceive(Context context, Intent intent) {
        runOnUiThread(new Runnable() {
          @Override
          public void run() {
            animateHeartbeat();
            updateHeartbeatCounter();
          }
        });
      }
    };
    registerReceiver(receiver, intentFilter);
  }

  private void startPushRegistration() {

    try {

      textViews.get(0).setText("");
      textViews.get(1).setText("");
      textViews.get(2).setText(R.string.registering);

      final ImmutableSet<String> tags = ImmutableSet.of("pcf.push.heartbeat");
      Push.getInstance(this)
          .startRegistration(Build.MODEL, tags, false, new RegistrationListener() {

            @Override
            public void onRegistrationComplete() {
              Log.i(Const.LOG_TAG, "Registration with PCF Push successful. Device ID is: " + Push
                  .getInstance(MainActivity.this).getDeviceUuid());

              runOnUiThread(new Runnable() {

                @Override
                public void run() {
                  isRegistered = true;
                  final String serviceUrl = Pivotal.getServiceUrl(MainActivity.this);
                  final URI uri = URI.create(serviceUrl);
                  textViews.get(2).setText(getString(R.string.monitoring, uri.getHost()));
                  updateHeartbeatCounter();
                }
              });
            }

            @Override
            public void onRegistrationFailed(final String s) {
              Log.e(Const.LOG_TAG, "Registration with PCF Push failed: " + s);
              runOnUiThread(new Runnable() {

                @Override
                public void run() {
                  textViews.get(0).setText("");
                  textViews.get(1).setText(R.string.registration_error);
                  textViews.get(2).setText(s);
                }
              });
            }
          });

    } catch (Exception e) {

      Log.e(Const.LOG_TAG, "Exception registering for PCF Push: " + e.getLocalizedMessage());
      textViews.get(1).setText(R.string.registration_error);
      textViews.get(0).setText("");
      if (e.getLocalizedMessage() != null) {
        textViews.get(2).setText(e.getLocalizedMessage());
      } else {
        textViews.get(2).setText(e.toString());
      }
    }
  }

  private void startUpdateTask() {
    handler = new Handler();
    handler.postDelayed(updateTask, TIMESTAMP_UPDATE_INTERVAL);
  }

  @Override
  protected void onPause() {
    super.onPause();

    unregisterReceiver(receiver);
    heartView = null;

    if (handler != null) {
      handler.removeCallbacks(updateTask);
    }
    handler = null;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    //noinspection SimplifiableIfStatement
    if (id == R.id.action_settings) {
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

  @OnClick(R.id.heart)
  public void onHeartTouched() {
    animateHeartbeat();
  }

  private void animateHeartbeat() {
    if (heartView != null) {
      final Animation heartbeat = AnimationUtils.loadAnimation(this, R.anim.heartbeat);
      heartView.startAnimation(heartbeat);
    }
  }

  private void updateHeartbeatCounter() {

    final int heartbeatCounter = Preferences.getHeartbeatCounter(this);
    final long latestTimestamp = Preferences.getLastTimestamp(this);

    if (isRegistered) {

      if (latestTimestamp > 0) {
        textViews.get(0).setText(getFormattedCounterScreen(heartbeatCounter));
        setTimestampInTextView(latestTimestamp);
      } else {
        textViews.get(0).setText("");
        textViews.get(1).setText(getFormattedCounterScreen(heartbeatCounter));
      }

    } else {
      textViews.get(0).setText("");
      textViews.get(1).setText("");
    }
  }

  private void setTimestampInTextView(final long timestamp) {
    final long difference = new Date().getTime() - timestamp;

    if (difference < 60000) {
      textViews.get(1).setText(getString(R.string.last_timestamp_less_than_minute_ago));
    } else {
      textViews.get(1).setText(
          getString(R.string.last_timestamp, DateUtils.getRelativeTimeSpanString(timestamp)));
    }
  }

  @NonNull
  private String getFormattedCounterScreen(int heartbeatCounter) {
    return getResources()
        .getQuantityString(R.plurals.heartbeat_counter, heartbeatCounter, heartbeatCounter);
  }
}
