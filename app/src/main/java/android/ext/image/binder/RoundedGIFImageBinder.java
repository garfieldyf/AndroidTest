package android.ext.image.binder;

import android.content.Context;
import android.ext.content.res.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.RoundedGIFDrawable;
import android.ext.image.ImageModule;
import android.ext.util.DebugUtils;
import android.graphics.drawable.Drawable;
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
 *     android:oneshot="false"
 *     android:autoStart="true"
 *     android:radius="20dp"
 *     android:topLeftRadius="20dp"
 *     android:topRightRadius="20dp"
 *     android:bottomLeftRadius="20dp"
 *     android:bottomRightRadius="20dp" /&gt;</pre>
 * @author Garfield
 */
public class RoundedGIFImageBinder extends GIFImageBinder {
    protected final float[] mRadii;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #RoundedGIFImageBinder(float[], boolean, boolean)
     */
    public RoundedGIFImageBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
        mRadii = XmlResources.loadCornerRadii(context.getResources(), attrs);
    }

    /**
     * Constructor
     * @param radii The corner radii, array of 8 values. Each corner receives two radius values [X, Y]. The
     * corners are ordered <tt>top-left</tt>, <tt>top-right</tt>, <tt>bottom-right</tt>, <tt>bottom-left</tt>.
     * @param autoStart <tt>true</tt> if the animation should auto play.
     * @param oneShot <tt>true</tt> if the animation should only play once.
     * @see #RoundedGIFImageBinder(Context, AttributeSet)
     */
    public RoundedGIFImageBinder(float[] radii, boolean autoStart, boolean oneShot) {
        super(autoStart, oneShot);
        mRadii = radii;
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, GIFImage image, int state) {
        final ImageView view = (ImageView)target;
        if (image != null) {
            setViewImage(view, image, mRadii, mAutoStart, mOneShot);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            ImageModule.setPlaceholder(view, params);
        }
    }

    @Override
    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { radii = ").append(Arrays.toString(mRadii))
            .append(", autoStart = ").append(mAutoStart)
            .append(", oneShot = ").append(mOneShot)
            .append(" }").toString());
    }

    /**
     * Sets a {@link GIFImage} as the content of the {@link ImageView}.
     * @param view The <tt>ImageView</tt>.
     * @param image The <tt>GIFImage</tt> to set. Never <tt>null</tt>.
     * @param radii The corner radii, array of 8 values.
     * @param autoStart <tt>true</tt> if the animation should auto play.
     * @param oneShot <tt>true</tt> if the animation should only play once.
     */
    public static void setViewImage(ImageView view, GIFImage image, float[] radii, boolean autoStart, boolean oneShot) {
        DebugUtils.__checkError(image == null, "Invalid parameter - image == null");
        final Drawable oldDrawable = view.getDrawable();
        if (oldDrawable instanceof RoundedGIFDrawable) {
            // Sets the RoundedGIFDrawable's internal image.
            final RoundedGIFDrawable drawable = (RoundedGIFDrawable)oldDrawable;
            final boolean isRunning = drawable.isRunning();
            drawable.setImage(image);
            drawable.setCornerRadii(radii);

            // Clear the ImageView's content to force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(drawable);

            if (isRunning) {
                drawable.start();
            }
        } else {
            final RoundedGIFDrawable drawable = new RoundedGIFDrawable(image, radii);
            drawable.setOneShot(oneShot);
            drawable.setAutoStart(autoStart);
            view.setImageDrawable(drawable);
        }
    }
}