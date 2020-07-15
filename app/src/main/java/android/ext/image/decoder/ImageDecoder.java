package android.ext.image.decoder;

import android.ext.cache.BitmapPool;
import android.ext.graphics.GIFImage;
import android.ext.image.ImageModule;
import android.ext.image.params.Parameters;
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
     * @param module The {@link ImageModule}.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool}
     * to reuse the bitmap when decoding bitmap.
     */
    public ImageDecoder(ImageModule<?, ?> module, BitmapPool bitmapPool) {
        super(module, bitmapPool);
    }

    @Override
    protected Object decodeImage(Object uri, Object target, Parameters parameters, int flags, Options opts) throws Exception {
        return (GIF_MIME_TYPE.equalsIgnoreCase(opts.outMimeType) ? GIFImage.decode(mModule.mContext, uri, opts.inTempStorage) : super.decodeImage(uri, target, parameters, flags, opts));
    }
}
