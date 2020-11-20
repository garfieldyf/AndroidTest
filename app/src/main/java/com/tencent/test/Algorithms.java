package com.tencent.test;

import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Algorithms {
    private static final int[] NUMBERS = {
        0, 8, 5, 41, 1, 7, 9, 60, 121, 43, -1, 454, 967,
    };

    public static class Node {
        public int value;
        public Node next;
    }

    ////////////////////////////////// LinkedList //////////////////////////////////

    private static Node make() {
        Node head = null;
        for (int i = 0; i < 6; ++i) {
            Node node  = new Node();
            node.value = NUMBERS[i];
            node.next  = head;
            head = node;
        }

        return head;
    }

    private static void print(Node head) {
        StringBuilder s = new StringBuilder();
        while (head != null) {
            s.append(head.value).append(' ');
            head = head.next;
        }

        Log.d("abcd", "print = " + s.toString());
    }

    private static Node reverse(Node head) {
        Node newHead = null;
        while (head != null) {
            final Node node = head;
            head = head.next;
            node.next = newHead;
            newHead = node;
        }

        return newHead;
    }

    public static void reverse() {
        final Node head = make();
        // 7 1 41 5 8 0
        print(head);

        final Node newHead = reverse(head);
        // 0 8 5 41 1 7
        print(newHead);
    }

    ////////////////////////////////// CircularLinkedList //////////////////////////////////

    private static Node makeCircular() {
        Node head = null, last = null;
        for (int i = 0; i < 6; ++i) {
            Node node = new Node();
            node.value = NUMBERS[i];
            node.next = head;
            head = node;
            if (i == 0) {
                last = node;
            }
        }

        last.next = head;
        return head;
    }

    private static void printCircular(Node head) {
        if (head == null) {
            return;
        }

        StringBuilder s = new StringBuilder();
        Node node = head;
        do {
            s.append(node.value).append(' ');
            node = node.next;
        } while (head != node);

        Log.d("abcd", "printCircular = " + s.toString());
    }

    private static Node reverseCircular(Node head) {
        if (head == null) {
            return null;
        }

        Node newHead = null, start = head;
        do {
            final Node node = head;
            head = head.next;
            node.next = newHead;
            newHead = node;
        } while (start != head);

        // 0 8 5 41 1 7
        head.next = newHead;
        return newHead;
    }

    public static void reverseCircular() {
        final Node head = makeCircular();
        // 7 1 41 5 8 0
        printCircular(head);

        final Node newHead = reverseCircular(head);
        // 0 8 5 41 1 7
        printCircular(newHead);
    }

    ////////////////////////////////// binarySearch //////////////////////////////////

    public static int binarySearch(int key) {
        final int[] numbers = NUMBERS.clone();
        Arrays.sort(numbers);

        int low = 0, high = numbers.length - 1;
        while (low <= high) {
            final int mid = (low + high) / 2;
            final int value = numbers[mid];

            if (value < key) {
                low = mid + 1;
            } else if (value > key) {
                high = mid - 1;
            } else {
                return mid;
            }
        }

        return -1;
    }

    ////////////////////////////////// remove //////////////////////////////////

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

    ////////////////////////////////// maxRange //////////////////////////////////

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

    ////////////////////////////////// unique //////////////////////////////////

    private static final String STRING = "fjjkkkdsaaaaab";

    public static void unique() {
        // fjkdsab
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

    ////////////////////////////////// compress //////////////////////////////////

    public static void compress() {
        // f2j3kds5ab
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

    ////////////////////////////////// big integer add //////////////////////////////////

    private static final int[] BIG_INTEGER1 = { 7, 5, 3, 4, 6, 1, 9, 4, 3 };
    private static final int[] BIG_INTEGER2 = { 3, 7, 9, 0, 2, 1, 8 };

    private static Node create(int[] numbers) {
        Node head = null;
        for (int i = 0; i < numbers.length; ++i) {
            Node node  = new Node();
            node.value = numbers[i];
            node.next  = head;
            head = node;
        }

        return head;
    }

    private static void printList(Node head) {
        StringBuilder s = new StringBuilder();
        while (head != null) {
            s.append(head.value);
            head = head.next;
        }

        Log.d("abcd", "printList = " + s.toString());
    }

    public static void add() {
        /*
         *  349164357
         *          +
         *    8120973
         * -----------
         *  357285330
         *
         *    9164357
         *          +
         *    8120973
         * -----------
         *   17285330
         */
        Node head1 = create(BIG_INTEGER1);
        Node head2 = create(BIG_INTEGER2);
        printList(head1);
        printList(head2);

        Node rhead1 = reverse(head1);
        Node rhead2 = reverse(head2);

        int carry = 0;
        Node newHead = null;
        while (rhead1 != null || rhead2 != null) {
            int value1 = 0;
            if (rhead1 != null) {
                value1 = rhead1.value;
                rhead1 = rhead1.next;
            }

            int value2 = 0;
            if (rhead2 != null) {
                value2 = rhead2.value;
                rhead2 = rhead2.next;
            }

            int result = value1 + value2 + carry;
            if (result >= 10) {
                carry = 1;
                result -= 10;
            } else {
                carry = 0;
            }

            Node node  = new Node();
            node.value = result;
            node.next  = newHead;
            newHead = node;
        }

        if (carry != 0) {
            Node node  = new Node();
            node.value = carry;
            node.next  = newHead;
            newHead = node;
        }

        printList(newHead);
    }
}
