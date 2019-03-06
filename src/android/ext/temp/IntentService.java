package android.ext.temp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;

/**
 * Like as {@link android.app.IntentService}, but the service will wait
 * <em>keepAliveTime</em> for a new <tt>Intent</tt> before terminating.
 * @author Garfield
 */
public abstract class IntentService extends Service implements Callback {
    private static final int MESSAGE_QUIT   = 0;
    private static final int MESSAGE_INTENT = 1;

    private Handler mHandler;
    protected final long mKeepAliveTime;

    /**
     * Constructor
     * @param keepAliveTime The maximum time in milliseconds that this
     * service will wait for a new <tt>Intent</tt> before terminating.
     */
    public IntentService(long keepAliveTime) {
        mKeepAliveTime = keepAliveTime;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final HandlerThread thread = new HandlerThread(getClass().getSimpleName() + "-thread", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        mHandler = new Handler(thread.getLooper(), this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.getLooper().quitSafely();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mHandler.removeMessages(MESSAGE_QUIT);
        mHandler.sendMessage(Message.obtain(mHandler, MESSAGE_INTENT, startId, flags, intent));
        return START_NOT_STICKY;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_QUIT:
            // msg.arg1 -- startId
            stopSelf(msg.arg1);
            break;

        case MESSAGE_INTENT:
            // msg.arg1 -- startId
            // msg.arg2 -- flags
            // msg.obj  -- intent
            onHandleIntent((Intent)msg.obj, msg.arg1, msg.arg2);
            sendQuitMessage(msg.arg1);
            break;
        }

        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Sends a QUIT message to the end of the worker thread's message queue.
     * This service will be stop at sometime in the future.
     * @param startId A unique integer, passed earlier by {@link #onStartCommand(Intent, int, int)}.
     */
    protected void sendQuitMessage(int startId) {
        if (mKeepAliveTime <= 0) {
            stopSelf(startId);
        } else if (!mHandler.hasMessages(MESSAGE_INTENT)) {
            mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_QUIT, startId, 0), mKeepAliveTime);
        }
    }

    /**
     * Callback method is invoked on the worker thread with a request to process. Only
     * one <tt>Intent</tt> is processed at a time. When all requests have been handled,
     * this <tt>IntentService</tt> stops itself, so you should not call <tt>stopSelf</tt>.
     * @param intent The <tt>Intent</tt>, passed earlier by {@link Context#startService(Intent)}.
     * @param startId A unique integer, passed earlier by {@link #onStartCommand(Intent, int, int)}.
     * @param flags The flags, passed earlier by {@link #onStartCommand(Intent, int, int)}.
     */
    protected abstract void onHandleIntent(Intent intent, int startId, int flags);
}