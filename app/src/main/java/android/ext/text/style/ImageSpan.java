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
    private int mPaddingLeft;
    private int mPaddingRight;
    private final Drawable mDrawable;

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
        mPaddingLeft = mPaddingRight = res.getDimensionPixelOffset(id);
    }

    public final void setPadding(int left, int right) {
        mPaddingLeft  = left;
        mPaddingRight = right;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        return mDrawable.getIntrinsicWidth() + mPaddingLeft + mPaddingRight;
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        final int dy = (bottom - top - mDrawable.getIntrinsicHeight()) / 2;
        mDrawable.setBounds((int)x + mPaddingLeft + 1, top + dy + 1, (int)x + mPaddingLeft + mDrawable.getIntrinsicWidth() - 1, bottom - dy - 1);
        mDrawable.draw(canvas);
    }
}
