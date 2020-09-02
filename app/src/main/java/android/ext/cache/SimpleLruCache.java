package android.ext.cache;

import static android.content.ComponentCallbacks2.TRIM_MEMORY_BACKGROUND;
import static android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL;
import static android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN;
import android.content.Context;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.util.Printer;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * A <tt>SimpleLruCache</tt> that holds strong references to a limited number
 * of values. Each time a value is accessed, it is moved to the head of a queue.
 * When a value is added to a full cache, the value at the end of that queue is
 * evicted and may become eligible for garbage collection. Unlike {@link LruCache}
 * this class is <b>not</b> thread-safely.
 * @author Garfield
 */
public class SimpleLruCache<K, V> implements Cache<K, V> {
    /* package */ final int maxSize;
    /* package */ final Map<K, V> map;

    /**
     * Constructor
     * @param maxSize The maximum number of values to allow in this cache.
     */
    public SimpleLruCache(int maxSize) {
        DebugUtils.__checkError(maxSize <= 0, "Invalid parameter - maxSize(" + maxSize + ") must be > 0");
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<K, V>(0, 0.75f, true);
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
     * Returns the number of entries in this cache.
     * @return The number of entries in this cache.
     * @see #maxSize()
     */
    public int size() {
        return map.size();
    }

    /**
     * Clears this cache, but do not call {@link #entryRemoved} on each removed entry.
     */
    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public V get(K key) {
        DebugUtils.__checkError(key == null, "Invalid parameter - key == null");
        return map.get(key);
    }

    /**
     * Maps the specified <em>key</em> to the specified <tt>value</tt>.
     * The <tt>value</tt> is moved to the head of the queue.
     * @param key The key.
     * @param value The value.
     * @return The previous value mapped by <em>key</em> or <tt>null</tt>
     * if there was no mapping.
     */
    @Override
    public V put(K key, V value) {
        DebugUtils.__checkError(key == null || value == null, "Invalid parameters - key == null || value == null");
        final V previous = putImpl(key, value);
        if (previous != null) {
            entryRemoved(false, key, previous, value);
        }

        trimToSize(maxSize, false);
        return previous;
    }

    @Override
    public V remove(K key) {
        DebugUtils.__checkError(key == null, "Invalid parameter - key == null");
        final V previous = removeImpl(key);
        if (previous != null) {
            entryRemoved(false, key, previous, null);
        }

        return previous;
    }

    @Override
    public void trimMemory(int level) {
        if (level >= TRIM_MEMORY_BACKGROUND) {
            // Entering list of cached background apps, clear this cache.
            clear();
        } else if (level >= TRIM_MEMORY_UI_HIDDEN || level == TRIM_MEMORY_RUNNING_CRITICAL) {
            // The app's UI is no longer visible, or app is in the foreground but system is running critically low on memory.
            // Remove the oldest half of this cache.
            trimToSize(maxSize / 2, true);
        }
    }

    /**
     * Returns a copy of the current contents of this cache.
     * @return A copy of this cache.
     */
    public Map<K, V> snapshot() {
        return new LinkedHashMap<K, V>(map);
    }

    /**
     * Remove the eldest entries until the total of remaining entries is
     * at or below the requested size.
     * @param maxSize The maximum size of the cache. May be <tt>-1</tt>
     * to evict all entries.
     * @param evicted If <tt>true</tt> the entry is being removed to make
     * space, <tt>false</tt> otherwise.
     */
    protected void trimToSize(int maxSize, boolean evicted) {
        while (map.size() > maxSize && !map.isEmpty()) {
            final Entry<K, V> toEvict = map.entrySet().iterator().next();
            final K key = toEvict.getKey();
            final V value = toEvict.getValue();
            map.remove(key);
            entryRemoved(evicted, key, value, null);
        }
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

    public void dump(Context context, Printer printer) {
        final StringBuilder result = new StringBuilder(256);
        final Set<Entry<K, V>> entries = snapshot().entrySet();

        DeviceUtils.dumpSummary(printer, result, 130, " Dumping %s [ count = %d, size = %d, maxSize = %d ] ", getClass().getSimpleName(), entries.size(), size(), maxSize());
        for (Entry<?, ?> entry : entries) {
            result.setLength(0);
            printer.println(result.append("  ").append(entry.getKey()).append(" ==> ").append(entry.getValue()).toString());
        }
    }
}
