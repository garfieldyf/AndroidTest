package com.tencent.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Algorithms {
    private static final int[] NUMBERS = {
        0, 8, 5, 41, 1, 0, 9, 60, 121, 43, -1, 454, 967,
    };

    public static void remove() {
        // 5, 41, 0, 60, 43, -1, 967
        final int[] indexes = { 1, 6, 8, 4, 0, 11 };
        Arrays.sort(indexes);

        final List<Integer> numbers = new ArrayList<>(NUMBERS.length);
        for (int i = 0; i < NUMBERS.length; ++i) {
            numbers.add(NUMBERS[i]);
        }

        int count = 0;
        for (int i = 0; i < indexes.length; ++i, ++count) {
            numbers.remove(indexes[i] - count);
        }

        System.out.println(numbers);
    }

    public static void maxRange(int w) {
        if (w < 1 || w > NUMBERS.length) {
            System.out.println("Invalid window size = " + w);
            return;
        }

        if (w == 1) {
            System.out.println(Arrays.toString(NUMBERS));
            return;
        }

        final int length = NUMBERS.length - w + 1;
        final int[] result = new int[length];
        int max, index = 0;
        for (int i = 0; i < length; ++i) {
            max = NUMBERS[i];
            for (int j = 1; j < w; ++j) {
                final int v = NUMBERS[i + j];
                if (max < v) {
                    max = v;
                }
            }

            result[index++] = max;
        }

        System.out.println(Arrays.toString(result));
    }

    private static final String STRING = "fjjkkkdsaaaaab"; // f2j3kds5ab  fjkdsab

    public static void unique() {
        final int length = STRING.length();
        if (length <= 1) {
            System.out.println(STRING);
        }

        final StringBuilder s = new StringBuilder();
        char prev = 0;
        for (int i = 0; i < length; ++i) {
            final char ch = STRING.charAt(i);
            if (prev != ch) {
                s.append(ch);
            }

            prev = ch;
        }

        System.out.println(s);
    }

    public static void compress() {
        final int length = STRING.length();
        if (length <= 1) {
            System.out.println(STRING);
        }

        int repeatCount = 0;
        char next = 0;
        final StringBuilder s = new StringBuilder();

        for (int i = 0; i < length - 1; ++i) {
            final char curr = STRING.charAt(i);
            next = STRING.charAt(i + 1);
            if (curr == next) {
                ++repeatCount;
            } else {
                if (repeatCount == 0) {
                    s.append(curr);
                } else {
                    s.append(repeatCount + 1).append(curr);
                }

                repeatCount = 0;
            }
        }

        if (repeatCount == 0) {
            s.append(next);
        } else {
            s.append(repeatCount + 1).append(next);
        }

        System.out.println(s);
    }
}
