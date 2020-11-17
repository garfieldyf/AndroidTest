package android.ext.cache;

import android.annotation.UiThread;
import android.content.Context;
import android.util.Printer;

/**
 * A <tt>Cache</tt> is a data structure consisting of a set
 * of keys and values in which each key is mapped to a value.
 * @author Garfield
 */
public interface Cache<K, V> {
    /**
     * Removes all elements from this cache, leaving it empty.
     */
    void clear();

    /**
     * Removes the value for the specified <em>key</em>.
     * @param key The key to remove.
     * @return The value mapped by <em>key</em>
     * or <tt>null</tt> if there was no mapping.
     */
    V remove(K key);

    /**
     * Returns the value of the mapping with the specified <em>key</em>.
     * @param key The key to find.
     * @return The value or <tt>null</tt> if there was no mapping.
     */
    V get(K key);

    /**
     * Maps the specified <em>key</em> to the specified <tt>value</tt>.
     * @param key The key.
     * @param value The value.
     * @return The previous value mapped by <em>key</em>
     * or <tt>null</tt> if there was no mapping.
     */
    V put(K key, V value);

    /**
     * Trim this cache to the appropriate level. Typically called on the
     * {@link android.content.ComponentCallbacks2#onTrimMemory(int)}.
     * @param level The integer represents a trim level as specified in
     * {@link android.content.ComponentCallbacks2}.
     */
    @UiThread
    default void trimMemory(int level) {
    }

    public static void dumpCache(Context context, Printer printer, Cache<?, ?> cache) {
        if (cache instanceof ArrayMapCache) {
            ((ArrayMapCache<?, ?>)cache).dump(printer);
        } else if (cache instanceof LruFileCache) {
            ((LruFileCache)cache).dump(printer);
        } else if (cache instanceof SimpleLruCache) {
            ((SimpleLruCache<?, ?>)cache).dump(context, printer);
        } else if (cache instanceof LruImageCache) {
            ((LruImageCache<?>)cache).dump(context, printer);
        }
    }
}
