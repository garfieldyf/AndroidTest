package android.ext.page;

import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.ArrayMap;
import java.util.Map;

/**
 * Like as {@link PageAdapter}, but this adapter will be call {@link #onPageRemoved}
 * when a {@link Page} removed from this adapter.
 * @author Garfield
 */
public abstract class PageAdapter2<E, VH extends ViewHolder> extends PageAdapter<E, VH> {
    /**
     * Constructor
     * <p>Creates a <tt>PageAdapter2</tt> with an <b>unlimitted-size</b> capacity.</p>
     * @param initialSize The item count of the first page (pageIndex = 0).
     * @param pageSize The item count of the each page (pageIndex > 0).
     * @param prefetchDistance Defines how far to the first or last item in the page to
     * this adapter should prefetch the data. Pass <tt>0</tt> indicates this adapter will
     * not prefetch data.
     * @see #PageAdapter2(int, int, int, int)
     */
    public PageAdapter2(int initialSize, int pageSize, int prefetchDistance) {
        super(null, initialSize, pageSize, prefetchDistance);
        mPageCache = new ArrayPageCache();
    }

    /**
     * Constructor
     * <p>Creates a <tt>PageAdapter2</tt> with a <em>maxPageCount</em> capacity.</p>
     * @param maxPageCount The maximum number of pages to allow in the page cache.
     * @param initialSize The item count of the first page (pageIndex = 0).
     * @param pageSize The item count of the each page (pageIndex > 0).
     * @param prefetchDistance Defines how far to the first or last item in the page to
     * this adapter should prefetch the data. Pass <tt>0</tt> indicates this adapter will
     * not prefetch data.
     * @see #PageAdapter2(int, int, int)
     */
    public PageAdapter2(int maxPageCount, int initialSize, int pageSize, int prefetchDistance) {
        super(null, initialSize, pageSize, prefetchDistance);
        mPageCache = (maxPageCount > 0 ? new LruPageCache(maxPageCount) : new ArrayPageCache());
    }

    /**
     * Called when a {@link Page} removed by this adapter.
     * @param pageIndex The index of the page.
     * @param oldPage The removed {@link Page}.
     */
    protected void onPageRemoved(int pageIndex, Page<E> oldPage) {
    }

    /**
     * Class <tt>LruPageCache</tt> is an implementation of a {@link SimpleLruCache}.
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
        protected void entryRemoved(boolean evicted, Integer pageIndex, Page<E> oldPage, Page<E> newPage) {
            if (evicted || oldPage != newPage) {
                onPageRemoved(pageIndex, oldPage);
            }
        }
    }

    /**
     * Class <tt>ArrayPageCache</tt> is an implementation of a {@link Cache}.
     */
    private final class ArrayPageCache implements Cache<Integer, Page<E>> {
        private final ArrayMap<Integer, Page<E>> mPages;

        public ArrayPageCache() {
            mPages = new ArrayMap<Integer, Page<E>>(8);
        }

        @Override
        public void clear() {
            for (int i = 0, size = mPages.size(); i < size; ++i) {
                onPageRemoved(mPages.keyAt(i), mPages.valueAt(i));
            }

            mPages.clear();
        }

        @Override
        public Page<E> remove(Integer pageIndex) {
            final Page<E> oldPage = mPages.remove(pageIndex);
            if (oldPage != null) {
                onPageRemoved(pageIndex, oldPage);
            }

            return oldPage;
        }

        @Override
        public Page<E> get(Integer pageIndex) {
            return mPages.get(pageIndex);
        }

        @Override
        public Page<E> put(Integer pageIndex, Page<E> page) {
            final Page<E> oldPage = mPages.put(pageIndex, page);
            if (oldPage != null) {
                onPageRemoved(pageIndex, oldPage);
            }

            return oldPage;
        }

        @Override
        public Map<Integer, Page<E>> snapshot() {
            return new ArrayMap<Integer, Page<E>>(mPages);
        }
    }
}
