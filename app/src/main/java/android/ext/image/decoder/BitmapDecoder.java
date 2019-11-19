package android.ext.image.decoder;

import static android.ext.image.ImageLoader.FLAG_DUMP_OPTIONS;
import static android.ext.image.ImageLoader.LoadRequest.PARAMETERS_INDEX;
import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.graphics.BitmapUtils;
import android.ext.image.params.Parameters;
import android.ext.util.DebugUtils;
import android.ext.util.Optional;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

/**
 * Class <tt>BitmapDecoder</tt> used to decode the image data to a {@link Bitmap}.
 * @param <Image> Must be <tt>Bitmap</tt> or <tt>Object</tt> that will be decode
 * the result type.
 * @author Garfield
 */
public class BitmapDecoder<Image> extends AbsImageDecoder<Image> {
    /**
     * The {@link BitmapPool} used to decode the bitmap.
     */
    protected final BitmapPool mBitmapPool;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param optionsPool The <tt>Options</tt> {@link Pool} to decode bitmap.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool} to
     * reuse the bitmap when decoding bitmap.
     */
    public BitmapDecoder(Context context, Pool<Options> optionsPool, BitmapPool bitmapPool) {
        super(context, optionsPool);
        mBitmapPool = Optional.ofNullable(bitmapPool);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Image decodeImage(Object uri, Object target, Object[] params, int flags, Options opts) throws Exception {
        // Computes the sample size.
        final Parameters parameters = (Parameters)params[PARAMETERS_INDEX];
        DebugUtils.__checkError(parameters == null, "parameters == null");
        opts.inMutable = parameters.mutable;
        opts.inPreferredConfig = parameters.config;
        parameters.computeSampleSize(mContext, target, opts);

        // Retrieves the bitmap from bitmap pool to reuse it.
        opts.inBitmap = mBitmapPool.get(mContext, parameters, opts);
        DebugUtils.__checkDebug(opts.inBitmap != null, "BitmapDecoder", "decodeBitmap will attempt to reuse the " + opts.inBitmap);

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
            Log.d("BitmapDecoder", new StringBuilder(opts.toString()).append("\n{")
               .append("\n  inSampleSize = ").append(opts.inSampleSize)
               .append("\n  inJustDecodeBounds = ").append(opts.inJustDecodeBounds)
               .append("\n  inPreferredConfig  = ").append(opts.inPreferredConfig)
               .append("\n  inMutable = ").append(opts.inMutable)
               .append("\n  inDensity = ").append(opts.inDensity)
               .append("\n  inTargetDensity = ").append(opts.inTargetDensity)
               .append("\n  inBitmap  = ").append(opts.inBitmap)
               .append("\n  outWidth  = ").append(opts.outWidth)
               .append("\n  outHeight = ").append(opts.outHeight)
               .append("\n  outMimeType = ").append(opts.outMimeType)
               .append("\n  inTempStorage = ").append(opts.inTempStorage)
               .append("\n  inScaled = ").append(opts.inScaled)
               .append("\n  inPremultiplied = ").append(opts.inPremultiplied)
               .append("\n  inScreenDensity = ").append(opts.inScreenDensity)
               .append("\n}").toString());
        }
    }
}
