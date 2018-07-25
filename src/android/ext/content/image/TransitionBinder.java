package android.ext.content.image;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.cache.Cache;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class <tt>TransitionBinder</tt> allows to play transition
 * animation while the image first bind to the {@link ImageView}.
 * @author Garfield
 * @version 1.0
 */
public class TransitionBinder<URI, Image> extends ImageBinder<URI, Image> {
    private static final int[] TRANSITION_BINDER_ATTRS = { android.R.attr.duration };

    /**
     * The length of the transition in milliseconds.
     */
    public int duration;

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
        duration = durationMillis;
    }

    @Override
    public TransitionBinder<URI, Image> copy(Drawable defaultImage) {
        return new TransitionBinder<URI, Image>(null, mTransformer, defaultImage, duration);
    }

    @Override
    protected void inflateAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, TRANSITION_BINDER_ATTRS);
        duration = a.getInt(0 /* android.R.attr.duration */, 300);
        a.recycle();
    }

    @Override
    public void bindValue(URI uri, Object[] params, Object target, Image value, int state) {
        final ImageView view = (ImageView)target;
        if (value == null) {
            view.setImageDrawable(mDefaultImage);
        } else {
            final Drawable image = mTransformer.transform(uri, target, value);
            if ((state & STATE_LOAD_FROM_CACHE) != 0) {
                view.setImageDrawable(image);
            } else {
                final TransitionDrawable drawable = new TransitionDrawable(new Drawable[] { mDefaultImage, image });
                view.setImageDrawable(drawable);
                drawable.setCrossFadeEnabled(true);
                drawable.startTransition(duration);
            }
        }
    }
}
