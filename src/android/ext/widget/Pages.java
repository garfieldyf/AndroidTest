package android.ext.widget;

import java.util.BitSet;
import java.util.Formatter;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.ext.util.DebugUtils;
import android.util.ArrayMap;
import android.util.Printer;
import android.util.SparseArray;

/**
 * Class Pages
 * @author Garfield
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
     * Returns the index of the page with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by <tt>getPageForPosition</tt>.
     * @return The index of the page.
     * @see #getOriginalPosition(long)
     * @see PageAdapter#getPageForPosition(int)
     * @see RecyclerPageAdapter#getPageForPosition(int)
     */
    public static int getOriginalPage(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "combinedPosition < 0");
        return (int)(combinedPosition >> 32);
    }

    /**
     * Returns the index of the item in the page with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by <tt>getPageForPosition</tt>.
     * @return The index of the item in the page.
     * @see #getOriginalPage(long)
     * @see PageAdapter#getPageForPosition(int)
     * @see RecyclerPageAdapter#getPageForPosition(int)
     */
    public static int getOriginalPosition(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "combinedPosition < 0");
        return (int)combinedPosition;
    }

    /**
     * Returns a new page cache instance.
     * @param maxPages The maximum number of pages to allow in the page cache. Pass
     * <tt>0</tt> that the returned page cache is the <b>unlimited-size</b> cache.
     * @return A new {@link Page} {@link Cache} instance.
     * @see SimpleLruCache
     * @see SparsePageCache
     */
    public static <E> Cache<Integer, Page<E>> createPageCache(int maxPages) {
        return (maxPages > 0 ? new SimpleLruCache<Integer, Page<E>>(maxPages) : new SparsePageCache<E>());
    }

    /**
     * Returns a string containing a concise, human-readable description of the <em>page</em>.
     */
    public static String toString(Page<?> page, Object data) {
        final int count = page.getCount();
        final StringBuilder result = DebugUtils.toSimpleString(data, new StringBuilder(64)).append(" { count = ").append(count);
        if (count > 0) {
            result.append(", itemType = ").append(page.getItem(0).getClass().getName());
        }

        return result.append(" }").toString();
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
            return Pages.toString(this, mData);
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
            return Pages.toString(this, mData);
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

        @Override
        public Map<Integer, Page<E>> snapshot() {
            return new ArrayMap<Integer, Page<E>>(mPages);
        }
    }

    /**
     * Class <tt>SparsePageCache</tt> is an implementation of a {@link Cache}.
     * This page cache is the <b>unlimited-size</b> cache.
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

        @Override
        public Map<Integer, Page<E>> snapshot() {
            final int size = mPages.size();
            final Map<Integer, Page<E>> result = new ArrayMap<Integer, Page<E>>(size);
            for (int i = 0; i < size; ++i) {
                result.put(mPages.keyAt(i), mPages.valueAt(i));
            }

            return result;
        }
    }

    /**
     * A <tt>PageLoader</tt> used to load page data.
     */
    /* package */ static interface PageLoader<E> {
        Page<E> loadPage(int position, int page, int pageOffset, int pageSize);
    }

    /**
     * Class <tt>PageAdapterImpl</tt> is an implementation of a page adapter.
     */
    /* package */ static final class PageAdapterImpl<E> {
        /* package */ int mItemCount;
        /* package */ final int mPageSize;
        /* package */ final int mFirstPageSize;

        /* package */ final BitSet mPageStates;
        /* package */ final PageLoader<E> mPageLoader;
        /* package */ final Cache<Integer, Page<E>> mPageCache;

        @SuppressWarnings("unchecked")
        /* package */ PageAdapterImpl(PageLoader<E> loader, Cache<Integer, ? extends Page<? extends E>> pageCache, int pageSize, int firstPageSize) {
            DebugUtils.__checkError(pageSize <= 0 || firstPageSize <= 0, "pageSize <= 0 || firstPageSize <= 0");
            mPageCache  = (Cache<Integer, Page<E>>)pageCache;
            mPageSize   = pageSize;
            mPageLoader = loader;
            mPageStates = new BitSet();
            mFirstPageSize = firstPageSize;
        }

        /* package */ final void setItemCount(int count) {
            mItemCount = count;
            mPageCache.clear();
            mPageStates.clear();
        }

        /* package */ final E getItem(int position) {
            DebugUtils.__checkUIThread("getItem");
            DebugUtils.__checkError(position < 0 || position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
            final long combinedPosition = getPageForPosition(position);
            final Page<E> page = getPage(getOriginalPage(combinedPosition), position);
            return (page != null ? page.getItem((int)combinedPosition) : null);
        }

        /* package */ final E peekItem(int position) {
            DebugUtils.__checkUIThread("peekItem");
            DebugUtils.__checkError(position < 0 || position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
            final long combinedPosition = getPageForPosition(position);
            final Page<E> page = mPageCache.get(getOriginalPage(combinedPosition));
            return (page != null ? page.getItem((int)combinedPosition) : null);
        }

        /* package */ final int setPage(int page, Page<E> data) {
            // Clears the page loading state when the page is load complete.
            DebugUtils.__checkUIThread("setPage");
            DebugUtils.__checkError(page < 0, "page < 0");
            mPageStates.clear(page);
            final int count = getCount(data);
            if (count > 0) {
                mPageCache.put(page, data);
            }

            return count;
        }

        /* package */ final Page<E> getPage(int page, int position) {
            DebugUtils.__checkUIThread("getPage");
            DebugUtils.__checkError(page < 0, "page < 0");
            DebugUtils.__checkError(position < 0 || position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
            Page<E> result = mPageCache.get(page);
            if (result == null && !mPageStates.get(page)) {
                // Computes the page offset and page Size.
                int pageOffset = 0, pageSize = mFirstPageSize;
                if (page > 0) {
                    pageSize = mPageSize;
                    pageOffset = (page - 1) * mPageSize + mFirstPageSize;
                }

                // Marks the page loading state, if the page is not load.
                mPageStates.set(page);
                result = mPageLoader.loadPage(position, page, pageOffset, pageSize);
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

        /* package */ final long getPageForPosition(int position) {
            DebugUtils.__checkError(position < 0, "position < 0");
            if (position < mFirstPageSize) {
                return (position & 0xFFFFFFFFL);
            } else {
                final int delta = position - mFirstPageSize;
                return (((long)(delta / mPageSize + 1) << 32) | ((delta % mPageSize) & 0xFFFFFFFFL));
            }
        }

        /* package */ final int getPositionForPage(int page, int position) {
            DebugUtils.__checkError(page < 0 || position < 0, "page < 0 || position < 0");
            return (page > 0 ? (page - 1) * mPageSize + mFirstPageSize + position : position);
        }

        @SuppressWarnings("resource")
        /* package */ final void dump(Printer printer, String className) {
            final StringBuilder result = new StringBuilder(128);
            final Formatter formatter  = new Formatter(result);
            final Set<Entry<Integer, Page<E>>> entries = mPageCache.snapshot().entrySet();

            DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ firstPageSize = %d, pageSize = %d, itemCount = %d ] ", className, mFirstPageSize, mPageSize, mItemCount);
            result.setLength(0);
            printer.println(DebugUtils.toString(mPageCache, result.append("  PageCache [ ")).append(", size = ").append(entries.size()).append(" ]").toString());

            for (Entry<Integer, Page<E>> entry : entries) {
                final Page<E> page = entry.getValue();
                result.setLength(0);

                formatter.format("    Page %-3d ==> [ ", entry.getKey());
                printer.println(DebugUtils.toString(page, result).append(", count = ").append(page.getCount()).append(" ]").toString());
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Pages() {
    }
}
