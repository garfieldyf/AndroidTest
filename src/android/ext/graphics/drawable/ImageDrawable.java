package android.ext.graphics.drawable;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.content.res.TypedArray;
import android.ext.graphics.DrawUtils;
import android.ext.util.Pools.MatrixPool;
import android.ext.util.Pools.RectFPool;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * Abstract class ImageDrawable
 * @author Garfield
 */
public abstract class ImageDrawable<T extends ImageDrawable.ImageState> extends Drawable {
    /* ------------------ mState.mFlags ------------------ */
    /**
     * Indicates the drawable none mirrored.
     */
    public static final int NONE_MIRRORED = 0;

    /**
     * Indicates the drawable horizontal mirrored.
     */
    public static final int HORIZONTAL_MIRRORED = 0x40000000;

    /**
     * Indicates the drawable vertical mirrored.
     */
    public static final int VERTICAL_MIRRORED = 0x80000000;

    /**
     * The binary mask to get the auto mirrored of a mirror.
     */
    private static final int MIRRORED_MASK = 0xC0000000;

    /* --------------------- mFlags --------------------- */
    /**
     * If set the drawable gravity has been changed.
     */
    protected static final int FLAG_GRAVITY = 0x40000000;

    /**
     * If set the drawable is mutated.
     */
    protected static final int FLAG_MUTATED = 0x80000000;

    /**
     * The image drawable attributes.
     */
    private static final int[] IMAGE_DRAWABLE_ATTRS = {
        android.R.attr.gravity,
        android.R.attr.src,
        android.R.attr.alpha,
        android.R.attr.autoMirrored,
    };

    /**
     * The {@link ConstantState} object.
     */
    protected T mState;

    /**
     * The flags, can be user-defined (must be range of [0 - 0xFFFF]).
     */
    protected int mFlags;

    /**
     * The drawing bounds to draw the content.
     */
    private final RectF mBounds;

    /**
     * Constructor
     * @param state The {@link ImageState}.
     */
    public ImageDrawable(T state) {
        mState  = state;
        mBounds = new RectF();
    }

    /**
     * Returns the {@link Paint} used to render this drawable.
     * @return The {@link Paint}.
     */
    public final Paint getPaint() {
        return mState.mPaint;
    }

    /**
     * Returns the gravity used to position/stretch the contents within its bounds.
     * @return The gravity applied to the contents.
     * @see #setGravity(int)
     * @see android.view.Gravity
     */
    public int getGravity() {
        return mState.mGravity;
    }

    /**
     * Sets the gravity used to position/stretch the contents within its bounds.
     * @param gravity The gravity to apply.
     * @see #getGravity()
     * @see android.view.Gravity
     */
    public void setGravity(int gravity) {
        if (mState.mGravity != gravity) {
            mState.mGravity = gravity;
            mFlags |= FLAG_GRAVITY;
            invalidateSelf();
        }
    }

    /**
     * Returns whether this <tt>Drawable</tt> is automatically mirrored.
     * @return One of {@link #HORIZONTAL_MIRRORED}, {@link #VERTICAL_MIRRORED} or {@link #NONE_MIRRORED}.
     * @see #setAutoMirrored(int)
     */
    public int getAutoMirrored() {
        return (mState.mFlags & MIRRORED_MASK);
    }

    /**
     * Sets whether this <tt>Drawable</tt> is automatically mirrored.
     * @param mirrored One of {@link #HORIZONTAL_MIRRORED}, {@link #VERTICAL_MIRRORED} or {@link #NONE_MIRRORED}.
     * @see #getAutoMirrored()
     */
    public void setAutoMirrored(int mirrored) {
        final int newFlags = (mState.mFlags & ~MIRRORED_MASK) | (mirrored & MIRRORED_MASK);
        if (mState.mFlags != newFlags) {
            mState.mFlags = newFlags;
            invalidateSelf();
        }
    }

    @Override
    public int getAlpha() {
        return mState.mPaint.getAlpha();
    }

    @Override
    public void setAlpha(int alpha) {
        if (mState.mPaint.getAlpha() != alpha) {
            mState.mPaint.setAlpha(alpha);
            invalidateSelf();
        }
    }

    @Override
    public void setDither(boolean dither) {
        mState.mPaint.setDither(dither);
        invalidateSelf();
    }

    @Override
    public void clearColorFilter() {
        mState.mPaint.setColorFilter(null);
        invalidateSelf();
    }

    @Override
    public ColorFilter getColorFilter() {
        return mState.mPaint.getColorFilter();
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        mState.mPaint.setColorFilter(filter);
        invalidateSelf();
    }

    /**
     * Returns whether this drawable filters its bitmap.
     * @return <tt>true</tt> if this drawable filters its
     * bitmap, <tt>false</tt> otherwise.
     */
    @SuppressLint("Override")
    public boolean isFilterBitmap() {
        return ((mState.mPaint.getFlags() & Paint.FILTER_BITMAP_FLAG) != 0);
    }

    @Override
    public void setFilterBitmap(boolean filter) {
        mState.mPaint.setFilterBitmap(filter);
        invalidateSelf();
    }

    @Override
    public boolean isAutoMirrored() {
        return (getAutoMirrored() != NONE_MIRRORED);
    }

    /**
     * @see #setAutoMirrored(int)
     */
    @Override
    public void setAutoMirrored(boolean mirrored) {
        setAutoMirrored(mirrored ? HORIZONTAL_MIRRORED : NONE_MIRRORED);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void getOutline(Outline outline) {
        if (getBounds().isEmpty()) {
            outline.setEmpty();
            outline.setAlpha(0);
        } else {
            computeDrawingBounds(mBounds);
            outline.setAlpha(getAlpha() / 255.0f);
            getOutline(outline, mBounds);
        }
    }

    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    @Override
    public void draw(Canvas canvas) {
        computeDrawingBounds(mBounds);
        switch (getAutoMirrored()) {
        case VERTICAL_MIRRORED:
            draw(canvas, 0, getBounds().height(), 1.0f);
            break;

        case HORIZONTAL_MIRRORED:
            draw(canvas, getBounds().width(), 0, -1.0f);
            break;

        default:
            draw(canvas, mBounds, mState.mPaint);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void inflate(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme) throws XmlPullParserException, IOException {
        final TypedArray a = res.obtainAttributes(attrs, IMAGE_DRAWABLE_ATTRS);
        inflateAttributes(res, parser, attrs, theme, a.getResourceId(1 /* android.R.attr.src */, 0));

        mState.mGravity = a.getInt(0 /* android.R.attr.gravity */, Gravity.FILL);
        mState.mPaint.setAlpha((int)(a.getFloat(2 /* android.R.attr.alpha */, 1) * 255 + 0.5f));
        mState.mFlags |= (IMAGE_DRAWABLE_ATTRS[3] == android.R.attr.autoMirrored ? (a.getBoolean(3 /* android.R.attr.autoMirrored */, false) ? HORIZONTAL_MIRRORED : NONE_MIRRORED) : a.getInt(3 /* R.attr.autoMirrored */, NONE_MIRRORED));
        a.recycle();

        super.inflate(res, parser, attrs, theme);
    }

    /**
     * Initialize the {@link ImageDrawable} attributes. <p>Note: This method recommended call in the
     * <tt>Application's</tt> static constructor.</p>
     * @param autoMirroredAttr The <tt>R.attr.autoMirrored</tt> attribute, as generated by the aapt tool.
     */
    public static void initAttrs(int autoMirroredAttr) {
        IMAGE_DRAWABLE_ATTRS[3] = autoMirroredAttr;
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        mFlags |= FLAG_GRAVITY;
    }

    /**
     * Draws the contents of this drawable in the <em>bounds</em>.
     * @param canvas The canvas to draw into.
     * @param bounds The frame of the contents of this drawable.
     * @param paint The paint used to draw the contents of this drawable.
     */
    protected abstract void draw(Canvas canvas, RectF bounds, Paint paint);

    /**
     * Called to get this drawable to populate the {@link Outline} that defines its drawing area.
     * @param outline The empty <tt>Outline</tt> to be populated.
     * @param bounds The frame of the contents of this drawable.
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void getOutline(Outline outline, RectF bounds) {
        outline.setRect((int)bounds.left, (int)bounds.top, (int)bounds.right, (int)bounds.bottom);
    }

    /**
     * Inflate this drawable's attributes from an XML resource.
     * @param res The <tt>Resources</tt>.
     * @param parser The XML parser from which to inflate this drawable.
     * @param attrs The base set of attribute values.
     * @param theme The <tt>Theme</tt> to apply, may be <tt>null</tt>.
     * @param id The resource id of 'src' attribute, may be <tt>0</tt>.
     */
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
    }

    /**
     * Computes this drawable's drawing bounds to draw the content. The returned
     * <em>outBounds</em> may not equals to this drawable's bounds. <p>By default,
     * this returns bounds computed with this drawable's gravity and bounds.</p>
     * @param outBounds The {@link RectF} to compute.
     */
    /* package */ void computeDrawingBounds(RectF outBounds) {
        if ((mFlags & FLAG_GRAVITY) != 0) {
            mFlags &= ~FLAG_GRAVITY;
            DrawUtils.applyGravity(mState.mGravity, getIntrinsicWidth(), getIntrinsicHeight(), getBounds(), outBounds);
        }
    }

    /**
     * Draws the contents of this drawable with the specified translation and scale.
     */
    private void draw(Canvas canvas, float dx, float dy, float scale) {
        final int saveCount = canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, -scale);
        draw(canvas, mBounds, mState.mPaint);
        canvas.restoreToCount(saveCount);
    }

    /**
     * Compares the specified <em>radius</em> to equals
     * the each element in the <em>radii</em> array.
     */
    /* package */ static boolean radiusEquals(float[] radii, int start, float radius) {
        if (radii != null && radii.length >= 8) {
            for (; start < 8; ++start) {
                if (Float.compare(radii[start], radius) != 0) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Sets the {@link Shader}'s local matrix.
     */
    /* package */ static void setShaderMatrix(Shader shader, int width, int height, RectF bounds) {
        // Computes the scale value that map the source
        // rectangle to the destination rectangle.
        final RectF src = RectFPool.sInstance.obtain(0, 0, width, height);
        final Matrix matrix = MatrixPool.sInstance.obtain();
        matrix.setRectToRect(src, bounds, ScaleToFit.FILL);

        // Sets the shader scale matrix.
        shader.setLocalMatrix(matrix);
        RectFPool.sInstance.recycle(src);
        MatrixPool.sInstance.recycle(matrix);
    }

    /**
     * Class <tt>ImageState</tt> is an implementation of a {@link ConstantState}.
     */
    public static abstract class ImageState extends ConstantState {
        /* package */ int mFlags;
        /* package */ int mGravity;
        /* package */ final Paint mPaint;

        /**
         * Constructor
         * @see #ImageState(ImageState)
         */
        public ImageState() {
            mPaint   = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG);
            mGravity = Gravity.FILL;
        }

        /**
         * Copy constructor
         * @param state The {@link ImageState}.
         * @see #ImageState()
         */
        public ImageState(ImageState state) {
            mGravity = state.mGravity;
            mFlags   = state.mFlags;
            mPaint   = new Paint(state.mPaint);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

        /* package */ final void setFlags(boolean set, int flags) {
            if (set) {
                mFlags |= flags;
            } else {
                mFlags &= ~flags;
            }
        }
    }
}