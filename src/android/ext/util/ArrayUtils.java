package android.ext.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.RandomAccess;

/**
 * Class ArrayUtils
 * @author Garfield
 */
@SuppressWarnings("unchecked")
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
     * @see #getSize(ByteArrayBuffer)
     */
    public static int getSize(Map<?, ?> map) {
        return (map != null ? map.size() : 0);
    }

    /**
     * Returns the number of bytes in the <em>buffer</em>,
     * handling <tt>null ByteArrayBuffer</tt>.
     * @param buffer The <tt>ByteArrayBuffer</tt>.
     * @return The number of bytes.
     * @see #getSize(Map)
     * @see #getSize(Collection)
     */
    public static int getSize(ByteArrayBuffer buffer) {
        return (buffer != null ? buffer.size() : 0);
    }

    /**
     * Returns the number of elements in the <em>collection</em>,
     * handling <tt>null Collection</tt>.
     * @param collection The <tt>Collection</tt>.
     * @return The number of elements.
     * @see #getSize(Map)
     * @see #getSize(ByteArrayBuffer)
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
        if (list instanceof RandomAccess) {
            for (; start < end; ++start) {
                if (filter.accept(list.get(start))) {
                    return start;
                }
            }
        } else {
            final ListIterator<T> itor = list.listIterator(start);
            for (; itor.hasNext() && start < end; ++start) {
                if (filter.accept(itor.next())) {
                    return itor.previousIndex();
                }
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
        if (list instanceof RandomAccess) {
            for (--end; end >= start; --end) {
                if (filter.accept(list.get(end))) {
                    return end;
                }
            }
        } else {
            final ListIterator<T> itor = list.listIterator(end);
            for (; itor.hasPrevious() && start < end; ++start) {
                if (filter.accept(itor.previous())) {
                    return itor.nextIndex();
                }
            }
        }

        return -1;
    }

    /**
     * Finds the <tt>Collection</tt> with specified <em>filter</em> and returns the element
     * of the first occurrence.
     * @param collection The <tt>Collection</tt> to find.
     * @param filter The {@link Filter} to find.
     * @return The element of the first occurrence, or <tt>null</tt> if it was not found.
     */
    public static <T> T find(Collection<T> collection, Filter<? super T> filter) {
        for (T value : collection) {
            if (filter.accept(value)) {
                return value;
            }
        }

        return null;
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
        final int writtenBytes = (end - start) >> 1;
        for (int high, low; start < end; ++offset) {
            high = Character.digit((int)hex.charAt(start++), 16) << 4;
            low  = Character.digit((int)hex.charAt(start++), 16);
            out[offset] = (byte)(high + low);
        }

        return writtenBytes;
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
     * This utility class cannot be instantiated.
     */
    private ArrayUtils() {
    }
}
