package android.ext.temp;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Class VerticalViewPager
 * @author Garfield
 * @version 1.5
 */
public class VerticalViewPager extends ViewPager {
    public VerticalViewPager(Context context) {
        super(context);
    }

    public VerticalViewPager(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    public VerticalViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void scrollToPage(int whichPage, float velocity, boolean animation) {
        if (mScrollPage != whichPage && mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(this, mScrollPage, whichPage);
        }

        mScrollPage  = whichPage;
        final int dy = whichPage * getHeight() - getScrollY();
        if (dy != 0) {
            if (animation) {
                mScroller.startScroll(0, getScrollY(), 0, dy, computeScrollDuration(dy, getHeight(), velocity));
            } else {
                scrollBy(0, dy);
            }
            invalidate();
        }
    }

    @Override
    public boolean isScrolling() {
        final int height = getHeight();
        return (mScrolling || (height != 0 && getScrollY() % height != 0));
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(0, mScroller.getCurrY());
            postInvalidate();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mGestureDetector.onTouchEvent(event)) {
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                onFling(0, getScrollY());
            }
        }

        return true;
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        mScrolling = true;
        final float y = event2.getY();
        final int deltaY = (int)(mLastMotionY - y);
        mLastMotionY = y;
        onScroll(0, getScrollY(), 0, deltaY, getHeight());

        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        onFling(velocityY, getScrollY());
        return true;
    }

    @Override
    protected boolean computeScrolling(MotionEvent event) {
        final float deltaY = Math.abs(event.getY() - mLastMotionY);
        return (deltaY > Math.abs(event.getX() - mLastMotionX) && deltaY > mTouchSlop);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int padding = getPaddingTop() + getPaddingBottom();
        final int height = b - t - padding;
        final int left = getPaddingLeft(), right = r - t - getPaddingRight();

        for (int i = 0, top = getPaddingTop(), count = getChildCount(); i < count; ++i) {
            getChildAt(i).layout(left, top, right, top += height);
            top += padding;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        scrollTo(0, mScrollPage * getHeight());
    }
}