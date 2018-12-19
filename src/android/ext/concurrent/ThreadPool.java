package android.ext.concurrent;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import android.ext.util.UIHandler;
import android.os.Process;

/**
 * Class ThreadPool
 * @author Garfield
 */
public class ThreadPool extends ThreadPoolExecutor implements RejectedExecutionHandler {
    /**
     * The pending task queue associated with this pool.
     */
    /* package */ final Queue<Runnable> mPendingTasks;

    /**
     * Constructor
     * <p>Creates a new <tt>ThreadPool</tt> to execute the given task. At any point, at most
     * <em>maxThreads</em> threads will be active processing tasks. If additional tasks are
     * submitted when all threads are active, they will wait in the queue until a thread is
     * available.</p>
     * <p>Calls to <em>execute</em> will reuse previously constructed threads if available.
     * If no existing thread is available, a new thread will be created and added to this pool.
     * Threads that have not been used for <em>60</em> seconds are terminated and removed from
     * the cache.</p>
     * @param coreThreads The number of threads to keep in this pool, even if they are idle.
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @see #ThreadPool(int, int, long, TimeUnit)
     * @see #computeMaximumThreads()
     */
    public ThreadPool(int coreThreads, int maxThreads) {
        this(coreThreads, maxThreads, 60, TimeUnit.SECONDS, new PriorityThreadFactory());
    }

    /**
     * Constructor
     * <p>Like as {@link #ThreadPool(int, int)}, but the threads will wait <em>keepAliveTime</em>
     * for new tasks before terminating in this pool.</p>
     * @param coreThreads The number of threads to keep in this pool, even if they are idle.
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @param keepAliveTime The maximum time that excess idle threads will wait for new tasks before terminating.
     * @param unit The time unit for the <em>keepAliveTime</em> parameter.
     * @see #ThreadPool(int, int)
     * @see #computeMaximumThreads()
     */
    public ThreadPool(int coreThreads, int maxThreads, long keepAliveTime, TimeUnit unit) {
        this(coreThreads, maxThreads, keepAliveTime, unit, new PriorityThreadFactory());
    }

    /**
     * @see #removeAll()
     */
    @Override
    public boolean remove(Runnable task) {
        return mPendingTasks.remove(task);
    }

    /**
     * Removes all pending tasks from the internal queue.
     * @see #remove(Runnable)
     */
    public void removeAll() {
        mPendingTasks.clear();
    }

    /**
     * Returns a new serial {@link Executor}. A serial <tt>Executor</tt>
     * that executes tasks one at a time in serial order.
     * @return A newly serial <tt>Executor</tt>.
     */
    public final Executor newSerialExecutor() {
        return new SerialExecutor(this);
    }

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        mPendingTasks.offer(task);
    }

    /**
     * Computes the maximum number of threads to allow in the thread pool.
     * @return The maximum number of threads.
     */
    public static int computeMaximumThreads() {
        return Math.min(Runtime.getRuntime().availableProcessors() * 2 + 1, 6);
    }

    /**
     * Creates a new <tt>ThreadPool</tt> to execute the given task.
     * @param coreThreads The number of threads to keep in this pool, even if they are idle.
     * @param maxThreads The maximum number of threads to allow in this pool.
     * @return The {@link ThreadPool} object.
     */
    public static ThreadPool createImageThreadPool(int coreThreads, int maxThreads) {
        return new ThreadPool(coreThreads, maxThreads, 60, TimeUnit.SECONDS, new ImageThreadFactory());
    }

    @Override
    protected void afterExecute(Runnable target, Throwable throwable) {
        final Runnable task = mPendingTasks.poll();
        if (task != null) {
            UIHandler.sInstance.execute(this, task);
        }
    }

    private ThreadPool(int coreThreads, int maxThreads, long keepAliveTime, TimeUnit unit, ThreadFactory factory) {
        super(coreThreads, maxThreads, keepAliveTime, unit, new SynchronousQueue<Runnable>(), factory);
        setRejectedExecutionHandler(this);
        mPendingTasks = new ConcurrentLinkedQueue<Runnable>();
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
        private static int sequence;
        private final String namePrefix;
        private final AtomicInteger nameSuffix;

        public PriorityThreadFactory() {
            this.nameSuffix = new AtomicInteger();
            this.namePrefix = "Pool-" + (++sequence) + "-thread-";
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return new PriorityThread(runnable, namePrefix + nameSuffix.incrementAndGet());
        }
    }

    /**
     * Class <tt>ImageThreadFactory</tt> is an implementation of a {@link ThreadFactory}.
     */
    private static final class ImageThreadFactory implements ThreadFactory {
        private final AtomicInteger nameSuffix;

        public ImageThreadFactory() {
            this.nameSuffix = new AtomicInteger();
        }

        @Override
        public Thread newThread(Runnable runnable) {
            return new PriorityThread(runnable, "ImagePool-thread-" + nameSuffix.incrementAndGet());
        }
    }
}