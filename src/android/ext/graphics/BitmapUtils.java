package android.ext.graphics;

import java.io.FileOutputStream;
import android.content.Context;
import android.ext.graphics.DrawUtils.MatrixPool;
import android.ext.graphics.DrawUtils.RectFPool;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.FileUtils;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Type;
import android.text.format.Formatter;
import android.util.Log;
import android.view.Gravity;
import android.view.View;

/**
 * Class BitmapUtils
 * @author Garfield
 * @version 2.0
 */
public final class BitmapUtils {
    /**
     * Grays the given the <em>bitmap</em>.
     * <pre>Algorithm : R = G = B = R * 0.30 + G + 0.59 + B * 0.11</pre>
     * @param bitmap The bitmap to gray, must be {@link Config#ARGB_8888} pixel format.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean grayBitmap(Bitmap bitmap);

    /**
     * Inverses the given the <em>bitmap</em>.
     * <pre>Algorithm : R = 255 - R, G = 255 - G, B = 255 - B</pre>
     * <p>Note that this method only supports {@link PixelFormat#OPAQUE}.</p>
     * @param bitmap The bitmap to inverse, must be {@link Config#ARGB_8888} pixel format.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean inverseBitmap(Bitmap bitmap);

    /**
     * Blurs the given the <em>bitmap</em>.
     * @param bitmap The bitmap to blur, must be {@link Config#ARGB_8888} pixel format.
     * @param radius The radius of the blur in pixels, must be > 1.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean blurBitmap(Bitmap bitmap, int radius);

    /**
     * Spreads the given the <em>bitmap</em>.
     * <pre>Algorithm : RGB[i, j] = RGB[i + rand() % spreadSize, j + rand() % spreadSize]</pre>
     * @param bitmap The bitmap to spread, must be {@link Config#ARGB_8888} pixel format.
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
     * @param bitmap The bitmap to mosaic, must be {@link Config#ARGB_8888} pixel format.
     * @param mosaicSize The mosaic size in pixels, must be > 1.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean mosaicBitmap(Bitmap bitmap, int mosaicSize);

    /**
     * Mirrors the given the <em>bitmap</em>.<pre>
     * Horizontal : swap(RGB[i, j], RGB[width - 1 - i, height - 1 - j])
     * Vertical   : swap(RGB[i, j], RGB[width * (height - i - 1), j])</pre>
     * @param bitmap The bitmap to mirror, must be {@link Config#ARGB_8888} pixel format.
     * @param horizontal Whether to mirror horizontal.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static native boolean mirrorBitmap(Bitmap bitmap, boolean horizontal);

    /**
     * Blurs the given the <em>bitmap</em>.
     * @param context The <tt>Context</tt>.
     * @param bitmap The bitmap to blur, must be {@link Config#ARGB_8888} pixel format.
     * @param radius The radius of the blur, Supported range 0 < radius <= 25.
     */
    public static void blurBitmap(Context context, Bitmap bitmap, float radius) {
        final RenderScript rs = RenderScript.create(context);
        final Element element = Element.U8_4(rs);
        final ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rs, element);

        try {
            blurBitmap(rs, blur, bitmap, radius);
        } finally {
            rs.destroy();
            blur.destroy();
            element.destroy();
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
            final Matrix matrix = MatrixPool.obtain();
            matrix.setScale(sx, sy);
            bitmap = createBitmap(bitmap, matrix, newConfig, paint);
            MatrixPool.recycle(matrix);
        }

        return bitmap;
    }

    /**
     * Creates a mutable rotation <tt>Bitmap</tt> from given the <em>bitmap</em>.
     * @param bitmap The source bitmap.
     * @param degrees The rotation degrees.
     * @param newConfig The bitmap config to create.
     * @return The new rotated <tt>Bitmap</tt> or the source <tt>Bitmap</tt>
     * if no rotating is required.
     * @param paint May be <tt>null</tt>. The paint used to draw the bitmap.
     * @see #createRotateBitmap(Bitmap, float, float, float, Config, Paint)
     */
    public static Bitmap createRotateBitmap(Bitmap bitmap, float degrees, Config newConfig, Paint paint) {
        return createRotateBitmap(bitmap, degrees, (float)bitmap.getWidth() * 0.5f, (float)bitmap.getHeight() * 0.5f, newConfig, paint);
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
        if (degrees != 0) {
            final Matrix matrix = MatrixPool.obtain();
            matrix.setRotate(degrees, px, py);
            bitmap = createBitmap(bitmap, matrix, newConfig, paint);
            MatrixPool.recycle(matrix);
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
     * Saves a compressed version of the bitmap to the specified <em>filename</em>.
     * @param filename The filename to save, must be absolute file path.
     * @param bitmap The bitmap to save.
     * @param format TThe format of the compressed image.
     * @param quality Hint to the compressor, 0-100. 0 meaning compress for small
     * size, 100 meaning compress for max quality.
     * @return <tt>true</tt> if the operation succeeded, <tt>false</tt> otherwise.
     */
    public static boolean saveBitmap(String filename, Bitmap bitmap, CompressFormat format, int quality) {
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(filename);
            return bitmap.compress(format, quality, os);
        } catch (Exception e) {
            Log.e(BitmapUtils.class.getName(), new StringBuilder("Couldn't save bitmap to - ").append(filename).toString(), e);
            return false;
        } finally {
            FileUtils.close(os);
        }
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
     * Adjusts the specified <em>sampleSize</em> rounded down to the nearest power of 2.
     */
    public static int fixSampleSize(int sampleSize) {
        return (sampleSize <= 1 ? 1 : (sampleSize <= 8 ? Integer.highestOneBit(sampleSize) : (sampleSize / 8 * 8)));
    }

    /**
     * Computes a sample size which makes the longer side at least
     * <em>desiredWidth</em> or <em>desiredHeight</em> long.
     * @param width The original width.
     * @param height The original height.
     * @param desiredWidth The desired width.
     * @param desiredHeight The desired height.
     * @return The sample size.
     */
    public static int computeSampleSize(int width, int height, int desiredWidth, int desiredHeight) {
        return fixSampleSize(Math.max(width / desiredWidth, height / desiredHeight));
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
        final int scaledDensity = context.getResources().getDisplayMetrics().densityDpi;

        result.append(" { width = ").append(bitmap.getWidth())
              .append(", height = ").append(bitmap.getHeight())
              .append(", density = ").append(density).append('(').append(DeviceUtils.toDensity(density)).append(')')
              .append(", config = ").append(config != null ? config.name() : "UNKNOWN")
              .append(", scaledWidth = ").append(bitmap.getScaledWidth(scaledDensity))
              .append(", scaledHeight = ").append(bitmap.getScaledHeight(scaledDensity))
              .append(", scaledDensity = ").append(scaledDensity).append('(').append(DeviceUtils.toDensity(scaledDensity)).append(')')
              .append(", size = ").append(size).append('(').append(Formatter.formatFileSize(context, size)).append(')')
              .append(", allocSize = ").append(allocSize).append('(').append(Formatter.formatFileSize(context, allocSize)).append(')')
              .append(", mutable = ").append(bitmap.isMutable())
              .append(", recycle = ").append(bitmap.isRecycled())
              .append(" }");

        return result;
    }

    /* package */ static void blurBitmap(RenderScript rs, ScriptIntrinsicBlur blur, Bitmap bitmap, float radius) {
        final Allocation input = Allocation.createFromBitmap(rs, bitmap);
        final Type type = input.getType();
        final Allocation output = Allocation.createTyped(rs, type);

        try {
            blur.setInput(input);
            blur.setRadius(radius);
            blur.forEach(output);
            output.copyTo(bitmap);
        } finally {
            type.destroy();
            output.destroy();
        }
    }

    private static Bitmap createBitmap(Bitmap source, Matrix matrix, Config config, Paint paint) {
        final RectF src = new RectF(0, 0, source.getWidth(), source.getHeight());
        final RectF dst = RectFPool.obtain();
        matrix.mapRect(dst, src);

        final Bitmap bitmap = Bitmap.createBitmap((int)(dst.width() + 0.5f), (int)(dst.height() + 0.5), config);
        final Canvas canvas = new Canvas(bitmap);
        canvas.translate(-dst.left, -dst.top);
        canvas.concat(matrix);
        canvas.drawBitmap(source, null, src, paint);
        canvas.setBitmap(null);
        RectFPool.recycle(dst);

        return bitmap;
    }

    /**
     * Class <tt>RenderScriptBlur</tt> used to blur the <tt>Bitmap</tt>.
     */
    public static final class RenderScriptBlur {
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

        /**
         * Destroys this <tt>RenderScriptBlur</tt>. Frees
         * any native resources associated with this object.
         */
        public synchronized final void destroy() {
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
