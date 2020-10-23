package android.ext.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.content.res.XmlResources;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.widget.ImageView;
import java.io.IOException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Class RoundedBitmapDrawable
 * @author Garfield
 */
public class RoundedBitmapDrawable extends ShapeBitmapDrawable<RoundedBitmapDrawable.RoundedBitmapState> {
    /**
     * The default constructor used by {@link Resources#getDrawable(int)},
     * do not call this method directly.
     * @hide
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
        if (mState.mRadii != radii) {
            if (radiusEquals(radii, 0, +0.0f)) {
                mState.mRadii = null;
                invalidateSelf(mState.mShader, false);
            } else {
                mState.mRadii = radii;
                invalidateSelf(mState.mShader, true);
            }
        }
    }

    @Override
    protected ConstantState copyConstantState() {
        return new RoundedBitmapState(mState);
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
     * Equivalent to calling <tt>setBitmap(view, view.getDrawable(), bitmap, radii)</tt>.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param bitmap The <tt>Bitmap</tt> to set. Never <tt>null</tt>.
     * @param radii An array of 8 values, 4 pairs of [X,Y] radii.
     * @see #setBitmap(ImageView, Drawable, Bitmap, float[])
     */
    public static void setBitmap(ImageView view, Bitmap bitmap, float[] radii) {
        DebugUtils.__checkError(view == null || bitmap == null, "Invalid parameters - view == null || bitmap == null");
        setBitmap(view, view.getDrawable(), bitmap, radii);
    }

    /**
     * Sets a {@link Bitmap} as the content of the {@link ImageView}. This method
     * will be reuse the <em>view's</em> original <tt>RoundedBitmapDrawable</tt>.
     * Allows us to avoid allocating new <tt>RoundedBitmapDrawable</tt> in many cases.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param origDrawable The <em>view's</em> original drawable or <tt>null</tt>.
     * @param bitmap The <tt>Bitmap</tt> to set. Never <tt>null</tt>.
     * @param radii An array of 8 values, 4 pairs of [X,Y] radii.
     * @see #setBitmap(ImageView, Bitmap, float[])
     */
    public static void setBitmap(ImageView view, Drawable origDrawable, Bitmap bitmap, float[] radii) {
        DebugUtils.__checkError(view == null || bitmap == null, "Invalid parameters - view == null || bitmap == null");
        if (origDrawable instanceof RoundedBitmapDrawable) {
            final RoundedBitmapDrawable drawable = (RoundedBitmapDrawable)origDrawable;
            drawable.setBitmap(bitmap);
            drawable.setCornerRadii(radii);

            // Force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(drawable);
        } else {
            view.setImageDrawable(new RoundedBitmapDrawable(bitmap, radii));
        }
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
            mRadii = state.mRadii;
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
