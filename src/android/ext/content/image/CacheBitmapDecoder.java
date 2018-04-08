package android.ext.content.image;

import android.content.Context;
import android.ext.util.Caches.BitmapPool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

/**
 * Class CacheBitmapDecoder
 * @author Garfield
 * @version 1.0
 */
public class CacheBitmapDecoder<Params> extends BitmapDecoder<Params> {
    /**
     * The {@link BitmapPool} to reuse the bitmap when the bitmap decode.
     */
    protected final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when the bitmap decode.
     */
    public CacheBitmapDecoder(Context context, Parameters parameters, int maxPoolSize, BitmapPool bitmapPool) {
        super(context, parameters, maxPoolSize);
        mBitmapPool = bitmapPool;
    }

    @Override
    protected Bitmap decodeImage(Object uri, Params[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        opts.inPreferredConfig = mParameters.config;
        mParameters.computeSampleSize(mContext, uri, opts);

        // Decodes the bitmap pixels.
        return decodeBitmap(uri, params, flags, opts, mBitmapPool);
    }
}
