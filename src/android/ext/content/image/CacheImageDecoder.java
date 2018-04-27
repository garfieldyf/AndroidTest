package android.ext.content.image;

import android.content.Context;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.util.Caches.BitmapPool;
import android.graphics.BitmapFactory.Options;

/**
 * Class CacheImageDecoder
 * @author Garfield
 * @version 1.0
 */
public class CacheImageDecoder extends ImageDecoder {
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
    public CacheImageDecoder(Context context, Parameters parameters, int maxPoolSize, BitmapPool bitmapPool) {
        super(context, parameters, maxPoolSize);
        mBitmapPool = bitmapPool;
    }

    @Override
    protected Object decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        if (GIF_MIME_TYPE.equalsIgnoreCase(opts.outMimeType)) {
            // Decodes the gif image.
            return decodeGIFImage(uri, params, flags, opts);
        } else {
            // Computes the sample size.
            opts.inPreferredConfig = mParameters.config;
            mParameters.computeSampleSize(mContext, uri, opts);

            // Decodes the bitmap.
            return decodeBitmap(uri, params, flags, opts, mBitmapPool);
        }
    }
}
