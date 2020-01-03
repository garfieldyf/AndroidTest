package android.ext.image.binder;

import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.image.ImageModule;
import android.ext.util.DebugUtils;
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
        if (bitmap == null) {
            view.setImageDrawable(ImageModule.getPlaceholder(view.getResources(), params));
        } else {
            final Drawable drawable = view.getDrawable();
            if (drawable instanceof OvalBitmapDrawable) {
                // Sets the OvalBitmapDrawable's internal bitmap.
                ((OvalBitmapDrawable)drawable).setBitmap(bitmap);

                // Clear the ImageView's content to force update the
                // ImageView's mDrawableWidth and mDrawableHeight.
                view.setImageDrawable(null);
                view.setImageDrawable(drawable);
                DebugUtils.__checkDebug(true, "OvalBitmapBinder", "OvalBitmapDrawable.setBitmap()");
            } else {
                view.setImageDrawable(new OvalBitmapDrawable(bitmap));
            }
        }
    }
}
