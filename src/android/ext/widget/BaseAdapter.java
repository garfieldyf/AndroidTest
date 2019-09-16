package android.ext.widget;

import android.ext.util.DebugUtils;
import android.ext.util.UIHandler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

/**
 * Class BaseAdapter implementation for an {@link Adapter} that can be used in {@link RecyclerView}.
 * @author Garfield
 */
public abstract class BaseAdapter<VH extends ViewHolder> extends Adapter<VH> {
    protected RecyclerView mRecyclerView;

    /**
     * Returns the {@link RecyclerView} associated with this adapter.
     * @return The {@link RecyclerView} object or <tt>null</tt> if
     * this adapter not attached to the <tt>RecyclerView</tt>.
     */
    public final RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Return the adapter position that the given <em>child</em> view corresponds to.
     * @param child The child <tt>View</tt> to query.
     * @return The adapter position corresponding to the given view or {@link #NO_POSITION}.
     */
    public final int getItemPosition(View child) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        return mRecyclerView.getChildAdapterPosition(child);
    }

    /**
     * Like as {@link #notifyDataSetChanged()}. If the recycler view is computing
     * a layout then this method will be post the change using a <tt>Handler</tt>.
     */
    public final void postNotifyDataSetChanged() {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        if (mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            UIHandler.sInstance.post(new NotificationRunnable(MESSAGE_DATA_CHANGED, 0, 0, null));
        } else {
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
     * Equivalent to calling <tt>postNotifyItemRangeChanged(position, 1, payload)</tt>.
     * @param position The position of the item that has changed
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
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
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        if (mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            UIHandler.sInstance.post(new NotificationRunnable(MESSAGE_ITEM_MOVED, fromPosition, toPosition, null));
        } else {
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
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        if (mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            UIHandler.sInstance.post(new NotificationRunnable(MESSAGE_ITEM_REMOVED, positionStart, itemCount, null));
        } else {
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
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        if (mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            UIHandler.sInstance.post(new NotificationRunnable(MESSAGE_ITEM_INSERTED, positionStart, itemCount, null));
        } else {
            notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    /**
     * Like as {@link #notifyItemRangeChanged(int, int, Object)}. If the recycler view is
     * computing a layout then this method will be post the change using a <tt>Handler</tt>.
     * @param positionStart The position of the first item that has changed.
     * @param itemCount The number of items that have changed.
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
     * @see #postNotifyItemChanged(int, Object)
     */
    public final void postNotifyItemRangeChanged(int positionStart, int itemCount, Object payload) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        if (mRecyclerView.isComputingLayout()) {
            DebugUtils.__checkDebug(true, "BaseAdapter", "The RecyclerView is computing layout, post the change using a Handler.");
            UIHandler.sInstance.post(new NotificationRunnable(MESSAGE_ITEM_CHANGED, positionStart, itemCount, payload));
        } else {
            notifyItemRangeChanged(positionStart, itemCount, payload);
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    /**
     * Class <tt>NotificationRunnable</tt> used to notify the {@link Adapter}'s events.
     */
    private final class NotificationRunnable implements Runnable {
        private final Object payload;
        private final int message;
        private final int itemCount;
        private final int positionStart;

        public NotificationRunnable(int message, int positionStart, int itemCount, Object payload) {
            this.message   = message;
            this.payload   = payload;
            this.itemCount = itemCount;
            this.positionStart = positionStart;
        }

        @Override
        public void run() {
            switch (message) {
            case MESSAGE_DATA_CHANGED:
                notifyDataSetChanged();
                break;

            case MESSAGE_ITEM_MOVED:
                notifyItemMoved(positionStart, itemCount);
                break;

            case MESSAGE_ITEM_REMOVED:
                notifyItemRangeRemoved(positionStart, itemCount);
                break;

            case MESSAGE_ITEM_INSERTED:
                notifyItemRangeInserted(positionStart, itemCount);
                break;

            case MESSAGE_ITEM_CHANGED:
                notifyItemRangeChanged(positionStart, itemCount, payload);
                break;

            default:
                throw new IllegalStateException("Unknown message: " + message);
            }
        }
    }

    private static final int MESSAGE_ITEM_MOVED    = 1;
    private static final int MESSAGE_DATA_CHANGED  = 2;
    private static final int MESSAGE_ITEM_REMOVED  = 3;
    private static final int MESSAGE_ITEM_CHANGED  = 4;
    private static final int MESSAGE_ITEM_INSERTED = 5;
}
