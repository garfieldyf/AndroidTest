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

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        final Drawable drawable;
        if (value == null) {
            drawable = (Drawable)params[2];
        } else if (value instanceof Drawable) {
            drawable = (Drawable)value;
        } else {
            drawable = ((Transformer)params[1]).transform(value);
        }

        ((ImageView)target).setImageDrawable(drawable);
    }
}
