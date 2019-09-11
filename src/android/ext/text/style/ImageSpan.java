package android.ext.text.style;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

/**
 * Class ImageSpan
 * @author Garfield
 */
public class ImageSpan extends ReplacementSpan {
    /* package */ int mPaddingLeft;
    /* package */ int mPaddingRight;
    /* package */ final Drawable mDrawable;

    /**
     * Constructor
     * @param drawable The {@link Drawable}.
     * @see #ImageSpan(Resources, int)
     */
    public ImageSpan(Drawable drawable) {
        mDrawable = drawable;
    }

    /**
     * Constructor
     * @param res The <tt>Resources</tt>.
     * @param id The resource id of the image.
     * @see #ImageSpan(Drawable)
     */
    @SuppressWarnings("deprecation")
    public ImageSpan(Resources res, int id) {
        mDrawable = res.getDrawable(id);
    }

    public final void setPadding(Resources res, int id) {
        mPaddingRight = mPaddingLeft = res.getDimensionPixelOffset(id);
    }

    public final void setPadding(int paddingLeft, int paddingRight) {
        mPaddingLeft  = paddingLeft;
        mPaddingRight = paddingRight;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        return mDrawable.getIntrinsicWidth() + mPaddingLeft + mPaddingRight;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        draw(canvas, mDrawable, (int)x, top, bottom);
    }

    /* package */ final void draw(Canvas canvas, Drawable drawable, int left, int top, int bottom) {
        final int dy = (bottom - top - drawable.getIntrinsicHeight()) / 2;
        drawable.setBounds(left + mPaddingLeft, top + dy, left + mPaddingLeft + drawable.getIntrinsicWidth(), bottom - dy);
        drawable.draw(canvas);
    }
}
