package android.ext.database;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDoneException;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteProgram;
import android.database.sqlite.SQLiteStatement;
import android.ext.annotation.CursorField;
import android.ext.util.ByteArrayBuffer;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.util.JsonWriter;
import android.util.Log;

/**
 * Class DatabaseUtils
 * @author Garfield
 * @version 1.0
 */
public final class DatabaseUtils {
    /**
     * Returns the numbers of rows in the <tt>Cursor</tt>,
     * handling <tt>null Cursor</tt>.
     * @param cursor The <tt>Cursor</tt>.
     * @return The numbers of rows in the <tt>Cursor</tt>.
     */
    public static int getCount(Cursor cursor) {
        return (cursor != null ? cursor.getCount() : 0);
    }

    /**
     * Executes a statement that returns a 1 by 1 table with a numeric value. For
     * example, <tt>SELECT COUNT(*) FROM table</tt>.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param bindArgs You may include ? in where clause in the query, which will
     * be replaced by the values from <em>bindArgs</em>. If no arguments, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @return The result of the query, or <tt>null</tt> if the query returns 0 rows.
     * @see #simpleQueryLong(ContentResolver, Uri, String, String, String[])
     */
    public static Long simpleQueryLong(SQLiteDatabase db, String sql, Object... bindArgs) {
        final SQLiteStatement prog = db.compileStatement(sql);
        try {
            bindArgs(prog, bindArgs);
            return prog.simpleQueryForLong();
        } catch (SQLiteException e) {
            return null;
        } finally {
            prog.close();
        }
    }

    /**
     * Query the given <em>uri</em>, return a 1 by 1 table with a numeric value.
     * @param resolver The <tt>ContentResolver</tt>.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param column The column to query.
     * @param selection A filter declaring which row to return, formatted as an SQL
     * WHERE clause (excluding the WHERE itself).
     * @param selectionArgs You may include ? in selection, which will be replaced
     * by the values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @return The result of the query, or <tt>null</tt> if the query returns 0 rows.
     * @see #simpleQueryLong(SQLiteDatabase, String, Object[])
     */
    public static Long simpleQueryLong(ContentResolver resolver, Uri uri, String column, String selection, String[] selectionArgs) {
        final Object result = simpleQuery(resolver, uri, column, selection, selectionArgs);
        if (result instanceof Long) {
            return (Long)result;
        } else if (result instanceof String) {
            return Long.valueOf((String)result, 10);
        } else {
            return null;
        }
    }

    /**
     * Executes a statement that returns a 1 by 1 table with a text value.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param bindArgs You may include ? in where clause in the query, which will
     * be replaced by the values from <em>bindArgs</em>. If no arguments, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @return The result of the query, or <tt>null</tt> if the query returns 0 rows.
     * @see #simpleQueryString(ContentResolver, Uri, String, String, String[])
     */
    public static String simpleQueryString(SQLiteDatabase db, String sql, Object... bindArgs) {
        final SQLiteStatement prog = db.compileStatement(sql);
        try {
            bindArgs(prog, bindArgs);
            return prog.simpleQueryForString();
        } catch (SQLiteException e) {
            return null;
        } finally {
            prog.close();
        }
    }

    /**
     * Query the given <em>uri</em>, return a 1 by 1 table with a text value.
     * @param resolver The <tt>ContentResolver</tt>.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param column The column to query.
     * @param selection A filter declaring which row to return, formatted as an SQL
     * WHERE clause (excluding the WHERE itself).
     * @param selectionArgs You may include ? in selection, which will be replaced
     * by the values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @return The result of the query, or <tt>null</tt> if the query returns 0 rows.
     * @see #simpleQueryString(SQLiteDatabase, String, Object[])
     */
    public static String simpleQueryString(ContentResolver resolver, Uri uri, String column, String selection, String[] selectionArgs) {
        final Object result = simpleQuery(resolver, uri, column, selection, selectionArgs);
        return (result != null ? result.toString() : null);
    }

    /**
     * Executes a statement that returns a 1 by 1 table with a blob value.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param bindArgs You may include ? in where clause in the query, which will
     * be replaced by the values from <em>bindArgs</em>. If no arguments, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @return The {@link InputStream} for a copy of the blob value, or <tt>null</tt>
     * if the value is <tt>null</tt> or could not be read for some reason.
     * @throws SQLiteDoneException If the query returns <tt>0</tt> rows.
     */
    @SuppressWarnings("resource")
    public static InputStream simpleQuery(SQLiteDatabase db, String sql, Object... bindArgs) {
        final SQLiteStatement prog = db.compileStatement(sql);
        try {
            bindArgs(prog, bindArgs);
            final ParcelFileDescriptor fd = prog.simpleQueryForBlobFileDescriptor();
            return (fd != null ? new AutoCloseInputStream(fd) : null);
        } finally {
            prog.close();
        }
    }

    /**
     * Executes a statement that returns a 1 by 1 table with a blob value.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param bindArgs You may include ? in where clause in the query, which will
     * be replaced by the values from <em>bindArgs</em>. If no arguments, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @return The {@link ByteArrayBuffer} of the blob value, or <tt>null</tt> if
     * the value is <tt>null</tt> or could not be read for some reason.
     * @see #simpleQueryBlob(SQLiteDatabase, OutputStream, String, Object[])
     * @see #simpleQueryBlob(ContentResolver, Uri, String, String, String[])
     */
    public static ByteArrayBuffer simpleQueryBlob(SQLiteDatabase db, String sql, Object... bindArgs) {
        try {
            final ByteArrayBuffer result = new ByteArrayBuffer();
            simpleQueryBlob(db, result, sql, bindArgs);
            return result;
        } catch (Exception e) {
            Log.e(DatabaseUtils.class.getName(), new StringBuilder("Couldn't query blob - ").append(sql).toString(), e);
            return null;
        }
    }

    /**
     * Executes a statement that returns a 1 by 1 table with a blob value.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param out The <tt>OutputStream</tt> to store the blob value.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param bindArgs You may include ? in where clause in the query, which will
     * be replaced by the values from <em>bindArgs</em>. If no arguments, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @return <tt>true</tt> if query successful, <tt>false</tt> otherwise.
     * @throws IOException if an error occurs while writing to <em>out</em>.
     * @see #simpleQueryBlob(SQLiteDatabase, String, Object[])
     * @see #simpleQueryBlob(ContentResolver, Uri, String, String, String[])
     */
    public static void simpleQueryBlob(SQLiteDatabase db, OutputStream out, String sql, Object... bindArgs) throws IOException {
        final InputStream is = simpleQuery(db, sql, bindArgs);
        try {
            FileUtils.copyStream(is, out, null);
        } finally {
            is.close();
        }
    }

    /**
     * Query the given <em>uri</em>, return a 1 by 1 table with a blob value.
     * @param resolver The <tt>ContentResolver</tt>.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param column The column to query.
     * @param selection A filter declaring which row to return, formatted as an SQL
     * WHERE clause (excluding the WHERE itself).
     * @param selectionArgs You may include ? in selection, which will be replaced
     * by the values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @return The byte array of the blob value, or <tt>null</tt> if the value is
     * <tt>null</tt> or could not be read for some reason.
     * @see #simpleQueryBlob(SQLiteDatabase, String, Object[])
     * @see #simpleQueryBlob(SQLiteDatabase, OutputStream, String, Object[])
     */
    public static byte[] simpleQueryBlob(ContentResolver resolver, Uri uri, String column, String selection, String[] selectionArgs) {
        final Object result = simpleQuery(resolver, uri, column, selection, selectionArgs);
        return (result instanceof byte[] ? (byte[])result : null);
    }

    /**
     * Query the given SQL statement, returning a new array with the specified <em>componentType</em>.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param componentType The any can be deserialized <tt>Class</tt> of the array elements. See
     * {@link CursorField}.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param selectionArgs You may include ? in where clause in the query, which will be replaced by
     * the values from <em>selectionArgs</em>. The values will be bound as Strings. If no arguments,
     * you can pass <em>(String[])null</em> instead of allocating an empty array.
     * @return A new array, or <tt>null</tt>.
     * @see #query(ContentResolver, Class, Uri, String[], String, String[], String)
     */
    public static <T> T query(SQLiteDatabase db, Class<?> componentType, String sql, String... selectionArgs) {
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(sql, selectionArgs);
            return (cursor != null ? DatabaseUtils.<T>newArray(cursor, componentType) : null);
        } catch (Exception e) {
            Log.e(DatabaseUtils.class.getName(), new StringBuilder("Couldn't query - ").append(sql).toString(), e);
            return null;
        } finally {
            FileUtils.close(cursor);
        }
    }

    /**
     * Query the given URI, returning a new array with the specified <em>componentType</em>.
     * @param resolver The <tt>ContentResolver</tt>.
     * @param componentType The any can be deserialized <tt>Class</tt> of the array elements.
     * See {@link CursorField}.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param projection A list of which columns to return. Passing <tt>null</tt> will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause
     * (excluding the WHERE itself). Passing <tt>null</tt> will return all rows for the given table.
     * @param selectionArgs You may include ? in where clause in the query, which will be replaced by the
     * values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself).
     * Passing <tt>null</tt> will use the default sort order, which may be unordered.
     * @return A new array, or <tt>null</tt>.
     * @see #query(SQLiteDatabase, Class, String, String[])
     */
    public static <T> T query(ContentResolver resolver, Class<?> componentType, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, projection, selection, selectionArgs, sortOrder);
            return (cursor != null ? DatabaseUtils.<T>newArray(cursor, componentType) : null);
        } catch (Exception e) {
            Log.e(DatabaseUtils.class.getName(), new StringBuilder("Couldn't query uri - ").append(uri).toString(), e);
            return null;
        } finally {
            FileUtils.close(cursor);
        }
    }

    /**
     * Executes the SQL statement and return the ID of the row inserted. The SQL statement
     * should be an INSERT for this to be a useful call.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param sql The INSERT SQL statement, may contain ? for unknown values to be bound.
     * @param bindArgs You may include ? in where clause in the insert, which will
     * be replaced by the values from <em>bindArgs</em>. If no arguments, you can pass
     * <em>(Object[])null</em> instead of allocating an empty array.
     * @return The row ID of inserted, if this insert is successful. <tt>-1</tt> otherwise.
     * @throws SQLException If the SQL string is invalid for some reason.
     * @see #executeUpdateDelete(SQLiteDatabase, String, Object[])
     */
    public static long executeInsert(SQLiteDatabase db, String sql, Object... bindArgs) {
        final SQLiteStatement prog = db.compileStatement(sql);
        try {
            bindArgs(prog, bindArgs);
            return prog.executeInsert();
        } finally {
            prog.close();
        }
    }

    /**
     * Executes the SQL statement and return the number of rows affected by execution. The SQL
     * statement should be a UPDATE / DELETE for this to be a useful call.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param sql The UPDATE / DELETE SQL statement, may contain ? for unknown values to be bound.
     * @param bindArgs You may include ? in where clause in the update / delete, which will be
     * replaced by the values from <em>bindArgs</em>. If no arguments, you can pass
     * <em>(Object[])null</em> instead of allocating an empty array.
     * @return The number of rows affected by the SQL statement execution.
     * @throws SQLException If the SQL string is invalid for some reason.
     * @see #executeInsert(SQLiteDatabase, String, Object[])
     */
    public static int executeUpdateDelete(SQLiteDatabase db, String sql, Object... bindArgs) {
        final SQLiteStatement prog = db.compileStatement(sql);
        try {
            bindArgs(prog, bindArgs);
            return prog.executeUpdateDelete();
        } finally {
            prog.close();
        }
    }

    /**
     * Binds the given Object array to the given <tt>SQLiteProgram</tt>.
     * If the <em>bindArgs</em> is <tt>null</tt> or <tt>0-length</tt>
     * then invoking this method has no effect.
     * @param prog The <tt>SQLiteProgram</tt> to bind.
     * @param bindArgs The Object array to bind.
     */
    public static void bindArgs(SQLiteProgram prog, Object... bindArgs) {
        if (bindArgs == null) {
            return;
        }

        for (int i = 1; i <= bindArgs.length; ++i) {
            final Object arg = bindArgs[i - 1];
            if (arg == null) {
                prog.bindNull(i);
            } else if (arg instanceof Float || arg instanceof Double) {
                prog.bindDouble(i, ((Number)arg).doubleValue());
            } else if (arg instanceof Number) {
                prog.bindLong(i, ((Number)arg).longValue());
            } else if (arg instanceof byte[]) {
                prog.bindBlob(i, (byte[])arg);
            } else if (arg instanceof Boolean) {
                prog.bindLong(i, ((Boolean)arg) ? 1 : 0);
            } else {
                prog.bindString(i, arg.toString());
            }
        }
    }

    /**
     * Returns the number of rows in the specified table.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param table The name of the table to query.
     * @return The number of rows in the table.
     * @see #getRowCount(ContentResolver, Uri)
     * @see #getRowCount(ContentResolver, Uri, String, String[])
     */
    public static int getRowCount(SQLiteDatabase db, String table) {
        return simpleQueryLong(db, "SELECT COUNT(*) FROM " + table, (Object[])null).intValue();
    }

    /**
     * Returns the number of rows with given the <em>uri</em>.
     * @param resolver The <tt>ContentResolver</tt>.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @return The number of rows.
     * @see #getRowCount(SQLiteDatabase, String)
     * @see #getRowCount(ContentResolver, Uri, String, String[])
     */
    public static int getRowCount(ContentResolver resolver, Uri uri) {
        return ((Number)simpleQuery(resolver, uri, "COUNT(*)", null, null)).intValue();
    }

    /**
     * Returns the number of rows with given the <em>uri</em>.
     * @param resolver The <tt>ContentResolver</tt>.
     * @param uri The URI, using the content:// scheme, for the content to retrieve.
     * @param selection The SQL WHERE clause (excluding the WHERE itself).
     * @param selectionArgs You may include ? in where clause in the query, which will be
     * replaced by the values from <em>selectionArgs</em>. The values will be bound as Strings.
     * @return The number of rows.
     * @see #getRowCount(ContentResolver, Uri)
     * @see #getRowCount(SQLiteDatabase, String)
     */
    public static int getRowCount(ContentResolver resolver, Uri uri, String selection, String[] selectionArgs) {
        return ((Number)simpleQuery(resolver, uri, "COUNT(*)", selection, selectionArgs)).intValue();
    }

    /**
     * Returns all table names in the specified database.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @return A array of all table names if succeeded,
     * <tt>null</tt> otherwise.
     * @see #getTableCount(SQLiteDatabase)
     * @see #exists(SQLiteDatabase, String)
     */
    public static String[] getTables(SQLiteDatabase db) {
        return query(db, String.class, "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name", (String[])null);
    }

    /**
     * Returns the number of tables in the specified database.
     * @param db The <tt>SQLiteDatabase</tt> to query.
     * @return The number of tables.
     * @see #getTables(SQLiteDatabase)
     * @see #exists(SQLiteDatabase, String)
     */
    public static int getTableCount(SQLiteDatabase db) {
        return simpleQueryLong(db, "SELECT COUNT(*) FROM sqlite_master WHERE type='table'", (Object[])null).intValue();
    }

    /**
     * Returns a boolean indicating whether the specified table can be found in the database.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param tableName The table name to find.
     * @return <tt>true</tt> if the table exists, <tt>false</tt> otherwise.
     * @see #getTables(SQLiteDatabase)
     * @see #getTableCount(SQLiteDatabase)
     */
    public static boolean exists(SQLiteDatabase db, String tableName) {
        return (simpleQueryLong(db, "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name=?", tableName) > 0);
    }

    /**
     * Returns a new instance with the specified <em>cursor</em> and <em>clazz</em>.
     * @param cursor The {@link Cursor} from which to get the data. The cursor must
     * be move to the correct position.
     * @param clazz The any can be deserialized <tt>Class</tt>. See {@link CursorField}.
     * @return A new instance.
     * @throws ReflectiveOperationException if the instance cannot be created.
     * @see #newList(Cursor, Class)
     * @see #newArray(Cursor, Class)
     */
    public static <T> T newInstance(Cursor cursor, Class<? extends T> clazz) throws ReflectiveOperationException {
        return newInstanceImpl(cursor, clazz, getDeclaredFields(clazz));
    }

    /**
     * Returns a new array with the specified <em>cursor</em> and <em>componentType</em>.
     * Equivalent to <tt>new componentType[cursor.getCount()]</tt>
     * @param cursor The {@link Cursor} from which to get the data.
     * @param componentType The any can be deserialized <tt>Class</tt> of the array elements.
     * See {@link CursorField}.
     * @return A new array.
     * @throws ReflectiveOperationException if the array cannot be created.
     * @see #newList(Cursor, Class)
     * @see #newInstance(Cursor, Class)
     */
    @SuppressWarnings("unchecked")
    public static <T> T newArray(Cursor cursor, Class<?> componentType) throws ReflectiveOperationException {
        final Object result;
        cursor.moveToPosition(-1);
        if (componentType == String.class) {
            result = createStringArray(cursor);
        } else if (!componentType.isPrimitive()) {
            result = createObjectArray(cursor, componentType);
        } else if (componentType == int.class) {
            result = createIntArray(cursor);
        } else if (componentType == long.class) {
            result = createLongArray(cursor);
        } else if (componentType == float.class) {
            result = createFloatArray(cursor);
        } else if (componentType == double.class) {
            result = createDoubleArray(cursor);
        } else if (componentType == boolean.class) {
            result = createBooleanArray(cursor);
        } else {
            throw new Error("Unsupported component type - " + componentType.toString());
        }

        return (T)result;
    }

    /**
     * Returns an immutable <tt>List</tt> with the specified <em>cursor</em> and <em>componentType</em>.
     * @param cursor The {@link Cursor} from which to get the data.
     * @param componentType The any can be deserialized <tt>Class</tt> of the array elements.
     * See {@link CursorField}.
     * @return An immutable <tt>List</tt>.
     * @see #newArray(Cursor, Class)
     * @see #newInstance(Cursor, Class)
     */
    public static <T> List<T> newList(Cursor cursor, Class<T> componentType) {
        try {
            DebugUtils.__checkError(componentType.isPrimitive(), "Unsupported primitive type - " + componentType.toString());
            return Arrays.asList(DatabaseUtils.<T[]>newArray(cursor, componentType));
        } catch (ReflectiveOperationException e) {
            return Collections.emptyList();
        }
    }

    /**
     * Writes the specified <tt>Cursor</tt> contents into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param cursor The {@link Cursor} from which to get the data.
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeCursor(JsonWriter, Cursor, String[])
     * @see #writeCursorRow(JsonWriter, Cursor, int[], String[])
     */
    public static JsonWriter writeCursor(JsonWriter writer, Cursor cursor) throws IOException {
        return writeCursor(writer, cursor, cursor.getColumnNames());
    }

    /**
     * Writes the specified <tt>Cursor</tt> contents into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param cursor The {@link Cursor} from which to get the data.
     * @param columnNames The name of the columns which the values to write.
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeCursor(JsonWriter, Cursor)
     * @see #writeCursorRow(JsonWriter, Cursor, int[], String[])
     */
    public static JsonWriter writeCursor(JsonWriter writer, Cursor cursor, String... columnNames) throws IOException {
        writer.beginArray();
        if (cursor.getCount() > 0) {
            // Gets the column indexes from column names.
            final int[] columnIndexes = new int[columnNames.length];
            for (int i = 0; i < columnNames.length; ++i) {
                columnIndexes[i] = cursor.getColumnIndexOrThrow(columnNames[i]);
            }

            // Writes the cursor contents into writer.
            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                writeCursorRow(writer, cursor, columnIndexes, columnNames);
            }
        }

        return writer.endArray();
    }

    /**
     * Writes the specified <tt>Cursor</tt> current row into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param cursor The {@link Cursor} from which to get the data. The cursor
     * must be move to the correct position.
     * @param columnIndexes The index of the columns which the values to write.
     * @param names The name of the properties which the names to write. The
     * <em>names</em> length must be equals the <em>columnIndexes</em> length.
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeCursor(JsonWriter, Cursor)
     * @see #writeCursor(JsonWriter, Cursor, String[])
     */
    public static JsonWriter writeCursorRow(JsonWriter writer, Cursor cursor, int[] columnIndexes, String[] names) throws IOException {
        DebugUtils.__checkError(columnIndexes.length != names.length, "columnIndexes.length != names.length");
        writer.beginObject();
        for (int i = 0; i < columnIndexes.length; ++i) {
            final String name = names[i];
            final int columnIndex = columnIndexes[i];
            switch (cursor.getType(columnIndex)) {
            case Cursor.FIELD_TYPE_INTEGER:
                writer.name(name).value(cursor.getLong(columnIndex));
                break;

            case Cursor.FIELD_TYPE_FLOAT:
                writer.name(name).value(cursor.getDouble(columnIndex));
                break;

            case Cursor.FIELD_TYPE_STRING:
                writer.name(name).value(cursor.getString(columnIndex));
                break;

            case Cursor.FIELD_TYPE_BLOB:
                DebugUtils.__checkError(true, "Unsupported column type - BLOB");
                break;
            }
        }

        return writer.endObject();
    }

    private static List<Field> getDeclaredFields(Class<?> clazz) {
        final List<Field> result = new ArrayList<Field>();
        for (; clazz != Object.class && clazz != null; clazz = clazz.getSuperclass()) {
            final Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                if (field.getAnnotation(CursorField.class) != null) {
                    result.add(field);
                    field.setAccessible(true);
                }
            }
        }

        return result;
    }

    private static <T> T newInstanceImpl(Cursor cursor, Class<? extends T> clazz, List<Field> fields) throws ReflectiveOperationException {
        DebugUtils.__checkError(clazz == Object.class || clazz == Void.class || clazz == String.class || (clazz.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) != 0, "Unsupported class type - " + clazz.toString());
        final T result = clazz.newInstance();
        for (int i = 0, size = fields.size(); i < size; ++i) {
            final Field field = fields.get(i);
            final int columnIndex = cursor.getColumnIndexOrThrow(field.getAnnotation(CursorField.class).value());
            final Class<?> type = field.getType();
            if (type == int.class) {
                field.setInt(result, cursor.getInt(columnIndex));
            } else if (type == long.class) {
                field.setLong(result, cursor.getLong(columnIndex));
            } else if (type == String.class) {
                field.set(result, cursor.getString(columnIndex));
            } else if (type == byte[].class) {
                field.set(result, cursor.getBlob(columnIndex));
            } else if (type == float.class) {
                field.setFloat(result, cursor.getFloat(columnIndex));
            } else if (type == double.class) {
                field.setDouble(result, cursor.getDouble(columnIndex));
            } else if (type == boolean.class) {
                field.setBoolean(result, cursor.getInt(columnIndex) != 0);
            } else {
                throw new Error("Unsupported field type - " + type.toString());
            }
        }

        return result;
    }

    private static int[] createIntArray(Cursor cursor) {
        final int[] result = new int[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); ++i) {
            result[i] = cursor.getInt(0);
        }

        return result;
    }

    private static long[] createLongArray(Cursor cursor) {
        final long[] result = new long[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); ++i) {
            result[i] = cursor.getLong(0);
        }

        return result;
    }

    private static float[] createFloatArray(Cursor cursor) {
        final float[] result = new float[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); ++i) {
            result[i] = cursor.getFloat(0);
        }

        return result;
    }

    private static double[] createDoubleArray(Cursor cursor) {
        final double[] result = new double[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); ++i) {
            result[i] = cursor.getDouble(0);
        }

        return result;
    }

    private static String[] createStringArray(Cursor cursor) {
        final String[] result = new String[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); ++i) {
            result[i] = cursor.getString(0);
        }

        return result;
    }

    private static boolean[] createBooleanArray(Cursor cursor) {
        final boolean[] result = new boolean[cursor.getCount()];
        for (int i = 0; cursor.moveToNext(); ++i) {
            result[i] = (cursor.getInt(0) != 0);
        }

        return result;
    }

    private static Object[] createObjectArray(Cursor cursor, Class<?> componentType) throws ReflectiveOperationException {
        final int count = cursor.getCount();
        final Object[] result = (Object[])Array.newInstance(componentType, count);
        if (count > 0) {
            final List<Field> fields = getDeclaredFields(componentType);
            for (int i = 0; cursor.moveToNext(); ++i) {
                result[i] = newInstanceImpl(cursor, componentType, fields);
            }
        }

        return result;
    }

    private static Object simpleQuery(ContentResolver resolver, Uri uri, String column, String selection, String[] selectionArgs) {
        Object result = null;
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, new String[] { column }, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                switch (cursor.getType(0)) {
                case Cursor.FIELD_TYPE_INTEGER:
                    result = cursor.getLong(0);
                    break;

                case Cursor.FIELD_TYPE_STRING:
                    result = cursor.getString(0);
                    break;

                case Cursor.FIELD_TYPE_BLOB:
                    result = cursor.getBlob(0);
                    break;
                }
            }
        } catch (Exception e) {
            Log.e(DatabaseUtils.class.getName(), new StringBuilder("Couldn't query uri - ").append(uri).toString(), e);
        } finally {
            FileUtils.close(cursor);
        }

        return result;
    }

    /**
     * This utility class cannot be instantiated.
     */
    private DatabaseUtils() {
    }
}
