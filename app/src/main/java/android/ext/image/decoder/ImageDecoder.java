package android.ext.image.decoder;

import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.graphics.GIFImage;
import android.ext.util.Pools.Pool;
import android.graphics.BitmapFactory.Options;

/**
 * Class <tt>ImageDecoder</tt> used to decode the image data to a <tt>Bitmap</tt> or a GIF image.
 * @author Garfield
 */
public final class ImageDecoder extends BitmapDecoder<Object> {
    /**
     * The MIME type of the GIF image.
     */
    public static final String GIF_MIME_TYPE = "image/gif";

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param optionsPool The <tt>Options</tt> {@link Pool} to decode image.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool} to reuse
     * the bitmap when decoding bitmap.
     */
    public ImageDecoder(Context context, Pool<Options> optionsPool, BitmapPool bitmapPool) {
        super(context, optionsPool, bitmapPool);
    }

    @Override
    protected Object decodeImage(Object uri, Object target, Object[] params, int flags, Options opts) throws Exception {
        return (GIF_MIME_TYPE.equalsIgnoreCase(opts.outMimeType) ? GIFImage.decode(mContext, uri, opts.inTempStorage) : super.decodeImage(uri, target, params, flags, opts));
    }
}
