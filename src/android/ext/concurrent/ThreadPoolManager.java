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
 * @version 2.0
 */
public class ThreadPoolManager extends ThreadPool {
    private final Queue<Task> mRunningTasks;

    /**
     * Constructor
     * <P>Creates a new <tt>ThreadPoolManager</tt> to execute the given task. At any point,
     * at most <em>maxThreads</em> threads will be active processing tasks. If additional
     * tasks are submitted when all threads are active, they will wait in the queue until
     * a thread is available.</P>
     * <P>Calls to <em>execute</em> will reuse previously constructed threads if available.
     * If no existing thread is available, a new thread will be created and added to this pool.
     * Threads that have not been used for <em>60</em> seconds are terminated and removed from
     * the cache.</P>
     * @param coreThreads The number of threads to keep in this pool, even if they are idle.
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @see #ThreadPoolManager(int, int, long, TimeUnit)
     */
    public ThreadPoolManager(int coreThreads, int maxThreads) {
        this(coreThreads, maxThreads, 60, TimeUnit.SECONDS);
    }

    /**
     * Constructor
     * <P>Like as {@link #ThreadPoolManager(int, int)}, but the threads will wait <em>keepAliveTime</em>
     * for new tasks before terminating in this pool.</P>
     * @param coreThreads The number of threads to keep in this pool, even if they are idle.
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @param keepAliveTime The maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit The time unit for the <em>keepAliveTime</em> parameter.
     * @see #ThreadPoolManager(int, int)
     */
    public ThreadPoolManager(int coreThreads, int maxThreads, long keepAliveTime, TimeUnit unit) {
        super(coreThreads, maxThreads, keepAliveTime, unit);
        mRunningTasks = new ConcurrentLinkedQueue<Task>();
    }

    /**
     * Attempts to stop all pending and running {@link Task}s from the internal queue.
     * @param mayInterruptIfRunning <tt>true</tt> if this pool executing all the running
     * <tt>Tasks</tt> should be interrupted, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if at least one task was cancelled, <tt>false</tt> otherwise.
     * @see #cancel(long, boolean)
     */
    public boolean cancelAll(boolean mayInterruptIfRunning) {
        // Cancel and remove from pending task queue.
        boolean result = false;
        final Iterator<Runnable> iter = mPendingTasks.iterator();
        while (iter.hasNext()) {
            final Runnable task = iter.next();
            if (task instanceof Task) {
                result |= ((Task)task).cancel(false);
                iter.remove();
            }
        }

        // Cancel and remove from running task queue.
        final Iterator<Task> itor = mRunningTasks.iterator();
        while (itor.hasNext()) {
            result |= itor.next().cancel(mayInterruptIfRunning);
            itor.remove();
        }

        return result;
    }

    /**
     * Attempts to stop the {@link Task} with specified identifier from the internal queue.
     * @param id The identifier of the task to cancel.
     * @param mayInterruptIfRunning <tt>true</tt> if the specified <tt>Task</tt> should be
     * interrupted, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the task was cancelled, <tt>false</tt> otherwise.
     * @see #cancelAll(boolean)
     */
    public boolean cancel(long id, boolean mayInterruptIfRunning) {
        // Cancel and remove from running task queue.
        final Iterator<Task> iter = mRunningTasks.iterator();
        while (iter.hasNext()) {
            if (iter.next().cancel(id, mayInterruptIfRunning)) {
                iter.remove();
                return true;
            }
        }

        // Cancel and remove from pending task queue.
        final Iterator<Runnable> itor = mPendingTasks.iterator();
        while (itor.hasNext()) {
            final Runnable task = itor.next();
            if (task instanceof Task && ((Task)task).cancel(id, false)) {
                itor.remove();
                return true;
            }
        }

        return false;
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(96);
        final String className = getClass().getSimpleName();
        dumpQueue(printer, result, mRunningTasks, className, " Dumping %s Running Tasks [ size = %d ] ");
        dumpQueue(printer, result, mPendingTasks, className, " Dumping %s Pending Tasks [ size = %d ] ");
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

        super.afterExecute(target, throwable);
    }

    private static void dumpQueue(Printer printer, StringBuilder result, Queue<?> queue, String className, String format) {
        final List<Object> tasks = new ArrayList<Object>(queue);
        DebugUtils.dumpSummary(printer, result, 80, format, className, tasks.size());
        for (int i = 0, size = tasks.size(); i < size; ++i) {
            result.setLength(0);
            printer.println(result.append("  ").append(tasks.get(i)).toString());
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
        private volatile Thread runner;

        /**
         * Possible state transitions:
         * <ul><li>RUNNING -> CANCELLED</li>
         * <li>RUNNING -> COMPLETED</li></ul>
         */
        private final AtomicInteger state;

        /**
         * Constructor
         */
        public Task() {
            state = new AtomicInteger(RUNNING);
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            final boolean result = state.compareAndSet(RUNNING, CANCELLED);
            if (result && mayInterruptIfRunning && runner != null) {
                runner.interrupt();
            }

            return result;
        }

        @Override
        public final boolean isCancelled() {
            return (state.get() == CANCELLED);
        }

        @Override
        public final void run() {
            if (state.get() == RUNNING) {
                try {
                    onExecute(runner = Thread.currentThread());
                } finally {
                    runner = null;
                    if (state.compareAndSet(RUNNING, COMPLETED)) {
                        onCompletion();
                    }
                }
            }
        }

        /**
         * Returns the identifier associated with this task.
         * @return This task's identifier.
         */
        public abstract long getId();

        /**
         * Callback method to be invoked when this task was finished.
         * The default implementation do nothing. If you write your
         * own implementation, do not call <tt>super.onCompletion()</tt>
         * <p>This method won't be invoked if this task was cancelled.</p>
         * @see #onExecute(Thread)
         */
        protected void onCompletion() {
        }

        /**
         * Callback method to be invoked when this task is executing.
         * @param thread The <tt>Thread</tt> whose executing this task.
         * @see #onCompletion()
         */
        protected abstract void onExecute(Thread thread);

        /**
         * Attempts to stop execution of this task.
         */
        /* package */ final boolean cancel(long id, boolean mayInterruptIfRunning) {
            return (getId() == id && cancel(mayInterruptIfRunning));
        }
    }
}