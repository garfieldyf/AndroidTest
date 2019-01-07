package android.ext.content.image;

import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.content.image.params.Parameters;
import android.ext.graphics.GIFImage;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Printer;

/**
 * Class ImageDecoder
 * @author Garfield
 */
public class ImageDecoder extends AbsImageDecoder<Object> {
    /**
     * The MIME type of the gif image.
     */
    public static final String GIF_MIME_TYPE = "image/gif";

    /**
     * The {@link Parameters} to decode bitmap.
     */
    protected final Parameters mParameters;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the {@link Parameters} to load.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @see #ImageDecoder(Context, Parameters, int)
     */
    public ImageDecoder(Context context, int id, int maxPoolSize) {
        super(context, maxPoolSize);
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @see #ImageDecoder(Context, int, int)
     */
    public ImageDecoder(Context context, Parameters parameters, int maxPoolSize) {
        super(context, maxPoolSize);
        mParameters = parameters;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>ImageDecoder</tt> to copy.
     * @param id The resource id of the {@link Parameters} to load.
     * @see #ImageDecoder(ImageDecoder, Parameters)
     */
    public ImageDecoder(ImageDecoder decoder, int id) {
        super(decoder);
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link ImageDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>ImageDecoder</tt> to copy.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @see #ImageDecoder(ImageDecoder, int)
     */
    public ImageDecoder(ImageDecoder decoder, Parameters parameters) {
        super(decoder);
        mParameters = parameters;
    }

    @Override
    public void dump(Printer printer) {
        super.dump(printer);
        DebugUtils.dumpSummary(printer, new StringBuilder(80), 80, " Dumping Parameters ", (Object[])null);
        printer.println("  " + mParameters.toString());
    }

    /**
     * Retrieves the bitmap from the internal bitmap cache to decode the bitmap.
     * @param uri The uri to decode.
     * @param params The parameters, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em>
     * <tt>inTempStorage</tt> and <tt>out...</tt> fields are set.
     * @return The {@link Bitmap}, or <tt>null</tt> if no bitmap cache.
     */
    protected Bitmap getCachedBitmap(Object uri, Object[] params, int flags, Options opts) {
        return null;
    }

    @Override
    protected Object decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        // Decodes the image bounds.
        decodeImageBounds(uri, flags, opts);

        if (GIF_MIME_TYPE.equalsIgnoreCase(opts.outMimeType)) {
            // Decodes the gif image.
            return GIFImage.decode(mContext, uri, opts.inTempStorage);
        } else {
            // Computes the sample size.
            opts.inPreferredConfig = mParameters.config;
            mParameters.computeSampleSize(mContext, opts);

            // Retrieves the bitmap from bitmap pool to reuse it.
            opts.inBitmap = getCachedBitmap(uri, params, flags, opts);
            return decodeBitmap(uri, params, flags, opts);
        }
    }
}
