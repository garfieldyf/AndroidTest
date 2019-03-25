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

    /**
     * Equivalent to calling <tt>onFocusChange(view, hasFocus, 1.14f, 120, invalidateParent)</tt>.
     * @param view The view whose state has changed.
     * @param hasFocus The new focus state of <em>view</em>.
     * @param invalidateParent Whether the <em>view's</em> parent should be invalidated as well.
     * @see #onFocusChange(View, boolean, float, long, boolean)
     */
    public static void onFocusChange(View view, boolean hasFocus, boolean invalidateParent) {
        onFocusChange(view, hasFocus, 1.14f, 120, invalidateParent);
    }

    /**
     * Called when the focus state of a view has changed.
     * @param view The view whose state has changed.
     * @param hasFocus The new focus state of <em>view</em>.
     * @param scale The scale value to be animated to.
     * @param duration The length of the property animations, in milliseconds.
     * @param invalidateParent Whether the <em>view's</em> parent should be invalidated as well.
     */
    public static void onFocusChange(View view, boolean hasFocus, float scale, long duration, boolean invalidateParent) {
        if (hasFocus) {
            if (invalidateParent) {
                final View parent = (View)view.getParent();
                if (parent != null) {
                    parent.invalidate(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
                }
            }

            view.animate().scaleX(scale).scaleY(scale).setDuration(duration).start();
        } else {
            view.animate().scaleX(1.0f).scaleY(1.0f).setDuration(duration).start();
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
