package android.ext.database;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.ext.util.UIHandler;
import android.util.Log;

/**
 * Class <tt>AsyncSQLiteHandler</tt> is a helper class to help make
 * handling asynchronous {@link SQLiteDatabase} queries easier.
 * @author Garfield
 * @version 1.0
 */
public abstract class AsyncSQLiteHandler extends DatabaseHandler {
    /* package */ final WeakReference<SQLiteDatabase> mDatabase;

    /**
     * Constructor
     * @param executor The serial <tt>Executor</tt>.
     * @param db The {@link SQLiteDatabase}.
     * @see #AsyncSQLiteHandler(Executor, SQLiteDatabase, Object)
     * @see ThreadPool#newSerialExecutor()
     */
    public AsyncSQLiteHandler(Executor executor, SQLiteDatabase db) {
        super(executor);
        mDatabase = new WeakReference<SQLiteDatabase>(db);
    }

    /**
     * Constructor
     * @param executor The serial <tt>Executor</tt>.
     * @param db The {@link SQLiteDatabase}.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncSQLiteHandler(Executor, SQLiteDatabase)
     * @see ThreadPool#newSerialExecutor()
     */
    public AsyncSQLiteHandler(Executor executor, SQLiteDatabase db, Object owner) {
        super(executor, owner);
        mDatabase = new WeakReference<SQLiteDatabase>(db);
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
        final SQLiteTask task = new SQLiteTask(token, MESSAGE_EXECUTE, null, null, null);
        task.values = params;
        mExecutor.execute(task);
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
        mExecutor.execute(new SQLiteTask(token, MESSAGE_RAWQUERY, null, sql, selectionArgs));
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
        final SQLiteTask task = new SQLiteTask(token, MESSAGE_QUERY, table, selection, selectionArgs);
        task.values = projection;
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
        final SQLiteTask task = new SQLiteTask(token, MESSAGE_INSERT, table, nullColumnHack, null);
        task.values = values;
        mExecutor.execute(task);
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
        final SQLiteTask task = new SQLiteTask(token, MESSAGE_REPLACE, table, nullColumnHack, null);
        task.values = values;
        mExecutor.execute(task);
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
        final SQLiteTask task = new SQLiteTask(token, MESSAGE_UPDATE, table, whereClause, whereArgs);
        task.values = values;
        mExecutor.execute(task);
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
        mExecutor.execute(new SQLiteTask(token, MESSAGE_DELETE, table, whereClause, whereArgs));
    }

    /**
     * Returns the {@link SQLiteDatabase} associated with this object.
     * @return The <tt>SQLiteDatabase</tt>, or <tt>null</tt> if the
     * database was released by the GC.
     */
    public final SQLiteDatabase getDatabase() {
        return mDatabase.get();
    }

    @Override
    public void dispatchMessage(int message, int token, Object result) {
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
     * Class <tt>SQLiteTask</tt> is an implementation of a {@link Runnable}.
     */
    private final class SQLiteTask implements Runnable {
        /* package */ final int token;
        /* package */ final int message;
        /* package */ final String table;
        /* package */ Object values;
        /* package */ String sortOrder;
        /* package */ final String selection;
        /* package */ final String[] selectionArgs;

        public SQLiteTask(int token, int message, String table, String selection, String[] selectionArgs) {
            this.token = token;
            this.table = table;
            this.message = message;
            this.selection = selection;
            this.selectionArgs = selectionArgs;
        }

        @Override
        public void run() {
            final SQLiteDatabase db = mDatabase.get();
            if (db == null || !db.isOpen()) {
                Log.w(getClass().getName(), "The SQLiteDatabase was closed or released by the GC.");
                return;
            }

            final Object result;
            switch (message) {
            case MESSAGE_QUERY:
            case MESSAGE_RAWQUERY:
                result = execQuery(db);
                break;

            case MESSAGE_EXECUTE:
                result = onExecute(db, token, (Object[])values);
                break;

            case MESSAGE_DELETE:
                result = db.delete(table, selection, selectionArgs);
                break;

            case MESSAGE_INSERT:
                result = db.insert(table, selection, (ContentValues)values);
                break;

            case MESSAGE_REPLACE:
                result = db.replace(table, selection, (ContentValues)values);
                break;

            case MESSAGE_UPDATE:
                result = db.update(table, (ContentValues)values, selection, selectionArgs);
                break;

            default:
                throw new IllegalStateException("Unknown message: " + message);
            }

            UIHandler.sInstance.sendMessage(AsyncSQLiteHandler.this, message, token, result);
        }

        private Cursor execQuery(SQLiteDatabase db) {
            final Cursor cursor;
            if (message == MESSAGE_RAWQUERY) {
                cursor = db.rawQuery(selection, selectionArgs);
            } else {
                cursor = db.query(table, (String[])values, selection, selectionArgs, null, null, sortOrder);
            }

            if (cursor != null) {
                // Calling getCount() causes the cursor window to be filled, which
                // will make the first access on the main thread a lot faster.
                cursor.getCount();
            }

            return cursor;
        }
    }
}
