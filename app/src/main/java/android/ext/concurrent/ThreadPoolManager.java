package android.ext.concurrent;

import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.os.Process;
import android.util.Printer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class ThreadPoolManager
 * @author Garfield
 */
public class ThreadPoolManager extends ThreadPool {
    private final Collection<Task> mRunningTasks;

    /**
     * Constructor
     * <P>Creates a new <tt>ThreadPoolManager</tt> to execute the given task. At any point,
     * at most <em>maxThreads</em> threads will be active processing tasks. If additional
     * tasks are submitted when all threads are active, they will wait in the queue until
     * a thread is available. Threads that have not been used for <em>60</em> seconds are
     * terminated and removed from the cache.</P>
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @see #ThreadPoolManager(int, long, TimeUnit, int)
     */
    public ThreadPoolManager(int maxThreads) {
        this(maxThreads, 60, TimeUnit.SECONDS, Process.THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * Constructor
     * <P>Like as {@link #ThreadPoolManager(int)}, but the threads will wait <em>keepAliveTime</em>
     * for new tasks before terminating in this pool.</P>
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @param keepAliveTime The maximum time that excess idle threads will wait for new tasks before
     * terminating.
     * @param unit The time unit for the <em>keepAliveTime</em> parameter.
     * @param priority The priority to run the work thread at. The value supplied must be from
     * {@link Process} and not from {@link Thread}.
     * @see #ThreadPoolManager(int)
     */
    public ThreadPoolManager(int maxThreads, long keepAliveTime, TimeUnit unit, int priority) {
        super(maxThreads, keepAliveTime, unit, "PoolM-", priority);
        mRunningTasks = new LinkedList<Task>();
    }

    /**
     * Attempts to stop all pending and running {@link Task}s from the internal queue.
     * @param mayInterruptIfRunning <tt>true</tt> if all the running {@link Task}s should
     * be interrupted, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if any tasks were cancelled, <tt>false</tt> otherwise.
     * @see #cancel(long, boolean)
     */
    public boolean cancelAll(boolean mayInterruptIfRunning) {
        // Cancel and remove from pending task queue.
        final boolean result = cancelAll(getQueue(), false);

        // Cancel and remove from running task queue.
        synchronized (mRunningTasks) {
            return (result | cancelAll(mRunningTasks, mayInterruptIfRunning));
        }
    }

    /**
     * Attempts to stop the {@link Task} with specified identifier from the internal queue.
     * @param id A unique identifier for the task to cancel.
     * @param mayInterruptIfRunning <tt>true</tt> if the specified <tt>Task</tt> should be
     * interrupted, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the task was cancelled, <tt>false</tt> otherwise.
     * @see Task#getId()
     * @see #cancelAll(boolean)
     */
    public boolean cancel(long id, boolean mayInterruptIfRunning) {
        // Cancel and remove from running task queue.
        synchronized (mRunningTasks) {
            final Iterator<Task> itor = mRunningTasks.iterator();
            while (itor.hasNext()) {
                final Task task = itor.next();
                if (task.getId() == id) {
                    itor.remove();
                    return task.cancel(mayInterruptIfRunning);
                }
            }
        }

        // Cancel and remove from pending task queue.
        final Iterator<Runnable> iter = getQueue().iterator();
        while (iter.hasNext()) {
            final Task task = (Task)iter.next();
            if (task.getId() == id) {
                iter.remove();
                return task.cancel(false);
            }
        }

        return false;
    }

    public final void dump(Printer printer) {
        final String className = getClass().getSimpleName();
        final StringBuilder result = new StringBuilder(96);
        dumpTasks(printer, result, getRunningTasks(), className, "Running");
        dumpTasks(printer, result, new ArrayList<Object>(getQueue()), className, "Pending");
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable target) {
        DebugUtils.__checkError(!(target instanceof Task), "Cannot execute " + target + ", The task must be " + Task.class.getName());
        synchronized (mRunningTasks) {
            mRunningTasks.add((Task)target);
        }
    }

    @Override
    protected void afterExecute(Runnable target, Throwable throwable) {
        DebugUtils.__checkError(!(target instanceof Task), "Cannot execute " + target + ", The task must be " + Task.class.getName());
        synchronized (mRunningTasks) {
            mRunningTasks.remove(target);
        }
    }

    private Collection<?> getRunningTasks() {
        synchronized (mRunningTasks) {
            return new ArrayList<Object>(mRunningTasks);
        }
    }

    private static boolean cancelAll(Collection<?> tasks, boolean mayInterruptIfRunning) {
        boolean result = false;
        if (!tasks.isEmpty()) {
            for (Object task : tasks) {
                result |= ((Task)task).cancel(mayInterruptIfRunning);
            }

            tasks.clear();
        }

        return result;
    }

    private static void dumpTasks(Printer printer, StringBuilder result, Collection<?> tasks, String className, String namePrefix) {
        DeviceUtils.dumpSummary(printer, result, 80, " Dumping %s %s Tasks [ size = %d ] ", className, namePrefix, tasks.size());
        for (Object task : tasks) {
            result.setLength(0);
            printer.println(result.append("  ").append(task).toString());
        }
    }

    /**
     * This abstract class should be implemented by any class whose
     * instances are intended to be executed by {@link ThreadPoolManager}.
     */
    public static abstract class Task implements Runnable, Cancelable {
        private static final int RUNNING   = 0;
        private static final int CANCELLED = 1;
        private static final int COMPLETED = 2;

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
        public Task() {
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
            if (mState.get() == RUNNING) {
                try {
                    mRunner = Thread.currentThread();
                    doInBackground();
                } finally {
                    mRunner = null;
                    if (mState.compareAndSet(RUNNING, COMPLETED)) {
                        onCompletion();
                    }
                }
            }
        }

        /**
         * Returns a unique identifier associated with this task.
         * @return This task's identifier.
         */
        protected abstract long getId();

        /**
         * Runs on a background thread after {@link #doInBackground()}.
         * <p>This method won't be invoked if this task was cancelled.</p>
         * @see #doInBackground()
         */
        protected abstract void onCompletion();

        /**
         * Runs on a background thread when this task is executing. <p>This method
         * won't be invoked if this task was cancelled when it has no start.</p>
         * @see #onCompletion()
         */
        protected abstract void doInBackground();
    }
}