package android.ext.cache;

import java.util.Map;

/**
 * A <tt>Cache</tt> is a data structure consisting of a set
 * of keys and values in which each key is mapped to a value.
 * @author Garfield
 */
public interface Cache<K, V> {
    /**
     * Removes all elements from this cache, leaving it empty.
     * @see #remove(K)
     */
    void clear();

    /**
     * Removes the value for the specified <em>key</em>.
     * @param key The key to remove.
     * @return The value mapped by <em>key</em>
     * or <tt>null</tt> if there was no mapping.
     * @see #clear()
     */
    V remove(K key);

    /**
     * Returns the value of the mapping with the specified <em>key</em>.
     * @param key The key to find.
     * @return The value or <tt>null</tt> if there was no mapping.
     * @see #put(K, V)
     */
    V get(K key);

    /**
     * Maps the specified <em>key</em> to the specified <tt>value</tt>.
     * @param key The key.
     * @param value The value.
     * @return The previous value mapped by <em>key</em>
     * or <tt>null</tt> if there was no mapping.
     * @see #get(K)
     */
    V put(K key, V value);

    /**
     * Returns a copy of the current contents of this cache.
     * @return A copy of this cache.
     */
    Map<K, V> snapshot();

    /**
     * Returns the {@link BitmapPool} associated with this cache.
     * @return The <tt>BitmapPool</tt> or <tt>null</tt> if no bitmap pool.
     */
    default BitmapPool getBitmapPool() {
        return null;
    }
}
