package android.ext.image.binder;

import android.content.Context;
import android.ext.content.res.XmlResources;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.ImageView;
import java.util.Arrays;

/**
 * Class <tt>RoundedTransitionBinder</tt> converts a {@link Bitmap} to a
 * {@link RoundedBitmapDrawable} and play transition animation when the
 * drawable bind to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;RoundedTransitionBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:duration="@android:integer/config_longAnimTime"
 *     android:radius="20dp"
 *     android:topLeftRadius="20dp"
 *     android:topRightRadius="20dp"
 *     android:bottomLeftRadius="20dp"
 *     android:bottomRightRadius="20dp"
 *     app:crossFade="true" /&gt;</pre>
 * @author Garfield
 */
public class RoundedTransitionBinder extends TransitionBinder {
    protected final float[] mRadii;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #RoundedTransitionBinder(float[], int, boolean)
     * @see #RoundedTransitionBinder(float, float, float, float, int, boolean)
     */
    public RoundedTransitionBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
    }

    /**
     * Constructor
     * @param radii The corner radii, array of 8 values. Each corner receives two radius values [X, Y]. The
     * corners are ordered <tt>top-left</tt>, <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @param durationMillis The length of the transition in milliseconds.
     * @param crossFade Enables or disables the cross fade of the drawables.
     * @see #RoundedTransitionBinder(Context, AttributeSet)
     * @see #RoundedTransitionBinder(float, float, float, float, int, boolean)
     * @see TransitionDrawable#setCrossFadeEnabled(boolean)
     */
    public RoundedTransitionBinder(float[] radii, int durationMillis, boolean crossFade) {
        super(durationMillis, crossFade);
        mRadii = radii;
    }

    /**
     * Constructor
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @param durationMillis The length of the transition in milliseconds.
     * @param crossFade Enables or disables the cross fade of the drawables.
     * @see #RoundedTransitionBinder(Context, AttributeSet)
     * @see #RoundedTransitionBinder(float[], int, boolean)
     * @see TransitionDrawable#setCrossFadeEnabled(boolean)
     */
    public RoundedTransitionBinder(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius, int durationMillis, boolean crossFade) {
        super(durationMillis, crossFade);
        mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { duration = ").append(mDuration)
            .append(", crossFade = ").append(mCrossFade)
            .append(", radii = ").append(Arrays.toString(mRadii))
            .append(" }").toString());
    }

    @Override
    protected Drawable newDrawable(ImageView view, Bitmap bitmap) {
        return new RoundedBitmapDrawable(bitmap, mRadii);
    }

    @Override
    protected void setImageBitmap(ImageView view, Bitmap bitmap) {
        RoundedBitmapDrawable.setBitmap(view, getDrawable(view), bitmap, mRadii);
    }
}
