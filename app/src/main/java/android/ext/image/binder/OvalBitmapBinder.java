package android.ext.image.binder;

import static android.ext.image.ImageModule.getPlaceholder;
import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

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
            setImageBitmap(view, view.getDrawable(), bitmap);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            view.setImageDrawable(getPlaceholder(view.getResources(), params));
        }
    }

    /**
     * Sets a {@link Bitmap} as the content of the {@link ImageView}.
     * @param view The <tt>ImageView</tt>.
     * @param drawable The <em>view's</em> old drawable or <tt>null</tt>.
     * @param bitmap The <tt>Bitmap</tt> to set. Never <tt>null</tt>.
     */
    /* package */ static void setImageBitmap(ImageView view, Drawable drawable, Bitmap bitmap) {
        if (drawable instanceof OvalBitmapDrawable) {
            // Sets the OvalBitmapDrawable's internal bitmap.
            ((OvalBitmapDrawable)drawable).setBitmap(bitmap);

            // Clear the ImageView's content to force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(drawable);
        } else {
            view.setImageDrawable(new OvalBitmapDrawable(bitmap));
        }
    }
}
