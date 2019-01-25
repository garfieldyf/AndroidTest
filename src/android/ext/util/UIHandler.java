package android.ext.util;

import android.ext.content.Loader.Task;
import android.ext.database.DatabaseHandler;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.util.Pair;
import android.view.View;

/**
 * Class UIHandler
 * @author Garfield
 */
@SuppressWarnings("rawtypes")
public final class UIHandler extends Handler {
    /**
     * The {@link Handler} associated with the UI thread's message queue.
     */
    public static final UIHandler sInstance = new UIHandler();

    /**
     * Runs the specified <em>action</em> on the UI thread. If the
     * current thread is the UI thread, then the action is executed
     * immediately. If the current thread is not the UI thread, the
     * action is posted to the event queue of the UI thread.
     * @param action The action to run on the UI thread.
     */
    public static void runOnUIThread(Runnable action) {
        if (sInstance.getLooper() == Looper.myLooper()) {
            action.run();
        } else {
            sInstance.post(action);
        }
    }

    /**
     * Like as {@link Adapter#notifyDataSetChanged()}. But if the <em>recyclerView</em> is currently
     * computing a layout this method will be post the change using the <tt>UIHandler</tt>.
     * @param recyclerView The {@link RecyclerView}.
     */
    public static void notifyDataSetChanged(RecyclerView recyclerView) {
        final Adapter adapter = recyclerView.getAdapter();
        DebugUtils.__checkError(adapter == null, "The RecyclerView not set adapter");
        if (recyclerView.isComputingLayout()) {
            sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_DATA_CHANGED, adapter));
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * Equivalent to calling <tt>notifyItemRangeRemoved(recyclerView, position, 1, payload)</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param position The position of the item that was removed.
     * @see #notifyItemRangeRemoved(RecyclerView, int, int)
     */
    public static void notifyItemRemoved(RecyclerView recyclerView, int position) {
        notifyItemRangeRemoved(recyclerView, position, 1);
    }

    /**
     * Equivalent to calling <tt>notifyItemRangeInserted(recyclerView, position, 1, payload)</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param position The position of the item that was inserted.
     * @see #notifyItemRangeInserted(RecyclerView, int, int)
     */
    public static void notifyItemInserted(RecyclerView recyclerView, int position) {
        notifyItemRangeInserted(recyclerView, position, 1);
    }

    /**
     * Equivalent to calling <tt>notifyItemRangeChanged(recyclerView, position, 1, payload)</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param position The position of the item that has changed
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
     * @see #notifyItemRangeChanged(RecyclerView, int, int, Object)
     */
    public static void notifyItemChanged(RecyclerView recyclerView, int position, Object payload) {
        notifyItemRangeChanged(recyclerView, position, 1, payload);
    }

    /**
     * Like as {@link Adapter#notifyItemMoved(int, int)}. But if the <em>recyclerView</em> is
     * currently computing a layout this method will be post the change using the <tt>UIHandler</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param fromPosition The previous position of the item.
     * @param toPosition The new position of the item.
     */
    public static void notifyItemMoved(RecyclerView recyclerView, int fromPosition, int toPosition) {
        final Adapter adapter = recyclerView.getAdapter();
        DebugUtils.__checkError(adapter == null, "The RecyclerView not set adapter");
        if (recyclerView.isComputingLayout()) {
            sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_ITEM_MOVED, fromPosition, toPosition, adapter));
        } else {
            adapter.notifyItemMoved(fromPosition, toPosition);
        }
    }

    /**
     * Like as {@link Adapter#notifyItemRangeRemoved(int, int)}. But if the <em>recyclerView</em> is
     * currently computing a layout this method will be post the change using the <tt>UIHandler</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param positionStart The position of the first item that was removed.
     * @param itemCount The number of items removed.
     */
    public static void notifyItemRangeRemoved(RecyclerView recyclerView, int positionStart, int itemCount) {
        final Adapter adapter = recyclerView.getAdapter();
        DebugUtils.__checkError(adapter == null, "The RecyclerView not set adapter");
        if (recyclerView.isComputingLayout()) {
            sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_ITEM_REMOVED, positionStart, itemCount, adapter));
        } else {
            adapter.notifyItemRangeRemoved(positionStart, itemCount);
        }
    }

    /**
     * Like as {@link Adapter#notifyItemRangeInserted(int, int)}. But if the <em>recyclerView</em> is
     * currently computing a layout this method will be post the change using the <tt>UIHandler</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param positionStart The position of the first item that was inserted.
     * @param itemCount The number of items inserted.
     */
    public static void notifyItemRangeInserted(RecyclerView recyclerView, int positionStart, int itemCount) {
        final Adapter adapter = recyclerView.getAdapter();
        DebugUtils.__checkError(adapter == null, "The RecyclerView not set adapter");
        if (recyclerView.isComputingLayout()) {
            sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_ITEM_INSERTED, positionStart, itemCount, adapter));
        } else {
            adapter.notifyItemRangeInserted(positionStart, itemCount);
        }
    }

    /**
     * Notify any registered observers that all visible child views have changed. If the <em>recyclerView</em>
     * is currently computing a layout this method will be post the change using the <tt>UIHandler</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
     */
    public static void notifyVisibleItemRangeChanged(RecyclerView recyclerView, Object payload) {
        final LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof LinearLayoutManager) {
            final LinearLayoutManager layoutManager = (LinearLayoutManager)manager;
            final int firstPos = layoutManager.findFirstVisibleItemPosition();
            final int lastPos  = layoutManager.findLastVisibleItemPosition();
            if (firstPos != RecyclerView.NO_POSITION && lastPos != RecyclerView.NO_POSITION) {
                notifyItemRangeChanged(recyclerView, firstPos, lastPos - firstPos + 1, payload);
            }
        }
    }

    /**
     * Like as {@link Adapter#notifyItemRangeChanged(int, int, Object)}. But if the <em>recyclerView</em>
     * is currently computing a layout this method will be post the change using the <tt>UIHandler</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param positionStart The position of the first item that has changed.
     * @param itemCount The number of items that have changed.
     * @param payload Optional parameter, use <tt>null</tt> to identify a "full" update.
     */
    public static void notifyItemRangeChanged(RecyclerView recyclerView, int positionStart, int itemCount, Object payload) {
        final Adapter adapter = recyclerView.getAdapter();
        DebugUtils.__checkError(adapter == null, "The RecyclerView not set adapter");
        if (recyclerView.isComputingLayout()) {
            sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_ITEM_CHANGED, positionStart, itemCount, new Pair<Adapter, Object>(adapter, payload)));
        } else {
            adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }
    }

    /**
     * Called on the {@link Task} internal, do not call this method directly.
     */
    public final void finish(Task task) {
        final Message msg = Message.obtain(this, task);
        msg.what = MESSAGE_FINISHED;
        sendMessage(msg);
    }

    /**
     * Called on the {@link Task} internal, do not call this method directly.
     */
    public final void setProgress(Task task, Object[] values) {
        final Message msg = Message.obtain(this, task);
        msg.what = MESSAGE_PROGRESS;
        msg.obj  = values;
        sendMessage(msg);
    }

    /**
     * Called on the <tt>PageScroller</tt> internal, do not call this method directly.
     */
    public final void requestChildFocus(LayoutManager layoutManager, int position) {
        requestChildFocus(layoutManager, position, 2);
    }

    /**
     * Called on the {@link DatabaseHandler} internal, do not call this method directly.
     */
    public final void sendMessage(DatabaseHandler handler, int message, int token, Object result) {
        sendMessage(Message.obtain(this, MESSAGE_DISPATCH_MESSAGE, message, token, new Pair<DatabaseHandler, Object>(handler, result)));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void dispatchMessage(Message msg) {
        switch (msg.what) {
        // Dispatch the Loader.Task messages.
        case MESSAGE_PROGRESS:
        case MESSAGE_FINISHED:
            ((Task)msg.getCallback()).handleMessage(msg);
            break;

        // Dispatch the RecyclerView messages.
        case MESSAGE_CHILD_FOCUS:
            requestChildFocus((LayoutManager)msg.obj, msg.arg1, msg.arg2);
            break;

        case MESSAGE_DATA_CHANGED:
            ((Adapter)msg.obj).notifyDataSetChanged();
            break;

        case MESSAGE_ITEM_MOVED:
            ((Adapter)msg.obj).notifyItemMoved(msg.arg1, msg.arg2);
            break;

        case MESSAGE_ITEM_REMOVED:
            ((Adapter)msg.obj).notifyItemRangeRemoved(msg.arg1, msg.arg2);
            break;

        case MESSAGE_ITEM_INSERTED:
            ((Adapter)msg.obj).notifyItemRangeInserted(msg.arg1, msg.arg2);
            break;

        case MESSAGE_ITEM_CHANGED:
            final Pair<Adapter, Object> params = (Pair<Adapter, Object>)msg.obj;
            params.first.notifyItemRangeChanged(msg.arg1, msg.arg2, params.second);
            break;

        // Dispatch the DatabaseHandler messages.
        case MESSAGE_DISPATCH_MESSAGE:
            final Pair<DatabaseHandler, Object> param = (Pair<DatabaseHandler, Object>)msg.obj;
            param.first.dispatchMessage(msg.arg1, msg.arg2, param.second);
            break;

        default:
            super.dispatchMessage(msg);
        }
    }

    /**
     * Handle the recycler view's child view request focus.
     */
    private void requestChildFocus(LayoutManager layoutManager, int position, int retryCount) {
        final View child = layoutManager.findViewByPosition(position);
        if (child != null) {
            child.requestFocus();
        } else if (retryCount > 0) {
            sendMessage(Message.obtain(this, MESSAGE_CHILD_FOCUS, position, retryCount - 1, layoutManager));
        }
    }

    // The Loader.Task messages.
    private static final int MESSAGE_PROGRESS = 0xDEDEDEDE;
    public static final int MESSAGE_FINISHED  = 0xDFDFDFDF;

    // The RecyclerView messages.
    private static final int MESSAGE_CHILD_FOCUS   = 0xEAEAEAEA;
    private static final int MESSAGE_ITEM_MOVED    = 0xECECECEC;
    private static final int MESSAGE_DATA_CHANGED  = 0xEBEBEBEB;
    private static final int MESSAGE_ITEM_REMOVED  = 0xEDEDEDED;
    private static final int MESSAGE_ITEM_CHANGED  = 0xEFEFEFEF;
    private static final int MESSAGE_ITEM_INSERTED = 0xEEEEEEEE;

    // The DatabaseHandler messages.
    private static final int MESSAGE_DISPATCH_MESSAGE = 0xFEFEFEFE;

    /**
     * This class cannot be instantiated.
     */
    private UIHandler() {
        super(Looper.getMainLooper());
    }
}
