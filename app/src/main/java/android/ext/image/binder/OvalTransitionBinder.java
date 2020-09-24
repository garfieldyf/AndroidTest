package android.ext.image.binder;

import android.content.Context;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class <tt>OvalTransitionBinder</tt> converts a {@link Bitmap} to an
 * {@link OvalBitmapDrawable} and play transition animation when the
 * drawable bind to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;OvalTransitionBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:duration="@android:integer/config_longAnimTime"
 *     app:crossFade="true" /&gt;</pre>
 * @author Garfield
 */
public final class OvalTransitionBinder extends TransitionBinder {
    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #OvalTransitionBinder(int, boolean)
     */
    public OvalTransitionBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     * @param durationMillis The length of the transition in milliseconds.
     * @param crossFade Enables or disables the cross fade of the drawables.
     * @see #OvalTransitionBinder(Context, AttributeSet)
     * @see TransitionDrawable#setCrossFadeEnabled(boolean)
     */
    public OvalTransitionBinder(int durationMillis, boolean crossFade) {
        super(durationMillis, crossFade);
    }

    @Override
    protected Drawable newDrawable(ImageView view, Bitmap bitmap) {
        return new OvalBitmapDrawable(bitmap);
    }

    @Override
    protected void setImageBitmap(ImageView view, Bitmap bitmap) {
        OvalBitmapDrawable.setBitmap(view, getDrawable(view), bitmap);
    }
}
