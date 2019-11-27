package android.ext.image.binder;

import static android.ext.image.ImageLoader.LoadRequest.PLACEHOLDER_INDEX;
import static android.ext.image.ImageLoader.LoadRequest.TRANSFORMER_INDEX;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.cache.SimpleLruCache;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.transformer.Transformer;
import android.ext.util.ClassUtils;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class <tt>ImageBinder</tt> allows to bind the image to the {@link ImageView}.
 * This class can be support the drawable cache.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;[ ImageBinder | binder ]
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     class="classFullName"
 *     app:maxCacheSize="128"
 *     app:attribute1="value1"
 *     app:attribute2="value2"
 *     ... ... /&gt;</pre>
 * @author Garfield
 */
public class ImageBinder<URI, Image> implements Binder<URI, Object, Image> {
    private final SimpleLruCache<URI, Drawable> mImageCache;

    /**
     * Constructor
     * @param maxCacheSize The maximum number of
     * drawables to allow in the internal cache.
     * @see #ImageBinder(Context, AttributeSet)
     */
    public ImageBinder(int maxCacheSize) {
        mImageCache = new SimpleLruCache<URI, Drawable>(maxCacheSize);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The base set of attribute values.
     * @see #ImageBinder(int)
     */
    public ImageBinder(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, (int[])ClassUtils.getFieldValue(context.getPackageName(), "ImageBinder"));
        mImageCache = new SimpleLruCache<URI, Drawable>(a.getInt(0 /* R.styleable.ImageBinder_maxCacheSize */, 0));
        a.recycle();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void bindValue(URI uri, Object[] params, Object target, Image image, int state) {
        Drawable drawable;
        if (image == null) {
            drawable = (Drawable)params[PLACEHOLDER_INDEX];
        } else if (image instanceof Drawable) {
            drawable = (Drawable)image;
        } else if ((drawable = mImageCache.get(uri)) == null) {
            mImageCache.put(uri, drawable = ((Transformer<Image>)params[TRANSFORMER_INDEX]).transform(image));
        }

        ((ImageView)target).setImageDrawable(drawable);
    }
}
