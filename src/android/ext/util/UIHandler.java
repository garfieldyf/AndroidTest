package android.ext.util;

import java.util.concurrent.Executor;
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
@SuppressWarnings({ "rawtypes", "unchecked" })
public final class UIHandler extends Handler implements Executor {
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
        sInstance.execute(action);
    }

    /**
     * Called when an item in the data set of the adapter wants focus.
     * @param layoutManager The {@link LayoutManager}.
     * @param position The position of the item in the data set of the adapter.
     */
    public static void requestChildFocus(LayoutManager layoutManager, int position) {
        sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_CHILD_FOCUS, position, 2, layoutManager));
    }

    /**
     * Like as {@link Adapter#notifyDataSetChanged()}. If the <em>recyclerView</em> is currently
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
     * Equivalent to calling <tt>notifyItemRangeRemoved(recyclerView, position, 1)</tt>.
     * @param recyclerView The {@link RecyclerView}.
     * @param position The position of the item that was removed.
     * @see #notifyItemRangeRemoved(RecyclerView, int, int)
     */
    public static void notifyItemRemoved(RecyclerView recyclerView, int position) {
        notifyItemRangeRemoved(recyclerView, position, 1);
    }

    /**
     * Equivalent to calling <tt>notifyItemRangeInserted(recyclerView, position, 1)</tt>.
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
     * Like as {@link Adapter#notifyItemMoved(int, int)}. If the <em>recyclerView</em> is currently
     * computing a layout this method will be post the change using the <tt>UIHandler</tt>.
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
     * Like as {@link Adapter#notifyItemRangeRemoved(int, int)}. If the <em>recyclerView</em> is
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
     * Like as {@link Adapter#notifyItemRangeInserted(int, int)}. If the <em>recyclerView</em> is
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
            DebugUtils.__checkDebug(true, UIHandler.class.getSimpleName(), "firstVisiblePosition = " + firstPos + "lastVisiblePosition = " + lastPos);
            if (firstPos != RecyclerView.NO_POSITION && lastPos != RecyclerView.NO_POSITION) {
                notifyItemRangeChanged(recyclerView, firstPos, lastPos - firstPos + 1, payload);
            }
        }
    }

    /**
     * Like as {@link Adapter#notifyItemRangeChanged(int, int, Object)}. If the <em>recyclerView</em>
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
            sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_ITEM_CHANGED, positionStart, itemCount, new Pair(adapter, payload)));
        } else {
            adapter.notifyItemRangeChanged(positionStart, itemCount, payload);
        }
    }

    /**
     * Called on the {@link Task} internal, do not call this method directly.
     */
    public final void finish(Task task, Object result) {
        sendMessage(task, MESSAGE_FINISHED, result);
    }

    /**
     * Called on the {@link Task} internal, do not call this method directly.
     */
    public final void setProgress(Task task, Object... values) {
        sendMessage(task, MESSAGE_PROGRESS, values);
    }

    /**
     * Called on the {@link DatabaseHandler} internal, do not call this method directly.
     */
    public final void sendMessage(DatabaseHandler handler, int message, int token, Object result) {
        final Message msg = Message.obtain(this, handler);
        msg.what = MESSAGE_DATABASE_MESSAGE;
        msg.arg1 = message;
        msg.arg2 = token;
        msg.obj  = result;
        sendMessage(msg);
    }

    @Override
    public void execute(Runnable command) {
        if (getLooper() == Looper.myLooper()) {
            command.run();
        } else {
            post(command);
        }
    }

    @Override
    public void dispatchMessage(Message msg) {
        switch (msg.what) {
        // Dispatch the Task messages.
        case MESSAGE_PROGRESS:
            ((Task)msg.getCallback()).onProgress((Object[])msg.obj);
            break;

        case MESSAGE_FINISHED:
            ((Task)msg.getCallback()).onPostExecute(msg.obj);
            break;

        // Dispatch the RecyclerView messages.
        case MESSAGE_CHILD_FOCUS:
            requestChildFocus(msg);
            break;

        case MESSAGE_ITEM_CHANGED:
            dispatchItemChanged(msg);
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

        // Dispatch the DatabaseHandler messages.
        case MESSAGE_DATABASE_MESSAGE:
            /*
             * msg.arg1 = message;
             * msg.arg2 = token;
             * msg.obj  = result;
             */
            ((DatabaseHandler)msg.getCallback()).dispatchMessage(msg.arg1, msg.arg2, msg.obj);
            break;

        default:
            super.dispatchMessage(msg);
        }
    }

    private void sendMessage(Task task, int what, Object obj) {
        final Message msg = Message.obtain(this, task);
        msg.what = what;
        msg.obj  = obj;
        sendMessage(msg);
    }

    private static void dispatchItemChanged(Message msg) {
        final Pair params = (Pair)msg.obj;
        ((Adapter)params.first).notifyItemRangeChanged(msg.arg1, msg.arg2, params.second);
    }

    private static void requestChildFocus(Message msg) {
        /*
         * msg.arg1 = position;
         * msg.arg2 = retryCount;
         * msg.obj  = layoutManager;
         */
        final View child = ((LayoutManager)msg.obj).findViewByPosition(msg.arg1);
        if (child != null) {
            child.requestFocus();
        } else if (msg.arg2 > 0) {
            sInstance.sendMessage(Message.obtain(sInstance, MESSAGE_CHILD_FOCUS, msg.arg1, msg.arg2 - 1, msg.obj));
        }
    }

    // The Task messages
    private static final int MESSAGE_PROGRESS = 0xDEDEDEDE;
    private static final int MESSAGE_FINISHED = 0xDFDFDFDF;

    // The RecyclerView messages
    private static final int MESSAGE_CHILD_FOCUS   = 0xEAEAEAEA;
    private static final int MESSAGE_ITEM_MOVED    = 0xEBEBEBEB;
    private static final int MESSAGE_DATA_CHANGED  = 0xECECECEC;
    private static final int MESSAGE_ITEM_REMOVED  = 0xEDEDEDED;
    private static final int MESSAGE_ITEM_CHANGED  = 0xEEEEEEEE;
    private static final int MESSAGE_ITEM_INSERTED = 0xEFEFEFEF;

    // The DatabaseHandler messages
    private static final int MESSAGE_DATABASE_MESSAGE = 0xFEFEFEFE;

    /**
     * This class cannot be instantiated.
     */
    private UIHandler() {
        super(Looper.getMainLooper());
    }
}
