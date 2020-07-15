package android.ext.image;

import android.content.Context;
import android.ext.image.ImageLoader.ImageDecoder;
import android.ext.image.params.Parameters;
import android.ext.image.params.SizeParameters;
import android.ext.support.AppCompat;
import android.ext.util.DebugUtils;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.Build;
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
    protected final ImageModule<?, ?> mModule;

    /**
     * Constructor
     * @param module The {@link ImageModule}.
     */
    public AbsImageDecoder(ImageModule<?, ?> module) {
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
     * @return The image object, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeImage(Object, Object, Parameters, int, Options)
     * @see UriUtils#openInputStream(Context, Object)
     */
    @Override
    public Image decodeImage(Object uri, Object target, Object[] params, int flags, byte[] tempStorage) {
        final Options opts = mModule.mOptionsPool.obtain();
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
            AbsImageDecoder.__checkOptions(opts);
            decodeBitmap(uri, opts);
            opts.inJustDecodeBounds = false;

            // Decodes the image pixels.
            DebugUtils.__checkLogError(opts.outWidth <= 0, "AbsImageDecoder", "decodeImage failed - outWidth = " + opts.outWidth + ", uri = " + uri);
            return (opts.outWidth > 0 ? decodeImage(uri, target, parameters, flags, opts) : null);
        } catch (Exception e) {
            Log.e(getClass().getName(), "Couldn't decode image from - " + uri + "\n" + e);
            return null;
        } finally {
            AppCompat.clearForRecycle(opts);
            mModule.mOptionsPool.recycle(opts);
        }
    }

    private static void __checkOptions(Options opts) {
        if (Build.VERSION.SDK_INT >= 26 && opts.inMutable && opts.inPreferredConfig == Config.HARDWARE) {
            throw new AssertionError("Bitmaps with Config.HARWARE are always immutable");
        }
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
        try (final InputStream is = UriUtils.openInputStream(mModule.mContext, uri)) {
            return BitmapFactory.decodeStream(is, null, opts);
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
    protected abstract Image decodeImage(Object uri, Object target, Parameters parameters, int flags, Options opts) throws Exception;
}
