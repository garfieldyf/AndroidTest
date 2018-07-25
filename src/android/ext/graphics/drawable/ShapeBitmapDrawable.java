package android.ext.graphics.drawable;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.graphics.DrawUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Abstract class ShapeBitmapDrawable
 * @author Garfield
 * @version 2.0
 */
public abstract class ShapeBitmapDrawable<T extends ShapeBitmapDrawable.BitmapState> extends AbstractDrawable<T> {
    private static final int FLAG_PATH = 0x01;  // mFlags

    /**
     * Constructor
     * @param state The {@link BitmapState}.
     */
    public ShapeBitmapDrawable(T state) {
        super(state);
    }

    /**
     * Returns the {@link Path} used by this drawable to render.
     * @return The {@link Path}.
     */
    public final Path getPath() {
        return mState.mPath;
    }

    /**
     * Returns the {@link Bitmap} used by this drawable to render.
     * @return The {@link Bitmap}.
     */
    public final Bitmap getBitmap() {
        return mState.mBitmap;
    }

    /**
     * Returns the mipmap hint is enabled on this drawable's bitmap.
     * @return <tt>true</tt> if the mipmap hint is enabled,
     * <tt>false</tt> otherwise.
     * @see #setMipMap(boolean)
     */
    public boolean hasMipMap() {
        return mState.mBitmap.hasMipMap();
    }

    /**
     * Enables or disables the mipmap hint for this drawable's bitmap.
     * See {@link Bitmap#setHasMipMap(boolean)} for more information.
     * @param mipMap <tt>true</tt> if this drawable's bitmap should use
     * mipmaps, <tt>false</tt> otherwise.
     * @see #hasMipMap()
     */
    public void setMipMap(boolean mipMap) {
        mState.mBitmap.setHasMipMap(mipMap);
        invalidateSelf();
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
    public int getMinimumWidth() {
        return mState.mBitmap.getWidth();
    }

    @Override
    public int getMinimumHeight() {
        return mState.mBitmap.getHeight();
    }

    @Override
    public int getIntrinsicWidth() {
        return mState.mBitmap.getWidth();
    }

    @Override
    public int getIntrinsicHeight() {
        return mState.mBitmap.getHeight();
    }

    @Override
    public int getOpacity() {
        return (mState.mPaint.getShader() != null || mState.mBitmap.hasAlpha() || mState.mPaint.getAlpha() < 255 ? PixelFormat.TRANSLUCENT : PixelFormat.OPAQUE);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        applyGravity();
    }

    @Override
    protected void draw(Canvas canvas, RectF bounds, Paint paint) {
        if (paint.getShader() != null) {
            canvas.drawPath(mState.mPath, paint);
        } else {
            canvas.drawBitmap(mState.mBitmap, null, bounds, paint);
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
        final Drawable drawable = res.getDrawable(id, theme);
        DebugUtils.__checkError(!(drawable instanceof BitmapDrawable), new StringBuilder(parser.getPositionDescription()).append(": The <").append(parser.getName()).append("> tag requires a valid 'src' attribute").toString());
        mState.setBitmap(((BitmapDrawable)drawable).getBitmap());
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
            DrawUtils.setShaderMatrix(mState.mShader, width, height, outBounds);
        }

        // Builds the convex path.
        if ((mFlags & FLAG_PATH) != 0) {
            mFlags &= ~FLAG_PATH;
            mState.mPath.rewind();
            getConvexPath(outBounds, mState.mPath);
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
            mState.mPaint.setShader(mState.mShader);
        } else {
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
     * Class <tt>BitmapState</tt> is an implementation of a {@link ConstantState}.
     */
    public static abstract class BitmapState extends BaseConstantState {
        /* package */ Bitmap mBitmap;
        /* package */ Shader mShader;
        /* package */ final Path mPath;

        /**
         * Constructor
         * @param bitmap The {@link Bitmap}.
         * @see #BitmapState(BitmapState)
         */
        public BitmapState(Bitmap bitmap) {
            setBitmap(bitmap);
            mPath = new Path();
        }

        /**
         * Copy constructor
         * @param state The {@link BitmapState}.
         * @see #BitmapState(Bitmap)
         */
        public BitmapState(BitmapState state) {
            super(state);
            mPath   = new Path(state.mPath);
            mBitmap = state.mBitmap;
            mShader = getShader();
        }

        /* package */ final void setBitmap(Bitmap bitmap) {
            if (bitmap != null) {
                mBitmap = bitmap;
                mShader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
            }
        }

        private Shader getShader() {
            final Shader shader = mPaint.getShader();
            return (shader != null ? shader : new BitmapShader(mBitmap, TileMode.CLAMP, TileMode.CLAMP));
        }
    }
}
