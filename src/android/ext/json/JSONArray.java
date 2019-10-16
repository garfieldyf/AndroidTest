package android.ext.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import android.ext.util.DebugUtils;

/**
 * Class JSONArray
 * @author Garfield
 */
public class JSONArray implements Iterable<Object> {
    /**
     * The <tt>0-length</tt>, immutable {@link JSONArray}.
     */
    public static final JSONArray EMPTY = new JSONArray(Collections.emptyList());

    /**
     * The JSON values.
     */
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
    private JSONArray(List<Object> values) {
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
     * @param value A double value to add. May not be {@link Double#isNaN() NaN}
     * or {@link Double#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(double value) {
        DebugUtils.__checkError(Double.isInfinite(value) || Double.isNaN(value), "Forbidden numeric value: " + value);
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
     * @param value A double value to add. May not be {@link Double#isNaN() NaN}
     * or {@link Double#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(int index, double value) {
        DebugUtils.__checkError(Double.isInfinite(value) || Double.isNaN(value), "Forbidden numeric value: " + value);
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
    public final int optInt(int index) {
        return optInt(index, 0);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is an int
     * or can be coerced to an int. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optInt(int)
     */
    public int optInt(int index, int fallback) {
        final Object value = opt(index);
        final Integer result = JSONUtils.toInteger(value);
        return (result != null ? result : fallback);
    }

    /**
     * Equivalent to calling <tt>optLong(index, 0)</tt>.
     * @see #optLong(int, long)
     */
    public final long optLong(int index) {
        return optLong(index, 0);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is a long
     * or can be coerced to a long. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optLong(int)
     */
    public long optLong(int index, long fallback) {
        final Object value = opt(index);
        final Long result  = JSONUtils.toLong(value);
        return (result != null ? result : fallback);
    }

    /**
     * Equivalent to calling <tt>optDouble(index, 0)</tt>.
     * @see #optDouble(int, double)
     */
    public final double optDouble(int index) {
        return optDouble(index, 0);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is a double
     * or can be coerced to a double. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optDouble(int)
     */
    public double optDouble(int index, double fallback) {
        final Object value  = opt(index);
        final Double result = JSONUtils.toDouble(value);
        return (result != null ? result : fallback);
    }

    /**
     * Equivalent to calling <tt>optBoolean(index, false)</tt>.
     * @see #optBoolean(int, boolean)
     */
    public final boolean optBoolean(int index) {
        return optBoolean(index, false);
    }

    /**
     * Returns the value at <em>index</em> if it exists and is a boolean
     * or can be coerced to a boolean. Returns <em>fallback</em> otherwise.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optBoolean(int)
     */
    public boolean optBoolean(int index, boolean fallback) {
        final Object value = opt(index);
        final Boolean result = JSONUtils.toBoolean(value);
        return (result != null ? result : fallback);
    }

    /**
     * Equivalent to calling <tt>optString(index, "")</tt>.
     * @see #optString(int, String)
     */
    public final String optString(int index) {
        return optString(index, "");
    }

    /**
     * Returns the value at <em>index</em> if it exists, coercing it
     * if necessary. Returns <em>fallback</em> if no such value exists.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <em>fallback</em>.
     * @see #optString(int)
     */
    public String optString(int index, String fallback) {
        final Object value  = opt(index);
        final String result = JSONUtils.toString(value);
        return (result != null ? result : fallback);
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
     * Sets the value at the specified <em>index</em> in this array
     * with the specified <em>value</em>.
     * @param index The index of the value.
     * @param value The value to set.
     * @return The previous value at the specified <em>index</em>.
     */
    public Object set(int index, Object value) {
        return values.set(index, value);
    }

    @Override
    public Iterator<Object> iterator() {
        return values.iterator();
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
