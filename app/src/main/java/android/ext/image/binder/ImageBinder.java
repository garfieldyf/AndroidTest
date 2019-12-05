package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.image.ImageModule;
import android.widget.ImageView;

/**
 * Class <tt>ImageBinder</tt> used to bind an image to the {@link ImageView}.
 * @author Garfield
 */
public final class ImageBinder implements Binder<Object, Object, Object> {
    public static final Binder<Object, Object, Object> sInstance = new ImageBinder();

    /**
     * This class cannot be instantiated.
     */
    private ImageBinder() {
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Object value, int state) {
        final ImageView view = (ImageView)target;
        if (value != null) {
            ImageModule.setViewImage(view, value);
        } else {
            view.setImageDrawable(ImageModule.getPlaceholder(params));
        }
    }
}
