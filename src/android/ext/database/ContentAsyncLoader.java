package android.ext.database;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.ext.content.AsyncTaskLoader;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

/**
 * Abstract class ContentAsyncLoader
 * @author Garfield
 * @version 1.0
 */
public abstract class ContentAsyncLoader extends AsyncTaskLoader<Integer, Object, Object> {
    private static final int MESSAGE_CALL    = 1;
    private static final int MESSAGE_BATCH   = 2;
    private static final int MESSAGE_QUERY   = 3;
    private static final int MESSAGE_INSERT  = 4;
    private static final int MESSAGE_UPDATE  = 5;
    private static final int MESSAGE_DELETE  = 6;
    private static final int MESSAGE_INSERTS = 7;
    private static final int MESSAGE_EXECUTE = 8;

    /**
     * The application <tt>Context</tt>.
     */
    protected final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The serial {@link Executor}.
     * @see #ContentAsyncLoader(Context, Executor, Object)
     */
    public ContentAsyncLoader(Context context, Executor executor) {
        super(executor);
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The serial {@link Executor}.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #ContentAsyncLoader(Context, Executor)
     */
    public ContentAsyncLoader(Context context, Executor executor, Object owner) {
        super(executor, owner);
        mContext = context.getApplicationContext();
    }

    /**
     * This method begins an asynchronous call a provider-defined method. When the call
     * is done {@link #onCallComplete} is called. <p><b>Note: This method must be invoked
     * on the UI thread.</b></p>
     * @param token A token passed into {@link #onCallComplete} to identify the call.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param method The provider-defined method name to call.
     * @param arg The provider-defined <tt>String</tt> argument. May be <tt>null</tt>.
     * @param extras The provider-defined <tt>Bundle</tt> argument. May be <tt>null</tt>.
     * @see #onCallComplete(int, Bundle)
     */
    public final void startCall(int token, Uri uri, String method, String arg, Bundle extras) {
        load(token, MESSAGE_CALL, uri, method, arg, extras);
    }

    /**
     * This method begins an asynchronous execute custom task. When the task is executing {@link #onExecute}
     * is called on a background thread. After the task is done {@link #onExecuteComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onExecute} and {@link #onExecuteComplete}
     * to identify execute.
     * @param params The parameters passed into {@link #onExecute}. If no parameters, you
     * can pass <em>(Object[])null</em> instead of allocating an empty array.
     * @see #onExecute(ContentResolver, int, Object[])
     * @see #onExecuteComplete(int, Object)
     */
    public final void startExecute(int token, Object... params) {
        load(token, MESSAGE_EXECUTE, params);
    }

    /**
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onQueryComplete} to identify the query.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param projection A list of which columns to return. Passing <tt>null</tt> will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
     * (excluding the WHERE itself). Passing <tt>null</tt> will return all rows for the given URI.
     * @param selectionArgs You may include ? in selection, which will be replaced by the values
     * from <em>selectionArgs</em>. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing <tt>null</tt> will use the default sort order, which may be unordered.
     * @see #onQueryComplete(int, Cursor)
     */
    public final void startQuery(int token, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        load(token, MESSAGE_QUERY, uri, projection, selection, selectionArgs, sortOrder);
    }

    /**
     * This method begins an asynchronous insert. When the insert is done {@link #onInsertComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The keys should be the
     * column names and the values the column values. Passing an empty ContentValues will create an empty row.
     * @see #onInsertComplete(int, Uri)
     */
    public final void startInsert(int token, Uri uri, ContentValues values) {
        load(token, MESSAGE_INSERT, uri, values);
    }

    /**
     * This method begins an asynchronous update. When the update is done {@link #onUpdateComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onUpdateComplete} to identify the update.
     * @param uri The URI to modify.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     * @param whereClause The WHERE clause to apply when updating. Passing <tt>null</tt> will update all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     * @see #onUpdateComplete(int, int)
     */
    public final void startUpdate(int token, Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        load(token, MESSAGE_UPDATE, uri, values, whereClause, whereArgs);
    }

    /**
     * This method begins an asynchronous delete. When the delete is done {@link #onDeleteComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onDeleteComplete} to identify the delete.
     * @param uri The URL of the row to delete.
     * @param whereClause The WHERE clause to apply when deleting. Passing <tt>null</tt> or <tt>"1"</tt> will
     * delete all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     * @see #onDeleteComplete(int, int)
     */
    public final void startDelete(int token, Uri uri, String whereClause, String[] whereArgs) {
        load(token, MESSAGE_DELETE, uri, whereClause, whereArgs);
    }

    /**
     * This method begins an asynchronous insert multiple rows into a given <tt>uri</tt>. When the insert is done
     * {@link #onBulkInsertComplete} is called. <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onBulkInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The keys should be the
     * column names and the values the column values.
     * @see #onBulkInsertComplete(int, int)
     */
    public final void startBulkInsert(int token, Uri uri, ContentValues[] values) {
        load(token, MESSAGE_INSERTS, uri, values);
    }

    /**
     * This method begins an asynchronous apply each of the <tt>ContentProviderOperation</tt> objects.
     * When the apply is done {@link #onApplyBatchComplete} is called. <p><b>Note: This method must be
     * invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onApplyBatchComplete} to identify the apply.
     * @param authority The authority of the <tt>ContentProvider</tt> to which this batch should be applied.
     * @param operations The operations to apply.
     * @see #onApplyBatchComplete(int, ContentProviderResult[])
     */
    public final void startApplyBatch(int token, String authority, ArrayList<ContentProviderOperation> operations) {
        load(token, MESSAGE_BATCH, authority, operations);
    }

    /**
     * Returns a {@link ContentResolver} instance for your application's package.
     * @return The <tt>ContentResolver</tt>.
     */
    public final ContentResolver getContentResolver() {
        return mContext.getContentResolver();
    }

    @Override
    protected Object loadInBackground(Task<?, ?> task, Integer token, Object[] params) {
        final ContentResolver resolver = mContext.getContentResolver();
        final Object result;
        switch ((Integer)params[0]) {
        case MESSAGE_CALL:
            result = resolver.call((Uri)params[1], (String)params[2], (String)params[3], (Bundle)params[4]);
            break;

        case MESSAGE_BATCH:
            result = applyBatch(resolver, params);
            break;

        case MESSAGE_QUERY:
            result = execQuery(resolver, params);
            break;

        case MESSAGE_INSERT:
            result = resolver.insert((Uri)params[1], (ContentValues)params[2]);
            break;

        case MESSAGE_UPDATE:
            result = resolver.update((Uri)params[1], (ContentValues)params[2], (String)params[3], (String[])params[4]);
            break;

        case MESSAGE_DELETE:
            result = resolver.delete((Uri)params[1], (String)params[2], (String[])params[3]);
            break;

        case MESSAGE_INSERTS:
            result = resolver.bulkInsert((Uri)params[1], (ContentValues[])params[2]);
            break;

        case MESSAGE_EXECUTE:
            result = onExecute(resolver, token, (Object[])params[1]);
            break;

        default:
            throw new IllegalStateException("Unknown message: " + params[0]);
        }

        return result;
    }

    @Override
    protected void onLoadComplete(Integer token, Object[] params, Object result) {
        switch ((Integer)params[0]) {
        case MESSAGE_CALL:
            onCallComplete(token, (Bundle)result);
            break;

        case MESSAGE_BATCH:
            onApplyBatchComplete(token, (ContentProviderResult[])result);
            break;

        case MESSAGE_QUERY:
            onQueryComplete(token, (Cursor)result);
            break;

        case MESSAGE_INSERT:
            onInsertComplete(token, (Uri)result);
            break;

        case MESSAGE_UPDATE:
            onUpdateComplete(token, (Integer)result);
            break;

        case MESSAGE_DELETE:
            onDeleteComplete(token, (Integer)result);
            break;

        case MESSAGE_INSERTS:
            onBulkInsertComplete(token, (Integer)result);
            break;

        case MESSAGE_EXECUTE:
            onExecuteComplete(token, result);
            break;
        }
    }

    @Override
    protected void onLoadCancelled(Integer token, Object[] params, Object result) {
        if (result instanceof Cursor) {
            ((Cursor)result).close();
        }
    }

    @Override
    protected boolean rejectedRequest(Integer token, Object[] params, Object[] prevParams) {
        if ((Integer)params[0] == MESSAGE_QUERY) {
            cancelTask(token, false);
        }

        return false;
    }

    /**
     * Executes custom query on a background thread.
     * @param resolver The {@link ContentResolver}.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param params The parameters passed in from {@link #startExecute}.
     * @return The execution result.
     * @see #startExecute(int, Object[])
     * @see #onExecuteComplete(int, Object)
     */
    protected Object onExecute(ContentResolver resolver, int token, Object[] params) {
        return null;
    }

    /**
     * Called when an asynchronous execute is completed on the UI thread.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param result The result, returned earlier by {@link #onExecute}.
     * @see #startExecute(int, Object[])
     * @see #onExecute(ContentResolver, int, Object[])
     */
    protected void onExecuteComplete(int token, Object result) {
    }

    /**
     * Called when an asynchronous call is completed on the UI thread.
     * @param token The token to identify the call, passed in from {@link #startCall}.
     * @param result A result <tt>Bundle</tt> holding the results from the call.
     * @see #startCall(int, Uri, String, String, Bundle)
     */
    protected void onCallComplete(int token, Bundle result) {
    }

    /**
     * Called when an asynchronous query is completed on the UI thread.
     * @param token The token to identify the query, passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     * @see #startQuery(int, Uri, String[], String, String[], String)
     */
    protected void onQueryComplete(int token, Cursor cursor) {
    }

    /**
     * Called when an asynchronous insert is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startInsert}.
     * @param newUri The URL of the newly created row.
     * @see #startInsert(int, Uri, ContentValues)
     */
    protected void onInsertComplete(int token, Uri newUri) {
    }

    /**
     * Called when an asynchronous update is completed on the UI thread.
     * @param token The token to identify the update, passed in from {@link #startUpdate}.
     * @param rowsAffected The number of rows affected.
     * @see #startUpdate(int, Uri, ContentValues, String, String[])
     */
    protected void onUpdateComplete(int token, int rowsAffected) {
    }

    /**
     * Called when an asynchronous delete is completed on the UI thread.
     * @param token The token to identify the delete, passed in from {@link #startDelete}.
     * @param rowsAffected The number of rows affected.
     * @see #startDelete(int, Uri, String, String[])
     */
    protected void onDeleteComplete(int token, int rowsAffected) {
    }

    /**
     * Called when an asynchronous multiple inserts is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startBulkInsert}.
     * @param newRows The number of newly created rows.
     * @see #startBulkInsert(int, Uri, ContentValues[])
     */
    protected void onBulkInsertComplete(int token, int newRows) {
    }

    /**
     * Called when an asynchronous apply is completed on the UI thread.
     * @param token The token to identify the apply, passed in from {@link #startApplyBatch}.
     * @param results The results of the applications.
     * @see #startApplyBatch(int, String, ArrayList)
     */
    protected void onApplyBatchComplete(int token, ContentProviderResult[] results) {
    }

    private Cursor execQuery(ContentResolver resolver, Object[] params) {
        Cursor cursor = null;
        try {
            cursor = resolver.query((Uri)params[1], (String[])params[2], (String)params[3], (String[])params[4], (String)params[5]);
            if (cursor != null) {
                // Calling getCount() causes the cursor window to be filled, which
                // will make the first access on the main thread a lot faster.
                cursor.getCount();
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't query - ").append(params[2]).toString(), e);
        }

        return cursor;
    }

    @SuppressWarnings("unchecked")
    private static ContentProviderResult[] applyBatch(ContentResolver resolver, Object[] params) {
        try {
            return resolver.applyBatch((String)params[1], (ArrayList<ContentProviderOperation>)params[2]);
        } catch (Exception e) {
            throw new RuntimeException(new StringBuilder("Couldn't apply batch, authority - ").append(params[1]).toString(), e);
        }
    }
}
