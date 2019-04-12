package android.ext.image.transformer;

import android.content.Context;
import android.ext.content.XmlResources;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * Class <tt>RoundedRectTransformer</tt> used to transforms a {@link Bitmap} to a {@link RoundedBitmapDrawable}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;RoundedRectTransformer xmlns:android="http://schemas.android.com/apk/res/android"
 *      android:radius="20dp"
 *      android:topLeftRadius="20dp"
 *      android:topRightRadius="20dp"
 *      android:bottomLeftRadius="20dp"
 *      android:bottomRightRadius="20dp" /&gt;</pre>
 * @author Garfield
 */
public class RoundedRectTransformer<URI> implements Transformer<URI, Bitmap> {
    /**
     * The corner radii, array of 8 values. Each corner receives two
     * radius values [X, Y]. The corners are ordered <tt>top-left</tt>,
     * <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     */
    protected final float[] mRadii;

    /**
     * Constructor
     * @param radii The corner radii, array of 8 values. Each corner receives two radius values [X, Y]. The
     * corners are ordered <tt>top-left</tt>, <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @see #RoundedRectTransformer(Context, AttributeSet)
     * @see #RoundedRectTransformer(float, float, float, float)
     */
    public RoundedRectTransformer(float[] radii) {
        mRadii = radii;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #RoundedRectTransformer(float[])
     * @see #RoundedRectTransformer(float, float, float, float)
     */
    public RoundedRectTransformer(Context context, AttributeSet attrs) {
        mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
    }

    /**
     * Constructor
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #RoundedRectTransformer(float[])
     * @see #RoundedRectTransformer(Context, AttributeSet)
     */
    public RoundedRectTransformer(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
    }

    @Override
    public Drawable transform(URI uri, Bitmap bitmap) {
        return new RoundedBitmapDrawable(bitmap, mRadii);
    }
}
