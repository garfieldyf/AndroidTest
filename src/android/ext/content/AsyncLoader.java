package android.ext.content;

import java.util.concurrent.Executor;
import android.ext.cache.Cache;
import android.ext.util.DebugUtils;

/**
 * Class <tt>AsyncLoader</tt> allows to load the resource on
 * a background thread and bind it to target on the UI thread.
 * <h3>AsyncLoader's generic types</h3>
 * <p>The three types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Params</tt>, The load task's parameters type.</li>
 * <li><tt>Value</tt>, The value type of the load result.</li></ol>
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AsyncLoader<Key, Params, Value> extends Loader<Object> {
    /**
     * If set the loader will be ignored the memory cache when it will be load value.
     */
    public static final int FLAG_IGNORE_MEMORY_CACHE = 0x00800000;

    /**
     * FLAG_MASK = ~(FLAG_IGNORE_MEMORY_CACHE | FLAG_DUMP_OPTIONS);
     */
    private static final int FLAG_MASK = 0xFF3FFFFF;

    /**
     * The {@link Cache} to store the loaded values.
     */
    /* package */ final Cache<Key, Value> mCache;

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param cache The {@link Cache} to store the loaded values.
     */
    public AsyncLoader(Executor executor, Cache<Key, Value> cache) {
        super(executor, 48);
        mCache = cache;
        DebugUtils.__checkError(cache == null, "cache == null");
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
        DebugUtils.__checkError(binder == null, "binder == null");
        DebugUtils.__checkError((flags & FLAG_MASK) > 0xFFFF, "The custom flags (0x" + Integer.toHexString(flags & FLAG_MASK) + ") must be range of [0 - 0xFFFF]");
        if (mState != SHUTDOWN) {
            if (key == null) {
                bindValue(binder, key, params, target, null, flags);
                return;
            }

            // Loads the value from the memory cache.
            if ((flags & FLAG_IGNORE_MEMORY_CACHE) == 0) {
                final Value value = mCache.get(key);
                if (value != null) {
                    bindValue(binder, key, params, target, value, flags | Binder.STATE_LOAD_FROM_CACHE);
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
        DebugUtils.__checkError((flags & FLAG_MASK) > 0xFFFF, "The custom flags (0x" + Integer.toHexString(flags & FLAG_MASK) + ") must be range of [0 - 0xFFFF]");
        if (key == null || mState == SHUTDOWN) {
            return null;
        }

        Value value;
        if ((flags & FLAG_IGNORE_MEMORY_CACHE) != 0) {
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
     * @return The <tt>Cache</tt>.
     */
    public final Cache<Key, Value> getCache() {
        return mCache;
    }

    @Override
    public final Task newInstance() {
        return new LoadTask();
    }

    /**
     * Returns a type-safe empty {@link Binder} associated with this class.
     * The empty <tt>Binder</tt> {@link Binder#bindValue bindValue}
     * implementation do nothing.
     * @return An empty <tt>Binder</tt>.
     */
    public static <Key, Params, Value> Binder<Key, Params, Value> emptyBinder() {
        return EmptyBinder.sInstance;
    }

    /**
     * Returns the target associated with the <em>task</em>.
     * @param task The {@link Task}.
     * @return The <tt>Object</tt> target.
     */
    protected final Object getTarget(Task task) {
        return (task != null ? ((LoadTask)task).mTarget : null);
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
     * or <tt>null</tt> if the load synchronously.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param flags Loading flags, passed earlier by {@link #load}.
     * @return A result, defined by the subclass of this loader.
     * @see #loadSync(Key, int, Params[])
     * @see #load(Key, Object, int, Binder, Params[])
     */
    protected abstract Value loadInBackground(Task task, Key key, Params[] params, int flags);

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
     * Retrieves a new {@link Task} from the task pool. Allows us to avoid allocating new tasks in many cases.
     */
    private LoadTask obtain(Key key, Params[] params, Object target, int flags, Binder binder) {
        final LoadTask task = (LoadTask)mTaskPool.obtain();
        task.mKey = key;
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
    /* package */ final class LoadTask extends Task {
        /* package */ Key mKey;
        /* package */ int mFlags;
        /* package */ Object mTarget;
        /* package */ Binder mBinder;

        @Override
        public Object doInBackground(Object params) {
            waitResumeIfPaused();
            Value value = null;
            if (mState != SHUTDOWN && !isCancelled()) {
                value = loadInBackground(this, mKey, (Params[])params, mFlags);
                if (value != null && (mFlags & FLAG_IGNORE_MEMORY_CACHE) == 0) {
                    mCache.put(mKey, value);
                }
            }

            return value;
        }

        @Override
        public void onPostExecute(Object value) {
            if (mState != SHUTDOWN && !isCancelled() && mRunningTasks.remove(mTarget) == this) {
                mBinder.bindValue(mKey, (Params[])mParams, mTarget, value, mFlags | Binder.STATE_LOAD_FROM_BACKGROUND);
            }

            // Recycles this task to avoid potential memory
            // leaks, Even the loader has been shut down.
            onRecycle((Params[])mParams);
            clearForRecycle();
            mKey = null;
            mTarget = null;
            mBinder = null;
            mTaskPool.recycle(this);
        }
    }

    /**
     * Class <tt>EmptyBinder</tt> is an implementation of a {@link Binder}.
     */
    private static final class EmptyBinder implements Binder {
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
    }
}
