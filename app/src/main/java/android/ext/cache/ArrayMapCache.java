package android.ext.cache;

import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.util.ArrayMap;
import android.util.Printer;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class <tt>ArrayMapCache</tt> is an implementation of a {@link Cache}.
 * @author Garfield
 */
public final class ArrayMapCache<K, V> implements Cache<K, V> {
    private final ArrayMap<K, V> map;

    /**
     * Constructor
     * @see #ArrayMapCache(int)
     */
    public ArrayMapCache() {
        map = new ArrayMap<K, V>();
    }

    /**
     * Constructor
     * @param capacity The initial capacity of the cache.
     * @see #ArrayMapCache()
     */
    public ArrayMapCache(int capacity) {
        map = new ArrayMap<K, V>(capacity);
    }

    /**
     * Returns the number of key-value in this cache.
     * @return The number of key-value in this cache.
     */
    public final int size() {
        return map.size();
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public V remove(K key) {
        return map.remove(key);
    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        return map.put(key, value);
    }

    /**
     * Returns the key at the given <em>index</em> in this cache.
     * @param index The index, must be between 0 and {@link #size()} - 1.
     * @return The key that was stored at the <em>index</em>.
     */
    public final K keyAt(int index) {
        DebugUtils.__checkError(index < 0 || index >= map.size(), "Invalid parameter - index out of bounds [ index = " + index + ", size = " + map.size() + " ]");
        return map.keyAt(index);
    }

    /**
     * Returns the value at the given <em>index</em> in this cache.
     * @param index The index, must be between 0 and {@link #size()} - 1.
     * @return The value that was stored at the <em>index</em>.
     */
    public final V valueAt(int index) {
        DebugUtils.__checkError(index < 0 || index >= map.size(), "Invalid parameter - index out of bounds [ index = " + index + ", size = " + map.size() + " ]");
        return map.valueAt(index);
    }

    /**
     * Returns a {@link Set} of all of the keys and values.
     * @return A <tt>Set</tt> of all of the keys and values.
     */
    public final Set<Entry<K, V>> entrySet() {
        return map.entrySet();
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(196);
        final int size = map.size();
        DeviceUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d ] ", getClass().getSimpleName(), size);
        for (int i = 0; i < size; ++i) {
            result.setLength(0);
            printer.println(DeviceUtils.toString(map.valueAt(i), result.append("  ").append(map.keyAt(i)).append(" ==> ")).toString());
        }
    }
}
