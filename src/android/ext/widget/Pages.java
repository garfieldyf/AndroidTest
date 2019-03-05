package android.ext.widget;

import java.util.BitSet;
import java.util.Formatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import android.ext.cache.ArrayMapCache;
import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.ext.util.DebugUtils;
import android.util.Printer;

/**
 * Class Pages
 * @author Garfield
 */
public final class Pages {
    /**
     * Returns the index of the page with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by <tt>getPageForPosition</tt>.
     * @return The index of the page.
     * @see #getOriginalPosition(long)
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
     * @see ArrayMapCache
     */
    public static <E> Cache<Integer, Page<E>> createPageCache(int maxPages) {
        return (maxPages > 0 ? new SimpleLruCache<Integer, Page<E>>(maxPages) : new ArrayMapCache<Integer, Page<E>>(8));
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
    }

    /**
     * A <tt>PageLoader</tt> used to load page data.
     */
    /* package */ static interface PageLoader<E> {
        Page<E> loadPage(int page, int offset, int count);
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
        /* package */ PageAdapterImpl(PageLoader<E> loader, Cache<Integer, ? extends Page<? extends E>> pageCache, int firstPageSize, int pageSize) {
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
            final Page<E> page = getPage(getOriginalPage(combinedPosition));
            return (page != null ? page.getItem((int)combinedPosition) : null);
        }

        /* package */ final E peekItem(int position) {
            DebugUtils.__checkUIThread("peekItem");
            DebugUtils.__checkError(position < 0 || position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
            final long combinedPosition = getPageForPosition(position);
            final Page<E> page = mPageCache.get(getOriginalPage(combinedPosition));
            return (page != null ? page.getItem((int)combinedPosition) : null);
        }

        /* package */ final Page<E> getPage(int page) {
            DebugUtils.__checkUIThread("getPage");
            DebugUtils.__checkError(page < 0, "page < 0");
            Page<E> result = mPageCache.get(page);
            if (result == null && !mPageStates.get(page)) {
                // Computes the offset and item count to load.
                final int offset, count;
                if (page == 0) {
                    offset = 0;
                    count  = mFirstPageSize;
                } else {
                    count  = mPageSize;
                    offset = (page - 1) * mPageSize + mFirstPageSize;
                }

                // Loads the page data and marks the page loading state.
                mPageStates.set(page);
                result = mPageLoader.loadPage(page, offset, count);
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

        /* package */ final int setPage(int page, Page<E> data) {
            DebugUtils.__checkUIThread("setPage");
            DebugUtils.__checkError(page < 0, "page < 0");
            // Clears the page loading state when the page is load complete.
            mPageStates.clear(page);
            final int count = getCount(data);
            if (count > 0) {
                mPageCache.put(page, data);
            }

            return count;
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
            final Set<Entry<Integer, Page<E>>> entries = mPageCache.entries().entrySet();

            DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ firstPageSize = %d, pageSize = %d, itemCount = %d ] ", className, mFirstPageSize, mPageSize, mItemCount);
            DebugUtils.dumpSummary(printer, result, 100, " PageCache [ %s, size = %d ] ", DebugUtils.toString(mPageCache), entries.size());
            for (Entry<Integer, Page<E>> entry : entries) {
                final Page<E> page = entry.getValue();
                result.setLength(0);

                formatter.format("  Page %-2d ==> ", entry.getKey());
                printer.println(DebugUtils.toString(page, result).append(" { count = ").append(page.getCount()).append(" }").toString());
            }
        }

        private static int getCount(Page<?> page) {
            return (page != null ? page.getCount() : 0);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Pages() {
    }
}
