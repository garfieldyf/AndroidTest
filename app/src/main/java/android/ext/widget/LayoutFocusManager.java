package android.ext.widget;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import java.util.ArrayList;

/**
 * Class LayoutFocusManager
 * @author Garfield
 */
public final class LayoutFocusManager implements OnFocusChangeListener {
    private int mPosition;
    private final LinearLayoutManager mLayout;

    /**
     * Constructor
     * @param layout The {@link LinearLayoutManager}.
     */
    public LayoutFocusManager(LinearLayoutManager layout) {
        mLayout = layout;
    }

    /**
     * Returns the adapter position of the item view which has focus.
     * @return The adapter position of the item view.
     * @see #setFocusedChild(View)
     */
    public int getFocusedChild() {
        return mPosition;
    }

    /**
     * Sets the item view which has focus.
     * @param child The item view which has focus.
     * @return The adapter position of the <em>child</em>.
     * @see #getFocusedChild()
     */
    public int setFocusedChild(View child) {
        return (mPosition = (child != null ? mLayout.getPosition(child) : 0));
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param position The position of the item in the data set of the adapter.
     */
    public void requestChildFocus(int position) {
        mPosition = position;
        if (!requestChildFocus()) {
            mLayout.scrollToPosition(mPosition);
            LayoutManagerHelper.requestItemFocus(mLayout, mPosition);
        }
    }

    /**
     * Sets the <em>recyclerView</em> to the <tt>LinearLayoutManager</tt>.
     * <p>Note: This method recommended call in the {@link LinearLayoutManager#setRecyclerView}.</p>
     */
    public void setRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            recyclerView.setFocusable(true);
            recyclerView.setOnFocusChangeListener(this);
            recyclerView.setDescendantFocusability(RecyclerView.FOCUS_BEFORE_DESCENDANTS);
        }
    }

    /**
     * Called to populate focusable views within the <tt>RecyclerView</tt>.
     * <p>Note: This method recommended call in the {@link LinearLayoutManager#onAddFocusables}.</p>
     */
    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views) {
        return (recyclerView.getFocusedChild() == null && views.add(recyclerView));
    }

    /**
     * Requests that the given child of the <tt>RecyclerView</tt> be positioned onto the screen.
     * <p>Note: This method recommended call in the {@link LinearLayoutManager#requestChildRectangleOnScreen}.</p>
     * @param parent The {@link RecyclerView}.
     * @param child The child making the request.
     * @param immediate <tt>true</tt> to forbid animated or delayed scrolling, <tt>false</tt> otherwise.
     * @return Whether the group scrolled to handle the operation.
     */
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, boolean immediate) {
        if (mLayout.getOrientation() == LinearLayoutManager.VERTICAL) {
            final int dy = (child.getTop() + child.getHeight() / 2) - mLayout.getHeight() / 2;
            if (dy != 0) {
                scrollBy(parent, 0, dy, immediate);
                return true;
            }
        } else {
            final int dx = (child.getLeft() + child.getWidth() / 2) - mLayout.getWidth() / 2;
            if (dx != 0) {
                scrollBy(parent, dx, 0, immediate);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            requestChildFocus();
        }
    }

    private boolean requestChildFocus() {
        final View focused = mLayout.findViewByPosition(mPosition);
        return (focused != null && focused.requestFocus());
    }

    private static void scrollBy(RecyclerView view, int dx, int dy, boolean immediate) {
        if (immediate || view.getScrollState() != RecyclerView.SCROLL_STATE_IDLE) {
            view.scrollBy(dx, dy);
        } else {
            view.smoothScrollBy(dx, dy);
        }
    }
}
