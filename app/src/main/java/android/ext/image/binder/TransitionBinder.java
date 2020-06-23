package android.ext.image.binder;

import static android.ext.image.ImageModule.getPlaceholder;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.content.AsyncLoader.Binder;
import android.ext.util.ClassUtils;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.util.Printer;
import android.widget.ImageView;

/**
 * Class <tt>TransitionBinder</tt> used to play transition animation when the
 * {@link Bitmap} bind to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;TransitionBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:duration="@android:integer/config_longAnimTime"
 *     app:crossFade="true" /&gt;</pre>
 * @author Garfield
 */
public class TransitionBinder implements Binder<Object, Object, Bitmap> {
    /**
     * The length of the transition in milliseconds.
     */
    protected final int mDuration;

    /**
     * The cross fade of the drawables.
     */
    protected final boolean mCrossFade;

    /**
     * Constructor
     * @param durationMillis The length of the transition in milliseconds.
     * @param crossFade Enables or disables the cross fade of the drawables.
     * @see #TransitionBinder(Context, AttributeSet)
     * @see TransitionDrawable#setCrossFadeEnabled(boolean)
     */
    public TransitionBinder(int durationMillis, boolean crossFade) {
        mCrossFade = crossFade;
        mDuration  = durationMillis;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #TransitionBinder(int, boolean)
     */
    public TransitionBinder(Context context, AttributeSet attrs) {
        final String packageName = context.getPackageName();
        final TypedArray a = context.obtainStyledAttributes(attrs, ClassUtils.getFieldValue(packageName, "TransitionBinder"));
        mCrossFade = a.getBoolean(ClassUtils.getFieldValue(packageName, "TransitionBinder_crossFade"), false);
        mDuration  = a.getInt(ClassUtils.getFieldValue(packageName, "TransitionBinder_android_duration"), 300);
        a.recycle();
    }

    public void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { duration = ").append(mDuration)
            .append(", crossFade = ").append(mCrossFade)
            .append(" }").toString());
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Bitmap bitmap, int state) {
        final ImageView view = (ImageView)target;
        if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            if (bitmap != null) {
                setImageBitmap(view, bitmap);
            } else {
                view.setImageDrawable(getPlaceholder(view.getResources(), params));
            }
        } else if (bitmap != null) {
            final Drawable placeholder = getPlaceholder(view.getResources(), params);
            DebugUtils.__checkError(placeholder == null, "The placeholder drawable is null");
            final TransitionDrawable drawable = new TransitionDrawable(new Drawable[] { placeholder, newDrawable(view, bitmap) });
            view.setImageDrawable(drawable);
            drawable.setCrossFadeEnabled(mCrossFade);
            drawable.startTransition(mDuration);
        }
    }

    /**
     * Converts a {@link Bitmap} to a {@link Drawable}. Subclasses
     * should override this method to convert the drawable. The
     * default implementation returns a <tt>BitmapDrawable</tt>.
     */
    protected Drawable newDrawable(ImageView view, Bitmap bitmap) {
        return new BitmapDrawable(view.getResources(), bitmap);
    }

    /**
     * Sets a {@link Bitmap} as the content of the <em>view</em>.
     * Subclasses should override this method to set the bitmap.
     * The default implementation invoke <em>view.setImageBitmap(bitmap)</em>.
     */
    protected void setImageBitmap(ImageView view, Bitmap bitmap) {
        view.setImageBitmap(bitmap);
    }

    /**
     * Return the view's drawable, or <tt>null</tt> if no drawable has been assigned.
     */
    /* package */ static Drawable getDrawable(ImageView view) {
        Drawable drawable = view.getDrawable();
        if (drawable instanceof TransitionDrawable) {
            drawable = ((TransitionDrawable)drawable).getDrawable(1);
        }

        return drawable;
    }
}
