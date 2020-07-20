package android.ext.database;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteProgram;
import android.database.sqlite.SQLiteStatement;
import android.ext.annotation.CursorField;
import android.ext.json.JSONArray;
import android.ext.json.JSONObject;
import android.ext.util.ArrayUtils;
import android.ext.util.ByteArrayBuffer;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.ReflectUtils;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.util.JsonWriter;
import android.util.Log;
import android.util.LogPrinter;
import android.util.Pair;
import android.util.Printer;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Class DatabaseUtils
 * @author Garfield
 */
public final class DatabaseUtils {
    /**
     * Returns the number of rows in the <tt>Cursor</tt>,
     * handling <tt>null Cursor</tt>.
     * @param cursor The <tt>Cursor</tt>.
     * @return The number of rows in the <tt>Cursor</tt>.
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
        try (final SQLiteStatement prog = db.compileStatement(sql)) {
            bindArgs(prog, bindArgs);
            return prog.simpleQueryForLong();
        } catch (SQLiteException e) {
            DebugUtils.__checkLogError(true, DatabaseUtils.class.getName(), "Couldn't query - " + sql, e);
            return null;
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
        try (final SQLiteStatement prog = db.compileStatement(sql)) {
            bindArgs(prog, bindArgs);
            return prog.simpleQueryForString();
        } catch (SQLiteException e) {
            DebugUtils.__checkLogError(true, DatabaseUtils.class.getName(), "Couldn't query - " + sql, e);
            return null;
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
     * @return An {@link InputStream} for a copy of the blob value, or <tt>null</tt>
     * if the value is <tt>null</tt> or could not be read for some reason.
     * @see #simpleQueryBlob(SQLiteDatabase, String, Object[])
     * @see #simpleQueryBlob(ContentResolver, Uri, String, String, String[])
     */
    public static InputStream simpleQuery(SQLiteDatabase db, String sql, Object... bindArgs) {
        InputStream result = null;
        try (final SQLiteStatement prog = db.compileStatement(sql)) {
            bindArgs(prog, bindArgs);
            final ParcelFileDescriptor fd = prog.simpleQueryForBlobFileDescriptor();
            if (fd != null) {
                // Don't close the fd, The AutoCloseInputStream take care of close it.
                result = new AutoCloseInputStream(fd);
            }
        } catch (SQLiteException e) {
            Log.e(DatabaseUtils.class.getName(), "Couldn't query - " + sql, e);
        }

        return result;
    }

    /**
     * Executes a statement that returns a 1 by 1 table with a blob value.
     * @param db The <tt>SQLiteDatabase</tt>.
     * @param sql The SQL query. The SQL string must not be <tt>;</tt> terminated.
     * @param bindArgs You may include ? in where clause in the query, which will
     * be replaced by the values from <em>bindArgs</em>. If no arguments, you can
     * pass <em>(Object[])null</em> instead of allocating an empty array.
     * @return An {@link ByteArrayBuffer} of the blob value, or <tt>null</tt> if
     * the value is <tt>null</tt> or could not be read for some reason.
     * @see #simpleQuery(SQLiteDatabase, String, Object[])
     * @see #simpleQueryBlob(ContentResolver, Uri, String, String, String[])
     */
    public static ByteArrayBuffer simpleQueryBlob(SQLiteDatabase db, String sql, Object... bindArgs) {
        ByteArrayBuffer result = null;
        try (final InputStream is = simpleQuery(db, sql, bindArgs)) {
            if (is != null) {
                FileUtils.copyStream(is, result = new ByteArrayBuffer(), null, null);
            }
        } catch (Exception e) {
            Log.e(DatabaseUtils.class.getName(), "Couldn't query - " + sql, e);
        }

        return result;
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
     * @see #simpleQuery(SQLiteDatabase, String, Object[])
     * @see #simpleQueryBlob(SQLiteDatabase, String, Object[])
     */
    public static byte[] simpleQueryBlob(ContentResolver resolver, Uri uri, String column, String selection, String[] selectionArgs) {
        final Object result = simpleQuery(resolver, uri, column, selection, selectionArgs);
        return (result instanceof byte[] ? (byte[])result : null);
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
     * @throws SQLException if the SQL string is invalid for some reason.
     * @see #executeUpdateDelete(SQLiteDatabase, String, Object[])
     */
    public static long executeInsert(SQLiteDatabase db, String sql, Object... bindArgs) {
        try (final SQLiteStatement prog = db.compileStatement(sql)) {
            bindArgs(prog, bindArgs);
            return prog.executeInsert();
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
     * @throws SQLException if the SQL string is invalid for some reason.
     * @see #executeInsert(SQLiteDatabase, String, Object[])
     */
    public static int executeUpdateDelete(SQLiteDatabase db, String sql, Object... bindArgs) {
        try (final SQLiteStatement prog = db.compileStatement(sql)) {
            bindArgs(prog, bindArgs);
            return prog.executeUpdateDelete();
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
        for (int i = 1, size = ArrayUtils.getSize(bindArgs); i <= size; ++i) {
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
     * Parses a <tt>Cursor</tt>'s data to a <tt>List</tt> from the specified <em>cursor</em>.
     * @param cursor The {@link Cursor} from which to get the data.
     * @param componentType A <tt>Class</tt> can be deserialized of the list elements. See {@link CursorField}.
     * @return A new <tt>List</tt>.
     * @throws ReflectiveOperationException if the elements cannot be created.
     */
    public static <T> List<T> parse(Cursor cursor, Class<? extends T> componentType) throws ReflectiveOperationException {
        DebugUtils.__checkError(cursor == null || componentType == null, "Invalid parameters - cursor == null || componentType == null");
        DebugUtils.__checkError(componentType.isPrimitive() || componentType.getName().startsWith("java.lang") || (componentType.getModifiers() & (Modifier.ABSTRACT | Modifier.INTERFACE)) != 0, "Unsupported component type - " + componentType.getName());
        final int count = cursor.getCount();
        final List<T> result = new ArrayList<T>(count);
        if (count > 0) {
            cursor.moveToPosition(-1);
            final List<Pair<Field, String>> cursorFields = getCursorFields(componentType);
            final Constructor<? extends T> ctor = ReflectUtils.getConstructor(componentType, (Class<?>[])null);
            while (cursor.moveToNext()) {
                final T object = ctor.newInstance((Object[])null);
                setCursorFields(cursor, object, cursorFields);
                result.add(object);
            }
        }

        return result;
    }

    /**
     * Converts the specified <em>cursor's</em> data to a {@link JSONArray}.
     * @param cursor The {@link Cursor} from which to get the data.
     * @param columnNames The name of the columns which the values to write.
     * @return The <tt>JSONArray</tt>.
     * @see #toJSONObject(Cursor, String[], int[])
     */
    public static JSONArray toJSONArray(Cursor cursor, String... columnNames) {
        DebugUtils.__checkError(cursor == null || columnNames == null, "Invalid parameters - cursor == null || columnNames == null");
        final JSONArray result = new JSONArray();
        if (cursor.getCount() > 0) {
            // Gets the column indexes from column names.
            final int[] columnIndexes = new int[columnNames.length];
            for (int i = 0; i < columnNames.length; ++i) {
                columnIndexes[i] = cursor.getColumnIndexOrThrow(columnNames[i]);
            }

            cursor.moveToPosition(-1);
            while (cursor.moveToNext()) {
                result.add(toJSONObject(cursor, columnNames, columnIndexes));
            }
        }

        return result;
    }

    /**
     * Converts the specified <em>cursor</em> current row to a {@link JSONObject}.
     * @param cursor The {@link Cursor} from which to get the data. The cursor
     * must be move to the correct position.
     * @param names The name of the properties to write.
     * @param columnIndexes The index of the columns which the values to write.
     * The <em>columnIndexes</em> length must be equals the <em>names</em> length.
     * @return The <tt>JSONObject</tt>.
     * @see #toJSONArray(Cursor, String[])
     */
    public static JSONObject toJSONObject(Cursor cursor, String[] names, int... columnIndexes) {
        DebugUtils.__checkError(cursor == null || columnIndexes == null || names == null, "Invalid parameters - cursor == null || columnIndexes == null || names == null");
        DebugUtils.__checkError(columnIndexes.length != names.length, "Invalid parameters - columnIndexes.length(" + columnIndexes.length + ") != names.length(" + names.length + ")");
        final JSONObject result = new JSONObject();
        for (int i = 0; i < columnIndexes.length; ++i) {
            final int columnIndex = columnIndexes[i];
            final String name = names[i];
            switch (cursor.getType(columnIndex)) {
            case Cursor.FIELD_TYPE_INTEGER:
                result.put(name, cursor.getLong(columnIndex));
                break;

            case Cursor.FIELD_TYPE_FLOAT:
                result.put(name, cursor.getDouble(columnIndex));
                break;

            case Cursor.FIELD_TYPE_STRING:
                result.put(name, cursor.getString(columnIndex));
                break;

            case Cursor.FIELD_TYPE_BLOB:
                result.put(name, toBigInteger(cursor.getBlob(columnIndex)));
                break;
            }
        }

        return result;
    }

    /**
     * Writes the specified <em>cursor's</em> data into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param cursor The {@link Cursor} from which to get the data.
     * @param columnNames The name of the columns which the values to write.
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeCursorRow(JsonWriter, Cursor, String[], int[])
     */
    public static JsonWriter writeCursor(JsonWriter writer, Cursor cursor, String... columnNames) throws IOException {
        DebugUtils.__checkError(cursor == null || columnNames == null, "Invalid parameters - cursor == null || columnNames == null");
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
                writeCursorRow(writer, cursor, columnNames, columnIndexes);
            }
        }

        return writer.endArray();
    }

    /**
     * Writes the specified <em>cursor</em> current row into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param cursor The {@link Cursor} from which to get the data. The cursor
     * must be move to the correct position.
     * @param names The name of the properties which the names to write.
     * @param columnIndexes The index of the columns which the values to write.
     * The <em>columnIndexes</em> length must be equals the <em>names</em> length.
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeCursor(JsonWriter, Cursor, String[])
     */
    public static JsonWriter writeCursorRow(JsonWriter writer, Cursor cursor, String[] names, int... columnIndexes) throws IOException {
        DebugUtils.__checkError(cursor == null || columnIndexes == null || names == null, "Invalid parameters - cursor == null || columnIndexes == null || names == null");
        DebugUtils.__checkError(columnIndexes.length != names.length, "Invalid parameters - columnIndexes.length(" + columnIndexes.length + ") != names.length(" + names.length + ")");
        writer.beginObject();
        for (int i = 0; i < columnIndexes.length; ++i) {
            final int columnIndex = columnIndexes[i];
            final String name = names[i];
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
                writer.name(name).value(toString(cursor.getBlob(columnIndex)));
                break;
            }
        }

        return writer.endObject();
    }

    private static BigInteger toBigInteger(byte[] blob) {
        return (ArrayUtils.getSize(blob) > 0 ? new BigInteger(blob) : null);
    }

    private static String toString(byte[] blob) {
        return (ArrayUtils.getSize(blob) > 0 ? new BigInteger(blob).toString() : null);
    }

    private static List<Pair<Field, String>> getCursorFields(Class<?> clazz) {
        final List<Pair<Field, String>> cursorFields = new ArrayList<Pair<Field, String>>();
        for (Class<?> kclass = clazz; kclass != Object.class; kclass = kclass.getSuperclass()) {
            final Field[] fields = kclass.getDeclaredFields();
            for (int i = 0; i < fields.length; ++i) {
                final Field field = fields[i];
                final CursorField cursorField = field.getAnnotation(CursorField.class);
                if (cursorField != null) {
                    DebugUtils.__checkError((field.getModifiers() & (Modifier.FINAL | Modifier.STATIC)) != 0, "Unsupported static or final field - " + field.toString());
                    field.setAccessible(true);
                    cursorFields.add(new Pair<Field, String>(field, cursorField.value()));
                }
            }
        }

        DatabaseUtils.__checkDumpCursorFields(clazz, cursorFields);
        return cursorFields;
    }

    private static void setCursorFields(Cursor cursor, Object object, List<Pair<Field, String>> cursorFields) throws ReflectiveOperationException {
        for (int i = 0, size = cursorFields.size(); i < size; ++i) {
            final Pair<Field, String> cursorField = cursorFields.get(i);
            final Field field = cursorField.first;
            final int columnIndex = cursor.getColumnIndexOrThrow(cursorField.second);
            final Class<?> type = field.getType();
            if (type == int.class) {
                field.setInt(object, cursor.getInt(columnIndex));
            } else if (type == long.class) {
                field.setLong(object, cursor.getLong(columnIndex));
            } else if (type == String.class) {
                field.set(object, cursor.getString(columnIndex));
            } else if (type == byte[].class) {
                field.set(object, cursor.getBlob(columnIndex));
            } else if (type == float.class) {
                field.setFloat(object, cursor.getFloat(columnIndex));
            } else if (type == short.class) {
                field.setShort(object, cursor.getShort(columnIndex));
            } else if (type == double.class) {
                field.setDouble(object, cursor.getDouble(columnIndex));
            } else if (type == boolean.class) {
                field.setBoolean(object, cursor.getInt(columnIndex) != 0);
            } else {
                throw new AssertionError("Unsupported field type - " + type.getName());
            }
        }
    }

    private static Object simpleQuery(ContentResolver resolver, Uri uri, String column, String selection, String[] selectionArgs) {
        Object result = null;
        try (final Cursor cursor = resolver.query(uri, new String[] { column }, selection, selectionArgs, null)) {
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
            DebugUtils.__checkLogError(true, DatabaseUtils.class.getName(), "Couldn't query - " + uri, e);
        }

        return result;
    }

    private static void __checkDumpCursorFields(Class<?> clazz, List<Pair<Field, String>> cursorFields) {
        final Printer printer = new LogPrinter(Log.DEBUG, "DatabaseUtils");
        final StringBuilder result = new StringBuilder(100);
        DebugUtils.dumpSummary(printer, result, 100, " Dumping %s cursor fields [ size = %d ] ", clazz.getName(), cursorFields.size());
        for (Pair<Field, String> cursorField : cursorFields) {
            final String modifier = Modifier.toString(cursorField.first.getModifiers());
            result.setLength(0);
            result.append("  ");
            if (modifier.length() != 0) {
                result.append(modifier).append(' ');
            }

            printer.println(result.append(cursorField.first.getType().getSimpleName())
                   .append(' ').append(cursorField.first.getName())
                   .append(" { columnName = ").append(cursorField.second).append(" }").toString());
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private DatabaseUtils() {
    }
}
