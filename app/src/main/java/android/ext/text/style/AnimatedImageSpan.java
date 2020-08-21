package android.ext.text.style;

import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.util.DebugUtils;
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
    @SuppressWarnings("deprecation")
    public static AnimatedImageSpan newAnimatedImageSpan(View view, int id) {
        final AnimationDrawable drawable = (AnimationDrawable)view.getResources().getDrawable(id);
        DebugUtils.__checkError(drawable == null, "Couldn't load resource - ID #0x" + Integer.toHexString(id));
        return new AnimatedImageSpan(view, drawable);
    }

    /**
     * Rerturns an {@link AnimatedImageSpan} with given the resource <em>id</em>.
     * @param view The {@link View}.
     * @param id The resource id of the {@link GIFImage}.
     * @return The <tt>AnimatedImageSpan</tt>.
     * @see #newAnimatedImageSpan(View, int)
     */
    public static AnimatedImageSpan newGIFImageSpan(View view, int id) {
        final GIFImage image = GIFImage.decode(view.getResources(), id);
        DebugUtils.__checkError(image == null, "Couldn't load resource - ID #0x" + Integer.toHexString(id));
        return new AnimatedImageSpan(view, new GIFDrawable(image));
    }

    /**
     * Constructor
     * @param view The {@link View}.
     * @param drawable The {@link Animatable} drawable.
     */
    public <T extends Drawable & Animatable> AnimatedImageSpan(View view, T drawable) {
        super(drawable);
        DebugUtils.__checkError(view == null, "Invalid parameter - view == null");
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
