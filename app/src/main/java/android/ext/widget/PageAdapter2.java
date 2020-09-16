package android.ext.widget;

import android.app.Activity;
import android.content.Context;
import android.ext.content.AbsAsyncTask;
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

    /**
     * Called on a background thread to load a page with given the <em>pageIndex</em>.
     * @param pageIndex The index of the page whose data should be load.
     * @param startPosition The start position of data to load.
     * @param loadSize The number of items should be load.
     * @return The page, or <tt>null</tt>.
     */
    protected abstract List<E> loadPage(int pageIndex, int startPosition, int loadSize);

    /**
     * Returns a page at the given the <em>pageIndex</em>.
     */
    @Override
    /* package */ List<E> loadPageImpl(int pageIndex, int startPosition, int loadSize) {
        new LoadTask(this, pageIndex).executeOnExecutor(mExecutor, startPosition, loadSize);
        return null;
    }

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link AbsAsyncTask}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final class LoadTask extends AbsAsyncTask<Integer, Object, List> {
        private final int mPageIndex;

        public LoadTask(Object owner, int pageIndex) {
            super(owner);
            mPageIndex = pageIndex;
        }

        @Override
        protected List doInBackground(Integer[] params) {
            final PageAdapter adapter = getOwner();
            return (adapter != null ? adapter.loadPage(mPageIndex, params[0], params[1]) : null);
        }

        @Override
        protected void onPostExecute(List result) {
            final PageAdapter adapter = getOwner();
            if (adapter != null && !isDestroyed(adapter)) {
                adapter.setPage(mPageIndex, result, null);
            }
        }

        private static boolean isDestroyed(PageAdapter adapter) {
            final Context context = adapter.mRecyclerView.getContext();
            if (context instanceof Activity) {
                final Activity activity = (Activity)context;
                return (activity.isFinishing() || activity.isDestroyed());
            }

            return false;
        }
    }
}
