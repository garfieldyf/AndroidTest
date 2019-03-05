package android.ext.widget;

import android.support.v7.widget.RecyclerView.ChildDrawingOrderCallback;
import android.view.View;
import android.view.ViewGroup;

/**
 * Class ChildDrawingOrder
 * @author Garfield
 */
public final class ChildDrawingOrder implements ChildDrawingOrderCallback {
    private final ViewGroup mRootView;

    /**
     * Constructor
     * @param rootView The root <tt>View</tt>.
     */
    public ChildDrawingOrder(ViewGroup rootView) {
        mRootView = rootView;
    }

    @Override
    public int onGetChildDrawingOrder(int childCount, int i) {
        final View focused = mRootView.getFocusedChild();
        if (focused != null) {
            if (mRootView.getChildAt(i) == focused) {
                // Move the focused child order to last.
                return childCount - 1;
            } else if (i == childCount - 1) {
                // Move the last child order to the focused child order.
                return mRootView.indexOfChild(focused);
            }
        }

        return i;
    }
}
