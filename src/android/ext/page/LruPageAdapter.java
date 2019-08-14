package android.ext.page;

import android.ext.cache.SimpleLruCache;
import android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Like as {@link PageAdapter}, but this adapter will be call {@link #onPageRemoved}
 * when a {@link Page} removed by this adapter.
 * @author Garfield
 */
public abstract class LruPageAdapter<E, VH extends ViewHolder> extends PageAdapter<E, VH> {
    /**
     * Constructor
     * @param maxPageCount The maximum number of pages to allow in the page cache.
     * @param initialSize The item count of the first page (page index == 0).
     * @param pageSize The item count of the each page (page index > 0).
     * @param prefetchDistance Defines how far to the first or last item in the
     * page to this adapter should prefetch the data. Pass <tt>0</tt> indicates
     * this adapter will not prefetch data.
     */
    public LruPageAdapter(int maxPageCount, int initialSize, int pageSize, int prefetchDistance) {
        super(null, initialSize, pageSize, prefetchDistance);
        setPageCache(new LruPageCache(maxPageCount));
    }

    /**
     * Called when a {@link Page} removed by this adapter.
     * @param page The index of the page.
     * @param oldPage The removed <tt>Page</tt>.
     */
    protected void onPageRemoved(int page, Page<E> oldPage) {
    }

    /**
     * Class <tt>LruPageCache</tt> is an implementation of a {@link Cache}.
     */
    private final class LruPageCache extends SimpleLruCache<Integer, Page<E>> {
        public LruPageCache(int maxSize) {
            super(maxSize);
        }

        @Override
        public void clear() {
            trimToSize(-1);
        }

        @Override
        protected void entryRemoved(boolean evicted, Integer page, Page<E> oldPage, Page<E> newPage) {
            if (evicted || oldPage != newPage) {
                onPageRemoved(page, oldPage);
            }
        }
    }
}
