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
 * @author Garfield
 */
public class NumericView extends View {
    private int mDotWidth;
    private int mNumberWidth;
    private int mNumberHeight;
    private int mHorizontalMargin;

    private String mValue;
    private final Value[] mValues;

    public NumericView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NumericView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final String packageName = context.getPackageName();
        final TypedArray a = context.obtainStyledAttributes(attrs, ReflectUtils.getResourceStyleable(packageName, "NumericView"));
        final String value = a.getString(ReflectUtils.getResourceStyleable(packageName, "NumericView_value"));
        mValue  = (value != null ? value : "");
        mValues = initValues(a.getResourceId(ReflectUtils.getResourceStyleable(packageName, "NumericView_drawables"), 0));
        mDotWidth = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_dotWidth"), 0);
        mNumberWidth  = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_numberWidth"), 0);
        mNumberHeight = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_numberHeight"), 0);
        mHorizontalMargin = a.getDimensionPixelOffset(ReflectUtils.getResourceStyleable(packageName, "NumericView_horizontalMargin"), 0);
        a.recycle();

        final Resources res = context.getResources();
        if (mNumberWidth == 0) {
            mNumberWidth = mValues[0].getDrawable(res).getIntrinsicWidth();
        }

        if (mNumberHeight == 0) {
            mNumberHeight = mValues[0].getDrawable(res).getIntrinsicHeight();
        }

        if (mDotWidth == 0) {
            mDotWidth = mValues[mValues.length - 1].getDrawable(res).getIntrinsicWidth();
        }
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
        if (TextUtils.isEmpty(value)) {
            value = "";
        } else {
            // Check the value is a number.
            Float.parseFloat(value);
        }

        setValueInternal(value);
    }

    /**
     * Equivalent to calling <tt>setValue(Integer.toString(value))</tt>.
     * @see #setValue(float)
     * @see #setValue(String)
     */
    public final void setValue(int value) {
        setValueInternal(Integer.toString(value));
    }

    /**
     * Equivalent to calling <tt>setValue(Float.toString(value))</tt>.
     * @see #setValue(int)
     * @see #setValue(String)
     */
    public final void setValue(float value) {
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
     * Returns the width of the number drawable.
     * @return The width of the number drawable, in pixels.
     */
    public int getNumberWidth() {
        return mNumberWidth;
    }

    /**
     * Sets the width of the number drawable.
     * @param width The width of the number drawable, in pixels.
     */
    public void setNumberWidth(int width) {
        if (mNumberWidth != width) {
            mNumberWidth = width;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the height of the number drawable.
     * @return The height of the number drawable, in pixels.
     */
    public int getNumberHeight() {
        return mNumberHeight;
    }

    /**
     * Sets the height of the number drawable.
     * @param height The height of the number drawable, in pixels.
     */
    public void setNumberHeight(int height) {
        if (mNumberHeight != height) {
            mNumberHeight = height;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Returns the horizontal margin between each number drawable in this view.
     * @return The horizontal margin, in pixels.
     */
    public int getHorizontalMargin() {
        return mHorizontalMargin;
    }

    /**
     * Sets the horizontal margin between each number drawable in this view.
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
        DeviceUtils.dumpSummary(printer, result, 100, " Dumping NumericView [ value = %s, size = %d ] ", mValue, mValues.length);
        for (int i = 0; i < mValues.length; ++i) {
            result.setLength(0);
            final Value value = mValues[i];
            printer.println(result.append("  [ index = ").append(i).append(", id = 0x").append(Integer.toHexString(value.id)).append(", drawable = ").append(value.drawable).append(" ]").toString());
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
        final int bottom = top + mNumberHeight;

        for (int i = 0, left = getPaddingLeft(); i < length; ++i) {
            final char c = mValue.charAt(i);
            final int width, index;
            if (c == '.') {
                width = mDotWidth;
                index = mValues.length - 1;
            } else {
                index = c - '0';
                width = mNumberWidth;
            }

            final Drawable drawable = mValues[index].getDrawable(res);
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
            final Drawable drawable = mValues[c == '.' ? mValues.length - 1 : c - '0'].getDrawable(res);
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
            measuredWidth  = getPaddingLeft() + getPaddingRight() + (mNumberWidth + mHorizontalMargin) * (length - 1) + (mValue.indexOf('.') == -1 ? mNumberWidth : mDotWidth);
            measuredHeight = getPaddingTop() + getPaddingBottom() + mNumberHeight;
        }

        setMeasuredDimension(measuredWidth, measuredHeight);
    }

    private Value[] initValues(int id) {
        DebugUtils.__checkError(id == 0, "The <NumericView> tag requires a valid 'drawables' attribute");
        final TypedArray a = getResources().obtainTypedArray(id);
        final int length = a.length();
        DebugUtils.__checkError(length < 10, "The drawable array must be >= 10");

        final Value[] values = new Value[length];
        for (int i = 0; i < length; ++i) {
            values[i] = new Value(a.getResourceId(i, 0));
        }

        a.recycle();
        return values;
    }

    private void setValueInternal(String value) {
        if (!mValue.equals(value)) {
            mValue = value;
            requestLayout();
            invalidate();
        }
    }

    /**
     * Class <tt>Value</tt> wrapped a drawable and a resource id.
     */
    private static final class Value {
        /* package */ final int id;
        /* package */ Drawable drawable;

        public Value(int id) {
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
    }
}
