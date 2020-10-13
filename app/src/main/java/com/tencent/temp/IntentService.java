package com.tencent.temp;

import android.app.Service;
import android.content.Intent;
import android.ext.content.AsyncTask;
import android.os.IBinder;

/**
 * Class IntentService
 * @author Garfield
 */
public abstract class IntentService extends Service {
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AsyncTask.SERIAL_EXECUTOR.execute(() -> {
            onHandleIntent(intent);
            stopSelf(startId);
        });

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Callback method is invoked on the worker thread with a request to process. Only
     * one <tt>Intent</tt> is processed at a time. When all requests have been handled,
     * this <tt>IntentService</tt> stops itself, so you should not call <tt>stopSelf</tt>.
     * @param intent The <tt>Intent</tt>, passed earlier by {@link #onStartCommand}.
     */
    protected abstract void onHandleIntent(Intent intent);
}
