package android.ext.image.binder;

import static android.ext.image.ImageLoader.LoadRequest.PLACEHOLDER_INDEX;
import static android.ext.image.ImageLoader.LoadRequest.TRANSFORMER_INDEX;
import android.content.Context;
import android.content.res.TypedArray;
import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.ext.content.AsyncLoader.Binder;
import android.ext.image.transformer.Transformer;
import android.ext.util.ClassUtils;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

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
    protected final Cache<URI, Drawable> mImageCache;

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
    public void bindValue(URI uri, Object[] params, Object target, Image image, int state) {
        final ImageView view = (ImageView)target;
        if (image == null) {
            view.setScaleType(ScaleType.CENTER);
            view.setImageDrawable((Drawable)params[PLACEHOLDER_INDEX]);
        } else {
            view.setScaleType(ScaleType.FIT_XY);
            if (image instanceof Drawable) {
                view.setImageDrawable((Drawable)image);
            } else {
                view.setImageDrawable(getCachedDrawable(uri, params, image));
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected Drawable getCachedDrawable(URI uri, Object[] params, Image image) {
        Drawable drawable = mImageCache.get(uri);
        if (drawable == null) {
            mImageCache.put(uri, drawable = ((Transformer<Image>)params[TRANSFORMER_INDEX]).transform(image));
        }

        return drawable;
    }
}
