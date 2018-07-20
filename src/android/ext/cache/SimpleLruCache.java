package android.ext.cache;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.util.Printer;

/**
 * A <tt>SimpleLruCache</tt> that holds strong references to a limited number
 * of values. Each time a value is accessed, it is moved to the head of a queue.
 * When a value is added to a full cache, the value at the end of that queue is
 * evicted and may become eligible for garbage collection. Unlike {@link LruCache}
 * this class is <b>not</b> thread-safely.
 * @author Garfield
 * @version 4.0
 */
public class SimpleLruCache<K, V> implements Cache<K, V> {
    /* package */ final int maxSize;
    /* package */ final Map<K, V> map;

    /**
     * Constructor
     * @param maxSize The maximum number of values to allow in this cache.
     */
    public SimpleLruCache(int maxSize) {
        DebugUtils.__checkError(maxSize <= 0, "maxSize <= 0");
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
    }

    /**
     * Returns the number of entries in this cache.
     * @return The number of entries in this cache.
     * @see #maxSize()
     */
    public int size() {
        return map.size();
    }

    /**
     * Returns the maximum size in this cache in user-defined units.
     * @return The maximum size.
     * @see #size()
     */
    public final int maxSize() {
        return maxSize;
    }

    /**
     * Clears this cache, but do not call {@link #entryRemoved} on each removed entry.
     * @see #evictAll()
     * @see #trimToSize(int)
     */
    @Override
    public void clear() {
        map.clear();
    }

    /**
     * @see #put(K, V)
     * @see #remove(K)
     */
    @Override
    public V get(K key) {
        DebugUtils.__checkError(key == null, "key == null");
        return map.get(key);
    }

    /**
     * Maps the specified <em>key</em> to the specified <tt>value</tt>.
     * The <tt>value</tt> is moved to the head of the queue.
     * @param key The key.
     * @param value The value.
     * @return The previous value mapped by <em>key</em> or <tt>null</tt>
     * if there was no mapping.
     * @see #get(K)
     * @see #remove(K)
     */
    @Override
    public V put(K key, V value) {
        DebugUtils.__checkError(key == null || value == null, "key == null || value == null");
        final V previous = putImpl(key, value);
        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }

        trimToSize(maxSize, false);
        return previous;
    }

    /**
     * @see #get(K)
     * @see #put(K, V)
     */
    @Override
    public V remove(K key) {
        DebugUtils.__checkError(key == null, "key == null");
        final V previous = removeImpl(key);
        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }

    @Override
    public Map<K, V> snapshot() {
        return new LinkedHashMap<K, V>(map);
    }

    /**
     * Clears this cache, calling {@link #entryRemoved} on each removed entry.
     * @see #clear()
     * @see #trimToSize(int)
     */
    public void evictAll() {
        trimToSize(-1, true);
    }

    /**
     * Remove the eldest entries until the total of remaining entries is
     * at or below the requested size.
     * @param maxSize The maximum size of the cache. May be <tt>-1</tt>
     * to evict all entries.
     * @see #clear()
     * @see #evictAll()
     */
    public void trimToSize(int maxSize) {
        trimToSize(maxSize, true);
    }

    @Override
    public String toString() {
        return new StringBuilder(64)
            .append(getClass().getSimpleName())
            .append(" { size = ").append(map.size())
            .append(", maxSize = ").append(maxSize)
            .append(" }").toString();
    }

    /**
     * Called for entries that have been evicted or removed. This method is invoked when a value
     * is evicted to make space, removed by a call to {@link #remove}, or replaced by a call to
     * {@link #put}. The default implementation does nothing. <p>The method is called without
     * synchronization: other threads may access the cache while this method is executing.</p>
     * @param evicted If <tt>true</tt> the entry is being removed to make space, <tt>false</tt>
     * if the removal was caused by a {@link #put} or {@link #remove}.
     * @param key The key.
     * @param oldValue The old value for <em>key</em>.
     * @param newValue The new value for <em>key</em> or <tt>null</tt>.
     */
    protected void entryRemoved(boolean evicted, K key, V oldValue, V newValue) {
    }

    /**
     * Removes the entry for the specified <em>key</em>.
     */
    /* package */ V removeImpl(K key) {
        return map.remove(key);
    }

    /**
     * Maps the specified <em>key</em> to the specified <tt>value</tt>.
     */
    /* package */ V putImpl(K key, V value) {
        return map.put(key, value);
    }

    /**
     * Remove the eldest entries until the total of remaining entries
     * is at or below the requested size.
     */
    /* package */ void trimToSize(int maxSize, boolean evicted) {
        final Iterator<Entry<K, V>> itor = map.entrySet().iterator();
        while (itor.hasNext() && map.size() > maxSize) {
            final Entry<K, V> toEvict = itor.next();
            itor.remove();
            entryRemoved(evicted, toEvict.getKey(), toEvict.getValue(), null);
        }
    }

    /* package */ void dump(Context context, Printer printer) {
        dump(context, printer, map);
    }

    /* package */ final void dump(Context context, Printer printer, Map<K, V> map) {
        final StringBuilder result = new StringBuilder(256);
        DebugUtils.dumpSummary(printer, result, 130, " Dumping %s [ count = %d, size = %d, maxSize = %d ] ", getClass().getSimpleName(), map.size(), size(), maxSize());
        for (Entry<?, ?> entry : map.entrySet()) {
            result.setLength(0);
            printer.println(result.append("  ").append(entry.getKey()).append(" ==> ").append(entry.getValue()).toString());
        }
    }
}
