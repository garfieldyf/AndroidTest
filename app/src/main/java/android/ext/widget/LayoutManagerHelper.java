package android.ext.widget;

import android.content.res.Resources;
import android.ext.util.DebugUtils;
import android.ext.util.UIHandler;
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
     * Called when an item in the data set of the adapter wants focus.
     * @param layout The {@link LayoutManager}.
     * @param position The position of the item in the data set of the adapter.
     */
    public static void requestItemFocus(LayoutManager layout, int position) {
        DebugUtils.__checkError(layout == null, "layout == null");
        UIHandler.sInstance.post(new FocusFinder(layout, position));
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
     * Called when searching for a focusable view in the given direction
     * has failed for the current content of the <tt>RecyclerView</tt>.
     */
    public static View onFocusSearchFailed(View focused, int focusDirection, Recycler recycler, State state) {
        // Returns the currently focused view when searching for a focusable view has failed.
        // This operation can be supported the RecyclerView has a fixed item count.
        return focused;
    }

    /**
     * Scroll horizontally on the screen. <p>Note: This method recommended call in the
     * {@link LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)}.</p>
     * @param parent The {@link RecyclerView}.
     * @param child The child making the request.
     * @param rect The rectangle in the child's coordinates the child wishes to be on the screen.
     * @param offset The offset to apply to the horizontally in pixels.
     * @param immediate <tt>true</tt> to forbid animated or delayed scrolling, <tt>false</tt> otherwise.
     * @return Whether the group scrolled to handle the operation.
     * @see LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)
     */
    public static boolean scrollHorizontally(RecyclerView parent, View child, Rect rect, int offset, boolean immediate) {
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
            return scrollBy(parent, offsetLeft - offset, 0, immediate);
        } else if (dx < 0) {
            // scroll to left.
            return scrollBy(parent, offset - parentRight + childRight, 0, immediate);
        } else {
            // no scroll.
            return false;
        }
    }

    /**
     * Scroll vertically on the screen. <p>Note: This method recommended call in the
     * {@link LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)}.</p>
     * @param parent The {@link RecyclerView}.
     * @param child The child making the request.
     * @param rect The rectangle in the child's coordinates the child wishes to be on the screen.
     * @param offset The offset to apply to the vertically in pixels.
     * @param immediate <tt>true</tt> to forbid animated or delayed scrolling, <tt>false</tt> otherwise.
     * @return Whether the group scrolled to handle the operation.
     * @see LayoutManager#requestChildRectangleOnScreen(RecyclerView, View, Rect, boolean, boolean)
     */
    public static boolean scrollVertically(RecyclerView parent, View child, Rect rect, int offset, boolean immediate) {
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
            return scrollBy(parent, 0, offsetTop - offset, immediate);
        } else if (dy < 0) {
            // scroll to up.
            return scrollBy(parent, 0, offset - parentBottom + childBottom, immediate);
        } else {
            // no scroll.
            return false;
        }
    }

    /**
     * Class <tt>ItemViewFinder</tt> used to find the specified child view from the {@link RecyclerView}.
     */
    public static abstract class ItemViewFinder implements Runnable {
        private int mRetryCount;
        private final int mPosition;
        private final LayoutManager mLayout;

        /**
         * @param layout The {@link LayoutManager}.
         * @param position The adapter position of the item to find.
         */
        public ItemViewFinder(LayoutManager layout, int position) {
            mRetryCount = 3;
            mLayout = layout;
            mPosition = position;
        }

        @Override
        public void run() {
            final View itemView = mLayout.findViewByPosition(mPosition);
            DebugUtils.__checkDebug(itemView == null && mRetryCount <= 0, "LayoutManagerHelper", "The LayoutManager couldn't find view by position - " + mPosition);
            if (itemView != null) {
                onItemViewFound(mLayout, mPosition, itemView);
            } else if (--mRetryCount > 0) {
                UIHandler.sInstance.post(this);
            }
        }

        /**
         * Called when an item in the data set of the adapter has been found.
         * @param layout The {@link LayoutManager}.
         * @param position The adapter position of the item.
         * @param itemView The item {@link View} has been found.
         */
        protected abstract void onItemViewFound(LayoutManager layout, int position, View itemView);
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
         */
        public MarginItemDecoration() {
        }

        /**
         * Copy constructor
         * @param from The decoration to copy.
         */
        public MarginItemDecoration(MarginItemDecoration from) {
            this.leftMargin   = from.leftMargin;
            this.topMargin    = from.topMargin;
            this.rightMargin  = from.rightMargin;
            this.bottomMargin = from.bottomMargin;
        }

        /**
         * Constructor
         * @param margin The margin in pixels of the children.
         */
        public MarginItemDecoration(int margin) {
            this.leftMargin = this.topMargin = this.rightMargin = this.bottomMargin = margin;
        }

        /**
         * Constructor
         * @param res The <tt>Resources</tt>.
         * @param id The resource id of the margin dimension.
         */
        public MarginItemDecoration(Resources res, int id) {
            this.leftMargin = this.topMargin = this.rightMargin = this.bottomMargin = res.getDimensionPixelSize(id);
        }

        /**
         * Constructor
         * @param leftMargin The left margin in pixels of the children.
         * @param topMargin The top margin in pixels of the children.
         * @param rightMargin The right margin in pixels of the children.
         * @param bottomMargin The bottom margin in pixels of the children.
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
            return ViewUtils.getChildDrawingOrder(mContainer, childCount, i);
        }
    }

    /**
     * Class <tt>FocusFinder</tt> is an implementation of an {@link ItemViewFinder}.
     */
    private static final class FocusFinder extends ItemViewFinder {
        public FocusFinder(LayoutManager layout, int position) {
            super(layout, position);
        }

        @Override
        protected void onItemViewFound(LayoutManager layout, int position, View itemView) {
            itemView.requestFocus();
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
