package android.ext.database;

import static android.ext.content.AsyncTask.SERIAL_EXECUTOR;
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
     * @see #AsyncQueryHandler(Context, Object)
     */
    public AsyncQueryHandler(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * Constructor
     * @param context The <tt>Context</tt>.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncQueryHandler(Context)
     */
    public AsyncQueryHandler(Context context, Object owner) {
        mContext = context.getApplicationContext();
        setOwner(owner);
    }

    /**
     * This method begins an asynchronous execute custom query. When the query is executing
     * {@link #onExecute} is called on a background thread. After the query is done
     * {@link #onExecuteComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onExecute} and {@link #onExecuteComplete}
     * to identify execute.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param arg1 The <em>arg1</em> passed into {@link #onExecute}.
     * @param arg2 The <em>arg2</em> passed into {@link #onExecute}.
     * @param params The parameters passed into {@link #onExecute}. If no parameters, you
     * can pass <em>(Object[])null</em> instead of allocating an empty array.
     */
    public final void startExecute(int token, Uri uri, String arg1, String arg2, Object... params) {
        final AsyncQueryTask task = obtainTask(token, MESSAGE_EXECUTE, uri, arg1, null, params);
        task.sortOrder = arg2;
        SERIAL_EXECUTOR.execute(task);
    }

    /**
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
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
        SERIAL_EXECUTOR.execute(task);
    }

    /**
     * This method begins an asynchronous call a provider-defined method. When the call
     * is done {@link #onCallComplete} is called.<p><b>Note: This method must be invoked
     * on the UI thread.</b></p>
     * @param token A token passed into {@link #onCallComplete} to identify the call.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param method The provider-defined method name to call.
     * @param arg The provider-defined <tt>String</tt> argument. May be <tt>null</tt>.
     * @param extras The provider-defined <tt>Bundle</tt> argument. May be <tt>null</tt>.
     */
    public final void startCall(int token, Uri uri, String method, String arg, Bundle extras) {
        final AsyncQueryTask task = obtainTask(token, MESSAGE_CALL, uri, method, null, extras);
        task.sortOrder = arg;
        SERIAL_EXECUTOR.execute(task);
    }

    /**
     * This method begins an asynchronous insert. When the insert is done {@link #onInsertComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The keys should be the
     * column names and the values the column values. Passing an empty ContentValues will create an empty row.
     */
    public final void startInsert(int token, Uri uri, ContentValues values) {
        SERIAL_EXECUTOR.execute(obtainTask(token, MESSAGE_INSERT, uri, null, null, values));
    }

    /**
     * This method begins an asynchronous update. When the update is done {@link #onUpdateComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onUpdateComplete} to identify the update.
     * @param uri The URI to update in.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     * @param whereClause The WHERE clause to apply when updating. Passing <tt>null</tt> will update all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startUpdate(int token, Uri uri, ContentValues values, String whereClause, String[] whereArgs) {
        SERIAL_EXECUTOR.execute(obtainTask(token, MESSAGE_UPDATE, uri, whereClause, whereArgs, values));
    }

    /**
     * This method begins an asynchronous delete. When the delete is done {@link #onDeleteComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onDeleteComplete} to identify the delete.
     * @param uri The URI of the row to delete.
     * @param whereClause The WHERE clause to apply when deleting. Passing <tt>null</tt> or <tt>"1"</tt> will
     * delete all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startDelete(int token, Uri uri, String whereClause, String[] whereArgs) {
        SERIAL_EXECUTOR.execute(obtainTask(token, MESSAGE_DELETE, uri, whereClause, whereArgs, null));
    }

    /**
     * This method begins an asynchronous insert multiple rows into a given <tt>uri</tt>.
     * When the insert is done {@link #onBulkInsertComplete} is called.<p><b>Note: This
     * method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onBulkInsertComplete} to identify the insert.
     * @param uri The URI to insert into.
     * @param values The map contains the initial column values the newly inserted row. The
     * keys should be the column names and the values the column values.
     */
    public final void startBulkInsert(int token, Uri uri, ContentValues[] values) {
        SERIAL_EXECUTOR.execute(obtainTask(token, MESSAGE_INSERTS, uri, null, null, values));
    }

    /**
     * This method begins an asynchronous apply each of the <tt>ContentProviderOperation</tt> objects.
     * When the apply is done {@link #onApplyBatchComplete} is called.<p><b>Note: This method must be
     * invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onApplyBatchComplete} to identify the apply.
     * @param authority The authority of the <tt>ContentProvider</tt> to which this batch should be applied.
     * @param operations The operations to apply.
     */
    public final void startApplyBatch(int token, String authority, ArrayList<ContentProviderOperation> operations) {
        SERIAL_EXECUTOR.execute(obtainTask(token, MESSAGE_BATCH, null, authority, null, operations));
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

    /**
     * Executes custom query on a background thread.
     * @param resolver The {@link ContentResolver}.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param uri The <em>uri</em>, passed in from {@link #startExecute}.
     * @param arg1 The <em>arg1</em>, passed in from {@link #startExecute}.
     * @param arg2 The <em>arg2</em>, passed in from {@link #startExecute}.
     * @param params The parameters passed in from {@link #startExecute}.
     * @return The execution result.
     */
    protected Object onExecute(ContentResolver resolver, int token, Uri uri, String arg1, String arg2, Object[] params) {
        return null;
    }

    /**
     * Retrieves a new {@link AsyncQueryTask} from the task pool. Allows us to avoid allocating new tasks in many cases.
     */
    private AsyncQueryTask obtainTask(int token, int message, Uri uri, String selection, String[] selectionArgs, Object values) {
        DebugUtils.__checkUIThread("obtainTask");
        final AsyncQueryTask task = (AsyncQueryTask)mTaskPool.obtain();
        task.uri = uri;
        task.token = token;
        task.handler = this;
        task.values  = values;
        task.message = message;
        task.selection = selection;
        task.selectionArgs = selectionArgs;
        return task;
    }

    /**
     * Class <tt>AsyncQueryTask</tt> is an implementation of a {@link AbsSQLiteTask}.
     */
    /* package */ static final class AsyncQueryTask extends AbsSQLiteTask {
        /* package */ Uri uri;

        @Override
        public final void run() {
            final ContentResolver resolver = ((AsyncQueryHandler)handler).mContext.getContentResolver();
            final Object result;
            switch (message) {
            case MESSAGE_QUERY:
                result = execQuery(resolver);
                break;

            case MESSAGE_BATCH:
                result = applyBatch(resolver);
                break;

            case MESSAGE_INSERT:
                result = resolver.insert(uri, (ContentValues)values);
                break;

            case MESSAGE_DELETE:
                result = resolver.delete(uri, selection, selectionArgs);
                break;

            case MESSAGE_INSERTS:
                result = resolver.bulkInsert(uri, (ContentValues[])values);
                break;

            case MESSAGE_CALL:
                result = resolver.call(uri, selection, sortOrder, (Bundle)values);
                break;

            case MESSAGE_UPDATE:
                result = resolver.update(uri, (ContentValues)values, selection, selectionArgs);
                break;

            case MESSAGE_EXECUTE:
                result = ((AsyncQueryHandler)handler).onExecute(resolver, token, uri, selection, sortOrder, (Object[])values);
                break;

            default:
                throw new IllegalStateException("Unknown message: " + message);
            }

            UIHandler.sInstance.post(this, result);
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
                Log.e(handler.getClass().getName(), "Couldn't query from - " + uri, e);
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
