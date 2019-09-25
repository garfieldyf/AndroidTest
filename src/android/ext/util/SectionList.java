package android.ext.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import android.util.Printer;

/**
 * Class <tt>SectionList</tt> allows to adding data by section.
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public class SectionList<E> implements Cloneable {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private int mItemCount;
    private int[] mPositions;
    private int mSectionCount;
    private Object[] mSections;

    /**
     * Constructor
     * @see #SectionList(int)
     * @see #SectionList(Collection)
     */
    public SectionList() {
        this(0);
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this <tt>SectionList</tt>.
     * @see #SectionList()
     * @see #SectionList(Collection)
     */
    public SectionList(int capacity) {
        DebugUtils.__checkError(capacity < 0, "capacity < 0");
        if (capacity == 0) {
            mPositions = EMPTY_INT_ARRAY;
            mSections  = EMPTY_OBJECT_ARRAY;
        } else {
            mPositions = new int[capacity];
            mSections  = new Object[capacity];
        }
    }

    /**
     * Constructor
     * @param sections The collection of {@link Section}s to add.
     * @see #SectionList()
     * @see #SectionList(int)
     */
    public SectionList(Collection<? extends Section<? extends E>> sections) {
        this(sections.size());

        // Adds the section collection to mSections.
        for (Section<? extends E> section : sections) {
            mSections[mSectionCount] = section;
            mPositions[mSectionCount++] = mItemCount;
            mItemCount += section.size();
        }
    }

    @Override
    public SectionList<E> clone() {
        try {
            final SectionList<E> result = (SectionList<E>)super.clone();
            result.mSections  = mSections.clone();
            result.mPositions = mPositions.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * Removes all items from this <tt>SectionList</tt>, leaving it empty.
     * @see #getItemCount()
     */
    public void clear() {
        if (mItemCount > 0) {
            Arrays.fill(mSections, 0, mSectionCount, null);
            mItemCount = mSectionCount = 0;
        }
    }

    /**
     * Returns the number of items in this <tt>SectionList</tt>.
     * @return The number of items in this <tt>SectionList</tt>.
     * @see #getItem(int)
     */
    public int getItemCount() {
        return mItemCount;
    }

    /**
     * Returns the item at the specified <em>position</em> in this <tt>SectionList</tt>.
     * @param position The index of the item.
     * @return The item at the specified <em>position</em>.
     * @see #getItemCount()
     */
    public E getItem(int position) {
        final int sectionIndex = getSectionForPosition(position);
        return ((Section<E>)mSections[sectionIndex]).get(position - mPositions[sectionIndex]);
    }

    /**
     * Returns the number of sections in this <tt>SectionList</tt>.
     * @return The number of sections in this <tt>SectionList</tt>.
     * @see #getSection(int)
     */
    public int getSectionCount() {
        return mSectionCount;
    }

    /**
     * Returns the {@link Section} at the specified <em>sectionIndex</em> in this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @return The {@link Section} at the specified <em>sectionIndex</em>.
     * @see #getSectionCount()
     * @see #setSection(int, Section)
     */
    public Section<E> getSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        return (Section<E>)mSections[sectionIndex];
    }

    /**
     * Replaces the section at the specified <em>sectionIndex</em> in this
     * <tt>SectionList</tt> with the specified <em>section</em>.
     * @param sectionIndex The index at which to put the <em>section</em>.
     * @param section The {@link Section} to put.
     * @return The previous <tt>Section</tt> at the <em>sectionIndex</em>.
     * @see #getSection(int)
     */
    public Section<E> setSection(int sectionIndex, Section<? extends E> section) {
        DebugUtils.__checkError(getSize(section) == 0, "The section is null or 0-size");
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        final Section<E> oldSection = (Section<E>)mSections[sectionIndex];
        final int newSize = section.size();
        final int oldSize = oldSection.size();
        mSections[sectionIndex] = section;

        if (oldSize != newSize) {
            mItemCount -= oldSize;
            mItemCount += newSize;
            computePositions(sectionIndex);
        }

        return oldSection;
    }

    /**
     * Adds the specified <em>section</em> at the end of this <tt>SectionList</tt>.
     * @param section The {@link Section} to add.
     * @see #addSection(int, Section)
     */
    public void addSection(Section<? extends E> section) {
        DebugUtils.__checkError(getSize(section) == 0, "The section is null or 0-size");
        if (mSectionCount == mSections.length) {
            final int newLength = mSectionCount + ARRAY_CAPACITY_INCREMENT;
            mSections  = ArrayUtils.copyOf(mSections, mSectionCount, newLength);
            mPositions = ArrayUtils.copyOf(mPositions, mSectionCount, newLength);
        }

        mSections[mSectionCount] = section;
        mPositions[mSectionCount++] = mItemCount;
        mItemCount += section.size();
    }

    /**
     * Inserts the specified <em>section</em> into this <tt>SectionList</tt> at the specified <em>sectionIndex</em>. The <em>section</em>
     * is inserted before the current section at the specified <em>sectionIndex</em>. If the <em>sectionIndex</em> is equal to the section
     * count of this <tt>SectionList</tt>, the <em>section</em> is added at the end.
     * @param sectionIndex The index at which to insert.
     * @param section The {@link Section} to add.
     * @see #addSection(Section)
     */
    public void addSection(int sectionIndex, Section<? extends E> section) {
        DebugUtils.__checkError(getSize(section) == 0, "The section is null or 0-size");
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex > mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        if (sectionIndex == mSectionCount) {
            addSection(section);
        } else {
            if (mSectionCount < mSections.length) {
                System.arraycopy(mSections, sectionIndex, mSections, sectionIndex + 1, mSectionCount - sectionIndex);
            } else {
                final int newLength = mSectionCount + ARRAY_CAPACITY_INCREMENT;
                mSections  = newSectionArray(sectionIndex, newLength);
                mPositions = ArrayUtils.copyOf(mPositions, mSectionCount, newLength);
            }

            ++mSectionCount;
            mSections[sectionIndex] = section;
            computePositions(sectionIndex);
            mItemCount += section.size();
        }
    }

    /**
     * Removes the section at the specified <em>sectionIndex</em> from this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section to remove.
     * @return The start position of the removed section in this <tt>SectionList</tt>.
     */
    public int removeSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        mItemCount -= ((Section<?>)mSections[sectionIndex]).size();
        System.arraycopy(mSections, sectionIndex + 1, mSections, sectionIndex, --mSectionCount - sectionIndex);
        mSections[mSectionCount] = null;  // Prevent memory leak.

        // Computes all sections start position from the removed section index.
        return computePositions(sectionIndex);
    }

    /**
     * Given a position within this <tt>SectionList</tt>, returns the
     * index of the corresponding section within the array of sections.
     * @param position The position within this <tt>SectionList</tt>.
     * @return The index of the section.
     * @see #getPositionForSection(int)
     */
    public int getSectionForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position - " + position + ", itemCount = " + mItemCount);
        final int sectionIndex = Arrays.binarySearch(mPositions, 0, mSectionCount, position);
        return (sectionIndex >= 0 ? sectionIndex : -sectionIndex - 2);
    }

    /**
     * Given the index of a section within the array of sections, returns the starting
     * position of that section within this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @return The starting position of that section within this <tt>SectionList</tt>.
     * @see #getSectionForPosition(int)
     */
    public int getPositionForSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        return mPositions[sectionIndex];
    }

    /**
     * Returns a new array containing all items contained in this <tt>SectionList</tt>.
     * @return An array of the items from this <tt>SectionList</tt>.
     */
    public Object[] toArray() {
        return copyTo(new Object[mItemCount]);
    }

    /**
     * Returns an array containing all items contained in this <tt>SectionList</tt>.
     * @param contents The array.
     * @return An array of the items from this <tt>SectionList</tt>.
     */
    public E[] toArray(E[] contents) {
        if (contents.length < mItemCount) {
            contents = (E[])Array.newInstance(contents.getClass().getComponentType(), mItemCount);
        }

        if (copyTo(contents).length > mItemCount) {
            contents[mItemCount] = null;
        }

        return contents;
    }

    @SuppressWarnings("resource")
    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(100);
        final Formatter formatter  = new Formatter(result);
        DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ itemCount = %d, sectionCount = %d ] ", getClass().getSimpleName(), mItemCount, mSectionCount);

        for (int i = 0; i < mSectionCount; ++i) {
            final Section<?> section = (Section<?>)mSections[i];
            result.setLength(0);

            final int startPos = mPositions[i];
            final int size = section.size();
            formatter.format("  Section %-2d ==> ", i);
            printer.println(DebugUtils.toString(section, result).append(" { startPos = ").append(startPos).append(", endPos = ").append(startPos + size - 1).append(", size = ").append(size).append(" }").toString());
        }
    }

    /**
     * Returns the numbers of items in the section, handling <tt>null Section</tt>.
     * @param section The {@link Section}.
     * @return The numbers of items in the <em>section</em>.
     */
    public static int getSize(Section<?> section) {
        return (section != null ? section.size() : 0);
    }

    private Object[] copyTo(Object[] result) {
        for (int i = 0, index = 0; i < mSectionCount; ++i) {
            final Section<?> section = (Section<?>)mSections[i];
            for (int j = 0, size = section.size(); j < size; ++j) {
                result[index++] = section.get(j);
            }
        }

        return result;
    }

    private int computePositions(int sectionIndex) {
        final int startPosition = mPositions[sectionIndex];
        for (int position = startPosition; sectionIndex < mSectionCount; ++sectionIndex) {
            mPositions[sectionIndex] = position;
            position += ((Section<?>)mSections[sectionIndex]).size();
        }

        return startPosition;
    }

    private Object[] newSectionArray(int sectionIndex, int newLength) {
        final Object[] newSections = new Object[newLength];
        System.arraycopy(mSections, 0, newSections, 0, sectionIndex);
        System.arraycopy(mSections, sectionIndex, newSections, sectionIndex + 1, mSectionCount - sectionIndex);
        return newSections;
    }

    /**
     * A <tt>Section</tt> is a collection used to adds the data to the {@link SectionList}.
     */
    public static interface Section<E> {
        /**
         * Returns the total number of items in this section.
         * @return The total number of items in this section.
         * @see #get(int)
         */
        int size();

        /**
         * Returns the item at the specified <em>position</em> in this section.
         * @param position The index of the item.
         * @return The item at the specified <em>position</em>.
         * @see #size()
         */
        E get(int position);
    }

    /**
     * Class <tt>ArrayListSection</tt> is an implementation of a {@link Section}.
     */
    public static final class ArrayListSection<E> extends ArrayList<E> implements Section<E> {
        private static final long serialVersionUID = -7733674144000974287L;

        public ArrayListSection() {
            super();
        }

        public ArrayListSection(int capacity) {
            super(capacity);
        }

        public ArrayListSection(Collection<? extends E> collection) {
            super(collection);
        }
    }
}
