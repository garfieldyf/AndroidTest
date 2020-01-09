package android.ext.widget;

import android.ext.util.DebugUtils;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Class BaseAdapter implementation for an {@link Adapter} that can be used in {@link RecyclerView}.
 * @author Garfield
 */
public abstract class BaseAdapter<VH extends ViewHolder> extends Adapter<VH> {
    protected RecyclerView mRecyclerView;

    /**
     * Returns the {@link RecyclerView} associated with this adapter.
     * @return The <tt>RecyclerView</tt> or <tt>null</tt> if this
     * adapter not attached to the recycler view.
     */
    public final RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param position The position of the item in the data set of the adapter.
     */
    public final void requestItemFocus(int position) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        LayoutManagerHelper.requestItemFocus(mRecyclerView.getLayoutManager(), position);
    }

    /**
     * Equivalent to calling <tt>notifyItemChanged(id, null)</tt>.
     * @param id The id of the item.
     * @see #notifyItemChanged(long, Object)
     */
    public final void notifyItemChanged(long id) {
        notifyItemChanged(id, null);
    }

    /**
     * Notify any registered observers that the item's id equals the specified <em>id</em>
     * has changed. If {@link #hasStableIds()} would return <tt>false</tt> then invoking
     * this method has no effect.
     * @param id The id of the item.
     * @param payload Optional parameter, pass to {@link #notifyItemChanged(int, Object)}.
     * @see #notifyItemChanged(long)
     */
    public final void notifyItemChanged(long id, Object payload) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        final ViewHolder holder = mRecyclerView.findViewHolderForItemId(id);
        if (holder != null) {
            final int position = holder.getAdapterPosition();
            DebugUtils.__checkWarning(position == RecyclerView.NO_POSITION, "BaseAdapter", "The item has no position - id = " + id);
            if (position != RecyclerView.NO_POSITION) {
                postNotifyItemRangeChanged(position, 1, payload);
            }
        }
    }

    /**
     * Like as {@link #notifyDataSetChanged()}. If the recycler view is computing
     * a layout then this method will be post the change using a <tt>Handler</tt>.
     */
    public final void postNotifyDataSetChanged() {
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            mRecyclerView.post(this::notifyDataSetChanged);
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyDataSetChanged();
        }
    }

    /**
     * Equivalent to calling <tt>postNotifyItemRangeRemoved(position, 1)</tt>.
     * @param position The position of the item that was removed.
     * @see #postNotifyItemRangeRemoved(int, int)
     */
    public final void postNotifyItemRemoved(int position) {
        postNotifyItemRangeRemoved(position, 1);
    }

    /**
     * Equivalent to calling <tt>postNotifyItemRangeInserted(position, 1)</tt>.
     * @param position The position of the item that was inserted.
     * @see #postNotifyItemRangeInserted(int, int)
     */
    public final void postNotifyItemInserted(int position) {
        postNotifyItemRangeInserted(position, 1);
    }

    /**
     * Equivalent to calling <tt>postNotifyItemChanged(position, null)</tt>.
     * @param position The position of the item that has changed
     * @see #postNotifyItemChanged(int, Object)
     * @see #postNotifyItemRangeChanged(int, int, Object)
     */
    public final void postNotifyItemChanged(int position) {
        postNotifyItemRangeChanged(position, 1, null);
    }

    /**
     * Equivalent to calling <tt>postNotifyItemRangeChanged(position, 1, payload)</tt>.
     * @param position The position of the item that has changed
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
     * @see #postNotifyItemChanged(int)
     * @see #postNotifyItemRangeChanged(int, int, Object)
     */
    public final void postNotifyItemChanged(int position, Object payload) {
        postNotifyItemRangeChanged(position, 1, payload);
    }

    /**
     * Like as {@link #notifyItemMoved(int, int)}. If the recycler view is computing
     * a layout then this method will be post the change using a <tt>Handler</tt>.
     * @param fromPosition The previous position of the item.
     * @param toPosition The new position of the item.
     */
    public final void postNotifyItemMoved(int fromPosition, int toPosition) {
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            mRecyclerView.post(() -> notifyItemMoved(fromPosition, toPosition));
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    /**
     * Like as {@link #notifyItemRangeRemoved(int, int)}. If the recycler view is computing
     * a layout then this method will be post the change using a <tt>Handler</tt>.
     * @param positionStart The position of the first item that was removed.
     * @param itemCount The number of items removed.
     * @see #postNotifyItemRemoved(int)
     */
    public final void postNotifyItemRangeRemoved(int positionStart, int itemCount) {
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            mRecyclerView.post(() -> notifyItemRangeRemoved(positionStart, itemCount));
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    /**
     * Like as {@link #notifyItemRangeInserted(int, int)}. If the recycler view is
     * computing a layout then this method will be post the change using a <tt>Handler</tt>.
     * @param positionStart The position of the first item that was inserted.
     * @param itemCount The number of items inserted.
     * @see #postNotifyItemInserted(int)
     */
    public final void postNotifyItemRangeInserted(int positionStart, int itemCount) {
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            mRecyclerView.post(() -> notifyItemRangeInserted(positionStart, itemCount));
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    /**
     * Like as {@link #notifyItemRangeChanged(int, int, Object)}. If the recycler view is
     * computing a layout then this method will be post the change using a <tt>Handler</tt>.
     * @param positionStart The position of the first item that has changed.
     * @param itemCount The number of items that have changed.
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
     * @see #postNotifyItemChanged(int)
     * @see #postNotifyItemChanged(int, Object)
     */
    public final void postNotifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            mRecyclerView.post(() -> notifyItemRangeChanged(positionStart, itemCount, payload));
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }
}
