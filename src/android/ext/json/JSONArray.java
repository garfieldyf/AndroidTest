package android.ext.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class JSONArray
 * @author Garfield
 */
public class JSONArray {
    /* package */ final List<Object> values;

    /**
     * Constructor
     * @see #JSONArray(JSONArray)
     */
    public JSONArray() {
        this.values = new ArrayList<Object>();
    }

    /**
     * Copy constructor
     * @param array The <tt>JSONArray</tt> to copy.
     * @see #JSONArray()
     */
    public JSONArray(JSONArray array) {
        this.values = new ArrayList<Object>(array.values);
    }

    /**
     * Constructor
     */
    /* package */ JSONArray(List<Object> values) {
        this.values = values;
    }

    /**
     * Returns the number of values in this array.
     * @return The number of values in this array.
     */
    public int length() {
        return values.size();
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A {@link JSONObject}, {@link JSONArray}, <tt>String,
     * Boolean, Integer, Long, Double</tt>, or <tt>null</tt>. May not be
     * {@link Double#isNaN() NaN} or {@link Double#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(Object value) {
        JSONUtils.__checkDouble(value);
        values.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value An integer value to add.
     * @return This array.
     */
    public JSONArray add(int value) {
        values.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A long value to add.
     * @return This array.
     */
    public JSONArray add(long value) {
        values.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A boolean value to add.
     * @return This array.
     */
    public JSONArray add(boolean value) {
        values.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A float value to add. May not be {@link Float#isNaN() NaN}
     * or {@link Float#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(float value) {
        JSONUtils.__checkDouble(value);
        values.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A double value to add. May not be {@link Double#isNaN() NaN}
     * or {@link Double#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(double value) {
        JSONUtils.__checkDouble(value);
        values.add(value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value A {@link JSONObject}, {@link JSONArray}, <tt>String,
     * Boolean, Integer, Long, Double</tt>, or <tt>null</tt>. May not be
     * {@link Double#isNaN() NaN} or {@link Double#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(int index, Object value) {
        JSONUtils.__checkDouble(value);
        values.add(index, value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value An integer value to add.
     * @return This array.
     */
    public JSONArray add(int index, int value) {
        values.add(index, value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value A long value to add.
     * @return This array.
     */
    public JSONArray add(int index, long value) {
        values.add(index, value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value A boolean value to add.
     * @return This array.
     */
    public JSONArray add(int index, boolean value) {
        values.add(index, value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value A float value to add. May not be {@link Float#isNaN() NaN}
     * or {@link Float#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(int index, float value) {
        JSONUtils.__checkDouble(value);
        values.add(index, value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value A double value to add. May not be {@link Double#isNaN() NaN}
     * or {@link Double#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(int index, double value) {
        JSONUtils.__checkDouble(value);
        values.add(index, value);
        return this;
    }

    /**
     * Returns the value at <em>index</em>, or <tt>null</tt>
     * if this array has no value at <em>index</em>.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <tt>null</tt>.
     */
    public Object opt(int index) {
        return (index >= 0 && index < values.size() ? values.get(index) : null);
    }

    /**
     * Equivalent to calling <tt>optInt(index, 0)</tt>.
     * @see #optInt(int, int)
     */
    public int optInt(int index) {
        return JSONUtils.toInt(opt(index), 0);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is an int
     * or can be coerced to an int. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optInt(int)
     */
    public int optInt(int index, int fallback) {
        return JSONUtils.toInt(opt(index), fallback);
    }

    /**
     * Equivalent to calling <tt>optLong(index, 0)</tt>.
     * @see #optLong(int, long)
     */
    public long optLong(int index) {
        return JSONUtils.toLong(opt(index), 0);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is a long
     * or can be coerced to a long. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optLong(int)
     */
    public long optLong(int index, long fallback) {
        return JSONUtils.toLong(opt(index), fallback);
    }

    /**
     * Equivalent to calling <tt>optFloat(index, 0)</tt>.
     * @see #optFloat(int, float)
     */
    public float optFloat(int index) {
        return (float)JSONUtils.toDouble(opt(index), 0);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is a float
     * or can be coerced to a float. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optFloat(int)
     */
    public float optFloat(int index, float fallback) {
        return (float)JSONUtils.toDouble(opt(index), fallback);
    }

    /**
     * Equivalent to calling <tt>optDouble(index, 0)</tt>.
     * @see #optDouble(int, double)
     */
    public double optDouble(int index) {
        return JSONUtils.toDouble(opt(index), 0);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is a double
     * or can be coerced to a double. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optDouble(int)
     */
    public double optDouble(int index, double fallback) {
        return JSONUtils.toDouble(opt(index), fallback);
    }

    /**
     * Equivalent to calling <tt>optBoolean(index, false)</tt>.
     * @see #optBoolean(int, boolean)
     */
    public boolean optBoolean(int index) {
        return JSONUtils.toBoolean(opt(index), false);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is a boolean
     * or can be coerced to a boolean. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optBoolean(int)
     */
    public boolean optBoolean(int index, boolean fallback) {
        return JSONUtils.toBoolean(opt(index), fallback);
    }

    /**
     * Equivalent to calling <tt>optString(index, "")</tt>.
     * @see #optString(int, String)
     */
    public String optString(int index) {
        return JSONUtils.toString(opt(index), "");
    }

    /**
     * Returns the value at <em>index</em> if it exists, coercing it
     * if necessary. Returns <em>fallback</em> if no such value exists.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optString(int)
     */
    public String optString(int index, String fallback) {
        return JSONUtils.toString(opt(index), fallback);
    }

    /**
     * Returns the value at <em>index</em> if it exists and
     * is a {@link JSONArray}. Returns <tt>null</tt> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <tt>null</tt>.
     */
    public JSONArray optJSONArray(int index) {
        final Object value = opt(index);
        return (value instanceof JSONArray ? (JSONArray)value : null);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is
     * a {@link JSONObject}. Returns <tt>null</tt> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <tt>null</tt>.
     */
    public JSONObject optJSONObject(int index) {
        final Object value = opt(index);
        return (value instanceof JSONObject ? (JSONObject)value : null);
    }

    /**
     * Returns the value at the specified <em>index</em> in this array.
     * @param index The index of the value.
     * @return The value at <em>index</em>.
     * @throws IndexOutOfBoundsException if <tt>index < 0 || index >= length()</tt>
     */
    public Object get(int index) {
        return values.get(index);
    }

    /**
     * Sets the value at the specified <em>index</em> in this array with
     * the specified <em>value</em>.
     * @param index The index of the value.
     * @param value The value to set.
     * @return The previous value at the specified <em>index</em>.
     * @throws IndexOutOfBoundsException if <tt>index < 0 || index >= length()</tt>
     */
    public Object set(int index, Object value) {
        return values.set(index, value);
    }

    /**
     * Returns <tt>true</tt> if this array has no value at
     * <em>index</em>, or if its value is the <tt>null</tt>.
     */
    public boolean isNull(int index) {
        return (opt(index) == null);
    }

    /**
     * Removes and returns the value at <em>index</em>, or <tt>null</tt>
     * if this array has no value at <em>index</em>.
     * @param index The index of the value to remove.
     * @return The removed value or <tt>null</tt>.
     */
    public Object remove(int index) {
        return (index >= 0 && index < values.size() ? values.remove(index) : null);
    }

    /**
     * Returns an unmodifiable {@link List} of the values contained in this array.
     * @return An unmodifiable <tt>List</tt> of the values.
     */
    public List<Object> values() {
        return Collections.unmodifiableList(values);
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
        return (object instanceof JSONArray && values.equals(((JSONArray)object).values));
    }
}
