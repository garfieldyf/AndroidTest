package android.support.v7.widget;

import android.content.Context;
import android.ext.widget.LayoutFocusManager;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;

/**
 * Class FocusLinearLayoutManager
 * @author Garfield
 */
public class FocusLinearLayoutManager extends LinearLayoutManager {
    protected final LayoutFocusManager mFocusManager;

    /**
     * Constructor
     * <p>Creates a vertical <tt>FocusLinearLayoutManager</tt>.</p>
     * @param context The <tt>Context</tt>.
     * @see #FocusLinearLayoutManager(Context, int, boolean)
     * @see #FocusLinearLayoutManager(Context, AttributeSet, int, int)
     */
    public FocusLinearLayoutManager(Context context) {
        super(context);
        mFocusManager = new LayoutFocusManager(this);
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param orientation Layout orientation. Should be {@link #HORIZONTAL} or {@link #VERTICAL}.
     * @param reverseLayout When set to <tt>true</tt>, layouts from end to start.
     * @see #FocusLinearLayoutManager(Context)
     * @see #FocusLinearLayoutManager(Context, AttributeSet, int, int)
     */
    public FocusLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mFocusManager = new LayoutFocusManager(this);
    }

    /**
     * Constructor
     * <p>Creates a <tt>FocusLinearLayoutManager</tt> is set in XML by RecyclerView attribute "layoutManager".</p>
     * @see #FocusLinearLayoutManager(Context)
     * @see #FocusLinearLayoutManager(Context, int, boolean)
     */
    public FocusLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mFocusManager = new LayoutFocusManager(this);
    }

    /**
     * Returns the adapter position of the item view which has focus.
     * @return The adapter position of the item view.
     * @see #setFocusedItem(View)
     */
    public int getFocusedItem() {
        return mFocusManager.getFocusedItem();
    }

    /**
     * Sets the item view which has focus.
     * @param child The item view which has focus.
     * @return The adapter position of the <em>child</em>.
     * @see #getFocusedItem()
     */
    public int setFocusedItem(View child) {
        return mFocusManager.setFocusedItem(child);
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param position The position of the item in the data set of the adapter.
     */
    public void requestItemFocus(int position) {
        mFocusManager.requestItemFocus(position);
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
