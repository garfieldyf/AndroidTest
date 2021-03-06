package android.ext.image;

import android.content.Context;
import android.ext.image.ImageLoader.ImageDecoder;
import android.ext.util.DebugUtils;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
import android.support.annotation.WorkerThread;
import android.util.Log;
import java.io.InputStream;

/**
 * Abstract class <tt>AbsImageDecoder</tt>
 * @author Garfield
 */
public abstract class AbsImageDecoder<Image> implements ImageDecoder<Image> {
    /**
     * The {@link ImageModule}.
     */
    protected final ImageModule mModule;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     */
    public AbsImageDecoder(ImageModule module) {
        mModule = module;
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
     * @return The image, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeImage(Object, Object, Object[], int, Options)
     * @see UriUtils#openInputStream(Context, Object)
     */
    @Override
    @WorkerThread
    public Image decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage) {
        final Options opts = mModule.mOptionsPool.obtain();
        try {
            final Config config = ImageLoader.parseConfig(flags);
            opts.inTempStorage  = tempStorage;
            opts.inPreferredConfig = config;

            // Bitmaps with Config.HARWARE are always immutable
            opts.inMutable = (Build.VERSION.SDK_INT < 26 || config != Config.HARDWARE);

            // Decodes the image bounds.
            opts.inJustDecodeBounds = true;
            decodeBitmap(uri, opts);
            opts.inJustDecodeBounds = false;

            // Decodes the image pixels.
            DebugUtils.__checkLogError(opts.outWidth <= 0, "AbsImageDecoder", "decodeImage failed - outWidth = " + opts.outWidth + ", uri = " + uri);
            return (opts.outWidth > 0 ? decodeImage(uri, target, params, flags, opts) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't decode image from - " + uri + "\n" + e);
            return null;
        } finally {
            recycleOptions(opts);
        }
    }

    @SuppressWarnings("deprecation")
    private void recycleOptions(Options opts) {
        opts.inBitmap  = null;
        opts.inDensity = 0;
        opts.outWidth  = 0;
        opts.outHeight = 0;
        opts.inScaled  = true;
        opts.inDither  = false;
        opts.inMutable = false;
        opts.inSampleSize  = 0;
        opts.outMimeType   = null;
        opts.inTempStorage = null;
        opts.inTargetDensity = 0;
        opts.inScreenDensity = 0;
        opts.inPremultiplied = true;
        opts.inJustDecodeBounds = false;
        opts.inPreferredConfig  = Config.ARGB_8888;

        if (Build.VERSION.SDK_INT >= 26) {
            opts.outConfig = null;
            opts.outColorSpace = null;
            opts.inPreferredColorSpace = null;
        }

        mModule.mOptionsPool.recycle(opts);
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param opts May be <tt>null</tt>. The {@link Options} to use for decoding.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     */
    @WorkerThread
    protected Bitmap decodeBitmap(Object uri, Options opts) throws Exception {
        try (final InputStream is = UriUtils.openInputStream(mModule.mContext, uri)) {
            return BitmapFactory.decodeStream(is, null, opts);
        }
    }

    /**
     * Decodes an image from the specified <em>uri</em>.
     * @param uri The uri to decode, passed earlier by {@link #decodeImage}.
     * @param target The target, passed earlier by {@link #decodeImage}.
     * @param params The parameters, passed earlier by {@link #decodeImage}.
     * @param flags The flags, passed earlier by {@link #decodeImage}.
     * @param opts The {@link Options} used to decode. The <em>opts's</em>
     * <tt>out...</tt> fields are set.
     * @return The image, or <tt>null</tt> if the image data cannot be decode.
     * @throws Exception if an error occurs while decode from <em>uri</em>.
     * @see #decodeImage(Object, Object, Object[], int, byte[])
     */
    @WorkerThread
    protected abstract Image decodeImage(Object uri, Object target, Object[] params, int flags, Options opts) throws Exception;
}
