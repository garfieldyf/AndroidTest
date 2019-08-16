package android.ext.page;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import android.ext.cache.Cache;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.JsonUtils;
import android.util.ArrayMap;

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
     * Class <tt>ArrayMapCache</tt> is an implementation of a {@link Cache}.
     */
    public static class ArrayMapCache<E> implements Cache<Integer, Page<E>> {
        protected final ArrayMap<Integer, Page<E>> mPages;

        /**
         * Constructor
         * @see #ArrayMapCache(int)
         */
        public ArrayMapCache() {
            mPages = new ArrayMap<Integer, Page<E>>(8);
        }

        /**
         * Constructor
         * @param capacity The initial capacity of this cache.
         * @see #ArrayMapCache()
         */
        public ArrayMapCache(int capacity) {
            mPages = new ArrayMap<Integer, Page<E>>(capacity);
        }

        @Override
        public void clear() {
            mPages.clear();
        }

        @Override
        public Page<E> remove(Integer page) {
            return mPages.remove(page);
        }

        @Override
        public Page<E> get(Integer page) {
            return mPages.get(page);
        }

        @Override
        public Page<E> put(Integer page, Page<E> value) {
            return mPages.put(page, value);
        }

        @Override
        public Map<Integer, Page<E>> snapshot() {
            return new ArrayMap<Integer, Page<E>>(mPages);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Pages() {
    }
}
