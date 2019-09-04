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
    /* package */ int mPadding;
    /* package */ final Drawable mDrawable;

    /**
     * Constructor
     * @param drawable The {@link Drawable}.
     * @see #ImageSpan(Drawable, int)
     */
    public ImageSpan(Drawable drawable) {
        this(drawable, 0);
    }

    /**
     * Constructor
     * @param drawable The {@link Drawable}.
     * @param padding The padding of the <em>drawable</em> in pixels.
     * @see #ImageSpan(Drawable)
     */
    public ImageSpan(Drawable drawable, int padding) {
        mPadding  = padding;
        mDrawable = drawable;
    }

    /**
     * Constructor
     * @param res The <tt>Resources</tt>.
     * @param id The resource id of the image.
     * @see #ImageSpan(Resources, int, int)
     */
    @SuppressWarnings("deprecation")
    public ImageSpan(Resources res, int id) {
        this(res.getDrawable(id), 0);
    }

    /**
     * Constructor
     * @param res The <tt>Resources</tt>.
     * @param id The resource id of the image.
     * @param resId The resource id of the padding.
     * @see #ImageSpan(Resources, int)
     */
    @SuppressWarnings("deprecation")
    public ImageSpan(Resources res, int id, int resId) {
        this(res.getDrawable(id), res.getDimensionPixelOffset(resId));
    }

    public final void setPadding(int padding) {
        mPadding = padding;
    }

    public final void setPadding(Resources res, int id) {
        mPadding = res.getDimensionPixelOffset(id);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        return mDrawable.getIntrinsicWidth() + mPadding;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        draw(canvas, mDrawable, (int)x, top, bottom);
    }

    /* package */ final void draw(Canvas canvas, Drawable drawable, int left, int top, int bottom) {
        final int dy = (bottom - top - drawable.getIntrinsicHeight()) / 2;
        drawable.setBounds(left + mPadding, top + dy, left + mPadding + drawable.getIntrinsicWidth(), bottom - dy);
        drawable.draw(canvas);
    }
}
