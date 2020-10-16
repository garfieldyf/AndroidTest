package android.ext.content;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.arch.lifecycle.GenericLifecycleObserver;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.Lifecycle.Event;
import android.arch.lifecycle.LifecycleOwner;
import android.ext.concurrent.ThreadPool;
import android.ext.content.Loader.Task;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;

/**
 * Like as {@link android.os.AsyncTask}, but this class has an owner <tt>Object</tt> (the owner may be
 * an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.) to avoid potential memory
 * leaks. This class listen the owner's lifecycle when the owner destroy this task will be cancel.
 * @author Garfield
 */
public abstract class AsyncTask<Params, Progress, Result> implements Cancelable {
    /**
     * An {@link Executor} that can be used to execute tasks.
     */
    public static final Executor THREAD_POOL_EXECUTOR;

    /**
     * An {@link Executor} that execute tasks one at a time in serial order.
     */
    public static final Executor SERIAL_EXECUTOR;

    /* package */ Status mStatus;
    /* package */ final Worker mWorker;
    /* package */ WeakReference<Object> mOwner;

    /**
     * Constructor
     * @see #AsyncTask(Object)
     */
    public AsyncTask() {
        DebugUtils.__checkMemoryLeaks(getClass());
        mWorker = new Worker();
        mStatus = Status.PENDING;
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncTask()
     */
    public AsyncTask(Object owner) {
        this();
        setOwner(owner);
    }

    /**
     * Returns the current status of this task.
     * @return The current {@link Status}.
     */
    public final Status getStatus() {
        return mStatus;
    }

    /**
     * Sets the object that owns this task.
     * @param owner May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
     * @return This <em>task</em>.
     * @see #getOwner()
     */
    public final AsyncTask<Params, Progress, Result> setOwner(Object owner) {
        DebugUtils.__checkError(owner == null, "Invalid parameter - owner == null");
        mOwner = new WeakReference<Object>(owner);
        addLifecycleObserver(owner);
        return this;
    }

    /**
     * Equivalent to calling <tt>executeOnExecutor(THREAD_POOL_EXECUTOR, params)</tt>.
     * @param params The parameters of this task. If this task no parameters, you can
     * pass <em>(Params[])null</em> instead of allocating an empty array.
     * @return This <em>task</em>.
     * @see #executeOnExecutor(Executor, Params[])
     */
    @SuppressWarnings("unchecked")
    public final AsyncTask<Params, Progress, Result> execute(Params... params) {
        return executeOnExecutor(THREAD_POOL_EXECUTOR, params);
    }

    /**
     * Executes this task with the specified parameters. <p><b>This method must
     * be invoked on the UI thread.</b></p>
     * @param executor The <tt>Executor</tt> to executing this task.
     * @param params The parameters of this task. If this task no parameters,
     * you can pass <em>(Params[])null</em> instead of allocating an empty array.
     * @return This <em>task</em>.
     * @see #execute(Params[])
     */
    @SuppressWarnings("unchecked")
    public final AsyncTask<Params, Progress, Result> executeOnExecutor(Executor executor, Params... params) {
        DebugUtils.__checkError(executor == null, "Invalid parameter - executor == null");
        DebugUtils.__checkUIThread("execute");
        switch (mStatus) {
        case RUNNING:
            throw new IllegalStateException("Cannot execute task: this task is already running.");

        case FINISHED:
            throw new IllegalStateException("Cannot execute task: this task has already been executed (a task can be executed only once)");
        }

        mStatus = Status.RUNNING;
        mWorker.mParams = params;
        onPreExecute(params);
        executor.execute(mWorker);
        return this;
    }

    @Override
    public final boolean isCancelled() {
        return mWorker.isCancelled();
    }

    /**
     * @see #onCancelled(Result)
     */
    @Override
    public final boolean cancel(boolean mayInterruptIfRunning) {
        return mWorker.cancel(mayInterruptIfRunning);
    }

    /**
     * Returns the object that owns this task.
     * @return The owner object or <tt>null</tt> if the owner released by the GC.
     * @see #setOwner(Object)
     */
    @SuppressWarnings("unchecked")
    protected final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    /**
     * This method can be invoked to publish progress values to update UI.
     * @param values The progress values to update.
     * @see #onProgressUpdate(Progress[])
     */
    @SuppressWarnings("unchecked")
    protected final void setProgress(Progress... values) {
        mWorker.setProgress(values);
    }

    /**
     * Runs on the UI thread after {@link #setProgress} is invoked.
     * @param values The progress values to update, passed earlier
     * by {@link #setProgress}.
     * @see #setProgress(Progress[])
     */
    protected void onProgressUpdate(Progress[] values) {
    }

    /**
     * Runs on the UI thread before {@link #doInBackground}.
     * @param params The parameters, passed earlier by {@link #execute}.
     * @see #doInBackground(Params[])
     * @see #onPostExecute(Params[], Result)
     */
    protected void onPreExecute(Params[] params) {
    }

    /**
     * Runs on the UI thread after {@link #cancel(boolean)} is invoked and
     * {@link #doInBackground(Params[])} has finished.
     * @param result The result, returned earlier by {@link #doInBackground}.
     * @see #cancel(boolean)
     * @see #doInBackground(Params[])
     * @see #onPostExecute(Params[], Result)
     */
    protected void onCancelled(Result result) {
    }

    /**
     * Overrides this method to perform a computation on a background thread.
     * @param params The parameters of this task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute(Params[])
     * @see #onCancelled(Result)
     * @see #onPostExecute(Params[], Result)
     */
    protected abstract Result doInBackground(Params[] params);

    /**
     * Runs on the UI thread after {@link #doInBackground}.
     * @param params The parameters, passed earlier by {@link #execute}.
     * @param result The result, returned earlier by {@link #doInBackground}.
     * @see #onPreExecute(Params[])
     * @see #onCancelled(Result)
     * @see #doInBackground(Params[])
     */
    protected abstract void onPostExecute(Params[] params, Result result);

    /**
     * Adds a <tt>LifecycleObserver</tt> that will be notified when the <tt>Lifecycle</tt> changes state.
     */
    private void addLifecycleObserver(Object owner) {
        if (owner instanceof Lifecycle) {
            ((Lifecycle)owner).addObserver(mWorker);
        } else if (owner instanceof LifecycleOwner) {
            ((LifecycleOwner)owner).getLifecycle().addObserver(mWorker);
        }
    }

    /**
     * Removes the <tt>LifecycleObserver</tt> from the <tt>Lifecycle</tt>.
     * @return <tt>true</tt> if the owner has been destroyed, <tt>false</tt> otherwise.
     */
    /* package */ final boolean removeLifecycleObserver() {
        final Object owner = mOwner.get();
        if (owner == null) {
            DebugUtils.__checkDebug(true, getClass().getName(), "The owner released by the GC: the task will be call onCancelled()");
            return true;
        }

        if (owner instanceof Lifecycle) {
            ((Lifecycle)owner).removeObserver(mWorker);
        } else if (owner instanceof LifecycleOwner) {
            ((LifecycleOwner)owner).getLifecycle().removeObserver(mWorker);
        }

        if (!mWorker.isCancelled() && owner instanceof Activity) {
            final Activity activity = (Activity)owner;
            DebugUtils.__checkDebug(activity.isFinishing() || activity.isDestroyed(), getClass().getName(), "The Activity - " + DeviceUtils.toString(owner) + " has been destroyed: the task will be call onCancelled()");
            return (activity.isFinishing() || activity.isDestroyed());
        }

        return false;
    }

    static {
        final ThreadPool threadPool = new ThreadPool(ThreadPool.computeMaximumThreads());
        THREAD_POOL_EXECUTOR = threadPool;
        SERIAL_EXECUTOR = threadPool.createSerialExecutor();
    }

    /**
     * Indicates the current status of the task.
     */
    public static enum Status {
        /**
         * Indicates that the task has not been executed.
         */
        PENDING,

        /**
         * Indicates that the task is running.
         */
        RUNNING,

        /**
         * Indicates that {@link AsyncTask#onPostExecute} has finished.
         */
        FINISHED,
    }

    /**
     * Class <tt>Worker</tt> is an implementation of a {@link Task}.
     */
    @SuppressLint("RestrictedApi")
    @SuppressWarnings("unchecked")
    /* package */ final class Worker extends Task implements GenericLifecycleObserver {
        @Override
        public void onProgress(Object value) {
            onProgressUpdate((Progress[])value);
        }

        @Override
        public Object doInBackground(Object params) {
            return AsyncTask.this.doInBackground((Params[])params);
        }

        @Override
        public void onPostExecute(Object result) {
            if (mOwner != null && removeLifecycleObserver()) {
                // Force update the state is cancelled.
                setCancelled();
            }

            if (isCancelled()) {
                AsyncTask.this.onCancelled((Result)result);
            } else {
                AsyncTask.this.onPostExecute((Params[])mParams, (Result)result);
            }

            // Prevent memory leak.
            mParams = null;
            mStatus = Status.FINISHED;
        }

        @Override
        public void onStateChanged(LifecycleOwner source, Event event) {
            if (event == Event.ON_DESTROY) {
                cancel(false);
                DebugUtils.__checkDebug(true, AsyncTask.this.getClass().getName(), "The LifecycleOwner - " + DeviceUtils.toString(source) + " has been destroyed: the task will be call onCancelled()");
            }
        }
    }
}
