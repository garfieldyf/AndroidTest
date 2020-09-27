package android.ext.widget;

import android.app.Activity;
import android.content.Context;
import android.ext.content.AsyncTask;
import android.support.v7.widget.RecyclerView.ViewHolder;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Class PageAdapter2
 * @author Garfield
 */
public abstract class PageAdapter2<E, VH extends ViewHolder> extends PageAdapter<E, VH> {
    private final Executor mExecutor;

    /**
     * Constructor
     * @param executor The <tt>Executor</tt> to executing load task.
     * @param config The {@link Config}, which defines how the adapter will load data.
     */
    public PageAdapter2(Executor executor, Config config) {
        super(config);
        mExecutor = executor;
    }

    @Override
    protected List<E> loadPage(int pageIndex, int startPosition, int loadSize) {
        new LoadTask(this).execute(mExecutor, pageIndex, startPosition, loadSize);
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
    protected abstract List<E> loadPage(AsyncTask<?, ?, ?> task, int pageIndex, int startPosition, int loadSize);

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link AbsAsyncTask}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final class LoadTask extends AsyncTask<Integer, Object, List> {
        public LoadTask(Object owner) {
            super(owner);
        }

        @Override
        protected List doInBackground(Integer[] params) {
            final PageAdapter2 adapter = getOwner();
            return (adapter != null && validateOwner(adapter) ? adapter.loadPage(this, params[0], params[1], params[2]) : null);
        }

        @Override
        protected void onPostExecute(Integer[] params, List result) {
            final PageAdapter adapter = getOwner();
            if (adapter != null && validateOwner(adapter)) {
                adapter.setPage(params[0], result, null);
            }
        }

        private static boolean validateOwner(PageAdapter adapter) {
            final Context context = adapter.mRecyclerView.getContext();
            if (context instanceof Activity) {
                final Activity activity = (Activity)context;
                return (!activity.isFinishing() && !activity.isDestroyed());
            }

            return true;
        }
    }
}
