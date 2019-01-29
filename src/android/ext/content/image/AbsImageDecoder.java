package android.ext.content.image;

import static android.graphics.Bitmap.Config.ARGB_8888;
import android.content.Context;
import android.ext.content.image.params.Parameters;
import android.ext.graphics.BitmapUtils;
import android.ext.util.DebugUtils;
import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory.Options;
import android.util.Log;
import android.util.Printer;

/**
 * Abstract class AbsImageDecoder
 * @author Garfield
 */
public abstract class AbsImageDecoder<Image> implements ImageLoader.ImageDecoder<Image>, Factory<Options> {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * The <tt>Options</tt> {@link Pool} to decode image.
     */
    protected final Pool<Options> mOptionsPool;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param maxPoolSize The maximum number of {@link Options} in the internal pool.
     * @see #AbsImageDecoder(AbsImageDecoder)
     */
    public AbsImageDecoder(Context context, int maxPoolSize) {
        mContext = context.getApplicationContext();
        mOptionsPool = Pools.synchronizedPool(Pools.newPool(this, maxPoolSize));
    }

    /**
     * Copy constructor
     * @see #AbsImageDecoder(Context, int)
     */
    public AbsImageDecoder(AbsImageDecoder<Image> decoder) {
        mContext = decoder.mContext;
        mOptionsPool = decoder.mOptionsPool;
    }

    @Override
    public Options newInstance() {
        return new Options();
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * <h5>Accepts the following URI schemes:</h5>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android_asset ({@link #SCHEME_FILE})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param uri The uri to decode, passed earlier by {@link ImageLoader#load}.
     * @param params The parameters, passed earlier by {@link ImageLoader#load}.
     * @param flags The flags, passed earlier by {@link ImageLoader#load}.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeImage(Object, Object[], int, Options)
     */
    @Override
    public Image decodeImage(Object uri, Object[] params, int flags, byte[] tempStorage) {
        final Options opts = mOptionsPool.obtain();
        try {
            // Decodes the image bounds.
            opts.inTempStorage = tempStorage;
            opts.inJustDecodeBounds = true;
            BitmapUtils.decodeBitmap(mContext, uri, opts);
            opts.inJustDecodeBounds = false;

            // Decodes the image pixels.
            return decodeImage(uri, params, flags, opts);
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't decode image from - '").append(uri).append("'\n").append(e).toString());
            return null;
        } finally {
            recycleOptions(opts);
        }
    }

    public void dump(Printer printer) {
        Pools.dumpPool(mOptionsPool, printer);
    }

    /**
     * Recycles the specified <em>opts</em> to the internal pool.
     * @param opts The {@link Options} to recycle.
     */
    private void recycleOptions(Options opts) {
        opts.inBitmap  = null;
        opts.inDensity = 0;
        opts.outWidth  = 0;
        opts.outHeight = 0;
        opts.inMutable = false;

        opts.inDither  = false;
        opts.mCancel   = false;
        opts.inSampleSize  = 0;
        opts.outMimeType   = null;
        opts.inTempStorage = null;

        opts.inTargetDensity = 0;
        opts.inScreenDensity = 0;
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig  = ARGB_8888;
        opts.inPreferQualityOverSpeed = false;
        mOptionsPool.recycle(opts);
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>. If the <tt>opts.inBitmap</tt> is not
     * <tt>null</tt> this method will attempt to reuse the bitmap (decode in <tt>opts.inBitmap</tt>).
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the bitmap data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     */
    private Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
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
     * Retrieves the bitmap from the internal bitmap cache to reuse.
     * @param parameters The decode parameters, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em>
     * <tt>inTempStorage</tt> and <tt>out...</tt> fields are set.
     * @return The {@link Bitmap}, or <tt>null</tt> if no bitmap cache.
     */
    /* package */ Bitmap getCachedBitmap(Parameters parameters, Options opts) {
        return null;
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param parameters The {@link Parameters} to decode bitmap.
     * @param opts The {@link Options} to decode bitmap.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the bitmap data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     */
    /* package */ final Bitmap decodeBitmap(Object uri, Parameters parameters, Options opts) throws Exception {
        // Computes the sample size.
        opts.inMutable = parameters.mutable;
        opts.inPreferredConfig = parameters.config;
        parameters.computeSampleSize(mContext, opts);

        // Retrieves the bitmap from bitmap pool to reuse it.
        opts.inBitmap = getCachedBitmap(parameters, opts);
        return decodeBitmap(uri, opts);
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param params The parameters, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em>
     * <tt>inTempStorage</tt> and <tt>out...</tt> fields are set.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     * @see #decodeImage(Object, Object[], int, byte[])
     */
    protected abstract Image decodeImage(Object uri, Object[] params, int flags, Options opts) throws Exception;
}
