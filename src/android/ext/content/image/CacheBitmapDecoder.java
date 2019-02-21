package android.ext.content.image;

import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.content.image.params.Parameters;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;

/**
 * Class CacheBitmapDecoder
 * @author Garfield
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
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when decoding bitmap.
     * @see #CacheBitmapDecoder(Context, Parameters, BitmapPool)
     */
    public CacheBitmapDecoder(Context context, int id, BitmapPool bitmapPool) {
        super(context, id);
        mBitmapPool = bitmapPool;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param bitmapPool The {@link BitmapPool} to reuse the bitmap when decoding bitmap.
     * @see #CacheBitmapDecoder(Context, int, BitmapPool)
     */
    public CacheBitmapDecoder(Context context, Parameters parameters, BitmapPool bitmapPool) {
        super(context, parameters);
        mBitmapPool = bitmapPool;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link CacheBitmapDecoder} from the specified <em>decoder</em>.
     * The returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The decoder to set.
     * @param parameters The {@link Parameters} to decode bitmap.
     */
    public CacheBitmapDecoder(CacheBitmapDecoder decoder, Parameters parameters) {
        super(decoder, parameters);
        mBitmapPool = decoder.mBitmapPool;
    }

    @Override
    /* package */ Bitmap getCachedBitmap(Parameters parameters, Options opts) {
        return mBitmapPool.get(parameters.computeByteCount(mContext, opts));
    }
}
