package android.ext.image.binder;

import android.content.Context;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.image.ImageModule;
import android.ext.util.DebugUtils;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.ImageView;

/**
 * Class <tt>GIFImageBinder</tt> converts a {@link GIFImage} to
 * a {@link GIFDrawable} and bind it to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;GIFImageBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:oneshot="false"
 *     android:autoStart="true" /&gt;</pre>
 * @author Garfield
 */
public class GIFImageBinder implements Binder<Object, Object, GIFImage> {
    protected final boolean mOneShot;
    protected final boolean mAutoStart;

    /**
     * Constructor
     * @param autoStart <tt>true</tt> if the animation should auto play.
     * @param oneShot <tt>true</tt> if the animation should only play once.
     * @see #GIFImageBinder(Context, AttributeSet)
     */
    public GIFImageBinder(boolean autoStart, boolean oneShot) {
        mOneShot   = oneShot;
        mAutoStart = autoStart;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #GIFImageBinder(boolean, boolean)
     */
    public GIFImageBinder(Context context, AttributeSet attrs) {
        final boolean[] results = XmlResources.loadAnimationAttrs(context.getResources(), attrs);
        mOneShot   = results[0]; // android.R.attr.oneshot
        mAutoStart = results[1]; // android.R.attr.autoStart
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, GIFImage image, int state) {
        final ImageView view = (ImageView)target;
        if (image != null) {
            setViewImage(view, image, mAutoStart, mOneShot);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            ImageModule.setPlaceholder(view, params);
        }
    }

    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { autoStart = ").append(mAutoStart)
            .append(", oneShot = ").append(mOneShot)
            .append(" }").toString());
    }

    /**
     * Sets a {@link GIFImage} as the content of the {@link ImageView}.
     * @param view The <tt>ImageView</tt>.
     * @param image The <tt>GIFImage</tt> to set. Never <tt>null</tt>.
     * @param autoStart <tt>true</tt> if the animation should auto play.
     * @param oneShot <tt>true</tt> if the animation should only play once.
     */
    public static void setViewImage(ImageView view, GIFImage image, boolean autoStart, boolean oneShot) {
        DebugUtils.__checkError(image == null, "Invalid parameter - image == null");
        final Drawable oldDrawable = view.getDrawable();
        if (oldDrawable instanceof GIFDrawable) {
            final GIFDrawable drawable = (GIFDrawable)oldDrawable;
            final boolean isRunning = drawable.isRunning();
            drawable.setImage(image);

            // Force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(drawable);

            if (isRunning) {
                drawable.start();
            }
        } else {
            final GIFDrawable drawable = new GIFDrawable(image);
            drawable.setOneShot(oneShot);
            drawable.setAutoStart(autoStart);
            view.setImageDrawable(drawable);
        }
    }
}
