package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.image.transformer.Transformer;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Class <tt>ImageBinder</tt> used to bind the image to the {@link ImageView}.
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class ImageBinder implements Binder<Object, Object, Object> {
    private static final ImageBinder sInstance = new ImageBinder();

    /**
     * Constructor
     */
    private ImageBinder() {
    }

    /**
     * Returns a type-safe {@link Binder} to bind the image to the {@link ImageView}.
     * @return The <tt>Binder</tt>.
     */
    public static <URI, Image> Binder<URI, Object, Image> getInstance() {
        return (Binder<URI, Object, Image>)sInstance;
    }

    /**
     * Returns a <tt>Drawable</tt> with the specified <em>params</em> and <em>value</em>.
     * @param params The parameters, passed earlier by {@link #bindValue}.
     * @param value The image value, passed earlier by {@link #bindValue}.
     * @return The <tt>Drawable</tt> to bind to target.
     */
    public static Drawable getImageValue(Object[] params, Object value) {
        if (value == null) {
            return (Drawable)params[2];
        } else if (value instanceof Drawable) {
            return (Drawable)value;
        } else {
            return ((Transformer)params[1]).transform(value);
        }
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        ((ImageView)target).setImageDrawable(getImageValue(params, value));
    }
}
