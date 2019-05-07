package android.ext.graphics;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import android.content.Context;
import android.ext.content.res.XmlResources;
import android.ext.image.params.Parameters;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.Pools.MatrixPool;
import android.ext.util.Pools.RectFPool;
import android.ext.util.UriUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

/**
 * Class BitmapUtils
 * @author Garfield
 */
public final class BitmapUtils {
    /**
     * Grays the given the <em>bitmap</em>.
     * <pre>Algorithm : R = G = B = R * 0.299 + G * 0.587 + B * 0.114</pre>
     * @param bitmap A mutable bitmap to gray, must be {@link Config#ARGB_8888} pixel format.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean grayBitmap(Bitmap bitmap);

    /**
     * Inverses the given the <em>bitmap</em>.
     * <pre>Algorithm : R = 255 - R, G = 255 - G, B = 255 - B</pre>
     * <p>Note that this method only supports {@link PixelFormat#OPAQUE}.</p>
     * @param bitmap A mutable bitmap to inverse, must be {@link Config#ARGB_8888} pixel format.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean inverseBitmap(Bitmap bitmap);

    /**
     * Blurs the given the <em>bitmap</em>.
     * @param bitmap A mutable bitmap to blur, must be {@link Config#ARGB_8888} pixel format.
     * @param radius The radius of the blur in pixels, must be > 1.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean blurBitmap(Bitmap bitmap, int radius);

    /**
     * Binarized the given the <em>bitmap</em>.
     * @param bitmap A mutable bitmap to binarized, must be {@link Config#ARGB_8888} pixel format.
     * @param gray Whether the <em>bitmap</em> is grey-scale bitmap.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean binaryBitmap(Bitmap bitmap, boolean gray);

    /**
     * Spreads the given the <em>bitmap</em>.
     * <pre>Algorithm : RGB[i, j] = RGB[i + rand() % spreadSize, j + rand() % spreadSize]</pre>
     * @param bitmap A mutable bitmap to spread, must be {@link Config#ARGB_8888} pixel format.
     * @param spreadSize The spread size in pixels, must be > 0.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean spreadBitmap(Bitmap bitmap, int spreadSize);

    /**
     * Mosaics the given the <em>bitmap</em>.
     * <pre>Algorithm :
     *              | RGB(i - 1, j - 1), RGB(i - 1, j), RGB(i - 1, j + 1) |
     *    RGB = sum(| RGB(i,     j - 1), RGB(i,     j), RGB(i,     j + 1) |) / 9
     *              | RGB(i + 1, j - 1), RGB(i + 1, j), RGB(i + 1, j + 1) |
     *
     *    RGB(i - 1, j - 1), RGB(i - 1, j), RGB(i - 1, j + 1) |
     *    RGB(i,     j - 1), RGB(i,     j), RGB(i,     j + 1) | = RGB
     *    RGB(i + 1, j - 1), RGB(i + 1, j), RGB(i + 1, j + 1) |</pre>
     * @param bitmap A mutable bitmap to mosaic, must be {@link Config#ARGB_8888} pixel format.
     * @param mosaicSize The mosaic size in pixels, must be > 1.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean mosaicBitmap(Bitmap bitmap, int mosaicSize);

    /**
     * Mirrors the given the <em>bitmap</em>.<pre>
     * Horizontal : swap(RGB[i, j], RGB[width - 1 - i, height - 1 - j])
     * Vertical   : swap(RGB[i, j], RGB[width * (height - i - 1), j])</pre>
     * @param bitmap A mutable bitmap to mirror, must be {@link Config#ARGB_8888} pixel format.
     * @param horizontal Whether to mirror horizontal.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean mirrorBitmap(Bitmap bitmap, boolean horizontal);

    /**
     * Blurs the given the <em>bitmap</em>.
     * @param context The <tt>Context</tt>.
     * @param bitmap A mutable bitmap to blur, must be {@link Config#ARGB_8888} pixel format.
     * @param radius The radius of the blur, Supported range 0 < radius <= 25.
     */
    public static void blurBitmap(Context context, Bitmap bitmap, float radius) {
        final RenderScript rs = RenderScript.create(context);
        final ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));

        try {
            blurBitmap(rs, blur, bitmap, radius);
        } finally {
            rs.destroy();
            blur.destroy();
        }
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * <h3>Accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android_asset ({@link #SCHEME_FILE})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param opts The {@link Options} to use for decoding.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @throws IOException if an error occurs while decode from <em>uri</em>.
     */
    public static Bitmap decodeBitmap(Context context, Object uri, Options opts) throws IOException {
        final InputStream is = UriUtils.openInputStream(context, uri);
        try {
            return BitmapFactory.decodeStream(is, null, opts);
        } finally {
            is.close();
        }
    }

    /**
     * Equivalent to calling <tt>decodeBitmap(context, UriUtils.getResourceUri(context, resId), null, null)</tt>.
     * @param context The <tt>Context</tt>.
     * @param resId The resource id of the image data.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeBitmap(Context, int, int)
     * @see #decodeBitmap(Context, Object, int)
     * @see #decodeBitmap(Context, Object, Parameters, byte[])
     */
    public static Bitmap decodeBitmap(Context context, int resId) {
        return decodeBitmap(context, UriUtils.getResourceUri(context, resId), null, null);
    }

    /**
     * Equivalent to calling <tt>decodeBitmap(context, UriUtils.getResourceUri(context, resId), id)</tt>.
     * @param context The <tt>Context</tt>.
     * @param resId The resource id of the image data.
     * @param id The xml resource id of the {@link Parameters}.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeBitmap(Context, int)
     * @see #decodeBitmap(Context, Object, int)
     * @see #decodeBitmap(Context, Object, Parameters, byte[])
     */
    public static Bitmap decodeBitmap(Context context, int resId, int id) {
        return decodeBitmap(context, UriUtils.getResourceUri(context, resId), XmlResources.loadParameters(context, id), null);
    }

    /**
     * Equivalent to calling <tt>decodeBitmap(context, uri, XmlResources.loadParameters(context, id), null)</tt>.
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param id The xml resource id of the {@link Parameters}.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeBitmap(Context, int)
     * @see #decodeBitmap(Context, int, int)
     * @see #decodeBitmap(Context, Object, Parameters, byte[])
     */
    public static Bitmap decodeBitmap(Context context, Object uri, int id) {
        return decodeBitmap(context, uri, XmlResources.loadParameters(context, id), null);
    }

    /**
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * <h3>Accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android_asset ({@link #SCHEME_FILE})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param parameters May be <tt>null</tt>. The {@link Parameters} to use for decoding.
     * @param tempStorage May be <tt>null</tt>. The temporary storage to use for decoding. Suggest 16K.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @see #decodeBitmap(Context, int)
     * @see #decodeBitmap(Context, int, int)
     * @see #decodeBitmap(Context, Object, int)
     */
    public static Bitmap decodeBitmap(Context context, Object uri, Parameters parameters, byte[] tempStorage) {
        DebugUtils.__checkError(uri == null, "uri == null");
        try {
            final Options opts = new Options();
            opts.inTempStorage = tempStorage;

            if (parameters != null) {
                // Decodes the bitmap bounds.
                opts.inJustDecodeBounds = true;
                decodeBitmap(context, uri, opts);
                opts.inJustDecodeBounds = false;

                // Computes the sample size.
                opts.inMutable = parameters.mutable;
                opts.inPreferredConfig = parameters.config;
                parameters.computeSampleSize(context, null, opts);
            }

            // Decodes the bitmap pixels.
            return decodeBitmap(context, uri, opts);
        } catch (Exception e) {
            Log.e(BitmapUtils.class.getName(), "Couldn't decode image from - '" + uri + "'\n" + e);
            return null;
        }
    }

    /**
     * Creates a mutable scaled <tt>Bitmap</tt> from given the <tt>Bitmap</tt>.
     * @param bitmap The source <tt>Bitmap</tt>.
     * @param sx The amount to scale in X.
     * @param sy The amount to scale in Y.
     * @param newConfig The bitmap config to create.
     * @param paint May be <tt>null</tt>. The paint used to draw the bitmap.
     * @return The new scaled <tt>Bitmap</tt> or the source <tt>Bitmap</tt>
     * if no scaling is required.
     */
    public static Bitmap createScaledBitmap(Bitmap bitmap, float sx, float sy, Config newConfig, Paint paint) {
        if (Float.compare(sx, +1.0f) != 0 || Float.compare(sy, +1.0f) != 0) {
            final Matrix matrix = MatrixPool.sInstance.obtain();
            matrix.setScale(sx, sy);
            bitmap = createBitmap(bitmap, matrix, newConfig, paint);
            MatrixPool.sInstance.recycle(matrix);
        }

        return bitmap;
    }

    /**
     * Equivalent to calling <tt>createRotateBitmap(bitmap, degrees, bitmap.getWidth() * 0.5f,
     * bitmap.getHeight() * 0.5f, newConfig, paint)</tt>.
     * @param bitmap The source bitmap.
     * @param degrees The rotation degrees.
     * @param newConfig The bitmap config to create.
     * @return The new rotated <tt>Bitmap</tt> or the source <tt>Bitmap</tt> if no rotating is required.
     * @param paint May be <tt>null</tt>. The paint used to draw the bitmap.
     * @see #createRotateBitmap(Bitmap, float, float, float, Config, Paint)
     */
    public static Bitmap createRotateBitmap(Bitmap bitmap, float degrees, Config newConfig, Paint paint) {
        return createRotateBitmap(bitmap, degrees, bitmap.getWidth() * 0.5f, bitmap.getHeight() * 0.5f, newConfig, paint);
    }

    /**
     * Creates a mutable rotation <tt>Bitmap</tt> from given the <em>bitmap</em>.
     * @param bitmap The source bitmap.
     * @param degrees The rotation degrees.
     * @param px The x-coord for the pivot point.
     * @param py The y-coord for the pivot point.
     * @param newConfig The bitmap config to create.
     * @param paint May be <tt>null</tt>. The paint used to draw the bitmap.
     * @return The new rotated <tt>Bitmap</tt> or the source <tt>Bitmap</tt>
     * if no rotating is required.
     * @see #createRotateBitmap(Bitmap, float, Config, Paint)
     */
    public static Bitmap createRotateBitmap(Bitmap bitmap, float degrees, float px, float py, Config newConfig, Paint paint) {
        if (Float.compare(degrees, +0.0f) != 0) {
            final Matrix matrix = MatrixPool.sInstance.obtain();
            matrix.setRotate(degrees, px, py);
            bitmap = createBitmap(bitmap, matrix, newConfig, paint);
            MatrixPool.sInstance.recycle(matrix);
        }

        return bitmap;
    }

    /**
     * Creates a mutable inverted <tt>Bitmap</tt> from given the <em>source</em>.
     * @param source The source's contents to be drawn, Pass a {@link View} or {@link Bitmap} object.
     * @param width The horizontal size of the <em>source</em>.
     * @param height The vertical size of the <em>source</em>.
     * @param alpha The alpha component [0..255] of the inverted bitmap.
     * @param percent The percentage, expressed as a percentage of the <em>source's</em> width or height.
     * @param direction The direction. One of {@link Gravity#LFET}, {@link Gravity#TOP}, {@link Gravity#RIGHT}
     * or {@link Gravity#BOTTOM}.
     * @param paint May be <tt>null</tt>. The paint used to draw the bitmap.
     * @return An inverted bitmap.
     */
    public static Bitmap createInvertedBitmap(Object source, int width, int height, int alpha, float percent, int direction, Paint paint) {
        DebugUtils.__checkError(source == null, "source == null");
        DebugUtils.__checkError(width <= 0 || height <= 0, "width <= 0 || height <= 0");
        DebugUtils.__checkError(!(source instanceof Bitmap || source instanceof View), "Invalid source - " + source.getClass().getName());

        final float origWidth = width, origHeight = height;
        if (direction == Gravity.LEFT || direction == Gravity.RIGHT) {
            width  = (int)(width * percent + 0.5f);
        } else {
            height = (int)(height * percent + 0.5f);
        }

        final Bitmap result = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        final Canvas canvas = new Canvas(result);
        DrawUtils.drawInvertedBitmap(canvas, source, origWidth, origHeight, alpha, percent, direction, (paint != null ? paint : new Paint(Paint.DITHER_FLAG | Paint.FILTER_BITMAP_FLAG)));
        canvas.setBitmap(null);

        return result;
    }

    /**
     * Returns the bytes usage per pixel of a bitmap based on its configuration.
     * @param config The bitmap {@link Config}.
     * @return The bytes usage per pixel.
     */
    public static int getBytesPerPixel(Config config) {
        switch (config) {
        case ALPHA_8:
            return 1;

        case RGB_565:
        case ARGB_4444:
            return 2;

        case ARGB_8888:
            return 4;

        default:
            throw new IllegalArgumentException("Unknown the bitmap config - " + config);
        }
    }

    /**
     * Computes a sample size which makes the longer side at least
     * <em>desiredWidth</em> or <em>desiredHeight</em> long.
     * @param width The original width.
     * @param height The original height.
     * @param desiredWidth The desired width.
     * @param desiredHeight The desired height.
     * @return The sample size rounded down to the nearest power of 2.
     */
    public static int computeSampleSize(int width, int height, int desiredWidth, int desiredHeight) {
        final int sampleSize = Math.max(width / desiredWidth, height / desiredHeight);
        return (sampleSize <= 1 ? 1 : (sampleSize <= 8 ? Integer.highestOneBit(sampleSize) : (sampleSize / 8 * 8)));
    }

    public static void dumpOptions(String tag, Options opts) {
        if (opts == null) {
            Log.d(tag, "The opts is null");
        } else {
            final StringBuilder result = new StringBuilder(opts.toString()).append("\n{")
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
               .append("\n  inScreenDensity = ").append(opts.inScreenDensity);
            Log.d(tag, result.append("\n}").toString());
        }
    }

    public static void dumpBitmap(Context context, String tag, Bitmap bitmap) {
        if (bitmap == null) {
            Log.d(tag, "The bitmap is null");
        } else {
            Log.d(tag, dumpBitmap(context, new StringBuilder(288).append(bitmap), bitmap).toString());
        }
    }

    public static StringBuilder dumpBitmap(Context context, StringBuilder result, Bitmap bitmap) {
        final int size = bitmap.getByteCount();
        final int density = bitmap.getDensity();
        final Config config = bitmap.getConfig();
        final int allocSize = bitmap.getAllocationByteCount();
        final int targetDensity = context.getResources().getDisplayMetrics().densityDpi;

        result.append(" { width = ").append(bitmap.getWidth())
              .append(", height = ").append(bitmap.getHeight())
              .append(", scaledWidth = ").append(bitmap.getScaledWidth(targetDensity))
              .append(", scaledHeight = ").append(bitmap.getScaledHeight(targetDensity))
              .append(", config = ").append(config != null ? config.name() : "UNKNOWN")
              .append(", density = ").append(density).append('(').append(DeviceUtils.toDensity(density)).append(')')
              .append(", targetDensity = ").append(targetDensity).append('(').append(DeviceUtils.toDensity(targetDensity)).append(')')
              .append(", size = ").append(size).append('(').append(Formatter.formatFileSize(context, size)).append(')')
              .append(", allocSize = ").append(allocSize).append('(').append(Formatter.formatFileSize(context, allocSize)).append(')')
              .append(", mutable = ").append(bitmap.isMutable())
              .append(", recycle = ").append(bitmap.isRecycled())
              .append(" }");

        return result;
    }

    /* package */ static void blurBitmap(RenderScript rs, ScriptIntrinsicBlur blur, Bitmap bitmap, float radius) {
        DebugUtils.__checkError(bitmap == null, "bitmap == null");
        DebugUtils.__checkError(bitmap.getConfig() != Config.ARGB_8888, "The bitmap must be ARGB_8888 pixel format.");
        final Allocation input  = Allocation.createFromBitmap(rs, bitmap);
        final Allocation output = Allocation.createTyped(rs, input.getType());

        try {
            blur.setInput(input);
            blur.setRadius(radius);
            blur.forEach(output);
            output.copyTo(bitmap);
        } finally {
            input.destroy();
            output.destroy();
        }
    }

    private static Bitmap createBitmap(Bitmap source, Matrix matrix, Config config, Paint paint) {
        final RectF src = new RectF(0, 0, source.getWidth(), source.getHeight());
        final RectF dst = RectFPool.sInstance.obtain();
        matrix.mapRect(dst, src);

        final Bitmap bitmap = Bitmap.createBitmap((int)(dst.width() + 0.5f), (int)(dst.height() + 0.5), config);
        bitmap.setHasAlpha(source.hasAlpha());
        bitmap.setDensity(source.getDensity());
        bitmap.setPremultiplied(source.isPremultiplied());

        final Canvas canvas = new Canvas(bitmap);
        canvas.translate(-dst.left, -dst.top);
        canvas.concat(matrix);
        canvas.drawBitmap(source, null, src, paint);
        canvas.setBitmap(null);
        RectFPool.sInstance.recycle(dst);

        return bitmap;
    }

    /**
     * Class <tt>RenderScriptBlur</tt> used to blur the <tt>Bitmap</tt>.
     */
    public static final class RenderScriptBlur implements Closeable {
        private RenderScript mRS;
        private final ScriptIntrinsicBlur mBlur;

        /**
         * Constructor
         * @param context The <tt>Context</tt>.
         */
        public RenderScriptBlur(Context context) {
            mRS = RenderScript.create(context);
            mBlur = ScriptIntrinsicBlur.create(mRS, Element.U8_4(mRS));
        }

        @Override
        public synchronized final void close() {
            if (mRS != null) {
                mBlur.destroy();
                mRS.destroy();
                mRS = null;
            }
        }

        /**
         * Blurs the given the <em>bitmap</em>.
         * @param bitmap The bitmap to blur, must be {@link Config#ARGB_8888} pixel format.
         * @param radius The radius of the blur, Supported range <tt>0 &lt; radius &lt;= 25</tt>.
         */
        public synchronized final void blur(Bitmap bitmap, float radius) {
            if (mRS != null) {
                blurBitmap(mRS, mBlur, bitmap, radius);
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private BitmapUtils() {
    }
}
