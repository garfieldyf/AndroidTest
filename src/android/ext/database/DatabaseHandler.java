package android.ext.database;

import java.lang.ref.WeakReference;
import android.content.ContentValues;
import android.database.Cursor;
import android.ext.concurrent.ThreadPool.MessageThread;
import android.ext.util.DebugUtils;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;

/**
 * Abstract class <tt>DatabaseHandler</tt>.
 * @author Garfield
 * @version 1.0
 */
public abstract class DatabaseHandler<URI> implements Callback {
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
        mHandler = createHandler();
    }

    /**
     * Constructor
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #DatabaseHandler()
     */
    /* package */ DatabaseHandler(Object owner) {
        DebugUtils.__checkMemoryLeaks(getClass());
        mHandler = createHandler();
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
        DebugUtils.__checkError(mOwner == null, "The " + getClass().getName() + " did not call setOwner()");
        return (T)mOwner.get();
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
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * @param token A token passed into {@link #onQueryComplete} to identify the query.
     * @param uri The URI or table to query.
     * @param projection A list of which columns to return. Passing <tt>null</tt> will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
     * (excluding the WHERE itself). Passing <tt>null</tt> will return all rows for the given URI.
     * @param selectionArgs You may include ? in selection, which will be replaced by the values
     * from <em>selectionArgs</em>. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing <tt>null</tt> will use the default sort order, which may be unordered.
     */
    public final void startQuery(int token, URI uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_QUERY
         * msg.obj  - { uri, projection, selection, selectionArgs, sortOrder }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_QUERY, 0, new Object[] { uri, projection, selection, selectionArgs, sortOrder }));
    }

    /**
     * This method begins an asynchronous update. When the update is done {@link #onUpdateComplete} is called.
     * @param token A token passed into {@link #onUpdateComplete} to identify the update.
     * @param uri The URI or table to update in.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     * @param whereClause The WHERE clause to apply when updating. Passing <tt>null</tt> will update all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startUpdate(int token, URI uri, ContentValues values, String whereClause, String[] whereArgs) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_UPDATE
         * msg.obj  - { uri, values, whereClause, whereArgs }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_UPDATE, 0, new Object[] { uri, values, whereClause, whereArgs }));
    }

    /**
     * This method begins an asynchronous delete. When the delete is done {@link #onDeleteComplete} is called.
     * @param token A token passed into {@link #onDeleteComplete} to identify the delete.
     * @param uri The URI or table of the row to delete.
     * @param whereClause The WHERE clause to apply when deleting. Passing <tt>null</tt> or <tt>"1"</tt> will
     * delete all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startDelete(int token, URI uri, String whereClause, String[] whereArgs) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_DELETE
         * msg.obj  - { uri, whereClause, whereArgs }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_DELETE, 0, new Object[] { uri, whereClause, whereArgs }));
    }

    /**
     * Returns the {@link Handler} associated with a background thread's message queue.
     * @return The <tt>Handler</tt>.
     */
    public final Handler getHandler() {
        return mHandler;
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
     * Creates a new {@link Handler} to execute asynchronous query.
     * @return A new <tt>Handler</tt>.
     */
    protected Handler createHandler() {
        return MessageThread.createHandler(this);
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
}
