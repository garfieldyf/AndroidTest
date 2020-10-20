package android.ext.graphics.drawable;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Abstract class ShapeBitmapDrawable
 * @author Garfield
 */
public abstract class ShapeBitmapDrawable<T extends ShapeBitmapDrawable.BitmapState> extends ImageDrawable<T> {
    /**
     * Constructor
     * @param state The {@link BitmapState}.
     */
    public ShapeBitmapDrawable(T state) {
        super(state);
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
    protected void draw(Canvas canvas, RectF bounds, Paint paint) {
        if (paint.getShader() != null) {
            canvas.drawPath(mState.mPath, paint);
        } else {
            canvas.drawBitmap(mState.mBitmap, null, bounds, paint);
        }
    }

    @Override
    @TargetApi(21)
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
        final Bitmap bitmap = BitmapFactory.decodeResource(res, id);
        if (bitmap == null) {
            throw new XmlPullParserException(parser.getPositionDescription() + ": The <" + parser.getName() + "> tag requires a valid 'src' attribute");
        }

        mState.setBitmap(bitmap);
    }

    @Override
    /* package */ void computeDrawingBounds(Rect bounds, RectF outBounds) {
        computeDrawingBounds(bounds, mState.mShader, mState.mPath, outBounds);
    }

    /**
     * Invalidate this drawable. If the <em>invalidatePath</em> parameter is <tt>true</tt>,
     * {@link #getConvexPath(RectF, Path)} will be called at some point in the future.
     * @param invalidatePath Whether the path for this drawable should be rebuild as well.
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
     * @param outPath The path to be build.
     */
    protected abstract void getConvexPath(RectF bounds, Path outPath);

    /**
     * Sets the specified {@link Bitmap} to this drawable.
     */
    /* package */ final void setBitmap(Bitmap bitmap) {
        if (mState.mBitmap != bitmap) {
            mFlags |= FLAG_BOUNDS;
            mState.setBitmap(bitmap);
            invalidateSelf(mState.mShader, true);
            DebugUtils.__checkDebug(true, getClass().getName(), "setBitmap() - " + bitmap);
        }
    }

    /**
     * Class <tt>BitmapState</tt> is an implementation of a {@link ConstantState}.
     */
    public static abstract class BitmapState extends ImageDrawable.ImageState {
        /* package */ Bitmap mBitmap;
        /* package */ Shader mShader;
        /* package */ final Path mPath;

        /**
         * Constructor
         * @param bitmap The {@link Bitmap}.
         * @see #BitmapState(BitmapState)
         */
        public BitmapState(Bitmap bitmap) {
            mPath = new Path();
            if (bitmap != null) {
                setBitmap(bitmap);
            }
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
            DebugUtils.__checkError(bitmap == null, "bitmap == null");
            mBitmap = bitmap;
            mShader = new BitmapShader(bitmap, TileMode.CLAMP, TileMode.CLAMP);
        }

        private Shader getShader() {
            final Shader shader = mPaint.getShader();
            return (shader != null ? shader : new BitmapShader(mBitmap, TileMode.CLAMP, TileMode.CLAMP));
        }
    }
}
