package android.ext.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.ext.util.ArrayUtils.Filter;
import android.util.JsonReader;
import android.util.JsonWriter;

/**
 * Class JSONUtils
 * @author Garfield
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public final class JSONUtils {
    /**
     * Returns a <tt>0-length</tt>, immutable {@link JSONArray}.
     * @return An empty {@link JSONArray}.
     */
    public static JSONArray emptyArray() {
        return EmptyJSONArray.sInstance;
    }

    /**
     * Returns a <tt>0-length</tt>, immutable {@link JSONObject}.
     * @return An empty {@link JSONObject}.
     */
    public static JSONObject emptyObject() {
        return EmptyJSONObject.sInstance;
    }

    /**
     * Returns the number of values in the <em>array</em>,
     * handling <tt>null array</tt>.
     * @param array The <tt>JSONArray</tt>.
     * @return The number of values.
     * @see #getSize(JSONObject)
     */
    public static int getSize(JSONArray array) {
        return (array != null ? array.length() : 0);
    }

    /**
     * Returns the number of name/value mappings in the
     * <em>object</em>, handling <tt>null object</tt>.
     * @param object The <tt>JSONObject</tt>.
     * @return The number of name/value mappings.
     * @see #getSize(JSONArray)
     */
    public static int getSize(JSONObject object) {
        return (object != null ? object.length() : 0);
    }

    /**
     * Equivalent to calling {@link JSONArray#put(int, Object)}.
     * @param array The <tt>JSONArray</tt> to add to.
     * @param index The index at which to put.
     * @param value A <tt>JSONObject, JSONArray, String, Boolean,
     * Number</tt>, {@link JSONObject#NULL}, or <tt>null</tt>.
     * @return The <em>array</em>.
     * @see #putOpt(JSONObject, String, Object)
     */
    public static JSONArray putOpt(JSONArray array, int index, Object value) {
        try {
            return array.put(index, value);
        } catch (JSONException e) {
            return array;
        }
    }

    /**
     * Equivalent to calling {@link JSONObject#putOpt(String, Object)}.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @param value A <tt>JSONObject, JSONArray, String, Boolean, Number</tt>,
     * {@link JSONObject#NULL}, or <tt>null</tt>.
     * @return The <em>object</em>.
     * @see #putOpt(JSONArray, int, Object)
     */
    public static JSONObject putOpt(JSONObject object, String name, Object value) {
        try {
            return object.putOpt(name, value);
        } catch (JSONException e) {
            return object;
        }
    }

    /**
     * Equivalent to calling <tt>indexOf(array, 0, array.length(), filter)</tt>.
     * @param array The <tt>JSONArray</tt> to search.
     * @param filter The {@link Filter} using to search.
     * @return The index of the first occurrence of the element, or <tt>-1</tt>
     * if it was not found.
     * @see #indexOf(JSONArray, int, int, Filter)
     */
    public static <T> int indexOf(JSONArray array, Filter<? super T> filter) {
        return indexOf(array, 0, array.length(), filter);
    }

    /**
     * Searches the specified {@link JSONArray} for the first occurrence of the element using the
     * specified <tt>filter</tt>.
     * @param array The <tt>JSONArray</tt> to search.
     * @param start The inclusive start index in <em>array</em>.
     * @param end The exclusive end index in <em>array</em>.
     * @param filter The {@link Filter} using to search.
     * @return The index of the first occurrence of the element, or <tt>-1</tt> if it was not found.
     * @see #indexOf(JSONArray, Filter)
     */
    public static <T> int indexOf(JSONArray array, int start, int end, Filter<? super T> filter) {
        DebugUtils.__checkRange(start, end - start, array.length());
        for (; start < end; ++start) {
            if (filter.accept((T)array.opt(start))) {
                return start;
            }
        }

        return -1;
    }

    /**
     * Equivalent to calling <tt>lastIndexOf(array, 0, array.length(), filter)</tt>.
     * @param array The <tt>JSONArray</tt> to search.
     * @param filter The {@link Filter} using to search.
     * @return The index of the last occurrence of the element, or <tt>-1</tt> if it
     * was not found.
     * @see #lastIndexOf(JSONArray, int, int, Filter)
     */
    public static <T> int lastIndexOf(JSONArray array, Filter<? super T> filter) {
        return lastIndexOf(array, 0, array.length(), filter);
    }

    /**
     * Searches the specified {@link JSONArray} for the last occurrence of the element using the
     * specified <tt>filter</tt>.
     * @param array The <tt>JSONArray</tt> to search.
     * @param start The inclusive start index in <em>array</em>.
     * @param end The exclusive end index in <em>array</em>.
     * @param filter The {@link Filter} using to search.
     * @return The index of the last occurrence of the element, or <tt>-1</tt> if it was not found.
     * @see #lastIndexOf(JSONArray, Filter)
     */
    public static <T> int lastIndexOf(JSONArray array, int start, int end, Filter<? super T> filter) {
        DebugUtils.__checkRange(start, end - start, array.length());
        for (--end; end >= start; --end) {
            if (filter.accept((T)array.opt(end))) {
                return end;
            }
        }

        return -1;
    }

    /**
     * Returns a new instance parsed from the specified <em>reader</em>.
     * @param reader The {@link JsonReader} to read the data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray}, If the operation was
     * cancelled before it completed normally then the returned value undefined.
     * @throws IOException if an error occurs while reading the data.
     * @throws JSONException if data can not be parsed.
     * @see #newInstance(String, Cancelable)
     */
    public static <T> T newInstance(JsonReader reader, Cancelable cancelable) throws IOException, JSONException {
        switch (reader.peek()) {
        case BEGIN_ARRAY:
            return (T)newArrayImpl(reader, DummyCancelable.obtain(cancelable));

        case BEGIN_OBJECT:
            return (T)newInstanceImpl(reader, DummyCancelable.obtain(cancelable));

        default:
            DebugUtils.__checkError(true, "Invalid json token - " + reader.peek());
            return null;
        }
    }

    /**
     * Returns a new instance parsed from the specified <em>jsonFile</em>.
     * @param jsonFile The json file to read the data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray}, If the operation was
     * cancelled before it completed normally then the returned value undefined.
     * @throws IOException if an error occurs while reading the data.
     * @throws JSONException if data can not be parsed.
     * @see #newInstance(JsonReader, Cancelable)
     */
    public static <T> T newInstance(String jsonFile, Cancelable cancelable) throws IOException, JSONException {
        final JsonReader reader = new JsonReader(new InputStreamReader(new FileInputStream(jsonFile)));
        try {
            return newInstance(reader, cancelable);
        } finally {
            reader.close();
        }
    }

    /**
     * Writes the specified <em>object</em> into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param object May be a <tt>JSONObject, JSONArray, String, Boolean,
     * Number</tt> or their collections(<tt>Array, Collection, Map</tt>).
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeObject(String, Object)
     */
    public static JsonWriter writeObject(JsonWriter writer, Object object) throws IOException {
        if (object == null || object == JSONObject.NULL) {
            return writer.nullValue();
        } else if (object instanceof String) {
            return writer.value((String)object);
        } else if (object instanceof Number) {
            return writer.value((Number)object);
        } else if (object instanceof Boolean) {
            return writer.value((boolean)object);
        } else if (object instanceof Object[]) {
            return writeColl(writer, Arrays.asList((Object[])object));
        } else if (object instanceof Map) {
            return writeMap(writer, (Map<String, ?>)object);
        } else if (object instanceof Collection) {
            return writeColl(writer, (Collection<?>)object);
        } else if (object instanceof JSONArray) {
            return writeJSONArray(writer, (JSONArray)object);
        } else if (object instanceof JSONObject) {
            return writeJSONObject(writer, (JSONObject)object);
        } else {
            return writer.value(object.toString());
        }
    }

    /**
     * Writes the specified <em>object</em> into a <em>jsonFile</em>.
     * @param jsonFile The json file to write.
     * @param object May be a <tt>JSONObject, JSONArray, String, Boolean,
     * Number</tt> or their collections(<tt>Array, Collection, Map</tt>).
     * @throws IOException if an error occurs while writing to the file.
     * @see #writeObject(JsonWriter, Object)
     */
    public static void writeObject(String jsonFile, Object object) throws IOException {
        final JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(jsonFile)));
        try {
            writeObject(writer, object);
        } finally {
            writer.close();
        }
    }

    private static JSONObject newInstanceImpl(JsonReader reader, Cancelable cancelable) throws IOException, JSONException {
        final JSONObject result = new JSONObject();
        reader.beginObject();

        while (reader.hasNext() && !cancelable.isCancelled()) {
            final String name = reader.nextName();
            switch (reader.peek()) {
            case STRING:
                result.put(name, reader.nextString());
                break;

            case NUMBER:
                result.put(name, readNumber(reader));
                break;

            case BOOLEAN:
                result.put(name, reader.nextBoolean());
                break;

            case BEGIN_ARRAY:
                result.put(name, newArrayImpl(reader, cancelable));
                break;

            case BEGIN_OBJECT:
                result.put(name, newInstanceImpl(reader, cancelable));
                break;

            default:
                reader.skipValue();
            }
        }

        if (!cancelable.isCancelled()) {
            reader.endObject();
        }

        return result;
    }

    private static JSONArray newArrayImpl(JsonReader reader, Cancelable cancelable) throws IOException, JSONException {
        final JSONArray result = new JSONArray();
        reader.beginArray();

        while (reader.hasNext() && !cancelable.isCancelled()) {
            switch (reader.peek()) {
            case STRING:
                result.put(reader.nextString());
                break;

            case NUMBER:
                result.put(readNumber(reader));
                break;

            case BOOLEAN:
                result.put(reader.nextBoolean());
                break;

            case BEGIN_ARRAY:
                result.put(newArrayImpl(reader, cancelable));
                break;

            case BEGIN_OBJECT:
                result.put(newInstanceImpl(reader, cancelable));
                break;

            default:
                reader.skipValue();
            }
        }

        if (!cancelable.isCancelled()) {
            reader.endArray();
        }

        return result;
    }

    private static Number readNumber(JsonReader reader) throws IOException {
        final String result = reader.nextString();
        try {
            return Long.valueOf(result, 10);
        } catch (NumberFormatException e) {
            return Double.valueOf(result);
        }
    }

    private static JsonWriter writeMap(JsonWriter writer, Map<String, ?> values) throws IOException {
        writer.beginObject();
        for (Entry<String, ?> entry : values.entrySet()) {
            writeObject(writer.name(entry.getKey()), entry.getValue());
        }

        return writer.endObject();
    }

    private static JsonWriter writeColl(JsonWriter writer, Collection<?> values) throws IOException {
        writer.beginArray();
        for (Object value : values) {
            writeObject(writer, value);
        }

        return writer.endArray();
    }

    private static JsonWriter writeJSONArray(JsonWriter writer, JSONArray values) throws IOException {
        writer.beginArray();
        for (int i = 0, length = values.length(); i < length; ++i) {
            writeObject(writer, values.opt(i));
        }

        return writer.endArray();
    }

    private static JsonWriter writeJSONObject(JsonWriter writer, JSONObject values) throws IOException {
        writer.beginObject();
        final Iterator<String> names = values.keys();
        while (names.hasNext()) {
            final String name = names.next();
            writeObject(writer.name(name), values.opt(name));
        }

        return writer.endObject();
    }

    /**
     * Class <tt>EmptyJSONArray</tt> is an implementation of a {@link JSONArray}.
     */
    private static final class EmptyJSONArray extends JSONArray {
        public static final JSONArray sInstance = new EmptyJSONArray();

        @Override
        public int length() {
            return 0;
        }

        @Override
        public boolean isNull(int index) {
            return true;
        }

        @Override
        public Object remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONArray put(int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONArray put(long value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONArray put(boolean value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONArray put(Object value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONArray put(double value) throws JSONException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONArray put(int index, Object value) throws JSONException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * Class <tt>EmptyJSONObject</tt> is an implementation of a {@link JSONObject}.
     */
    private static final class EmptyJSONObject extends JSONObject {
        public static final JSONObject sInstance = new EmptyJSONObject();

        @Override
        public int length() {
            return 0;
        }

        @Override
        public boolean has(String name) {
            return false;
        }

        @Override
        public boolean isNull(String name) {
            return true;
        }

        @Override
        public Object remove(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject put(String name, int value) throws JSONException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject put(String name, long value) throws JSONException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject put(String name, Object value) throws JSONException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject put(String name, double value) throws JSONException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject put(String name, boolean value) throws JSONException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject putOpt(String name, Object value) throws JSONException {
            throw new UnsupportedOperationException();
        }

        @Override
        public JSONObject accumulate(String name, Object value) throws JSONException {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private JSONUtils() {
    }
}
