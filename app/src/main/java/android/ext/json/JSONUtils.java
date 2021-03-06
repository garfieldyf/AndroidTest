package android.ext.json;

import android.content.Context;
import android.ext.util.Cancelable;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.util.UriUtils;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Printer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class JSONUtils
 * @author Garfield
 */
public final class JSONUtils {
    /**
     * The empty, immutable {@link JSONArray}.
     */
    public static final JSONArray EMPTY_ARRAY = new JSONArray(Collections.emptyList());

    /**
     * The empty, immutable {@link JSONObject}.
     */
    public static final JSONObject EMPTY_OBJECT = new JSONObject(Collections.emptyMap());

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
     * Equivalent to calling {@link JSONObject#optFloat(String, float)},
     * handling <tt>null</tt> <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @return A float value or <em>fallback</em>.
     */
    public static float optFloat(JSONObject object, String name, float fallback) {
        return (object != null ? object.optFloat(name, fallback) : fallback);
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
     * Parses a JSON data from the specified JSON string.
     * @param json A JSON-encoded string.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray}, If the operation was
     * cancelled before it completed normally the returned value is undefined.
     * @throws IOException if an error occurs while reading the data.
     * @see #parse(JsonReader, Cancelable)
     * @see #parse(Context, Object, Cancelable)
     * @see #parse(byte[], int, int, Cancelable)
     */
    public static <T> T parse(String json, Cancelable cancelable) throws IOException {
        DebugUtils.__checkError(json == null, "Invalid parameter - json == null");
        return parse(new JsonReader(new StringReader(json)), cancelable);
    }

    /**
     * Parses a JSON data from the specified <em>reader</em>.
     * @param reader The {@link JsonReader} to read the data.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray}, If the operation was
     * cancelled before it completed normally the returned value is undefined.
     * @throws IOException if an error occurs while reading the data.
     * @see #parse(String, Cancelable)
     * @see #parse(Context, Object, Cancelable)
     * @see #parse(byte[], int, int, Cancelable)
     */
    @SuppressWarnings("unchecked")
    public static <T> T parse(JsonReader reader, Cancelable cancelable) throws IOException {
        switch (reader.peek()) {
        case BEGIN_ARRAY:
            return (T)parseArray(reader, Cancelable.ofNullable(cancelable));

        case BEGIN_OBJECT:
            return (T)parseObject(reader, Cancelable.ofNullable(cancelable));

        default:
            throw new AssertionError("Invalid json token - " + reader.peek());
        }
    }

    /**
     * Parses a JSON data from the specified <em>uri</em>.
     * <h3>The default implementation accepts the following URI schemes:</h3>
     * <ul><li>path (no scheme)</li>
     * <li>{@link File} (no scheme)</li>
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
     * @see #parse(String, Cancelable)
     * @see #parse(JsonReader, Cancelable)
     * @see #parse(byte[], int, int, Cancelable)
     * @see UriUtils#openInputStream(Context, Object)
     */
    public static <T> T parse(Context context, Object uri, Cancelable cancelable) throws IOException {
        try (final JsonReader reader = new JsonReader(new InputStreamReader(UriUtils.openInputStream(context, uri), StandardCharsets.UTF_8))) {
            return parse(reader, cancelable);
        }
    }

    /**
     * Parses a JSON data from the specified <em>buf</em>.
     * @param buf The byte array to read the data.
     * @param offset The start position in the <em>buf</em>.
     * @param length The number of bytes to read.
     * @param cancelable A {@link Cancelable} can be check the operation is cancelled, or <tt>null</tt> if none.
     * @return If the operation succeeded return a {@link JSONObject} or {@link JSONArray}, If the operation was
     * cancelled before it completed normally the returned value is undefined.
     * @throws IOException if an error occurs while reading the data.
     * @see #parse(String, Cancelable)
     * @see #parse(JsonReader, Cancelable)
     * @see #parse(Context, Object, Cancelable)
     */
    public static <T> T parse(byte[] buf, int offset, int length, Cancelable cancelable) throws IOException {
        DebugUtils.__checkRange(offset, length, buf.length);
        return parse(new JsonReader(new InputStreamReader(new ByteArrayInputStream(buf, offset, length), StandardCharsets.UTF_8)), cancelable);
    }

    /**
     * Writes the specified <em>object</em> into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param object May be a <tt>String, Boolean, Number, JSONObject,
     * JSONArray</tt> or the collections(<tt>Array, Collection, Map</tt>).
     * @return The <em>writer</em>.
     * @throws IOException if an error occurs while writing to the <em>writer</em>.
     * @see #writeObject(File, Object)
     */
    @SuppressWarnings("unchecked")
    public static JsonWriter writeObject(JsonWriter writer, Object object) throws IOException {
        if (object == null) {
            return writer.nullValue();
        } else if (object instanceof String) {
            return writer.value((String)object);
        } else if (object instanceof Number) {
            return writer.value((Number)object);
        } else if (object instanceof Boolean) {
            return writer.value((boolean)object);
        } else if (object instanceof Object[]) {
            return writeValues(writer, Arrays.asList((Object[])object));
        } else if (object instanceof Set) {
            return writeSet(writer, (Set<?>)object);
        } else if (object instanceof Collection) {
            return writeValues(writer, (Collection<Object>)object);
        } else if (object instanceof Map) {
            return writeValues(writer, ((Map<String, Object>)object).entrySet());
        } else if (object instanceof org.json.JSONArray) {
            return writeValues(writer, ((org.json.JSONArray)object));
        } else if (object instanceof org.json.JSONObject) {
            return writeValues(writer, ((org.json.JSONObject)object));
        } else {
            return writer.value(object.toString());
        }
    }

    /**
     * Writes the specified <em>object</em> into a <em>jsonFile</em>.
     * <p>Note: This method will be create the necessary directories.</p>
     * @param jsonFile The json file to write, must be absolute file path.
     * @param object May be a <tt>String, Boolean, Number, JSONObject,
     * JSONArray</tt> or the collections(<tt>Array, Collection, Map</tt>).
     * @throws IOException if an error occurs while writing to the file.
     * @see #writeObject(JsonWriter, Object)
     */
    public static void writeObject(File jsonFile, Object object) throws IOException {
        FileUtils.mkdirs(jsonFile.getPath(), FileUtils.FLAG_IGNORE_FILENAME);
        try (final JsonWriter writer = new JsonWriter(new OutputStreamWriter(new FileOutputStream(jsonFile), StandardCharsets.UTF_8))) {
            writeObject(writer, object);
        }
    }

    /* package */ static int toInt(Object value, int fallback) {
        if (value instanceof Number) {
            return ((Number)value).intValue();
        } else if (value instanceof String) {
            try {
                return (int)Double.parseDouble((String)value);
            } catch (NumberFormatException e) {
                DebugUtils.__checkLogError(true, "JSONUtils", "Couldn't parse to int : " + value, e);
            }
        }

        return fallback;
    }

    /* package */ static long toLong(Object value, long fallback) {
        if (value instanceof Number) {
            return ((Number)value).longValue();
        } else if (value instanceof String) {
            try {
                return (long)Double.parseDouble((String)value);
            } catch (NumberFormatException e) {
                DebugUtils.__checkLogError(true, "JSONUtils", "Couldn't parse to long : " + value, e);
            }
        }

        return fallback;
    }

    /* package */ static String toString(Object value, String fallback) {
        if (value instanceof String) {
            return (String)value;
        } else if (value != null) {
            return value.toString();
        } else {
            return fallback;
        }
    }

    /* package */ static double toDouble(Object value, double fallback) {
        if (value instanceof Number) {
            return ((Number)value).doubleValue();
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String)value);
            } catch (NumberFormatException e) {
                DebugUtils.__checkLogError(true, "JSONUtils", "Couldn't parse to double : " + value, e);
            }
        }

        return fallback;
    }

    /* package */ static boolean toBoolean(Object value, boolean fallback) {
        if (value instanceof Boolean) {
            return (boolean)value;
        } else if (value instanceof String) {
            final String string = (String)value;
            if ("true".equalsIgnoreCase(string)) {
                return true;
            } else if ("false".equalsIgnoreCase(string)) {
                return false;
            }
        }

        return fallback;
    }

    /* package */ static String toJSONString(Object value) {
        try {
            final StringWriter out = new StringWriter(128);
            writeObject(new JsonWriter(out), value);
            return out.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /* package */ static void dump(Printer printer, Object value) {
        try {
            final StringWriter out  = new StringWriter(128);
            final JsonWriter writer = new JsonWriter(out);
            writer.setIndent("    ");
            writeObject(writer, value);
            printer.println(out.toString());
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /* package */ static void __checkDouble(double value) {
        if (Double.isInfinite(value) || Double.isNaN(value)) {
            throw new AssertionError("Forbidden numeric value: " + value);
        }
    }

    /* package */ static void __checkDouble(Object value) {
        if (value instanceof Number) {
            final double d = ((Number)value).doubleValue();
            if (Double.isInfinite(d) || Double.isNaN(d)) {
                throw new AssertionError("Forbidden numeric value: " + value);
            }
        }
    }

    private static Number parseNumber(JsonReader reader) throws IOException {
        final String value = reader.nextString();
        if (value.indexOf('.') == -1) {
            try {
                return Integer.valueOf(value, 10);
            } catch (NumberFormatException e) {
                try {
                    return Long.valueOf(value, 10);
                } catch (NumberFormatException ignored) {
                }
            }
        }

        return Double.valueOf(value);
    }

    private static JSONArray parseArray(JsonReader reader, Cancelable cancelable) throws IOException {
        final JSONArray result = new JSONArray();
        reader.beginArray();

        while (reader.hasNext()) {
            if (cancelable.isCancelled()) {
                DebugUtils.__checkDebug(true, "JSONUtils", "parseArray is cancelled.");
                return result;
            }

            switch (reader.peek()) {
            case STRING:
                result.add(reader.nextString());
                break;

            case NUMBER:
                result.add(parseNumber(reader));
                break;

            case BOOLEAN:
                result.add(reader.nextBoolean());
                break;

            case BEGIN_ARRAY:
                result.add(parseArray(reader, cancelable));
                break;

            case BEGIN_OBJECT:
                result.add(parseObject(reader, cancelable));
                break;

            case NULL:
                // Don't skip NULL, because the result's size will be incorrect.
                result.add(null);
                reader.nextNull();
                DebugUtils.__checkWarning(true, "JSONUtils", "The type is NULL, add null to JSONArray.");
                break;

            default:
                reader.skipValue();
            }
        }

        reader.endArray();
        return result;
    }

    private static JSONObject parseObject(JsonReader reader, Cancelable cancelable) throws IOException {
        final JSONObject result = new JSONObject();
        reader.beginObject();

        while (reader.hasNext()) {
            if (cancelable.isCancelled()) {
                DebugUtils.__checkDebug(true, "JSONUtils", "parseObject is cancelled.");
                return result;
            }

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
        }

        reader.endObject();
        return result;
    }

    @SuppressWarnings("unchecked")
    private static JsonWriter writeSet(JsonWriter writer, Set<?> values) throws IOException {
        if (values.isEmpty()) {
            return writer.nullValue();
        } else if (values.iterator().next() instanceof Entry) {
            return writeValues(writer, (Set<Entry<String, Object>>)values);
        } else {
            return writeValues(writer, (Collection<Object>)values);
        }
    }

    private static JsonWriter writeValues(JsonWriter writer, org.json.JSONArray values) throws IOException {
        writer.beginArray();
        for (int i = 0, length = values.length(); i < length; ++i) {
            writeObject(writer, values.opt(i));
        }

        return writer.endArray();
    }

    private static JsonWriter writeValues(JsonWriter writer, org.json.JSONObject values) throws IOException {
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
     * This utility class cannot be instantiated.
     */
    private JSONUtils() {
    }
}
