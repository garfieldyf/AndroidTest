package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * Class <tt>ImageBinder</tt> used to bind the image to the {@link ImageView}.
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
        if (value == null) {
            view.setScaleType(ScaleType.CENTER);
            view.setImageDrawable(ImageModule.getPlaceholder(params));
        } else {
            view.setScaleType(ScaleType.FIT_XY);
            if (value instanceof Bitmap) {
                view.setImageBitmap((Bitmap)value);
            } else if (value instanceof Drawable) {
                view.setImageDrawable((Drawable)value);
            } else {
                view.setImageDrawable(new GIFDrawable((GIFImage)value));
            }
        }
    }
}
