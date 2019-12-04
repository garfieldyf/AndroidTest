package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.image.ImageModule;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * Class <tt>GIFImageBinder</tt> used to bind the {@link GIFImage} to the {@link ImageView}.
 * @author Garfield
 */
public final class GIFImageBinder implements Binder<Object, Object, GIFImage> {
    public static final Binder<Object, Object, GIFImage> sInstance = new GIFImageBinder();

    /**
     * This class cannot be instantiated.
     */
    private GIFImageBinder() {
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, GIFImage image, int state) {
        final ImageView view = (ImageView)target;
        if (image != null) {
            view.setScaleType(ScaleType.FIT_XY);
            view.setImageDrawable(new GIFDrawable(image));
        } else {
            view.setScaleType(ScaleType.CENTER);
            view.setImageDrawable(ImageModule.getPlaceholder(params));
        }
    }
}
