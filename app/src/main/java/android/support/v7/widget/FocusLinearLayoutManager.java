package android.support.v7.widget;

import android.content.Context;
import android.support.v7.widget.FocusManager.OnItemSelectedListener;
import android.util.AttributeSet;
import android.view.View;
import java.util.ArrayList;

/**
 * Class FocusLinearLayoutManager
 * @author Garfield
 */
public class FocusLinearLayoutManager extends LinearLayoutManager {
    private final FocusManager mFocusManager;

    /**
     * Constructor
     * @see #FocusLinearLayoutManager(Context, int, boolean)
     * @see #FocusLinearLayoutManager(Context, AttributeSet, int, int)
     */
    public FocusLinearLayoutManager(Context context) {
        this(context, VERTICAL, false);
    }

    /**
     * Constructor
     * @see #FocusLinearLayoutManager(Context)
     * @see #FocusLinearLayoutManager(Context, AttributeSet, int, int)
     */
    public FocusLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
        mFocusManager = new FocusManager(this);
    }

    /**
     * Constructor
     * @see #FocusLinearLayoutManager(Context)
     * @see #FocusLinearLayoutManager(Context, int, boolean)
     */
    public FocusLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mFocusManager = new FocusManager(this);
    }

    /**
     * Returns the focused item in the data set of the adapter.
     * @return The position of the item in the data set of the adapter.
     * @see #setFocusedItem(int)
     * @see #setFocusedView(View)
     */
    public int getFocusedItem() {
        return mFocusManager.mPosition;
    }

    /**
     * Sets the focused item in the data set of the adapter.
     * @param position The position of the item in the data set of the adapter.
     * @see #getFocusedItem()
     * @see #setFocusedView(View)
     */
    public void setFocusedItem(int position) {
        mFocusManager.setFocusedItem(position);
    }

    /**
     * Sets the focused view in the <tt>RecyclerView</tt>.
     * @param focused The focused <tt>View</tt>.
     * @see #getFocusedItem()
     * @see #setFocusedItem(int)
     */
    public final void setFocusedView(View focused) {
        mFocusManager.setFocusedItem(focused != null ? getPosition(focused) : 0);
    }

    public final void setOnItemSelectedListener(OnItemSelectedListener listener) {
        mFocusManager.mListener = listener;
    }

    @Override
    public boolean onAddFocusables(RecyclerView recyclerView, ArrayList<View> views, int direction, int focusableMode) {
        return (recyclerView.getFocusedChild() == null && views.add(recyclerView));
    }

    @Override
    /* package */ void setRecyclerView(RecyclerView recyclerView) {
        super.setRecyclerView(recyclerView);
        mFocusManager.onSetRecyclerView(recyclerView);
    }
}
