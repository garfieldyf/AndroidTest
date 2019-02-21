package android.ext.content.image;

import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.content.image.params.Parameters;
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
     * @see #BitmapDecoder(Context, Parameters)
     */
    public BitmapDecoder(Context context, int id) {
        super(context);
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @see #BitmapDecoder(Context, int)
     */
    public BitmapDecoder(Context context, Parameters parameters) {
        super(context);
        mParameters = parameters;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BitmapDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The decoder to set.
     * @param parameters The {@link Parameters} to decode bitmap.
     */
    public BitmapDecoder(AbsImageDecoder<Bitmap> decoder, Parameters parameters) {
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
        mParameters.dump(printer, "  ");
    }

    @Override
    protected Bitmap decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        return decodeBitmap(uri, getParameters(params, flags), opts);
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
