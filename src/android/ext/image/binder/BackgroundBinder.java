package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.image.transformer.Transformer;
import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * Class <tt>BackgroundBinder</tt> used to bind the image to the <tt>View</tt>'s background.
 * @author Garfield
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class BackgroundBinder implements Binder<Object, Object, Object> {
    private static final BackgroundBinder sInstance = new BackgroundBinder();

    /**
     * Constructor
     */
    private BackgroundBinder() {
    }

    /**
     * Returns a type-safe {@link Binder} to bind the image to the <tt>View</tt>'s background.
     * @return The <tt>Binder</tt>.
     */
    public static <URI, Image> Binder<URI, Object, Image> getInstance() {
        return (Binder<URI, Object, Image>)sInstance;
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        final Drawable background;
        if (value == null) {
            background = (Drawable)params[2];
        } else if (value instanceof Drawable) {
            background = (Drawable)value;
        } else {
            background = ((Transformer)params[1]).transform(value);
        }

        ((View)target).setBackground(background);
    }
}
