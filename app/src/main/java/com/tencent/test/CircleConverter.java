package com.tencent.test;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.drawable.ImageDrawable;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.image.transformer.Transformer;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.Keep;
import android.util.AttributeSet;
import android.view.Gravity;

public class CircleConverter implements Transformer<Bitmap> {
    private final int mGravity;
    private final int mMirrored;

    @Keep
    private CircleConverter(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleConverter);
        mGravity = a.getInt(R.styleable.CircleConverter_android_gravity, Gravity.FILL);
        mMirrored = a.getInt(R.styleable.CircleConverter_autoMirrored, ImageDrawable.NONE_MIRRORED);
        a.recycle();
    }

    @Override
    public Drawable transform(Bitmap bitmap) {
        final OvalBitmapDrawable drawable = new OvalBitmapDrawable(bitmap);
        drawable.setGravity(mGravity);
        drawable.setAutoMirrored(mMirrored);
        return drawable;
    }
}
