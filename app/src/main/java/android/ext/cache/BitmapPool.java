package android.ext.cache;

import android.graphics.Bitmap;

/**
 * An interface for a pool that allows users to reuse {@link Bitmap} objects.
 * @author Garfield
 */
public interface BitmapPool {
    /**
     * Removes all bitmaps from this pool, leaving it empty.
     */
    void clear();

    /**
     * Retrieves and removes the <tt>Bitmap</tt> from this pool whose
     * allocation bytes greater than or equal to the given the size.
     * @param size The bytes to match.
     * @return The <tt>Bitmap</tt> or <tt>null</tt> if there is no
     * match the bitmap.
     */
    Bitmap get(int size);

    /**
     * Sets the specified <em>bitmap</em> to this pool.
     * @param bitmap The <tt>Bitmap</tt>.
     */
    void put(Bitmap bitmap);
}
