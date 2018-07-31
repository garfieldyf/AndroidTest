package android.ext.content.image;

import android.content.Context;
import android.ext.cache.BitmapPool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

/**
 * Class CacheBitmapDecoder
 * @author Garfield
 * @version 1.0
 */
public class CacheBitmapDecoder extends BitmapDecoder {
    /**
     * The {@link BitmapPool} used to decode the bitmap.
     */
    protected final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when decoding bitmap.
     */
    public CacheBitmapDecoder(Context context, Parameters parameters, int maxPoolSize, BitmapPool bitmapPool) {
        super(context, parameters, maxPoolSize);
        mBitmapPool = bitmapPool;
    }

    @Override
    protected Bitmap decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        opts.inPreferredConfig = mParameters.config;
        mParameters.computeSampleSize(mContext, uri, opts);

        // Decodes the bitmap pixels.
        return decodeBitmap(uri, params, flags, opts, mBitmapPool);
    }

    @Override
    protected void decodeImageBounds(Object uri, Object[] params, int flags, Options opts) throws Exception {
        opts.inJustDecodeBounds = true;
        decodeBitmap(uri, params, flags, opts);
        opts.inJustDecodeBounds = false;
    }
}
