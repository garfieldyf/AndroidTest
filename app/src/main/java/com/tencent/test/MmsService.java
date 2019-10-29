package com.tencent.test;

import android.content.Intent;
import android.ext.service.IntentService;
import android.util.Log;

public class MmsService extends IntentService {
    public static final String ACTION_MMS = "com.tencent.test.ACTION_MMS";
    public static final String ACTION_SMS = "com.tencent.test.ACTION_SMS";
    public static final String EXTRA_DATA = "extra_data";

    @Override
    public void onCreate() {
        super.onCreate();
        mSerialExecutor = MainApplication.sInstance.getSerialExecutor();
        Log.i("yf", "onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("yf", "onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("yf", "onDestroy");
    }

    @Override
    protected void onHandleIntent(Intent intent, int startId) {
        final String action = intent.getAction();
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Log.i("yf", "process = " + action + ", extra = " + intent.getStringExtra(EXTRA_DATA) + ", startId = " + startId);
    }
}
