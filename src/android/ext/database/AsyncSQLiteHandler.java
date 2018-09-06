package android.ext.database;

import java.lang.ref.WeakReference;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.ext.util.UIHandler;
import android.os.Message;
import android.util.Log;

/**
 * Class <tt>AsyncSQLiteHandler</tt> is a helper class to help make
 * handling asynchronous {@link SQLiteDatabase} queries easier.
 * @author Garfield
 * @version 1.0
 */
public abstract class AsyncSQLiteHandler extends DatabaseHandler<String> {
    /**
     * The {@link SQLiteDatabase}.
     */
    private final WeakReference<SQLiteDatabase> mDatabase;

    /**
     * Constructor
     * @param db The {@link SQLiteDatabase}.
     * @see #AsyncSQLiteHandler(SQLiteDatabase, Object)
     */
    public AsyncSQLiteHandler(SQLiteDatabase db) {
        mDatabase = new WeakReference<SQLiteDatabase>(db);
    }

    /**
     * Constructor
     * @param db The {@link SQLiteDatabase}.
     * @param owner The owner object. See {@link #setOwner(Object)}.
     * @see #AsyncSQLiteHandler(SQLiteDatabase)
     */
    public AsyncSQLiteHandler(SQLiteDatabase db, Object owner) {
        super(owner);
        mDatabase = new WeakReference<SQLiteDatabase>(db);
    }

    /**
     * This method begins an asynchronous query. When the query is done {@link #onQueryComplete} is called.
     * @param token A token passed into {@link #onQueryComplete} to identify the query.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param selectionArgs You may include ? in where clause in the query, which will be replaced by the
     * values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @see #startQuery(int, Object, String[], String, String[], String)
     */
    public final void startQuery(int token, String sql, String[] selectionArgs) {
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_RAWQUERY
         * msg.obj  - { sql, selectionArgs }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_RAWQUERY, 0, new Object[] { sql, selectionArgs }));
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
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_INSERT
         * msg.obj  - { table, nullColumnHack, values }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_INSERT, 0, new Object[] { table, nullColumnHack, values }));
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
        /*
         * msg.what - token
         * msg.arg1 - MESSAGE_REPLACE
         * msg.obj  - { table, nullColumnHack, values }
         */
        mHandler.sendMessage(Message.obtain(mHandler, token, MESSAGE_REPLACE, 0, new Object[] { table, nullColumnHack, values }));
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
    public boolean handleMessage(Message msg) {
        final SQLiteDatabase db = mDatabase.get();
        if (db == null || !db.isOpen()) {
            Log.w(getClass().getName(), "The SQLiteDatabase was closed or released by the GC.");
            return true;
        }

        /*
         * msg.what - token
         * msg.arg1 - message
         * msg.obj  - params
         */
        final Object[] params = (Object[])msg.obj;
        final Object result;
        switch (msg.arg1) {
        case MESSAGE_QUERY:
        case MESSAGE_RAWQUERY:
            result = execQuery(db, msg.arg1, params);
            break;

        case MESSAGE_EXECUTE:
            result = onExecute(db, msg.what, params);
            break;

        case MESSAGE_INSERT:
            // params - { table, nullColumnHack, values }
            result = db.insert((String)params[0], (String)params[1], (ContentValues)params[2]);
            break;

        case MESSAGE_REPLACE:
            // params - { table, nullColumnHack, values }
            result = db.replace((String)params[0], (String)params[1], (ContentValues)params[2]);
            break;

        case MESSAGE_DELETE:
            // params - { table, whereClause, whereArgs }
            result = db.delete((String)params[0], (String)params[1], (String[])params[2]);
            break;

        case MESSAGE_UPDATE:
            // params - { table, values, whereClause, whereArgs }
            result = db.update((String)params[0], (ContentValues)params[1], (String)params[2], (String[])params[3]);
            break;

        default:
            throw new IllegalStateException("Unknown message: " + msg.arg1);
        }

        UIHandler.sInstance.sendMessage(this, msg.arg1, msg.what, result);
        return true;
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

    private Cursor execQuery(SQLiteDatabase db, int message, Object[] params) {
        final Cursor cursor;
        if (message == MESSAGE_RAWQUERY) {
            // params - { sql, selectionArgs }
            cursor = db.rawQuery((String)params[0], (String[])params[1]);
        } else {
            // params - { table, columns, selection, selectionArgs, orderBy }
            cursor = db.query((String)params[0], (String[])params[1], (String)params[2], (String[])params[3], null, null, (String)params[4]);
        }

        if (cursor != null) {
            // Calling getCount() causes the cursor window to be filled, which
            // will make the first access on the main thread a lot faster.
            cursor.getCount();
        }

        return cursor;
    }
}
