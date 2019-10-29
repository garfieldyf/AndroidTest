package android.ext.graphics.drawable;

import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.content.res.XmlResources;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.util.AttributeSet;

/**
 * Class RoundedBitmapDrawable
 * @author Garfield
 */
public class RoundedBitmapDrawable extends ShapeBitmapDrawable<RoundedBitmapDrawable.RoundedBitmapState> {
    /**
     * Constructor
     * <p>The default constructor used by {@link Resources#getDrawable(int)}.</p>
     */
    @Keep
    public RoundedBitmapDrawable() {
        super(new RoundedBitmapState((Bitmap)null));
    }

    /**
     * Constructor
     * @param bitmap The {@link Bitmap}. Never <tt>null</tt>.
     * @see #RoundedBitmapDrawable(Bitmap, float)
     * @see #RoundedBitmapDrawable(Bitmap, float[])
     * @see #RoundedBitmapDrawable(Bitmap, float, float, float, float)
     */
    public RoundedBitmapDrawable(Bitmap bitmap) {
        super(new RoundedBitmapState(bitmap));
    }

    /**
     * Constructor
     * @param bitmap The {@link Bitmap}. Never <tt>null</tt>.
     * @param cornerRadius The corner radius.
     * @see #RoundedBitmapDrawable(Bitmap)
     * @see #RoundedBitmapDrawable(Bitmap, float[])
     * @see #RoundedBitmapDrawable(Bitmap, float, float, float, float)
     */
    public RoundedBitmapDrawable(Bitmap bitmap, float cornerRadius) {
        super(new RoundedBitmapState(bitmap));
        setCornerRadii(cornerRadius);
    }

    /**
     * Constructor
     * @param bitmap The {@link Bitmap}. Never <tt>null</tt>.
     * @param radii The corner radii, array of 8 values. Each corner receives two radius
     * values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
     * <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @see #RoundedBitmapDrawable(Bitmap)
     * @see #RoundedBitmapDrawable(Bitmap, float)
     * @see #RoundedBitmapDrawable(Bitmap, float, float, float, float)
     */
    public RoundedBitmapDrawable(Bitmap bitmap, float[] radii) {
        super(new RoundedBitmapState(bitmap));
        setCornerRadii(radii);
    }

    /**
     * Constructor
     * @param bitmap The {@link Bitmap}. Never <tt>null</tt>.
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #RoundedBitmapDrawable(Bitmap)
     * @see #RoundedBitmapDrawable(Bitmap, float)
     * @see #RoundedBitmapDrawable(Bitmap, float[])
     */
    public RoundedBitmapDrawable(Bitmap bitmap, float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        super(new RoundedBitmapState(bitmap));
        setCornerRadii(topLeftRadius, topRightRadius, bottomLeftRadius, bottomRightRadius);
    }

    /**
     * Return this drawable's corner radii. You should <b>not</b> change the object
     * returned by this method as it may be the same object stored in this drawable.
     * @return The corner radii of this drawable, array of 8 values, 4 pairs of [X,Y]
     * radii, or <tt>null</tt> if none set.
     */
    public float[] getCornerRadii() {
        return mState.mRadii;
    }

    /**
     * Sets the corner radii to be applied when drawing the bitmap.
     * @param cornerRadius The corner radius.
     * @see #setCornerRadii(float[])
     * @see #setCornerRadii(float, float, float, float)
     */
    public final void setCornerRadii(float cornerRadius) {
        setCornerRadii(new float[] { cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius });
    }

    /**
     * Sets the corner radii to be applied when drawing the bitmap.
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #setCornerRadii(float)
     * @see #setCornerRadii(float[])
     */
    public final void setCornerRadii(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        setCornerRadii(new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius });
    }

    /**
     * Sets the corner radii to be applied when drawing the bitmap. Each corner receives two
     * radius values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
     * <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @param radii An array of 8 values, 4 pairs of [X,Y] radii, or <tt>null</tt> to clear.
     * @see #setCornerRadii(float)
     * @see #setCornerRadii(float, float, float, float)
     */
    public void setCornerRadii(float[] radii) {
        if (radiusEquals(radii, 0, +0.0f)) {
            mState.mRadii = null;
            invalidateSelf(false);
        } else {
            mState.mRadii = radii;
            invalidateSelf(true);
        }
    }

    @Override
    public Drawable mutate() {
        if ((mFlags & FLAG_MUTATED) == 0) {
            mFlags |= FLAG_MUTATED;
            mState = new RoundedBitmapState(mState);
        }

        return this;
    }

    @Override
    protected void getConvexPath(RectF bounds, Path outPath) {
        outPath.addRoundRect(bounds, mState.mRadii, Direction.CW);
    }

    @Override
    protected void getOutline(Outline outline, RectF bounds) {
        getOutline(outline, mState.mRadii, mState.mPath, bounds);
    }

    @Override
    protected void inflateAttributes(Resources res, XmlPullParser parser, AttributeSet attrs, Theme theme, int id) throws XmlPullParserException, IOException {
        super.inflateAttributes(res, parser, attrs, theme, id);
        setCornerRadii(XmlResources.loadCornerRadii(res, attrs));
    }

    /**
     * Constructor
     * <p>The constructor used by {@link ConstantState#newDrawable()}.</p>
     */
    /* package */ RoundedBitmapDrawable(RoundedBitmapState state) {
        super(state);
    }

    /**
     * Class <tt>RoundedBitmapState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static final class RoundedBitmapState extends ShapeBitmapDrawable.BitmapState {
        /* package */ float[] mRadii;

        /**
         * Constructor
         * @param bitmap The {@link Bitmap}.
         * @see #RoundedBitmapState(RoundedBitmapState)
         */
        public RoundedBitmapState(Bitmap bitmap) {
            super(bitmap);
        }

        /**
         * Copy constructor
         * @param state The {@link RoundedBitmapState}.
         * @see #RoundedBitmapState(Bitmap)
         */
        public RoundedBitmapState(RoundedBitmapState state) {
            super(state);
            if (state.mRadii != null) {
                mRadii = state.mRadii.clone();
            }
        }

        @Override
        public Drawable newDrawable() {
            return new RoundedBitmapDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new RoundedBitmapDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new RoundedBitmapDrawable(this);
        }
    }
}
