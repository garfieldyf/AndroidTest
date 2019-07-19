package android.ext.temp;

import java.util.concurrent.atomic.AtomicReference;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.FontMetricsInt;
import android.graphics.drawable.Drawable;
import android.text.style.ReplacementSpan;

public abstract class DrawableSpan extends ReplacementSpan {
    private int mPadding;

    public final void setPadding(int padding) {
        mPadding = padding;
    }

    public final void setPadding(Resources res, int id) {
        mPadding = res.getDimensionPixelOffset(id);
    }

    @Override
    public int getSize(Paint paint, CharSequence text, int start, int end, FontMetricsInt fm) {
        final int width;
        if (fm != null) {
            width = (fm.bottom - fm.top);
        } else {
            final FontMetricsInt metrics = FontMetricsPool.obtain(paint);
            width = (metrics.bottom - metrics.top);
            FontMetricsPool.sInstance.compareAndSet(null, metrics);
        }

        return width;
    }

    protected final void draw(Canvas canvas, Drawable drawable, int left, int top, int bottom) {
        drawable.setBounds(left + mPadding, top + mPadding, left + (bottom - top) - mPadding, bottom - mPadding);
        drawable.draw(canvas);
    }

    /**
     * Class <tt>FontMetricsPool</tt> is an one-size {@link FontMetricsInt} pool.
     */
    @SuppressWarnings("serial")
    private static final class FontMetricsPool extends AtomicReference<FontMetricsInt> {
        public static final FontMetricsPool sInstance = new FontMetricsPool();

        public static FontMetricsInt obtain(Paint paint) {
            FontMetricsInt result = sInstance.getAndSet(null);
            if (result == null) {
                result = new FontMetricsInt();
            }

            paint.getFontMetricsInt(result);
            return result;
        }
    }
}
