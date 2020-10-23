package android.ext.graphics.drawable;

import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.ext.content.res.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.util.DebugUtils;
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
 * Class RoundedGIFDrawable
 * @author Garfield
 */
public class RoundedGIFDrawable extends ShapeGIFDrawable<RoundedGIFDrawable.RoundedGIFState> {
    /**
     * The default constructor used by {@link Resources#getDrawable(int)},
     * do not call this method directly.
     * @hide
     */
    @Keep
    public RoundedGIFDrawable() {
        super(new RoundedGIFState((GIFImage)null));
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     * @see #RoundedGIFDrawable(GIFImage, float)
     * @see #RoundedGIFDrawable(GIFImage, float[])
     * @see #RoundedGIFDrawable(GIFImage, float, float, float, float)
     */
    public RoundedGIFDrawable(GIFImage image) {
        super(new RoundedGIFState(image));
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     * @param cornerRadius The corner radius.
     * @see #RoundedGIFDrawable(GIFImage)
     * @see #RoundedGIFDrawable(GIFImage, float[])
     * @see #RoundedGIFDrawable(GIFImage, float, float, float, float)
     */
    public RoundedGIFDrawable(GIFImage image, float cornerRadius) {
        super(new RoundedGIFState(image));
        setCornerRadii(cornerRadius);
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     * @param radii The corner radii, array of 8 values. Each corner receives two radius
     * values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
     * <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @see #RoundedGIFDrawable(GIFImage)
     * @see #RoundedGIFDrawable(GIFImage, float)
     * @see #RoundedGIFDrawable(GIFImage, float, float, float, float)
     */
    public RoundedGIFDrawable(GIFImage image, float[] radii) {
        super(new RoundedGIFState(image));
        setCornerRadii(radii);
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #RoundedGIFDrawable(GIFImage)
     * @see #RoundedGIFDrawable(GIFImage, float)
     * @see #RoundedGIFDrawable(GIFImage, float[])
     */
    public RoundedGIFDrawable(GIFImage image, float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        super(new RoundedGIFState(image));
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
     * Sets the corner radii to be applied when drawing this GIF drawable.
     * @param cornerRadius The corner radius.
     * @see #setCornerRadii(float[])
     * @see #setCornerRadii(float, float, float, float)
     */
    public final void setCornerRadii(float cornerRadius) {
        setCornerRadii(new float[] { cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius });
    }

    /**
     * Sets the corner radii to be applied when drawing this GIF drawable.
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
     * Sets the corner radii to be applied when drawing this GIF drawable. Each corner receives
     * two radius values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
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
        return new RoundedGIFState(mState);
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
    /* package */ RoundedGIFDrawable(RoundedGIFState state) {
        super(state);
    }

    /**
     * Equivalent to calling <tt>setImage(view, image, radii, true, false)</tt>.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param image The <tt>GIFImage</tt> to set. Never <tt>null</tt>.
     * @param radii An array of 8 values, 4 pairs of [X,Y] radii.
     * @see #setImage(ImageView, GIFImage, float[], boolean, boolean)
     */
    public static void setImage(ImageView view, GIFImage image, float[] radii) {
        DebugUtils.__checkError(view == null || image == null, "Invalid parameters - view == null || image == null");
        setImage(view, image, radii, true, false);
    }

    /**
     * Sets a {@link GIFImage} as the content of the {@link ImageView}. This
     * method will be reuse the <em>view's</em> original <tt>GIFDrawable</tt>.
     * Allows us to avoid allocating new <tt>GIFDrawable</tt> in many cases.
     * @param view The <tt>ImageView</tt>. Never <tt>null</tt>.
     * @param image The <tt>GIFImage</tt> to set. Never <tt>null</tt>.
     * @param radii An array of 8 values, 4 pairs of [X,Y] radii.
     * @param autoStart <tt>true</tt> if the animation should auto play.
     * @param oneShot <tt>true</tt> if the animation should only play once.
     * @see #setImage(ImageView, GIFImage, float[])
     */
    public static void setImage(ImageView view, GIFImage image, float[] radii, boolean autoStart, boolean oneShot) {
        DebugUtils.__checkError(view == null || image == null, "Invalid parameters - view == null || image == null");
        final Drawable origDrawable = view.getDrawable();
        final RoundedGIFDrawable drawable;
        if (origDrawable instanceof RoundedGIFDrawable) {
            drawable = (RoundedGIFDrawable)origDrawable;
            drawable.setImage(image);

            // Force update the ImageView's mDrawable.
            view.setImageDrawable(null);
        } else {
            drawable = new RoundedGIFDrawable(image);
        }

        drawable.setOneShot(oneShot);
        drawable.setCornerRadii(radii);
        drawable.setAutoStart(autoStart);
        view.setImageDrawable(drawable);
    }

    /**
     * Class <tt>RoundedGIFState</tt> is an implementation of a {@link ConstantState}.
     */
    /* package */ static final class RoundedGIFState extends ShapeGIFDrawable.ShapeGIFState {
        /* package */ float[] mRadii;

        /**
         * Constructor
         * @param image The {@link GIFImage}.
         * @see #RoundedGIFState(RoundedGIFState)
         */
        public RoundedGIFState(GIFImage image) {
            super(image);
        }

        /**
         * Copy constructor
         * @param state The {@link RoundedGIFState}.
         * @see #RoundedGIFState(GIFImage)
         */
        public RoundedGIFState(RoundedGIFState state) {
            super(state);
            mRadii = state.mRadii;
        }

        @Override
        public Drawable newDrawable() {
            return new RoundedGIFDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new RoundedGIFDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res, Theme theme) {
            return new RoundedGIFDrawable(this);
        }
    }
}
