package android.ext.image.binder;

import static android.ext.image.ImageModule.getPlaceholder;
import android.content.Context;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.util.DebugUtils;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.ImageView;

/**
 * Class <tt>GIFImageBinder</tt> used to transforms a {@link GIFImage}
 * to a {@link GIFDrawable} to bind to the {@link ImageView}.
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

    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { autoStart = ").append(mAutoStart)
            .append(", oneShot = ").append(mOneShot)
            .append(" }").toString());
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, GIFImage image, int state) {
        final ImageView view = (ImageView)target;
        if (image != null) {
            setViewImage(view, image);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            view.setImageDrawable(getPlaceholder(view.getResources(), params));
        }
    }

    /**
     * Sets a {@link GIFImage} as the content of the {@link ImageView}.
     * @param view The <tt>ImageView</tt>.
     * @param image The <tt>GIFImage</tt> to set. Never <tt>null</tt>.
     */
    protected void setViewImage(ImageView view, GIFImage image) {
        DebugUtils.__checkError(image == null, "image == null");
        final Drawable oldDrawable = view.getDrawable();
        if (oldDrawable instanceof GIFDrawable) {
            // Sets the GIFDrawable's internal image.
            final GIFDrawable drawable = (GIFDrawable)oldDrawable;
            final boolean isRunning = drawable.isRunning();
            drawable.setImage(image);

            // Clear the ImageView's content to force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(drawable);

            if (isRunning) {
                drawable.start();
            }
        } else {
            final GIFDrawable drawable = new GIFDrawable(image);
            drawable.setOneShot(mOneShot);
            drawable.setAutoStart(mAutoStart);
            view.setImageDrawable(drawable);
        }
    }
}
