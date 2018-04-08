package android.ext.util;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.ext.content.Loader.Task;
import android.ext.util.Pools.TaskWrapper;
import android.os.AsyncTask;
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
     * The <tt>0-length</tt> {@link JSONArray}.
     */
    public static final JSONArray EMPTY_ARRAY = new EmptyJSONArray();

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
     * Returns an unmodifiable {@link List} from the specified <em>array</em>.
     * @param array The <tt>JSONArray</tt>.
     * @return An unmodifiable <tt>List</tt>.
     * @see #toMap(JSONObject)
     */
    public static <T> List<T> toList(JSONArray array) {
        DebugUtils._checkPotentialAssertion(array == null, "array == null");
        return Collections.unmodifiableList(JSON.<T>toList(array));
    }

    /**
     * Returns an unmodifiable {@link Map} from the specified <em>object</em>.
     * @param object The <tt>JSONObject</tt>.
     * @return An unmodifiable <tt>Map</tt>.
     * @see #toList(JSONArray)
     */
    public static <T> Map<String, T> toMap(JSONObject object) {
        DebugUtils._checkPotentialAssertion(object == null, "object == null");
        return Collections.unmodifiableMap(JSON.<T>toMap(object));
    }

    /**
     * Equivalent to calling {@link JSONObject#putOpt(String, Object)}.
     * @param object The <tt>JSONObject</tt>.
     * @param name The JSON property name.
     * @param value a <tt>JSONObject, JSONArray, String, Boolean,
     * Integer, Long, Double,</tt> or <tt>null</tt>.
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
     * Returns a new instance parsed from the specified <em>reader</em>.
     * @param reader The {@link JsonReader} to read the data.
     * @param task The task whose executing this method. May be one of
     * {@link AsyncTask}, {@link Task} or <tt>null</tt>.
     * @return If parse succeeded return a {@link JSONObject} or {@link JSONArray}
     * object, If the <tt>task</tt> was cancelled the returned value undefined.
     * @throws IOException if an error occurs while reading the data.
     * @throws JSONException if data can not be parsed.
     * @see #newInstance(String, Object)
     */
    public static <T> T newInstance(JsonReader reader, Object task) throws IOException, JSONException {
        final TaskWrapper wrapper = TaskWrapper.obtain(task);
        final T result;
        switch (reader.peek()) {
        case BEGIN_ARRAY:
            result = (T)newArrayImpl(reader, wrapper);
            break;

        case BEGIN_OBJECT:
            result = (T)newInstanceImpl(reader, wrapper);
            break;

        default:
            DebugUtils._checkPotentialAssertion(true, "Invalid json token - " + reader.peek());
            result = null;
        }

        TaskWrapper.recycle(wrapper);
        return result;
    }

    /**
     * Returns a new instance parsed from the specified <em>jsonFile</em>.
     * @param jsonFile The json file to read the data.
     * @param task The task whose executing this method. May be one of
     * {@link AsyncTask}, {@link Task} or <tt>null</tt>.
     * @return If parse succeeded return a {@link JSONObject} or {@link JSONArray}
     * object, If the <tt>task</tt> was cancelled the returned value undefined.
     * @throws IOException if an error occurs while reading the data.
     * @throws JSONException if data can not be parsed.
     * @see #newInstance(JsonReader, Object)
     */
    public static <T> T newInstance(String jsonFile, Object task) throws IOException, JSONException {
        final JsonReader reader = new JsonReader(new FileReader(jsonFile));
        try {
            return newInstance(reader, task);
        } finally {
            reader.close();
        }
    }

    /**
     * Writes the specified <em>object</em> into a {@link JsonWriter}.
     * @param writer The {@link JsonWriter}.
     * @param object May be a <tt>JSONObject, JSONArray, String, Boolean,
     * Number</tt> or a container of the <tt>JSONArray or JSONObject</tt>.
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
        } else if (object instanceof JSONObject) {
            return writeMap(writer, JSON.toMap((JSONObject)object));
        } else if (object instanceof JSONArray) {
            return writeColl(writer, JSON.toList((JSONArray)object));
        } else {
            return writer.value(object.toString());
        }
    }

    /**
     * Writes the specified <em>object</em> into a <em>jsonFile</em>.
     * @param jsonFile The json file to write.
     * @param object May be a <tt>JSONObject, JSONArray, String, Boolean,
     * Number</tt> or a container of the <tt>JSONArray or JSONObject</tt>.
     * @throws IOException if an error occurs while writing to the file.
     * @see #writeObject(JsonWriter, Object)
     */
    public static void writeObject(String jsonFile, Object object) throws IOException {
        final JsonWriter writer = new JsonWriter(new FileWriter(jsonFile));
        try {
            writeObject(writer, object);
        } finally {
            writer.close();
        }
    }

    private static JSONObject newInstanceImpl(JsonReader reader, TaskWrapper wrapper) throws IOException, JSONException {
        final JSONObject result = new JSONObject();
        reader.beginObject();

        while (reader.hasNext() && !wrapper.isCancelled()) {
            final String name = reader.nextName();
            switch (reader.peek()) {
            case BEGIN_ARRAY:
                result.put(name, newArrayImpl(reader, wrapper));
                break;

            case BEGIN_OBJECT:
                result.put(name, newInstanceImpl(reader, wrapper));
                break;

            case STRING:
                result.put(name, reader.nextString());
                break;

            case NUMBER:
                result.put(name, readNumber(reader));
                break;

            case BOOLEAN:
                result.put(name, reader.nextBoolean());
                break;

            default:
                reader.skipValue();
            }
        }

        if (!wrapper.isCancelled()) {
            reader.endObject();
        }

        return result;
    }

    private static JSONArray newArrayImpl(JsonReader reader, TaskWrapper wrapper) throws IOException, JSONException {
        final JSONArray result = new JSONArray();
        reader.beginArray();

        while (reader.hasNext() && !wrapper.isCancelled()) {
            switch (reader.peek()) {
            case BEGIN_ARRAY:
                result.put(newArrayImpl(reader, wrapper));
                break;

            case BEGIN_OBJECT:
                result.put(newInstanceImpl(reader, wrapper));
                break;

            case STRING:
                result.put(reader.nextString());
                break;

            case NUMBER:
                result.put(readNumber(reader));
                break;

            case BOOLEAN:
                result.put(reader.nextBoolean());
                break;

            default:
                reader.skipValue();
            }
        }

        if (!wrapper.isCancelled()) {
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

    private static JsonWriter writeMap(JsonWriter writer, Map<String, ?> map) throws IOException {
        writer.beginObject();
        for (Entry<String, ?> entry : map.entrySet()) {
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

    /**
     * Class <tt>JSON</tt> used to obtain the internal container from JSON object.
     */
    private static final class JSON {
        private static final Field sMapField;
        private static final Field sListField;

        public static <T> List<T> toList(JSONArray array) {
            try {
                return (List<T>)sListField.get(array);
            } catch (Exception e) {
                return Collections.emptyList();
            }
        }

        public static <T> Map<String, T> toMap(JSONObject object) {
            try {
                return (Map<String, T>)sMapField.get(object);
            } catch (Exception e) {
                return Collections.emptyMap();
            }
        }

        static {
            try {
                sListField = JSONArray.class.getDeclaredField("values");
                sListField.setAccessible(true);
                sMapField = JSONObject.class.getDeclaredField("nameValuePairs");
                sMapField.setAccessible(true);
            } catch (NoSuchFieldException e) {
                throw new Error(e);
            }
        }
    }

    /**
     * Class <tt>EmptyJSONArray</tt> is an implementation of a {@link JSONArray}.
     */
    /* package */ static final class EmptyJSONArray extends JSONArray {
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
            return null;
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
     * This utility class cannot be instantiated.
     */
    private JSONUtils() {
    }
}
