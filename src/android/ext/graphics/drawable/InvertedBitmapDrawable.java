package android.ext.graphics.drawable;

import android.ext.graphics.BitmapUtils;
import android.ext.graphics.DrawUtils;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.View;

/**
 * Class InvertedBitmapDrawable
 * @author Garfield
 * @version 1.0
 */
public class InvertedBitmapDrawable extends AbstractDrawable<InvertedBitmapDrawable.InvertedBitmapState> {
    /**
     * Constructor
     * @param bitmap The original {@link Bitmap} to create the inverted bitmap.
     * @param alpha The alpha component [0..255] of the inverted bitmap.
     * @param percent The percentage, expressed as a percentage of the <em>bitmap's</em>
     * width or height.
     * @param direction The direction. One of {@link Gravity#LFET}, {@link Gravity#TOP},
     * {@link Gravity#RIGHT} or {@link Gravity#BOTTOM}.
     * @see #InvertedBitmapDrawable(View, int, float, int)
     */
    public InvertedBitmapDrawable(Bitmap bitmap, int alpha, float percent, int direction) {
        super(new InvertedBitmapState(bitmap, alpha, percent, direction));
    }

    /**
     * Constructor
     * @param view The {@link View} to create the inverted bitmap.
     * @param alpha The alpha component [0..255] of the inverted bitmap.
     * @param percent The percentage, expressed as a percentage of the <em>view's</em>
     * width or height.
     * @param direction The direction. One of {@link Gravity#LFET}, {@link Gravity#TOP},
     * {@link Gravity#RIGHT} or {@link Gravity#BOTTOM}.
     * @see #InvertedBitmapDrawable(Bitmap, int, float, int)
     */
    public InvertedBitmapDrawable(View view, int alpha, float percent, int direction) {
        super(new InvertedBitmapState(view, alpha, percent, direction));
    }

    /**
     * Changes this drawable's content with the specified <em>view</em>.
     * @param view The {@link View} to rebuild the content.
     * @see #setBitmap(Bitmap)
     */
    public final void setView(View view) {
        mState.setSource(view);
        invalidateSelf();
    }

    /**
     * Changes this drawable's content with the specified <em>bitmap</em>.
     * @param bitmap The {@link Bitmap} to rebuild the content.
     * @see #setView(View)
     */
    public final void setBitmap(Bitmap bitmap) {
        mState.setSource(bitmap);
        invalidateSelf();
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
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    protected void draw(Canvas canvas, RectF bounds, Paint paint) {
        canvas.drawBitmap(mState.mBitmap, null, bounds, paint);
    }

    /**
     * Class <tt>InvertedBitmapState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static final class InvertedBitmapState extends AbstractDrawable.BaseConstantState {
        /* package */ final int mAlpha;
        /* package */ final int mDirection;
        /* package */ final float mPercent;
        /* package */ final Bitmap mBitmap;

        /**
         * Constructor
         * @param bitmap The original {@link Bitmap} to create the inverted bitmap.
         * @param alpha The alpha component [0..255] of the inverted bitmap.
         * @param percent The percentage, expressed as a percentage of the <em>bitmap's</em>
         * width or height.
         * @param direction The direction. One of {@link Gravity#LFET}, {@link Gravity#TOP},
         * {@link Gravity#RIGHT} or {@link Gravity#BOTTOM}.
         * @see #InvertedBitmapState(View, int, float, int)
         */
        public InvertedBitmapState(Bitmap bitmap, int alpha, float percent, int direction) {
            mBitmap  = BitmapUtils.createInvertedBitmap(bitmap, alpha, percent, direction, mPaint);
            mAlpha   = alpha;
            mPercent = percent;
            mDirection = direction;
        }

        /**
         * Constructor
         * @param view The {@link View} to create the inverted bitmap.
         * @param alpha The alpha component [0..255] of the inverted bitmap.
         * @param percent The percentage, expressed as a percentage of the <em>view's</em>
         * width or height.
         * @param direction The direction. One of {@link Gravity#LFET}, {@link Gravity#TOP},
         * {@link Gravity#RIGHT} or {@link Gravity#BOTTOM}.
         * @see #InvertedBitmapState(Bitmap, int, float, int)
         */
        public InvertedBitmapState(View view, int alpha, float percent, int direction) {
            mBitmap  = BitmapUtils.createInvertedBitmap(view, alpha, percent, direction, mPaint);
            mAlpha   = alpha;
            mPercent = percent;
            mDirection = direction;
        }

        @Override
        public Drawable newDrawable() {
            throw new UnsupportedOperationException("newDrawable() is not supported in InvertedBitmapState");
        }

        public final void setSource(Object source) {
            final Canvas canvas = new Canvas(mBitmap);
            mBitmap.eraseColor(0);
            if (source instanceof View) {
                DrawUtils.drawInvertedBitmap(canvas, (View)source, mAlpha, mPercent, mDirection, mPaint);
            } else {
                DrawUtils.drawInvertedBitmap(canvas, (Bitmap)source, mAlpha, mPercent, mDirection, mPaint);
            }

            canvas.setBitmap(null);
        }
    }
}
