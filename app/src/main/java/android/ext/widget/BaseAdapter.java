package android.ext.widget;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import android.ext.util.DebugUtils;
import android.ext.widget.UIHandler.MessageRunnable;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Class BaseAdapter implementation for an {@link Adapter} that can be used in {@link RecyclerView}.
 * @author Garfield
 */
public abstract class BaseAdapter<VH extends ViewHolder> extends Adapter<VH> implements MessageRunnable {
    private static final int MESSAGE_ITEM_MOVED    = 1;
    private static final int MESSAGE_DATA_CHANGED  = 2;
    private static final int MESSAGE_ITEM_REMOVED  = 3;
    private static final int MESSAGE_ITEM_CHANGED  = 4;
    private static final int MESSAGE_ITEM_INSERTED = 5;

    /**
     * The owner <tt>RecyclerView</tt>.
     */
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
     * Like as {@link #notifyDataSetChanged()}. If the recycler view is computing
     * a layout then this method will be post the change using a <tt>Handler</tt>.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     */
    public final void postNotifyDataSetChanged() {
        DebugUtils.__checkUIThread("postNotifyDataSetChanged");
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            sendMessage(MESSAGE_DATA_CHANGED, 0, 0, null);
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
     * Equivalent to calling <tt>postNotifyItemChanged(id, null)</tt>.
     * @param id The id of the item.
     * @see #postNotifyItemChanged(long, Object)
     */
    public final void postNotifyItemChanged(long id) {
        postNotifyItemChanged(id, null);
    }

    /**
     * Notify any registered observers that the item's id equals the specified <em>id</em> has
     * changed. If {@link #hasStableIds()} would return <tt>false</tt> then invoking this method
     * has no effect.<p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param id The id of the item.
     * @param payload Optional parameter, pass to {@link #notifyItemChanged(int, Object)}.
     * @see #postNotifyItemChanged(long)
     */
    public final void postNotifyItemChanged(long id, Object payload) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        final ViewHolder holder = mRecyclerView.findViewHolderForItemId(id);
        if (holder != null) {
            final int position = holder.getAdapterPosition();
            DebugUtils.__checkWarning(position == NO_POSITION, "BaseAdapter", "The item has been removed from this adapter - id = " + id);
            if (position != NO_POSITION) {
                postNotifyItemRangeChanged(position, 1, payload);
            }
        }
    }

    /**
     * Like as {@link #notifyItemMoved(int, int)}. If the recycler view is computing
     * a layout then this method will be post the change using a <tt>Handler</tt>.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param fromPosition The previous position of the item.
     * @param toPosition The new position of the item.
     */
    public final void postNotifyItemMoved(int fromPosition, int toPosition) {
        DebugUtils.__checkUIThread("postNotifyItemMoved");
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            sendMessage(MESSAGE_ITEM_MOVED, fromPosition, toPosition, null);
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    /**
     * Like as {@link #notifyItemRangeRemoved(int, int)}. If the recycler view is computing
     * a layout then this method will be post the change using a <tt>Handler</tt>.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param positionStart The position of the first item that was removed.
     * @param itemCount The number of items removed.
     * @see #postNotifyItemRemoved(int)
     */
    public final void postNotifyItemRangeRemoved(int positionStart, int itemCount) {
        DebugUtils.__checkUIThread("postNotifyItemRangeRemoved");
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            sendMessage(MESSAGE_ITEM_REMOVED, positionStart, itemCount, null);
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    /**
     * Like as {@link #notifyItemRangeInserted(int, int)}. If the recycler view is
     * computing a layout then this method will be post the change using a <tt>Handler</tt>.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param positionStart The position of the first item that was inserted.
     * @param itemCount The number of items inserted.
     * @see #postNotifyItemInserted(int)
     */
    public final void postNotifyItemRangeInserted(int positionStart, int itemCount) {
        DebugUtils.__checkUIThread("postNotifyItemRangeInserted");
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            sendMessage(MESSAGE_ITEM_INSERTED, positionStart, itemCount, null);
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    /**
     * Like as {@link #notifyItemRangeChanged(int, int, Object)}. If the recycler view is
     * computing a layout then this method will be post the change using a <tt>Handler</tt>.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param positionStart The position of the first item that has changed.
     * @param itemCount The number of items that have changed.
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
     * @see #postNotifyItemChanged(int)
     * @see #postNotifyItemChanged(int, Object)
     */
    public final void postNotifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
        DebugUtils.__checkUIThread("postNotifyItemRangeChanged");
        if (mRecyclerView != null && mRecyclerView.isComputingLayout()) {
            sendMessage(MESSAGE_ITEM_CHANGED, positionStart, itemCount, payload);
        } else {
            DebugUtils.__checkWarning(mRecyclerView == null, "BaseAdapter", "This adapter not attached to RecyclerView.");
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    @Override
    public final void handleMessage(Message msg) {
        switch (msg.what) {
        case MESSAGE_DATA_CHANGED:
            notifyDataSetChanged();
            break;

        case MESSAGE_ITEM_MOVED:
            notifyItemMoved(msg.arg1, msg.arg2);
            break;

        case MESSAGE_ITEM_REMOVED:
            notifyItemRangeRemoved(msg.arg1, msg.arg2);
            break;

        case MESSAGE_ITEM_INSERTED:
            notifyItemRangeInserted(msg.arg1, msg.arg2);
            break;

        case MESSAGE_ITEM_CHANGED:
            notifyItemRangeChanged(msg.arg1, msg.arg2, msg.obj);
            break;

        default:
            throw new IllegalStateException("Unknown message: " + msg.what);
        }
    }

    @Override
    public final void run() {
        throw new AssertionError("No Implementation, This method is a stub!");
    }

    private void sendMessage(int what, int arg1, int arg2, Object obj) {
        DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
        final Message msg = UIHandler.sInstance.obtianMessage(this, obj);
        msg.what = what;
        msg.arg1 = arg1;
        msg.arg2 = arg2;
        UIHandler.sInstance.sendMessage(msg);
    }
}
