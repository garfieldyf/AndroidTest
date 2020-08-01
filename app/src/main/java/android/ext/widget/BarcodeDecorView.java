package android.ext.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.ext.graphics.DrawUtils;
import android.ext.util.ReflectUtils;
import android.ext.util.StringUtils;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

/**
 * Class BarcodeDecorView
 * @author Garfield
 */
public class BarcodeDecorView extends View {
    private final int mMaskColor;
    private final int mBorderColor;

    private final int mCornerColor;
    private final float mCornerWidth;
    private final float mCornerHeight;

    private final Paint mPaint;
    private final Rect mScanningBounds;

    protected CharSequence mText;
    protected int mTextColor;
    protected float mTextOffset;

    protected Drawable mScanningIndicator;
    protected int mScanningIndicatorOffset;
    protected int mScanningIndicatorHeight;

    public BarcodeDecorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BarcodeDecorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mScanningBounds = new Rect();
        final String packageName = context.getPackageName();
        final DisplayMetrics dm  = getResources().getDisplayMetrics();

        final TypedArray a = context.obtainStyledAttributes(attrs, ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView"));
        mMaskColor    = a.getColor(ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView_maskColor"), 0x80000000);
        mBorderColor  = a.getColor(ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView_borderColor"), 0xff808080);
        mCornerColor  = a.getColor(ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView_cornerColor"), 0xff80ff00);
        mCornerWidth  = getDimension(packageName, "BarcodeDecorView_cornerWidth", a, 15, TypedValue.COMPLEX_UNIT_DIP, dm);
        mCornerHeight = getDimension(packageName, "BarcodeDecorView_cornerHeight", a, 3, TypedValue.COMPLEX_UNIT_DIP, dm);

        mText = a.getText(ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView_android_text"));
        mTextColor  = a.getColor(ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView_android_textColor"), 0xffa4a4a4);
        mTextOffset = getDimension(packageName, "BarcodeDecorView_textOffset", a, 20, TypedValue.COMPLEX_UNIT_DIP, dm);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setTextSize(getDimension(packageName, "BarcodeDecorView_android_textSize", a, 15, TypedValue.COMPLEX_UNIT_SP, dm));
        mPaint.setStrokeWidth(getDimension(packageName, "BarcodeDecorView_borderWidth", a, 1, TypedValue.COMPLEX_UNIT_DIP, dm));

        mScanningIndicator = a.getDrawable(ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView_scanningIndicator"));
        mScanningIndicatorHeight = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "BarcodeDecorView_scanningIndicatorHeight"), 0);

        if (mScanningIndicatorHeight <= 0 && mScanningIndicator != null) {
            mScanningIndicatorHeight = mScanningIndicator.getIntrinsicHeight();
        }

        a.recycle();
    }

    /**
     * Returns the text of this view is displaying.
     * @return The text, or <tt>null</tt> if this
     * view has no text.
     * @see #setText(int)
     * @see #setText(CharSequence)
     */
    public CharSequence getText() {
        return mText;
    }

    /**
     * Sets the text to a given <tt>CharSequence</tt>.
     * @param text The text to set.
     * @see #getText()
     * @see #setText(int)
     */
    public void setText(CharSequence text) {
        if (!TextUtils.equals(mText, text)) {
            mText = text;
            invalidate();
        }
    }

    /**
     * Sets the text of this view is displaying.
     * @param resId The resource id of the text to load.
     * @see #getText()
     * @see #setText(CharSequence)
     */
    public final void setText(int resId) {
        setText(getResources().getText(resId));
    }

    /**
     * Returns the text color.
     * @return The text color.
     * @see #setTextColor(int)
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Sets the text color to a given <em>color</em>.
     * @param color The text color to set.
     * @see #getTextColor()
     */
    public void setTextColor(int color) {
        if (mTextColor != color) {
            mTextColor = color;
            invalidate();
        }
    }

    /**
     * Returns the text size.
     * @return The text size.
     * @see #setTextSize(int)
     * @see #setTextSize(float)
     */
    public float getTextSize() {
        return mPaint.getTextSize();
    }

    /**
     * Sets the text size to a given <em>textSize</em>.
     * @param textSize The text size to set.
     * @see #getTextSize()
     * @see #setTextSize(int)
     */
    public void setTextSize(float textSize) {
        if (mPaint.getTextSize() != textSize) {
            mPaint.setTextSize(textSize);
            invalidate();
        }
    }

    /**
     * Sets the text size.
     * @param resId The resource id of the dimension to load.
     * @see #getTextSize()
     * @see #setTextSize(float)
     */
    public final void setTextSize(int resId) {
        setTextSize(getResources().getDimension(resId));
    }

    /**
     * Returns the text top offset.
     * @return The text top offset.
     * @see #setTextOffset(int)
     * @see #setTextOffset(float)
     */
    public float getTextOffset() {
        return mTextOffset;
    }

    /**
     * Sets the text top offset.
     * @param offset The offset to set.
     * @see #getTextOffset()
     * @see #setTextOffset(int)
     */
    public void setTextOffset(float offset) {
        if (mTextOffset != offset) {
            mTextOffset = offset;
            invalidate();
        }
    }

    /**
     * Sets the text top offset.
     * @param resId The resource id of the dimension to load.
     * @see #getTextOffset()
     * @see #setTextOffset(float)
     */
    public final void setTextOffset(int resId) {
        setTextOffset(getResources().getDimension(resId));
    }

    /**
     * Computes the coordinates of the scanning bounds on the screen.
     * @param percent The percent, expressed as a percentage of this
     * <em>view's</em> width.
     * @param topOffset The top offset of the scanning bounds in pixels.
     * @return The coordinates of the scanning bounds in pixels.
     * @see #computeScanningBounds(int, int, int)
     */
    public final Rect computeScanningBounds(float percent, int topOffset) {
        final int width = (int)(getWidth() * percent);
        return computeScanningBounds(width, width, topOffset);
    }

    /**
     * Computes the coordinates of the scanning bounds on the screen.
     * @param width The width of the scanning bounds in pixels.
     * @param height The height of the scanning bounds in pixels.
     * @param topOffset The top offset of the scanning bounds in pixels.
     * @return The coordinates of the scanning bounds in pixels.
     * @see #computeScanningBounds(float, int)
     */
    public Rect computeScanningBounds(int width, int height, int topOffset) {
        final int viewWidth = getWidth();
        final int left = (viewWidth - width) / 2;
        final int top  = (getHeight() - height) / 2 + topOffset;
        mScanningBounds.set(left, top, left + width, top + height);
        invalidate();

        // Computes the coordinates of the mScanningBounds on the screen.
        final int[] offset = new int[2];
        getLocationOnScreen(offset);
        return new Rect(mScanningBounds.left + offset[0], mScanningBounds.top + offset[1], mScanningBounds.right + offset[0], mScanningBounds.bottom + offset[1]);
    }

    /**
     * Returns the scanning indicator <tt>Drawable</tt>.
     * @return The scanning indicator <tt>Drawable</tt>, or
     * <tt>null</tt> if this view has no scanning indicator.
     * @see #setScanningIndicator(int)
     * @see #setScanningIndicator(Drawable)
     */
    public Drawable getScanningIndicator() {
        return mScanningIndicator;
    }

    /**
     * Sets the scanning indicator to a given the <tt>Drawable</tt>.
     * @param indicator The <tt>Drawable</tt> to use as the scanning
     * indicator, or <tt>null</tt> to remove the scanning indicator.
     * @see #getScanningIndicator()
     * @see #setScanningIndicator(int)
     */
    public void setScanningIndicator(Drawable indicator) {
        if (mScanningIndicator != indicator) {
            mScanningIndicator = indicator;
            if (indicator != null) {
                final int height = indicator.getIntrinsicHeight();
                if (height > 0) {
                    mScanningIndicatorHeight = height;
                }
            }

            invalidate();
        }
    }

    /**
     * Sets a drawable as the scanning indicator.
     * @param resId The resource id of the drawable to load.
     * @see #getScanningIndicator()
     * @see #setScanningIndicator(Drawable)
     */
    @SuppressWarnings("deprecation")
    public final void setScanningIndicator(int resId) {
        setScanningIndicator(getResources().getDrawable(resId));
    }

    /**
     * Returns the height of the scanning indicator in pixels.
     * @return The height of the scanning indicator.
     * @see #setScanningIndicatorHeight(int)
     */
    public int getScanningIndicatorHeight() {
        return mScanningIndicatorHeight;
    }

    /**
     * Sets the height of the scanning indicator.
     * @param height The new height of the scanning indicator in pixels.
     * @see #getScanningIndicatorHeight()
     */
    public void setScanningIndicatorHeight(int height) {
        if (mScanningIndicatorHeight != height) {
            mScanningIndicatorHeight = height;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mScanningBounds.isEmpty()) {
            return;
        }

        final int left   = mScanningBounds.left;
        final int top    = mScanningBounds.top;
        final int right  = mScanningBounds.right;
        final int bottom = mScanningBounds.bottom;
        final int width  = getWidth(), height = getHeight();

        // Draw the mask color.
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mMaskColor);
        canvas.drawRect(0, 0, width, top, mPaint);
        canvas.drawRect(0, top, left, bottom, mPaint);
        canvas.drawRect(0, bottom, width, height, mPaint);
        canvas.drawRect(right, top, width, bottom, mPaint);

        // Draw the scanning border.
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(mBorderColor);
        canvas.drawRect(left, top, right, bottom, mPaint);

        // Draw the scanning corner.
        mPaint.setStyle(Style.FILL);
        mPaint.setColor(mCornerColor);
        canvas.drawRect(left, top, left + mCornerWidth, top + mCornerHeight, mPaint);
        canvas.drawRect(left, top, left + mCornerHeight, top + mCornerWidth, mPaint);
        canvas.drawRect(right - mCornerWidth, top, right, top + mCornerHeight, mPaint);
        canvas.drawRect(right - mCornerHeight, top, right, top + mCornerWidth, mPaint);
        canvas.drawRect(left, bottom - mCornerWidth, left + mCornerHeight, bottom, mPaint);
        canvas.drawRect(left, bottom - mCornerHeight, left + mCornerWidth, bottom, mPaint);
        canvas.drawRect(right - mCornerWidth, bottom - mCornerHeight, right, bottom, mPaint);
        canvas.drawRect(right - mCornerHeight, bottom - mCornerWidth, right, bottom, mPaint);

        // Draw the text.
        drawText(canvas, left, top, right, bottom, mPaint);

        // Draw the scanning indicator.
        drawScanningIndicator(canvas, left, top, right, bottom);
    }

    /**
     * Draws the text using the specified <em>paint</em>.
     * @param canvas The <tt>Canvas</tt>.
     * @param left The specified a bounding rectangle left coordinate for the scanning bounds.
     * @param top The specified a bounding rectangle top coordinate for the scanning bounds.
     * @param right The specified a bounding rectangle right coordinate for the scanning bounds.
     * @param bottom The specified a bounding rectangle bottom coordinate for the scanning bounds.
     * @param paint The <tt>Paint</tt> used to draw the <em>text</em> (e.g. color, size, style).
     */
    protected void drawText(Canvas canvas, int left, int top, int right, int bottom, Paint paint) {
        final int length = StringUtils.getLength(mText);
        if (length > 0) {
            paint.setColor(mTextColor);
            DrawUtils.drawText(canvas, paint, mText, 0, length, 0, bottom + mTextOffset, getWidth(), getHeight(), Gravity.TOP | Gravity.CENTER_HORIZONTAL);
        }
    }

    /**
     * Draws the scanning indicator with in rect (left, top, right, bottom) coordinates.
     * @param canvas The <tt>Canvas</tt>.
     * @param left The specified a bounding rectangle left coordinate for the scanning bounds.
     * @param top The specified a bounding rectangle top coordinate for the scanning bounds.
     * @param right The specified a bounding rectangle right coordinate for the scanning bounds.
     * @param bottom The specified a bounding rectangle bottom coordinate for the scanning bounds.
     */
    protected void drawScanningIndicator(Canvas canvas, int left, int top, int right, int bottom) {
        if (mScanningIndicator != null) {
            mScanningIndicator.setBounds(left, top + mScanningIndicatorOffset, right, top + mScanningIndicatorOffset + mScanningIndicatorHeight);
            mScanningIndicator.draw(canvas);
            mScanningIndicatorOffset = (mScanningIndicatorOffset + 5) % (bottom - top - mScanningIndicatorHeight);
            postInvalidate(left, top, right, bottom);
        }
    }

    private static float getDimension(String packageName, String name, TypedArray array, float defaultValue, int unit, DisplayMetrics dm) {
        final float value = array.getDimension(ReflectUtils.getResourceStyleable(packageName, name), 0);
        return (value != 0f ? value : TypedValue.applyDimension(unit, defaultValue, dm));
    }
}
