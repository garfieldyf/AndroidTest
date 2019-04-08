package android.ext.cache;

import java.util.Collections;
import java.util.Map;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.util.ArrayMap;
import android.util.Printer;

/**
 * Class <tt>ArrayMapCache</tt> is an implementation of a {@link Cache}.
 * This cache is the <b>unlimited-size</b> and <b>not</b> thread-safely.
 * @author Garfield
 */
public class ArrayMapCache<K, V> implements Cache<K, V> {
    protected final ArrayMap<K, V> map;

    /**
     * Constructor
     * @see #ArrayMapCache(int)
     */
    public ArrayMapCache() {
        map = new ArrayMap<K, V>();
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this cache.
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

    @Override
    public Map<K, V> entries() {
        return Collections.unmodifiableMap(map);
    }

    /* package */ final void dump(Context context, Printer printer) {
        final StringBuilder result = new StringBuilder(256);
        final int size = map.size();
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d ] ", getClass().getSimpleName(), size);
        for (int i = 0; i < size; ++i) {
            result.setLength(0);
            printer.println(result.append("  ").append(map.keyAt(i)).append(" ==> ").append(map.valueAt(i)).toString());
        }
    }
}
