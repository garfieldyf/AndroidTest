package android.ext.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.ext.util.ReflectUtils;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Printer;
import android.view.View;

/**
 * Class NumericView
 * <h3>Usage</h3>
 * <p>Here is a resource example:</p><pre>
 * &lt;xxx.widget.NumericView
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:layout_width="wrap_content"
 *     android:layout_height="wrap_content"
 *     app:digitWidth="40dp"
 *     app:digitHeight="60dp"
 *     app:dotWidth="20dp"
 *     app:value="51.1"
 *     app:horizontalMargin="5dp"
 *     app:drawables="@array/digit_drawables" /&gt;</pre>
 * @author Garfield
 */
public class NumericView extends View {
    private int mDotWidth;
    private int mDigitWidth;
    private int mDigitHeight;
    private int mHorizontalMargin;

    private String mValue;
    private final Digit[] mDigits;

    public NumericView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumericView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final String packageName = context.getPackageName();
        final TypedArray a = context.obtainStyledAttributes(attrs, ReflectUtils.getResourceStyleable(packageName, "NumericView"));
        mValue  = checkValue(a.getString(ReflectUtils.getResourceStyleable(packageName, "NumericView_value")));
        mDigits = initDigits(a.getResourceId(ReflectUtils.getResourceStyleable(packageName, "NumericView_drawables"), 0));
        mDotWidth = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_dotWidth"), 0);
        mDigitWidth  = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_digitWidth"), 0);
        mDigitHeight = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_digitHeight"), 0);
        mHorizontalMargin = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_horizontalMargin"), 0);
        a.recycle();

        final Resources res = context.getResources();
        if (mDigitWidth == 0) {
            mDigitWidth = mDigits[0].getDrawable(res).getIntrinsicWidth();
        }

        if (mDigitHeight == 0) {
            mDigitHeight = mDigits[0].getDrawable(res).getIntrinsicHeight();
        }

        if (mDotWidth == 0) {
            mDotWidth = mDigits[mDigits.length - 1].getDrawable(res).getIntrinsicWidth();
        }

        DebugUtils.__checkError(mDotWidth <= 0, "Invalid parameter - dotWidth(" + mDotWidth + ") must be > 0");
        DebugUtils.__checkError(mDigitWidth <= 0, "Invalid parameter - digitWidth(" + mDigitWidth + ") must be > 0");
        DebugUtils.__checkError(mDigitHeight <= 0, "Invalid parameter - digitHeight(" + mDigitHeight + ") must be > 0");
    }

    /**
     * Returns the currently displayed value.
     * @return The currently displayed value
     * or an empty string (<tt>0-length</tt>).
     */
    public String getValue() {
        return mValue;
    }

    /**
     * Sets the currently displayed value.
     * @param value The value to display.
     * @throws NumberFormatException if the <em>value</em> is not a number.
     * @see #setValue(int)
     * @see #setValue(float)
     */
    public void setValue(String value) {
        setValueInternal(checkValue(value));
    }

    /**
     * Equivalent to calling <tt>setValue(Integer.toString(value))</tt>.
     * @see #setValue(float)
     * @see #setValue(String)
     */
    public void setValue(int value) {
        setValueInternal(Integer.toString(value));
    }

    /**
     * Equivalent to calling <tt>setValue(Float.toString(value))</tt>.
     * @see #setValue(int)
     * @see #setValue(String)
     */
    public void setValue(float value) {
        setValueInternal(Float.toString(value));
    }

    /**
     * Returns the width of the dot (<tt>.</tt>) drawable.
     * @return The width of the dot drawable, in pixels.
     */
    public int getDotWidth() {
        return mDotWidth;
    }

    /**
     * Sets the width of the dot (<tt>.</tt>) drawable.
     * @param width The width of the dot drawable, in pixels.
     */
    public void setDotWidth(int width) {
        if (mDotWidth != width) {
            mDotWidth = width;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the width of the digit drawable.
     * @return The width of the digit drawable, in pixels.
     */
    public int getDigitWidth() {
        return mDigitWidth;
    }

    /**
     * Sets the width of the digit drawable.
     * @param width The width of the digit drawable, in pixels.
     */
    public void setDigitWidth(int width) {
        if (mDigitWidth != width) {
            mDigitWidth = width;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the height of the digit drawable.
     * @return The height of the digit drawable, in pixels.
     */
    public int getDigitHeight() {
        return mDigitHeight;
    }

    /**
     * Sets the height of the digit drawable.
     * @param height The height of the digit drawable, in pixels.
     */
    public void setDigitHeight(int height) {
        if (mDigitHeight != height) {
            mDigitHeight = height;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the horizontal margin between each digit drawable in this view.
     * @return The horizontal margin, in pixels.
     */
    public int getHorizontalMargin() {
        return mHorizontalMargin;
    }

    /**
     * Sets the horizontal margin between each digit drawable in this view.
     * @param horizontalMargin The horizontal margin, in pixels.
     */
    public void setHorizontalMargin(int horizontalMargin) {
        if (mHorizontalMargin != horizontalMargin) {
            mHorizontalMargin = horizontalMargin;
            requestLayout();
            invalidate();
        }
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(128);
        DeviceUtils.dumpSummary(printer, result, 100, " Dumping NumericView [ value = %s, size = %d ] ", mValue, mDigits.length);
        for (int i = 0; i < mDigits.length; ++i) {
            mDigits[i].dump(printer, (mDigits.length > 10 && i == mDigits.length - 1 ? '.' : (char)('0' + i)), result);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int length = mValue.length();
        if (length == 0) {
            return;
        }

        final Resources res = getResources();
        final int top = getPaddingTop();
        final int bottom = top + mDigitHeight;

        for (int i = 0, left = getPaddingLeft(); i < length; ++i) {
            final char c = mValue.charAt(i);
            final int width, index;
            if (c == '.') {
                width = mDotWidth;
                index = mDigits.length - 1;
            } else {
                index = c - '0';
                width = mDigitWidth;
            }

            final Drawable drawable = mDigits[index].getDrawable(res);
            drawable.setBounds(left, top, left + width, bottom);
            drawable.draw(canvas);
            left += width + mHorizontalMargin;
        }
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();

        final int length = mValue.length();
        if (length == 0) {
            return;
        }

        boolean changed = false;
        final Resources res  = getResources();
        final int[] stateSet = getDrawableState();
        for (int i = 0; i < length; ++i) {
            final char c = mValue.charAt(i);
            final Drawable drawable = mDigits[c == '.' ? mDigits.length - 1 : c - '0'].getDrawable(res);
            if (drawable.isStateful()) {
                changed |= drawable.setState(stateSet);
            }
        }

        if (changed) {
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int measuredWidth = 0, measuredHeight = 0;
        final int length = mValue.length();
        if (length > 0) {
            measuredWidth  = getPaddingLeft() + getPaddingRight() + (mDigitWidth + mHorizontalMargin) * (length - 1) + (mValue.indexOf('.') == -1 ? mDigitWidth : mDotWidth);
            measuredHeight = getPaddingTop() + getPaddingBottom() + mDigitHeight;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private Digit[] initDigits(int id) {
        DebugUtils.__checkError(id == 0, "The <NumericView> tag requires a valid 'drawables' attribute");
        final TypedArray a = getResources().obtainTypedArray(id);
        final int length = a.length();
        DebugUtils.__checkError(length < 10, "The drawable array must be >= 10");

        final Digit[] digits = new Digit[length];
        for (int i = 0; i < length; ++i) {
            digits[i] = new Digit(a.getResourceId(i, 0));
        }

        a.recycle();
        return digits;
    }

    private String checkValue(String value) {
        if (TextUtils.isEmpty(value)) {
            value = "";
        } else {
            // Check the value is a number.
            Float.parseFloat(value);
        }

        return value;
    }

    private void setValueInternal(String value) {
        if (!mValue.equals(value)) {
            mValue = value;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Class <tt>Digit</tt> wrapped a drawable and a resource id.
     */
    private static final class Digit {
        private final int id;
        private Drawable drawable;

        public Digit(int id) {
            DebugUtils.__checkError(id == 0, "Invalid drawable id");
            this.id = id;
        }

        @SuppressWarnings("deprecation")
        public final Drawable getDrawable(Resources res) {
            if (drawable == null) {
                drawable = res.getDrawable(id);
            }

            return drawable;
        }

        public final void dump(Printer printer, char digit, StringBuilder result) {
            result.setLength(0);
            printer.println(result.append("  [ digit = ").append(digit).append(", id = 0x").append(Integer.toHexString(id)).append(", drawable = ").append(drawable).append(" ]").toString());
        }
    }
}
