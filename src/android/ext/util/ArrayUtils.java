package android.ext.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import android.ext.util.Pools.Pool;
import android.util.Log;

/**
 * Class ArrayUtils
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public final class ArrayUtils {
    /**
     * The <tt>0-length</tt> byte array.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];

    /**
     * Returns the number of elements in the <em>array</em>,
     * handling <tt>null Array</tt>.
     * @param array The <tt>Array</tt>.
     * @return The number of elements.
     * @see #getSize(int[])
     * @see #getSize(long[])
     * @see #getSize(Object[])
     */
    public static int getSize(byte[] array) {
        return (array != null ? array.length : 0);
    }

    /**
     * Returns the number of elements in the <em>array</em>,
     * handling <tt>null Array</tt>.
     * @param array The <tt>Array</tt>.
     * @return The number of elements.
     * @see #getSize(byte[])
     * @see #getSize(long[])
     * @see #getSize(Object[])
     */
    public static int getSize(int[] array) {
        return (array != null ? array.length : 0);
    }

    /**
     * Returns the number of elements in the <em>array</em>,
     * handling <tt>null Array</tt>.
     * @param array The <tt>Array</tt>.
     * @return The number of elements.
     * @see #getSize(int[])
     * @see #getSize(byte[])
     * @see #getSize(Object[])
     */
    public static int getSize(long[] array) {
        return (array != null ? array.length : 0);
    }

    /**
     * Returns the number of elements in the <em>array</em>,
     * handling <tt>null Array</tt>.
     * @param array The <tt>Array</tt>.
     * @return The number of elements.
     * @see #getSize(int[])
     * @see #getSize(byte[])
     * @see #getSize(long[])
     */
    public static int getSize(Object[] array) {
        return (array != null ? array.length : 0);
    }

    /**
     * Returns the number of elements in the <em>map</em>,
     * handling <tt>null Map</tt>.
     * @param map The <tt>Map</tt>.
     * @return The number of elements.
     * @see #getSize(Collection)
     */
    public static int getSize(Map<?, ?> map) {
        return (map != null ? map.size() : 0);
    }

    /**
     * Returns the number of elements in the <em>collection</em>,
     * handling <tt>null Collection</tt>.
     * @param collection The <tt>Collection</tt>.
     * @return The number of elements.
     * @see #getSize(Map)
     */
    public static int getSize(Collection<?> collection) {
        return (collection != null ? collection.size() : 0);
    }

    /**
     * Searches the <tt>array</tt> for the specified <em>value</em> and
     * returns the index of the first occurrence.
     * @param array The <tt>array</tt> to search.
     * @param value The element to search for.
     * @return The index of the first occurrence of the <em>value</em>,
     * or <tt>-1</tt> if it was not found.
     * @see #indexOf(Object[], int, int, Object)
     */
    public static int indexOf(Object[] array, Object value) {
        return indexOf(array, 0, array.length, value);
    }

    /**
     * Searches the <tt>array</tt> for the specified <em>value</em> and
     * returns the index of the first occurrence.
     * @param array The <tt>array</tt> to search.
     * @param start The inclusive start index in <em>array</em>.
     * @param end The exclusive end index in <em>array</em>.
     * @param value The element to search for.
     * @return The index of the first occurrence of the <em>value</em>,
     * or <tt>-1</tt> if it was not found.
     * @see #indexOf(Object[], Object)
     */
    public static int indexOf(Object[] array, int start, int end, Object value) {
        DebugUtils.__checkRange(start, end - start, array.length);
        if (value != null) {
            for (; start < end; ++start) {
                if (value.equals(array[start])) {
                    return start;
                }
            }
        } else {
            for (; start < end; ++start) {
                if (array[start] == null) {
                    return start;
                }
            }
        }

        return -1;
    }

    /**
     * Searches the specified <tt>array</tt> for the first occurrence
     * of the element using the specified <tt>filter</tt>.
     * @param array The <tt>array</tt> to search.
     * @param start The inclusive start index in <em>array</em>.
     * @param end The exclusive end index in <em>array</em>.
     * @param filter The {@link Filter} using to search.
     * @return The index of the first occurrence of the element,
     * or <tt>-1</tt> if it was not found.
     * @see #indexOf(List, int, int, Filter)
     */
    public static <T> int indexOf(T[] array, int start, int end, Filter<? super T> filter) {
        DebugUtils.__checkRange(start, end - start, array.length);
        for (; start < end; ++start) {
            if (filter.accept(array[start])) {
                return start;
            }
        }

        return -1;
    }

    /**
     * Searches the specified <tt>List</tt> for the first occurrence
     * of the element using the specified <tt>filter</tt>.
     * @param list The <tt>List</tt> to search.
     * @param start The inclusive start index in <em>list</em>.
     * @param end The exclusive end index in <em>list</em>.
     * @param filter The {@link Filter} using to search.
     * @return The index of the first occurrence of the element,
     * or <tt>-1</tt> if it was not found.
     * @see #indexOf(T[], int, int, Filter)
     */
    public static <T> int indexOf(List<T> list, int start, int end, Filter<? super T> filter) {
        DebugUtils.__checkRange(start, end - start, list.size());
        final ListIterator<T> itor = list.listIterator(start);
        for (; start < end; ++start) {
            if (filter.accept(itor.next())) {
                return itor.previousIndex();
            }
        }

        return -1;
    }

    /**
     * Searches the <tt>array</tt> for the specified <em>value</em> and
     * returns the index of the last occurrence.
     * @param array The <tt>array</tt> to search.
     * @param value The element to search for.
     * @return The index of the last occurrence of the <em>value</em>,
     * or <tt>-1</tt> if it was not found.
     * @see #lastIndexOf(Object[], int, int, Object)
     */
    public static int lastIndexOf(Object[] array, Object value) {
        return lastIndexOf(array, 0, array.length, value);
    }

    /**
     * Searches the <tt>array</tt> for the specified <em>value</em> and
     * returns the index of the last occurrence.
     * @param array The <tt>array</tt> to search.
     * @param start The inclusive start index in <em>array</em>.
     * @param end The exclusive end index in <em>array</em>.
     * @param value The element to search for.
     * @return The index of the last occurrence of the <em>value</em>,
     * or <tt>-1</tt> if it was not found.
     * @see #lastIndexOf(Object[], Object)
     */
    public static int lastIndexOf(Object[] array, int start, int end, Object value) {
        DebugUtils.__checkRange(start, end - start, array.length);
        if (value != null) {
            for (--end; end >= start; --end) {
                if (value.equals(array[end])) {
                    return end;
                }
            }
        } else {
            for (--end; end >= start; --end) {
                if (array[end] == null) {
                    return end;
                }
            }
        }

        return -1;
    }

    /**
     * Searches the specified <tt>array</tt> for the last occurrence
     * of the element using the specified <tt>filter</tt>.
     * @param array The <tt>array</tt> to search.
     * @param start The inclusive start index in <em>array</em>.
     * @param end The exclusive end index in <em>array</em>.
     * @param filter The {@link Filter} using to search.
     * @return The index of the last occurrence of the element,
     * or <tt>-1</tt> if it was not found.
     * @see #lastIndexOf(List, int, int, Filter)
     */
    public static <T> int lastIndexOf(T[] array, int start, int end, Filter<? super T> filter) {
        DebugUtils.__checkRange(start, end - start, array.length);
        for (--end; end >= start; --end) {
            if (filter.accept(array[end])) {
                return end;
            }
        }

        return -1;
    }

    /**
     * Searches the specified <tt>List</tt> for the last occurrence
     * of the element using the specified <tt>filter</tt>.
     * @param list The <tt>List</tt> to search.
     * @param start The inclusive start index in <em>list</em>.
     * @param end The exclusive end index in <em>list</em>.
     * @param filter The {@link Filter} using to search.
     * @return The index of the last occurrence of the element,
     * or <tt>-1</tt> if it was not found.
     * @see #lastIndexOf(T[], int, int, Filter)
     */
    public static <T> int lastIndexOf(List<T> list, int start, int end, Filter<? super T> filter) {
        DebugUtils.__checkRange(start, end - start, list.size());
        final ListIterator<T> itor = list.listIterator(end);
        for (; start < end; ++start) {
            if (filter.accept(itor.previous())) {
                return itor.nextIndex();
            }
        }

        return -1;
    }

    /**
     * Removes the items in the specified range in the <em>list</em>.
     * @param list The {@link List} to remove.
     * @param start The inclusive start index in <em>list</em>.
     * @param end The exclusive end index in <em>list</em>.
     */
    public static <T> void removeRange(List<T> list, int start, int end) {
        DebugUtils.__checkRange(start, end - start, list.size());
        if (start == 0 && end == list.size()) {
            list.clear();
        } else {
            final ListIterator<T> itor = list.listIterator(start);
            for (; start < end; ++start) {
                itor.next();
                itor.remove();
            }
        }
    }

    /**
     * Sorts the specified range in the <em>list</em> in ascending natural order.
     * @param list The {@link List} to sort.
     * @param start The inclusive start index in <em>list</em>.
     * @param end The exclusive end index in <em>list</em>.
     * @see #sort(List, int, int, Comparator)
     */
    public static <T extends Comparable<? super T>> void sort(List<T> list, int start, int end) {
        DebugUtils.__checkRange(start, end - start, list.size());
        if (list instanceof ArrayList) {
            try {
                Arrays.sort(array(list), start, end);
            } catch (Exception e) {
                Log.w(ArrayUtils.class.getName(), "Couldn't sort ArrayList internal array\n" + e.getMessage());
                sortList(list, start, end, null);
            }
        } else {
            sortList(list, start, end, null);
        }
    }

    /**
     * Sorts the specified range in the <em>list</em> using the specified <em>comparator</em>.
     * @param list The {@link List} to sort.
     * @param start The inclusive start index in <em>list</em>.
     * @param end The exclusive end index in <em>list</em>.
     * @param comparator The {@link Comparator} to compare.
     * @see #sort(List, int, int)
     */
    public static <T> void sort(List<T> list, int start, int end, Comparator<? super T> comparator) {
        DebugUtils.__checkRange(start, end - start, list.size());
        if (list instanceof ArrayList) {
            try {
                Arrays.sort(array(list), start, end, (Comparator<Object>)comparator);
            } catch (Exception e) {
                Log.w(ArrayUtils.class.getName(), "Couldn't sort ArrayList internal array\n" + e.getMessage());
                sortList(list, start, end, comparator);
            }
        } else {
            sortList(list, start, end, comparator);
        }
    }

    /**
     * Filters the specified <em>collection</em> using the specified <em>filter</em>.
     * @param collection The collection to filter.
     * @param filter The {@link Filter}.
     * @return The <em>collection</em>.
     */
    public static <E, T extends Collection<? super E>> T filter(T collection, Filter<? super E> filter) {
        final Iterator<E> itor = (Iterator<E>)collection.iterator();
        while (itor.hasNext()) {
            if (!filter.accept(itor.next())) {
                itor.remove();
            }
        }

        return collection;
    }

    /**
     * Inserts the specified <em>value</em> into the specified sorted <em>list</em>
     * at the appropriate position. The <em>list</em> needs to be already sorted in
     * natural sorting order.
     * @param list The sorted list to insert to.
     * @param value The element to insert.
     * @see #insert(List, T, Comparator)
     */
    public static <T extends Comparable<? super T>> void insert(List<T> list, T value) {
        final ListIterator<T> itor = list.listIterator();
        while (itor.hasNext()) {
            final T next = itor.next();
            if (next.compareTo(value) > 0) {
                // Swaps the value to next.
                itor.set(value);
                value = next;
                break;
            }
        }

        // Inserts the value into the list between previous and next.
        itor.add(value);
    }

    /**
     * Inserts the specified <em>value</em> into the specified sorted <em>list</em> at
     * the appropriate position. The <em>list</em> needs to be already sorted according
     * to the <em>comparator</em>.
     * @param list The sorted list to insert to.
     * @param value The element to insert.
     * @param comparator The {@link Comparator} to compare.
     * @see #insert(List, T)
     */
    public static <T> void insert(List<? extends T> list, T value, Comparator<? super T> comparator) {
        final ListIterator<T> itor = (ListIterator<T>)list.listIterator();
        while (itor.hasNext()) {
            final T next = itor.next();
            if (comparator.compare(next, value) > 0) {
                // Swaps the value to next.
                itor.set(value);
                value = next;
                break;
            }
        }

        // Inserts the value into the list between previous and next.
        itor.add(value);
    }

    /**
     * Adjusts the specified <em>value</em> into range of <em>[min, max]</em>.
     * @param value The value to adjust.
     * @param min The minimum value.
     * @param max The maximum value.
     * @return If <tt>value < min</tt>, returns <tt>min</tt>; if <tt>value > max</tt>,
     * returns <tt>max</tt>; otherwise returns <tt>value</tt>.
     */
    public static int rangeOf(int value, int min, int max) {
        return (value < min ? min : (value > max ? max : value));
    }

    /**
     * Returns a mutable {@link List} from the specified <em>array</em>,
     * handling <tt>null</tt> <em>array</em>.
     * @param array The array.
     * @return The <tt>List</tt> contains the array elements.
     */
    public static <T> List<T> toList(T... array) {
        final int size = getSize(array);
        final List<T> result = new ArrayList<T>(size);
        for (int i = 0; i < size; ++i) {
            result.add(array[i]);
        }

        return result;
    }

    /**
     * Converts the specified hexadecimal string to a byte array.
     * @param hex The hexadecimal string to convert.
     * @return The converted byte array.
     * @see #toByteArray(CharSequence, int, int)
     * @see #toByteArray(CharSequence, byte[], int)
     * @see #toByteArray(CharSequence, int, int, byte[], int)
     */
    public static byte[] toByteArray(CharSequence hex) {
        final int length = hex.length();
        final byte[] result = new byte[length >> 1];
        toByteArray(hex, 0, length, result, 0);
        return result;
    }

    /**
     * Converts the specified hexadecimal string to a byte array.
     * @param hex The hexadecimal string to convert.
     * @param start The inclusive beginning index of the <em>hex</em>.
     * @param end The exclusive end index of the <em>hex</em>.
     * @return The converted byte array.
     * @see #toByteArray(CharSequence)
     * @see #toByteArray(CharSequence, byte[], int)
     * @see #toByteArray(CharSequence, int, int, byte[], int)
     */
    public static byte[] toByteArray(CharSequence hex, int start, int end) {
        final byte[] result = new byte[(end - start) >> 1];
        toByteArray(hex, start, end, result, 0);
        return result;
    }

    /**
     * Converts the specified hexadecimal string into the specified byte array.
     * @param hex The hexadecimal string to convert.
     * @param out The byte array to store the converted result.
     * @param offset The starting offset of the <em>out</em>.
     * @return The number of bytes written to <em>out</em>.
     * @see #toByteArray(CharSequence)
     * @see #toByteArray(CharSequence, int, int)
     * @see #toByteArray(CharSequence, int, int, byte[], int)
     */
    public static int toByteArray(CharSequence hex, byte[] out, int offset) {
        return toByteArray(hex, 0, hex.length(), out, offset);
    }

    /**
     * Converts the specified hexadecimal string into the specified byte array.
     * @param hex The hexadecimal string to convert.
     * @param start The inclusive beginning index of the <em>hex</em>.
     * @param end The exclusive end index of the <em>hex</em>.
     * @param out The byte array to store the converted result.
     * @param offset The starting offset of the <em>out</em>.
     * @return The number of bytes written to <em>out</em>.
     * @see #toByteArray(CharSequence)
     * @see #toByteArray(CharSequence, int, int)
     * @see #toByteArray(CharSequence, byte[], int)
     */
    public static int toByteArray(CharSequence hex, int start, int end, byte[] out, int offset) {
        DebugUtils.__checkRange(start, end - start, hex.length());
        DebugUtils.__checkRange(offset, (end - start) >> 1, out.length);
        DebugUtils.__checkError((end - start) % 2 != 0, "The hex length (" + (end - start) + ") must be even.");
        for (int i = start, high, low; i < end; ++offset) {
            high = Character.digit((int)hex.charAt(i++), 16) << 4;
            low  = Character.digit((int)hex.charAt(i++), 16);
            out[offset] = (byte)(high + low);
        }

        return (end - start) >> 1;
    }

    private static void sortList(List list, int start, int end, Comparator comparator) {
        final Object[] array = list.toArray();
        if (comparator == null) {
            Arrays.sort(array, start, end);
        } else {
            Arrays.sort(array, start, end, comparator);
        }

        final ListIterator itor = list.listIterator(start);
        for (; start < end; ++start) {
            itor.next();
            itor.set(array[start]);
        }
    }

    private static synchronized Object[] array(Object list) throws Exception {
        if (sArrayField == null) {
            sArrayField = ClassUtils.getDeclaredField(ArrayList.class, "array");
        }

        return (Object[])sArrayField.get(list);
    }

    /**
     * Class <tt>ByteArrayPool</tt> for managing a pool of byte arrays.
     */
    public static final class ByteArrayPool {
        private static final Pool<byte[]> sInstance = Pools.synchronizedPool(Pools.newPool(2, 8192));

        /**
         * Retrieves a byte array from this pool. Allows us to avoid allocating new
         * byte array in many cases. When the byte array can no longer be used, The
         * caller should be call {@link #recycle(byte[])} to recycles the byte array.
         * @return A byte array.
         * @see #recycle(byte[])
         */
        public static byte[] obtain() {
            return sInstance.obtain();
        }

        /**
         * Recycles the specified <em>array</em> to this pool. After calling
         * this function you must not ever touch the <em>array</em> again.
         * @param array The byte array to recycle.
         * @see #obtain()
         */
        public static void recycle(byte[] array) {
            sInstance.recycle(array);
        }
    }

    /**
     * An interface for filtering objects based on their informations.
     * @see Filter#accept(T)
     */
    public static interface Filter<T> {
        /**
         * Indicating whether a specific <em>value</em> should be accepted.
         * @param value The value to check.
         * @return <tt>true</tt> if the <em>value</em> should be accepted,
         * <tt>false</tt> otherwise.
         */
        public boolean accept(T value);
    }

    /**
     * The {@link ArrayList#array} field.
     */
    private static Field sArrayField;

    /**
     * This utility class cannot be instantiated.
     */
    private ArrayUtils() {
    }
}
