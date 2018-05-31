package android.ext.widget;

import java.util.BitSet;
import java.util.List;
import org.json.JSONArray;
import android.ext.util.Caches.Cache;
import android.ext.util.Caches.SimpleLruCache;
import android.ext.util.DebugUtils;
import android.util.ArrayMap;
import android.util.SparseArray;

/**
 * Class Pages
 * @author Garfield
 * @version 3.0
 */
public final class Pages {
    /**
     * Returns the total number of items in the <em>page</em>,
     * handling <tt>null Page</tt>.
     * @param page The {@link Page}.
     * @return The total number of items.
     */
    public static int getCount(Page<?> page) {
        return (page != null ? page.getCount() : 0);
    }

    /**
     * Returns a new {@link Page} {@link Cache} instance.
     * @param maxPages The maximum number of pages to allow in the page cache. Pass
     * <tt>0</tt> that the returned page cache is the <b>unlimited-size</b> cache.
     * @return A new page cache instance.
     */
    public static <E> Cache<Integer, Page<E>> createPageCache(int maxPages) {
        return (maxPages > 0 ? new SimpleLruCache<Integer, Page<E>>(maxPages) : new SparsePageCache<E>());
    }

    /**
     * A <tt>Page</tt> is a collection used to adds the page data to the adapter.
     */
    public static interface Page<E> {
        /**
         * Returns the total number of items in this page.
         * @return The total number of items in this page.
         * @see #getItem(int)
         */
        int getCount();

        /**
         * Returns the item at the specified <em>position</em> in this page.
         * @param position The position of the item.
         * @return The item at the specified <em>position</em>, or <tt>null</tt>
         * if this page has no item at <em>position</em>.
         * @see #getCount()
         */
        E getItem(int position);
    }

    /**
     * Class <tt>ListPage</tt> is an implementation of a {@link Page}.
     */
    public static class ListPage<E> implements Page<E> {
        protected final List<E> mData;

        /**
         * Constructor
         * @param data A {@link List} of this page data.
         */
        public ListPage(List<E> data) {
            DebugUtils.__checkError(data == null || data.size() <= 0, "data == null || data.size() <= 0");
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public E getItem(int position) {
            return mData.get(position);
        }

        @Override
        public String toString() {
            return toString(this, mData);
        }

        /* package */ static String toString(Page<?> page, Object data) {
            final int count = page.getCount();
            final StringBuilder result = DebugUtils.toSimpleString(data, new StringBuilder(64)).append(" { count = ").append(count);
            if (count > 0) {
                result.append(", itemType = ").append(page.getItem(0).getClass().getName());
            }

            return result.append(" }").toString();
        }
    }

    /**
     * Class <tt>JSONPage</tt> is an implementation of a {@link Page}.
     */
    public static class JSONPage<E> implements Page<E> {
        protected final JSONArray mData;

        /**
         * Constructor
         * @param data A {@link JSONArray} of this page data.
         */
        public JSONPage(JSONArray data) {
            DebugUtils.__checkError(data == null || data.length() <= 0, "data == null || data.length() <= 0");
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.length();
        }

        @Override
        @SuppressWarnings("unchecked")
        public E getItem(int position) {
            return (E)mData.opt(position);
        }

        @Override
        public String toString() {
            return ListPage.toString(this, mData);
        }
    }

    /**
     * Class <tt>ArrayPageCache</tt> is an implementation of a {@link Cache}.
     */
    public static class ArrayPageCache<E> implements Cache<Integer, Page<E>> {
        protected final ArrayMap<Integer, Page<E>> mPages;

        /**
         * Constructor
         */
        public ArrayPageCache() {
            mPages = new ArrayMap<Integer, Page<E>>(8);
        }

        @Override
        public void clear() {
            mPages.clear();
        }

        @Override
        public Page<E> remove(Integer key) {
            return mPages.remove(key);
        }

        @Override
        public Page<E> get(Integer key) {
            return mPages.get(key);
        }

        @Override
        public Page<E> put(Integer key, Page<E> page) {
            return mPages.put(key, page);
        }
    }

    /**
     * Class <tt>SparsePageCache</tt> is an implementation of a {@link Cache}.
     */
    public static class SparsePageCache<E> implements Cache<Integer, Page<E>> {
        protected final SparseArray<Page<E>> mPages;

        /**
         * Constructor
         */
        public SparsePageCache() {
            mPages = new SparseArray<Page<E>>(8);
        }

        @Override
        public void clear() {
            mPages.clear();
        }

        @Override
        public Page<E> remove(Integer key) {
            mPages.delete(key);
            return null;
        }

        @Override
        public Page<E> get(Integer key) {
            return mPages.get(key, null);
        }

        @Override
        public Page<E> put(Integer key, Page<E> page) {
            mPages.append(key, page);
            return null;
        }
    }

    /**
     * A <tt>PageLoader</tt> used to load page data.
     */
    /* package */ static interface PageLoader<E> {
        Page<E> loadPage(int position, int page, int offset, int itemCount);
    }

    /**
     * Class <tt>PageAdapterImpl</tt> is an implementation of a page adapter.
     */
    /* package */ static final class PageAdapterImpl<E> {
        public int mItemCount;
        public final int mPageSize;
        public final int mFirstPageSize;

        public final BitSet mPageStates;
        public final PageLoader<E> mPageLoader;
        public final Cache<Integer, Page<E>> mPageCache;

        @SuppressWarnings("unchecked")
        public PageAdapterImpl(PageLoader<E> loader, Cache<Integer, ? extends Page<E>> pageCache, int pageSize, int firstPageSize) {
            DebugUtils.__checkError(pageSize <= 0 || firstPageSize <= 0, "pageSize <= 0 || firstPageSize <= 0");
            mFirstPageSize = firstPageSize;
            mPageSize   = pageSize;
            mPageLoader = loader;
            mPageStates = new BitSet();
            mPageCache  = (Cache<Integer, Page<E>>)pageCache;
        }

        public final void setItemCount(int count) {
            mItemCount = count;
            mPageCache.clear();
            mPageStates.clear();
        }

        public final E getItem(int position) {
            final Page<E> page = getPage(getPageIndex(position), position);
            return (page != null ? page.getItem(getPagePosition(position)) : null);
        }

        public final E peekItem(int position) {
            DebugUtils.__checkUIThread("peekItem");
            DebugUtils.__checkError(position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
            final Page<E> page = mPageCache.get(getPageIndex(position));
            return (page != null ? page.getItem(getPagePosition(position)) : null);
        }

        public final int setPage(int page, Page<E> data) {
            DebugUtils.__checkUIThread("setPage");
            // Clears the page loading state when the page is load complete.
            mPageStates.clear(page);
            final int count = getCount(data);
            if (count > 0) {
                mPageCache.put(page, data);
            }

            return count;
        }

        public final Page<E> getPage(int page, int position) {
            DebugUtils.__checkUIThread("getPage");
            DebugUtils.__checkError(position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
            Page<E> result = mPageCache.get(page);
            if (result == null && !mPageStates.get(page)) {
                // Computes the page offset and item count.
                int offset = 0, itemCount = mFirstPageSize;
                if (page > 0) {
                    itemCount = mPageSize;
                    offset = (page - 1) * mPageSize + mFirstPageSize;
                }

                // Marks the page loading state, if the page is not load.
                mPageStates.set(page);
                result = mPageLoader.loadPage(position, page, offset, itemCount);
                if (getCount(result) > 0) {
                    // If the page is load successful.
                    // 1. Adds the page to page cache.
                    // 2. Clears the page loading state.
                    mPageStates.clear(page);
                    mPageCache.put(page, result);
                }
            }

            return result;
        }

        public final int getPageIndex(int position) {
            return (position < mFirstPageSize ? 0 : (position - mFirstPageSize) / mPageSize + 1);
        }

        public final int getPagePosition(int position) {
            return (position < mFirstPageSize ? position : (position - mFirstPageSize) % mPageSize);
        }

        public final int getAdapterPosition(int page, int pagePosition) {
            return (page > 0 ? (page - 1) * mPageSize + mFirstPageSize + pagePosition : pagePosition);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Pages() {
    }
}
