package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

/**
 * Class <tt>OvalBitmapBinder</tt> used to transforms a {@link Bitmap}
 * to a {@link OvalBitmapDrawable} to bind the {@link ImageView}.
 * @author Garfield
 */
public final class OvalBitmapBinder implements Binder<Object, Object, Bitmap> {
    public static final Binder<Object, Object, Bitmap> sInstance = new OvalBitmapBinder();

    /**
     * This class cannot be instantiated.
     */
    private OvalBitmapBinder() {
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Bitmap bitmap, int state) {
        final ImageView view = (ImageView)target;
        if (bitmap != null) {
            view.setScaleType(ScaleType.FIT_XY);
            view.setImageDrawable(new OvalBitmapDrawable(bitmap));
        } else {
            view.setScaleType(ScaleType.CENTER);
            view.setImageDrawable(ImageModule.getPlaceholder(params));
        }
    }
}
