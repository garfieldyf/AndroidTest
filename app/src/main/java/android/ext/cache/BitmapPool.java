package android.ext.cache;

import android.ext.image.params.Parameters;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.os.Build;

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
     * Retrieves the <tt>Bitmap</tt> from this pool.
     * @param parameters The decode parameters, passed earlier by <tt>decodeImage</tt>.
     * @param opts The {@link Options}, passed earlier by <tt>decodeImage</tt>.
     * @return The <tt>Bitmap</tt> or <tt>null</tt> if there is no match the bitmap.
     */
    default Bitmap get(Parameters parameters, Options opts) {
        return (Build.VERSION.SDK_INT >= 26 && opts.inPreferredConfig == Config.HARDWARE ? null : get(parameters.computeByteCount(opts)));
    }
}
