package android.ext.temp;

import java.lang.reflect.Array;
import java.util.Arrays;
import android.ext.util.ArrayUtils;

public final class IntArray {
    private int size;
    private int[] data;

    public IntArray() {
    }

    public final void add(int value) {
        final int newSize = size + 1;
        if (newSize > data.length) {
            data = Arrays.copyOfRange(data, 0, newCapacity(newSize, data.length));
        }

        data[size++] = value;
    }

    public final void add(int index, int value) {
        if (index > size || index < 0) {
            // throwIndexOutOfBoundsException(index, size);
        }

        data = (int[])expandCapacity(data, size, data.length, size + 1, index, 1);
        data[index] = value;
        ++size;
    }

    public final void addAll(int[] values) {
        addAll(size, values, 0, values.length);
    }

    public final void addAll(int index, int[] values) {
        addAll(index, values, 0, values.length);
    }

    public final void addAll(int index, int[] values, int offset, int count) {
        ArrayUtils.checkRange(offset, count, values.length);
        if (index > size || index < 0) {
            // throwIndexOutOfBoundsException(index, size);
        }

        if (count > 0) {
            data = (int[])expandCapacity(data, size, data.length, size + count, index, count);
            System.arraycopy(values, offset, data, index, count);
            size += count;
        }
    }

    /* package */ static int newCapacity(int minCapacity, int length) {
        return Math.max(minCapacity, (length * 3) / 2 + 1);
    }

    /* package */ static Object expandCapacity(Object array, int size, int length, int minCapacity, int index, int count) {
        if (minCapacity <= length) {
            System.arraycopy(array, index, array, index + count, size - index);
        } else {
            final Object newArray = Array.newInstance(array.getClass().getComponentType(), newCapacity(minCapacity, length));
            System.arraycopy(array, 0, newArray, 0, index);
            System.arraycopy(array, index, newArray, index + count, size - index);
            array = newArray;
        }

        return array;
    }
}
