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
 * Class <tt>RingBitmapBinder</tt> converts a {@link Bitmap} to a
 * {@link RingBitmapDrawable} and bind it to the {@link ImageView}.
 * <h3>Usage</h3>
 * <p>Here is a xml resource example:</p><pre>
 * &lt;xxx.xxx.RingBitmapBinder xmlns:android="http://schemas.android.com/apk/res/android"
 *     android:innerRadius="20dp" /&gt;</pre>
 * @author Garfield
 */
public final class RingBitmapBinder implements Binder<String, Object, Bitmap> {
    private final float mInnerRadius;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param attrs The attributes of the XML tag that is inflating the data.
     */
    @Keep
    public RingBitmapBinder(Context context, AttributeSet attrs) {
        mInnerRadius = XmlResources.loadInnerRadius(context.getResources(), attrs);
    }

    @Override
    public void bindValue(String uri, Object[] params, Object target, Bitmap bitmap, int state) {
        final ImageView view = (ImageView)target;
        if (bitmap != null) {
            setImageBitmap(view, bitmap);
        } else if ((state & STATE_LOAD_FROM_BACKGROUND) == 0) {
            ImageModule.setPlaceholder(view, params);
        }
    }

    private void setImageBitmap(ImageView view, Bitmap bitmap) {
        final Drawable oldDrawable = view.getDrawable();
        if (oldDrawable instanceof RingBitmapDrawable) {
            final RingBitmapDrawable drawable = (RingBitmapDrawable)oldDrawable;
            drawable.setBitmap(bitmap);
            drawable.setInnerRadius(mInnerRadius);

            // Force update the ImageView's mDrawable.
            view.setImageDrawable(null);
            view.setImageDrawable(drawable);
        } else {
            view.setImageDrawable(new RingBitmapDrawable(bitmap, mInnerRadius));
        }
    }
}
