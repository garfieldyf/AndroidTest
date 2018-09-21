package android.ext.content.image;

import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.graphics.BitmapUtils;
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
     * @param id The resource id of the {@link Parameters} to load.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when decoding bitmap.
     * @see #CacheBitmapDecoder(Context, Parameters, int, BitmapPool)
     */
    public CacheBitmapDecoder(Context context, int id, int maxPoolSize, BitmapPool bitmapPool) {
        super(context, id, maxPoolSize);
        mBitmapPool = bitmapPool;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when decoding bitmap.
     * @see #CacheBitmapDecoder(Context, int, int, BitmapPool)
     */
    public CacheBitmapDecoder(Context context, Parameters parameters, int maxPoolSize, BitmapPool bitmapPool) {
        super(context, parameters, maxPoolSize);
        mBitmapPool = bitmapPool;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link CacheBitmapDecoder} from the specified <em>decoder</em>.
     * The returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>CacheBitmapDecoder</tt> to copy.
     * @param id The resource id of the {@link Parameters} to load.
     * @see #CacheBitmapDecoder(CacheBitmapDecoder, Parameters)
     */
    public CacheBitmapDecoder(CacheBitmapDecoder decoder, int id) {
        super(decoder, id);
        mBitmapPool = decoder.mBitmapPool;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link CacheBitmapDecoder} from the specified <em>decoder</em>.
     * The returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>CacheBitmapDecoder</tt> to copy.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @see #CacheBitmapDecoder(CacheBitmapDecoder, int)
     */
    public CacheBitmapDecoder(CacheBitmapDecoder decoder, Parameters parameters) {
        super(decoder, parameters);
        mBitmapPool = decoder.mBitmapPool;
    }

    @Override
    protected Bitmap decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        opts.inPreferredConfig = mParameters.config;
        mParameters.computeSampleSize(mContext, opts);

        // Retrieves the bitmap from bitmap pool to reuse it.
        opts.inBitmap = mBitmapPool.get(mParameters.computeByteCount(mContext, opts));
        return decodeBitmap(uri, params, flags, opts);
    }

    @Override
    protected void decodeImageBounds(Object uri, Object[] params, int flags, Options opts) throws Exception {
        opts.inJustDecodeBounds = true;
        BitmapUtils.decodeBitmap(mContext, uri, opts);
        opts.inJustDecodeBounds = false;
    }
}
