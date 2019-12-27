package com.tencent.test;

import android.content.Context;
import android.ext.content.AsyncLoader.Binder;
import android.ext.content.res.XmlResources;
import android.ext.graphics.drawable.RingBitmapDrawable;
import android.ext.image.ImageModule;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RingBitmapBinder implements Binder<String, Object, Bitmap> {
    private final float mInnerRadius;

    public RingBitmapBinder(Context context, AttributeSet attrs) {
        mInnerRadius = XmlResources.loadInnerRadius(context.getResources(), attrs);
    }

    @Override
    public void bindValue(String uri, Object[] params, Object target, Bitmap bitmap, int state) {
        final ImageView view = (ImageView)target;
        if (bitmap != null) {
            view.setImageDrawable(new RingBitmapDrawable(bitmap, mInnerRadius));
        } else {
            view.setImageDrawable(ImageModule.getPlaceholder(view.getResources(), params));
        }
    }
}
