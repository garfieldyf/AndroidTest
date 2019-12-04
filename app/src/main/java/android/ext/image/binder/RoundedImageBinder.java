package android.ext.image.binder;

import android.content.Context;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.graphics.drawable.RoundedGIFDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import java.util.Arrays;

/**
 * Class <tt>RoundedImageBinder</tt> used to transforms an image
 * to a <tt>RoundedXXXDrawable</tt> to bind the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;RoundedImageBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *      android:radius="20dp"
 *      android:topLeftRadius="20dp"
 *      android:topRightRadius="20dp"
 *      android:bottomLeftRadius="20dp"
 *      android:bottomRightRadius="20dp" /&gt;</pre>
 * @author Garfield
 */
public class RoundedImageBinder implements Binder<Object, Object, Object> {
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
     * @see #RoundedImageBinder(Context, AttributeSet)
     * @see #RoundedImageBinder(float, float, float, float)
     */
    public RoundedImageBinder(float[] radii) {
        mRadii = radii;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #RoundedImageBinder(float[])
     * @see #RoundedImageBinder(float, float, float, float)
     */
    public RoundedImageBinder(Context context, AttributeSet attrs) {
        mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
    }

    /**
     * Constructor
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #RoundedImageBinder(float[])
     * @see #RoundedImageBinder(Context, AttributeSet)
     */
    public RoundedImageBinder(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        final ImageView view = (ImageView)target;
        if (value == null) {
            view.setScaleType(ScaleType.CENTER);
            view.setImageDrawable(ImageModule.getPlaceholder(params));
        } else {
            view.setScaleType(ScaleType.FIT_XY);
            if (value instanceof Bitmap) {
                view.setImageDrawable(new RoundedBitmapDrawable((Bitmap)value, mRadii));
            } else {
                view.setImageDrawable(new RoundedGIFDrawable((GIFImage)value, mRadii));
            }
        }
    }

    public final void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { radii = ").append(Arrays.toString(mRadii))
            .append(" }").toString());
    }
}
