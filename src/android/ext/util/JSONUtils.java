package android.ext.util;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.ext.util.ArrayUtils.Filter;
import android.util.JsonReader;
import android.util.JsonWriter;

/**
 * Class JSONUtils
 * @author Garfield
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
     * @see #getLength(JSONObject)
     */
    public static int getLength(JSONArray array) {
        return (array != null ? array.length() : 0);
    }

    /**
     * Returns the number of name/value mappings in the
     * <em>object</em>, handling <tt>null object</tt>.
     * @param object The <tt>JSONObject</tt>.
     * @return The number of name/value mappings.
     * @see #getLength(JSONArray)
     */
    public static int getLength(JSONObject object) {
        return (object != null ? object.length() : 0);
    }

    /**
     * Compares the two JSON values, handling <tt>null</tt> values.
     * @param a The first value.
     * @param b The second value.
     * @return <tt>true</tt> if both values are <tt>null</tt> or are
     * equals, <tt>false</tt> otherwise.
     */
    public static boolean equals(Object a, Object b) {
        if (a == b) {
            return true;
        }

        if (JSONObject.NULL.equals(a)) {
            return (JSONObject.NULL.equals(b));
        }

        if (a instanceof JSONArray && b instanceof JSONArray) {
            return equals((JSONArray)a, (JSONArray)b);
        } else if (a instanceof JSONObject && b instanceof JSONObject) {
            return equals((JSONObject)a, (JSONObject)b);
        } else {
            return a.equals(b);
        }
    }

    /**
     * Equivalent to calling {@link JSONObject#optJSONArray(String)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return A <tt>JSONArray</tt> or <tt>null</tt>.
     */
    public static JSONArray optJSONArray(JSONObject object, String name) {
        return (object != null ? object.optJSONArray(name) : null);
    }

    /**
     * Equivalent to calling {@link JSONArray#optJSONObject(int)},
     * handling <tt>null</tt> <em>array</em>.
     * @param array The <tt>JSONArray</tt>.
     * @param index The index of the <tt>JSONObject</tt>.
     * @return A <tt>JSONObject</tt> or <tt>null</tt>.
     */
    public static JSONObject optJSONObject(JSONArray array, int index) {
        return (array != null ? array.optJSONObject(index) : null);
    }

    /**
     * Equivalent to calling {@link JSONObject#optJSONObject(String)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return A <tt>JSONObject</tt> or <tt>null</tt>.
     */
    public static JSONObject optJSONObject(JSONObject object, String name) {
        return (object != null ? object.optJSONObject(name) : null);
    }

    /**
     * Equivalent to calling {@link JSONObject#optInt(String, int)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return An integer value or <em>fallback</em>.
     */
    public static int optInt(JSONObject object, String name, int fallback) {
        return (object != null ? object.optInt(name, fallback) : fallback);
    }

    /**
     * Equivalent to calling {@link JSONObject#optLong(String, long)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return A long value or <em>fallback</em>.
     */
    public static long optLong(JSONObject object, String name, long fallback) {
        return (object != null ? object.optLong(name, fallback) : fallback);
    }

    /**
     * Equivalent to calling {@link JSONObject#optDouble(String, double)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return A double value or <em>fallback</em>.
     */
    public static double optDouble(JSONObject object, String name, double fallback) {
        return (object != null ? object.optDouble(name, fallback) : fallback);
    }

    /**
     * Equivalent to calling {@link JSONObject#optString(String, String)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return A <tt>String</tt> value or <em>fallback</em>.
     */
    public static String optString(JSONObject object, String name, String fallback) {
        return (object != null ? object.optString(name, fallback) : fallback);
    }

    /**
     * Equivalent to calling {@link JSONObject#optBoolean(String, boolean)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return A boolean value or <em>fallback</em>.
     */
    public static boolean optBoolean(JSONObject object, String name, boolean fallback) {
        return (object != null ? object.optBoolean(name, fallback) : fallback);
    }

    /**
     * Equivalent to calling {@link JSONArray#put(int, Object)}.
     * @param array The <tt>JSONArray</tt> to add to.
     * @param index The index at which to put.
     * @param value A <tt>JSONObject, JSONArray, String, Boolean,
     * Number</tt>, {@link JSONObject#NULL}, or <tt>null</tt>.
     * @return The <em>array</em>.
     */
    public static JSONArray put(JSONArray array, int index, Object value) {
        try {
            return array.put(index, value);
        } catch (JSONException e) {
            return array;
        }
    }

    /**
     * Equivalent to calling {@link JSONObject#put(String, Object)}.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @param value A <tt>JSONObject, JSONArray, String, Boolean, Number</tt>,
     * {@link JSONObject#NULL}, or <tt>null</tt>.
     * @return The <em>object</em>.
     */
    public static JSONObject put(JSONObject object, String name, Object value) {
        try {
            return object.put(name, value);
        } catch (JSONException e) {
            return object;
        }
    }

    /**
     * Equivalent to calling {@link JSONObject#putOpt(String, Object)}.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @param value A <tt>JSONObject, JSONArray, String, Boolean, Number</tt>,
     * {@link JSONObject#NULL}, or <tt>null</tt>.
     * @return The <em>object</em>.
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
     * Parses a JSON data from the specified JSON string.
     * @param json A JSON-encoded string.
     * @throws IOException if an error occurs while reading the data.
     * @throws JSONException if data can not be parsed.
     * @return A {@link JSONObject} or {@link JSONArray}.
     * @see #parse(JsonReader, Cancelable)
     * @see #parse(Context, Object, Cancelable)
     */
    public static <T> T parse(String json) throws IOException, JSONException {
        return parse(new JsonReader(new StringReader(json)), null);
    }

    /**
     * Parses a JSON data from the specified <em>reader</em>.
     * @param reader The {@link JsonReader} to read the data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray}, If the operation was
     * cancelled before it completed normally the returned value is undefined.
     * @throws IOException if an error occurs while reading the data.
     * @throws JSONException if data can not be parsed.
     * @see #parse(String)
     * @see #parse(Context, Object, Cancelable)
     */
    public static <T> T parse(JsonReader reader, Cancelable cancelable) throws IOException, JSONException {
        switch (reader.peek()) {
        case BEGIN_ARRAY:
            return (T)parseArray(reader, FileUtils.wrap(cancelable));

        case BEGIN_OBJECT:
            return (T)parseObject(reader, FileUtils.wrap(cancelable));

        default:
            throw new AssertionError("Invalid json token - " + reader.peek());
        }
    }

    /**
     * Parses a JSON data from the specified <em>uri</em>.
     * <h3>Accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>file ({@link #SCHEME_FILE})</li>
     * <li>content ({@link #SCHEME_CONTENT})</li>
     * <li>android.asset ({@link #SCHEME_ANDROID_ASSET})</li>
     * <li>android.resource ({@link #SCHEME_ANDROID_RESOURCE})</li></ul>
     * @param context The <tt>Context</tt>.
     * @param uri The uri to read the data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray}, If the operation was
     * cancelled before it completed normally the returned value is undefined.
     * @throws IOException if an error occurs while reading the data.
     * @throws JSONException if data can not be parsed.
     * @see #parse(String)
     * @see #parse(JsonReader, Cancelable)
     * @see UriUtils#openInputStream(Context, Object)
     */
    public static <T> T parse(Context context, Object uri, Cancelable cancelable) throws IOException, JSONException {
        final JsonReader reader = new JsonReader(new InputStreamReader(UriUtils.openInputStream(context, uri)));
        try {
            return parse(reader, cancelable);
        } finally {
            reader.close();
        }
    }

    /**
     * Writes the specified <em>object</em> into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param object May be a <tt>String, Boolean, Number, JSONObject,
     * JSONArray</tt> or their collections(<tt>Array, Collection, Map</tt>).
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeObject(String, Object)
     */
    public static JsonWriter writeObject(JsonWriter writer, Object object) throws IOException {
        if (JSONObject.NULL.equals(object)) {
            return writer.nullValue();
        } else if (object instanceof String) {
            return writer.value((String)object);
        } else if (object instanceof Number) {
            return writer.value((Number)object);
        } else if (object instanceof Boolean) {
            return writer.value((boolean)object);
        } else if (object instanceof JSONArray) {
            return writeValues(writer, (JSONArray)object);
        } else if (object instanceof JSONObject) {
            return writeValues(writer, (JSONObject)object);
        } else if (object instanceof Object[]) {
            return writeValues(writer, Arrays.asList((Object[])object));
        } else if (object instanceof Set) {
            return writeValues(writer, (Set<Entry<String, Object>>)object);
        } else if (object instanceof Collection) {
            return writeValues(writer, (Collection<Object>)object);
        } else if (object instanceof Map) {
            return writeValues(writer, ((Map<String, Object>)object).entrySet());
        } else {
            throw new AssertionError("Unsupported type - " + object.getClass().getName());
        }
    }

    /**
     * Writes the specified <em>object</em> into a <em>jsonFile</em>.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param jsonFile The json file to write.
     * @param object May be a <tt>String, Boolean, Number, JSONObject,
     * JSONArray</tt> or their collections(<tt>Array, Collection, Map</tt>).
     * @throws IOException if an error occurs while writing to the file.
     * @see #writeObject(JsonWriter, Object)
     */
    public static void writeObject(String jsonFile, Object object) throws IOException {
        FileUtils.mkdirs(jsonFile, FileUtils.FLAG_IGNORE_FILENAME);
        final JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(jsonFile)));
        try {
            writeObject(writer, object);
        } finally {
            writer.close();
        }
    }

    private static boolean equals(JSONArray a, JSONArray b) {
        final int length = a.length();
        if (length != b.length()) {
            return false;
        }

        for (int i = 0; i < length; ++i) {
            if (!equals(a.opt(i), b.opt(i))) {
                return false;
            }
        }

        return true;
    }

    private static boolean equals(JSONObject a, JSONObject b) {
        if (a.length() != b.length()) {
            return false;
        }

        final Iterator<String> keys = a.keys();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (!b.has(key) || !equals(a.opt(key), b.opt(key))) {
                return false;
            }
        }

        return true;
    }

    private static Number parseNumber(JsonReader reader) throws IOException {
        final String string = reader.nextString();
        if (string.indexOf('.') == -1) {
            try {
                return Integer.valueOf(string, 10);
            } catch (NumberFormatException e) {
                try {
                    return Long.valueOf(string, 10);
                } catch (NumberFormatException ex) {
                }
            }
        }

        return Double.valueOf(string);
    }

    private static JSONArray parseArray(JsonReader reader, Cancelable cancelable) throws IOException, JSONException {
        final JSONArray result = new JSONArray();
        reader.beginArray();

        while (reader.hasNext()) {
            switch (reader.peek()) {
            case NULL:
                reader.nextNull();
                result.put(JSONObject.NULL);
                break;

            case STRING:
                result.put(reader.nextString());
                break;

            case NUMBER:
                result.put(parseNumber(reader));
                break;

            case BOOLEAN:
                result.put(reader.nextBoolean());
                break;

            case BEGIN_ARRAY:
                result.put(parseArray(reader, cancelable));
                break;

            case BEGIN_OBJECT:
                result.put(parseObject(reader, cancelable));
                break;

            default:
                reader.skipValue();
            }

            if (cancelable.isCancelled()) {
                return result;
            }
        }

        reader.endArray();
        return result;
    }

    private static JSONObject parseObject(JsonReader reader, Cancelable cancelable) throws IOException, JSONException {
        final JSONObject result = new JSONObject();
        reader.beginObject();

        while (reader.hasNext()) {
            final String name = reader.nextName();
            switch (reader.peek()) {
            case STRING:
                result.put(name, reader.nextString());
                break;

            case NUMBER:
                result.put(name, parseNumber(reader));
                break;

            case BOOLEAN:
                result.put(name, reader.nextBoolean());
                break;

            case BEGIN_ARRAY:
                result.put(name, parseArray(reader, cancelable));
                break;

            case BEGIN_OBJECT:
                result.put(name, parseObject(reader, cancelable));
                break;

            default:
                reader.skipValue();
            }

            if (cancelable.isCancelled()) {
                return result;
            }
        }

        reader.endObject();
        return result;
    }

    private static JsonWriter writeValues(JsonWriter writer, JSONArray values) throws IOException {
        writer.beginArray();
        for (int i = 0, length = values.length(); i < length; ++i) {
            writeObject(writer, values.opt(i));
        }

        return writer.endArray();
    }

    private static JsonWriter writeValues(JsonWriter writer, JSONObject values) throws IOException {
        final Iterator<String> names = values.keys();
        writer.beginObject();
        while (names.hasNext()) {
            final String name = names.next();
            writeObject(writer.name(name), values.opt(name));
        }

        return writer.endObject();
    }

    private static JsonWriter writeValues(JsonWriter writer, Collection<Object> values) throws IOException {
        writer.beginArray();
        for (Object value : values) {
            writeObject(writer, value);
        }

        return writer.endArray();
    }

    private static JsonWriter writeValues(JsonWriter writer, Set<Entry<String, Object>> values) throws IOException {
        writer.beginObject();
        for (Entry<String, Object> value : values) {
            writeObject(writer.name(value.getKey()), value.getValue());
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
