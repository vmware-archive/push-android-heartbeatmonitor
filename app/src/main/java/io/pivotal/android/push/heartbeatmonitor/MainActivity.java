package io.pivotal.android.push.heartbeatmonitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.common.collect.ImmutableSet;

import java.net.URI;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.pivotal.android.push.Push;
import io.pivotal.android.push.prefs.Pivotal;
import io.pivotal.android.push.registration.RegistrationListener;

public class MainActivity extends AppCompatActivity {

    public static final String HEARTBEAT_RECEIVED = "io.pivotal.android.push.heartbeatmonitor.HEARTBEAT_RECEIVED";

    @Bind(R.id.heart) ImageView heartView;
    @Bind(R.id.toolbar) Toolbar toolbar;
    @Bind(R.id.serviceUrl) TextView serviceUrlTextView;
    @Bind(R.id.counter) TextView counterTextView;

    private BroadcastReceiver receiver;
    private boolean isRegistered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        try {

            serviceUrlTextView.setText(R.string.registering);

            final ImmutableSet<String> tags = ImmutableSet.of("heartbeat");
            Push.getInstance(this).startRegistration(Build.MODEL, tags, false, new RegistrationListener() {

                @Override
                public void onRegistrationComplete() {
                    Log.i(Const.LOG_TAG, "Registration with PCF Push successful. Device ID is: " + Push.getInstance(MainActivity.this).getDeviceUuid());

                    runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            isRegistered = true;
                            final String serviceUrl = Pivotal.getServiceUrl(MainActivity.this);
                            final URI uri = URI.create(serviceUrl);
                            serviceUrlTextView.setText(getString(R.string.monitoring, uri.getHost()));
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
                            counterTextView.setText(R.string.registration_error);
                            serviceUrlTextView.setText(s);
                        }
                    });
                }
            });

        } catch (Exception e) {

            Log.e(Const.LOG_TAG, "Exception registering for PCF Push: " + e.getLocalizedMessage());
            counterTextView.setText(R.string.registration_error);
            if (e.getLocalizedMessage() != null) {
                serviceUrlTextView.setText(e.getLocalizedMessage());
            } else {
                serviceUrlTextView.setText(e.toString());
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        updateHeartbeatCounter();

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

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(receiver);
        heartView = null;
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
        if (counterTextView != null) {
            if (isRegistered) {
                final int heartbeatCounter = Preferences.getHeartbeatCounter(this);
                counterTextView.setText(getString(R.string.heartbeat_counter, heartbeatCounter));
            } else {
                counterTextView.setText("");
            }
        }
    }
}
