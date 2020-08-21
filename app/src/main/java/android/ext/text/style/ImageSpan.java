package android.ext.text.style;

import android.content.res.Resources;
import android.ext.util.DebugUtils;
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
    private int mPaddingTop;
    private int mPaddingRight;
    private int mPaddingBottom;
    private final Drawable mDrawable;

    /**
     * Constructor
     * @param drawable The {@link Drawable}.
     * @see #ImageSpan(Resources, int)
     */
    public ImageSpan(Drawable drawable) {
        DebugUtils.__checkError(drawable == null, "Invalid parameter - drawable == null");
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
        DebugUtils.__checkError(mDrawable == null, "Couldn't load resource - ID #0x" + Integer.toHexString(id));
    }

    public final void setPadding(Resources res, int id) {
        mPaddingLeft = mPaddingTop = mPaddingRight = mPaddingBottom = res.getDimensionPixelOffset(id);
    }

    public final void setPadding(int left, int top, int right, int bottom) {
        mPaddingLeft   = left;
        mPaddingTop    = top;
        mPaddingRight  = right;
        mPaddingBottom = bottom;
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        return mDrawable.getIntrinsicWidth();
    }

    @Override
    public void draw(Canvas canvas, CharSequence text, int start, int end, float x, int top, int y, int bottom, Paint paint) {
        final int left = (int)x + mPaddingLeft;
        mDrawable.setBounds(left, top + mPaddingTop, left + mDrawable.getIntrinsicWidth() - mPaddingRight, bottom - mPaddingBottom);
        mDrawable.draw(canvas);
    }
}
