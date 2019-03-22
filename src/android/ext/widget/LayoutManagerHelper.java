package android.ext.widget;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.view.View;

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
        final int offScreenLeft  = Math.min(0, childLeft  - parentLeft);
        final int offScreenRight = Math.max(0, childRight - parentRight);

        final int dx = (offScreenLeft != 0 ? offScreenLeft : Math.min(childLeft - parentLeft, offScreenRight));
        if (dx > 0) {
            // scroll to right.
            return scrollBy(parent, childLeft - parentLeft, 0, immediate);
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
        final int offScreenTop    = Math.min(0, childTop - parentTop);
        final int offScreenBottom = Math.max(0, childBottom - parentBottom);

        final int dy = (offScreenTop != 0 ? offScreenTop : Math.min(childTop - parentTop, offScreenBottom));
        if (dy > 0) {
            // scroll to down.
            return scrollBy(parent, 0, childTop - parentTop, immediate);
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
