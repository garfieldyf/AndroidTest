package android.ext.image.binder;

import android.content.Context;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class <tt>OvalTransitionBinder</tt> used to transforms a {@link Bitmap} to
 * an {@link OvalBitmapDrawable} and play transition animation when the drawable
 * bind to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;OvalTransitionBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:duration="@android:integer/config_longAnimTime" /&gt;</pre>
 * @author Garfield
 */
public final class OvalTransitionBinder extends TransitionBinder {
    /**
     * Constructor
     * @param durationMillis The length of the transition in milliseconds.
     * @see #OvalTransitionBinder(Context, AttributeSet)
     */
    public OvalTransitionBinder(int durationMillis) {
        super(durationMillis);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #OvalTransitionBinder(int)
     */
    public OvalTransitionBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected Drawable getDrawable(ImageView view, Bitmap bitmap) {
        return new OvalBitmapDrawable(bitmap);
    }
}
