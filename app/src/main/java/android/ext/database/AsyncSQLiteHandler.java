package android.ext.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.ext.util.DebugUtils;
import android.ext.util.UIHandler;
import android.util.Log;
import java.util.concurrent.Executor;

/**
 * Class <tt>AsyncSQLiteHandler</tt> is a helper class to help make
 * handling asynchronous {@link SQLiteDatabase} queries easier.
 * @author Garfield
 */
public abstract class AsyncSQLiteHandler extends DatabaseHandler {
    /* package */ final SQLiteDatabase mDatabase;

    /**
     * Constructor
     * @param executor The serial <tt>Executor</tt>.
     * See {@link ThreadPool#createSerialExecutor()}.
     * @param db The {@link SQLiteDatabase}.
     * @see #AsyncSQLiteHandler(Executor, SQLiteDatabase, Object)
     */
    public AsyncSQLiteHandler(Executor executor, SQLiteDatabase db) {
        super(executor);
        mDatabase = db;
    }

    /**
     * Constructor
     * @param executor The serial <tt>Executor</tt>.
     * See {@link ThreadPool#createSerialExecutor()}.
     * @param db The {@link SQLiteDatabase}.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncSQLiteHandler(Executor, SQLiteDatabase)
     */
    public AsyncSQLiteHandler(Executor executor, SQLiteDatabase db, Object owner) {
        super(executor, owner);
        mDatabase = db;
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
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param selectionArgs You may include ? in where clause in the query, which will be replaced by the
     * values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @see #startQuery(int, String, String[], String, String[], String)
     */
    public final void startQuery(int token, String sql, String[] selectionArgs) {
        mExecutor.execute(obtainTask(token, MESSAGE_RAWQUERY, null, sql, selectionArgs, null));
    }

    /**
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * @param token A token passed into {@link #onQueryComplete} to identify the query.
     * @param table The table to query.
     * @param projection A list of which columns to return. Passing <tt>null</tt> will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
     * (excluding the WHERE itself). Passing <tt>null</tt> will return all rows for the given URI.
     * @param selectionArgs You may include ? in selection, which will be replaced by the values
     * from <em>selectionArgs</em>. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing <tt>null</tt> will use the default sort order, which may be unordered.
     * @see #startQuery(int, String, String[])
     */
    public final void startQuery(int token, String table, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        final SQLiteTask task = obtainTask(token, MESSAGE_QUERY, table, selection, selectionArgs, projection);
        task.sortOrder = sortOrder;
        mExecutor.execute(task);
    }

    /**
     * This method begins an asynchronous insert. When the insert is done {@link #onInsertComplete} is called.
     * @param token A token passed into {@link #onInsertComplete} to identify the insert.
     * @param table The table to insert the row into.
     * @param nullColumnHack Optional, may be <tt>null</tt>. SQL doesn't allow inserting a completely
     * empty row without naming at least one column name. If your provided values is empty, no column
     * names are known and an empty row can't be inserted. If not set to null, the nullColumnHack
     * parameter provides the name of nullable column name to explicitly insert a NULL into in the case
     * where your values is empty.
     * @param values The map contains the initial column values for the row. The keys should be the column
     * names and the values the column values.
     */
    public final void startInsert(int token, String table, String nullColumnHack, ContentValues values) {
        mExecutor.execute(obtainTask(token, MESSAGE_INSERT, table, nullColumnHack, null, values));
    }

    /**
     * This method begins an asynchronous replace. When the replace is done {@link #onReplaceComplete} is called.
     * @param token A token passed into {@link #onReplaceComplete} to identify the replace.
     * @param table The table in which to replace the row.
     * @param nullColumnHack Optional, may be <tt>null</tt>. SQL doesn't allow inserting a completely empty row
     * without naming at least one column name. If your provided values is empty, no column names are known and
     * an empty row can't be inserted. If not set to null, the nullColumnHack parameter provides the name of
     * nullable column name to explicitly insert a NULL into in the case where your values is empty.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     */
    public final void startReplace(int token, String table, String nullColumnHack, ContentValues values) {
        mExecutor.execute(obtainTask(token, MESSAGE_REPLACE, table, nullColumnHack, null, values));
    }

    /**
     * This method begins an asynchronous update. When the update is done {@link #onUpdateComplete} is called.
     * @param token A token passed into {@link #onUpdateComplete} to identify the update.
     * @param table The table to update in.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     * @param whereClause The WHERE clause to apply when updating. Passing <tt>null</tt> will update all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startUpdate(int token, String table, ContentValues values, String whereClause, String[] whereArgs) {
        mExecutor.execute(obtainTask(token, MESSAGE_UPDATE, table, whereClause, whereArgs, values));
    }

    /**
     * This method begins an asynchronous delete. When the delete is done {@link #onDeleteComplete} is called.
     * @param token A token passed into {@link #onDeleteComplete} to identify the delete.
     * @param table The table of the row to delete.
     * @param whereClause The WHERE clause to apply when deleting. Passing <tt>null</tt> or <tt>"1"</tt> will
     * delete all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     */
    public final void startDelete(int token, String table, String whereClause, String[] whereArgs) {
        mExecutor.execute(obtainTask(token, MESSAGE_DELETE, table, whereClause, whereArgs, null));
    }

    /**
     * Returns the {@link SQLiteDatabase} associated with this object.
     * @return The <tt>SQLiteDatabase</tt>.
     */
    public final SQLiteDatabase getDatabase() {
        return mDatabase;
    }

    @Override
    public final Object newInstance() {
        return new SQLiteTask();
    }

    @Override
    public final void dispatchMessage(int message, int token, Object result) {
        if (!validateOwner()) {
            return;
        }

        switch (message) {
        case MESSAGE_INSERT:
            onInsertComplete(token, (long)result);
            break;

        case MESSAGE_REPLACE:
            onReplaceComplete(token, (long)result);
            break;

        default:
            super.dispatchMessage(message, token, result);
        }
    }

    /**
     * Executes SQL statements on a background thread.
     * @param db The {@link SQLiteDatabase}.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param params The parameters passed in from {@link #startExecute}.
     * @return The execution result.
     */
    protected Object onExecute(SQLiteDatabase db, int token, Object[] params) {
        return null;
    }

    /**
     * Called when an asynchronous insert is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startInsert}.
     * @param id The row ID of the newly inserted row, or -1 if an error occurred.
     */
    protected void onInsertComplete(int token, long id) {
    }

    /**
     * Called when an asynchronous replace is completed on the UI thread.
     * @param token The token to identify the replace, passed in from {@link #startReplace}.
     * @param id The row ID of the newly inserted row, or -1 if an error occurred.
     */
    protected void onReplaceComplete(int token, long id) {
    }

    /**
     * Recycles the specified {@link SQLiteTask} to the task pool.
     */
    /* package */ final void recycleTask(SQLiteTask task) {
        task.table  = null;
        task.values = null;
        task.sortOrder = null;
        task.selection = null;
        task.selectionArgs = null;
        mTaskPool.recycle(task);
    }

    /**
     * Retrieves a new {@link SQLiteTask} from the task pool. Allows us to avoid allocating new tasks in many cases.
     */
    private SQLiteTask obtainTask(int token, int message, String table, String selection, String[] selectionArgs, Object values) {
        final SQLiteTask task = (SQLiteTask)mTaskPool.obtain();
        task.token = token;
        task.table = table;
        task.values  = values;
        task.message = message;
        task.selection = selection;
        task.selectionArgs = selectionArgs;
        return task;
    }

    /**
     * Class <tt>SQLiteTask</tt> is an implementation of a {@link Runnable}.
     */
    /* package */ final class SQLiteTask implements Runnable {
        /* package */ int token;
        /* package */ int message;
        /* package */ String table;
        /* package */ Object values;
        /* package */ String sortOrder;
        /* package */ String selection;
        /* package */ String[] selectionArgs;

        @Override
        public void run() {
            if (!mDatabase.isOpen()) {
                Log.w(getClass().getName(), "The SQLiteDatabase was closed.");
                return;
            }

            final Object result;
            switch (message) {
            case MESSAGE_QUERY:
            case MESSAGE_RAWQUERY:
                result = execQuery();
                break;

            case MESSAGE_EXECUTE:
                result = onExecute(mDatabase, token, (Object[])values);
                break;

            case MESSAGE_DELETE:
                result = mDatabase.delete(table, selection, selectionArgs);
                break;

            case MESSAGE_INSERT:
                result = mDatabase.insert(table, selection, (ContentValues)values);
                break;

            case MESSAGE_REPLACE:
                result = mDatabase.replace(table, selection, (ContentValues)values);
                break;

            case MESSAGE_UPDATE:
                result = mDatabase.update(table, (ContentValues)values, selection, selectionArgs);
                break;

            default:
                throw new IllegalStateException("Unknown message: " + message);
            }

            UIHandler.sInstance.sendMessage(AsyncSQLiteHandler.this, message, token, result);
            recycleTask(this);
        }

        private Cursor execQuery() {
            DebugUtils.__checkStartMethodTracing();
            final Cursor cursor;
            if (message == MESSAGE_RAWQUERY) {
                cursor = mDatabase.rawQuery(selection, selectionArgs);
            } else {
                cursor = mDatabase.query(table, (String[])values, selection, selectionArgs, null, null, sortOrder);
            }

            if (cursor != null) {
                // Calling getCount() causes the cursor window to be filled, which
                // will make the first access on the main thread a lot faster.
                cursor.getCount();
            }

            DebugUtils.__checkStopMethodTracing("AsyncSQLiteHandler", "execQuery");
            return cursor;
        }
    }
}
