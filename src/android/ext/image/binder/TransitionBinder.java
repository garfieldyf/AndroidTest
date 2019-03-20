package android.ext.image.binder;

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
 */
public class TransitionBinder<URI, Image> extends ImageBinder<URI, Image> {
    private static final int[] TRANSITION_BINDER_ATTRS = { android.R.attr.duration };

    /**
     * The length of the transition in milliseconds.
     */
    private int mDuration;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     * @see #TransitionBinder(ImageBinder, Drawable, int)
     * @see #TransitionBinder(Cache, Transformer, Drawable, int)
     */
    public TransitionBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link TransitionBinder} from the specified <em>binder</em>. The returned binder will
     * be share the drawable cache and transformer with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @param durationMillis The length of the transition in milliseconds.
     * @see #TransitionBinder(Context, AttributeSet)
     * @see #TransitionBinder(Cache, Transformer, Drawable, int)
     */
    public TransitionBinder(ImageBinder<URI, Image> binder, Drawable defaultImage, int durationMillis) {
        super(binder, defaultImage);
        mDuration = durationMillis;
    }

    /**
     * Constructor
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the drawables.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @param durationMillis The length of the transition in milliseconds.
     * @see #TransitionBinder(Context, AttributeSet)
     * @see #TransitionBinder(ImageBinder, Drawable, int)
     */
    public TransitionBinder(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer, Drawable defaultImage, int durationMillis) {
        super(imageCache, transformer, defaultImage);
        mDuration = durationMillis;
    }

    /**
     * Returns the length of the transition in milliseconds.
     * @return The length of the transition in milliseconds.
     */
    public final int getDuration() {
        return mDuration;
    }

    @Override
    protected void inflateAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, TRANSITION_BINDER_ATTRS);
        mDuration = a.getInt(0 /* android.R.attr.duration */, 300);
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
                drawable.startTransition(mDuration);
            }
        }
    }
}
