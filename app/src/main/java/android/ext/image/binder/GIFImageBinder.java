package android.ext.image.binder;

import static android.ext.graphics.drawable.GIFBaseDrawable.GIF_DRAWABLE_ATTRS;
import static android.ext.image.ImageModule.getPlaceholder;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
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
public final class GIFImageBinder implements Binder<Object, Object, GIFImage> {
    private final boolean mOneShot;
    private final boolean mAutoStart;

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
    @SuppressLint("ResourceType")
    public GIFImageBinder(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, GIF_DRAWABLE_ATTRS);
        mOneShot   = a.getBoolean(0 /* android.R.attr.oneshot */, false);
        mAutoStart = a.getBoolean(1 /* android.R.attr.autoStart */, true);
        a.recycle();
    }

    public final void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { autoStart = ").append(mAutoStart)
            .append(", oneShot = ").append(mOneShot)
            .append(" }").toString());
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, GIFImage image, int state) {
        final ImageView view = (ImageView)target;
        if (image == null) {
            view.setImageDrawable(getPlaceholder(view.getResources(), params));
            return;
        }

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
