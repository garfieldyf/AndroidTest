package android.ext.temp;

import java.util.Arrays;
import java.util.List;
import android.ext.util.ArrayUtils;
import android.widget.SectionIndexer;

/**
 * Class ArraySectionIndexer
 * @author Garfield
 * @version 1.0
 */
public class ArraySectionIndexer<T> implements SectionIndexer {
    /**
     * The number of elements in the adapter.
     */
    private final int mCount;

    /**
     * The section array.
     */
    private final T[] mSections;

    /**
     * The start position of per section.
     */
    private final int[] mPositions;

    /**
     * Constructor
     * @param countsOrIndexes The count array or indexes array of the sections.
     * @param count The number of element in the adapter or <tt>-1</tt>, if the
     * <em>countsOrIndexes</em> parameter is a count array.
     * @see #ArraySectionIndexer(List, int)
     * @see #ArraySectionIndexer(T[], List, int)
     * @see #ArraySectionIndexer(T[], int[], int)
     */
    public ArraySectionIndexer(int[] countsOrIndexes, int count) {
        this(null, countsOrIndexes, count);
    }

    /**
     * Constructor
     * @param sections The section array.
     * @param countsOrIndexes The count array or indexes array of the sections.
     * @param count The number of element in the adapter or <tt>-1</tt>, if the
     * <em>countsOrIndexes</em> parameter is a count array.
     * @see #ArraySectionIndexer(List, int)
     * @see #ArraySectionIndexer(int[], int)
     * @see #ArraySectionIndexer(T[], List, int)
     */
    public ArraySectionIndexer(T[] sections, int[] countsOrIndexes, int count) {
        if (sections != null && sections.length != countsOrIndexes.length) {
            throw new IllegalArgumentException("The sections and countsOrIndexes arrays must have the same length");
        }

        mSections  = sections;
        mPositions = countsOrIndexes;
        mCount = computeCount(count);
    }

    /**
     * Constructor
     * @param countsOrIndexes The count list or indexes list of the sections.
     * @param count The number of element in the adapter or <tt>-1</tt>, if
     * the <em>countsOrIndexes</em> parameter is a count list.
     * @see #ArraySectionIndexer(int[], int)
     * @see #ArraySectionIndexer(T[], List, int)
     * @see #ArraySectionIndexer(T[], int[], int)
     */
    public ArraySectionIndexer(List<Integer> countsOrIndexes, int count) {
        this(null, toIntArray(countsOrIndexes), count);
    }

    /**
     * Constructor
     * @param sections The section array.
     * @param countsOrIndexes The count list or indexes list of the sections.
     * @param count The number of element in the adapter or <tt>-1</tt>, if
     * the <em>countsOrIndexes</em> parameter is a count list.
     * @see #ArraySectionIndexer(List, int)
     * @see #ArraySectionIndexer(int[], int)
     * @see #ArraySectionIndexer(T[], int[], int)
     */
    public ArraySectionIndexer(T[] sections, List<Integer> countsOrIndexes, int count) {
        this(sections, toIntArray(countsOrIndexes), count);
    }

    /**
     * Returns the number of sections.
     * @return The number of sections.
     * @see #getSections()
     * @see #getSection(int)
     */
    public final int getSectionCount() {
        return mPositions.length;
    }

    /**
     * Returns the section object at the specified index in this object.
     * @param section The index of the section object to return.
     * @return The section object at the specified index, or <tt>null</tt>
     * if the index out of bounds.
     * @see #getSections()
     * @see #getSectionCount()
     */
    public final T getSection(int section) {
        return (section >= 0 && section < mSections.length ? mSections[section] : null);
    }

    @Override
    public T[] getSections() {
        return mSections;
    }

    @Override
    public int getPositionForSection(int section) {
        return (section >= 0 && section < mPositions.length ? mPositions[section] : -1);
    }

    @Override
    public int getSectionForPosition(int position) {
        if (position < 0 || position >= mCount) {
            return -1;
        }

        final int index = Arrays.binarySearch(mPositions, position);
        return (index >= 0 ? index : -index - 2);
    }

    private int computeCount(int count) {
        if (count == -1) {
            count = 0;
            for (int i = 0, temp = 0; i < mPositions.length; ++i) {
                temp = mPositions[i];
                mPositions[i] = count;
                count += temp;
            }
        }

        return count;
    }

    private static int[] toIntArray(List<Integer> list) {
        final int size = ArrayUtils.getSize(list);
        if (size == 0) {
            return new int[0];
        }

        final int[] result = new int[size];
        for (int i = 0; i < size; ++i) {
            result[i] = list.get(i);
        }

        return result;
    }

//    /**
//     * Closes the stream and releases any system resources associated with it. If the
//     * stream is <tt>null</tt> or already closed then invoking this method has no effect.
//     * @param is An InputStream is a source or destination of data that can be closed.
//     * @param forceClose If <tt>true</tt> prevents re-use of the underlying connection
//     * of the stream, <tt>false</tt> otherwise. If the <em>is</em> parameter is <b>not</b>
//     * an {@link EofSensorInputStream}, this parameter is ignored.
//     */
//    public static void close(InputStream is, boolean forceClose) {
//        if (is != null) {
//            try {
//                // Aborts the stream. calling the abortConnection() method
//                // prevents re-use of the underlying connection.
//                if (forceClose && is instanceof EofSensorInputStream) {
//                    ((EofSensorInputStream)is).abortConnection();
//                }
//
//                // Closes the stream and release any system resources it holds.
//                is.close();
//            } catch (IOException e) {
//                Log.e(FileUtils.class.getName(), "Couldn't close - " + is.getClass().getName(), e);
//            }
//        }
//    }
}
