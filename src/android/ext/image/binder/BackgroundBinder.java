package android.ext.image.binder;

import android.content.Context;
import android.ext.cache.Cache;
import android.ext.image.transformer.Transformer;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Class <tt>BackgroundBinder</tt> allows to bind the image to the <tt>View</tt>'s background.
 * @author Garfield
 */
public class BackgroundBinder<URI, Image> extends ImageBinder<URI, Image> {
    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #BackgroundBinder(ImageBinder, Drawable)
     * @see #BackgroundBinder(Cache, Transformer, Drawable)
     */
    public BackgroundBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BackgroundBinder} from the specified <em>binder</em>. The returned binder will
     * be share the drawable cache and transformer with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #BackgroundBinder(Context, AttributeSet)
     * @see #BackgroundBinder(Cache, Transformer, Drawable)
     */
    public BackgroundBinder(ImageBinder<URI, Image> binder, Drawable defaultImage) {
        super(binder, defaultImage);
    }

    /**
     * Constructor
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the drawables.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #BackgroundBinder(Context, AttributeSet)
     * @see #BackgroundBinder(ImageBinder, Drawable)
     */
    public BackgroundBinder(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer, Drawable defaultImage) {
        super(imageCache, transformer, defaultImage);
    }

    @Override
    public void bindValue(URI uri, Object[] params, Object target, Image value, int state) {
        final View view = (View)target;
        if (value == null) {
            view.setBackground(mDefaultImage);
        } else {
            final Drawable drawable = mTransformer.transform(uri, value);
            view.setBackground(drawable);
            if (drawable instanceof Animatable) {
                ((Animatable)drawable).start();
            }
        }
    }
}
