package android.ext.widget;

import android.ext.content.AsyncTask;
import android.ext.util.DebugUtils;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.RecyclerView.ViewHolder;
import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Class PageAdapter2
 * @author Garfield
 */
public abstract class PageAdapter2<E, VH extends ViewHolder> extends PageAdapter<E, VH> {
    private Object mOwner;

    /**
     * Constructor
     * @param config The {@link Config}, which defines how the adapter will load data.
     * @see #PageAdapter2(Config, Object)
     */
    public PageAdapter2(Config config) {
        super(config);
    }

    /**
     * Constructor
     * @param config The {@link Config}, which defines how the adapter will load data.
     * @param owner May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
     * @see #PageAdapter2(Config)
     */
    public PageAdapter2(Config config, Object owner) {
        super(config);
        mOwner = owner;
    }

    /**
     * Sets the object that owns this adapter.
     * @param owner May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
     */
    public final void setOwner(Object owner) {
        DebugUtils.__checkError(mOwner != null, "The owner is already exists (a PageAdapter can be setOwner only once)");
        mOwner = owner;
    }

    @Override
    protected List<E> loadPage(int pageIndex, int startPosition, int loadSize) {
        new LoadTask(this, mOwner).execute(pageIndex, startPosition, loadSize);
        return null;
    }

    /**
     * Called on a background thread to load a page with given the <em>pageIndex</em>.
     * @param task The current {@link AsyncTask} whose executing this method.
     * @param pageIndex The index of the page whose data should be load.
     * @param startPosition The start position of data to load.
     * @param loadSize The number of items should be load.
     * @return The page, or <tt>null</tt>.
     */
    @WorkerThread
    protected abstract List<E> loadPage(AsyncTask<?, ?, ?> task, int pageIndex, int startPosition, int loadSize);

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link AbsAsyncTask}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final class LoadTask extends AsyncTask<Integer, Object, List> {
        private final WeakReference<PageAdapter2> mAdapter;

        /**
         * Constructor
         * @param adapter The {@link PageAdapter2}.
         * @param owner The owner object.
         */
        public LoadTask(PageAdapter2 adapter, Object owner) {
            super(owner != null ? owner : adapter.mRecyclerView.getContext());
            mAdapter = new WeakReference<PageAdapter2>(adapter);
        }

        @Override
        protected List doInBackground(Integer[] params) {
            final PageAdapter2 adapter = mAdapter.get();
            return (adapter != null ? adapter.loadPage(this, params[0], params[1], params[2]) : null);
        }

        @Override
        protected void onPostExecute(Integer[] params, List result) {
            final PageAdapter2 adapter = mAdapter.get();
            if (adapter != null) {
                adapter.setPage(params[0], result, null);
            }
        }
    }
}
