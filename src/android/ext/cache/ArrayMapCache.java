package android.ext.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
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
        final Set<Entry<K, V>> entries = entries().entrySet();

        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ size = %d ] ", getClass().getSimpleName(), entries.size());
        for (Entry<?, ?> entry : entries) {
            result.setLength(0);
            printer.println(result.append("  ").append(entry.getKey()).append(" ==> ").append(entry.getValue()).toString());
        }
    }
}
