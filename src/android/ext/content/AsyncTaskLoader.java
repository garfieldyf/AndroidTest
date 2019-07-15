package android.ext.content;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import android.app.Activity;
import android.ext.util.DebugUtils;

/**
 * Class <tt>AsyncTaskLoader</tt> allows to load the resource
 * on a background thread and publish results on the UI thread.
 * <h3>AsyncTaskLoader's generic types</h3>
 * <p>The three types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Params</tt>, The load task's parameters type.</li>
 * <li><tt>Result</tt>, The load result type.</li></ol>
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public abstract class AsyncTaskLoader<Key, Params, Result> extends Loader<Key> {
    private static final int MAX_POOL_SIZE = 8;
    private WeakReference<Object> mOwner;

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #AsyncTaskLoader(Executor, Object)
     */
    public AsyncTaskLoader(Executor executor) {
        super(executor, MAX_POOL_SIZE);
    }

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncTaskLoader(Executor)
     */
    public AsyncTaskLoader(Executor executor, Object owner) {
        super(executor, MAX_POOL_SIZE);
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Executes the load task on a background thread. If the <em>key</em> is
     * mapped to the task is running then invoking this method has no effect.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param key The identifier of the load task.
     * @param params The parameters of the load task. If the task no parameters,
     * you can pass <em>(Params[])null</em> instead of allocating an empty array.
     */
    public final void load(Key key, Params... params) {
        DebugUtils.__checkUIThread("load");
        DebugUtils.__checkError(key == null, "key == null");
        if (mState != SHUTDOWN) {
            final Task task = mRunningTasks.get(key);
            if (task == null || task.isCancelled()) {
                onStartLoading(key, params);
                final LoadTask newTask = obtain(key, params);
                mRunningTasks.put(key, newTask);
                mExecutor.execute(newTask);
            }
        }
    }

    /**
     * Sets the object that owns this loader.
     * @param owner The owner object.
     * @see #getOwner()
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this loader.
     * @return The owner object or <tt>null</tt>
     * if the owner released by the GC.
     * @see #setOwner(Object)
     */
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    /**
     * Alias of {@link #getOwner()}.
     * @return The <tt>Activity</tt> that owns this loader or <tt>null</tt> if
     * the owner activity has been finished or destroyed or release by the GC.
     * @see #setOwner(Object)
     */
    public final <T extends Activity> T getOwnerActivity() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        final T activity = (T)mOwner.get();
        return (activity != null && !activity.isFinishing() && !activity.isDestroyed() ? activity : null);
    }

    @Override
    public final Task newInstance() {
        return new LoadTask();
    }

    /**
     * Called on the UI thread before {@link #loadInBackground}. <p>The default
     * implementation do nothing. If you write your own implementation, do not
     * call <tt>super.onStartLoading()</tt>.</p>
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @see #onLoadComplete(Key, Params[], Result)
     * @see #loadInBackground(Task, Key, Params[])
     */
    protected void onStartLoading(Key key, Params[] params) {
    }

    /**
     * Called on the UI thread when a load was cancelled.<p>The default
     * implementation do nothing. If you write your own implementation,
     * do not call <tt>super.onLoadCancelled()</tt>.</p>
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param result The result, or <tt>null</tt>.
     */
    protected void onLoadCancelled(Key key, Params[] params, Result result) {
    }

    /**
     * Called on the UI thread after {@link Task#setProgress(Object[])}.
     * <p>The default implementation do nothing. If you write your own
     * implementation, do not call <tt>super.onProgressUpdate()</tt>.</p>
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param values The progress values to update.
     */
    protected void onProgressUpdate(Key key, Params[] params, Object[] values) {
    }

    /**
     * Called on the UI thread when a load is complete. <p>This method won't be
     * invoked if the task was cancelled.</p>
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param result The result, returned earlier by {@link #loadInBackground}.
     * @see #loadInBackground(Task, Key, Params[])
     */
    protected abstract void onLoadComplete(Key key, Params[] params, Result result);

    /**
     * Called on a background thread to perform the actual load task.
     * @param task The current {@link Task} whose executing this method,
     * or <tt>null</tt> if the load synchronously.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @return A result, defined by the subclass of this loader.
     * @see #onLoadComplete(Key, Params[], Result)
     */
    protected abstract Result loadInBackground(Task<?, ?> task, Key key, Params[] params);

    /**
     * Checks the owner object is valid.
     */
    /* package */ final boolean isOwnerValid() {
        // This loader has no owner, validate OK.
        if (mOwner == null) {
            return true;
        }

        final Object owner = mOwner.get();
        if (owner == null) {
            // The owner released by the GC.
            DebugUtils.__checkDebug(true, "AsyncTaskLoader", "The " + owner + " released by the GC.");
            return false;
        } else if (owner instanceof Activity) {
            final Activity activity = (Activity)owner;
            DebugUtils.__checkDebug(activity.isFinishing() || activity.isDestroyed(), "AsyncTaskLoader", "The " + activity + " has been destroyed.");
            return (!activity.isFinishing() && !activity.isDestroyed());
        } else {
            return true;
        }
    }

    /**
     * Retrieves a new {@link Task} from the task pool.
     * Allows us to avoid allocating new tasks in many cases.
     */
    private LoadTask obtain(Key key, Params[] params) {
        final LoadTask task = (LoadTask)mTaskPool.obtain();
        task.mKey = key;
        task.mParams = params;
        return task;
    }

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link Task}.
     */
    /* package */ final class LoadTask extends Task<Params, Result> {
        /* package */ Key mKey;

        @Override
        public void onProgress(Object[] values) {
            if (mState != SHUTDOWN) {
                onProgressUpdate(mKey, mParams, values);
            }
        }

        @Override
        public Result doInBackground(Params[] params) {
            waitResumeIfPaused();
            return (mState != SHUTDOWN && !isCancelled() ? loadInBackground(this, mKey, params) : null);
        }

        @Override
        public void onPostExecute(Result result) {
            if (mState != SHUTDOWN) {
                // Removes the finished task from running
                // tasks, excluding the cancelled task.
                if (mRunningTasks.get(mKey) == this) {
                    mRunningTasks.remove(mKey);
                }

                if (isCancelled()) {
                    onLoadCancelled(mKey, mParams, result);
                } else {
                    onLoadComplete(mKey, mParams, result);
                }
            }

            // Recycles this task to avoid potential memory
            // leaks, Even the loader has been shut down.
            clearForRecycle();
            mKey = null;
            mTaskPool.recycle(this);
        }
    }
}
