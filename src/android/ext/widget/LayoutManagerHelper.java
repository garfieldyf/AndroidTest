package android.ext.widget;

import android.content.res.Resources;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ChildDrawingOrderCallback;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.Printer;
import android.view.View;
import android.view.ViewGroup;

/**
 * Class LayoutManagerHelper
 * @author Garfield
 */
public final class LayoutManagerHelper {
    /**
     * Requests that the given child of the RecyclerView be positioned onto the screen. <p>Note: This method recommended
     * call in the {@link LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)}.</p>
     * @see LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)
     */
    public static boolean requestChildRectHorizontally(RecyclerView parent, View child, Rect rect, boolean immediate) {
        // Gets the parent left and right.
        final int parentLeft  = parent.getPaddingLeft();
        final int parentRight = parent.getWidth() - parent.getPaddingRight();

        // Gets the child left and right relative to it's parent.
        final int childLeft  = child.getLeft() + rect.left - child.getScrollX();
        final int childRight = childLeft + rect.width();

        // Gets the offscreen left and right.
        final int offsetLeft = childLeft - parentLeft;
        final int offScreenLeft  = Math.min(0, offsetLeft);
        final int offScreenRight = Math.max(0, childRight - parentRight);

        final int dx = (offScreenLeft != 0 ? offScreenLeft : Math.min(offsetLeft, offScreenRight));
        if (dx > 0) {
            // scroll to right.
            return scrollBy(parent, offsetLeft, 0, immediate);
        } else if (dx < 0) {
            // scroll to left.
            return scrollBy(parent, -(parentRight - childRight), 0, immediate);
        } else {
            // no scroll.
            return false;
        }
    }

    /**
     * Requests that the given child of the RecyclerView be positioned onto the screen. <p>Note: This method recommended
     * call in the {@link LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)}.</p>
     * @see LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)
     */
    public static boolean requestChildRectVertically(RecyclerView parent, View child, Rect rect, boolean immediate) {
        // Gets the parent top and bottom.
        final int parentTop    = parent.getPaddingTop();
        final int parentBottom = parent.getHeight() - parent.getPaddingBottom();

        // Gets the child top and bottom relative to it's parent.
        final int childTop    = child.getTop() + rect.top - child.getScrollY();
        final int childBottom = childTop + rect.height();

        // Gets the offscreen top and bottom.
        final int offsetTop = childTop - parentTop;
        final int offScreenTop    = Math.min(0, offsetTop);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom);

        final int dy = (offScreenTop != 0 ? offScreenTop : Math.min(offsetTop, offScreenBottom));
        if (dy > 0) {
            // scroll to down.
            return scrollBy(parent, 0, offsetTop, immediate);
        } else if (dy < 0) {
            // scroll to up.
            return scrollBy(parent, 0, -(parentBottom - childBottom), immediate);
        } else {
            // no scroll.
            return false;
        }
    }

    /**
     * Called when searching for a focusable view in the given direction
     * has failed for the current content of the <tt>RecyclerView</tt>.
     */
    public static View onFocusSearchFailed(View focused, int focusDirection, Recycler recycler, State state) {
        // Returns the currently focused view when searching for a focusable view has failed.
        // This operation can be supported the RecyclerView has a fixed item count.
        return focused;
    }

    /**
     * Returns the index of the child to draw for this iteration.
     * @param container The <tt>ViewGroup</tt> whose child to draw.
     * @param childCount The number of child to draw.
     * @param i The current iteration.
     * @return The index of the child to draw this iteration.
     */
    public static int getChildDrawingOrder(ViewGroup container, int childCount, int i) {
        final View focused = container.getFocusedChild();
        if (focused != null) {
            if (container.getChildAt(i) == focused) {
                // Move the focused child order to last.
                return childCount - 1;
            } else if (i == childCount - 1) {
                // Move the last child order to the focused child order.
                return container.indexOfChild(focused);
            }
        }

        return i;
    }

    /**
     * Equivalent to calling <tt>recyclerView.setChildDrawingOrderCallback(new ChildDrawingOrder(recyclerView))</tt>.
     * @param recyclerView The {@link RecyclerView} to set.
     * @see RecyclerView#setChildDrawingOrderCallback(ChildDrawingOrderCallback)
     */
    public static void setChildDrawingOrderCallback(RecyclerView recyclerView) {
        recyclerView.setChildDrawingOrderCallback(new ChildDrawingOrder(recyclerView));
    }

    /**
     * Called when the focus state of a view has changed.
     * @param view The view whose state has changed.
     * @param resId The resource id of the property animation.
     * @param invalidateParent Whether the <em>view's</em> parent should be invalidated as well.
     */
    public static void onFocusChange(View view, int resId, boolean invalidateParent) {
        if (invalidateParent) {
            final View parent = (View)view.getParent();
            if (parent != null) {
                parent.invalidate();
            }
        }

        ViewAnimationUtils.animate(view, resId).start();
    }

    /**
     * Class <tt>MarginItemDecoration</tt> is an implementation of an {@link ItemDecoration}.
     */
    public static final class MarginItemDecoration extends ItemDecoration {
        /**
         * The left margin in pixels of the children.
         */
        public int leftMargin;

        /**
         * The top margin in pixels of the children.
         */
        public int topMargin;

        /**
         * The right margin in pixels of the children.
         */
        public int rightMargin;

        /**
         * The bottom margin in pixels of the children.
         */
        public int bottomMargin;

        /**
         * Constructor
         * @see #MarginItemDecoration(Resources, int)
         * @see #MarginItemDecoration(int, int, int, int)
         * @see #MarginItemDecoration(MarginItemDecoration)
         */
        public MarginItemDecoration() {
        }

        /**
         * Copy constructor
         * @param from The decoration to copy.
         * @see #MarginItemDecoration()
         * @see #MarginItemDecoration(Resources, int)
         * @see #MarginItemDecoration(int, int, int, int)
         */
        public MarginItemDecoration(MarginItemDecoration from) {
            this.leftMargin   = from.leftMargin;
            this.topMargin    = from.topMargin;
            this.rightMargin  = from.rightMargin;
            this.bottomMargin = from.bottomMargin;
        }

        /**
         * Constructor
         * @param res The <tt>Resources</tt>.
         * @param id The resource id of the margin dimension.
         * @see #MarginItemDecoration()
         * @see #MarginItemDecoration(int, int, int, int)
         * @see #MarginItemDecoration(MarginItemDecoration)
         */
        public MarginItemDecoration(Resources res, int id) {
            leftMargin = topMargin = rightMargin = bottomMargin = res.getDimensionPixelSize(id);
        }

        /**
         * Constructor
         * @param leftMargin The left margin in pixels of the children.
         * @param topMargin The top margin in pixels of the children.
         * @param rightMargin The right margin in pixels of the children.
         * @param bottomMargin The bottom margin in pixels of the children.
         * @see #MarginItemDecoration()
         * @see #MarginItemDecoration(Resources, int)
         * @see #MarginItemDecoration(MarginItemDecoration)
         */
        public MarginItemDecoration(int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
            this.leftMargin   = leftMargin;
            this.topMargin    = topMargin;
            this.rightMargin  = rightMargin;
            this.bottomMargin = bottomMargin;
        }

        public final void dump(Printer printer) {
            printer.println(new StringBuilder(80)
                .append("MarginItemDecoration { leftMargin = ").append(leftMargin)
                .append(", topMargin = ").append(topMargin)
                .append(", rightMargin = ").append(rightMargin)
                .append(", bottomMargin = ").append(bottomMargin)
                .append(" }").toString());
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            outRect.set(leftMargin, topMargin, rightMargin, bottomMargin);
        }
    }

    /**
     * Class <tt>ChildDrawingOrder</tt> is an implementation of a {@link ChildDrawingOrderCallback}.
     */
    public static final class ChildDrawingOrder implements ChildDrawingOrderCallback {
        private final ViewGroup mContainer;

        /**
         * Constructor
         * @param container The <tt>ViewGroup</tt>.
         */
        public ChildDrawingOrder(ViewGroup container) {
            mContainer = container;
        }

        @Override
        public int onGetChildDrawingOrder(int childCount, int i) {
            return getChildDrawingOrder(mContainer, childCount, i);
        }
    }

    private static boolean scrollBy(RecyclerView view, int dx, int dy, boolean immediate) {
        if (immediate) {
            view.scrollBy(dx, dy);
        } else {
            view.smoothScrollBy(dx, dy);
        }

        return true;
    }

    /**
     * This utility class cannot be instantiated.
     */
    private LayoutManagerHelper() {
    }
}
