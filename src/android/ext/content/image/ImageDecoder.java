package android.ext.content.image;

import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.content.image.params.Parameters;
import android.ext.graphics.GIFImage;
import android.ext.util.DebugUtils;
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
     * @param decoder The decoder to set.
     * @param parameters The {@link Parameters} to decode bitmap.
     */
    public ImageDecoder(AbsImageDecoder<Object> decoder, Parameters parameters) {
        super(decoder);
        mParameters = parameters;
    }

    /**
     * Returns the {@link Parameters} associated with this decoder.
     * @return The <tt>Parameters</tt>.
     */
    public final Parameters getParameters() {
        return mParameters;
    }

    @Override
    public void dump(Printer printer) {
        super.dump(printer);
        DebugUtils.dumpSummary(printer, new StringBuilder(80), 80, " Dumping Parameters ", (Object[])null);
        printer.println("  " + mParameters.toString());
    }

    @Override
    protected Object decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        return (GIF_MIME_TYPE.equalsIgnoreCase(opts.outMimeType) ? GIFImage.decode(mContext, uri, opts.inTempStorage) : decodeBitmap(uri, getParameters(params, flags), opts));
    }

    /**
     * Returns the {@link Parameters} to decode bitmap.
     * @param params The parameters, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @return The <tt>Parameters</tt> to decode.
     */
    private Parameters getParameters(Object[] params, int flags) {
        return ((flags & ImageLoader.FLAG_CUSTOM_PARAMETERS) != 0 ? (Parameters)params[0] : mParameters);
    }
}
