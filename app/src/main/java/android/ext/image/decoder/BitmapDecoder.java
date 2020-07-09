package android.ext.image.decoder;

import static android.ext.image.ImageLoader.FLAG_DUMP_OPTIONS;
import static android.ext.support.AppCompat.clearForRecycle;
import android.content.Context;
import android.ext.cache.BitmapPool;
import android.ext.cache.Caches;
import android.ext.image.ImageLoader;
import android.ext.image.ImageModule;
import android.ext.image.params.Parameters;
import android.ext.image.params.SizeParameters;
import android.ext.util.DebugUtils;
import android.ext.util.Pools.Pool;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.util.Log;
import java.io.InputStream;

/**
 * Class <tt>BitmapDecoder</tt> used to decode the image data to a {@link Bitmap}.
 * @param <Image> Must be <tt>Bitmap</tt> or <tt>Object</tt> that will be decode
 * the result type.
 * @author Garfield
 */
public class BitmapDecoder<Image> implements ImageLoader.ImageDecoder<Image> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The {@link BitmapPool} used to decode the bitmap.
     */
    protected final BitmapPool mBitmapPool;

    /**
     * The <tt>Options</tt> {@link Pool} to decode bitmap.
     */
    private final Pool<Options> mOptionsPool;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param optionsPool The <tt>Options</tt> {@link Pool} to decode bitmap.
     * @param bitmapPool May be <tt>null</tt>. The {@link BitmapPool} to reuse
     * the bitmap when decoding bitmap.
     */
    public BitmapDecoder(Context context, Pool<Options> optionsPool, BitmapPool bitmapPool) {
        mContext = context.getApplicationContext();
        mOptionsPool = optionsPool;
        mBitmapPool  = (bitmapPool != null ? bitmapPool : Caches.emptyBitmapPool());
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param uri The uri to decode.
     * @param target The target, passed earlier by {@link ImageLoader#load}.
     * @param params The parameters, passed earlier by {@link ImageLoader#load}.
     * @param flags The flags, passed earlier by {@link ImageLoader#load}.
     * @param tempStorage The temporary storage to use for decoding. Suggest 16K.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeImage(Object, Object, Parameters, int, Options)
     * @see UriUtils#openInputStream(Context, Object)
     */
    @Override
    public Image decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage) {
        final Options opts = mOptionsPool.obtain();
        try {
            Parameters parameters = ImageModule.getParameters(params);
            if (parameters == null) {
                parameters = SizeParameters.defaultParameters;
            }

            // Decodes the image bounds.
            opts.inTempStorage = tempStorage;
            opts.inMutable = parameters.isMutable();
            opts.inPreferredConfig  = parameters.config;
            opts.inJustDecodeBounds = true;
            decodeBitmap(uri, opts);
            opts.inJustDecodeBounds = false;

            // Decodes the image pixels.
            DebugUtils.__checkLogError(opts.outWidth <= 0, "BitmapDecoder", "decodeImage failed - outWidth = " + opts.outWidth + ", uri = " + uri);
            return (opts.outWidth > 0 ? decodeImage(uri, target, parameters, flags, opts) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't decode image from - " + uri + "\n" + e);
            return null;
        } finally {
            clearForRecycle(opts);
            mOptionsPool.recycle(opts);
        }
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param target The target, passed earlier by {@link #decodeImage}.
     * @param parameters The parameters, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     * @see #decodeImage(Object, Object, Object[], int, byte[])
     */
    @SuppressWarnings("unchecked")
    protected Image decodeImage(Object uri, Object target, Parameters parameters, int flags, Options opts) throws Exception {
        // Computes the sample size.
        parameters.computeSampleSize(target, opts);

        // Retrieves the bitmap from bitmap pool to reuse it.
        opts.inBitmap = mBitmapPool.get(parameters, opts);
        DebugUtils.__checkDebug(opts.inBitmap != null, "BitmapDecoder", "decodeBitmap will attempt to reuse the " + opts.inBitmap);

        // Decodes the image pixels.
        Bitmap result = null;
        try {
            DebugUtils.__checkError(opts.inBitmap != null && !opts.inBitmap.isMutable(), "Only mutable bitmap can be reused - " + opts.inBitmap);
            BitmapDecoder.__checkDumpOptions(opts, flags);
            result = decodeBitmap(uri, opts);
        } catch (IllegalArgumentException e) {
            // Decodes the bitmap again, If decode the bitmap into inBitmap failed.
            if (opts.inBitmap != null) {
                DebugUtils.__checkLogError(true, "BitmapDecoder", "decodeBitmap failed - " + e.getMessage());
                opts.inBitmap = null;
                result = decodeBitmap(uri, opts);
            }
        }

        BitmapDecoder.__checkBitmap(result, opts);
        return (Image)result;
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param opts May be <tt>null</tt>. The {@link Options} to use for decoding.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     */
    protected Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
        try (final InputStream is = UriUtils.openInputStream(mContext, uri)) {
            return BitmapFactory.decodeStream(is, null, opts);
        }
    }

    private static void __checkBitmap(Bitmap bitmap, Options opts) {
        if (bitmap != null && Build.VERSION.SDK_INT >= 26) {
            final Config config = bitmap.getConfig();
            if (config != null && opts.outConfig != null && config != opts.outConfig) {
                throw new AssertionError("The bitmap config = " + config + ", opts.outConfig = " + opts.outConfig + " are not equal.");
            }
        }
    }

    private static void __checkDumpOptions(Options opts, int flags) {
        if ((flags & FLAG_DUMP_OPTIONS) != 0) {
            final StringBuilder builder = new StringBuilder(opts.toString()).append("\n{")
                .append("\n  inSampleSize = ").append(opts.inSampleSize)
                .append("\n  inJustDecodeBounds = ").append(opts.inJustDecodeBounds)
                .append("\n  inPreferredConfig  = ").append(opts.inPreferredConfig)
                .append("\n  inMutable = ").append(opts.inMutable)
                .append("\n  inScaled  = ").append(opts.inScaled)
                .append("\n  inDensity = ").append(opts.inDensity)
                .append("\n  inTargetDensity = ").append(opts.inTargetDensity)
                .append("\n  inScreenDensity = ").append(opts.inScreenDensity)
                .append("\n  inBitmap  = ").append(opts.inBitmap)
                .append("\n  outWidth  = ").append(opts.outWidth)
                .append("\n  outHeight = ").append(opts.outHeight);

            if (Build.VERSION.SDK_INT >= 26) {
                builder.append("\n  outConfig = ").append(opts.outConfig)
                    .append("\n  outMimeType = ").append(opts.outMimeType)
                    .append("\n  outColorSpace = ").append(opts.outColorSpace)
                    .append("\n  inPreferredColorSpace = ").append(opts.inPreferredColorSpace);
            } else {
                builder.append("\n  outMimeType = ").append(opts.outMimeType);
            }

            Log.d("BitmapDecoder", builder.append("\n  inTempStorage = ").append(opts.inTempStorage)
               .append(opts.inTempStorage != null ? " { length = " + opts.inTempStorage.length + " }" : "")
               .append("\n}").toString());
        }
    }
}
