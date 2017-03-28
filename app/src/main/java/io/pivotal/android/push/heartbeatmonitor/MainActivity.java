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

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;

import java.net.URI;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.pivotal.android.push.Push;
import io.pivotal.android.push.PushServiceInfo;
import io.pivotal.android.push.registration.RegistrationListener;

public class MainActivity extends AppCompatActivity {

    public static final String HEARTBEAT_RECEIVED = "io.pivotal.android.push.heartbeatmonitor.HEARTBEAT_RECEIVED";
    public static final int TIMESTAMP_UPDATE_INTERVAL = 20000;

    @Bind(R.id.heart)
    ImageView heartView;

    @Bind(R.id.toolbar)
    Toolbar toolbar;

    @Bind({R.id.text1, R.id.text2, R.id.text3, R.id.text4, R.id.text5})
    List<TextView> textViews;

    private BroadcastReceiver receiver;
    private boolean isRegistered = false;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        requestPushPlatformInformation();
    }

    @Override
    protected void onResume() {
        super.onResume();

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

    private void requestPushPlatformInformation() {
        final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(getString(R.string.title_push_platform_dlg));
        final View view = alertDialog.getLayoutInflater().inflate(R.layout.platform_info, null);

        final EditText serverUrlTextField = (EditText) view.findViewById(R.id.server_url);
        final EditText platformUuidTextField = (EditText) view.findViewById(R.id.platform_uuid);
        final EditText platformSecretTextField = (EditText) view.findViewById(R.id.platform_secret);
        serverUrlTextField.setText(Preferences.getServiceUrl(this));
        platformUuidTextField.setText(Preferences.getPlatformUuid(this));
        platformSecretTextField.setText(Preferences.getPlatformSecret(this));

        alertDialog.setView(view);
        alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, getString(R.string.OK), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            final String serverUrl = serverUrlTextField.getText().toString();
            final String platformUuid = platformUuidTextField.getText().toString();
            final String platformSecret = platformSecretTextField.getText().toString();

            storePlatformInfoDetails(serverUrl, platformUuid, platformSecret);

            startPushRegistration(serverUrl, platformUuid, platformSecret);
            }
        });
        alertDialog.show();
    }

    private void startPushRegistration(final String serverUrl, final String platformUuid, final String platformSecret) {

        try {

            textViews.get(0).setText("");
            textViews.get(1).setText("");
            textViews.get(2).setText(R.string.registering);
            textViews.get(3).setText("");
            textViews.get(4).setText("");

            final ImmutableSet<String> tags = ImmutableSet.of("pcf.push.heartbeat");
            final PushServiceInfo pushServiceInfo = PushServiceInfo.Builder()
                    .setServiceUrl(serverUrl)
                    .setPlatformUuid(platformUuid)
                    .setPlatformSecret(platformSecret)
                    .build();

            Push push = Push.getInstance(this);
            push.setPushServiceInfo(pushServiceInfo);

            push.startRegistration(Build.MODEL, tags, false, new RegistrationListener() {

                @Override
                public void onRegistrationComplete() {
                    Log.i(Const.LOG_TAG, "Registration with PCF Push successful. Device ID is: " + Push.getInstance(MainActivity.this).getDeviceUuid());

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            isRegistered = true;
                            displayPlatformInfoDetails(pushServiceInfo);
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
                            textViews.get(3).setText("");
                            textViews.get(4).setText("");
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

    private Runnable updateTask = new Runnable() {

        @Override
        public void run() {
            updateHeartbeatCounter();
            handler.postDelayed(updateTask, TIMESTAMP_UPDATE_INTERVAL);
        }
    };

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
            textViews.get(1).setText(getString(R.string.last_timestamp, DateUtils.getRelativeTimeSpanString(timestamp)));
        }
    }

    private void displayPlatformInfoDetails(final PushServiceInfo pushServiceInfo) {
        final String serviceUrl = pushServiceInfo.getServiceUrl();
        final String platformUuid = pushServiceInfo.getPlatformUuid();
        final String platformSecret = pushServiceInfo.getPlatformSecret();
        final URI uri = URI.create(serviceUrl);
        textViews.get(2).setText(getString(R.string.monitoring, uri.toString()));
        textViews.get(3).setText(getString(R.string.monitor_platform_uuid, platformUuid));
        textViews.get(4).setText(getString(R.string.monitor_platform_secret, platformSecret));
    }

    private void storePlatformInfoDetails(final String serviceUrl, final String platformUuid, final String platformSecret) {
        Preferences.setServiceUrl(this, serviceUrl);
        Preferences.setPlatformUuid(this, platformUuid);
        Preferences.setPlatformSecret(this, platformSecret);
    }

    @NonNull
    private String getFormattedCounterScreen(int heartbeatCounter) {
        return getResources().getQuantityString(R.plurals.heartbeat_counter, heartbeatCounter, heartbeatCounter);
    }
}
