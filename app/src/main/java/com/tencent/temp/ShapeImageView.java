package com.tencent.temp;

import android.content.Context;
import android.ext.graphics.drawable.OvalBitmapDrawable;
import android.ext.graphics.drawable.RoundedBitmapDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;

public class ShapeImageView extends ImageView {
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
        final Drawable drawable = getDrawable();
        if (drawable instanceof OvalBitmapDrawable) {
            // Sets the OvalBitmapDrawable's internal bitmap.
            ((OvalBitmapDrawable)drawable).setBitmap(bitmap);

            // Clear the ImageView's content to force update the ImageView's mDrawable.
            setImageDrawable(null);
            setImageDrawable(drawable);
        } else {
            setImageDrawable(new OvalBitmapDrawable(bitmap));
        }
    }

    public void setImageRoundedBitmap(Bitmap bitmap, float[] radii) {
        final Drawable drawable = getDrawable();
        if (drawable instanceof RoundedBitmapDrawable) {
            // Sets the RoundedBitmapDrawable's internal bitmap.
            final RoundedBitmapDrawable d = (RoundedBitmapDrawable)drawable;
            d.setBitmap(bitmap);
            d.setCornerRadii(radii);

            // Clear the ImageView's content to force update the ImageView's mDrawable.
            setImageDrawable(null);
            setImageDrawable(drawable);
        } else {
            setImageDrawable(new RoundedBitmapDrawable(bitmap, radii));
        }
    }
}
