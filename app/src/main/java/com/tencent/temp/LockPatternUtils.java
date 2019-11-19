package com.tencent.temp;

import android.ext.util.ArrayUtils;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class LockPatternUtils {
    public static String flatten(List<Cell> pattern) {
        final int size = ArrayUtils.getSize(pattern);
        if (size == 0) {
            return "";
        }

        final byte[] data = new byte[size];
        for (int i = 0; i < size; ++i) {
            final Cell cell = pattern.get(i);
            data[i] = (byte)(cell.row * 3 + cell.col);
        }

        return new String(data);
    }

    public static List<Cell> unflatten(String flattened) {
        if (TextUtils.isEmpty(flattened)) {
            return Collections.emptyList();
        }

        final List<Cell> result = new ArrayList<Cell>(9);
        final byte[] data = flattened.getBytes();
        for (int i = 0, index = 0; i < data.length; ++i) {
            index = data[i];
            result.add(Cell.obtain(index / 3, index % 3));
        }

        return result;
    }

    /**
     * Nested class Cell
     */
    public static final class Cell {
        public final int row;
        public final int col;

        public static Cell obtain(int row, int col) {
            return sCells[row][col];
        }

        private Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public String toString() {
            return new StringBuilder(32)
                .append("{ row = ").append(row)
                .append(", col = ").append(col)
                .append(" }").toString();
        }
    }

    /**
     * Keeps the <tt>Cell</tt> objects limited to 9.
     */
    private static final Cell[][] sCells = new Cell[3][3];

    static {
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                sCells[i][j] = new Cell(i, j);
            }
        }
    }

    /**
     * This utility class cannot be instantiated.
     */
    private LockPatternUtils() {
    }
}
