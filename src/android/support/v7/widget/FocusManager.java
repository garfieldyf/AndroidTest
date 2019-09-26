package android.support.v7.widget;

import android.ext.widget.LayoutManagerHelper.ItemViewFinder;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.view.View;
import android.view.View.OnFocusChangeListener;

/**
 * Class FocusManager
 * @author Garfield
 */
public class FocusManager implements OnFocusChangeListener {
    /* package */ int mPosition;
    /* package */ final LayoutManager mLayout;
    /* package */ OnItemSelectedListener mListener;

    /**
     * Constructor
     * @param layout The {@link LayoutManager}.
     */
    public FocusManager(LayoutManager layout) {
        mLayout = layout;
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param position The position of the item in the data set of the adapter.
     * @param countPerPage The item count of per-page to display.
     */
    public void requestItemFocus(int position, int countPerPage) {
        if (position >= 0 && position < mLayout.getItemCount()) {
            setSelection(position);
            final View child = mLayout.findViewByPosition(position);
            if (child != null) {
                requestChildFocus(child);
            } else {
                mLayout.scrollToPosition((position / countPerPage) * countPerPage);
                mLayout.mRecyclerView.post(new FocusFinder(mLayout, position));
            }
        }
    }

    /**
     * Sets the focused item in the data set of the adapter.
     * @param position The position of the item in the data set of the adapter.
     */
    public void setFocusedItem(int position) {
        if (position >= 0 && position < mLayout.getItemCount()) {
            setSelection(position);
        }
    }

    /**
     * This method call the on {@link LayoutManager#setRecyclerView(RecyclerView)}.
     */
    public void onSetRecyclerView(RecyclerView recyclerView) {
        if (recyclerView != null) {
            recyclerView.setFocusable(true);
            recyclerView.setOnFocusChangeListener(this);
            recyclerView.setDescendantFocusability(RecyclerView.FOCUS_BEFORE_DESCENDANTS);
        }
    }

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if (hasFocus) {
            final View focused = mLayout.findViewByPosition(mPosition);
            if (focused != null) {
                focused.requestFocus();
            }
        }
    }

    private void setSelection(int position) {
        if (mPosition != position) {
            final int oldPosition = mPosition;
            mPosition = position;
            if (mListener != null) {
                mListener.onItemSelected(mLayout.mRecyclerView, position, oldPosition);
            }
        }
    }

    /* package */ final void requestChildFocus(View child) {
        if (mLayout.mRecyclerView.getFocusedChild() != null) {
            child.requestFocus();
        }
    }

    /**
     * Class <tt>FocusFinder</tt> is an implementation of an {@link ItemViewFinder}.
     */
    private final class FocusFinder extends ItemViewFinder {
        public FocusFinder(LayoutManager layout, int position) {
            super(layout, position);
        }

        @Override
        protected void onItemViewFound(LayoutManager layout, int position, View itemView) {
            requestChildFocus(itemView);
        }
    }

    /**
     * Callback interface to be invoked when an item in the data set of the adapter has been selected.
     */
    public static interface OnItemSelectedListener {
        /**
         * Callback method to be invoked when an item in the data set of the adapter has been selected.
         * @param parent The {@link RecyclerView} where the selection happened.
         * @param newPosition The newly position of the item in the data set of the adapter.
         * @param oldPosition The previously position of the item in the data set of the adapter.
         */
        void onItemSelected(RecyclerView parent, int newPosition, int oldPosition);
    }
}
