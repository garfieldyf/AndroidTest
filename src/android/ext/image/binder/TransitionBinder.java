package android.ext.image.binder;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.cache.Cache;
import android.ext.image.transformer.Transformer;
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
     * Copy constructor
     * <p>Creates a new {@link TransitionBinder} from the specified <em>binder</em>. The returned binder will
     * be share the internal drawable cache and the transformer with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @param durationMillis The length of the transition in milliseconds.
     * @see #TransitionBinder(ImageBinder, Transformer, int)
     */
    public TransitionBinder(ImageBinder<URI, Image> binder, Drawable defaultImage, int durationMillis) {
        this(null, binder.mTransformer, defaultImage, durationMillis);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link TransitionBinder} from the specified <em>binder</em>. The returned binder
     * will be share the internal drawable cache and the default image with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param durationMillis The length of the transition in milliseconds.
     * @see #TransitionBinder(ImageBinder, Drawable, int)
     */
    public TransitionBinder(ImageBinder<URI, Image> binder, Transformer<URI, Image> transformer, int durationMillis) {
        this(binder.getImageCache(), transformer, binder.mDefaultImage, durationMillis);
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
        } else if (value instanceof Drawable) {
            setViewImage(view, (Drawable)value, state);
        } else {
            setViewImage(view, mTransformer.transform(uri, value), state);
        }
    }

    private void setViewImage(ImageView view, Drawable value, int state) {
        if ((state & STATE_LOAD_FROM_CACHE) != 0) {
            view.setImageDrawable(value);
        } else {
            final TransitionDrawable drawable = new TransitionDrawable(new Drawable[] { mDefaultImage, value });
            view.setImageDrawable(drawable);
            drawable.setCrossFadeEnabled(true);
            drawable.startTransition(mDuration);
        }
    }
}
