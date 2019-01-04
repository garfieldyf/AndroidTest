package android.ext.content.image;

import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.content.image.params.Parameters;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Printer;

/**
 * Class BitmapDecoder
 * @author Garfield
 */
public class BitmapDecoder extends AbsImageDecoder<Bitmap> {
    /**
     * The {@link Parameters} to decode bitmap.
     */
    protected final Parameters mParameters;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the {@link Parameters} to load.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @see #BitmapDecoder(Context, Parameters, int)
     */
    public BitmapDecoder(Context context, int id, int maxPoolSize) {
        super(context, maxPoolSize);
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @see #BitmapDecoder(Context, int, int)
     */
    public BitmapDecoder(Context context, Parameters parameters, int maxPoolSize) {
        super(context, maxPoolSize);
        mParameters = parameters;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BitmapDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>BitmapDecoder</tt> to copy.
     * @param id The resource id of the {@link Parameters} to load.
     * @see #BitmapDecoder(BitmapDecoder, Parameters)
     */
    public BitmapDecoder(BitmapDecoder decoder, int id) {
        super(decoder);
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BitmapDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>BitmapDecoder</tt> to copy.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @see #BitmapDecoder(BitmapDecoder, int)
     */
    public BitmapDecoder(BitmapDecoder decoder, Parameters parameters) {
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
    protected Bitmap decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        opts.inPreferredConfig = mParameters.config;
        mParameters.computeSampleSize(mContext, opts);

        // Decodes the bitmap pixels.
        return BitmapUtils.decodeBitmap(mContext, uri, opts);
    }

    @Override
    protected void decodeImageBounds(Object uri, Object[] params, int flags, Options opts) throws Exception {
        if (mParameters.requestDecodeBounds()) {
            super.decodeImageBounds(uri, params, flags, opts);
        }
    }
}
