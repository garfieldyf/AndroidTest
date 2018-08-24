package android.ext.database;

import java.lang.ref.WeakReference;
import android.database.Cursor;
import android.ext.util.DebugUtils;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

/**
 * Abstract class <tt>DatabaseHandler</tt>.
 * @author Garfield
 * @version 1.0
 */
public abstract class DatabaseHandler implements Callback {
    /* package */ static final int MESSAGE_CALL     = 1;
    /* package */ static final int MESSAGE_BATCH    = 2;
    /* package */ static final int MESSAGE_QUERY    = 3;
    /* package */ static final int MESSAGE_INSERT   = 4;
    /* package */ static final int MESSAGE_UPDATE   = 5;
    /* package */ static final int MESSAGE_DELETE   = 6;
    /* package */ static final int MESSAGE_REPLACE  = 7;
    /* package */ static final int MESSAGE_INSERTS  = 8;
    /* package */ static final int MESSAGE_EXECUTE  = 9;
    /* package */ static final int MESSAGE_RAWQUERY = 10;

    /* package */ final Handler mHandler;
    /* package */ WeakReference<Object> mOwner;

    /**
     * Constructor
     * @see #DatabaseHandler(Object)
     */
    /* package */ DatabaseHandler() {
        DebugUtils.__checkMemoryLeaks(getClass());
        mHandler = Factory.createHandler(this);
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #DatabaseHandler()
     */
    /* package */ DatabaseHandler(Object owner) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mHandler = Factory.createHandler(this);
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * Returns the object that owns this handler.
     * @return The owner object or <tt>null</tt> if
     * no owner set or the owner released by the GC.
     * @see #setOwner(Object)
     */
    @SuppressWarnings("unchecked")
    public final <T> T getOwner() {
        return (mOwner != null ? (T)mOwner.get() : null);
    }

    /**
     * Sets the object that owns this handler.
     * @param owner The owner object.
     * @see #getOwner()
     */
    public final void setOwner(Object owner) {
        mOwner = new WeakReference<Object>(owner);
    }

    /**
     * This method begins an asynchronous execute custom query. When the query is executing
     * {@link #onExecute} is called on a background thread. After the query is done
     * {@link #onExecuteComplete} is called.
     * @param token A token passed into {@link #onExecute} and {@link #onExecuteComplete}
     * to identify execute.
     * @param params The parameters passed into <tt>onExecute</tt>. If no parameters, you
     * can pass <em>(Object[])null</em> instead of allocating an empty array.
     */
    public final void startExecute(int token, Object... params) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_EXECUTE
         * msg.obj  - params
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_EXECUTE, 0, params));
    }

    /**
     * Attempts to cancel operation that has not already started.
     * @param token The token representing the operation to be cancelled.
     * If multiple operations have the same token they will all be cancelled.
     */
    public final void cancel(int token) {
        mHandler.removeMessages(token);
    }

    /**
     * Called on the UI thread when this handler handle messages.
     * <p><b>Note: Do not call this method directly.</b></p>
     * @param message The message.
     * @param token The token to identify the operation.
     * @param result The result.
     */
    public void dispatchMessage(int message, int token, Object result) {
        switch (message) {
        case MESSAGE_EXECUTE:
            onExecuteComplete(token, result);
            break;

        case MESSAGE_UPDATE:
            onUpdateComplete(token, (int)result);
            break;

        case MESSAGE_DELETE:
            onDeleteComplete(token, (int)result);
            break;

        case MESSAGE_QUERY:
        case MESSAGE_RAWQUERY:
            onQueryComplete(token, (Cursor)result);
            break;

        default:
            throw new IllegalStateException("Unknown message: " + message);
        }
    }

    /**
     * Called when an asynchronous execute is completed on the UI thread.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param result The result, returned earlier by {@link #onExecute}.
     */
    protected void onExecuteComplete(int token, Object result) {
    }

    /**
     * Called when an asynchronous query is completed on the UI thread.
     * @param token The token to identify the query, passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     */
    protected void onQueryComplete(int token, Cursor cursor) {
    }

    /**
     * Called when an asynchronous update is completed on the UI thread.
     * @param token The token to identify the update, passed in from {@link #startUpdate}.
     * @param rowsAffected The number of rows affected.
     */
    protected void onUpdateComplete(int token, int rowsAffected) {
    }

    /**
     * Called when an asynchronous delete is completed on the UI thread.
     * @param token The token to identify the delete, passed in from {@link #startDelete}.
     * @param rowsAffected The number of rows affected.
     */
    protected void onDeleteComplete(int token, int rowsAffected) {
    }

    /**
     * Class <tt>Factory</tt> used to create a new {@link Handler}.
     */
    private static final class Factory {
        private static final Looper sLooper;

        public static Handler createHandler(Callback callback) {
            return new Handler(sLooper, callback);
        }

        static {
            final HandlerThread thread = new HandlerThread("AsyncQuery-thread");
            thread.start();
            sLooper = thread.getLooper();
        }
    }
}
