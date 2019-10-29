package com.tencent.test;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public class TestService extends Service {
    private static final String ACTION_STOP_SERVICE = "com.tencent.test.ACTION_STOP_SERVICE";
    private static final String EXTRA_START_ID = "startId";

    @Override
    public void onCreate() {
        super.onCreate();
        LocalBroadcastManager.getInstance(this).registerReceiver(mReceiver, new IntentFilter(ACTION_STOP_SERVICE));
        Log.i("yf", "onCreate");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);
        Log.i("yf", "onDestroy");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("yf", "onStartCommand -  startId = " + startId);
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void stopService(Context context, int startId) {
        final Intent intent = new Intent(ACTION_STOP_SERVICE);
        intent.putExtra(EXTRA_START_ID, startId);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_STOP_SERVICE.equals(intent.getAction())) {
                stopSelfResult(intent.getIntExtra(EXTRA_START_ID, 0));
            }
        }
    };
}
