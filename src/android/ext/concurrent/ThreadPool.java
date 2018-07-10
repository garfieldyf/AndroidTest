package android.ext.concurrent;

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
 * @version 2.0
 */
public class ThreadPool extends ThreadPoolExecutor implements RejectedExecutionHandler {
    /**
     * The serial <tt>Executor</tt>.
     */
    private Executor mSerialExecutor;

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
        this(coreThreads, maxThreads, 60, TimeUnit.SECONDS);
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
        super(coreThreads, maxThreads, keepAliveTime, unit, new SynchronousQueue<Runnable>(), new PriorityThreadFactory());
        setRejectedExecutionHandler(this);
        mPendingTasks = new ConcurrentLinkedQueue<Runnable>();
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
     * Returns the serial {@link Executor} associated with this pool.
     * @return The serial <tt>Executor</tt>.
     * @see #newSerialExecutor()
     */
    public synchronized final Executor getSerialExecutor() {
        if (mSerialExecutor == null) {
            mSerialExecutor = new SerialExecutor();
        }

        return mSerialExecutor;
    }

    /**
     * Creates a new serial {@link Executor}.
     * @return The newly created serial <tt>Executor</tt>.
     * @see #getSerialExecutor()
     */
    public final Executor newSerialExecutor() {
        return new SerialExecutor();
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
        return Math.min(Runtime.getRuntime().availableProcessors() << 1, 8);
    }

    @Override
    protected void afterExecute(Runnable target, Throwable throwable) {
        final Runnable task = mPendingTasks.poll();
        if (task != null) {
            UIHandler.sInstance.execute(this, task);
        }
    }

    /**
     * Class <tt>SerialExecutor</tt> is an implementation of an {@link Executor}.
     */
    private final class SerialExecutor implements Executor, Runnable {
        private static final int IDLE    = 0;
        private static final int RUNNING = 1;

        private final AtomicInteger mState;
        private final Queue<Runnable> mQueue;

        public SerialExecutor() {
            mState = new AtomicInteger(IDLE);
            mQueue = new ConcurrentLinkedQueue<Runnable>();
        }

        @Override
        public void execute(Runnable task) {
            // Adds the new task to the task queue.
            mQueue.offer(task);

            // If this executor is not running, run it.
            if (mState.compareAndSet(IDLE, RUNNING)) {
                ThreadPool.this.execute(this);
            }
        }

        @Override
        public void run() {
            try {
                Runnable task;
                while ((task = mQueue.poll()) != null) {
                    task.run();
                }
            } finally {
                // Reset the state to idle.
                mState.set(IDLE);
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
}