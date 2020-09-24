package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Class <tt>OvalBitmapBinder</tt> converts a {@link Bitmap} to a
 * {@link OvalBitmapDrawable} and bind it to the {@link ImageView}.
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
            OvalBitmapDrawable.setBitmap(view, view.getDrawable(), bitmap);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            ImageModule.setPlaceholder(view, params);
        }
    }
}
