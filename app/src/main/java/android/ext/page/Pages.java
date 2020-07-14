package android.ext.page;

import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import java.util.List;

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
     * @see PageAdapter#getPageForPosition(int)
     */
    public static int getOriginalPage(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "combinedPosition(" + combinedPosition + ") < 0");
        return (int)(combinedPosition >> 32);
    }

    /**
     * Returns the index of the item in the page with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by <tt>getPageForPosition</tt>.
     * @return The index of the item in the page.
     * @see #getOriginalPage(long)
     * @see PageAdapter#getPageForPosition(int)
     */
    public static int getOriginalPosition(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "combinedPosition(" + combinedPosition + ") < 0");
        return (int)combinedPosition;
    }

    /**
     * Returns a new {@link Page} to hold the <tt>data</tt>, handling <tt>null</tt> <em>data</em>.
     * @param data A {@link List} of the page data.
     * @return A new <tt>Page</tt> or <tt>null</tt>.
     * @see ListPage
     */
    public static <E> Page<E> newPage(List<?> data) {
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
        @SuppressWarnings({ "unchecked", "rawtypes" })
        public ListPage(List data) {
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

        @Override
        public E setItem(int position, E value) {
            return mData.set(position, value);
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
        @SuppressWarnings("unchecked")
        public ArrayPage(E... data) {
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

        @Override
        public E setItem(int position, E value) {
            final E previous = mData[position];
            mData[position] = value;
            return previous;
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private Pages() {
    }
}
