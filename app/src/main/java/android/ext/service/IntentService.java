package android.ext.service;

import android.app.Service;
import android.content.Intent;
import android.ext.util.DebugUtils;
import android.os.IBinder;
import java.util.concurrent.Executor;

/**
 * Class IntentService
 * @author Garfield
 */
public abstract class IntentService extends Service {
    /**
     * The serial {@link Executor} to executes tasks.
     */
    protected Executor mSerialExecutor;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        DebugUtils.__checkError(mSerialExecutor == null, "The mSerialExecutor uninitialized, You can initialize it on onCreate() method.");
        mSerialExecutor.execute(new IntentAction(intent, startId));
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
     * @param intent The <tt>Intent</tt>, passed earlier by {@link #onStartCommand(Intent, int, int)}.
     * @param startId A unique integer, passed earlier by {@link #onStartCommand(Intent, int, int)}.
     */
    protected abstract void onHandleIntent(Intent intent, int startId);

    /**
     * Class <tt>IntentAction</tt> is an implementation of a {@link Runnable}.
     */
    private final class IntentAction implements Runnable {
        private final int mStartId;
        private final Intent mIntent;

        public IntentAction(Intent intent, int startId) {
            mIntent  = intent;
            mStartId = startId;
        }

        @Override
        public void run() {
            onHandleIntent(mIntent, mStartId);
            stopSelf(mStartId);
        }
    }
}
