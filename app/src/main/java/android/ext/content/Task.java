package android.ext.content;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleOwner;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.widget.UIHandler;
import android.ext.widget.UIHandler.MessageRunnable;
import android.os.Message;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This abstract class should be implemented by any class whose instances are intended to be execute.
 */
@SuppressLint("RestrictedApi")
public abstract class Task implements MessageRunnable, Cancelable, GenericLifecycleObserver {
    private static final int MESSAGE_PROGRESS = 1;
    private static final int RUNNING   = 0;
    private static final int CANCELLED = 1;
    private static final int COMPLETED = 2;

    /**
     * The parameter of this task.
     */
    /* package */ Object mParams;

    /**
     * The thread running this task.
     */
    private volatile Thread mRunner;

    /**
     * Possible state transitions:
     * <ul><li>RUNNING -> CANCELLED</li>
     * <li>RUNNING -> COMPLETED</li></ul>
     */
    private final AtomicInteger mState;

    /**
     * Constructor
     */
    /* package */ Task() {
        mState = new AtomicInteger(RUNNING);
    }

    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        final boolean result = mState.compareAndSet(RUNNING, CANCELLED);
        if (result && mayInterruptIfRunning && mRunner != null) {
            mRunner.interrupt();
        }

        return result;
    }

    @Override
    public final boolean isCancelled() {
        return (mState.get() == CANCELLED);
    }

    @Override
    public final void run() {
        Object result = null;
        if (mState.get() == RUNNING) {
            try {
                mRunner = Thread.currentThread();
                result  = doInBackground(mParams);
            } finally {
                mRunner = null;
                mState.compareAndSet(RUNNING, COMPLETED);
            }
        }

        UIHandler.sInstance.post(this, result);
    }

    @Override
    @UiThread
    public final void handleMessage(Message msg) {
        if (msg.what == MESSAGE_PROGRESS) {
            onProgress(msg.obj);
        } else {
            onPostExecute(msg.obj);
        }
    }

    @Override
    public final void onStateChanged(LifecycleOwner source, Event event) {
        if (event == Event.ON_DESTROY) {
            // Force update the state is cancelled.
            mState.set(CANCELLED);
            DebugUtils.__checkDebug(true, "Task", "The LifecycleOwner - " + DeviceUtils.toString(source) + " has been destroyed");
        }
    }

    /**
     * Runs on the UI thread after {@link #setProgress} is invoked.
     * @param value The progress value to update.
     */
    @UiThread
    /* package */ void onProgress(Object value) {
    }

    /**
     * Runs on the UI thread after {@link #doInBackground}.
     * @param result The result, returned earlier by {@link #doInBackground}.
     * @see #doInBackground(Object)
     */
    @UiThread
    /* package */ abstract void onPostExecute(Object result);

    /**
     * Overrides this method to perform a computation on a background thread.
     * @param params The parameter of this task.
     * @return A result, defined by the subclass of this task.
     * @see #onPostExecute(Object)
     */
    @WorkerThread
    /* package */ abstract Object doInBackground(Object params);

    /**
     * Clears all fields for recycle.
     */
    @UiThread
    /* package */ final void clearForRecycle() {
        mParams = null;
        mRunner = null;
        mState.set(RUNNING);
    }

    /**
     * This method can be invoked to publish progress value to update UI.
     * @param value The progress value to update.
     */
    /* package */ final void setProgress(Object value) {
        if (mState.get() == RUNNING) {
            UIHandler.sInstance.sendMessage(UIHandler.sInstance.obtianMessage(this, MESSAGE_PROGRESS, value));
        }
    }

    /**
     * Adds a <tt>LifecycleObserver</tt> that will be notified when the <tt>Lifecycle</tt> changes state.
     */
    @UiThread
    /* package */ final void addLifecycleObserver(Object owner) {
        if (owner instanceof Lifecycle) {
            ((Lifecycle)owner).addObserver(this);
        } else if (owner instanceof LifecycleOwner) {
            ((LifecycleOwner)owner).getLifecycle().addObserver(this);
        }
    }

    /**
     * Removes the <tt>LifecycleObserver</tt> from the <tt>Lifecycle</tt>.
     */
    @UiThread
    /* package */ final void removeLifecycleObserver(WeakReference<Object> ownerRef) {
        if (ownerRef != null && removeLifecycleObserver(ownerRef.get())) {
            // Force update the state is cancelled.
            mState.set(CANCELLED);
        }
    }

    /**
     * Removes the <tt>LifecycleObserver</tt> from the <tt>Lifecycle</tt>.
     * @return <tt>true</tt> if the owner has been destroyed, <tt>false</tt> otherwise.
     */
    private boolean removeLifecycleObserver(Object owner) {
        if (owner == null) {
            DebugUtils.__checkDebug(true, "Task", "The owner released by the GC");
            return true;
        }

        if (owner instanceof Lifecycle) {
            ((Lifecycle)owner).removeObserver(this);
        } else if (owner instanceof LifecycleOwner) {
            ((LifecycleOwner)owner).getLifecycle().removeObserver(this);
        }

        if (mState.get() != CANCELLED && owner instanceof Activity) {
            final Activity activity = (Activity)owner;
            DebugUtils.__checkDebug(activity.isFinishing() || activity.isDestroyed(), "Task", "The Activity - " + DeviceUtils.toString(owner) + " has been destroyed");
            return (activity.isFinishing() || activity.isDestroyed());
        }

        return false;
    }
}
