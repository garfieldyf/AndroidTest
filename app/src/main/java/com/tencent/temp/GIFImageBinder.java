package com.tencent.temp;

import android.ext.content.AsyncLoader.Binder;
import android.ext.graphics.GIFImage;
import android.ext.graphics.drawable.GIFDrawable;
import android.ext.image.ImageModule;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

/**
 * Class <tt>GIFImageBinder</tt> used to transforms a {@link GIFImage}
 * to a {@link GIFDrawable} to bind the {@link ImageView}.
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
        if (image == null) {
            view.setImageDrawable(ImageModule.getPlaceholder(view.getResources(), params));
        } else {
            final Drawable drawable = view.getDrawable();
            if (drawable instanceof GIFDrawable) {
                // Sets the GIFDrawable's internal image.
                //((GIFDrawable)drawable).setImage(image);

                // Clear the ImageView's content to force update the ImageView's mDrawable.
                view.setImageDrawable(null);
                view.setImageDrawable(drawable);
            } else {
                view.setImageDrawable(new GIFDrawable(image));
            }
        }
    }
}
