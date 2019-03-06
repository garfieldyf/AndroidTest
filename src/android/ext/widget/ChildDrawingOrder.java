package android.ext.widget;

import android.support.v7.widget.RecyclerView.ChildDrawingOrderCallback;
import android.view.View;
import android.view.ViewGroup;

/**
 * Class ChildDrawingOrder
 * @author Garfield
 */
public final class ChildDrawingOrder implements ChildDrawingOrderCallback {
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
}
