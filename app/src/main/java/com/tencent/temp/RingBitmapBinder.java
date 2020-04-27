package com.tencent.temp;

import android.content.Context;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.graphics.drawable.RingBitmapDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class <tt>RingBitmapBinder</tt> used to transforms a {@link Bitmap}
 * to a {@link RingBitmapDrawable} to bind the {@link ImageView}.
 * @author Garfield
 */
public final class RingBitmapBinder implements Binder<String, Object, Bitmap> {
    private final float mInnerRadius;

    @Keep
    public RingBitmapBinder(Context context, AttributeSet attrs) {
        mInnerRadius = XmlResources.loadInnerRadius(context.getResources(), attrs);
    }

    @Override
    public void bindValue(String uri, Object[] params, Object target, Bitmap bitmap, int state) {
        final ImageView view = (ImageView)target;
        if (bitmap == null) {
            view.setImageDrawable(ImageModule.getPlaceholder(view.getResources(), params));
        } else {
            final Drawable oldDrawable = view.getDrawable();
            if (oldDrawable instanceof RingBitmapDrawable) {
                // Sets the RingBitmapDrawable's internal bitmap.
                final RingBitmapDrawable drawable = (RingBitmapDrawable)oldDrawable;
                drawable.setBitmap(bitmap);
                drawable.setInnerRadius(mInnerRadius);

                // Clear the ImageView's content to force update the ImageView's mDrawable.
                view.setImageDrawable(null);
                view.setImageDrawable(drawable);
            } else {
                view.setImageDrawable(new RingBitmapDrawable(bitmap, mInnerRadius));
            }
        }
    }
}
