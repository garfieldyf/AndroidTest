package android.ext.content.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.util.Caches.Cache;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class TransitionBinder
 * @author Garfield
 * @version 1.0
 */
public class TransitionBinder<URI, Params, Image> extends ImageBinder<URI, Params, Image> {
    private static final int[] TRANSITION_BINDER_ATTRS = { android.R.attr.duration };

    /**
     * The length of the transition in milliseconds.
     */
    protected int mDuration;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #TransitionBinder(Cache, Transformer, Drawable, int)
     */
    public TransitionBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the drawables.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @param durationMillis The length of the transition in milliseconds.
     * @see #TransitionBinder(Context, AttributeSet)
     */
    public TransitionBinder(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer, Drawable defaultImage, int durationMillis) {
        super(imageCache, transformer, defaultImage);
        mDuration = durationMillis;
    }

    /**
     * Returns the length of the transition in milliseconds.
     * @return The length of the transition.
     */
    public final int getDuration() {
        return mDuration;
    }

    @Override
    public TransitionBinder<URI, Params, Image> copy(Drawable defaultImage) {
        return new TransitionBinder<URI, Params, Image>(null, mTransformer, defaultImage, mDuration);
    }

    @Override
    protected void inflateAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, TRANSITION_BINDER_ATTRS);
        mDuration = a.getInt(0 /* android.R.attr.duration */, 300);
        a.recycle();
    }

    @Override
    public void bindValue(URI uri, Params[] params, Object target, Image value, int state) {
        final ImageView view = (ImageView)target;
        if (value == null) {
            view.setImageDrawable(mDefaultImage);
        } else if ((state & STATE_LOAD_FROM_CACHE) != 0) {
            view.setImageDrawable(mTransformer.transform(uri, target, value));
        } else {
            final TransitionDrawable drawable = new TransitionDrawable(new Drawable[] { mDefaultImage, mTransformer.transform(uri, target, value) });
            view.setImageDrawable(drawable);
            drawable.setCrossFadeEnabled(true);
            drawable.startTransition(mDuration);
        }
    }
}
