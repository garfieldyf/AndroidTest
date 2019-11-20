package android.ext.util;

import android.os.Handler;

/**
 * Class CancelableRunnable
 * <h3>Usage</h3>
 * <p>Here is an example of subclassing:</p><pre>
 * private static final class UpdateTask extends CancelableRunnable {
 *     {@code @Override}
 *     public void run() {
 *         if (!isCancelled()) {
 *             // Running this task on the UI thread.
 *         }
 *     }
 * }
 *
 * private UpdateTask mTask;
 *
 * if (mTask != null) {
 *     mTask.cancel();
 * }
 *
 * mTask = new UpdateTask();
 * mTask.postDelayed(2000);
 * @author Garfield
 */
public abstract class CancelableRunnable implements Runnable {
    private final Handler mHandler;
    private volatile boolean mCancelled;

    /**
     * Constructor
     * <p>This runnable will be post associated with the UI thread's message queue.</p>
     * @see #CancelableRunnable(Handler)
     */
    public CancelableRunnable() {
        mHandler = UIHandler.sInstance;
    }

    /**
     * Constructor
     * @param handler A {@link Handler} allows you to post this
     * runnable associated with a thread's message queue.
     * @see #CancelableRunnable()
     */
    public CancelableRunnable(Handler handler) {
        mHandler = handler;
    }

    /**
     * Attempts to cancel this runnable and remove it from the message queue.
     * @see #isCancelled()
     */
    public final void cancel() {
        mCancelled = true;
        mHandler.removeCallbacks(this);
    }

    /**
     * Returns <tt>true</tt> if this runnable was cancelled.
     * @return <tt>true</tt> if this runnable was cancelled,
     * <tt>false</tt> otherwise.
     * @see #cancel()
     */
    public final boolean isCancelled() {
        return mCancelled;
    }

    /**
     * Adds this runnable to the message queue, to be run after the specified amount of time
     * elapses. This runnable will be run on the thread to which the internal handler is attached.
     * @param delayMillis The delay (in milliseconds) until this runnable will be executed.
     */
    public final void postDelayed(long delayMillis) {
        mCancelled = false;
        mHandler.postDelayed(this, delayMillis);
    }
}
