package android.ext.service;

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
 * @version 1.0
 */
public abstract class IntentService extends Service implements Callback {
    private static final int MESSAGE_QUIT   = 0;
    private static final int MESSAGE_INTENT = 1;

    private Handler mHandler;
    private final int mPriority;
    private final long mKeepAliveTime;
    private final boolean mRedelivery;

    /**
     * Constructor
     * @param keepAliveTime The maximum time in milliseconds that this
     * service will wait for a new <tt>Intent</tt> before terminating.
     * @see #IntentService(long, boolean, int)
     */
    public IntentService(long keepAliveTime) {
        this(keepAliveTime, false, Process.THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * Constructor
     * @param keepAliveTime The maximum time in milliseconds that this service will wait
     * for a new <tt>Intent</tt> before terminating.
     * @param redelivery The intent redelivery preferences. If <tt>true</tt>
     * {@link #onStartCommand(Intent, int, int)} will return {@link Service#START_REDELIVER_INTENT},
     * otherwise return {@link Service#START_NOT_STICKY}.
     * @param priority The priority to run the worker thread at. The value supplied must
     * be from {@link android.os.Process} and <b>not</b> from {@link java.lang.Thread}.
     * @see #IntentService(long)
     */
    public IntentService(long keepAliveTime, boolean redelivery, int priority) {
        mPriority   = priority;
        mRedelivery = redelivery;
        mKeepAliveTime = keepAliveTime;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        final HandlerThread thread = new HandlerThread(getClass().getSimpleName() + "-thread", mPriority);
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
        mHandler.sendMessage(Message.obtain(mHandler, MESSAGE_INTENT, flags, startId, intent));
        return (mRedelivery ? START_REDELIVER_INTENT : START_NOT_STICKY);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_QUIT:
            // msg.arg1 -- startId
            stopSelf(msg.arg1);
            break;

        case MESSAGE_INTENT:
            // msg.arg1 -- flags
            // msg.arg2 -- startId
            // msg.obj  -- intent
            onHandleIntent((Intent)msg.obj, msg.arg1, msg.arg2);
            if (!mHandler.hasMessages(MESSAGE_INTENT)) {
                mHandler.sendMessageDelayed(Message.obtain(mHandler, MESSAGE_QUIT, msg.arg2, 0), mKeepAliveTime);
            }
            break;
        }

        return true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Callback method is invoked on the worker thread with a request to process. Only
     * one <tt>Intent</tt> is processed at a time. When all requests have been handled,
     * the <tt>IntentService</tt> stops itself, so you should not call <tt>stopSelf</tt>.
     * @param intent The <tt>Intent</tt>, passed earlier by {@link Context#startService(Intent)}.
     * @param flags The flags, passed earlier by {@link #onStartCommand(Intent, int, int)}.
     * @param startId A unique integer, passed earlier by {@link #onStartCommand(Intent, int, int)}.
     */
    protected abstract void onHandleIntent(Intent intent, int flags, int startId);
}
