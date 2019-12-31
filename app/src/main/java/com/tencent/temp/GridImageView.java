package com.tencent.temp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import com.tencent.test.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Class GridImageView
 * @author Garfield
 */
public class GridImageView extends View {
    private int mColumnCount;
    private int mVerticalMargin;
    private int mHorizontalMargin;
    private final List<Drawable> mDrawables;

    public GridImageView(Context context) {
        super(context);
        mDrawables = new ArrayList<Drawable>(4);
    }

    public GridImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GridImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mDrawables = new ArrayList<Drawable>(4);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GridImageView);
        final int id = a.getResourceId(R.styleable.GridImageView_drawables, 0);
        mColumnCount = a.getInt(R.styleable.GridImageView_android_columnCount, 0);
        mVerticalMargin   = a.getDimensionPixelOffset(R.styleable.GridImageView_verticalMargin, 0);
        mHorizontalMargin = a.getDimensionPixelOffset(R.styleable.GridImageView_horizontalMargin, 0);
        a.recycle();

        if (id != 0) {
            final TypedArray array = context.getResources().obtainTypedArray(id);
            for (int i = 0, length = array.length(); i < length; ++i) {
                mDrawables.add(array.getDrawable(i));
            }

            array.recycle();
        }
    }

    public void clearDrawables() {
        mDrawables.clear();
        invalidate();
    }

    public void addDrawable(Drawable drawable) {
        mDrawables.add(drawable);
        invalidate();
    }

    public Drawable getDrawable(int index) {
        return mDrawables.get(index);
    }

    public void setDrawable(int index, Drawable drawable) {
        mDrawables.set(index, drawable);
        invalidate();
    }

    public int getColumnCount() {
        return mColumnCount;
    }

    public void setColumnCount(int count) {
        if (mColumnCount != count) {
            mColumnCount = count;
            invalidate();
        }
    }

    public int getVerticalMargin() {
        return mVerticalMargin;
    }

    public void setVerticalMargin(int verticalMargin) {
        if (mVerticalMargin != verticalMargin) {
            mVerticalMargin = verticalMargin;
            invalidate();
        }
    }

    public int getHorizontalMargin() {
        return mHorizontalMargin;
    }

    public void setHorizontalMargin(int horizontalMargin) {
        if (mHorizontalMargin != horizontalMargin) {
            mHorizontalMargin = horizontalMargin;
            invalidate();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        final int size = mDrawables.size();
        if (size <= 0 || mColumnCount <= 0) {
            return;
        }

        final int x = getPaddingLeft();
        final int bottom = getHeight() - getPaddingBottom();
        final int width  = (getWidth() - getPaddingRight() - x - (mColumnCount - 1) * mHorizontalMargin) / mColumnCount;

        int rowCount = size / mColumnCount;
        if ((size % mColumnCount) != 0) {
            ++rowCount;
        }

        for (int i = 0, index = 0, top = getPaddingTop(); i < rowCount; ++i) {
            if (top >= bottom) {
                return;
            }

            for (int j = 0, left = x; j < mColumnCount; ++j) {
                if (index >= size) {
                    return;
                }

                final Drawable drawable = mDrawables.get(index++);
                if (drawable != null) {
                    drawable.setBounds(left, top, left + width, top + width);
                    drawable.draw(canvas);
                }

                left += width + mHorizontalMargin;
            }

            top += width + mVerticalMargin;
        }
    }
}
