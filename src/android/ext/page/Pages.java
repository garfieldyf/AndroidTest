package android.ext.page;

import java.util.List;
import org.json.JSONArray;
import android.database.Cursor;
import android.ext.cache.ArrayMapCache;
import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.ext.database.DatabaseUtils;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.JsonUtils;

/**
 * Class Pages
 * @author Garfield
 */
public final class Pages {
    /**
     * Returns the numbers of items in the <em>page</em>, handling <tt>null Page</tt>.
     * @param page The {@link Page}.
     * @return The numbers of items in the <em>page</em>.
     */
    public static int getCount(Page<?> page) {
        return (page != null ? page.getCount() : 0);
    }

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
     * This utility class cannot be instantiated.
     */
    private Pages() {
    }
}
