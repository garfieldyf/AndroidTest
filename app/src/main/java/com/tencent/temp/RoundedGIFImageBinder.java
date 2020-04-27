package com.tencent.temp;

import android.content.Context;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.RoundedGIFDrawable;
import android.ext.image.ImageModule;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.ImageView;
import java.util.Arrays;

/**
 * Class <tt>RoundedGIFImageBinder</tt> used to transforms a {@link GIFImage}
 * to a {@link RoundedGIFDrawable} to bind to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;RoundedGIFImageBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:radius="20dp"
 *     android:topLeftRadius="20dp"
 *     android:topRightRadius="20dp"
 *     android:bottomLeftRadius="20dp"
 *     android:bottomRightRadius="20dp" /&gt;</pre>
 * @author Garfield
 */
public final class RoundedGIFImageBinder implements Binder<Object, Object, GIFImage> {
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
     * @see #RoundedGIFImageBinder(Context, AttributeSet)
     * @see #RoundedGIFImageBinder(float, float, float, float)
     */
    public RoundedGIFImageBinder(float[] radii) {
        mRadii = radii;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #RoundedGIFImageBinder(float[])
     * @see #RoundedGIFImageBinder(float, float, float, float)
     */
    @Keep
    public RoundedGIFImageBinder(Context context, AttributeSet attrs) {
        mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
    }

    /**
     * Constructor
     * @param topLeftRadius The top-left corner radius.
     * @param topRightRadius The top-right corner radius.
     * @param bottomLeftRadius The bottom-left corner radius.
     * @param bottomRightRadius The bottom-right corner radius.
     * @see #RoundedGIFImageBinder(float[])
     * @see #RoundedGIFImageBinder(Context, AttributeSet)
     */
    public RoundedGIFImageBinder(float topLeftRadius, float topRightRadius, float bottomLeftRadius, float bottomRightRadius) {
        mRadii = new float[] { topLeftRadius, topLeftRadius, topRightRadius, topRightRadius, bottomRightRadius, bottomRightRadius, bottomLeftRadius, bottomLeftRadius };
    }

    public final void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { radii = ").append(Arrays.toString(mRadii))
            .append(" }").toString());
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, GIFImage image, int state) {
        final ImageView view = (ImageView)target;
        if (image == null) {
            view.setImageDrawable(ImageModule.getPlaceholder(view.getResources(), params));
        } else {
            final Drawable oldDrawable = view.getDrawable();
            if (oldDrawable instanceof RoundedGIFDrawable) {
                // Sets the RoundedGIFDrawable's internal image.
                final RoundedGIFDrawable drawable = (RoundedGIFDrawable)oldDrawable;
                //drawable.setImage(image);
                drawable.setCornerRadii(mRadii);

                // Clear the ImageView's content to force update the ImageView's mDrawable.
                view.setImageDrawable(null);
                view.setImageDrawable(drawable);
            } else {
                view.setImageDrawable(new RoundedGIFDrawable(image, mRadii));
            }
        }
    }
}