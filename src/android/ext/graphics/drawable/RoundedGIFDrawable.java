package android.ext.graphics.drawable;

import android.annotation.TargetApi;
import android.ext.graphics.GIFImage;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.os.Build;

/**
 * Class RoundedGIFDrawable
 * @author Garfield
 */
public class RoundedGIFDrawable extends ShapeGIFDrawable {
    private float[] mRadii;

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     * @see #RoundedGIFDrawable(GIFImage, float)
     * @see #RoundedGIFDrawable(GIFImage, float[])
     */
    public RoundedGIFDrawable(GIFImage image) {
        super(image);
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     * @param cornerRadius The corner radius.
     * @see #RoundedGIFDrawable(GIFImage)
     * @see #RoundedGIFDrawable(GIFImage, float[])
     */
    public RoundedGIFDrawable(GIFImage image, float cornerRadius) {
        super(image);
        setCornerRadius(cornerRadius);
    }

    /**
     * Constructor
     * @param image The {@link GIFImage}. Never <tt>null</tt>.
     * @param radii The corner radii, array of 8 values. Each corner receives two radius
     * values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
     * <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @see #RoundedGIFDrawable(GIFImage)
     * @see #RoundedGIFDrawable(GIFImage, float)
     */
    public RoundedGIFDrawable(GIFImage image, float[] radii) {
        super(image);
        setCornerRadii(radii);
    }

    /**
     * Sets the corner radius to be applied when drawing this GIF drawable.
     * @param cornerRadius The corner radius.
     * @see #setCornerRadius(float, float, float, float)
     */
    public final void setCornerRadius(float cornerRadius) {
        setCornerRadii(new float[] { cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius, cornerRadius });
    }

    /**
     * Sets the corner radius to be applied when drawing this GIF drawable.
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #setCornerRadius(float)
     */
    public final void setCornerRadius(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        setCornerRadii(new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius });
    }

    /**
     * Return this drawable's corner radii. You should <b>not</b> change the object
     * returned by this method as it may be the same object stored in this drawable.
     * @return The corner radii of this drawable, array of 8 values, 4 pairs of [X,Y]
     * radii, or <tt>null</tt> if none set.
     * @see #setCornerRadii(float[])
     */
    public float[] getCornerRadii() {
        return mRadii;
    }

    /**
     * Sets the corner radii to be applied when drawing this GIF drawable. Each corner receives
     * two radius values [X, Y]. The corners are ordered <tt>top-left</tt>, <tt>top-right</tt>,
     * <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @param radii An array of 8 values, 4 pairs of [X,Y] radii, or <tt>null</tt> to clear.
     * @see #getCornerRadii()
     */
    public void setCornerRadii(float[] radii) {
        if (radiusEquals(radii, 0, +0.0f)) {
            mRadii = null;
            invalidateSelf(false);
        } else {
            mRadii = radii;
            invalidateSelf(true);
        }
    }

    @Override
    protected void getConvexPath(RectF bounds, Path outPath) {
        outPath.addRoundRect(bounds, mRadii, Direction.CW);
    }

    @Override
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void getOutline(Outline outline, RectF bounds) {
        if (mRadii == null) {
            outline.setRect((int)bounds.left, (int)bounds.top, (int)bounds.right, (int)bounds.bottom);
        } else {
            final float radius = mRadii[0];
            if (radiusEquals(mRadii, 1, radius)) {
                // Round rect all corner radii are equals, for efficiency, and to enable clipping.
                outline.setRoundRect((int)bounds.left, (int)bounds.top, (int)bounds.right, (int)bounds.bottom, radius);
            } else {
                super.getOutline(outline, bounds);
            }
        }
    }
}
