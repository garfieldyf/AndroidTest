package android.ext.cache;

import android.ext.util.DebugUtils;
import android.util.ArrayMap;
import android.util.Printer;

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

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public V remove(K key) {
        DebugUtils.__checkError(key == null, "key == null");
        return map.remove(key);
    }

    @Override
    public V get(K key) {
        DebugUtils.__checkError(key == null, "key == null");
        return map.get(key);
    }

    @Override
    public V put(K key, V value) {
        DebugUtils.__checkError(key == null || value == null, "key == null || value == null");
        return map.put(key, value);
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(196);
        final int size = map.size();
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d ] ", getClass().getSimpleName(), size);
        for (int i = 0; i < size; ++i) {
            result.setLength(0);
            printer.println(result.append("  ").append(map.keyAt(i)).append(" ==> ").append(map.valueAt(i)).toString());
        }
    }
}
