package android.ext.cache;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.ext.util.DebugUtils;

/**
 * Like as {@link SimpleLruCache}, but this class is thread-safely.
 * By default, this cache size is the number of entries. Overrides
 * {@link #sizeOf} to size this cache in different units.
 * @author Garfield
 */
public class LruCache<K, V> extends SimpleLruCache<K, V> {
    /**
     * The size of this cache in units. Not necessarily the number of entries.
     */
    private int size;

    /**
     * Constructor
     * @param maxSize For caches that do not override {@link #sizeOf}, this is
     * the maximum number of entries to allow in this cache. For all other caches,
     * this is the maximum sum of the sizes of the entries to allow in this cache.
     */
    public LruCache(int maxSize) {
        super(maxSize);
    }

    /**
     * For caches that do not override {@link #sizeOf}, this returns
     * the number of entries in the cache. For all other caches, this
     * returns the sum of the sizes of the entries in this cache.
     * @return The size.
     */
    @Override
    public synchronized int size() {
        return size;
    }

    @Override
    public synchronized void clear() {
        size = 0;
        map.clear();
    }

    @Override
    public synchronized V get(K key) {
        DebugUtils.__checkError(key == null, "key == null");
        return map.get(key);
    }

    @Override
    public void trimToSize(int maxSize) {
        while (true) {
            final Entry<K, V> toEvict = evict(maxSize);
            if (toEvict == null) {
                break;
            }

            final K key = toEvict.getKey();
            final V value = toEvict.getValue();
            DebugUtils.__checkError(key == null || value == null, "key == null || value == null");
            entryRemoved(true, key, value, null);
        }
    }

    @Override
    public synchronized Map<K, V> entries() {
        return Collections.unmodifiableMap(new LinkedHashMap<K, V>(map));
    }

    /**
     * Returns the size of the entry for <tt>key</tt> and <tt>value</tt> in
     * user-defined units. The default implementation returns 1 so that size
     * is the number of entries and max size is the maximum number of entries.
     * @param key The key.
     * @param value The value.
     * @return The size of the entry, must be <tt>>= 0</tt>.
     */
    protected int sizeOf(K key, V value) {
        return 1;
    }

    @Override
    /* package */ synchronized V removeImpl(K key) {
        final V previous = map.remove(key);
        if (previous != null) {
            size -= sizeOf(key, previous);
        }

        return previous;
    }

    @Override
    /* package */ synchronized V putImpl(K key, V value) {
        int result = sizeOf(key, value);
        DebugUtils.__checkError(result < 0, "Negative size: " + key + " = " + value);
        size += result;
        final V previous = map.put(key, value);
        if (previous != null) {
            result = sizeOf(key, previous);
            DebugUtils.__checkError(result < 0, "Negative size: " + key + " = " + previous);
            size -= result;
        }

        return previous;
    }

    private synchronized Entry<K, V> evict(int maxSize) {
        if (size <= maxSize || map.isEmpty()) {
            return null;
        }

        final Entry<K, V> toEvict = map.entrySet().iterator().next();
        final K key = toEvict.getKey();
        final V value = toEvict.getValue();
        map.remove(key);
        size -= sizeOf(key, value);
        return toEvict;
    }
}
