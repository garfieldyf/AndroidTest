package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.view.View;

/**
 * Class <tt>BackgroundBinder</tt> used to bind the image to the <tt>View</tt>'s background.
 * @author Garfield
 */
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
    @SuppressWarnings("unchecked")
    public static <URI, Image> Binder<URI, Object, Image> getInstance() {
        return (Binder<URI, Object, Image>)sInstance;
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        ((View)target).setBackground(ImageBinder.getImage(params, value));
    }
}
