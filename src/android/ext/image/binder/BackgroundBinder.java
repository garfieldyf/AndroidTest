package android.ext.image.binder;

import android.content.Context;
import android.ext.cache.Cache;
import android.ext.image.transformer.Transformer;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Class <tt>BackgroundBinder</tt> allows to bind the image to the <tt>View</tt>'s background.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;BackgroundBinder xmlns:app="http://schemas.android.com/apk/res-auto"
 *     app:defaultImage="@drawable/ic_placeholder"
 *     app:maxCacheSize="128"&gt;
 *
 *     &lt;!--
 *         The bitmap transformer (Optional), See transformer class
 *         Example: &lt;BitmapTransformer /&gt;
 *     --&gt;
 *
 *     &lt;!--
 *         The image transformer (Optional), See transformer class
 *         Example: &lt;GIFTransformer /&gt;
 *     --&gt;
 * &lt;/BackgroundBinder&gt;</pre>
 * @author Garfield
 */
public class BackgroundBinder<URI, Image> extends ImageBinder<URI, Image> {
    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #BackgroundBinder(Cache, Transformer, Drawable)
     */
    public BackgroundBinder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * Constructor
     * @param imageCache May be <tt>null</tt>. The {@link Cache} to store the drawables.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #BackgroundBinder(Context, AttributeSet)
     */
    public BackgroundBinder(Cache<URI, Drawable> imageCache, Transformer<URI, Image> transformer, Drawable defaultImage) {
        super(imageCache, transformer, defaultImage);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BackgroundBinder} from the specified <em>binder</em>. The returned binder will
     * be share the internal drawable cache and the transformer with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param defaultImage May be <tt>null</tt>. The <tt>Drawable</tt> to be used when the image is loading.
     * @see #BackgroundBinder(ImageBinder, Transformer)
     */
    public BackgroundBinder(ImageBinder<URI, Image> binder, Drawable defaultImage) {
        super(null, binder.mTransformer, defaultImage);
    }

    /**
     * Copy constructor
     * <p>Creates a new {@link BackgroundBinder} from the specified <em>binder</em>. The returned binder
     * will be share the internal drawable cache and the default image with the <em>binder</em>.</p>
     * @param binder The <tt>ImageBinder</tt> to copy.
     * @param transformer The {@link Transformer} to be used transforms an image to a <tt>Drawable</tt>.
     * @see #BackgroundBinder(ImageBinder, Drawable)
     */
    public BackgroundBinder(ImageBinder<URI, Image> binder, Transformer<URI, Image> transformer) {
        super(binder.getImageCache(), transformer, binder.mDefaultImage);
    }

    @Override
    public void bindValue(URI uri, Object[] params, Object target, Image value, int state) {
        final View view = (View)target;
        if (value == null) {
            view.setBackground(mDefaultImage);
        } else if (value instanceof Drawable) {
            view.setBackground((Drawable)value);
        } else {
            view.setBackground(mTransformer.transform(uri, value));
        }
    }
}
