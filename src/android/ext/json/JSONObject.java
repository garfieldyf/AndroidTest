package android.ext.json;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import android.ext.util.DebugUtils;

/**
 * Class JSONObject
 * @author Garfield
 */
public class JSONObject {
    /* package */ final Map<String, Object> values;

    /**
     * Constructor
     * @see #JSONObject(JSONObject)
     */
    public JSONObject() {
        this.values = new LinkedHashMap<String, Object>();
    }

    /**
     * Copy constructor
     * @param object The <tt>JSONObject</tt> to copy.
     * @see #JSONObject()
     */
    public JSONObject(JSONObject object) {
        this.values = new LinkedHashMap<String, Object>(object.values);
    }

    /**
     * Constructor
     */
    /* package */ JSONObject(Map<String, Object> values) {
        this.values = values;
    }

    /**
     * Returns the number of name/value mappings in this object.
     * @return The number of name/value mappings in this object.
     */
    public int length() {
        return values.size();
    }

    /**
     * Maps the specified <em>name</em> to the specified <em>value</em>.
     * @param name The JSON property name.
     * @param value A {@link JSONObject}, {@link JSONArray}, <tt>String,
     * Boolean, Integer, Long, Double</tt>, or <tt>null</tt>. May not be
     * {@link Double#isNaN() NaN} or {@link Double#isInfinite() infinite}.
     * @return This object.
     */
    public JSONObject put(String name, Object value) {
        JSONUtils.__checkDouble(value);
        DebugUtils.__checkError(name == null, "name == null");
        values.put(name, value);
        return this;
    }

    /**
     * Maps the specified <em>name</em> to the specified <em>value</em>.
     * @param name The JSON property name.
     * @param value An integer value.
     * @return This object.
     */
    public JSONObject put(String name, int value) {
        DebugUtils.__checkError(name == null, "name == null");
        values.put(name, value);
        return this;
    }

    /**
     * Maps the specified <em>name</em> to the specified <em>value</em>.
     * @param name The JSON property name.
     * @param value A long value.
     * @return This object.
     */
    public JSONObject put(String name, long value) {
        DebugUtils.__checkError(name == null, "name == null");
        values.put(name, value);
        return this;
    }

    /**
     * Maps the specified <em>name</em> to the specified <em>value</em>.
     * @param name The JSON property name.
     * @param value A boolean value.
     * @return This object.
     */
    public JSONObject put(String name, boolean value) {
        DebugUtils.__checkError(name == null, "name == null");
        values.put(name, value);
        return this;
    }

    /**
     * Maps the specified <em>name</em> to the specified <em>value</em>.
     * @param name The JSON property name.
     * @param value A float value. May not be {@link Float#isNaN() NaN}
     * or {@link Float#isInfinite() infinite}.
     * @return This object.
     */
    public JSONObject put(String name, float value) {
        DebugUtils.__checkError(name == null, "name == null");
        JSONUtils.__checkDouble(value);
        values.put(name, value);
        return this;
    }

    /**
     * Maps the specified <em>name</em> to the specified <em>value</em>.
     * @param name The JSON property name.
     * @param value A double value. May not be {@link Double#isNaN() NaN}
     * or {@link Double#isInfinite() infinite}.
     * @return This object.
     */
    public JSONObject put(String name, double value) {
        DebugUtils.__checkError(name == null, "name == null");
        JSONUtils.__checkDouble(value);
        values.put(name, value);
        return this;
    }

    /**
     * Returns the value mapped by <em>name</em>.
     * @param name The JSON property name.
     * @return The value mapped by <em>name</em>,
     * or <tt>null</tt> if no such mapping exists.
     */
    public Object opt(String name) {
        return values.get(name);
    }

    /**
     * Equivalent to calling <tt>optInt(name, 0)</tt>.
     * @see #optInt(String, int)
     */
    public int optInt(String name) {
        return JSONUtils.toInt(values.get(name), 0);
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists and is an int
     * or can be coerced to an int. Returns <em>fallback</em> otherwise.
     * @param name The JSON property name.
     * @return An integer value or <em>fallback</em>.
     * @see #optInt(String)
     */
    public int optInt(String name, int fallback) {
        return JSONUtils.toInt(values.get(name), fallback);
    }

    /**
     * Equivalent to calling <tt>optLong(name, 0)</tt>.
     * @see #optLong(String, int)
     */
    public long optLong(String name) {
        return JSONUtils.toLong(values.get(name), 0);
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists and is a long
     * or can be coerced to a long. Returns <em>fallback</em> otherwise.
     * @param name The JSON property name.
     * @return The long value or <em>fallback</em>.
     * @see #optLong(String)
     */
    public long optLong(String name, long fallback) {
        return JSONUtils.toLong(values.get(name), fallback);
    }

    /**
     * Equivalent to calling <tt>optString(name, "")</tt>.
     * @see #optString(String, String)
     */
    public String optString(String name) {
        return JSONUtils.toString(values.get(name), "");
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists, coercing it
     * if necessary. Returns <em>fallback</em> if no such mapping exists.
     * @param name The JSON property name.
     * @return The <tt>String</tt> value or <em>fallback</em>.
     * @see #optString(String)
     */
    public String optString(String name, String fallback) {
        return JSONUtils.toString(values.get(name), fallback);
    }

    /**
     * Equivalent to calling <tt>optFloat(name, 0)</tt>.
     * @see #optFloat(String, float)
     */
    public float optFloat(String name) {
        return (float)JSONUtils.toDouble(values.get(name), 0);
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists and is a float
     * or can be coerced to a float. Returns <em>fallback</em> otherwise.
     * @param name The JSON property name.
     * @return The float value or <em>fallback</em>.
     * @see #optFloat(String)
     */
    public float optFloat(String name, float fallback) {
        return (float)JSONUtils.toDouble(values.get(name), fallback);
    }

    /**
     * Equivalent to calling <tt>optDouble(name, 0)</tt>.
     * @see #optDouble(String, double)
     */
    public double optDouble(String name) {
        return JSONUtils.toDouble(values.get(name), 0);
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists and is a double
     * or can be coerced to a double. Returns <em>fallback</em> otherwise.
     * @param name The JSON property name.
     * @return The double value or <em>fallback</em>.
     * @see #optDouble(String)
     */
    public double optDouble(String name, double fallback) {
        return JSONUtils.toDouble(values.get(name), fallback);
    }

    /**
     * Equivalent to calling <tt>optBoolean(name, false)</tt>.
     * @see #optBoolean(String, boolean)
     */
    public boolean optBoolean(String name) {
        return JSONUtils.toBoolean(values.get(name), false);
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists and is a boolean
     * or can be coerced to a boolean. Returns <em>fallback</em> otherwise.
     * @param name The JSON property name.
     * @return The boolean value or <em>fallback</em>.
     * @see #optBoolean(String)
     */
    public boolean optBoolean(String name, boolean fallback) {
        return JSONUtils.toBoolean(values.get(name), fallback);
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists and
     * is a {@link JSONArray}. Returns <tt>null</tt> otherwise.
     * @param name The JSON property name.
     * @return The <tt>JSONArray</tt> or <tt>null</tt>.
     */
    public JSONArray optJSONArray(String name) {
        final Object value = values.get(name);
        return (value instanceof JSONArray ? (JSONArray)value : null);
    }

    /**
     * Returns the value mapped by <em>name</em> if it exists and
     * is a {@link JSONObject}. Returns <tt>null</tt> otherwise.
     * @param name The JSON property name.
     * @return The <tt>JSONObject</tt> or <tt>null</tt>.
     */
    public JSONObject optJSONObject(String name) {
        final Object value = values.get(name);
        return (value instanceof JSONObject ? (JSONObject)value : null);
    }

    /**
     * Returns <tt>true</tt> if this object has a mapping for <em>name</em>.
     */
    public boolean has(String name) {
        return values.containsKey(name);
    }

    /**
     * Returns <tt>true</tt> if this object has no mapping for <em>name</em>.
     */
    public boolean isNull(String name) {
        return (values.get(name) == null);
    }

    /**
     * Removes a mapping with the specified <em>name</em> from this object.
     * @param name The JSON property name to remove.
     * @return The value previously mapped by <em>name</em>, or <tt>null</tt>
     * if there was no such mapping.
     */
    public Object remove(String name) {
        return values.remove(name);
    }

    /**
     * Returns a {@link Set} of the property names in this object.
     * @return A <tt>Set</tt> of the property names.
     */
    public Set<String> names() {
        return values.keySet();
    }

    /**
     * Returns a {@link Collection} of the values contained in this object.
     * @return A <tt>Collection</tt> of the values.
     */
    public Collection<Object> values() {
        return values.values();
    }

    /**
     * Returns a {@link Set} of the name/value entries in this object.
     * @return A <tt>Set</tt> of the name/value entries.
     */
    public Set<Entry<String, Object>> entries() {
        return values.entrySet();
    }

    @Override
    public int hashCode() {
        return values.hashCode();
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(values);
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof JSONObject && values.equals(((JSONObject)object).values));
    }
}
