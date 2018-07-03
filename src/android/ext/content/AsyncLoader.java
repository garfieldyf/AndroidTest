package android.ext.content;

import java.util.concurrent.Executor;
import android.ext.cache.Cache;
import android.ext.util.DebugUtils;

/**
 * Class <tt>AsyncLoader</tt> allows to load the resource on
 * a background thread and bind it to target on the UI thread.
 * <h5>AsyncLoader's generic types</h5>
 * <p>The three types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Params</tt>, The load task's parameters type.</li>
 * <li><tt>Value</tt>, The value type of the load result.</li></ol>
 * @author Garfield
 * @version 4.5
 */
@SuppressWarnings("unchecked")
public abstract class AsyncLoader<Key, Params, Value> extends Loader {
    /**
     * Indicates the loader will be ignore the memory cache when
     * it will be load value.
     */
    public static final int FLAG_IGNORE_MEMORY_CACHE = 0x00800000;

    /**
     * The {@link Cache} to store the loaded values.
     */
    /* package */ final Cache<Key, Value> mCache;

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache May be <tt>null</tt>. The {@link Cache} to store
     * the loaded values.
     */
    public AsyncLoader(Executor executor, Cache<Key, Value> cache) {
        super(executor);
        mCache = cache;
    }

    /**
     * Equivalent to calling <tt>load(key, target, 0, binder, (Params[])null)</tt>.
     * @param key The key to find value.
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
     * @param key The key to find value.
     * @param target The <tt>Object</tt> to bind.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @param binder The {@link Binder} used to bind value to <em>target</em>.
     * @param params The parameters of the load task. If the task no parameters, you can pass
     * <em>(Params[])null</em> instead of allocating an empty array.
     * @see #load(Key, Object, Binder)
     */
    public final void load(Key key, Object target, int flags, Binder<Key, Params, Value> binder, Params... params) {
        DebugUtils.__checkUIThread("load");
        DebugUtils.__checkError(target == null, "target == null");
        DebugUtils.__checkError((flags & 0xFF000000) != 0, "The flags must be range of [0 - 0xFFFFFF]");
        if (mState != SHUTDOWN) {
            if (key == null) {
                bindValue(key, params, target, null, flags, binder);
                return;
            }

            // Loads the value from the memory cache.
            if (isCacheValid(flags)) {
                final Value value = mCache.get(key);
                if (value != null) {
                    bindValue(key, params, target, value, flags | Binder.STATE_LOAD_FROM_CACHE, binder);
                    return;
                }
            }

            // Loads the value from the background thread.
            if (!isTaskRunning(key, target)) {
                binder.bindValue(key, params, target, null, flags);
                final LoadTask task = obtain(key, params, target, flags, binder);
                mRunningTasks.put(target, task);
                mExecutor.execute(task);
            }
        }
    }

    /**
     * Equivalent to calling <tt>preload(key, 0, (Params[])null)</tt>.
     * @param key The key to find value.
     * @see #preload(Key, int, Params[])
     */
    public final void preload(Key key) {
        preload(key, 0, (Params[])null);
    }

    /**
     * Preloads the value into the internal cache with the specified <em>key</em>.
     * If the value is already cached, it is return immediately. If the parameter
     * <em>flags</em> contains the {@link #FLAG_IGNORE_MEMORY_CACHE} then invoking
     * this method has no effect. Otherwise loads the value on a background thread.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param key The key to find value.
     * @param flags Loading flags. May be <tt>0</tt> or any combination of
     * <tt>FLAG_XXX</tt> constants.
     * @param params The parameters of the load task. If the task no parameters,
     * you can pass <em>(Params[])null</em> instead of allocating an empty array.
     * @see #preload(Key)
     */
    public final void preload(Key key, int flags, Params... params) {
        DebugUtils.__checkUIThread("preload");
        DebugUtils.__checkError((flags & 0xFF000000) != 0, "The flags must be range of [0 - 0xFFFFFF]");
        if (mState != SHUTDOWN && key != null && isCacheValid(flags) && mCache.get(key) == null && !isTaskRunning(key, key)) {
            final LoadTask task = obtain(key, params, key, flags, AsyncLoader.<Key, Params, Value>emptyBinder());
            mRunningTasks.put(key, task);
            mExecutor.execute(task);
        }
    }

    /**
     * Equivalent to calling <tt>loadSync(key, 0, (Params[])null)</tt>.
     * @param key The key to find value.
     * @see #loadSync(Key, int, Params[])
     */
    public final Value loadSync(Key key) {
        return loadSync(key, 0, (Params[])null);
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
     * @see #loadSync(Key)
     */
    public final Value loadSync(Key key, int flags, Params... params) {
        if (key == null || mState == SHUTDOWN) {
            return null;
        }

        Value value;
        if (!isCacheValid(flags)) {
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
     * Returns the {@link Cache} associated with this loader.
     * @return The <tt>Cache</tt> or <tt>null</tt>.
     */
    public final Cache<Key, Value> getCache() {
        return mCache;
    }

    @Override
    public final Task<?, ?> newInstance() {
        return new LoadTask();
    }

    /**
     * Returns a type-safe empty {@link Binder} associated with this class.
     * The empty <tt>Binder</tt> {@link Binder#bindValue binderValue}
     * implementation do nothing.
     * @return An empty <tt>Binder</tt>.
     */
    public static <Key, Params, Value> Binder<Key, Params, Value> emptyBinder() {
        return (Binder<Key, Params, Value>)EmptyBinder.sInstance;
    }

    /**
     * Called on a background thread to perform the actual load task.
     * @param task The current {@link Task} whose executing this method,
     * or <tt>null</tt> if the value load synchronously.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param flags Loading flags, passed earlier by {@link #load}.
     * @return The result, or <tt>null</tt> if the <em>task</em> was failed or cancelled.
     * @see #loadSync(Key, int, Params[])
     * @see #load(Key, Object, int, Binder, Params[])
     */
    protected abstract Value loadInBackground(Task<?, ?> task, Key key, Params[] params, int flags);

    /**
     * Tests if the {@link mCache} is valid.
     */
    /* package */ final boolean isCacheValid(int flags) {
        return (mCache != null && (flags & FLAG_IGNORE_MEMORY_CACHE) == 0);
    }

    /**
     * Tests the specified task is running.
     */
    private boolean isTaskRunning(Key key, Object target) {
        final LoadTask task = (LoadTask)mRunningTasks.get(target);
        if (task != null && !task.isCancelled()) {
            if (task.key.equals(key)) {
                return true;
            } else {
                task.cancel(false);
            }
        }

        return false;
    }

    /**
     * Retrieves a new {@link Task} from the task pool. Allows us to avoid allocating new tasks in many cases.
     */
    private LoadTask obtain(Key key, Params[] params, Object target, int flags, Binder<Key, Params, Value> binder) {
        final LoadTask task = (LoadTask)mTaskPool.obtain();
        task.key = key;
        task.flags  = flags;
        task.params = params;
        task.binder = binder;
        task.target = target;
        return task;
    }

    /**
     * Binds the specified <em>value</em> to the specified <em>target</em>.
     */
    private void bindValue(Key key, Params[] params, Object target, Value value, int state, Binder<Key, Params, Value> binder) {
        // Cancel the task associated with the target.
        cancelTask(target, false);
        binder.bindValue(key, params, target, value, state);
    }

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link Task}.
     */
    /* package */ final class LoadTask extends Task<Params, Value> {
        /* package */ Key key;
        /* package */ int flags;
        /* package */ Object target;
        /* package */ Binder<Key, Params, Value> binder;

        @Override
        /* package */ Value doInBackground(Params[] params) {
            waitResumeIfPaused();
            Value value = null;
            if (mState != SHUTDOWN && !isCancelled()) {
                value = loadInBackground(this, key, params, flags);
                if (value != null && isCacheValid(flags)) {
                    mCache.put(key, value);
                }
            }

            return value;
        }

        @Override
        /* package */ void onPostExecute(Params[] params, Value value) {
            if (mState != SHUTDOWN && !isCancelled() && mRunningTasks.remove(target) == this) {
                binder.bindValue(key, params, target, value, flags | Binder.STATE_LOAD_FROM_BACKGROUND);
            }

            // Recycles this task to avoid potential memory
            // leaks, Even the loader has been shut down.
            this.clearForRecycle();
            this.key = null;
            this.binder = null;
            this.target = null;
            mTaskPool.recycle(this);
        }
    }

    /**
     * Class <tt>EmptyBinder</tt> is an implementation of a {@link Binder}.
     */
    private static final class EmptyBinder implements Binder<Object, Object, Object> {
        public static final EmptyBinder sInstance = new EmptyBinder();

        @Override
        public void bindValue(Object key, Object[] params, Object target, Object value, int state) {
        }
    }

    /**
     * Callback interface used to bind the value to the target.
     */
    public static interface Binder<Key, Params, Value> {
        /**
         * Indicates the value load from memory cache.
         */
        int STATE_LOAD_FROM_CACHE = 0x40000000;

        /**
         * Indicates the value load from a background thread.
         */
        int STATE_LOAD_FROM_BACKGROUND = 0x80000000;

        /**
         * Binds the specified <em>value</em> to the specified <em>target</em>
         * on the UI thread.</p>
         * @param key The key, passed earlier by {@link AsyncLoader#load}.
         * @param params The parameters, passed earlier by {@link AsyncLoader#load}.
         * @param target The <tt>Object</tt> to bind the <em>value</em>, passed
         * earlier by {@link AsyncLoader#load}.
         * @param value The load result, or <tt>null</tt> if the load failed.
         * @param state May be <tt>0</tt> or any combination of <tt>STATE_XXX</tt>
         * and <tt>FLAG_XXX</tt> constants.
         */
        void bindValue(Key key, Params[] params, Object target, Value value, int state);
    }
}
