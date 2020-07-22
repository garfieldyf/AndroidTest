package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.Window;

/**
 * Class <tt>BackgroundBinder</tt> converts a {@link Bitmap} to
 * a {@link BitmapDrawable} and bind it to the {@link Window}.
 * @author Garfield
 */
public final class BackgroundBinder implements Binder<Object, Object, Bitmap> {
    public static final Binder<Object, Object, Bitmap> sInstance = new BackgroundBinder();

    /**
     * This class cannot be instantiated.
     */
    private BackgroundBinder() {
    }

    @Override
    public void bindValue(Object uri, Object[] params, Object target, Bitmap bitmap, int state) {
        if (bitmap != null) {
            final Window window = (Window)target;
            window.setBackgroundDrawable(new BitmapDrawable(window.getContext().getResources(), bitmap));
        }
    }
}
