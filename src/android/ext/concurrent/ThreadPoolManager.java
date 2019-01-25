package android.ext.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.util.Printer;

/**
 * Class ThreadPoolManager
 * @author Garfield
 */
public class ThreadPoolManager extends ThreadPool {
    private final Queue<Task> mRunningTasks;

    /**
     * Constructor
     * <P>Creates a new <tt>ThreadPoolManager</tt> to execute the given task. At any point,
     * at most <em>maxThreads</em> threads will be active processing tasks. If additional
     * tasks are submitted when all threads are active, they will wait in the queue until
     * a thread is available. Threads that have not been used for <em>60</em> seconds are
     * terminated and removed from the cache.</P>
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @see #ThreadPoolManager(int, long, TimeUnit)
     */
    public ThreadPoolManager(int maxThreads) {
        this(maxThreads, 60, TimeUnit.SECONDS);
    }

    /**
     * Constructor
     * <P>Like as {@link #ThreadPoolManager(int)}, but the threads will wait <em>keepAliveTime</em>
     * for new tasks before terminating in this pool.</P>
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @param keepAliveTime The maximum time that excess idle threads will wait for new tasks before
     * terminating.
     * @param unit The time unit for the <em>keepAliveTime</em> parameter.
     * @see #ThreadPoolManager(int)
     */
    public ThreadPoolManager(int maxThreads, long keepAliveTime, TimeUnit unit) {
        super(maxThreads, keepAliveTime, unit);
        mRunningTasks = new ConcurrentLinkedQueue<Task>();
    }

    /**
     * Attempts to stop the {@link Task} with specified identifier from the internal queue.
     * @param id A unique identifier for the task to cancel.
     * @param mayInterruptIfRunning <tt>true</tt> if the specified <tt>Task</tt> should be
     * interrupted, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the task was cancelled, <tt>false</tt> otherwise.
     * @see Task#getId()
     * @see #cancelAll(boolean, boolean)
     */
    public boolean cancel(long id, boolean mayInterruptIfRunning) {
        // Cancel and remove from running task queue.
        final Iterator<Task> itor = mRunningTasks.iterator();
        while (itor.hasNext()) {
            if (itor.next().cancel(id, mayInterruptIfRunning)) {
                itor.remove();
                return true;
            }
        }

        // Cancel and remove from pending task queue.
        final Iterator<Runnable> iter = getQueue().iterator();
        while (iter.hasNext()) {
            final Runnable task = iter.next();
            if (task instanceof Task && ((Task)task).cancel(id, false)) {
                iter.remove();
                return true;
            }
        }

        return false;
    }

    /**
     * Attempts to stop all pending and running {@link Task}s from the internal queue.
     * @param mayInterruptIfRunning <tt>true</tt> if all the running {@link Task}s should
     * be interrupted, <tt>false</tt> otherwise.
     * @param mayNotifyIfCancelled <tt>true</tt> if all cancelled {@link Task}s should be
     * call {@link Task#onCancelled() onCancelled()}, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if at least one task was cancelled, <tt>false</tt> otherwise.
     * @see #cancel(long, boolean)
     */
    public boolean cancelAll(boolean mayInterruptIfRunning, boolean mayNotifyIfCancelled) {
        // Cancel and remove from pending task queue.
        boolean result = false;
        final Iterator<Runnable> itor = getQueue().iterator();
        while (itor.hasNext()) {
            final Runnable task = itor.next();
            if (task instanceof Task) {
                result |= ((Task)task).cancel(false, mayNotifyIfCancelled);
                itor.remove();
            }
        }

        // Cancel and remove from running task queue.
        final Iterator<Task> iter = mRunningTasks.iterator();
        while (iter.hasNext()) {
            result |= iter.next().cancel(mayInterruptIfRunning, mayNotifyIfCancelled);
            iter.remove();
        }

        return result;
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(96);
        dumpQueue(printer, result, mRunningTasks, " Dumping %s Running Tasks [ size = %d ] ");
        dumpQueue(printer, result, getQueue(), " Dumping %s Pending Tasks [ size = %d ] ");
    }

    @Override
    protected void beforeExecute(Thread thread, Runnable target) {
        if (target instanceof Task) {
            mRunningTasks.offer((Task)target);
        }
    }

    @Override
    protected void afterExecute(Runnable target, Throwable throwable) {
        if (target instanceof Task) {
            mRunningTasks.remove(target);
        }
    }

    private void dumpQueue(Printer printer, StringBuilder result, Queue<?> queue, String format) {
        final List<Object> tasks = new ArrayList<Object>(queue);
        DebugUtils.dumpSummary(printer, result, 80, format, getClass().getSimpleName(), tasks.size());
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
        public final boolean isCancelled() {
            return (mState.get() == CANCELLED);
        }

        @Override
        public final void run() {
            if (mState.get() == RUNNING) {
                try {
                    onExecute(mRunner = Thread.currentThread());
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
        public abstract long getId();

        /**
         * Callback method to be invoked when this task was cancelled.
         * The default implementation do nothing. If you write your
         * own implementation, do not call <tt>super.onCancelled()</tt>
         * @see #onExecute(Thread)
         * @see #onCompletion()
         */
        public void onCancelled() {
        }

        /**
         * Runs on a background thread after {@link #onExecute(Thread)}.
         * The default implementation do nothing. If you write your
         * own implementation, do not call <tt>super.onCompletion()</tt>
         * <p>This method won't be invoked if this task was cancelled.</p>
         * @see #onExecute(Thread)
         * @see #onCancelled()
         */
        public void onCompletion() {
        }

        /**
         * Runs on a background thread when this task is executing. <p>This method
         * won't be invoked if this task was cancelled when it has no start.</p>
         * @param thread The <tt>Thread</tt> whose executing this task.
         * @see #onCancelled()
         * @see #onCompletion()
         */
        protected abstract void onExecute(Thread thread);

        /**
         * Attempts to stop execution of this task. This attempt will fail
         * if this task has already completed, or already been cancelled.
         */
        /* package */ final boolean cancel(boolean interrupt, boolean notify) {
            final boolean result = mState.compareAndSet(RUNNING, CANCELLED);
            if (result) {
                if (interrupt && mRunner != null) {
                    mRunner.interrupt();
                }

                // Notify the callback method.
                if (notify) {
                    onCancelled();
                }
            }

            return result;
        }

        /**
         * Attempts to stop execution of this task. This attempt will fail
         * if this task has already completed, or already been cancelled.
         */
        /* package */ final boolean cancel(long id, boolean mayInterruptIfRunning) {
            return (getId() == id && cancel(mayInterruptIfRunning, true));
        }
    }
}