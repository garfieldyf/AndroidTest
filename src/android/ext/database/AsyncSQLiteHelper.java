package android.ext.database;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.ext.content.AsyncTaskLoader;
import android.util.Log;

/**
 * Class <tt>AsyncSQLiteHelper</tt> is a helper class to help make
 * handling asynchronous {@link SQLiteDatabase} queries easier.
 * @author Garfield
 * @version 1.0
 */
public abstract class AsyncSQLiteHelper extends AsyncTaskLoader<Integer, Object, Object> {
    private static final int MESSAGE_QUERY    = 1;
    private static final int MESSAGE_INSERT   = 2;
    private static final int MESSAGE_UPDATE   = 3;
    private static final int MESSAGE_DELETE   = 4;
    private static final int MESSAGE_REPLACE  = 5;
    private static final int MESSAGE_EXECUTE  = 6;
    private static final int MESSAGE_RAWQUERY = 7;

    /**
     * The {@link SQLiteDatabase}.
     */
    private final WeakReference<SQLiteDatabase> mDatabase;

    /**
     * Constructor
     * @param db The {@link SQLiteDatabase}.
     * @param executor The serial {@link Executor}.
     * @see #AsyncSQLiteHelper(SQLiteDatabase, Executor, Object)
     */
    public AsyncSQLiteHelper(SQLiteDatabase db, Executor executor) {
        super(executor);
        mDatabase = new WeakReference<SQLiteDatabase>(db);
    }

    /**
     * Constructor
     * @param db The {@link SQLiteDatabase}.
     * @param executor The serial {@link Executor}.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncSQLiteHelper(SQLiteDatabase, Executor)
     */
    public AsyncSQLiteHelper(SQLiteDatabase db, Executor executor, Object owner) {
        super(executor, owner);
        mDatabase = new WeakReference<SQLiteDatabase>(db);
    }

    /**
     * This method begins an asynchronous execute SQL statements. When the SQL statements is
     * executing {@link #onExecute} is called on a background thread. After the SQL statements
     * is done {@link #onExecuteComplete} is called. <p><b>Note: This method must be invoked
     * on the UI thread.</b></p>
     * @param token A token passed into {@link #onExecute} and {@link #onExecuteComplete} to
     * identify execute.
     * @param params The parameters passed into {@link #onExecute}. If no parameters, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @see #onExecute(SQLiteDatabase, int, Object[])
     * @see #onExecuteComplete(int, Object)
     */
    public final void startExecute(int token, Object... params) {
        load(token, MESSAGE_EXECUTE, params);
    }

    /**
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onQueryComplete} to identify the query.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param selectionArgs You may include ? in where clause in the query, which will be replaced by the
     * values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @see #startQuery(int, String, String[], String, String[], String, String)
     * @see #onQueryComplete(int, Cursor)
     */
    public final void startQuery(int token, String sql, String[] selectionArgs) {
        load(token, MESSAGE_RAWQUERY, sql, selectionArgs);
    }

    /**
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onQueryComplete} to identify the query.
     * @param table The table name to compile the query against.
     * @param columns A list of which columns to return. Passing <tt>null</tt> will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
     * (excluding the WHERE itself). Passing <tt>null</tt> will return all rows for the given table.
     * @param selectionArgs You may include ? in where clause in the query, which will be replaced by the
     * values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @param orderBy How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing <tt>null</tt> will use the default sort order, which may be unordered.
     * @param limit Limits the number of rows returned by the query, formatted as LIMIT clause (excluding the
     * LIMIT itself). Passing <tt>null</tt> denotes no LIMIT clause.
     * @see #startQuery(int, String, String[])
     * @see #onQueryComplete(int, Cursor)
     */
    public final void startQuery(int token, String table, String[] columns, String selection, String[] selectionArgs, String orderBy, String limit) {
        load(token, MESSAGE_QUERY, table, columns, selection, selectionArgs, orderBy, limit);
    }

    /**
     * This method begins an asynchronous insert. When the insert is done {@link #onInsertComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onInsertComplete} to identify the insert.
     * @param table The table to insert the row into.
     * @param nullColumnHack Optional, may be <tt>null</tt>. SQL doesn't allow inserting a completely
     * empty row without naming at least one column name. If your provided values is empty, no column
     * names are known and an empty row can't be inserted. If not set to null, the nullColumnHack
     * parameter provides the name of nullable column name to explicitly insert a NULL into in the case
     * where your values is empty.
     * @param values The map contains the initial column values for the row. The keys should be the column
     * names and the values the column values.
     * @see #onInsertComplete(int, long)
     */
    public final void startInsert(int token, String table, String nullColumnHack, ContentValues values) {
        load(token, MESSAGE_INSERT, table, nullColumnHack, values);
    }

    /**
     * This method begins an asynchronous replace. When the replace is done {@link #onReplaceComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onReplaceComplete} to identify the replace.
     * @param table The table in which to replace the row.
     * @param nullColumnHack Optional, may be <tt>null</tt>. SQL doesn't allow inserting a completely empty row
     * without naming at least one column name. If your provided values is empty, no column names are known and
     * an empty row can't be inserted. If not set to null, the nullColumnHack parameter provides the name of
     * nullable column name to explicitly insert a NULL into in the case where your values is empty.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     * @see #onReplaceComplete(int, long)
     */
    public final void startReplace(int token, String table, String nullColumnHack, ContentValues values) {
        load(token, MESSAGE_REPLACE, table, nullColumnHack, values);
    }

    /**
     * This method begins an asynchronous update. When the update is done {@link #onUpdateComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onUpdateComplete} to identify the update.
     * @param table The table to update in.
     * @param values A map from column names to new column values. <tt>null</tt> is a valid value that will be
     * translated to NULL.
     * @param whereClause The WHERE clause to apply when updating. Passing <tt>null</tt> will update all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     * @see #onUpdateComplete(int, int)
     */
    public final void startUpdate(int token, String table, ContentValues values, String whereClause, String[] whereArgs) {
        load(token, MESSAGE_UPDATE, table, values, whereClause, whereArgs);
    }

    /**
     * This method begins an asynchronous delete. When the delete is done {@link #onDeleteComplete} is called.
     * <p><b>Note: This method must be invoked on the UI thread.</b></p>
     * @param token A token passed into {@link #onDeleteComplete} to identify the delete.
     * @param table The table to delete from.
     * @param whereClause The WHERE clause to apply when deleting. Passing <tt>null</tt> or <tt>"1"</tt> will
     * delete all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced by the values from <em>whereArgs</em>.
     * The values will be bound as Strings.
     * @see #onDeleteComplete(int, int)
     */
    public final void startDelete(int token, String table, String whereClause, String[] whereArgs) {
        load(token, MESSAGE_DELETE, table, whereClause, whereArgs);
    }

    @Override
    protected Object loadInBackground(Task<?, ?> task, Integer token, Object[] params) {
        final SQLiteDatabase db = mDatabase.get();
        if (db == null || !db.isOpen()) {
            Log.w(getClass().getName(), "The SQLiteDatabase was closed or released by the GC.");
            return this;
        }

        final int message = (Integer)params[0];
        switch (message) {
        case MESSAGE_QUERY:
        case MESSAGE_RAWQUERY:
            return execQuery(db, message, params);

        case MESSAGE_INSERT:
            return db.insert((String)params[1], (String)params[2], (ContentValues)params[3]);

        case MESSAGE_UPDATE:
            return db.update((String)params[1], (ContentValues)params[2], (String)params[3], (String[])params[4]);

        case MESSAGE_DELETE:
            return db.delete((String)params[1], (String)params[2], (String[])params[3]);

        case MESSAGE_REPLACE:
            return db.replace((String)params[1], (String)params[2], (ContentValues)params[3]);

        case MESSAGE_EXECUTE:
            return onExecute(db, token, (Object[])params[1]);

        default:
            throw new IllegalStateException("Unknown message: " + message);
        }
    }

    @Override
    protected void onLoadComplete(Integer token, Object[] params, Object result) {
        if (result == this) {
            return;
        }

        switch ((Integer)params[0]) {
        case MESSAGE_QUERY:
        case MESSAGE_RAWQUERY:
            onQueryComplete(token, (Cursor)result);
            break;

        case MESSAGE_INSERT:
            onInsertComplete(token, (Long)result);
            break;

        case MESSAGE_UPDATE:
            onUpdateComplete(token, (Integer)result);
            break;

        case MESSAGE_DELETE:
            onDeleteComplete(token, (Integer)result);
            break;

        case MESSAGE_REPLACE:
            onReplaceComplete(token, (Long)result);
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
        final int message = (Integer)params[0];
        if (message == MESSAGE_QUERY || message == MESSAGE_RAWQUERY) {
            cancelTask(token, false);
        }

        return false;
    }

    /**
     * Executes SQL statements on a background thread.
     * @param db The {@link SQLiteDatabase}.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param params The parameters passed in from {@link #startExecute}.
     * @return The execution result.
     * @see #startExecute(int, Object[])
     * @see #onExecuteComplete(int, Object)
     */
    protected Object onExecute(SQLiteDatabase db, int token, Object[] params) {
        return null;
    }

    /**
     * Called when an asynchronous execute is completed on the UI thread.
     * @param token The token to identify the execute, passed in from {@link #startExecute}.
     * @param result The result, returned earlier by {@link #onExecute}.
     * @see #startExecute(int, Object[])
     * @see #onExecute(SQLiteDatabase, int, Object[])
     */
    protected void onExecuteComplete(int token, Object result) {
    }

    /**
     * Called when an asynchronous query is completed on the UI thread.
     * @param token The token to identify the query, passed in from {@link #startQuery}.
     * @param cursor The cursor holding the results from the query.
     * @see #startQuery(int, String, String[])
     * @see #startQuery(int, String, String[], String, String[], String, String)
     */
    protected void onQueryComplete(int token, Cursor cursor) {
    }

    /**
     * Called when an asynchronous insert is completed on the UI thread.
     * @param token The token to identify the insert, passed in from {@link #startInsert}.
     * @param id The row ID of the newly inserted row, or -1 if an error occurred.
     * @see #startInsert(int, String, String, ContentValues)
     */
    protected void onInsertComplete(int token, long id) {
    }

    /**
     * Called when an asynchronous replace is completed on the UI thread.
     * @param token The token to identify the replace, passed in from {@link #startReplace}.
     * @param id The row ID of the newly inserted row, or -1 if an error occurred.
     * @see #startReplace(int, String, String, ContentValues)
     */
    protected void onReplaceComplete(int token, long id) {
    }

    /**
     * Called when an asynchronous update is completed on the UI thread.
     * @param token The token to identify the update, passed in from {@link #startUpdate}.
     * @param rowsAffected The number of rows affected.
     * @see #startUpdate(int, String, ContentValues, String, String[])
     */
    protected void onUpdateComplete(int token, int rowsAffected) {
    }

    /**
     * Called when an asynchronous delete is completed on the UI thread.
     * @param token The token to identify the delete, passed in from {@link #startDelete}.
     * @param rowsAffected The number of rows affected if {@link #startDelete} whereClause
     * is passed in, 0 otherwise. To remove all rows and get a count pass "1" as the
     * {@link #startDelete} whereClause.
     * @see #startDelete(int, String, String, String[])
     */
    protected void onDeleteComplete(int token, int rowsAffected) {
    }

    private Cursor execQuery(SQLiteDatabase db, int message, Object[] params) {
        Cursor cursor = null;
        try {
            if (message == MESSAGE_RAWQUERY) {
                cursor = db.rawQuery((String)params[1], (String[])params[2]);
            } else {
                cursor = db.query((String)params[1], (String[])params[2], (String)params[3], (String[])params[4], null, null, (String)params[5], (String)params[6]);
            }

            if (cursor != null) {
                // Calling getCount() causes the cursor window to be filled, which
                // will make the first access on the main thread a lot faster.
                cursor.getCount();
            }
        } catch (Exception e) {
            Log.e(getClass().getName(), new StringBuilder("Couldn't query - ").append(params[1]).toString(), e);
        }

        return cursor;
    }
}
