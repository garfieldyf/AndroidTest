package android.ext.cache;

/**
 * An interface for a <tt>Cache</tt> that has a {@link BitmapPool}.
 * @author Garfield
 */
public interface ImageCache<K, V> extends Cache<K, V> {
    /**
     * Returns the {@link BitmapPool} associated with this cache.
     * @return The <tt>BitmapPool</tt> or <tt>null</tt> if no bitmap pool.
     */
    BitmapPool getBitmapPool();
}
