package android.ext.graphics.drawable;

import android.ext.graphics.DrawUtils;
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
import android.graphics.drawable.Drawable;

/**
 * Abstract class ShapeGIFDrawable
 * @author Garfield
 */
public abstract class ShapeGIFDrawable extends GIFDrawable {
    private static final int FLAG_PATH = 0x04;
    private final Path mPath;
    private final Shader mShader;

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     */
    public ShapeGIFDrawable(GIFImage image) {
        super(image);
        mPath = new Path();
        mShader = new BitmapShader(mState.mCanvas, TileMode.CLAMP, TileMode.CLAMP);
    }

    @Override
    public void setGravity(int gravity) {
        if (mState.mGravity != gravity) {
            mState.mGravity = gravity;
            applyGravity();
            invalidateSelf();
        }
    }

    @Override
    public Drawable mutate() {
        return this;
    }

    @Override
    public ConstantState getConstantState() {
        return null;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        applyGravity();
    }

    @Override
    protected void drawFrame(Canvas canvas, int frameIndex, Bitmap frame, RectF bounds, Paint paint) {
        if (paint.getShader() != null) {
            canvas.drawPath(mPath, paint);
        } else {
            canvas.drawBitmap(frame, null, bounds, paint);
        }
    }

    @Override
    /* package */ void computeDrawingBounds(RectF outBounds) {
        // Computes the drawing bounds.
        if ((mFlags & FLAG_GRAVITY) != 0) {
            mFlags &= ~FLAG_GRAVITY;
            final int width  = getIntrinsicWidth();
            final int height = getIntrinsicHeight();
            DrawUtils.applyGravity(mState.mGravity, width, height, getBounds(), outBounds);

            // Sets the shader's scale matrix.
            setShaderMatrix(mShader, width, height, outBounds);
        }

        // Builds the convex path.
        if ((mFlags & FLAG_PATH) != 0) {
            mFlags &= ~FLAG_PATH;
            mPath.rewind();
            getConvexPath(outBounds, mPath);
        }
    }

    /**
     * Returns the {@link Path} used by this drawable to render.
     * @return The {@link Path}.
     */
    /* package */ final Path getPath() {
        return mPath;
    }

    /**
     * Adds the <tt>FLAG_GRAVITY</tt> constant. If paint shader
     * is not <tt>null</tt> adds the <tt>FLAG_PATH</tt> constant.
     */
    private void applyGravity() {
        mFlags |= FLAG_GRAVITY;
        if (mState.mPaint.getShader() != null) {
            mFlags |= FLAG_PATH;
        }
    }

    /**
     * Invalidate this drawable. If the <em>invalidatePath</em> parameter is <tt>true</tt>,
     * {@link #getConvexPath(RectF, Path)} will be called at some point in the future.
     * @param invalidatePath Whether the path for this drawable should be invalidated as well.
     */
    protected final void invalidateSelf(boolean invalidatePath) {
        if (invalidatePath) {
            mFlags |= FLAG_PATH;
            mState.mPaint.setShader(mShader);
        } else {
            mFlags &= ~FLAG_PATH;
            mState.mPaint.setShader(null);
        }

        invalidateSelf();
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
}
