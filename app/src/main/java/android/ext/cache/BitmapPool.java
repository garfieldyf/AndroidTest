package android.ext.cache;

import android.graphics.Bitmap;

/**
 * An interface for a pool that allows users to reuse {@link Bitmap} objects.
 * @author Garfield
 */
public interface BitmapPool {
    /**
     * Retrieves the <tt>Bitmap</tt> in this pool that it's allocation
     * bytes greater than or equal to the given the <em>size</em>.
     * @param size The bytes to match.
     * @return The <tt>Bitmap</tt> or <tt>null</tt> if there is no match
     * the bitmap.
     */
    Bitmap get(int size);

    /**
     * Sets the specified <em>bitmap</em> to this pool.
     * @param bitmap The <tt>Bitmap</tt>.
     */
    void put(Bitmap bitmap);

    /**
     * Trim this pool to the appropriate level. Typically called on the
     * {@link android.content.ComponentCallbacks2#onTrimMemory(int)}.
     * @param level The integer represents a trim level as specified in
     * {@link android.content.ComponentCallbacks2}.
     */
    default void trimMemory(int level) {
    }
}
