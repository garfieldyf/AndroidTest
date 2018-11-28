package android.ext.graphics;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.View;

/**
 * Abstract class ShapeLayer
 * @author Garfield
 */
public abstract class ShapeLayer {
    private final Path mPath;
    private final Paint mPaint;
    private final RectF mBounds;

    /**
     * Constructor
     */
    public ShapeLayer() {
        mBounds = new RectF();
        mPath   = new Path();
        mPaint  = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStyle(Style.FILL);
        mPaint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));
    }

    /**
     * Allocates an offscreen bitmap. All drawing calls are directed there, and
     * only when the balancing call to {@link #restore(Canvas, int)} is made is
     * that offscreen transfered to the canvas.
     * @param canvas The <tt>Canvas</tt> to save.
     * @param view The <tt>View</tt>.
     * @return The value to pass to {@link #restore(Canvas, int)} to balance this
     * <tt>save()</tt>.
     * @see #save(Canvas, int, int, int, int, int, int)
     * @see #restore(Canvas, int)
     */
    public final int save(Canvas canvas, View view) {
        return save(canvas, view.getPaddingLeft(), view.getPaddingTop(), view.getPaddingRight(), view.getPaddingBottom(), view.getWidth(), view.getHeight());
    }

    /**
     * Allocates an offscreen bitmap. All drawing calls are directed there, and
     * only when the balancing call to {@link #restore(Canvas, int)} is made is
     * that offscreen transfered to the canvas.
     * @param canvas The <tt>Canvas</tt> to save.
     * @param paddingLeft The left padding in pixels.
     * @param paddingTop The top padding in pixels.
     * @param paddingRight The right padding in pixels.
     * @param paddingBottom The bottom padding in pixels.
     * @param width The width of the offscreen bitmap in pixels.
     * @param height The height of the offscreen bitmap in pixels.
     * @return The value to pass to {@link #restore(Canvas, int)} to balance this
     * <tt>save()</tt>.
     * @see #save(Canvas, View)
     * @see #restore(Canvas, int)
     */
    public int save(Canvas canvas, int paddingLeft, int paddingTop, int paddingRight, int paddingBottom, int width, int height) {
        final int right = width - paddingRight, bottom = height - paddingBottom;
        if (mBounds.left != paddingLeft || mBounds.top != paddingTop || mBounds.right != right || mBounds.bottom != bottom) {
            mPath.rewind();
            mBounds.set(paddingLeft, paddingTop, right, bottom);
            getConvexPath(mBounds, mPath);
        }

        return canvas.saveLayer(0, 0, width, height, null, Canvas.ALL_SAVE_FLAG);
    }

    /**
     * This call balances a previous call to <tt>save()</tt>.
     * @param canvas The <tt>Canvas</tt> to restore.
     * @param saveCount The save level to restore to.
     * @see #save(Canvas, View)
     * @see #save(Canvas, int, int, int, int, int, int)
     */
    public void restore(Canvas canvas, int saveCount) {
        canvas.drawPath(mPath, mPaint);
        canvas.restoreToCount(saveCount);
    }

    /**
     * Returns the rectangular bounds to be drawn.
     * @return The rectangular bounds to be drawn.
     */
    public final RectF getBounds() {
        return mBounds;
    }

    /**
     * Called to builds the {@link Path#isConvex() convex path}. The input
     * <em>outPath</em> is empty. Subclasses should override this method to
     * build the <em>outPath</em>.
     * @param bounds The new bounds of the path.
     * @param outPath The empty path to be build.
     */
    protected abstract void getConvexPath(RectF bounds, Path outPath);

    /**
     * Class <tt>OvalLayer</tt> is an implementation of a {@link ShapeLayer}.
     */
    public static final class OvalLayer extends ShapeLayer {
        @Override
        protected void getConvexPath(RectF bounds, Path outPath) {
            outPath.addOval(bounds, Direction.CW);
        }
    }

    /**
     * Class <tt>RoundedRectLayer</tt> is an implementation of a {@link ShapeLayer}.
     */
    public static final class RoundedRectLayer extends ShapeLayer {
        private final float[] mRadii;

        /**
         * Constructor
         * @param radii The corner radii, array of 8 values. Each corner receives two radius
         * values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
         * <tt>bottom-right</tt>, <tt>bottom-left</tt>.
         * @see #RoundedRectLayer(float, float, float, float)
         */
        public RoundedRectLayer(float[] radii) {
            mRadii = radii;
        }

        /**
         * Constructor
         * @param topLeftRadius The top-left radius used to round the corners.
         * @param topRightRadius The top-right radius used to round the corners.
         * @param bottomLeftRadius The bottom-left radius used to round the corners.
         * @param bottomRightRadius The bottom-right radius used to round the corners.
         * @see #RoundedRectLayer(float[])
         */
        public RoundedRectLayer(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
            mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
        }

        @Override
        protected void getConvexPath(RectF bounds, Path outPath) {
            outPath.addRoundRect(bounds, mRadii, Direction.CW);
        }
    }
}
