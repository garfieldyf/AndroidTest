package com.tencent.temp;

import android.content.Context;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.ext.util.DebugUtils;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ShapeImageView extends ImageView {
    private OvalBitmapDrawable mOvalBitmapDrawable;
    private RoundedBitmapDrawable mRoundedBitmapDrawable;

    public ShapeImageView(Context context) {
        super(context);
    }

    public ShapeImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ShapeImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setImageOvalBitmap(Bitmap bitmap) {
        if (mOvalBitmapDrawable == null) {
            mOvalBitmapDrawable = new OvalBitmapDrawable();
            DebugUtils.__checkDebug(true, "ShapeImageView", "new OvalBitmapDrawable");
        }

        setImageDrawable(null);
        //mOvalBitmapDrawable.setBitmap(bitmap);
        setImageDrawable(mOvalBitmapDrawable);
    }

    public void setImageRoundedBitmap(Bitmap bitmap, float[] radii) {
        if (mRoundedBitmapDrawable == null) {
            mRoundedBitmapDrawable = new RoundedBitmapDrawable();
            DebugUtils.__checkDebug(true, "ShapeImageView", "new RoundedBitmapDrawable");
        }

        setImageDrawable(null);
        //mRoundedBitmapDrawable.setBitmap(bitmap);
        mRoundedBitmapDrawable.setCornerRadii(radii);
        setImageDrawable(mRoundedBitmapDrawable);
    }
}
