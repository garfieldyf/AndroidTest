package android.ext.graphics.drawable;

import android.ext.graphics.GIFImage;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;

/**
 * Abstract class ShapeGIFDrawable
 * @author Garfield
 */
public abstract class ShapeGIFDrawable<T extends ShapeGIFDrawable.ShapeGIFState> extends GIFBaseDrawable<T> {
    /**
     * Constructor
     * @param state The {@link ShapeGIFState}.
     */
    public ShapeGIFDrawable(T state) {
        super(state);
    }

    @Override
    protected void drawFrame(Canvas canvas, int frameIndex, Bitmap frame, RectF bounds, Paint paint) {
        if (paint.getShader() != null) {
            canvas.drawPath(mState.mPath, paint);
        } else {
            canvas.drawBitmap(frame, null, bounds, paint);
        }
    }

    @Override
    /* package */ void computeDrawingBounds(Rect bounds, RectF outBounds) {
        computeDrawingBounds(bounds, mState.mShader, mState.mPath, outBounds);
    }

    /**
     * Invalidate this drawable. If the <em>invalidatePath</em> parameter is <tt>true</tt>,
     * {@link #getConvexPath(RectF, Path)} will be called at some point in the future.
     * @param invalidatePath Whether the path for this drawable should be invalidated as well.
     */
    protected final void invalidateSelf(boolean invalidatePath) {
        invalidateSelf(mState.mShader, invalidatePath);
    }

    /**
     * Called to builds the {@link Path#isConvex() convex path}.
     * This method will be called by when this drawable's bounds
     * has been changed or if {@link #invalidateSelf(boolean)}
     * is called explicitly. The input <em>outPath</em> is empty.
     * @param bounds The new bounds of the path.
     * @param outPath The empty path to be build.
     */
    protected abstract void getConvexPath(RectF bounds, Path outPath);

    /**
     * Class <tt>ShapeGIFState</tt> is an implementation of a {@link ConstantState}.
     */
    public static abstract class ShapeGIFState extends GIFBaseDrawable.GIFBaseState {
        /* package */ Shader mShader;
        /* package */ final Path mPath;

        /**
         * Constructor
         * @param image The {@link GIFImage}.
         * @see #ShapeGIFState(ShapeGIFState)
         */
        public ShapeGIFState(GIFImage image) {
            super(image);
            mPath = new Path();
        }

        /**
         * Copy constructor
         * @param state The {@link ShapeGIFState}.
         * @see #ShapeGIFState(GIFImage)
         */
        public ShapeGIFState(ShapeGIFState state) {
            super(state);
            mPath   = new Path(state.mPath);
            mShader = new BitmapShader(mCanvas, TileMode.CLAMP, TileMode.CLAMP);
            if (mPaint.getShader() != null) {
                mPaint.setShader(mShader);
            }
        }

        @Override
        /* package */ void setImage(GIFImage image) {
            if (image != null) {
                mImage  = image;
                mCanvas = image.createBitmapCanvas();
                mShader = new BitmapShader(mCanvas, TileMode.CLAMP, TileMode.CLAMP);
            }
        }
    }
}
