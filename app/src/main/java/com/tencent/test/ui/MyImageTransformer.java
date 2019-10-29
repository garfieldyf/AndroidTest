package com.tencent.test.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.image.transformer.ImageTransformer;
import android.util.AttributeSet;
import android.util.Log;
import com.tencent.test.R;

public class MyImageTransformer extends ImageTransformer {
    public MyImageTransformer(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    
    @Override
    protected void inflateAttributes(Context context, AttributeSet attrs) {
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleConverter);
        int mirrored = a.getInt(R.styleable.CircleConverter_autoMirrored, 0);
        int gravity  = a.getInt(R.styleable.CircleConverter_android_gravity, 0);
        Log.i("yf", "mirrored = 0x" + Integer.toHexString(mirrored) + ", gravity = " + gravity);
        a.recycle();
    }
}
