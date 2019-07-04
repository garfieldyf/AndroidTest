package android.ext.image.decoder;

import static android.ext.image.ImageLoader.FLAG_CUSTOM_PARAMETERS;
import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.graphics.BitmapUtils;
import android.ext.image.params.Parameters;
import android.ext.util.DebugUtils;
import android.ext.util.Pools.Pool;
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
     * If set the image decoder will be dump the {@link Options} when
     * it will be decode image. <p>This flag can be used DEBUG mode.</p>
     */
    public static final int FLAG_DUMP_OPTIONS = 0x00100000;

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
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param optionsPool The <tt>Options</tt> {@link Pool} to decode bitmap.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool} to
     * reuse the bitmap when decoding bitmap.
     */
    public BitmapDecoder(Context context, Parameters parameters, Pool<Options> optionsPool, BitmapPool bitmapPool) {
        super(context, optionsPool);
        mBitmapPool = bitmapPool;
        mParameters = parameters;
        DebugUtils.__checkError(parameters == null, "parameters == null");
    }

    /**
     * Returns the {@link Parameters} associated with this decoder.
     * @return The <tt>Parameters</tt>.
     */
    public final Parameters getParameters() {
        return mParameters;
    }

    public void dump(Printer printer) {
        DebugUtils.dumpSummary(printer, new StringBuilder(120), 120, " Dumping Parameters ", (Object[])null);
        mParameters.dump(printer, "  ");
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Image decodeImage(Object uri, Object target, Object[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        final Parameters parameters = ((flags & FLAG_CUSTOM_PARAMETERS) != 0 ? (Parameters)params[0] : mParameters);
        DebugUtils.__checkError(parameters == null, "The custom Parameters must be not null");
        opts.inMutable = parameters.mutable;
        opts.inPreferredConfig = parameters.config;
        parameters.computeSampleSize(mContext, target, opts);

        // Retrieves the bitmap from bitmap pool to reuse it.
        if (mBitmapPool != null) {
            opts.inBitmap = mBitmapPool.get(parameters.computeByteCount(mContext, opts));
            DebugUtils.__checkDebug(opts.inBitmap != null, "BitmapDecoder", "opts.inBitmap = " + opts.inBitmap);
        }

        // Decodes the image pixels.
        return (Image)decodeBitmap(uri, flags, opts);
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the bitmap data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     */
    protected Bitmap decodeBitmap(Object uri, int flags, Options opts) throws Exception {
        Bitmap bitmap = null;
        try {
            DebugUtils.__checkError(opts.inBitmap != null && !opts.inBitmap.isMutable(), "Only mutable bitmap can be reused");
            BitmapDecoder.__checkDumpOptions(opts, flags);
            bitmap = BitmapUtils.decodeBitmap(mContext, uri, opts);
        } catch (IllegalArgumentException e) {
            // Decodes the bitmap again, If decode the bitmap into inBitmap failed.
            if (opts.inBitmap != null) {
                opts.inBitmap = null;
                Log.w(getClass().getName(), e.getMessage());
                bitmap = BitmapUtils.decodeBitmap(mContext, uri, opts);
            }
        }

        return bitmap;
    }

    private static void __checkDumpOptions(Options opts, int flags) {
        if ((flags & FLAG_DUMP_OPTIONS) != 0) {
            BitmapUtils.dumpOptions("BitmapDecoder", opts);
        }
    }
}
