package android.ext.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.RandomAccess;

/**
 * Class JSONArray
 * @author Garfield
 */
public class JSONArray implements List<Object>, RandomAccess {
    private final List<Object> values;

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

    @Override
    public void clear() {
        values.clear();
    }

    @Override
    public int size() {
        return values.size();
    }

    @Override
    public boolean isEmpty() {
        return values.isEmpty();
    }

    /**
     * Adds <em>value</em> to the end of this array.
     * @param value A {@link JSONObject}, {@link JSONArray}, <tt>String,
     * Boolean, Integer, Long, Double</tt>, or <tt>null</tt>. May not be
     * {@link Double#isNaN() NaN} or {@link Double#isInfinite() infinite}.
     */
    @Override
    public boolean add(Object value) {
        JSONUtils.__checkDouble(value);
        return values.add(value);
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
     */
    @Override
    public void add(int index, Object value) {
        JSONUtils.__checkDouble(value);
        values.add(index, value);
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

    @Override
    public Object get(int index) {
        return values.get(index);
    }

    @Override
    public Object set(int index, Object value) {
        JSONUtils.__checkDouble(value);
        return values.set(index, value);
    }

    @Override
    public boolean remove(Object value) {
        return values.remove(value);
    }

    /**
     * Removes and returns the value at <em>index</em>, or <tt>null</tt>
     * if this array has no value at <em>index</em>.
     * @param index The index of the value to remove.
     * @return The removed value or <tt>null</tt>.
     */
    @Override
    public Object remove(int index) {
        return (index >= 0 && index < values.size() ? values.remove(index) : null);
    }

    @Override
    public int indexOf(Object value) {
        return values.indexOf(value);
    }

    @Override
    public int lastIndexOf(Object value) {
        return values.lastIndexOf(value);
    }

    @Override
    public Iterator<Object> iterator() {
        return values.iterator();
    }

    @Override
    public ListIterator<Object> listIterator() {
        return values.listIterator();
    }

    @Override
    public ListIterator<Object> listIterator(int index) {
        return values.listIterator(index);
    }

    @Override
    public Object[] toArray() {
        return values.toArray();
    }

    @Override
    public <T> T[] toArray(T[] array) {
        return values.toArray(array);
    }

    @Override
    public List<Object> subList(int start, int end) {
        return values.subList(start, end);
    }

    @Override
    public boolean contains(Object value) {
        return values.contains(value);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        return values.containsAll(collection);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return values.retainAll(collection);
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return values.removeAll(collection);
    }

    @Override
    public boolean addAll(Collection<?> collection) {
        JSONUtils.__checkDouble(collection);
        return values.addAll(collection);
    }

    @Override
    public boolean addAll(int index, Collection<?> collection) {
        JSONUtils.__checkDouble(collection);
        return values.addAll(index, collection);
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
