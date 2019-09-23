package android.ext.util;

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
            mItemCount += section.getCount();
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
        return ((Section<E>)mSections[sectionIndex]).getItem(position - mPositions[sectionIndex]);
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
        DebugUtils.__checkError(getCount(section) == 0, "The section is null or 0-count");
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        final Section<E> oldSection = (Section<E>)mSections[sectionIndex];
        final int newCount = section.getCount();
        final int oldCount = oldSection.getCount();
        mSections[sectionIndex] = section;

        if (oldCount != newCount) {
            mItemCount -= oldCount;
            mItemCount += newCount;
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
        DebugUtils.__checkError(getCount(section) == 0, "The section is null or 0-count");
        if (mSectionCount == mSections.length) {
            final int newLength = mSectionCount + ARRAY_CAPACITY_INCREMENT;
            mSections  = ArrayUtils.copyOf(mSections, mSectionCount, newLength);
            mPositions = ArrayUtils.copyOf(mPositions, mSectionCount, newLength);
        }

        mSections[mSectionCount] = section;
        mPositions[mSectionCount++] = mItemCount;
        mItemCount += section.getCount();
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
        DebugUtils.__checkError(getCount(section) == 0, "The section is null or 0-count");
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
            mItemCount += section.getCount();
        }
    }

    /**
     * Removes the section at the specified <em>sectionIndex</em> from this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section to remove.
     * @return The start position of the removed section in this <tt>SectionList</tt>.
     */
    public int removeSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        mItemCount -= ((Section<?>)mSections[sectionIndex]).getCount();
        System.arraycopy(mSections, sectionIndex + 1, mSections, sectionIndex, --mSectionCount - sectionIndex);
        mSections[mSectionCount] = null;  // Prevent memory leak.

        // Computes all sections start position from the removed section index.
        return computePositions(sectionIndex);
    }

    /**
     * Returns the index of the section with the given the <em>position</em>.
     * @param position The position of the item in this <tt>SectionList</tt>.
     * @return The index of the section.
     * @see #getPositionForSection(int, int)
     */
    public int getSectionForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position - " + position + ", itemCount = " + mItemCount);
        final int sectionIndex = Arrays.binarySearch(mPositions, 0, mSectionCount, position);
        return (sectionIndex >= 0 ? sectionIndex : -sectionIndex - 2);
    }

    /**
     * Returns the position with the given <em>sectionIndex</em> and <em>sectionPosition</em>.
     * @param sectionIndex The index of the section.
     * @param sectionPosition The index of the item in the section.
     * @return The position of the item in this <tt>SectionList</tt>.
     * @see #getSectionForPosition(int)
     */
    public int getPositionForSection(int sectionIndex, int sectionPosition) {
        DebugUtils.__checkError(sectionPosition < 0, "Invalid sectionPosition - " + sectionPosition);
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex - " + sectionIndex + ", sectionCount = " + mSectionCount);
        return (mPositions[sectionIndex] + sectionPosition);
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
            final int count = section.getCount();
            formatter.format("  Section %-2d ==> ", i);
            printer.println(DebugUtils.toString(section, result).append(" { startPos = ").append(startPos).append(", endPos = ").append(startPos + count - 1).append(", count = ").append(count).append(" }").toString());
        }
    }

    /**
     * Returns the numbers of items in the section, handling <tt>null Section</tt>.
     * @param section The {@link Section}.
     * @return The numbers of items in the <em>section</em>.
     */
    public static int getCount(Section<?> section) {
        return (section != null ? section.getCount() : 0);
    }

    private int computePositions(int sectionIndex) {
        final int startPosition = mPositions[sectionIndex];
        for (int position = startPosition; sectionIndex < mSectionCount; ++sectionIndex) {
            mPositions[sectionIndex] = position;
            position += ((Section<?>)mSections[sectionIndex]).getCount();
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
    public interface Section<E> {
        /**
         * Returns the total number of items in this section.
         * @return The total number of items in this section.
         * @see #getItem(int)
         */
        int getCount();

        /**
         * Returns the item at the specified <em>position</em> in this section.
         * @param position The position of the item.
         * @return The item at the specified <em>position</em>.
         * @see #getCount()
         */
        E getItem(int position);
    }
}
