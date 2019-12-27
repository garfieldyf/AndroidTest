package android.ext.image.binder;

import android.content.Context;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.ImageView;
import java.util.Arrays;

/**
 * Class <tt>RoundedBitmapBinder</tt> used to transforms a {@link Bitmap}
 * to a {@link RoundedBitmapDrawable} to bind to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;RoundedBitmapBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:radius="20dp"
 *     android:topLeftRadius="20dp"
 *     android:topRightRadius="20dp"
 *     android:bottomLeftRadius="20dp"
 *     android:bottomRightRadius="20dp" /&gt;</pre>
 * @author Garfield
 */
public final class RoundedBitmapBinder implements Binder<Object, Object, Bitmap> {
    /**
     * The corner radii, array of 8 values. Each corner receives two
     * radius values [X, Y]. The corners are ordered <tt>top-left</tt>,
     * <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     */
    private final float[] mRadii;

    /**
     * Constructor
     * @param radii The corner radii, array of 8 values. Each corner receives two radius values [X, Y]. The
     * corners are ordered <tt>top-left</tt>, <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @see #RoundedBitmapBinder(Context, AttributeSet)
     * @see #RoundedBitmapBinder(float, float, float, float)
     */
    public RoundedBitmapBinder(float[] radii) {
        mRadii = radii;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #RoundedBitmapBinder(float[])
     * @see #RoundedBitmapBinder(float, float, float, float)
     */
    public RoundedBitmapBinder(Context context, AttributeSet attrs) {
        mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
    }

    /**
     * Constructor
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #RoundedBitmapBinder(float[])
     * @see #RoundedBitmapBinder(Context, AttributeSet)
     */
    public RoundedBitmapBinder(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
    }

    public final void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { radii = ").append(Arrays.toString(mRadii))
            .append(" }").toString());
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Bitmap bitmap, int state) {
        final ImageView view = (ImageView)target;
        if (bitmap != null) {
            view.setImageDrawable(new RoundedBitmapDrawable(bitmap, mRadii));
        } else {
            view.setImageDrawable(ImageModule.getPlaceholder(view.getResources(), params));
        }
    }
}
