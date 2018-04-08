package android.ext.graphics;

import android.ext.util.Pools;
import android.ext.util.Pools.Factory;
import android.ext.util.Pools.Pool;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;
import android.util.StateSet;
import android.view.Gravity;
import android.view.View;

/**
 * Class DrawUtils
 * @author Garfield
 * @version 2.0
 */
public final class DrawUtils {
    /**
     * The content is align to forward diagonal.
     */
    public static final int FORWARD_DIAGONAL = 0x10000000;

    /**
     * The content is align to backward diagonal.
     */
    public static final int BACKWARD_DIAGONAL = 0x20000000;

    /**
     * Draws a single specified range of text, specified by start/end, with it's content in rect,
     * with the specified paint and gravity.
     * @param canvas The <tt>Canvas</tt>.
     * @param paint The <tt>Paint</tt> used to draw the <em>text</em> (e.g. color, size, style).
     * @param text The text to be drawn.
     * @param start The inclusive beginning index in <em>text</em> to draw.
     * @param end The exclusive end index in <em>text</em> to draw.
     * @param rect The specified a bounding rectangle for the <em>text</em>.
     * @param gravity May be any combination of {@link Gravity#LEFT}, {@link Gravity#TOP}, {@link Gravity#RIGHT},
     * {@link Gravity#BOTTOM}, {@link Gravity#CENTER}, {@link Gravity#CENTER_HORIZONTAL} and {@link Gravity#CENTER_VERTICAL}.
     * @return The y-coordinate of origin for where to draw the <em>text</em>.
     * @see #drawText(Canvas, Paint, CharSequence, int, int, float, float, float, float, int)
     */
    public static float drawText(Canvas canvas, Paint paint, CharSequence text, int start, int end, Rect rect, int gravity) {
        return drawText(canvas, paint, text, start, end, rect.left, rect.top, rect.right, rect.bottom, gravity);
    }

    /**
     * Draws a single specified range of text, specified by start/end, with it's content in
     * rect (left, top, right, bottom), with the specified paint and gravity.
     * @param canvas The <tt>Canvas</tt>.
     * @param paint The <tt>Paint</tt> used to draw the <em>text</em> (e.g. color, size, style).
     * @param text The text to be drawn.
     * @param start The inclusive beginning index in <em>text</em> to draw.
     * @param end The exclusive end index in <em>text</em> to draw.
     * @param left The specified a bounding rectangle left coordinate for the <em>text</em>.
     * @param top The specified a bounding rectangle top coordinate for the <em>text</em>.
     * @param right The specified a bounding rectangle right coordinate for the <em>text</em>.
     * @param bottom The specified a bounding rectangle bottom coordinate for the <em>text</em>.
     * @param gravity May be any combination of {@link Gravity#LEFT}, {@link Gravity#TOP}, {@link Gravity#RIGHT},
     * {@link Gravity#BOTTOM}, {@link Gravity#CENTER}, {@link Gravity#CENTER_HORIZONTAL} and {@link Gravity#CENTER_VERTICAL}.
     * @return The y-coordinate of origin for where to draw the <em>text</em>.
     * @see #drawText(Canvas, Paint, CharSequence, int, int, Rect, int)
     */
    public static float drawText(Canvas canvas, Paint paint, CharSequence text, int start, int end, float left, float top, float right, float bottom, int gravity) {
        final RectF rect = RectFPool.obtain();
        final Align oldAlign = paint.getTextAlign();
        paint.setTextAlign(computeText(paint, rect, left, top, right, bottom, gravity));

        final float y = rect.top;
        canvas.drawText(text, start, end, rect.left, y, paint);
        paint.setTextAlign(oldAlign);
        RectFPool.recycle(rect);

        return y;
    }

    /**
     * Draw a line with in rect (left, top, right, bottom) coordinates, using the specified paint and gravity.
     * @param canvas The <tt>Canvas</tt>.
     * @param paint The <tt>Paint</tt> used to draw the line (e.g. color, stroke width).
     * @param rect The specified a bounding rectangle for the line.
     * @param gravity May be one of {@link Gravity#LEFT}, {@link Gravity#TOP}, {@link Gravity#RIGHT},
     * {@link Gravity#BOTTOM}, {@link Gravity#CENTER_HORIZONTAL}, {@link Gravity#CENTER_VERTICAL},
     * {@link #FORWARD_DIAGONAL} or {@link #BACKWARD_DIAGONAL}.
     * @see #drawLine(Canvas, Paint, float, float, float, float, int)
     */
    public static void drawLine(Canvas canvas, Paint paint, Rect rect, int gravity) {
        drawLine(canvas, paint, rect.left, rect.top, rect.right, rect.bottom, gravity);
    }

    /**
     * Draw a line with in rect (left, top, right, bottom) coordinates, using the specified paint and gravity.
     * @param canvas The <tt>Canvas</tt>.
     * @param paint The <tt>Paint</tt> used to draw the line (e.g. color, stroke width).
     * @param left The specified a bounding rectangle left coordinate for the line.
     * @param top The specified a bounding rectangle top coordinate for the line.
     * @param right The specified a bounding rectangle right coordinate for the line.
     * @param bottom The specified a bounding rectangle bottom coordinate for the line.
     * @param gravity May be one of {@link Gravity#LEFT}, {@link Gravity#TOP}, {@link Gravity#RIGHT},
     * {@link Gravity#BOTTOM}, {@link Gravity#CENTER_HORIZONTAL}, {@link Gravity#CENTER_VERTICAL},
     * {@link #FORWARD_DIAGONAL} or {@link #BACKWARD_DIAGONAL}.
     * @see #drawLine(Canvas, Paint, Rect, int)
     */
    public static void drawLine(Canvas canvas, Paint paint, float left, float top, float right, float bottom, int gravity) {
        final RectF rect = RectFPool.obtain();
        computeLine(rect, left, top, right, bottom, gravity);
        canvas.drawLine(rect.left, rect.top, rect.right, rect.bottom, paint);
        RectFPool.recycle(rect);
    }

    /**
     * Draw the drawable with the specified the {@link View} states.
     * @param canvas The canvas to draw into.
     * @param drawable The drawable to be drawn.
     * @param view The <tt>View</tt> obtains the current states.
     * @param stateSpec An array of required {@link View} states. If
     * the <em>drawable</em> is state full, this parameter will be ignored.
     * @see #drawDrawable(Canvas, Drawable, int[], int[], int, int, int, int)
     */
    public static void drawDrawable(Canvas canvas, Drawable drawable, View view, int[] stateSpec) {
        drawDrawable(canvas, drawable, view.getDrawableState(), stateSpec, 0, 0, view.getWidth(), view.getHeight());
    }

    /**
     * Draw the drawable with the specified the {@link View} states.
     * @param canvas The canvas to draw into.
     * @param drawable The drawable to be drawn.
     * @param stateSet An array of the {@link View} current states.
     * @param stateSpec An array of required {@link View} states. If the <em>drawable</em> is
     * state full, this parameter will be ignored.
     * @param left The specified a bounding rectangle left coordinate for the <em>drawable</em>.
     * @param top The specified a bounding rectangle top coordinate for the <em>drawable</em>.
     * @param right The specified a bounding rectangle right coordinate for the <em>drawable</em>.
     * @param bottom The specified a bounding rectangle bottom coordinate for the <em>drawable</em>.
     * @see #drawDrawable(Canvas, Drawable, View, int[])
     */
    public static void drawDrawable(Canvas canvas, Drawable drawable, int[] stateSet, int[] stateSpec, int left, int top, int right, int bottom) {
        if (drawable.isStateful()) {
            drawable.setState(stateSet);
            drawDrawable(canvas, drawable, left, top, right, bottom);
        } else if (StateSet.stateSetMatches(stateSpec, stateSet)) {
            drawDrawable(canvas, drawable, left, top, right, bottom);
        }
    }

    /**
     * Draws a mirrored drawable with given the <em>drawable</em> and <em>canvas</em>.
     * The <em>drawable</em> must already have had {@link Drawable#setBounds} called.
     * @param canvas The <tt>Canvas</tt>.
     * @param drawable The drawable to be drawn.
     * @param horizontal Whether to draw a horizontal mirrored drawable.
     * @see #drawMirroredDrawable(Canvas, Drawable, int, int, int, int, boolean)
     */
    public static void drawMirroredDrawable(Canvas canvas, Drawable drawable, boolean horizontal) {
        final Rect bounds = drawable.getBounds();
        drawMirroredDrawable(canvas, drawable, bounds.width(), bounds.height(), horizontal);
    }

    /**
     * Draws a mirrored drawable with given the <em>drawable</em> and <em>canvas</em>.
     * @param canvas The <tt>Canvas</tt>.
     * @param drawable The drawable to be drawn.
     * @param left The specified a bounding rectangle left coordinate for the <em>drawable</em>.
     * @param top The specified a bounding rectangle top coordinate for the <em>drawable</em>.
     * @param right The specified a bounding rectangle right coordinate for the <em>drawable</em>.
     * @param bottom The specified a bounding rectangle bottom coordinate for the <em>drawable</em>.
     * @param horizontal Whether to draw a horizontal mirrored drawable.
     * @see #drawMirroredDrawable(Canvas, Drawable, boolean)
     */
    public static void drawMirroredDrawable(Canvas canvas, Drawable drawable, int left, int top, int right, int bottom, boolean horizontal) {
        drawable.setBounds(left, top, right, bottom);
        drawMirroredDrawable(canvas, drawable, right - left, bottom - top, horizontal);
    }

    /**
     * Draws a mirrored bitmap with given the <em>bitmap</em> and <em>canvas</em>.
     * @param canvas The <tt>Canvas</tt>.
     * @param bitmap The bitmap to be drawn.
     * @param dst The rectangle that the <em>bitmap</em> to fit into.
     * @param horizontal Whether to draw a horizontal mirrored bitmap.
     * @param paint May be <tt>null</tt>. The paint used to draw the bitmap.
     * @see #drawMirroredBitmap(Canvas, Bitmap, float, float, float, float, boolean, Paint)
     */
    public static void drawMirroredBitmap(Canvas canvas, Bitmap bitmap, RectF dst, boolean horizontal, Paint paint) {
        final float dx, dy, scale;
        if (horizontal) {
            dx = dst.width();
            dy = 0;
            scale = -1.0f;
        } else {
            dx = 0;
            dy = dst.height();
            scale = 1.0f;
        }

        final int saveCount = canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, -scale);
        canvas.drawBitmap(bitmap, null, dst, paint);
        canvas.restoreToCount(saveCount);
    }

    /**
     * Draws a mirrored bitmap with given the <em>bitmap</em> and <em>canvas</em>.
     * @param canvas The <tt>Canvas</tt>.
     * @param bitmap The bitmap to be drawn.
     * @param left The specified a bounding rectangle left coordinate for the <em>bitmap</em>.
     * @param top The specified a bounding rectangle top coordinate for the <em>bitmap</em>.
     * @param right The specified a bounding rectangle right coordinate for the <em>bitmap</em>.
     * @param bottom The specified a bounding rectangle bottom coordinate for the <em>bitmap</em>.
     * @param horizontal Whether to draw a horizontal mirrored bitmap.
     * @param paint May be <tt>null</tt>. The paint used to draw the bitmap.
     * @see #drawMirroredBitmap(Canvas, Bitmap, RectF, boolean, Paint)
     */
    public static void drawMirroredBitmap(Canvas canvas, Bitmap bitmap, float left, float top, float right, float bottom, boolean horizontal, Paint paint) {
        final RectF rect = RectFPool.obtain();
        rect.set(left, top, right, bottom);
        drawMirroredBitmap(canvas, bitmap, rect, horizontal, paint);
        RectFPool.recycle(rect);
    }

    /**
     * Draws an inverted bitmap with given the <em>bitmap</em> and <em>canvas</em>.
     * @param canvas The <tt>Canvas</tt>.
     * @param bitmap The bitmap to be drawn.
     * @param alpha The alpha component [0..255] to be drawn.
     * @param percent The percentage, expressed as a percentage of the <em>bitmap's</em>
     * width or height.
     * @param direction The direction. One of {@link Gravity#LFET}, {@link Gravity#TOP},
     * {@link Gravity#RIGHT} or {@link Gravity#BOTTOM}.
     * @param paint The paint used to draw, This parameter can <b>not</b> be <tt>null</tt>.
     */
    public static void drawInvertedBitmap(Canvas canvas, Bitmap bitmap, int alpha, float percent, int direction, Paint paint) {
        float left = 0, top = 0, width = bitmap.getWidth(), height = bitmap.getHeight();
        float scale, dx = 0, dy = 0, startX = 0, stopX = 0, startY = 0, stopY = 0;

        if (direction == Gravity.LEFT || direction == Gravity.RIGHT) {
            dx = width;
            width = width * percent;
            scale = -1.0f;
            stopY = startY = height * 0.5f;
            if (direction == Gravity.LEFT) {
                startX = width;
                left = dx - width - 1;
            } else {
                stopX = width;
            }
        } else {
            dy = height;
            height = height * percent;
            scale  = 1.0f;
            startX = stopX = width * 0.5f;
            if (direction == Gravity.TOP) {
                startY = height;
                top = dy - height - 1;
            } else {
                stopY = height;
            }
        }

        // Draws the inverted bitmap.
        final int saveCount = canvas.save();
        canvas.clipRect(0, 0, width, height);
        canvas.translate(dx, dy);
        canvas.scale(scale, -scale);
        canvas.drawBitmap(bitmap, left, top, paint);
        canvas.restoreToCount(saveCount);

        // Draws the rect use linear gradient filter the bitmap.
        final Shader shader = paint.getShader();
        final Xfermode mode = paint.getXfermode();

        paint.setShader(new LinearGradient(startX, startY, stopX, stopY, Color.argb(alpha, 255, 255, 255), Color.TRANSPARENT, TileMode.CLAMP));
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
        canvas.drawRect(0, 0, width, height, paint);
        paint.setShader(shader);
        paint.setXfermode(mode);
    }

    /**
     * Computes the drawn line coordinate.
     * @param outRect The drawn line coordinate to store (startX = left, stopX = right,
     * startY = top, stopY = bottom).
     * @param left The specified a bounding rectangle left coordinate for the line.
     * @param top The specified a bounding rectangle top coordinate for the line.
     * @param right The specified a bounding rectangle right coordinate for the line.
     * @param bottom The specified a bounding rectangle bottom coordinate for the line.
     * @param gravity The gravity, Pass one of {@link Gravity#LEFT}, {@link Gravity#TOP},
     * {@link Gravity#RIGHT}, {@link Gravity#BOTTOM}, {@link Gravity#CENTER_HORIZONTAL},
     * {@link Gravity#CENTER_VERTICAL}, {@link #FORWARD_DIAGONAL} or {@link #BACKWARD_DIAGONAL}.
     */
    public static void computeLine(RectF outRect, float left, float top, float right, float bottom, int gravity) {
        outRect.set(left, top, right, bottom);
        switch (gravity) {
        case Gravity.LEFT:
            outRect.right = left;
            break;

        case Gravity.TOP:
            outRect.bottom = top;
            break;

        case Gravity.RIGHT:
            outRect.left = right;
            break;

        case Gravity.BOTTOM:
            outRect.top = bottom;
            break;

        case Gravity.CENTER_VERTICAL:
            outRect.left = outRect.right = (left + right) * 0.5f;
            break;

        case Gravity.CENTER_HORIZONTAL:
            outRect.top = outRect.bottom = (top + bottom) * 0.5f;
            break;

        case BACKWARD_DIAGONAL:
            outRect.left  = right;
            outRect.right = top;
            outRect.top   = left;
            break;
        }
    }

    /**
     * Sets the {@link Shader}'s local matrix.
     * @param shader The <tt>Shader</tt> to set.
     * @param width The horizontal size of the object.
     * @param height The vertical size of the object.
     * @param container The frame of the containing space, in which the object will be placed.
     */
    public static void setShaderMatrix(Shader shader, int width, int height, RectF container) {
        final RectF src = RectFPool.obtain();
        src.set(0, 0, width, height);

        // Computes the scale value that map the source
        // rectangle to the destination rectangle.
        final Matrix matrix = MatrixPool.obtain();
        matrix.setRectToRect(src, container, ScaleToFit.FILL);

        // Sets the shader scale matrix.
        shader.setLocalMatrix(matrix);
        RectFPool.recycle(src);
        MatrixPool.recycle(matrix);
    }

    /**
     * Like as {@link Gravity#apply(int, int, int, Rect, Rect)}, but the <em>outRect</em> is {@link RectF}.
     * @param gravity The desired placement of the object, as defined by the constants in {@link Gravity}.
     * @param width The horizontal size of the object.
     * @param height The vertical size of the object.
     * @param container The frame of the containing space, in which the object will be placed.
     * @param outRect The computed frame of the object in its container.
     * @see Gravity#apply(int, int, int, Rect, Rect)
     */
    public static void applyGravity(int gravity, int width, int height, Rect container, RectF outRect) {
        final Rect rect = RectPool.obtain();
        Gravity.apply(gravity, width, height, container, 0, 0, rect);
        outRect.set(rect);
        RectPool.recycle(rect);
    }

    private static void drawDrawable(Canvas canvas, Drawable drawable, int left, int top, int right, int bottom) {
        final Rect padding = RectPool.obtain();
        if (drawable.getPadding(padding)) {
            left   -= padding.left;
            top    -= padding.top;
            right  += padding.right;
            bottom += padding.bottom;
        }

        RectPool.recycle(padding);
        drawable.setBounds(left, top, right, bottom);
        drawable.draw(canvas);
    }

    private static void drawMirroredDrawable(Canvas canvas, Drawable drawable, int width, int height, boolean horizontal) {
        final float dx, dy, scale;
        if (horizontal) {
            dx = width;
            dy = 0;
            scale = -1.0f;
        } else {
            dx = 0;
            dy = height;
            scale = 1.0f;
        }

        final int saveCount = canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, -scale);
        drawable.draw(canvas);
        canvas.restoreToCount(saveCount);
    }

    private static Align computeText(Paint paint, RectF outRect, float left, float top, float right, float bottom, int gravity) {
        final FontMetrics fm = FontMetricsPool.obtain();
        paint.getFontMetrics(fm);

        // Computes x-coordinate.
        final Align textAlign;
        switch (gravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
        case Gravity.RIGHT:
            outRect.left = right;
            textAlign = Align.RIGHT;
            break;

        case Gravity.CENTER_HORIZONTAL:
            outRect.left = (left + right) * 0.5f;
            textAlign = Align.CENTER;
            break;

        // Gravity.LEFT
        default:
            outRect.left = left;
            textAlign = Align.LEFT;
        }

        // Computes y-coordinate.
        switch (gravity & Gravity.VERTICAL_GRAVITY_MASK) {
        case Gravity.BOTTOM:
            outRect.top = bottom - fm.descent;
            break;

        case Gravity.CENTER_VERTICAL:
            outRect.top = (top + bottom - fm.bottom - fm.ascent) * 0.5f;
            break;

        // Gravity.TOP
        default:
            outRect.top = top - fm.ascent;
        }

        FontMetricsPool.recycle(fm);
        return textAlign;
    }

    /**
     * Class <tt>RectPool</tt>.
     */
    public static final class RectPool implements Factory<Rect> {
        private static final RectPool sInstance = new RectPool();
        private final Pool<Rect> pool = Pools.newSimplePool(this);

        @Override
        public Rect newInstance() {
            return new Rect();
        }

        public static Rect obtain() {
            return sInstance.pool.obtain();
        }

        public static void recycle(Rect rect) {
            sInstance.pool.recycle(rect);
        }
    }

    /**
     * Class <tt>RectFPool</tt>.
     */
    public static final class RectFPool implements Factory<RectF> {
        private static final RectFPool sInstance = new RectFPool();
        private final Pool<RectF> pool = Pools.newSimplePool(this);

        @Override
        public RectF newInstance() {
            return new RectF();
        }

        public static RectF obtain() {
            return sInstance.pool.obtain();
        }

        public static void recycle(RectF rect) {
            sInstance.pool.recycle(rect);
        }
    }

    /**
     * Class <tt>MatrixPool</tt>.
     */
    public static final class MatrixPool implements Factory<Matrix> {
        private static final MatrixPool sInstance = new MatrixPool();
        private final Pool<Matrix> pool = Pools.newSimplePool(this);

        @Override
        public Matrix newInstance() {
            return new Matrix();
        }

        public static Matrix obtain() {
            return sInstance.pool.obtain();
        }

        public static void recycle(Matrix matrix) {
            sInstance.pool.recycle(matrix);
        }
    }

    /**
     * Class <tt>FontMetricsPool</tt>.
     */
    public static final class FontMetricsPool implements Factory<FontMetrics> {
        private static final FontMetricsPool sInstance = new FontMetricsPool();
        private final Pool<FontMetrics> pool = Pools.newSimplePool(this);

        @Override
        public FontMetrics newInstance() {
            return new FontMetrics();
        }

        public static FontMetrics obtain() {
            return sInstance.pool.obtain();
        }

        public static void recycle(FontMetrics metrics) {
            sInstance.pool.recycle(metrics);
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private DrawUtils() {
    }
}
