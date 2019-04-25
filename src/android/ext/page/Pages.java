package android.ext.page;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Formatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import android.database.Cursor;
import android.ext.cache.ArrayMapCache;
import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.ext.database.DatabaseUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.JsonUtils;
import android.util.Printer;

/**
 * Class Pages
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "resource" })
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
     * Returns a new {@link Page} to hold the <tt>data</tt>, handling <tt>null</tt> <em>data</em>.
     * @param data A {@link List} of the page data.
     * @return A new <tt>Page</tt> or <tt>null</tt>.
     */
    public static <E> Page<E> newPage(List<E> data) {
        return (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(data) : null);
    }

    /**
     * Returns a new {@link Page} to hold the <tt>data</tt>, handling <tt>null</tt> <em>data</em>.
     * @param data An array of the page data.
     * @return A new <tt>Page</tt> or <tt>null</tt>.
     */
    public static <E> Page<E> newPage(E... data) {
        return (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(Arrays.asList(data)) : null);
    }

    /**
     * Returns a new {@link Page} to hold the <tt>data</tt>, handling <tt>null</tt> <em>data</em>.
     * @param data A {@link JSONArray} of the page data.
     * @return A new <tt>Page</tt> or <tt>null</tt>.
     */
    public static <E> Page<E> newPage(JSONArray data) {
        return (JsonUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null);
    }

    /**
     * Returns a new {@link ResourcePage} to hold the <tt>cursor</tt>, handling <tt>null</tt> <em>cursor</em>.
     * @param cursor A {@link Cursor} of the page data.
     * @return A new <tt>ResourcePage</tt> or <tt>null</tt>.
     */
    public static ResourcePage<Cursor> newPage(Cursor cursor) {
        return (DatabaseUtils.getCount(cursor) > 0 ? new CursorPage(cursor) : null);
    }

    /**
     * Returns a new page cache instance.
     * @param maxPages The maximum number of pages to allow in the page cache. Pass
     * <tt>0</tt> that the returned page cache is the <b>unlimited-size</b> cache.
     * @return A new {@link Page} {@link Cache} instance.
     * @see SimpleLruCache
     * @see ArrayMapCache
     * @see ResourcePageCache
     */
    public static <E> Cache<Integer, Page<E>> newPageCache(int maxPages) {
        return (maxPages > 0 ? new SimpleLruCache<Integer, Page<E>>(maxPages) : new ArrayMapCache<Integer, Page<E>>(8));
    }

    /**
     * Class <tt>ListPage</tt> is an implementation of a {@link Page}.
     */
    private static final class ListPage<E> implements Page<E> {
        private final List<E> mData;

        /**
         * Constructor
         * @param data A {@link List} of this page data.
         */
        public ListPage(List<E> data) {
            DebugUtils.__checkError(ArrayUtils.getSize(data) <= 0, "data == null || data.size() == 0");
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public E getItem(int position) {
            return (position >= 0 && position < mData.size() ? mData.get(position) : null);
        }
    }

    /**
     * Class <tt>JSONPage</tt> is an implementation of a {@link Page}.
     */
    private static final class JSONPage<E> implements Page<E> {
        private final JSONArray mData;

        /**
         * Constructor
         * @param data A {@link JSONArray} of this page data.
         */
        public JSONPage(JSONArray data) {
            DebugUtils.__checkError(JsonUtils.getSize(data) <= 0, "data == null || data.length() == 0");
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.length();
        }

        @Override
        public E getItem(int position) {
            return (E)mData.opt(position);
        }
    }

    /**
     * Class <tt>CursorPage</tt> is an implementation of a {@link ResourcePage}.
     */
    private static final class CursorPage implements ResourcePage<Cursor> {
        private final Cursor mCursor;

        /**
         * Constructor
         * @param cursor A {@link Cursor} of this page data.
         */
        public CursorPage(Cursor cursor) {
            DebugUtils.__checkError(DatabaseUtils.getCount(cursor) <= 0, "cursor == null || cursor.getCount() == 0");
            mCursor = cursor;
        }

        @Override
        public void close() {
            mCursor.close();
        }

        @Override
        public int getCount() {
            return mCursor.getCount();
        }

        @Override
        public Cursor getItem(int position) {
            return (mCursor.moveToPosition(position) ? mCursor : null);
        }
    }

    /**
     * A <tt>PageLoader</tt> used to load page data.
     */
    /* package */ static interface PageLoader<E> {
        Page<E> loadPage(int page, int startPosition, int itemCount);
    }

    /**
     * Class <tt>PageAdapterImpl</tt> is an implementation of a page adapter.
     */
    /* package */ static final class PageAdapterImpl<E> {
        /* package */ int mItemCount;
        /* package */ final int mPageSize;
        /* package */ final int mInitialSize;
        /* package */ final int mPrefetchDistance;

        /* package */ final BitSet mPageStates;
        /* package */ final PageLoader<E> mPageLoader;
        /* package */ final Cache<Integer, Page<E>> mPageCache;

        /* package */ PageAdapterImpl(Cache<Integer, ? extends Page<? extends E>> pageCache, int initialSize, int pageSize, int prefetchDistance, PageLoader<E> loader) {
            DebugUtils.__checkError(pageSize <= 0 || initialSize <= 0, "pageSize <= 0 || initialSize <= 0");
            DebugUtils.__checkError(prefetchDistance > Math.min(pageSize, initialSize), "prefetchDistance = " + prefetchDistance + " greater than pageSize = " + Math.min(pageSize, initialSize));
            mInitialSize = initialSize;
            mPageLoader  = loader;
            mPageSize    = pageSize;
            mPageStates  = new BitSet();
            mPageCache   = (Cache<Integer, Page<E>>)pageCache;
            mPrefetchDistance = prefetchDistance;
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
            final int currentPage = getOriginalPage(combinedPosition);
            final Page<E> page = getPage(currentPage);
            if (mPrefetchDistance > 0) {
                prefetchPage(currentPage, (int)combinedPosition);
            }

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
                // Computes the startPosition and itemCount to load.
                final int startPosition, itemCount;
                if (page == 0) {
                    itemCount = mInitialSize;
                    startPosition = 0;
                } else {
                    itemCount = mPageSize;
                    startPosition = (page - 1) * mPageSize + mInitialSize;
                }

                // Loads the page data and marks the page loading state.
                mPageStates.set(page);
                result = mPageLoader.loadPage(page, startPosition, itemCount);
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
            final int offset = position - mInitialSize;
            return (offset < 0 ? (position & 0xFFFFFFFFL) : (((long)(offset / mPageSize + 1) << 32) | ((offset % mPageSize) & 0xFFFFFFFFL)));
        }

        /* package */ final int getPositionForPage(int page, int position) {
            DebugUtils.__checkError(page < 0 || position < 0, "page < 0 || position < 0");
            return (page > 0 ? (page - 1) * mPageSize + mInitialSize + position : position);
        }

        /* package */ final void dump(Printer printer, String className) {
            final StringBuilder result = new StringBuilder(128);
            final Formatter formatter  = new Formatter(result);
            final Set<Entry<Integer, Page<E>>> entries = mPageCache.entries().entrySet();

            DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ initialSize = %d, pageSize = %d, itemCount = %d ] ", className, mInitialSize, mPageSize, mItemCount);
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

        private void prefetchPage(int currentPage, int position) {
            // Prefetch the previous page data.
            if (currentPage > 0 && position == mPrefetchDistance - 1) {
                getPage(currentPage - 1);
            }

            final int lastPage = (mItemCount - mInitialSize - 1) / mPageSize + 1;
            if (currentPage < lastPage) {
                // Prefetch the next page data.
                final int pageSize = (currentPage > 0 ? mPageSize : mInitialSize);
                if (position == pageSize - mPrefetchDistance) {
                    getPage(currentPage + 1);
                }
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Pages() {
    }
}
