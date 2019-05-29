package android.ext.page;

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
     * @see ListPage
     */
    public static <E> Page<E> newPage(List<E> data) {
        return (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(data) : null);
    }

    /**
     * Returns a new {@link Page} to hold the <tt>data</tt>, handling <tt>null</tt> <em>data</em>.
     * @param data An array of the page data.
     * @return A new <tt>Page</tt> or <tt>null</tt>.
     * @see ArrayPage
     */
    @SuppressWarnings("unchecked")
    public static <E> Page<E> newPage(E... data) {
        return (ArrayUtils.getSize(data) > 0 ? new ArrayPage<E>(data) : null);
    }

    /**
     * Returns a new {@link Page} to hold the <tt>data</tt>, handling <tt>null</tt> <em>data</em>.
     * @param data A {@link JSONArray} of the page data.
     * @return A new <tt>Page</tt> or <tt>null</tt>.
     * @see JSONPage
     */
    public static <E> Page<E> newPage(JSONArray data) {
        return (JsonUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null);
    }

    /**
     * Returns a new {@link ResourcePage} to hold the <tt>cursor</tt>, handling <tt>null</tt> <em>cursor</em>.
     * @param cursor A {@link Cursor} of the page data.
     * @return A new <tt>ResourcePage</tt> or <tt>null</tt>.
     * @see CursorPage
     */
    public static ResourcePage<Cursor> newPage(Cursor cursor) {
        return (DatabaseUtils.getCount(cursor) > 0 ? new CursorPage(cursor) : null);
    }

    /**
     * Returns a new page cache instance.
     * @param maxPages The maximum number of pages to allow in the page cache. Pass
     * <tt>0</tt> that the returned page cache is the <b>unlimited-size</b> cache.
     * @return A new {@link Page} {@link Cache} instance.
     * @see #newResourcePageCache(int)
     * @see ArrayMapCache
     * @see SimpleLruCache
     */
    public static <E> Cache<Integer, Page<E>> newPageCache(int maxPages) {
        return (maxPages > 0 ? new SimpleLruCache<Integer, Page<E>>(maxPages) : new ArrayMapCache<Integer, Page<E>>(8));
    }

    /**
     * Returns a new resource page cache instance.
     * @param maxPages The maximum number of pages to allow in the page cache. Pass
     * <tt>0</tt> that the returned page cache is the <b>unlimited-size</b> cache.
     * @return A new {@link ResourcePage} {@link Cache} instance.
     * @see ResourcePage
     * @see #newPageCache(int)
     */
    public static <E> Cache<Integer, ResourcePage<E>> newResourcePageCache(int maxPages) {
        return (maxPages > 0 ? new LruResourcePageCache<E>(maxPages) : new ResourcePageCache<E>(8));
    }

    /**
     * Class <tt>ListPage</tt> is an implementation of a {@link Page}.
     */
    public static class ListPage<E> implements Page<E> {
        /**
         * The {@link List} of the page data.
         */
        protected final List<E> mData;

        /**
         * Constructor
         * @param data A {@link List} of the page data.
         */
        public ListPage(List<E> data) {
            DebugUtils.__checkError(ArrayUtils.getSize(data) == 0, "data == null || data.size() == 0");
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
     * Class <tt>ArrayPage</tt> is an implementation of a {@link Page}.
     */
    public static class ArrayPage<E> implements Page<E> {
        /**
         * The array of the page data.
         */
        protected final E[] mData;

        /**
         * Constructor
         * @param data An array of the page data.
         */
        public ArrayPage(E[] data) {
            DebugUtils.__checkError(ArrayUtils.getSize(data) == 0, "data == null || data.length == 0");
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.length;
        }

        @Override
        public E getItem(int position) {
            return mData[position];
        }
    }

    /**
     * Class <tt>JSONPage</tt> is an implementation of a {@link Page}.
     */
    public static class JSONPage<E> implements Page<E> {
        /**
         * The {@link JSONArray} of the page data.
         */
        protected final JSONArray mData;

        /**
         * Constructor
         * @param data A {@link JSONArray} of the page data.
         */
        public JSONPage(JSONArray data) {
            DebugUtils.__checkError(JsonUtils.getSize(data) == 0, "data == null || data.length() == 0");
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
     * Class <tt>CursorPage</tt> is an implementation of a {@link ResourcePage}.
     */
    public static class CursorPage implements ResourcePage<Cursor> {
        /**
         * The {@link Cursor} of the page data.
         */
        protected final Cursor mCursor;

        /**
         * Constructor
         * @param cursor A {@link Cursor} of the page data.
         */
        public CursorPage(Cursor cursor) {
            DebugUtils.__checkError(DatabaseUtils.getCount(cursor) == 0, "cursor == null || cursor.getCount() == 0");
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
     * Class <tt>ResourcePageCache</tt> is an implementation of a {@link Cache}.
     */
    private static final class ResourcePageCache<E> extends ArrayMapCache<Integer, ResourcePage<E>> {
        /**
         * Constructor
         * @param capacity The initial capacity of this cache.
         */
        public ResourcePageCache(int capacity) {
            super(capacity);
        }

        @Override
        public void clear() {
            final int size = map.size();
            if (size > 0) {
                for (int i = 0; i < size; ++i) {
                    map.valueAt(i).close();
                }

                map.clear();
            }
        }

        @Override
        public ResourcePage<E> remove(Integer key) {
            final ResourcePage<E> result = map.remove(key);
            if (result != null) {
                result.close();
            }

            return result;
        }

        @Override
        public ResourcePage<E> put(Integer key, ResourcePage<E> value) {
            final ResourcePage<E> result = map.put(key, value);
            if (result != null) {
                result.close();
            }

            return result;
        }
    }

    /**
     * Class <tt>LruResourcePageCache</tt> is an implementation of a {@link Cache}.
     */
    private static final class LruResourcePageCache<E> extends SimpleLruCache<Integer, ResourcePage<E>> {
        /**
         * Constructor
         * @param maxSize The maximum number of pages to allow in this cache.
         */
        public LruResourcePageCache(int maxSize) {
            super(maxSize);
        }

        @Override
        public void clear() {
            trimToSize(-1);
        }

        @Override
        protected void entryRemoved(boolean evicted, Integer key, ResourcePage<E> oldPage, ResourcePage<E> newPage) {
            if (evicted || oldPage != newPage) {
                oldPage.close();
            }
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

        @SuppressWarnings("unchecked")
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
            DebugUtils.__checkError(count < 0, "count < 0");
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

        @SuppressWarnings("resource")
        /* package */ final void dump(Printer printer, String className) {
            final StringBuilder result = new StringBuilder(128);
            final Formatter formatter  = new Formatter(result);
            final Set<Entry<Integer, Page<E>>> entries = mPageCache.entries().entrySet();

            DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ initialSize = %d, pageSize = %d, itemCount = %d ] ", className, mInitialSize, mPageSize, mItemCount);
            result.setLength(0);
            printer.println(result.append("  PageCache [ ").append(DebugUtils.toString(mPageCache)).append(", size = ").append(entries.size()).append(" ]").toString());

            for (Entry<Integer, Page<E>> entry : entries) {
                final Page<E> page = entry.getValue();
                result.setLength(0);

                formatter.format("    Page %-2d ==> ", entry.getKey());
                printer.println(DebugUtils.toString(page, result).append(" { itemCount = ").append(page.getCount()).append(" }").toString());
            }
        }

        private static int getCount(Page<?> page) {
            return (page != null ? page.getCount() : 0);
        }

        private void prefetchPage(int currentPage, int position) {
            // Prefetch the previous page data.
            if (currentPage > 0 && position == mPrefetchDistance - 1) {
                DebugUtils.__checkDebug(true, getClass().getSimpleName(), "prefetchPage = " + (currentPage - 1) + ", position = " + position);
                getPage(currentPage - 1);
            }

            final int lastPage = (mItemCount - mInitialSize - 1) / mPageSize + 1;
            if (currentPage < lastPage) {
                // Prefetch the next page data.
                final int pageSize = (currentPage > 0 ? mPageSize : mInitialSize);
                if (position == pageSize - mPrefetchDistance) {
                    DebugUtils.__checkDebug(true, getClass().getSimpleName(), "prefetchPage = " + (currentPage + 1) + ", position = " + position);
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
