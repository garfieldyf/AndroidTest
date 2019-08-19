package android.ext.image.binder;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.transformer.Transformer;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class <tt>TransitionBinder</tt> used to play transition animation when the image
 * first bind to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;TransitionBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:duration="@android:integer/config_longAnimTime" /&gt;</pre>
 * @author Garfield
 */
public final class TransitionBinder implements Binder<Object, Object, Object> {
    private static final int[] TRANSITION_BINDER_ATTRS = { android.R.attr.duration };

    /**
     * The length of the transition in milliseconds.
     */
    private final int mDuration;

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
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        final ImageView view = (ImageView)target;
        final Drawable defaultImage = (Drawable)params[2];
        if (value == null) {
            view.setImageDrawable(defaultImage);
        } else if (value instanceof Drawable) {
            setViewImage(view, (Drawable)value, defaultImage, state);
        } else {
            setViewImage(view, ((Transformer)params[1]).transform(value), defaultImage, state);
        }
    }

    private void setViewImage(ImageView view, Drawable value, Drawable defaultImage, int state) {
        if ((state & STATE_LOAD_FROM_CACHE) != 0) {
            view.setImageDrawable(value);
        } else {
            final TransitionDrawable drawable = new TransitionDrawable(new Drawable[] { defaultImage, value });
            view.setImageDrawable(drawable);
            drawable.setCrossFadeEnabled(true);
            drawable.startTransition(mDuration);
        }
    }
}
