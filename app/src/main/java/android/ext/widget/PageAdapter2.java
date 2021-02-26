package android.ext.widget;

import android.ext.content.AsyncTaskLoader;
import android.ext.content.ResourceLoader.OnLoadCompleteListener;
import android.ext.content.Task;
import android.support.annotation.UiThread;
import android.support.annotation.WorkerThread;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Printer;
import java.util.List;

/**
 * Class PageAdapter2
 * @author Garfield
 */
public abstract class PageAdapter2<E, VH extends ViewHolder> extends PageAdapter<E, VH> implements OnLoadCompleteListener<Integer, List<?>> {
    private final Loader mLoader;

    /**
     * Constructor
     * @param config The {@link Config}, which defines how the adapter will load data.
     * @see #PageAdapter2(Config, Object)
     */
    public PageAdapter2(Config config) {
        super(config);
        mLoader = new Loader();
    }

    /**
     * Constructor
     * @param config The {@link Config}, which defines how the adapter will load data.
     * @param owner May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
     * @see #PageAdapter2(Config)
     */
    public PageAdapter2(Config config, Object owner) {
        this(config);
        mLoader.setOwner(owner);
    }

    /**
     * Sets the object that owns this adapter.
     * @param owner May be an <tt>Activity, LifecycleOwner, Lifecycle</tt> or <tt>Fragment</tt> etc.
     */
    public final void setOwner(Object owner) {
        mLoader.setOwner(owner);
    }

    @UiThread
    @Override
    public void dump(Printer printer) {
        super.dump(printer);
        mLoader.dump(printer);
    }

    @Override
    public void onLoadComplete(Integer[] params, List<?> result) {
        setPage(params[0], result, null);
    }

    @Override
    protected List<?> loadPage(int pageIndex, int startPosition, int loadSize) {
        mLoader.load(this, pageIndex, startPosition, loadSize);
        return null;
    }

    /**
     * Called on a background thread to load a page with given the <em>pageIndex</em>.
     * @param task The current {@link Task} whose executing this method.
     * @param pageIndex The index of the page whose data should be load.
     * @param startPosition The start position of data to load.
     * @param loadSize The number of items should be load.
     * @return The page, or <tt>null</tt>.
     */
    @WorkerThread
    protected abstract List<?> loadPage(Task task, int pageIndex, int startPosition, int loadSize);

    /**
     * Class <tt>Loader</tt> is an implementation of a {@link AsyncTaskLoader}.
     */
    private static final class Loader extends AsyncTaskLoader<Integer, List<?>> {
        /**
         * Constructor
         */
        public Loader() {
            super(4);
        }

        @Override
        protected List<?> loadInBackground(Task task, Integer[] params) {
            return ((PageAdapter2<?, ?>)getListener(task)).loadPage(task, params[0], params[1], params[2]);
        }
    }
}