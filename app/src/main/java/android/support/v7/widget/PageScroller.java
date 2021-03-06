package android.support.v7.widget;

import static android.support.v7.widget.LinearLayoutManager.HORIZONTAL;
import static android.support.v7.widget.RecyclerView.SCROLL_STATE_IDLE;
import android.ext.widget.LayoutManagerHelper;
import android.graphics.Rect;
import android.view.View;

/**
 * Class PageScroller
 * @author Garfield
 */
public class PageScroller {
    /* package */ int mCurrentPage;
    /* package */ final int mPageSize;

    /* package */ final LinearLayoutManager mLayout;
    /* package */ OnPageChangeListener mOnPageChangeListener;

    /**
     * Constructor
     * @param layout The {@link LinearLayoutManager}.
     * @param pageSize The page size in pixels.
     */
    public PageScroller(LinearLayoutManager layout, int pageSize) {
        mLayout = layout;
        mPageSize = pageSize;
    }

    /**
     * Returns the current page index.
     * @return The current page index.
     * @see #getPageSize()
     */
    public final int getCurrentPage() {
        return mCurrentPage;
    }

    /**
     * Returns the page size in pixels.
     * @return The page size.
     * @see #getCurrentPage()
     */
    public final int getPageSize() {
        return mPageSize;
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
    public boolean scrollToPage(int newPage, boolean immediate) {
        final boolean canScroll = canScroll(newPage);
        if (canScroll) {
            int dx = 0, dy = 0, offset = (newPage - mCurrentPage) * mPageSize;
            if (mLayout.mOrientation == HORIZONTAL) {
                dx = offset;
            } else {
                dy = offset;
            }

            if (immediate || mLayout.mRecyclerView.getScrollState() != SCROLL_STATE_IDLE) {
                mLayout.mRecyclerView.scrollBy(dx, dy);
            } else {
                mLayout.mRecyclerView.smoothScrollBy(dx, dy);
            }

            // Dispatch the current page changed.
            dispatchPageChanged(newPage, mCurrentPage);
        }

        return canScroll;
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
        return scrollToPage(mCurrentPage + 1, immediate);
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
        return scrollToPage(mCurrentPage - 1, immediate);
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param position The position of the item in the data set of the adapter.
     * @param countPerPage The item count of per-page to display.
     */
    public void requestItemFocus(int position, int countPerPage) {
        if (position >= 0 && position < mLayout.getItemCount()) {
            final View child = mLayout.findViewByPosition(position);
            if (child == null) {
                final int newPage = position / countPerPage;
                mLayout.scrollToPositionWithOffset(newPage * countPerPage, 0);

                // Dispatch the current page changed.
                if (mCurrentPage != newPage) {
                    dispatchPageChanged(newPage, mCurrentPage);
                }
            }

            LayoutManagerHelper.requestItemFocus(mLayout, position);
        }
    }

    public final void setOnPageChangeListener(OnPageChangeListener listener) {
        mOnPageChangeListener = listener;
    }

    /**
     * Scroll horizontally the child of the <tt>RecyclerView</tt> be positioned onto the screen.
     * <p>Note: This method recommended call in the {@link LinearLayoutManager#requestChildRectangleOnScreen}.</p>
     * @param parent The {@link RecyclerView}.
     * @param child The child making the request.
     * @param rect The rectangle in the child's coordinates the child wishes to be on the screen.
     * @param immediate <tt>true</tt> to forbid animated or delayed scrolling, <tt>false</tt> otherwise.
     * @return Whether the group scrolled to handle the operation.
     * @see #scrollVertically(RecyclerView, View, Rect, boolean)
     */
    public boolean scrollHorizontally(RecyclerView parent, View child, Rect rect, boolean immediate) {
        // Gets the parent left and right.
        final int parentLeft, parentRight;
        if (mLayout.getClipToPadding()) {
            parentLeft  = parent.getPaddingLeft();
            parentRight = parent.getWidth() - parent.getPaddingRight();
        } else {
            parentLeft  = 0;
            parentRight = parent.getWidth();
        }

        // Gets the child left and right relative to it's parent.
        final int childLeft  = child.getLeft() + rect.left - child.getScrollX();
        final int childRight = childLeft + rect.width();

        // Gets the offscreen left and right.
        final int offsetLeft = childLeft - parentLeft;
        final int offScreenLeft  = Math.min(0, offsetLeft);
        final int offScreenRight = Math.max(0, childRight - parentRight);

        final int dx = (offScreenLeft != 0 ? offScreenLeft : Math.min(offsetLeft, offScreenRight));
        if (dx > 0) {
            // scroll to next page.
            return scrollToPage(mCurrentPage + 1, immediate);
        } else if (dx < 0) {
            // scroll to previous page.
            return scrollToPage(mCurrentPage - 1, immediate);
        } else {
            // no scroll.
            return false;
        }
    }

    /**
     * Scroll vertically the child of the <tt>RecyclerView</tt> be positioned onto the screen.
     * <p>Note: This method recommended call in the {@link LinearLayoutManager#requestChildRectangleOnScreen}.</p>
     * @param parent The {@link RecyclerView}.
     * @param child The child making the request.
     * @param rect The rectangle in the child's coordinates the child wishes to be on the screen.
     * @param immediate <tt>true</tt> to forbid animated or delayed scrolling, <tt>false</tt> otherwise.
     * @return Whether the group scrolled to handle the operation.
     * @see #scrollHorizontally(RecyclerView, View, Rect, boolean)
     */
    public boolean scrollVertically(RecyclerView parent, View child, Rect rect, boolean immediate) {
        // Gets the parent top and bottom.
        final int parentTop, parentBottom;
        if (mLayout.getClipToPadding()) {
            parentTop    = parent.getPaddingTop();
            parentBottom = parent.getHeight() - parent.getPaddingBottom();
        } else {
            parentTop    = 0;
            parentBottom = parent.getHeight();
        }

        // Gets the child top and bottom relative to it's parent.
        final int childTop    = child.getTop() + rect.top - child.getScrollY();
        final int childBottom = childTop + rect.height();

        // Gets the offscreen top and bottom.
        final int offsetTop = childTop - parentTop;
        final int offScreenTop    = Math.min(0, offsetTop);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom);

        final int dy = (offScreenTop != 0 ? offScreenTop : Math.min(offsetTop, offScreenBottom));
        if (dy > 0) {
            // scroll to next page.
            return scrollToPage(mCurrentPage + 1, immediate);
        } else if (dy < 0) {
            // scroll to previous page.
            return scrollToPage(mCurrentPage - 1, immediate);
        } else {
            // no scroll.
            return false;
        }
    }

    /**
     * Returns the adapter position of visible view from <tt>fromIndex</tt> to <tt>toIndex</tt>.
     * <p>Note: This method recommended call in the {@link LinearLayoutManager#findOneVisibleChild}.</p>
     */
    public View findOneVisibleChild(int fromIndex, int toIndex, boolean completelyVisible, boolean acceptPartiallyVisible) {
        mLayout.ensureLayoutState();
        final int start, end;
        if (mLayout.getClipToPadding()) {
            start = mLayout.mOrientationHelper.getStartAfterPadding();
            end   = mLayout.mOrientationHelper.getEndAfterPadding();
        } else {
            start = 0;
            end   = mLayout.mOrientationHelper.getEnd();
        }

        View partiallyVisible = null;
        for (int i = toIndex > fromIndex ? 1 : -1; fromIndex != toIndex; fromIndex += i) {
            final View child = mLayout.getChildAt(fromIndex);
            final int childStart = mLayout.mOrientationHelper.getDecoratedStart(child);
            final int childEnd = mLayout.mOrientationHelper.getDecoratedEnd(child);
            if (childStart < end && childEnd > start) {
                if (completelyVisible) {
                    if (childStart >= start && childEnd <= end) {
                        return child;
                    } else if (acceptPartiallyVisible && partiallyVisible == null) {
                        partiallyVisible = child;
                    }
                } else {
                    return child;
                }
            }
        }

        return partiallyVisible;
    }

    private boolean canScroll(int newPage) {
        if (newPage >= 0 && mCurrentPage != newPage && mLayout.mRecyclerView != null) {
            final int direction = (mCurrentPage > newPage ? -1 : 1);
            if (mLayout.mOrientation == HORIZONTAL) {
                return mLayout.mRecyclerView.canScrollHorizontally(direction);
            } else {
                return mLayout.mRecyclerView.canScrollVertically(direction);
            }
        }

        return false;
    }

    private void dispatchPageChanged(int newPage, int oldPage) {
        mCurrentPage = newPage;
        if (mOnPageChangeListener != null) {
            mOnPageChangeListener.onPageChanged(mLayout.mRecyclerView, newPage, oldPage);
        }
    }

    /**
     * Callback interface for responding to changing state of the selected page.
     */
    public static interface OnPageChangeListener {
        /**
         * This method will be invoked when a new page becomes selected. Animation
         * is not necessarily complete.
         * @param recyclerView The {@link RecyclerView} whose page becomes selected.
         * @param newPage The index of the new page.
         * @param oldPage The index of the old page.
         */
        void onPageChanged(RecyclerView recyclerView, int newPage, int oldPage);
    }
}
