package android.ext.temp;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.tencent.test.R;

/**
 * Class ViewPager
 * @author Garfield
 */
public class ViewPager extends ViewGroup implements OnGestureListener {
    private static final float MIN_VELOCITY = 600.0f;

    protected float mExtent;
    protected int mExtentSize;

    protected int mTouchSlop;
    protected int mScrollPage;
    protected boolean mScrolling;

    protected float mLastMotionX;
    protected float mLastMotionY;

    protected Scroller mScroller;
    protected GestureDetector mGestureDetector;

    protected OnScrollListener mOnScrollListener;
    protected OnPageChangeListener mOnPageChangeListener;

    public ViewPager(Context context) {
        super(context);
        initView(context);
    }

    public ViewPager(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewPager(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPager);
        mScrollPage = a.getInteger(R.styleable.ViewPager_defaultPage, 0);
        final TypedValue value = a.peekValue(R.styleable.ViewPager_extent);
        if (value != null) {
            switch (value.type) {
            case TypedValue.TYPE_FLOAT:
                mExtent = value.getFloat();
                break;

            case TypedValue.TYPE_DIMENSION:
                mExtentSize = (int)value.getDimension(getResources().getDisplayMetrics());
                break;
            }
        }

        a.recycle();
    }

    /**
     * Scroll to the specify specified page. This will cause a call to
     * {@link #computeScrollDuration(int, int, float)}.
     * @param whichPage The index of page.
     * @param animation Whether the <tt>ViewPager</tt> should be scrolled
     * with an animation. 
     * @see #scrollToNextPage(boolean)
     * @see #scrollToPrevPage(boolean)
     * @see #scrollToPage(int, float, boolean)
     */
    public final void scrollToPage(int whichPage, boolean animation) {
        scrollToPage(whichPage, 0, animation);
    }

    /**
     * Scroll to the specify specified page. This will cause a call to
     * {@link #computeScrollDuration(int, int, float)}.
     * @param whichPage The index of page.
     * @param velocity The velocity in pixels per second along the x axis or y axis.
     * @param animation Whether the <tt>ViewPager</tt> should be scrolled with an animation.
     * @see #scrollToNextPage(boolean)
     * @see #scrollToPrevPage(boolean)
     * @see #scrollToPage(int, boolean)
     */
    public void scrollToPage(int whichPage, float velocity, boolean animation) {
        if (mScrollPage != whichPage && mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageSelected(this, mScrollPage, whichPage);
        }

        mScrollPage  = whichPage;
        final int dx = whichPage * getWidth() - getScrollX();
        if (dx != 0) {
            if (animation) {
                mScroller.startScroll(getScrollX(), 0, dx, 0, computeScrollDuration(dx, getWidth(), velocity));
            } else {
                scrollBy(dx, 0);
            }
            invalidate();
        }
    }

    /**
     * Scroll to the next page. This will cause a call to {@link #computeScrollDuration(int, int, float)}.
     * @param animation Whether the <tt>ViewPager</tt> should be scrolled with an animation.
     * @return <tt>true</tt> if can scroll to the next page, <tt>false</tt> otherwise.
     * @see #scrollToPrevPage(boolean)
     * @see #scrollToPage(int, boolean)
     * @see #scrollToPage(int, float, boolean)
     */
    public final boolean scrollToNextPage(boolean animation) {
        final int nextPage = mScrollPage + 1;
        final boolean canScroll = (nextPage < getChildCount());
        if (canScroll) {
            scrollToPage(nextPage, 0, animation);
        }

        return canScroll;
    }

    /**
     * Scroll to the previous page. This will cause a call to {@link #computeScrollDuration(int, int, float)}.
     * @param animation Whether the <tt>ViewPager</tt> should be scrolled with an animation.
     * @return <tt>true</tt> if can scroll to the previous page, <tt>false</tt> otherwise.
     * @see #scrollToNextPage(boolean)
     * @see #scrollToPage(int, boolean)
     * @see #scrollToPage(int, float, boolean)
     */
    public final boolean scrollToPrevPage(boolean animation) {
        final int prevPage = mScrollPage - 1;
        final boolean canScroll = (prevPage >= 0);
        if (canScroll) {
            scrollToPage(prevPage, 0, animation);
        }

        return canScroll;
    }

    /**
     * Tests if this view is scrolling.
     * @return <tt>true</tt> if this view is scrolling, <tt>false</tt> otherwise.
     * @see #stopScrolling()
     */
    public boolean isScrolling() {
        final int width = getWidth();
        return (mScrolling || (width != 0 && getScrollX() % width != 0));
    }

    /**
     * Stops the scrolling.
     * @see #isScrolling()
     */
    public final void stopScrolling() {
        if (!mScroller.isFinished()) {
            mScroller.abortAnimation();
        }
    }

    /**
     * Returns the index of page where was scrolled.
     * @return The index of page.
     * @see #getScrollPageView()
     */
    public final int getScrollPage() {
        return mScrollPage;
    }

    /**
     * Returns the child view where was scrolled.
     * @return The child view.
     * @see #getScrollPage()
     */
    public final View getScrollPageView() {
        return getChildAt(mScrollPage);
    }

    public final void setOnScrollListener(OnScrollListener listener) {
        mOnScrollListener = listener;
    }

    public final void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), 0);
            postInvalidate();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
        case MotionEvent.ACTION_DOWN:
            mLastMotionX = event.getX();
            mLastMotionY = event.getY();
            mScrolling = !mScroller.isFinished();
            break;

        case MotionEvent.ACTION_MOVE:
            mScrolling = computeScrolling(event);
            break;

        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_CANCEL:
            mScrolling = false;
            break;
        }

        return mScrolling;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!mGestureDetector.onTouchEvent(event)) {
            final int action = event.getAction() & MotionEvent.ACTION_MASK;
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                onFling(0, getScrollX());
            }
        }

        return true;
    }

    @Override
    public boolean onDown(MotionEvent event) {
        stopScrolling();
        mLastMotionX = event.getX();
        mLastMotionY = event.getY();
        return true;
    }

    @Override
    public void onShowPress(MotionEvent event) {
    }

    @Override
    public void onLongPress(MotionEvent event) {
        if (isLongClickable()) {
            performLongClick();
        }
    }

    @Override
    public boolean onSingleTapUp(MotionEvent event) {
        return (isClickable() && performClick());
    }

    @Override
    public boolean onScroll(MotionEvent event1, MotionEvent event2, float distanceX, float distanceY) {
        mScrolling = true;
        final float x = event2.getX();
        final int deltaX = (int)(mLastMotionX - x);
        mLastMotionX = x;
        onScroll(getScrollX(), 0, deltaX, 0, getWidth());

        return true;
    }

    @Override
    public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
        onFling(velocityX, getScrollX());
        return true;
    }

    /**
     * Computes this view is scrolling.
     * @param event The {@link MotionEvent}.
     * @return <tt>true</tt> if this view is scrolling, <tt>false</tt> otherwise.
     */
    protected boolean computeScrolling(MotionEvent event) {
        final float deltaX = Math.abs(event.getX() - mLastMotionX);
        return (deltaX > Math.abs(event.getY() - mLastMotionY) && deltaX > mTouchSlop);
    }

    /**
     * Computes the scroll duration when scrolling this view.
     * @param distance The scroll distance.
     * @param size This view width or height.
     * @param velocity The velocity in pixels per second along the x axis or y axis.
     * @return The duration of the scroll in milliseconds.
     */
    protected int computeScrollDuration(int distance, int size, float velocity) {
        distance = Math.abs(distance);
        velocity = Math.abs(velocity);

        int duration = 500;
        if (distance > 0 && velocity > 0) {
            final int halfSize = size / 2;
            final float delta = halfSize + halfSize * distanceInfluenceScrollDuration(Math.min(1.0f, 1.0f * distance / size));
            duration = Math.min(3 * (int)(1000 * Math.abs(delta / velocity) + 0.5f), duration);
        }

        return duration;
    }

    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);
        if (mOnScrollListener != null) {
            mOnScrollListener.onScrollChanged(this, x, y, oldX, oldY);
        }

        if (mOnPageChangeListener != null && !isScrolling()) {
            mOnPageChangeListener.onPageScrolled(this, mScrollPage);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        final int padding = getPaddingLeft() + getPaddingRight();
        final int width = r - l - padding;
        final int top = getPaddingTop(), bottom = b - t - getPaddingBottom();

        for (int i = 0, left = getPaddingLeft(), count = getChildCount(); i < count; ++i) {
            getChildAt(i).layout(left, top, left += width, bottom);
            left += padding;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),
                getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
        measureChildren(widthMeasureSpec, heightMeasureSpec);
        scrollTo(mScrollPage * getWidth(), 0);
    }

    protected final void onFling(float velocity, int scrollPos) {
        int whichPage = 0;
        if (velocity > MIN_VELOCITY && mScrollPage > 0) {
            whichPage = mScrollPage - 1;
        } else if (velocity < -MIN_VELOCITY && mScrollPage < getChildCount() - 1) {
            whichPage = mScrollPage + 1;
        } else {
            whichPage = Math.min((scrollPos + getWidth() / 2) / getWidth(), getChildCount() - 1);
        }

        mScrolling = false;
        scrollToPage(whichPage, velocity, true);
    }

    protected final void onScroll(int scrollX, int scrollY, int deltaX, int deltaY, int size) {
        final int extentSize = (mExtentSize > 0 ? mExtentSize : (int)(mExtent * size));
        int x = scrollX, y = scrollY, scrollRange = getChildCount() * size;
        if (deltaX < 0) {
            // Scroll to left.
            x += Math.max(-scrollX - extentSize, deltaX);
        } else if (deltaX > 0) {
            // Scroll to right.
            x += Math.min(scrollRange - scrollX - size + extentSize, deltaX);
        } else if (deltaY < 0) {
            // Scroll to up.
            y += Math.max(-scrollY - extentSize, deltaY);
        } else if (deltaY > 0) {
            // Scroll to down.
            y += Math.min(scrollRange - scrollY - size + extentSize, deltaY);
        }

        scrollTo(x, y);
        if (mOnScrollListener != null) {
            if (x < 0 || y < 0) {
                mOnScrollListener.onExtentScrolled(x, y);
            } else if ((x + size) > scrollRange) {
                mOnScrollListener.onExtentScrolled((x + size - scrollRange), 0);
            } else if ((y + size) > scrollRange) {
                mOnScrollListener.onExtentScrolled(0, (y + size - scrollRange));
            }
        }
    }

    protected final float distanceInfluenceScrollDuration(float input) {
        return (float)Math.sin((input - 0.5f) * 0.3 * Math.PI / 2.0);
    }

    private void initView(Context context) {
        mScroller  = new Scroller(context);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mGestureDetector = new GestureDetector(context, this);
    }

    /**
     * Used for being notified when the view is scrolling.
     * @see OnScrollListener#onScrollChanged(ViewPager, int, int, int, int)
     */
    public static interface OnScrollListener {
        /**
         * Callback method to be invoked when the view scrolling
         * beyond the normal content boundaries.
         * @param extentX The extent X in pixels.
         * @param extentY The extent Y in pixels.
         */
        void onExtentScrolled(int extentX, int extentY);

        /**
         * Callback method to be invoked when the view is scrolling.
         * @param view The {@link ViewPager} whose view is scrolling.
         * @param x The current horizontal scroll origin.
         * @param y The current vertical scroll origin.
         * @param oldX The previous horizontal scroll origin.
         * @param oldY The previous vertical scroll origin.
         */
        void onScrollChanged(ViewPager view, int x, int y, int oldX, int oldY);
    }

    /**
     * Used for being notified when the view has been scrolled.
     * @see OnPageChangeListener#onPageScrolled(ViewPager, int)
     * @see OnPageChangeListener#onPageSelected(ViewPager, int, int)
     */
    public static interface OnPageChangeListener {
        /**
         * Callback method to be invoked when the current page is scrolled.
         * @param view The {@link ViewPager} whose page is being scrolled.
         * @param page The index of page.
         * @see #onPageSelected(ViewPager, int, int)
         */
        void onPageScrolled(ViewPager view, int page);

        /**
         * Callback method to be invoked when a new page becomes selected.
         * @param view The {@link ViewPager} whose page is being selected.
         * @param oldPage The index of the old page.
         * @param newPage The index of the new page.
         * @see #onPageScrolled(ViewPager, int)
         */
        void onPageSelected(ViewPager view, int oldPage, int newPage);
    }
}