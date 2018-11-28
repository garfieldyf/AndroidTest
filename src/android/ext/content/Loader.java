package android.ext.content;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.Context;
import android.ext.content.Loader.Task;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.ext.util.UIHandler;
import android.os.Message;
import android.util.ArrayMap;
import android.util.Printer;

/**
 * An abstract class that performs asynchronous loading of data.
 * @author Garfield
 */
@SuppressWarnings("rawtypes")
public abstract class Loader implements Factory<Task> {
    /* package */ static final int RUNNING  = 0;
    /* package */ static final int PAUSED   = 1;
    /* package */ static final int SHUTDOWN = 2;

    /* package */ volatile int mState;
    /* package */ final Executor mExecutor;

    /* package */ final Pool<Task> mTaskPool;
    /* package */ final ArrayMap<Object, Task> mRunningTasks;

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     */
    /* package */ Loader(Executor executor) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mExecutor = executor;
        mTaskPool = Pools.newPool(this, 64);
        mRunningTasks = new ArrayMap<Object, Task>();
    }

    /**
     * Shutdown this loader, stop all actively running tasks
     * and no new tasks will be accepted.
     * @see #isShutdown()
     */
    public synchronized void shutdown() {
        cancelAll();
        mState = SHUTDOWN;
        notifyAll();
    }

    /**
     * Returns <tt>true</tt> if this loader has been shut down.
     * @return <tt>true</tt> if this loader has been shut down,
     * <tt>false</tt> otherwise.
     * @see #shutdown()
     */
    public final boolean isShutdown() {
        return (mState == SHUTDOWN);
    }

    /**
     * Temporarily stops all actively running tasks.
     * @see #resume()
     */
    public final void pause() {
        mState = PAUSED;
    }

    /**
     * Resumes all actively running tasks.
     * @see #pause()
     */
    public synchronized final void resume() {
        mState = RUNNING;
        notifyAll();
    }

    /**
     * Returns the {@link Executor} associated with this loader.
     * @return The <tt>Executor</tt>.
     */
    public final Executor getExecutor() {
        return mExecutor;
    }

    /**
     * Returns <tt>true</tt> if the <em>task</em> was cancelled
     * before it completed normally.
     * @param task May be <tt>null</tt>. The {@link Task} to test.
     * @return <tt>true</tt> if the <em>task</em> was cancelled
     * or this loader has been shut down, <tt>false</tt> otherwise.
     * @see #cancelTask(Object, boolean)
     */
    public final boolean isTaskCancelled(Task task) {
        return (mState == SHUTDOWN || (task != null && task.isCancelled()));
    }

    /**
     * Attempts to stop execution of the specified task. To ensure that the
     * task is stopped as quickly as possible, you should always check the
     * return value of {@link #isTaskCancelled(Task)} periodically from the
     * background thread to end the task as soon as possible. <p><b>Note:
     * This method must be invoked on the UI thread.</b></p>
     * @param key The key to find the task.
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing
     * the task should be interrupted, <tt>false</tt> otherwise.
     * @return <tt>false</tt> if the task could not be cancelled, typically
     * because it has already completed, <tt>true</tt> otherwise.
     * @see #shutdown()
     * @see #isTaskCancelled(Task)
     */
    public final boolean cancelTask(Object key, boolean mayInterruptIfRunning) {
        DebugUtils.__checkUIThread("cancelTask");
        final Task task = mRunningTasks.remove(key);
        return (task != null && task.cancel(mayInterruptIfRunning));
    }

    public void dump(Context context, Printer printer) {
        DebugUtils.__checkUIThread("dump");
        Pools.dumpPool(mTaskPool, printer);
        final int size = mRunningTasks.size();
        if (size > 0) {
            final StringBuilder result = new StringBuilder(130);
            DebugUtils.dumpSummary(printer, result, 130, " Dumping Running Tasks [ size = %d ] ", size);

            for (int i = 0; i < size; ++i) {
                result.setLength(0);
                printer.println(DebugUtils.toSimpleString(mRunningTasks.keyAt(i), result.append("  ")).append(" ==> ").append(mRunningTasks.valueAt(i)).toString());
            }
        }
    }

    /**
     * Computes the maximum pool size of the loader internal pool.
     * @param executor The <tt>Executor</tt> to compute.
     * @return The maximum pool size.
     */
    public static int computeMaximumPoolSize(Executor executor) {
        return Math.min((executor instanceof ThreadPoolExecutor ? ((ThreadPoolExecutor)executor).getMaximumPoolSize() : 4), 8) * 3 / 2;
    }

    /**
     * Waits if necessary for this loader has been resume. (after call {@link #resume()})
     */
    /* package */ synchronized final void waitResumeIfPaused() {
        while (mState == PAUSED) {
            try {
                wait();
            } catch (InterruptedException e) {
                // Ignored.
            }
        }
    }

    /**
     * Stops all running tasks.
     */
    private void cancelAll() {
        for (int i = mRunningTasks.size() - 1; i >= 0; --i) {
            mRunningTasks.valueAt(i).cancel(false);
        }

        mRunningTasks.clear();
    }

    /**
     * This abstract class should be implemented by any class whose instances are intended to be execute.
     */
    public static abstract class Task<Params, Result> implements Runnable, Cancelable {
        private static final int CANCELLED = 1;
        private static final int COMPLETED = 2;

        /* package */ Result mResult;
        /* package */ Params[] mParams;

        private volatile Thread mRunner;
        private final AtomicInteger mState;

        /**
         * Constructor
         */
        /* package */ Task() {
            mState = new AtomicInteger(RUNNING);
        }

        /**
         * This method can be invoked to publish progress values to update UI.
         * @param values The progress values to update.
         */
        public final void setProgress(Object... values) {
            if (mState.get() == RUNNING) {
                UIHandler.sInstance.setProgress(this, values);
            }
        }

        /**
         * Called on the UI thread when this task handle messages.
         * <p><b>Note: Do not call this method directly.</b></p>
         * @param msg A {@link Message} to handle.
         */
        public final void handleMessage(Message msg) {
            if (msg.what == UIHandler.MESSAGE_FINISHED) {
                onPostExecute(mParams, mResult);
            } else {
                onProgress(mParams, (Object[])msg.obj);
            }
        }

        @Override
        public final boolean isCancelled() {
            return (mState.get() == CANCELLED);
        }

        @Override
        public final void run() {
            if (mState.get() == RUNNING) {
                try {
                    mRunner = Thread.currentThread();
                    mResult = doInBackground(mParams);
                } finally {
                    mRunner = null;
                    mState.compareAndSet(RUNNING, COMPLETED);
                }
            }

            UIHandler.sInstance.finish(this);
        }

        /**
         * Clears all fields for recycle.
         */
        /* package */ final void clearForRecycle() {
            mParams = null;
            mResult = null;
            mRunner = null;
            mState.set(RUNNING);
        }

        /**
         * Attempts to stop execution of this task. This attempt will fail
         * if this task has already completed, or already been cancelled.
         */
        /* package */ final boolean cancel(boolean mayInterruptIfRunning) {
            final boolean result = mState.compareAndSet(RUNNING, CANCELLED);
            if (result && mayInterruptIfRunning && mRunner != null) {
                mRunner.interrupt();
            }

            return result;
        }

        /**
         * Runs on the UI thread after {@link #setProgress(Object[])} is
         * invoked. The default implementation do nothing. If you write
         * your own implementation, do not call <tt>super.onProgress()</tt>
         * @param params The parameters of the task.
         * @param values The progress values, passed earlier by {@link #setProgress}.
         */
        /* package */ void onProgress(Params[] params, Object[] values) {
        }

        /**
         * Overrides this method to perform a computation on a background thread.
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPostExecute(Params[], Result)
         */
        /* package */ abstract Result doInBackground(Params[] params);

        /**
         * Runs on the UI thread after {@link #doInBackground(Params[])}.
         * @param params The parameters of the task.
         * @param result The result, returned earlier by {@link #doInBackground}.
         * @see #doInBackground(Params[])
         */
        /* package */ abstract void onPostExecute(Params[] params, Result result);
    }
}
