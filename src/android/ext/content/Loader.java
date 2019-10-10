package android.ext.content;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import android.content.Context;
import android.ext.content.Loader.Task;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.ext.util.UIHandler;
import android.util.Printer;

/**
 * An abstract class that performs asynchronous loading of data.
 * @author Garfield
 */
public abstract class Loader<Key> implements Factory<Task> {
    /* package */ static final int RUNNING  = 0;
    /* package */ static final int PAUSED   = 1;
    /* package */ static final int SHUTDOWN = 2;

    /* package */ volatile int mState;
    /* package */ final Executor mExecutor;

    /* package */ final Pool<Task> mTaskPool;
    /* package */ final Map<Key, Task> mRunningTasks;

    /**
     * Constructor
     */
    /* package */ Loader(Executor executor, int maxPoolSize) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mExecutor = executor;
        mTaskPool = Pools.newPool(this, maxPoolSize);
        mRunningTasks = new HashMap<Key, Task>();
    }

    /**
     * Shutdown this loader, stop all actively running tasks
     * and no new tasks will be accepted.
     * @see #isShutdown()
     */
    public synchronized void shutdown() {
        DebugUtils.__checkUIThread("shutdown");
        mState = SHUTDOWN;
        cancelAll();
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
    public synchronized final void pause() {
        if (mState != SHUTDOWN) {
            mState = PAUSED;
        }
    }

    /**
     * Resumes all actively running tasks.
     * @see #pause()
     */
    public synchronized final void resume() {
        if (mState != SHUTDOWN) {
            mState = RUNNING;
            notifyAll();
        }
    }

    /**
     * Returns the {@link Executor} associated with this loader.
     * @return The <tt>Executor</tt>.
     */
    public final Executor getExecutor() {
        return mExecutor;
    }

    /**
     * Attempts to stop execution of the specified task. After invoking this
     * method, you should check the value returned by {@link #isTaskCancelled(Task)}
     * periodically from the background thread to finish the task as soon as
     * possible. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param key The key to find the task.
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing the
     * task should be interrupted, <tt>false</tt> otherwise.
     * @return <tt>false</tt> if the task could not be cancelled, typically
     * because it has already completed, <tt>true</tt> otherwise.
     * @see #shutdown()
     * @see #isTaskCancelled(Task)
     */
    public final boolean cancelTask(Key key, boolean mayInterruptIfRunning) {
        DebugUtils.__checkUIThread("cancelTask");
        final Task task = mRunningTasks.remove(key);
        return (task != null && task.cancel(mayInterruptIfRunning));
    }

    public void dump(Context context, Printer printer) {
        DebugUtils.__checkUIThread("dump");
        Pools.dumpPool(mTaskPool, printer);
        final int size = mRunningTasks.size();
        if (size > 0) {
            final StringBuilder result = new StringBuilder(80);
            DebugUtils.dumpSummary(printer, result, 80, " Dumping Running Tasks [ size = %d ] ", size);
            for (Entry<Key, Task> entry : mRunningTasks.entrySet()) {
                result.setLength(0);
                printer.println(DebugUtils.toSimpleString(entry.getKey(), result.append("  ")).append(" ==> ").append(entry.getValue()).toString());
            }
        }
    }

    /**
     * Returns <tt>true</tt> if the <em>task</em> was cancelled before it
     * completed normally. To ensure that the <em>task</em> is cancelled
     * as quickly as possible, you should always check the return value
     * of this method, if possible (inside a loop for instance.)
     * @param task May be <tt>null</tt>. The {@link Task} to test.
     * @return <tt>true</tt> if the <em>task</em> was cancelled or this
     * loader has been shut down, <tt>false</tt> otherwise.
     * @see #cancelTask(Object, boolean)
     */
    protected final boolean isTaskCancelled(Task task) {
        return (mState == SHUTDOWN || (task != null && task.isCancelled()));
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
        if (mRunningTasks.size() > 0) {
            for (Task task : mRunningTasks.values()) {
                task.cancel(false);
            }

            mRunningTasks.clear();
        }
    }

    /**
     * This abstract class should be implemented by any class whose instances are intended to be execute.
     */
    public static abstract class Task implements Runnable, Cancelable {
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

        /**
         * Attempts to stop execution of this task. This attempt will fail if this task
         * has already completed, or already been cancelled.
         * @param mayInterruptIfRunning <tt>true</tt> if the thread executing this task
         * should be interrupted, <tt>false</tt> otherwise.
         * @return <tt>true</tt> if this task has been cancelled, <tt>false</tt> otherwise.
         */
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

            UIHandler.sInstance.finish(this, result);
        }

        /**
         * Clears all fields for recycle.
         */
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
                UIHandler.sInstance.setProgress(this, value);
            }
        }

        /**
         * Runs on the UI thread after {@link #setProgress} is invoked.
         * @param value The progress value to update.
         */
        public void onProgress(Object value) {
        }

        /**
         * Runs on the UI thread after {@link #doInBackground}.
         * @param result The result, returned earlier by {@link #doInBackground}.
         * @see #doInBackground(Object)
         */
        public abstract void onPostExecute(Object result);

        /**
         * Overrides this method to perform a computation on a background thread.
         * @param params The parameter of this task.
         * @return A result, defined by the subclass of this task.
         * @see #onPostExecute(Object)
         */
        public abstract Object doInBackground(Object params);
    }
}
