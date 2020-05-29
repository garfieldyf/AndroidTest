package android.ext.json;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Class JSONArray
 * @author Garfield
 */
public class JSONArray extends ArrayList<Object> {
    /**
     * Constructor
     * @see #JSONArray(int)
     * @see #JSONArray(Collection)
     */
    public JSONArray() {
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this array.
     * @see #JSONArray()
     * @see #JSONArray(Collection)
     */
    public JSONArray(int capacity) {
        super(capacity);
    }

    /**
     * Constructor
     * @param values The values to add.
     * @see #JSONArray()
     * @see #JSONArray(int)
     */
    public JSONArray(Collection<?> values) {
        super(values);
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value An integer value to add.
     * @return This array.
     */
    public JSONArray add(int value) {
        __checkMutable();
        super.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A long value to add.
     * @return This array.
     */
    public JSONArray add(long value) {
        __checkMutable();
        super.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A boolean value to add.
     * @return This array.
     */
    public JSONArray add(boolean value) {
        __checkMutable();
        super.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A float value to add. May not be {@link Float#isNaN() NaN}
     * or {@link Float#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(float value) {
        __checkMutable();
        JSONUtils.__checkDouble(value);
        super.add(value);
        return this;
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A double value to add. May not be {@link Double#isNaN() NaN}
     * or {@link Double#isInfinite() infinite}.
     * @return This array.
     */
    public JSONArray add(double value) {
        __checkMutable();
        JSONUtils.__checkDouble(value);
        super.add(value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value An integer value to add.
     * @return This array.
     */
    public JSONArray add(int index, int value) {
        __checkMutable();
        super.add(index, value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value A long value to add.
     * @return This array.
     */
    public JSONArray add(int index, long value) {
        __checkMutable();
        super.add(index, value);
        return this;
    }

    /**
     * Inserts <em>value</em> into this array at the specified <em>index</em>.
     * @param index The index at which to insert.
     * @param value A boolean value to add.
     * @return This array.
     */
    public JSONArray add(int index, boolean value) {
        __checkMutable();
        super.add(index, value);
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
        __checkMutable();
        JSONUtils.__checkDouble(value);
        super.add(index, value);
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
        __checkMutable();
        JSONUtils.__checkDouble(value);
        super.add(index, value);
        return this;
    }

    /**
     * Returns the value at <em>index</em>, or <tt>null</tt>
     * if this array has no value at <em>index</em>.
     * @param index The index of the value.
     * @return The value at <em>index</em> or <tt>null</tt>.
     */
    public Object opt(int index) {
        return (index >= 0 && index < size() ? get(index) : null);
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
     * Removes and returns the value at <em>index</em>, or <tt>null</tt>
     * if this array has no value at <em>index</em>.
     * @param index The index of the value to remove.
     * @return The removed value or <tt>null</tt>.
     */
    @Override
    public Object remove(int index) {
        __checkMutable();
        return (index >= 0 && index < size() ? super.remove(index) : null);
    }

    /**
     * Removes from this array all of the values whose index is between
     * <em>fromIndex</em> , inclusive, and <em>toIndex</em>, exclusive.
     * If <em>fromIndex==toIndex</em>, this operation has no effect.
     */
    @Override
    public void removeRange(int fromIndex, int toIndex) {
        // The removeRange is protected in the super class.
        __checkMutable();
        super.removeRange(fromIndex, toIndex);
    }

    @Override
    public String toString() {
        return JSONUtils.toJSONString(this);
    }

    @Override
    public boolean equals(Object object) {
        return (object instanceof JSONArray && super.equals(object));
    }

    private void __checkMutable() {
        if (this instanceof JSONUtils.EmptyJSONArray) {
            throw new UnsupportedOperationException("The JSONArray is immutable");
        }
    }
}
