package android.ext.concurrent;

import java.util.ArrayDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import android.ext.util.ArrayUtils;
import android.os.Process;

/**
 * Class ThreadPool
 * @author Garfield
 */
public class ThreadPool extends ThreadPoolExecutor {
    /**
     * Constructor
     * <p>Creates a new <tt>ThreadPool</tt> to execute the given task. At any point, at most
     * <em>maxThreads</em> threads will be active processing tasks. If additional tasks are
     * submitted when all threads are active, they will wait in the queue until a thread is
     * available. Threads that have not been used for <em>60</em> seconds are terminated and
     * removed from the cache.</p>
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @see #ThreadPool(int, long, TimeUnit)
     * @see #computeMaximumThreads()
     */
    public ThreadPool(int maxThreads) {
        this(maxThreads, 60, TimeUnit.SECONDS, "Pool-thread-");
    }

    /**
     * Constructor
     * <p>Like as {@link #ThreadPool(int)}, but the threads will wait <em>keepAliveTime</em>
     * for new tasks before terminating in this pool.</p>
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @param keepAliveTime The maximum time that excess idle threads will wait for new tasks
     * before terminating.
     * @param unit The time unit for the <em>keepAliveTime</em> parameter.
     * @see #ThreadPool(int)
     * @see #computeMaximumThreads()
     */
    public ThreadPool(int maxThreads, long keepAliveTime, TimeUnit unit) {
        this(maxThreads, keepAliveTime, unit, "Pool-thread-");
    }

    /**
     * Removes all pending tasks from the internal task queue.
     * @see #remove(Runnable)
     */
    public void removeAll() {
        getQueue().clear();
    }

    /**
     * Returns a new serial {@link Executor}. A serial <tt>Executor</tt>
     * that executes tasks one at a time in serial order.
     * @return A newly serial <tt>Executor</tt>.
     */
    public final Executor createSerialExecutor() {
        return new SerialExecutor(this);
    }

    /**
     * Computes the maximum number of threads to allow in the thread pool.
     * @return The maximum number of threads.
     */
    public static int computeMaximumThreads() {
        return ArrayUtils.rangeOf(Runtime.getRuntime().availableProcessors() * 2 + 1, 5, 8);
    }

    /**
     * Creates a new <tt>ThreadPool</tt> to execute the given task.
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @param keepAliveTime The maximum time that excess idle threads will wait
     * for new tasks before terminating.
     * @param unit The time unit for the <em>keepAliveTime</em> parameter.
     * @return A {@link ThreadPool} instance.
     */
    public static ThreadPool createImageThreadPool(int maxThreads, long keepAliveTime, TimeUnit unit) {
        return new ThreadPool(maxThreads, keepAliveTime, unit, "ImagePool-thread-");
    }

    /**
     * Constructor
     */
    /* package */ ThreadPool(int maxThreads, long keepAliveTime, TimeUnit unit, String namePrefix) {
        super(maxThreads, maxThreads, keepAliveTime, unit, new LinkedBlockingQueue<Runnable>(), new PriorityThreadFactory(namePrefix));
        allowCoreThreadTimeOut(true);
    }

    /**
     * Class <tt>SerialExecutor</tt> is an implementation of an {@link Executor}.
     */
    private static final class SerialExecutor implements Executor {
        private Runnable mActive;
        private final Executor mExecutor;
        private final ArrayDeque<Runnable> mTasks;

        public SerialExecutor(Executor executor) {
            mExecutor = executor;
            mTasks = new ArrayDeque<Runnable>();
        }

        @Override
        public synchronized void execute(Runnable task) {
            // Adds the new task to the task queue.
            mTasks.addLast(new Task(task));

            // If mActive is not running, run it.
            if (mActive == null) {
                scheduleNext();
            }
        }

        public synchronized void scheduleNext() {
            if ((mActive = mTasks.pollFirst()) != null) {
                mExecutor.execute(mActive);
            }
        }

        /**
         * Class <tt>Task</tt> is an implementation of an {@link Runnable}.
         */
        private final class Task implements Runnable {
            private final Runnable task;

            public Task(Runnable task) {
                this.task = task;
            }

            @Override
            public void run() {
                try {
                    task.run();
                } finally {
                    // Executes the next task.
                    scheduleNext();
                }
            }
        }
    }

    /**
     * Class <tt>PriorityThread</tt> is an implementation of a {@link Thread}.
     */
    private static final class PriorityThread extends Thread {
        public PriorityThread(Runnable runnable, String threadName) {
            super(runnable, threadName);
        }

        @Override
        public void run() {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            super.run();
        }
    }

    /**
     * Class <tt>PriorityThreadFactory</tt> is an implementation of a {@link ThreadFactory}.
     */
    private static final class PriorityThreadFactory implements ThreadFactory {
        private final String namePrefix;
        private final AtomicInteger nameSuffix;

        public PriorityThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
            this.nameSuffix = new AtomicInteger();
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return new PriorityThread(runnable, namePrefix + nameSuffix.incrementAndGet());
        }
    }
}
