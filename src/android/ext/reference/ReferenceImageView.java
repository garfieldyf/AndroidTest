package android.ext.reference;

import android.content.Context;
import android.ext.reference.ReferenceDrawable;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Class ReferenceImageView
 * @author Garfield
 * @version 1.0
 */
public class ReferenceImageView extends ImageView {
    public ReferenceImageView(Context context) {
        super(context);
    }

    public ReferenceImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReferenceImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        final Drawable drawable = getDrawable();
        super.setImageBitmap(bm);
        release(drawable);
    }

    @Override
    public void setImageResource(int resId) {
        final Drawable drawable = getDrawable();
        super.setImageResource(resId);
        release(drawable);
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
        final Drawable oldDrawable = getDrawable();
        super.setImageDrawable(drawable);
        if (drawable != oldDrawable) {
            addRef(drawable);
            release(oldDrawable);
        }
    }

    @Override
    public void setImageURI(Uri uri) {
        final Drawable drawable = getDrawable();
        super.setImageURI(uri);
        release(drawable);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setImageDrawable(null);
    }

    private static void addRef(Drawable drawable) {
        if (drawable instanceof ReferenceDrawable) {
            ((ReferenceDrawable)drawable).addRef();
        } else if (drawable instanceof LayerDrawable) {
            final LayerDrawable layerDrawable = (LayerDrawable)drawable;
            for (int i = layerDrawable.getNumberOfLayers() - 1; i >= 0; --i) {
                addRef(layerDrawable.getDrawable(i));
            }
        }
    }

    private static void release(Drawable drawable) {
        if (drawable instanceof ReferenceDrawable) {
            ((ReferenceDrawable)drawable).release();
        } else if (drawable instanceof LayerDrawable) {
            final LayerDrawable layerDrawable = (LayerDrawable)drawable;
            for (int i = layerDrawable.getNumberOfLayers() - 1; i >= 0; --i) {
                release(layerDrawable.getDrawable(i));
            }
        }
    }
}
