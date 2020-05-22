package android.ext.graphics;

import android.content.Context;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
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
import android.support.v4.graphics.BitmapCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import java.io.IOException;
import java.io.InputStream;

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
     * @param grayscale Whether the <em>bitmap</em> is gray-scale bitmap.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean binaryBitmap(Bitmap bitmap, boolean grayscale);

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
     * Decodes a {@link Bitmap} from the specified <em>uri</em>.
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to decode.
     * @param opts The {@link Options} to use for decoding.
     * @return The <tt>Bitmap</tt>, or <tt>null</tt> if the image data cannot be decode.
     * @throws IOException if an error occurs while decode from <em>uri</em>.
     * @see UriUtils#openInputStream(Context, Object)
     */
    public static Bitmap decodeBitmap(Context context, Object uri, Options opts) throws IOException {
        try (final InputStream is = UriUtils.openInputStream(context, uri)) {
            return BitmapFactory.decodeStream(is, null, opts);
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
     * @param direction The direction. One of {@link Gravity#LEFT}, {@link Gravity#TOP}, {@link Gravity#RIGHT}
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
        final int allocSize = BitmapCompat.getAllocationByteCount(bitmap);
        final int targetDensity = context.getResources().getDisplayMetrics().densityDpi;

        result.append(" { width = ").append(bitmap.getWidth())
              .append(", height = ").append(bitmap.getHeight())
              .append(", scaledWidth = ").append(bitmap.getScaledWidth(targetDensity))
              .append(", scaledHeight = ").append(bitmap.getScaledHeight(targetDensity))
              .append(", config = ").append(config != null ? config.name() : "UNKNOWN")
              .append(", density = ").append(density).append('(').append(DeviceUtils.toDensity(density)).append(')')
              .append(", targetDensity = ").append(targetDensity).append('(').append(DeviceUtils.toDensity(targetDensity)).append(')')
              .append(", size = ").append(size).append('(').append(FileUtils.formatFileSize(size)).append(')')
              .append(", allocSize = ").append(allocSize).append('(').append(FileUtils.formatFileSize(allocSize)).append(')')
              .append(", mutable = ").append(bitmap.isMutable())
              .append(", recycle = ").append(bitmap.isRecycled())
              .append(" }");

        return result;
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
     * This utility class cannot be instantiated.
     */
    private BitmapUtils() {
    }
}
