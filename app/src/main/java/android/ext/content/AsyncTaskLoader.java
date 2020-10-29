package android.ext.content;

import android.ext.content.Loader.Task;
import android.ext.content.ResourceLoader.OnLoadCompleteListener;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.util.Printer;
import java.lang.ref.WeakReference;

/**
 * An abstract class that performs asynchronous loading of data.
 * @author Garfield
 */
public abstract class AsyncTaskLoader<Params, Result> implements Factory<Object> {
    /* package */ WeakReference<Object> mOwner;
    /* package */ final Pool<Object> mTaskPool;

    /**
     * Constructor
     * @param maxPoolSize The maximum number of tasks to allow in the internal pool.
     * @see #AsyncTaskLoader(int, Object)
     */
    public AsyncTaskLoader(int maxPoolSize) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mTaskPool = Pools.newPool(this, maxPoolSize);
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @param maxPoolSize The maximum number of tasks to allow in the internal pool.
     * @see #AsyncTaskLoader(int)
     */
    public AsyncTaskLoader(int maxPoolSize, Object owner) {
        this(maxPoolSize);
        setOwner(owner);
    }

    /**
     * Executes the load task on a background thread. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param listener An {@link OnLoadCompleteListener} to receive callbacks when a load is complete.
     * @param params The parameters of the load task. If the task no parameters, you can pass <em>(Params[])null</em>
     * instead of allocating an empty array.
     */
    @SuppressWarnings("unchecked")
    public final void load(OnLoadCompleteListener<Params, Result> listener, Params... params) {
        DebugUtils.__checkUIThread("load");
        DebugUtils.__checkError(listener == null, "Invalid parameter - listener == null");
        final LoadTask task = (LoadTask)mTaskPool.obtain();
        task.mParams   = params;
        task.mListener = listener;
        if (mOwner != null) {
            task.addLifecycleObserver(mOwner.get());
        }

        AsyncTask.THREAD_POOL_EXECUTOR.execute(task);
    }

    /**
     * Sets the object that owns this loader.
     * @param owner May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
     */
    public final void setOwner(Object owner) {
        DebugUtils.__checkError(owner == null, "Invalid parameter - owner == null");
        DebugUtils.__checkError(mOwner != null, "The owner is already exists (a loader can be setOwner only once)");
        mOwner = new WeakReference<Object>(owner);
    }

    public final void dump(Printer printer) {
        Pools.dumpPool(mTaskPool, printer);
    }

    @Override
    public final Object newInstance() {
        return new LoadTask();
    }

    /**
     * Called on a background thread to perform the actual load task.
     * @param task The current {@link Task} whose executing this method.
     * @param params The parameters, passed earlier by {@link #load}.
     * @return A result, defined by the subclass of this loader.
     * @see #load(OnLoadCompleteListener, Params[])
     */
    protected abstract Result loadInBackground(Task task, Params[] params);

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link Task}.
     */
    @SuppressWarnings("unchecked")
    /* package */ final class LoadTask extends Task {
        /* package */ OnLoadCompleteListener<Params, Result> mListener;

        @Override
        public void onPostExecute(Object result) {
            removeLifecycleObserver(mOwner);
            if (!isCancelled()) {
                mListener.onLoadComplete((Params[])mParams, (Result)result);
            }

            // Recycles this task.
            clearForRecycle();
            mListener = null;
            mTaskPool.recycle(this);
        }

        @Override
        public Object doInBackground(Object params) {
            return (isCancelled() ? null : loadInBackground(this, (Params[])params));
        }
    }
}
