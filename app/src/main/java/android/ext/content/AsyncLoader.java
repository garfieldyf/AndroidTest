package android.ext.content;

import android.ext.cache.Cache;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Pool;
import android.util.Printer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Executor;

/**
 * Class <tt>AsyncLoader</tt> allows to load the resource on a background
 * thread and bind it to target on the UI thread.
 * <h3>AsyncLoader's generic types</h3>
 * <p>The three types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Params</tt>, The load task's parameters type.</li>
 * <li><tt>Value</tt>, The value type of the load result.</li></ol>
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AsyncLoader<Key, Params, Value> {
    /**
     * If set the loader will be ignored the memory cache when it will be load value.
     */
    public static final int FLAG_IGNORE_MEMORY_CACHE = 0x08000000;    /* flags 0x0F000000 */

    /**
     * FLAG_MASK = ~(FLAG_IGNORE_MEMORY_CACHE | FLAG_DUMP_OPTIONS);
     */
    private static final int FLAG_MASK = 0xF3FFFFFF;

    private static final int RUNNING  = 0;
    private static final int PAUSED   = 1;
    private static final int SHUTDOWN = 2;

    /* package */ final Pool<Task> mTaskPool;
    /* package */ final Cache<Key, Value> mCache;

    private volatile int mState;
    private final Executor mExecutor;
    private final Map<Object, Task> mRunningTasks;

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache May be <tt>null</tt>. The {@link Cache} to store the loaded values.
     * @param maxPoolSize The maximum number of tasks to allow in the task pool.
     * @see #AsyncLoader(Executor, Cache, Pool)
     */
    public AsyncLoader(Executor executor, Cache<Key, Value> cache, int maxPoolSize) {
        this(executor, cache, newTaskPool(maxPoolSize));
    }

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache May be <tt>null</tt>. The {@link Cache} to store the loaded values.
     * @param taskPool The {@link Task} {@link Pool} to reused the <tt>Task</tt>.
     * @see #AsyncLoader(Executor, Cache, int)
     */
    public AsyncLoader(Executor executor, Cache<Key, Value> cache, Pool<Task> taskPool) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mCache = cache;
        mExecutor = executor;
        mTaskPool = taskPool;
        mRunningTasks = new HashMap<Object, Task>();
    }

    /**
     * Equivalent to calling <tt>load(key, target, 0, binder, (Params[])null)</tt>.
     * @param key May be <tt>null</tt>. The key to find value.
     * @param target The <tt>Object</tt> to bind.
     * @param binder The {@link Binder} used to bind value to <em>target</em>.
     * @see #load(Key, Object, int, Binder, Params[])
     */
    public final void load(Key key, Object target, Binder<Key, Params, Value> binder) {
        load(key, target, 0, binder, (Params[])null);
    }

    /**
     * Loads the value, bind it to the <em>target</em>. If the value is already cached, it
     * is bind immediately. Otherwise loads the value on a background thread. <p><b>Note:
     * This method must be invoked on the UI thread.</b></p>
     * @param key May be <tt>null</tt>. The key to find value.
     * @param target The <tt>Object</tt> to bind.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @param binder The {@link Binder} used to bind value to <em>target</em>.
     * @param params The parameters of the load task. If the task no parameters, you can pass
     * <em>(Params[])null</em> instead of allocating an empty array.
     * @see #load(Key, Object, Binder)
     */
    public void load(Key key, Object target, int flags, Binder<Key, Params, Value> binder, Params... params) {
        DebugUtils.__checkUIThread("load");
        DebugUtils.__checkError(target == null || binder == null, "Invalid parameters - target == null || binder == null");
        DebugUtils.__checkError((flags & FLAG_MASK) > 0xFFFF, "The custom flags (0x" + Integer.toHexString(flags & FLAG_MASK) + ") must be range of [0 - 0xFFFF]");
        if (mState != SHUTDOWN) {
            if (key == null) {
                bindValue(binder, key, params, target, null, flags);
                return;
            }

            // Loads the value from the memory cache.
            if (validateCache(flags)) {
                final Value value = mCache.get(key);
                if (value != null) {
                    bindValue(binder, key, params, target, value, flags);
                    return;
                }
            }

            // Loads the value on a background thread.
            if (!isTaskRunning(key, target)) {
                binder.bindValue(key, params, target, null, flags);
                final LoadTask task = obtain(key, params, target, flags, binder);
                mRunningTasks.put(target, task);
                mExecutor.execute(task);
            }
        }
    }

    /**
     * Loads the value synchronously. Call this method, pass the {@link #loadInBackground}
     * the <em>task</em> parameter always <tt>null</tt>.<p><b>Note: This method will block
     * the calling thread until it was returned.</b></p>
     * @param key The key to find value.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @param params The parameters to load. If no parameters, you can pass <em>(Params[])null</em>
     * instead of allocating an empty array.
     * @return The result, or <tt>null</tt> if load failed or this loader was shut down.
     */
    public Value loadSync(Key key, int flags, Params... params) {
        DebugUtils.__checkError((flags & FLAG_MASK) > 0xFFFF, "The custom flags (0x" + Integer.toHexString(flags & FLAG_MASK) + ") must be range of [0 - 0xFFFF]");
        if (key == null || mState == SHUTDOWN) {
            return null;
        }

        Value value;
        if (!validateCache(flags)) {
            value = loadInBackground(null, key, params, flags);
        } else if ((value = mCache.get(key)) == null) {
            value = loadInBackground(null, key, params, flags);
            if (value != null) {
                mCache.put(key, value);
            }
        }

        return value;
    }

    /**
     * Shutdown this loader, stop all actively running tasks
     * and no new tasks will be accepted. <p><b>Note: This
     * method must be invoked on the UI thread.</b></p>
     * @see #isShutdown()
     */
    public synchronized final void shutdown() {
        DebugUtils.__checkUIThread("shutdown");
        mState = SHUTDOWN;
        cancelAll();
        notifyAll();
        onShutdown();
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
     * Removes the value for the specified <em>key</em> from the
     * cache of this loader.
     * @param key The key to remove.
     * @return The value mapped by <em>key</em> or <tt>null</tt>
     * if there was no mapping.
     */
    public Value remove(Key key) {
        return (mCache != null ? mCache.remove(key) : null);
    }

    /**
     * Attempts to stop execution of the task with specified <em>target</em>.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param target The target to find the task.
     * @param mayInterruptIfRunning <tt>true</tt> if the thread executing the
     * task should be interrupted, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the task was cancelled, <tt>false</tt> otherwise.
     * @see #shutdown()
     * @see #isTaskCancelled(Task)
     */
    public final boolean cancelTask(Object target, boolean mayInterruptIfRunning) {
        DebugUtils.__checkUIThread("cancelTask");
        final Task task = mRunningTasks.remove(target);
        return (task != null && task.cancel(mayInterruptIfRunning));
    }

    /**
     * Returns the {@link Executor} associated with this loader.
     * @return The <tt>Executor</tt>.
     */
    public final Executor getExecutor() {
        return mExecutor;
    }

    /**
     * Returns the {@link Cache} associated with this loader.
     * @return The <tt>Cache</tt> or <tt>null</tt>.
     */
    public final Cache<Key, Value> getCache() {
        return mCache;
    }

    public final void dump(Printer printer) {
        DebugUtils.__checkUIThread("dump");
        Pools.dumpPool(mTaskPool, printer);
        final int size = mRunningTasks.size();
        if (size > 0) {
            final StringBuilder result = new StringBuilder(80);
            DeviceUtils.dumpSummary(printer, result, 80, " Dumping Running Tasks [ size = %d ] ", size);
            for (Entry<Object, Task> entry : mRunningTasks.entrySet()) {
                result.setLength(0);
                printer.println(DeviceUtils.toString(entry.getKey(), result.append("  ")).append(" ==> ").append(entry.getValue()).toString());
            }
        }
    }

    /**
     * Returns a new {@link Task} {@link Pool}.
     * @param maxSize The maximum number of tasks in the pool.
     * @return An newly created <tt>Task Pool</tt>.
     */
    public static Pool<Task> newTaskPool(int maxSize) {
        return Pools.newPool(LoadTask::new, maxSize);
    }

    /**
     * Returns the target associated with the <em>task</em>.
     * @param task The {@link Task}.
     * @return The target <tt>Object</tt> or <tt>null</tt> if
     * loads synchronously.
     */
    protected final Object getTarget(Task task) {
        return (task != null ? ((LoadTask)task).mTarget : null);
    }

    /**
     * Returns <tt>true</tt> if the <em>task</em> was cancelled before it completed
     * normally or this loader has been shut down. To ensure that the <em>task</em>
     * is cancelled as quickly as possible, you should always check the return value
     * of this method, if possible (inside a loop for instance.)
     * @param task May be <tt>null</tt>. The {@link Task} to test.
     * @return <tt>true</tt> if the <em>task</em> was cancelled or this loader has
     * been shut down, <tt>false</tt> otherwise.
     * @see #cancelTask(Object, boolean)
     */
    protected final boolean isTaskCancelled(Task task) {
        return (mState == SHUTDOWN || (task != null && task.isCancelled()));
    }

    /**
     * Called on the UI thread when this loader has been shut down.
     */
    protected void onShutdown() {
        DebugUtils.__checkDebug(true, getClass().getName(), "shutdown()");
    }

    /**
     * Called on the UI thread to recycle the <em>params</em>.
     * @param params The parameters to recycle, passed earlier by {@link #load}.
     */
    protected void onRecycle(Params[] params) {
    }

    /**
     * Called on a background thread to perform the actual load task.
     * @param task The current {@link Task} whose executing this method,
     * or <tt>null</tt> if loads synchronously.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param flags Loading flags, passed earlier by {@link #load}.
     * @return A result, defined by the subclass of this loader.
     * @see #loadSync(Key, int, Params[])
     * @see #load(Key, Object, int, Binder, Params[])
     */
    protected abstract Value loadInBackground(Task task, Key key, Params[] params, int flags);

    /**
     * Tests the cache is valid.
     */
    /* package */ final boolean validateCache(int flags) {
        return (mCache != null && (flags & FLAG_IGNORE_MEMORY_CACHE) == 0);
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
     * Returns <tt>true</tt> if the <em>task</em> was cancelled.
     */
    /* package */ final boolean isTaskCancelled(Object target, Task task) {
        if (mState == SHUTDOWN) {
            return true;
        }

        // Removes the task from running tasks if exists.
        if (mRunningTasks.get(target) == task) {
            DebugUtils.__checkDebug(task.isCancelled(), "AsyncLoader", "remove task - target = " + target);
            mRunningTasks.remove(target);
        }

        return task.isCancelled();
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
     * Tests the specified task is running.
     */
    private boolean isTaskRunning(Key key, Object target) {
        final LoadTask task = (LoadTask)mRunningTasks.get(target);
        if (task != null && !task.isCancelled()) {
            if (task.mKey.equals(key)) {
                return true;
            } else {
                task.cancel(false);
            }
        }

        return false;
    }

    /**
     * Retrieves a new {@link LoadTask} from the task pool. Allows us to avoid allocating new tasks in many cases.
     */
    private LoadTask obtain(Key key, Params[] params, Object target, int flags, Binder binder) {
        final LoadTask task = (LoadTask)mTaskPool.obtain();
        task.mKey = key;
        task.mLoader = this;
        task.mFlags  = flags;
        task.mParams = params;
        task.mBinder = binder;
        task.mTarget = target;
        return task;
    }

    /**
     * Binds the specified <em>value</em> to the specified <em>target</em>.
     */
    private void bindValue(Binder binder, Key key, Params[] params, Object target, Value value, int state) {
        // Cancel the task associated with the target.
        cancelTask(target, false);
        binder.bindValue(key, params, target, value, state);
        onRecycle(params);
    }

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link Task}.
     */
    /* package */ static final class LoadTask extends Task {
        /* package */ int mFlags;
        /* package */ Object mKey;
        /* package */ Object mTarget;
        /* package */ Binder mBinder;
        /* package */ AsyncLoader mLoader;

        @Override
        /* package */ Object doInBackground(Object params) {
            mLoader.waitResumeIfPaused();
            Object value = null;
            if (!mLoader.isTaskCancelled(this)) {
                value = mLoader.loadInBackground(this, mKey, (Object[])params, mFlags);
                if (value != null && mLoader.validateCache(mFlags)) {
                    mLoader.mCache.put(mKey, value);
                }
            }

            return value;
        }

        @Override
        /* package */ void onPostExecute(Object value) {
            final Object[] params = (Object[])mParams;
            if (!mLoader.isTaskCancelled(mTarget, this)) {
                mBinder.bindValue(mKey, params, mTarget, value, mFlags | Binder.STATE_LOAD_FROM_BACKGROUND);
            }

            // Recycles this task.
            mLoader.onRecycle(params);
            recycle(mLoader.mTaskPool);
        }

        private void recycle(Pool<Task> taskPool) {
            clearForRecycle();
            mKey = null;
            mTarget = null;
            mBinder = null;
            mLoader = null;
            taskPool.recycle(this);
        }
    }

    /**
     * Callback interface used to bind the value to the target.
     */
    public static interface Binder<Key, Params, Value> {
        /**
         * Indicates the value load from a background thread.
         */
        int STATE_LOAD_FROM_BACKGROUND = 0x80000000;    /* state 0xF0000000 */

        /**
         * Binds the specified <em>value</em> to the specified <em>target</em> on the
         * UI thread.
         * @param key The key, passed earlier by {@link #load}.
         * @param params The parameters, passed earlier by {@link #load}.
         * @param target The <tt>Object</tt> to bind the <em>value</em>, passed earlier
         * by {@link #load}.
         * @param value The load result or <tt>null</tt>.
         * @param state May be <tt>0</tt> or any combination of <tt>STATE_XXX</tt> and
         * <tt>FLAG_XXX</tt> constants.
         */
        void bindValue(Key key, Params[] params, Object target, Value value, int state);

        /**
         * Returns a type-safe empty {@link Binder} associated with this class.
         * The empty <tt>Binder</tt> {@link #bindValue} implementation do nothing.
         * @return An empty <tt>Binder</tt>.
         */
        public static <Key, Params, Value> Binder<Key, Params, Value> emptyBinder() {
            return (key, params, target, value, state) -> { };
        }
    }
}
