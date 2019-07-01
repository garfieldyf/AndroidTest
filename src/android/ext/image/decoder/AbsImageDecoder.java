package android.ext.image.decoder;

import android.content.Context;
import android.ext.graphics.BitmapUtils;
import android.ext.image.ImageLoader;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.util.Log;

/**
 * Abstract class <tt>AbsImageDecoder</tt>
 * @author Garfield
 */
public abstract class AbsImageDecoder<Image> implements ImageLoader.ImageDecoder<Image> {
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
     * @param optionsPool The <tt>Options</tt> {@link Pool} to decode image.
     */
    public AbsImageDecoder(Context context, Pool<Options> optionsPool) {
        mOptionsPool = optionsPool;
        mContext = context.getApplicationContext();
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * <h3>Accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
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
     * @see #decodeImage(Object, Object, Object[], int, Options)
     */
    @Override
    public Image decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage) {
        final Options opts = mOptionsPool.obtain();
        try {
            // Decodes the image bounds.
            opts.inTempStorage = tempStorage;
            decodeImageBounds(uri, params, flags, opts);

            // Decodes the image pixels.
            return decodeImage(uri, target, params, flags, opts);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't decode image from - '" + uri + "'\n" + e);
            return null;
        } finally {
            recycleOptions(opts);
        }
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
        opts.inScaled  = true;
        opts.inMutable = false;
        opts.inSampleSize  = 0;
        opts.outMimeType   = null;
        opts.inTempStorage = null;
        opts.inTargetDensity = 0;
        opts.inScreenDensity = 0;
        opts.inPremultiplied = true;
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig  = Config.ARGB_8888;
        mOptionsPool.recycle(opts);
    }

    /**
     * Decodes an image bounds (width, height and MIME type) from the specified <em>uri</em>.
     * @param uri The uri to decode.
     * @param params The parameters, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} to store the <tt>out...</tt> fields.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     */
    protected void decodeImageBounds(Object uri, Object[] params, int flags, Options opts) throws Exception {
        opts.inJustDecodeBounds = true;
        BitmapUtils.decodeBitmap(mContext, uri, opts);
        opts.inJustDecodeBounds = false;
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param target The target, passed earlier by {@link #decodeImage}.
     * @param params The parameters, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     * @see #decodeImage(Object, Object, Object[], int, byte[])
     */
    protected abstract Image decodeImage(Object uri, Object target, Object[] params, int flags, Options opts) throws Exception;
}
