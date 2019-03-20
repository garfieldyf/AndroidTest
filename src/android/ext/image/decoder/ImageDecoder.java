package android.ext.image.decoder;

import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.graphics.GIFImage;
import android.ext.image.params.Parameters;
import android.graphics.BitmapFactory.Options;

/**
 * Class ImageDecoder
 * @author Garfield
 */
public class ImageDecoder extends BitmapDecoder<Object> {
    /**
     * The MIME type of the gif image.
     */
    public static final String GIF_MIME_TYPE = "image/gif";

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the {@link Parameters} to load.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool}
     * to reuse the bitmap when decoding bitmap.
     * @see #ImageDecoder(Context, Parameters, BitmapPool)
     */
    public ImageDecoder(Context context, int id, BitmapPool bitmapPool) {
        super(context, id, bitmapPool);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool}
     * to reuse the bitmap when decoding bitmap.
     * @see #ImageDecoder(Context, int, BitmapPool)
     */
    public ImageDecoder(Context context, Parameters parameters, BitmapPool bitmapPool) {
        super(context, parameters, bitmapPool);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>ImageDecoder</tt> to set.
     * @param parameters The {@link Parameters} to decode bitmap.
     */
    public ImageDecoder(ImageDecoder decoder, Parameters parameters) {
        super(decoder, parameters);
    }

    @Override
    protected Object decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        return (GIF_MIME_TYPE.equalsIgnoreCase(opts.outMimeType) ? GIFImage.decode(mContext, uri, opts.inTempStorage) : super.decodeImage(uri, params, flags, opts));
    }
}
