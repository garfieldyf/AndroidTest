package android.ext.content.image;

import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.content.image.params.Parameters;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

/**
 * Class CacheImageDecoder
 * @author Garfield
 */
public class CacheImageDecoder extends ImageDecoder {
    /**
     * The {@link BitmapPool} used to decode the bitmap.
     */
    protected final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the {@link Parameters} to load.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when decoding bitmap.
     * @see #CacheImageDecoder(Context, Parameters, int, BitmapPool)
     */
    public CacheImageDecoder(Context context, int id, int maxPoolSize, BitmapPool bitmapPool) {
        super(context, id, maxPoolSize);
        mBitmapPool = bitmapPool;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when decoding bitmap.
     * @see #CacheImageDecoder(Context, int, int, BitmapPool)
     */
    public CacheImageDecoder(Context context, Parameters parameters, int maxPoolSize, BitmapPool bitmapPool) {
        super(context, parameters, maxPoolSize);
        mBitmapPool = bitmapPool;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link CacheImageDecoder} from the specified <em>decoder</em>.
     * The returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>CacheImageDecoder</tt> to copy.
     * @param id The resource id of the {@link Parameters} to load.
     * @see #CacheImageDecoder(CacheImageDecoder, Parameters)
     */
    public CacheImageDecoder(CacheImageDecoder decoder, int id) {
        super(decoder, id);
        mBitmapPool = decoder.mBitmapPool;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link CacheImageDecoder} from the specified <em>decoder</em>.
     * The returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>CacheImageDecoder</tt> to copy.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @see #CacheImageDecoder(CacheImageDecoder, int)
     */
    public CacheImageDecoder(CacheImageDecoder decoder, Parameters parameters) {
        super(decoder, parameters);
        mBitmapPool = decoder.mBitmapPool;
    }

    @Override
    protected Bitmap getCachedBitmap(Object uri, Object[] params, int flags, Options opts) {
        return mBitmapPool.get(mParameters.computeByteCount(mContext, opts));
    }
}
