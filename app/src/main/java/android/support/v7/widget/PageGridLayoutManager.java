package android.support.v7.widget;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.PageScroller.OnPageChangeListener;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

/**
 * Class PageGridLayoutManager
 * @author Garfield
 */
public class PageGridLayoutManager extends GridLayoutManager {
    protected final PageScroller mScroller;

    /**
     * Creates a vertical <tt>PageGridLayoutManager</tt>.
     * @param context The <tt>Context</tt>.
     * @param spanCount The number of columns in the grid.
     * @param pageSize The page size in pixels.
     * @see #PageGridLayoutManager(Context, int, int, boolean, int)
     */
    public PageGridLayoutManager(Context context, int spanCount, int pageSize) {
        super(context, spanCount);
        mScroller = new PageScroller(this, pageSize);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param spanCount The number of columns in the grid.
     * @param orientation Layout orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     * @param reverseLayout When set to <tt>true</tt>, layouts from end to start.
     * @param pageSize The page size in pixels.
     * @see #PageGridLayoutManager(Context, int, int)
     */
    public PageGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout, int pageSize) {
        super(context, spanCount, orientation, reverseLayout);
        mScroller = new PageScroller(this, pageSize);
    }

    /**
     * Returns the current page index.
     * @return The current page index.
     * @see #getPageSize()
     */
    public final int getCurrentPage() {
        return mScroller.mCurrentPage;
    }

    /**
     * Returns the page size in pixels.
     * @return The page size.
     * @see #getCurrentPage()
     */
    public final int getPageSize() {
        return mScroller.mPageSize;
    }

    /**
     * Scroll to the specified <em>newPage</em>.
     * @param newPage The new page to scroll.
     * @param immediate <tt>true</tt> to forbid animated or delayed
     * scrolling, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the <tt>RecyclerView</tt> scrolled
     * to handle the operation, <tt>false</tt> otherwise.
     * @see #scrollToNextPage(boolean)
     * @see #scrollToPrevPage(boolean)
     */
    public final boolean scrollToPage(int newPage, boolean immediate) {
        return mScroller.scrollToPage(newPage, immediate);
    }

    /**
     * Scroll from the current page to next page.
     * @param immediate <tt>true</tt> to forbid animated or delayed
     * scrolling, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the <tt>RecyclerView</tt> scrolled
     * to handle the operation, <tt>false</tt> otherwise.
     * @see #scrollToPage(int, boolean)
     * @see #scrollToPrevPage(boolean)
     */
    public final boolean scrollToNextPage(boolean immediate) {
        return mScroller.scrollToPage(mScroller.mCurrentPage + 1, immediate);
    }

    /**
     * Scroll from the current page to previous page.
     * @param immediate <tt>true</tt> to forbid animated or delayed
     * scrolling, <tt>false</tt> otherwise.
     * @return <tt>true</tt> if the <tt>RecyclerView</tt> scrolled
     * to handle the operation, <tt>false</tt> otherwise.
     * @see #scrollToPage(int, boolean)
     * @see #scrollToNextPage(boolean)
     */
    public final boolean scrollToPrevPage(boolean immediate) {
        return mScroller.scrollToPage(mScroller.mCurrentPage - 1, immediate);
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param position The position of the item in the data set of the adapter.
     * @param countPerPage The item count of per-page to display.
     */
    public final void requestItemFocus(int position, int countPerPage) {
        mScroller.requestItemFocus(position, countPerPage);
    }

    public final void setOnPageChangeListener(OnPageChangeListener listener) {
        mScroller.mOnPageChangeListener = listener;
    }

    @Override
    public View onFocusSearchFailed(View focused, int focusDirection, Recycler recycler, State state) {
        // Returns the currently focused view when searching for a focusable view has failed.
        // This operation can be supported the RecyclerView has a fixed item count.
        return focused;
    }

    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
        return (mOrientation == HORIZONTAL ? mScroller.scrollHorizontally(parent, child, rect, immediate) : mScroller.scrollVertically(parent, child, rect, immediate));
    }

    @Override
    /* package */ View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
        return mScroller.findOneVisibleChild(fromIndex, toIndex, completelyVisible, acceptPartiallyVisible);
    }
}
