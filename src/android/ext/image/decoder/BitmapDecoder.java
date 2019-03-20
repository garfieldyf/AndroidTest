package android.ext.image.decoder;

import static android.ext.image.ImageLoader.FLAG_CUSTOM_PARAMETERS;
import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.content.XmlResources;
import android.ext.graphics.BitmapUtils;
import android.ext.image.params.Parameters;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import android.util.Printer;

/**
 * Class <tt>BitmapDecoder</tt> used to decode the image data to a {@link Bitmap}.
 * @param <Image> Must be <tt>Bitmap</tt> or <tt>Object</tt> that will be decode
 * the result type.
 * @author Garfield
 */
public class BitmapDecoder<Image> extends AbsImageDecoder<Image> {
    /**
     * The {@link Parameters} to decode bitmap.
     */
    protected final Parameters mParameters;

    /**
     * The {@link BitmapPool} used to decode the bitmap.
     */
    protected final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param id The resource id of the {@link Parameters} to load.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool}
     * to reuse the bitmap when decoding bitmap.
     * @see #BitmapDecoder(BitmapDecoder, Parameters)
     * @see #BitmapDecoder(Context, Parameters, BitmapPool)
     */
    public BitmapDecoder(Context context, int id, BitmapPool bitmapPool) {
        super(context);
        mBitmapPool = bitmapPool;
        mParameters = XmlResources.loadParameters(mContext, id);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool}
     * to reuse the bitmap when decoding bitmap.
     * @see #BitmapDecoder(Context, int, BitmapPool)
     * @see #BitmapDecoder(BitmapDecoder, Parameters)
     */
    public BitmapDecoder(Context context, Parameters parameters, BitmapPool bitmapPool) {
        super(context);
        mBitmapPool = bitmapPool;
        mParameters = parameters;
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BitmapDecoder} from the specified <em>decoder</em>. The
     * returned decoder will be share the internal cache with the <em>decoder</em>.</p>
     * @param decoder The <tt>BitmapDecoder</tt> to copy.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @see #BitmapDecoder(Context, int, BitmapPool)
     * @see #BitmapDecoder(Context, Parameters, BitmapPool)
     */
    public BitmapDecoder(BitmapDecoder<Image> decoder, Parameters parameters) {
        super(decoder);
        mParameters = parameters;
        mBitmapPool = decoder.mBitmapPool;
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
    @SuppressWarnings("unchecked")
    protected Image decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        final Parameters parameters = ((flags & FLAG_CUSTOM_PARAMETERS) != 0 ? (Parameters)params[0] : mParameters);
        opts.inMutable = parameters.mutable;
        opts.inPreferredConfig = parameters.config;
        parameters.computeSampleSize(mContext, opts);

        // Decodes the image pixels.
        return (Image)decodeBitmap(uri, parameters, opts);
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param parameters The decode parameters, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em> <tt>out...</tt> fields are set.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the bitmap data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     */
    protected Bitmap decodeBitmap(Object uri, Parameters parameters, Options opts) throws Exception {
        // Retrieves the bitmap from bitmap pool to reuse it.
        opts.inBitmap = getCachedBitmap(parameters, opts);
        Bitmap bitmap = null;
        try {
            DebugUtils.__checkError(opts.inBitmap != null && !opts.inBitmap.isMutable(), "Only mutable bitmap can be reused");
            bitmap = BitmapUtils.decodeBitmap(mContext, uri, opts);
        } catch (IllegalArgumentException e) {
            // Decodes the bitmap again, If decode the bitmap into inBitmap failed.
            if (opts.inBitmap != null) {
                opts.inBitmap = null;
                Log.w(getClass().getName(), e);
                bitmap = BitmapUtils.decodeBitmap(mContext, uri, opts);
            }
        }

        return bitmap;
    }

    /**
     * Retrieves the bitmap from the internal bitmap cache to reuse it.
     * @param parameters The decode parameters, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
     * @return The {@link Bitmap}, or <tt>null</tt> if no bitmap cache.
     */
    private Bitmap getCachedBitmap(Parameters parameters, Options opts) {
        return (mBitmapPool != null ? mBitmapPool.get(parameters.computeByteCount(mContext, opts)) : null);
    }
}
