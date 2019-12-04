package android.ext.image.binder;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.ImageModule;
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
 *     android:duration="@android:integer/config_longAnimTime" /&gt;</pre>
 * @author Garfield
 */
public class TransitionBinder implements Binder<Object, Object, Bitmap> {
    private static final int[] TRANSITION_BINDER_ATTRS = {
        android.R.attr.duration
    };

    /**
     * The length of the transition in milliseconds.
     */
    protected final int mDuration;

    /**
     * Constructor
     * @param durationMillis The length of the transition in milliseconds.
     * @see #TransitionBinder(Context, AttributeSet)
     */
    public TransitionBinder(int durationMillis) {
        mDuration = durationMillis;
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #TransitionBinder(int)
     */
    public TransitionBinder(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, TRANSITION_BINDER_ATTRS);
        mDuration = a.getInt(0 /* android.R.attr.duration */, 300);
        a.recycle();
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Bitmap bitmap, int state) {
        final ImageView view = (ImageView)target;
        if (bitmap == null) {
            view.setImageDrawable(ImageModule.getPlaceholder(params));
        } else {
            setViewImage(view, new BitmapDrawable(view.getResources(), bitmap), params, state);
        }
    }

    /**
     * Sets an image as the content of the specified {@link ImageView}.
     * @param view The target <tt>ImageView</tt>.
     * @param drawable The <tt>Drawable</tt> to set.
     * @param params The parameters, passed earlier by {@link #bindValue}.
     * @param state The state, passed earlier by {@link #bindValue}.
     */
    protected void setViewImage(ImageView view, Drawable drawable, Object[] params, int state) {
        if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            view.setImageDrawable(drawable);
        } else {
            final Drawable placeholder = ImageModule.getPlaceholder(params);
            DebugUtils.__checkError(placeholder == null, "The placeholder drawable is null");
            final TransitionDrawable transition = new TransitionDrawable(new Drawable[] { placeholder, drawable });
            view.setImageDrawable(transition);
            transition.setCrossFadeEnabled(true);
            transition.startTransition(mDuration);
        }
    }

    public final void dump(Printer printer, StringBuilder result) {
        printer.println(result.append(getClass().getSimpleName())
            .append(" { duration = ").append(mDuration)
            .append(" }").toString());
    }
}
