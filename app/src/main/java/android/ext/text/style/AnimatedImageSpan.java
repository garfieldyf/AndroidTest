package android.ext.text.style;

import android.ext.graphics.drawable.GIFDrawable;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Drawable.Callback;
import android.os.SystemClock;
import android.view.View;
import java.lang.ref.WeakReference;

/**
 * Class AnimatedImageSpan
 * @author Garfield
 */
public class AnimatedImageSpan extends ImageSpan implements Callback {
    private final WeakReference<View> mView;

    /**
     * Rerturns an {@link AnimatedImageSpan} with given the resource <em>id</em>.
     * @param view The {@link View}.
     * @param id The resource id of the {@link AnimationDrawable}.
     * @return The <tt>AnimatedImageSpan</tt>.
     * @see #newGIFImageSpan(View, int)
     */
    @SuppressWarnings("unchecked")
    public static AnimatedImageSpan newAnimatedImageSpan(View view, int id) {
        return new AnimatedImageSpan(view, (AnimationDrawable)view.getResources().getDrawable(id));
    }

    /**
     * Rerturns an {@link AnimatedImageSpan} with given the resource <em>id</em>.
     * @param view The {@link View}.
     * @param id The resource id of the GIF image.
     * @return The <tt>AnimatedImageSpan</tt>.
     * @see #newAnimatedImageSpan(View, int)
     */
    public static AnimatedImageSpan newGIFImageSpan(View view, int id) {
        return new AnimatedImageSpan(view, GIFDrawable.decode(view.getResources(), id));
    }

    /**
     * Constructor
     * @param view The {@link View}.
     * @param drawable The {@link Animatable} drawable.
     */
    public <T extends Drawable & Animatable> AnimatedImageSpan(View view, T drawable) {
        super(drawable);
        mView = new WeakReference<View>(view);
        drawable.setCallback(this);
        drawable.start();
    }

    @Override
    public void invalidateDrawable(Drawable who) {
        final View view = mView.get();
        if (view != null && view.getParent() != null) {
            view.invalidate();
        }
    }

    @Override
    public void unscheduleDrawable(Drawable who, Runnable what) {
        final View view = mView.get();
        if (view != null && view.getParent() != null) {
            view.removeCallbacks(what);
        }
    }

    @Override
    public void scheduleDrawable(Drawable who, Runnable what, long when) {
        final View view = mView.get();
        if (view != null && view.getParent() != null) {
            view.postDelayed(what, when - SystemClock.uptimeMillis());
        }
    }
}
