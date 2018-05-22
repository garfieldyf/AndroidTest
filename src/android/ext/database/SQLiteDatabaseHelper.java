package android.ext.database;

import java.util.ArrayList;
import java.util.List;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.ext.util.ArrayUtils;
import android.net.Uri;
import android.util.ArrayMap;

/**
 * Class SQLiteDatabaseHelper
 * @author Garfield
 * @version 2.0
 */
public abstract class SQLiteDatabaseHelper extends SQLiteOpenHelper {
    private final ArrayMap<String, List<ContentObserver>> mObservables;

    /**
     * Constructor
     * <P>Create a helper object to create, open, and/or manage a database. This method
     * always returns very quickly. The database is not actually created or opened until
     * one of {@link SQLiteOpenHelper#getWritableDatabase getWritableDatabase} or
     * {@link SQLiteOpenHelper#getReadableDatabase getReadableDatabase} is called.</p>
     * @param context The <tt>Context</tt>.
     * @param name The name of the database file, or <tt>null</tt> for an in-memory database.
     * @param factory The cursor factory to use for creating cursor objects, or <tt>null</tt>
     * for the default.
     * @param version The version of the database (starting at 1); if the database is older,
     * {@link SQLiteOpenHelper#onUpgrade onUpgrade} will be used to upgrade the database.
     */
    public SQLiteDatabaseHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        mObservables = new ArrayMap<String, List<ContentObserver>>();
    }

    /**
     * Dispatchs update on each observer with the specified <em>table</em>.
     * @param table The table name to identify the notification.
     * @param selfChange Whether the observer is interested in notifications
     * for changes made self.
     * @see #dispatchChange(String, boolean, Uri)
     */
    public final void dispatchChange(String table, boolean selfChange) {
        dispatchChange(table, selfChange, null);
    }

    /**
     * Dispatchs update on each observer with the specified <em>table</em>.
     * @param table The table name to identify the notification.
     * @param selfChange Whether the observer is interested in notifications
     * for changes made self.
     * @param uri The Uri of the changed content, or <tt>null</tt> if unknown.
     * @see #dispatchChange(String, boolean)
     */
    public void dispatchChange(String table, boolean selfChange, Uri uri) {
        synchronized (mObservables) {
            final List<ContentObserver> observers = mObservables.get(table);
            final int size = ArrayUtils.getSize(observers);
            for (int i = 0; i < size; ++i) {
                final ContentObserver observer = observers.get(i);
                if (!selfChange || observer.deliverSelfNotifications()) {
                    observer.dispatchChange(selfChange, uri);
                }
            }
        }
    }

    /**
     * Adds an observer to the list with the specified <em>table</em>.
     * The observer cannot be <tt>null</tt> and it must <b>not</b>
     * already be registered.
     * @param table The table name to register.
     * @param observer The observer to register.
     * @see #unregisterObserver(String, ContentObserver)
     * @see #unregisterAllObservers(String)
     * @see #unregisterAllObservers()
     */
    public final void registerObserver(String table, ContentObserver observer) {
        synchronized (mObservables) {
            List<ContentObserver> observers = mObservables.get(table);
            if (observers == null) {
                mObservables.put(table, observers = new ArrayList<ContentObserver>());
                observers.add(observer);
            } else if (!observers.contains(observer)) {
                observers.add(observer);
            }
        }
    }

    /**
     * Removes a previously registered observer with the specified <em>table</em>.
     * The observer must not be <tt>null</tt> and it must already have been registered.
     * @param table The table name to unregister.
     * @param observer The observer to unregister.
     * @see #registerObserver(String, ContentObserver)
     * @see #unregisterAllObservers(String)
     * @see #unregisterAllObservers()
     */
    public final void unregisterObserver(String table, ContentObserver observer) {
        synchronized (mObservables) {
            final List<ContentObserver> observers = mObservables.get(table);
            if (observers != null) {
                observers.remove(observer);
            }
        }
    }

    /**
     * Removes all registered observers with the specified <em>table</em>.
     * @param table The table name to unregister.
     * @see #registerObserver(String, ContentObserver)
     * @see #unregisterObserver(String, ContentObserver)
     * @see #unregisterAllObservers()
     */
    public final void unregisterAllObservers(String table) {
        synchronized (mObservables) {
            final List<ContentObserver> observers = mObservables.remove(table);
            if (observers != null) {
                observers.clear();
            }
        }
    }

    /**
     * Removes all registered observers.
     * @see #registerObserver(String, ContentObserver)
     * @see #unregisterObserver(String, ContentObserver)
     * @see #unregisterAllObservers(String)
     */
    public final void unregisterAllObservers() {
        synchronized (mObservables) {
            for (int i = mObservables.size() - 1; i >= 0; --i) {
                mObservables.valueAt(i).clear();
            }

            mObservables.clear();
        }
    }

    /**
     * Inserts a row into the <em>table</em> and notify the registered observers.
     * @param table The table to insert the row into.
     * @param nullColumnHack Optional, may be <tt>null</tt>. SQL doesn't allow inserting
     * a completely empty row without naming at least one column name. If your provided
     * values is empty, no column names are known and an empty row can't be inserted. If
     * not set to <tt>null</tt>, the nullColumnHack parameter provides the name of nullable
     * column name to explicitly insert a NULL into in the case where your values is empty.
     * @param initialValues A map contains the initial column values for the row. The keys
     * should be the column names and the values the column values.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public final long insert(String table, String nullColumnHack, ContentValues initialValues) {
        final long id = getWritableDatabase().insert(table, nullColumnHack, initialValues);
        if (id != -1) {
            dispatchChange(table, false, null);
        }

        return id;
    }

    /**
     * Replaces a row in the <em>table</em> and notify the registered observers.
     * @param table The table in which to replace the row.
     * @param nullColumnHack SQL doesn't allow inserting a completely empty row, so
     * if initialValues is empty this column will explicitly be assigned a NULL value.
     * @param initialValues A map contains the initial column values for the row.
     * The keys should be the column names and the values the column values.
     * @return The row ID of the newly inserted row, or -1 if an error occurred.
     */
    public final long replace(String table, String nullColumnHack, ContentValues initialValues) {
        final long id = getWritableDatabase().replace(table, nullColumnHack, initialValues);
        if (id != -1) {
            dispatchChange(table, false, null);
        }

        return id;
    }

    /**
     * Deletes rows in the <em>table</em> and notify the registered observers.
     * @param table The table to delete from.
     * @param whereClause The WHERE clause to apply when deleting. Passing null
     * or <tt>"1"</tt> will delete all rows.
     * @param whereArgs You may include ? in whereClause, which will be replaced
     * by the values from whereArgs. The values will be bound as Strings.
     * @return The number of rows affected if a whereClause is passed in, <tt>0</tt>
     * otherwise. To remove all rows and get a count pass "1" as the whereClause.
     */
    public final int delete(String table, String whereClause, String[] whereArgs) {
        final int rowsAffected = getWritableDatabase().delete(table, whereClause, whereArgs);
        if (rowsAffected > 0) {
            dispatchChange(table, false, null);
        }

        return rowsAffected;
    }

    /**
     * Updates rows in the <em>table</em> and notify the registered observers.
     * @param table The table to update in.
     * @param values A map from column names to new column values. <tt>null</tt>
     * is a valid value that will be translated to NULL.
     * @param whereClause The WHERE clause to apply when updating.
     * @param whereArgs You may include ? in whereClause, which will be replaced
     * by the values from whereArgs. The values will be bound as Strings.
     * @return The number of rows affected.
     */
    public final int update(String table, ContentValues values, String whereClause, String[] whereArgs) {
        final int rowsAffected = getWritableDatabase().update(table, values, whereClause, whereArgs);
        if (rowsAffected > 0) {
            dispatchChange(table, false, null);
        }

        return rowsAffected;
    }

    /**
     * Close any open database object and remove all registered observers.
     */
    @Override
    public void close() {
        super.close();
        unregisterAllObservers();
    }
}