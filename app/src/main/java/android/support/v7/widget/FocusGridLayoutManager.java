package android.support.v7.widget;

import android.content.Context;
import android.ext.widget.LayoutFocusManager;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;

/**
 * Class FocusGridLayoutManager
 * @author Garfield
 */
public class FocusGridLayoutManager extends GridLayoutManager {
    protected final LayoutFocusManager mFocusManager;

    /**
     * Constructor
     * <p>Creates a vertical <tt>FocusGridLayoutManager</tt>.</p>
     * @param context The <tt>Context</tt>.
     * @param spanCount The number of columns in the grid.
     * @see #FocusGridLayoutManager(Context, int, int, boolean)
     * @see #FocusGridLayoutManager(Context, AttributeSet, int, int)
     */
    public FocusGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
        mFocusManager = new LayoutFocusManager(this);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param spanCount The number of columns in the grid.
     * @param orientation Layout orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     * @param reverseLayout When set to <tt>true</tt>, layouts from end to start.
     * @see #FocusGridLayoutManager(Context, int)
     * @see #FocusGridLayoutManager(Context, AttributeSet, int, int)
     */
    public FocusGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
        mFocusManager = new LayoutFocusManager(this);
    }

    /**
     * Constructor
     * <p>Creates a <tt>FocusGridLayoutManager</tt> is set in XML by RecyclerView attribute "layoutManager".</p>
     * @see #FocusGridLayoutManager(Context, int)
     * @see #FocusGridLayoutManager(Context, int, int, boolean)
     */
    public FocusGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mFocusManager = new LayoutFocusManager(this);
    }

    /**
     * Returns the adapter position of the item view which has focus.
     * @return The adapter position of the item view.
     * @see #setFocusedItem(View)
     */
    public int getFocusedItem() {
        return mFocusManager.getFocusedChild();
    }

    /**
     * Sets the item view which has focus.
     * @param child The item view which has focus.
     * @return The adapter position of the <em>child</em>.
     * @see #getFocusedItem()
     */
    public int setFocusedItem(View child) {
        return mFocusManager.setFocusedChild(child);
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param position The position of the item in the data set of the adapter.
     */
    public void requestItemFocus(int position) {
        mFocusManager.requestChildFocus(position);
    }

    @Override
    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        return (recyclerView.getFocusedChild() == null && views.add(recyclerView));
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {
        return mFocusManager.requestChildRectangleOnScreen(parent, child, immediate);
    }

    @Override
    /* package */ void setRecyclerView(RecyclerView recyclerView) {
        super.setRecyclerView(recyclerView);
        mFocusManager.setRecyclerView(recyclerView);
    }
}
