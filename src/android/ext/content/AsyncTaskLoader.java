package android.ext.content;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import android.ext.util.DebugUtils;

/**
 * Class <tt>AsyncTaskLoader</tt> allows to load the resource
 * on a background thread and publish results on the UI thread.
 * <h5>AsyncTaskLoader's generic types</h5>
 * <p>The three types used by a loader are the following:</p>
 * <ol><li><tt>Key</tt>, The loader's key type.</li>
 * <li><tt>Params</tt>, The load task's parameters type.</li>
 * <li><tt>Result</tt>, The load result type.</li></ol>
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public abstract class AsyncTaskLoader<Key, Params, Result> extends Loader {
    private WeakReference<Object> mOwner;

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @see #AsyncTaskLoader(Executor, Object)
     */
    public AsyncTaskLoader(Executor executor) {
        super(executor);
    }

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncTaskLoader(Executor)
     */
    public AsyncTaskLoader(Executor executor, Object owner) {
        super(executor);
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Executes the load task on a background thread. If the <em>key</em> is mapped
     * to the task is running and {@link #rejectedRequest} returns <tt>true</tt>
     * then invoking this method has no effect. <p><b>Note: This method must be
     * invoked on the UI thread.</b></p>
     * @param key The identifier of the load task.
     * @param params The parameters of the load task. If the task no parameters,
     * you can pass <em>(Params[])null</em> instead of allocating an empty array.
     * @see #rejectedRequest(Key, Params[], Params[])
     * @see #loadSync(Key, Params[])
     */
    public final void load(Key key, Params... params) {
        DebugUtils.__checkUIThread("load");
        DebugUtils.__checkError(key == null, "key == null");
        if (mState != SHUTDOWN) {
            final LoadTask task = (LoadTask)mRunningTasks.get(key);
            if (task == null || !rejectedRequest(key, params, task.mParams)) {
                onStartLoading(key, params);
                final LoadTask newTask = obtain(key, params);
                mRunningTasks.put(key, newTask);
                mExecutor.execute(newTask);
            }
        }
    }

    /**
     * Loads the value synchronously. Call this method, Pass the {@link #loadInBackground}
     * the <em>task</em> parameter always <tt>null</tt>.<p><b>Note: This method will block
     * the calling thread until it was returned.</b></p>
     * @param key The key passed to the {@link #loadInBackground}.
     * @param params The parameters passed to the {@link #loadInBackground}. If no parameters,
     * you can pass <em>(Params[])null</em> instead of allocating an empty array.
     * @return The result, or <tt>null</tt> if load failed or this loader was shut down.
     * @see #load(Key, Params[])
     */
    public final Result loadSync(Key key, Params... params) {
        return (mState != SHUTDOWN ? loadInBackground(null, key, params) : null);
    }

    /**
     * Returns the object that owns this loader.
     * @return The owner object or <tt>null</tt> if
     * no owner set or the owner released by the GC.
     * @see #setOwner(Object)
     */
    public final <T> T getOwner() {
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
    }

    /**
     * Sets the object that owns this loader.
     * @param owner The owner object.
     * @see #getOwner()
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    @Override
    public final Task<?, ?> newInstance() {
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
     * Called on the UI thread when a load is complete.<p>This method
     * won't be invoked if the task was cancelled.</p> <p>The default
     * implementation do nothing. If you write your own implementation,
     * do not call <tt>super.onLoadComplete()</tt>.</p>
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param result The result, or <tt>null</tt> if the load failed.
     * @see #onStartLoading(Key, Params[])
     * @see #loadInBackground(Task, Key, Params[])
     */
    protected void onLoadComplete(Key key, Params[] params, Result result) {
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
     * Called on the UI thread when updates progress values. <p>The default
     * implementation do nothing. If you write your own implementation, do
     * not call <tt>super.onProgressUpdate()</tt>.</p>
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param values The progress values to update.
     */
    protected void onProgressUpdate(Key key, Params[] params, Object[] values) {
    }

    /**
     * Called by this loader when the loader cannot accept a load request (maybe the
     * <em>key</em> is mapped to the task is running). Subclasses should override this
     * method to cancel the running task and return <tt>false</tt> to add a new task.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @param oldParams The previous mapped load task's parameters.
     * @return <tt>true</tt> the load request will be rejected, <tt>false</tt> otherwise.
     * @see #load(Key, Params[])
     */
    protected boolean rejectedRequest(Key key, Params[] params, Params[] oldParams) {
        return true;
    }

    /**
     * Called on a background thread to perform the actual load task.
     * @param task The current {@link Task} whose executing this method,
     * or <tt>null</tt> if the value load synchronously.
     * @param key The key, passed earlier by {@link #load}.
     * @param params The parameters, passed earlier by {@link #load}.
     * @return The result, or <tt>null</tt> if the load <em>task</em> was
     * failed or cancelled.
     * @see #onStartLoading(Key, Params[])
     * @see #onLoadComplete(Key, Params[], Result)
     */
    protected abstract Result loadInBackground(Task<?, ?> task, Key key, Params[] params);

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
        /* package */ Result doInBackground(Params[] params) {
            waitResumeIfPaused();
            return (mState != SHUTDOWN && !isCancelled() ? loadInBackground(this, mKey, params) : null);
        }

        @Override
        /* package */ void onProgress(Params[] params, Object[] values) {
            onProgressUpdate(mKey, params, values);
        }

        @Override
        /* package */ void onPostExecute(Params[] params, Result result) {
            if (mState != SHUTDOWN) {
                if (isCancelled()) {
                    onLoadCancelled(mKey, params, result);
                } else {
                    onLoadComplete(mKey, params, result);
                    mRunningTasks.remove(mKey);
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
