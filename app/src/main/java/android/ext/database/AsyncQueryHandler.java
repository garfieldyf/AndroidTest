package android.ext.database;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.ext.util.DebugUtils;
import android.ext.widget.UIHandler;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import java.util.ArrayList;
import java.util.concurrent.Executor;

/**
 * Class <tt>AsyncQueryHandler</tt> is a helper class to help make
 * handling asynchronous {@link ContentResolver} queries easier.
 * @author Garfield
 */
public abstract class AsyncQueryHandler extends DatabaseHandler {
    /**
     * The application <tt>Context</tt>.
     */
    public final Context mContext;

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param executor The serial <tt>Executor</tt>.
     * See {@link ThreadPool#createSerialExecutor()}.
     * @see #AsyncQueryHandler(Activity, Executor)
     */
    public AsyncQueryHandler(Context context, Executor executor) {
        super(executor);
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param activity The owner <tt>Activity</tt>.
     * @param executor The serial <tt>Executor</tt>.
     * See {@link ThreadPool#createSerialExecutor()}.
     * @see #AsyncQueryHandler(Context, Executor)
     */
    public AsyncQueryHandler(Activity ownerActivity, Executor executor) {
        super(executor, ownerActivity);
        mContext = ownerActivity.getApplicationContext();
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
        mExecutor.execute(obtainTask(token, MESSAGE_EXECUTE, null, null, null, params));
    }

    /**
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * @param token A token passed into {@link #onQueryComplete} to identify the query.
     * @param uri The URI to query, using the content:// scheme, for the content to retrieve.
     * @param projection A list of which columns to return. Passing <tt>null</tt> will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
     * (excluding the WHERE itself). Passing <tt>null</tt> will return all rows for the given URI.
     * @param selectionArgs You may include ? in selection, which will be replaced by the values
     * from <em>selectionArgs</em>. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing <tt>null</tt> will use the default sort order, which may be unordered.
     */
    public final void startQuery(int token, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final AsyncQueryTask task = obtainTask(token, MESSAGE_QUERY, uri, selection, selectionArgs, projection);
        task.sortOrder = sortOrder;
        mExecutor.execute(task);
    }

    /**
     * This method begins an asynchronous call a provider-defined method. When the call
     * is done {@link #onCallComplete} is called.
     * @param token A token passed into {@link #onCallComplete} to identify the call.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param method The provider-defined method name to call.
     * @param arg The provider-defined <tt>String</tt> argument. May be <tt>null</tt>.
     * @param extras The provider-defined <tt>Bundle</tt> argument. May be <tt>null</tt>.
     */
    public final void startCall(int token, Uri uri, String method, String arg, Bundle extras) {
        final AsyncQueryTask task = obtainTask(token, MESSAGE_CALL, uri, method, null, extras);
        task.sortOrder = arg;
        mExecutor.execute(task);
    }

    /**
     * This method begins an asynchronous insert. When the insert is done {@link #onInsertComplete} is called.
     * @param token A token passed into {@link #onInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The keys should be the
     * column names and the values the column values. Passing an empty ContentValues will create an empty row.
     */
    public final void startInsert(int token, Uri uri, ContentValues values) {
        mExecutor.execute(obtainTask(token, MESSAGE_INSERT, uri, null, null, values));
    }

    /**
     * This method begins an asynchronous update. When the update is done {@link #onUpdateComplete} is called.
     * @param token A token passed into {@link #onUpdateComplete} to identify the update.
     * @param uri The URI to update in.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     * @param whereClause The WHERE clause to apply when updating. Passing <tt>null</tt> will update all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startUpdate(int token, Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        mExecutor.execute(obtainTask(token, MESSAGE_UPDATE, uri, whereClause, whereArgs, values));
    }

    /**
     * This method begins an asynchronous delete. When the delete is done {@link #onDeleteComplete} is called.
     * @param token A token passed into {@link #onDeleteComplete} to identify the delete.
     * @param uri The URI of the row to delete.
     * @param whereClause The WHERE clause to apply when deleting. Passing <tt>null</tt> or <tt>"1"</tt> will
     * delete all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startDelete(int token, Uri uri, String whereClause, String[] whereArgs) {
        mExecutor.execute(obtainTask(token, MESSAGE_DELETE, uri, whereClause, whereArgs, null));
    }

    /**
     * This method begins an asynchronous insert multiple rows into a given <tt>uri</tt>.
     * When the insert is done {@link #onBulkInsertComplete} is called.
     * @param token A token passed into {@link #onBulkInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The
     * keys should be the column names and the values the column values.
     */
    public final void startBulkInsert(int token, Uri uri, ContentValues[] values) {
        mExecutor.execute(obtainTask(token, MESSAGE_INSERTS, uri, null, null, values));
    }

    /**
     * This method begins an asynchronous apply each of the <tt>ContentProviderOperation</tt> objects.
     * When the apply is done {@link #onApplyBatchComplete} is called.
     * @param token A token passed into {@link #onApplyBatchComplete} to identify the apply.
     * @param authority The authority of the <tt>ContentProvider</tt> to which this batch should be applied.
     * @param operations The operations to apply.
     */
    public final void startApplyBatch(int token, String authority, ArrayList<ContentProviderOperation> operations) {
        mExecutor.execute(obtainTask(token, MESSAGE_BATCH, null, authority, null, operations));
    }

    /**
     * Returns the {@link ContentResolver} associated with this object.
     * @return The <tt>ContentResolver</tt>.
     */
    public final ContentResolver getContentResolver() {
        return mContext.getContentResolver();
    }

    @Override
    public final Object newInstance() {
        return new AsyncQueryTask();
    }

    @Override
    public final void dispatchMessage(int message, int token, Object result) {
        if (!validateOwner()) {
            return;
        }

        switch (message) {
        case MESSAGE_CALL:
            onCallComplete(token, (Bundle)result);
            break;

        case MESSAGE_INSERT:
            onInsertComplete(token, (Uri)result);
            break;

        case MESSAGE_INSERTS:
            onBulkInsertComplete(token, (int)result);
            break;

        case MESSAGE_BATCH:
            onApplyBatchComplete(token, (ContentProviderResult[])result);
            break;

        default:
            super.dispatchMessage(message, token, result);
        }
    }

    /**
     * Executes custom query on a background thread.
     * @param resolver The {@link ContentResolver}.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param params The parameters passed in from {@link #startExecute}.
     * @return The execution result.
     */
    protected Object onExecute(ContentResolver resolver, int token, Object[] params) {
        return null;
    }

    /**
     * Called when an asynchronous call is completed on the UI thread.
     * @param token The token to identify the call, passed in from {@link #startCall}.
     * @param result A result <tt>Bundle</tt> holding the results from the call.
     */
    protected void onCallComplete(int token, Bundle result) {
    }

    /**
     * Called when an asynchronous insert is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startInsert}.
     * @param newUri The URL of the newly created row.
     */
    protected void onInsertComplete(int token, Uri newUri) {
    }

    /**
     * Called when an asynchronous multiple inserts is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startBulkInsert}.
     * @param newRows The number of newly created rows.
     */
    protected void onBulkInsertComplete(int token, int newRows) {
    }

    /**
     * Called when an asynchronous apply is completed on the UI thread.
     * @param token The token to identify the apply, passed in from {@link #startApplyBatch}.
     * @param results The results of the applications.
     */
    protected void onApplyBatchComplete(int token, ContentProviderResult[] results) {
    }

    /**
     * Recycles the specified {@link AsyncQueryTask} to the task pool.
     */
    /* package */ final void recycleTask(AsyncQueryTask task) {
        task.uri = null;
        task.values = null;
        task.sortOrder = null;
        task.selection = null;
        task.selectionArgs = null;
        mTaskPool.recycle(task);
    }

    /**
     * Retrieves a new {@link AsyncQueryTask} from the task pool. Allows us to avoid allocating new tasks in many cases.
     */
    private AsyncQueryTask obtainTask(int token, int message, Uri uri, String selection, String[] selectionArgs, Object values) {
        final AsyncQueryTask task = (AsyncQueryTask)mTaskPool.obtain();
        task.uri = uri;
        task.token = token;
        task.values  = values;
        task.message = message;
        task.selection = selection;
        task.selectionArgs = selectionArgs;
        return task;
    }

    /**
     * Class <tt>AsyncQueryTask</tt> is an implementation of a {@link Runnable}.
     */
    /* package */ final class AsyncQueryTask implements Runnable {
        /* package */ Uri uri;
        /* package */ int token;
        /* package */ int message;
        /* package */ Object values;
        /* package */ String sortOrder;
        /* package */ String selection;
        /* package */ String[] selectionArgs;

        @Override
        public void run() {
            final ContentResolver resolver = mContext.getContentResolver();
            final Object result;
            switch (message) {
            case MESSAGE_CALL:
                result = resolver.call(uri, selection, sortOrder, (Bundle)values);
                break;

            case MESSAGE_BATCH:
                result = applyBatch(resolver);
                break;

            case MESSAGE_QUERY:
                result = execQuery(resolver);
                break;

            case MESSAGE_INSERT:
                result = resolver.insert(uri, (ContentValues)values);
                break;

            case MESSAGE_EXECUTE:
                result = onExecute(resolver, token, (Object[])values);
                break;

            case MESSAGE_DELETE:
                result = resolver.delete(uri, selection, selectionArgs);
                break;

            case MESSAGE_INSERTS:
                result = resolver.bulkInsert(uri, (ContentValues[])values);
                break;

            case MESSAGE_UPDATE:
                result = resolver.update(uri, (ContentValues)values, selection, selectionArgs);
                break;

            default:
                throw new IllegalStateException("Unknown message: " + message);
            }

            UIHandler.sInstance.sendMessage(AsyncQueryHandler.this, message, token, result);
            recycleTask(this);
        }

        private Cursor execQuery(ContentResolver resolver) {
            DebugUtils.__checkStartMethodTracing();
            Cursor cursor = null;
            try {
                cursor = resolver.query(uri, (String[])values, selection, selectionArgs, sortOrder);
                if (cursor != null) {
                    // Calling getCount() causes the cursor window to be filled, which
                    // will make the first access on the main thread a lot faster.
                    cursor.getCount();
                }
            } catch (Exception e) {
                Log.e(getClass().getName(), "Couldn't query from - " + uri, e);
            }

            DebugUtils.__checkStopMethodTracing("AsyncQueryHandler", "execQuery");
            return cursor;
        }

        @SuppressWarnings("unchecked")
        private ContentProviderResult[] applyBatch(ContentResolver resolver) {
            try {
                return resolver.applyBatch(selection, (ArrayList<ContentProviderOperation>)values);
            } catch (Exception e) {
                throw new RuntimeException("Couldn't apply batch, authority - " + selection, e);
            }
        }
    }
}
