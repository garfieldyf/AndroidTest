package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
            setViewImage(view, value);
        } else {
            view.setImageDrawable(ImageModule.getPlaceholder(params));
        }
    }

    /**
     * Sets an image as the content of the specified {@link ImageView}.
     * @param view The target <tt>ImageView</tt>.
     * @param value The image value to set.
     */
    public static void setViewImage(ImageView view, Object value) {
        if (value instanceof Bitmap) {
            view.setImageBitmap((Bitmap)value);
        } else if (value instanceof Drawable) {
            view.setImageDrawable((Drawable)value);
        } else {
            view.setImageDrawable(new GIFDrawable((GIFImage)value));
        }
    }
}
