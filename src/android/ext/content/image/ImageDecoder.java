package android.ext.content.image;

import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.content.image.BitmapDecoder.Parameters;
import android.ext.graphics.BitmapUtils;
import android.ext.graphics.GIFImage;
import android.ext.util.DebugUtils;
import android.graphics.BitmapFactory.Options;
import android.util.Printer;

/**
 * Class ImageDecoder
 * @author Garfield
 * @version 1.0
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

    @Override
    protected Object decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        if (GIF_MIME_TYPE.equalsIgnoreCase(opts.outMimeType)) {
            // Decodes the gif image.
            return GIFImage.decode(mContext, uri, opts.inTempStorage);
        } else {
            // Computes the sample size.
            opts.inPreferredConfig = mParameters.config;
            mParameters.computeSampleSize(mContext, opts);

            // Decodes the bitmap.
            return BitmapUtils.decodeBitmap(mContext, uri, opts);
        }
    }
}
