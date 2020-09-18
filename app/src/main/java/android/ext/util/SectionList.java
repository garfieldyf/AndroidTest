package android.ext.util;

import android.util.Printer;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

/**
 * Class <tt>SectionList</tt> allows to adding data by section. This
 * class does not support adding or removing the item, but the items
 * can be set. Setting an item modifies the underlying list.
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class SectionList<E> extends AbstractList<E> implements Cloneable {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int ARRAY_INITIAL_CAPACITY   = 8;

    /* package */ int mSize;
    /* package */ int mCount;
    /* package */ int[] mIndexes;
    /* package */ List[] mSections;

    /**
     * Constructor
     * @see #SectionList(int)
     */
    public SectionList() {
        this(new int[ARRAY_INITIAL_CAPACITY]);
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this <tt>SectionList</tt>.
     * @see #SectionList()
     */
    public SectionList(int capacity) {
        this(new int[capacity > 0 ? capacity : ARRAY_INITIAL_CAPACITY]);
    }

    /**
     * Constructor
     */
    private SectionList(int[] indexes) {
        mIndexes  = indexes;
        mSections = new List[indexes.length];
    }

    @Override
    public void clear() {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The SectionList is immutable");
        if (mSize > 0) {
            Arrays.fill(mSections, 0, mCount, null);
            mSize  = 0;
            mCount = 0;
        }
    }

    @Override
    public int size() {
        return mSize;
    }

    @Override
    public boolean isEmpty() {
        return (mSize == 0);
    }

    @Override
    public E get(int index) {
        final int sectionIndex = getSectionForPosition(index);
        return (E)mSections[sectionIndex].get(index - mIndexes[sectionIndex]);
    }

    @Override
    public E set(int index, E value) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The SectionList is immutable");
        final int sectionIndex = getSectionForPosition(index);
        return (E)mSections[sectionIndex].set(index - mIndexes[sectionIndex], value);
    }

    /**
     * Returns the number of sections in this <tt>SectionList</tt>.
     * @return The number of sections in this <tt>SectionList</tt>.
     * @see #getSection(int)
     */
    public int getSectionCount() {
        return mCount;
    }

    /**
     * Returns the section at the specified <em>sectionIndex</em> in this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @return The {@link List} at the specified <em>sectionIndex</em>.
     * @see #getSectionCount()
     * @see #setSection(int, List)
     */
    public List<E> getSection(int sectionIndex) {
        checkSectionIndex(sectionIndex);
        return mSections[sectionIndex];
    }

    /**
     * Replaces the section at the specified <em>sectionIndex</em>
     * in this <tt>SectionList</tt>.
     * @param sectionIndex The index at which to set the section.
     * @param section The {@link List} to set.
     * @return The previous section at the <em>sectionIndex</em>.
     * @see #getSection(int)
     */
    public List<E> setSection(int sectionIndex, List<?> section) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The SectionList is immutable");
        DebugUtils.__checkError(ArrayUtils.getSize(section) == 0, "Invalid parameter - The section is null or 0-size");
        checkSectionIndex(sectionIndex);
        final List<E> oldSection = mSections[sectionIndex];
        mSections[sectionIndex] = section;

        final int sizeDelta = section.size() - oldSection.size();
        if (sizeDelta != 0) {
            mSize += sizeDelta;
            DebugUtils.__checkError(mSize < 0, "Error: The SectionList's size(" + mSize + ") < 0");
            updateIndexes(sectionIndex);
        }

        return oldSection;
    }

    /**
     * Adds the specified <em>section</em> at the end of this <tt>SectionList</tt>.
     * @param section The {@link List} to add.
     * @see #addSection(int, List)
     */
    public void addSection(List<?> section) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The SectionList is immutable");
        DebugUtils.__checkError(ArrayUtils.getSize(section) == 0, "Invalid parameter - The section is null or 0-size");
        if (mCount == mSections.length) {
            final int newLength = mCount + ARRAY_CAPACITY_INCREMENT;
            mIndexes  = ArrayUtils.copyOf(mIndexes, mCount, newLength);
            mSections = ArrayUtils.copyOf(mSections, mCount, newLength);
        }

        mSections[mCount] = section;
        mIndexes[mCount++] = mSize;
        mSize += section.size();
    }

    /**
     * Inserts the specified <em>section</em> into this <tt>SectionList</tt> at the specified <em>sectionIndex</em>. The <em>section</em>
     * is inserted before the current section at the specified <em>sectionIndex</em>. If the <em>sectionIndex</em> is equal to the section
     * count of this <tt>SectionList</tt>, the <em>section</em> is added at the end.
     * @param sectionIndex The index at which to insert.
     * @param section The {@link List} to add.
     * @see #addSection(List)
     */
    public void addSection(int sectionIndex, List<?> section) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The SectionList is immutable");
        DebugUtils.__checkError(ArrayUtils.getSize(section) == 0, "Invalid parameter - The section is null or 0-size");
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex > mCount, "Invalid parameter - sectionIndex out of bounds [ sectionIndex = " + sectionIndex + ", sectionCount = " + mCount + " ]");
        if (sectionIndex == mCount) {
            addSection(section);
        } else {
            if (mCount < mSections.length) {
                System.arraycopy(mSections, sectionIndex, mSections, sectionIndex + 1, mCount - sectionIndex);
            } else {
                final int newLength = mCount + ARRAY_CAPACITY_INCREMENT;
                mSections = newSectionArray(sectionIndex, newLength);
                mIndexes  = ArrayUtils.copyOf(mIndexes, mCount, newLength);
            }

            ++mCount;
            mSections[sectionIndex] = section;
            mSize += section.size();
            updateIndexes(sectionIndex);
        }
    }

    /**
     * Removes the section at the specified <em>sectionIndex</em> from this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section to remove.
     * @return The start index of the removed section in this <tt>SectionList</tt>.
     */
    public int removeSection(int sectionIndex) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The SectionList is immutable");
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mCount, "Invalid parameter - sectionIndex out of bounds [ sectionIndex = " + sectionIndex + ", sectionCount = " + mCount + " ]");
        mSize -= mSections[sectionIndex].size();
        System.arraycopy(mSections, sectionIndex + 1, mSections, sectionIndex, --mCount - sectionIndex);
        mSections[mCount] = null;  // Prevent memory leak.

        // Computes all pages start index from the removed section index.
        DebugUtils.__checkError(mSize < 0, "Error: The SectionList's size(" + mSize + ") < 0");
        DebugUtils.__checkError(mCount < 0, "Error: The SectionList's sectionCount(" + mCount + ") < 0");
        return updateIndexes(sectionIndex);
    }

    /**
     * Given a position within this <tt>SectionList</tt>, returns the index of the
     * section within the array of sections.
     * @param index The position of the item within this <tt>SectionList</tt>.
     * @return The index of the section within the array of sections.
     * @see #getPositionForSection(int)
     */
    public int getSectionForPosition(int index) {
        if (index < 0 || index >= mSize) {
            throw new IndexOutOfBoundsException("Invalid parameter - index out of bounds [ index = " + index + ", size = " + mSize + " ]");
        }

        final int sectionIndex = Arrays.binarySearch(mIndexes, 0, mCount, index);
        return (sectionIndex >= 0 ? sectionIndex : -sectionIndex - 2);
    }

    /**
     * Given the index of a section within the array of sections, returns the starting
     * index of that section within this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @return The starting index of that section within this <tt>SectionList</tt>.
     * @see #getSectionForPosition(int)
     */
    public int getPositionForSection(int sectionIndex) {
        checkSectionIndex(sectionIndex);
        return mIndexes[sectionIndex];
    }

    /**
     * Returns a type-safe empty, immutable {@link SectionList}.
     * @return An empty {@link SectionList}.
     */
    public static <E> SectionList<E> emptyList() {
        return EMPTY_IMMUTABLE_LIST;
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(128);
        final Formatter formatter  = new Formatter(result);
        DeviceUtils.dumpSummary(printer, result, 100, " Dumping %s [ size = %d, sectionCount = %d ] ", getClass().getSimpleName(), mSize, mCount);

        for (int i = 0; i < mCount; ++i) {
            final List section = mSections[i];
            result.setLength(0);

            final int start = mIndexes[i];
            final int size  = section.size();
            formatter.format("  Section %-2d ==> ", i);
            printer.println(DeviceUtils.toString(section, result).append(" { start = ").append(start).append(", end = ").append(start + size - 1).append(", size = ").append(size).append(" }").toString());
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new SectionListIterator();
    }

    @Override
    public boolean contains(Object value) {
        return (indexOf(value) != -1);
    }

    @Override
    public int indexOf(Object value) {
        for (int i = 0; i < mCount; ++i) {
            final int index = mSections[i].indexOf(value);
            if (index != -1) {
                return mIndexes[i] + index;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object value) {
        for (int i = mCount - 1; i >= 0; --i) {
            final int index = mSections[i].lastIndexOf(value);
            if (index != -1) {
                return mIndexes[i] + index;
            }
        }

        return -1;
    }

    @Override
    public SectionList<E> clone() {
        try {
            final SectionList<E> result = (SectionList<E>)super.clone();
            result.mIndexes  = mIndexes.clone();
            result.mSections = mSections.clone();
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object object) {
        return (this == object);
    }

    @Override
    public Object[] toArray() {
        return copyTo(new Object[mSize]);
    }

    @Override
    public <T> T[] toArray(T[] contents) {
        if (contents.length < mSize) {
            contents = (T[])Array.newInstance(contents.getClass().getComponentType(), mSize);
        }

        if (copyTo(contents).length > mSize) {
            contents[mSize] = null;
        }

        return contents;
    }

    private Object[] copyTo(Object[] contents) {
        for (int i = 0, index = 0; i < mCount; ++i) {
            final List section = mSections[i];
            for (int j = 0, size = section.size(); j < size; ++j) {
                contents[index++] = section.get(j);
            }
        }

        return contents;
    }

    private int updateIndexes(int sectionIndex) {
        final int result = mIndexes[sectionIndex];
        for (int index = result; sectionIndex < mCount; ++sectionIndex) {
            mIndexes[sectionIndex] = index;
            index += mSections[sectionIndex].size();
        }

        return result;
    }

    private List[] newSectionArray(int sectionIndex, int newLength) {
        final List[] newSections = new List[newLength];
        System.arraycopy(mSections, 0, newSections, 0, sectionIndex);
        System.arraycopy(mSections, sectionIndex, newSections, sectionIndex + 1, mCount - sectionIndex);
        return newSections;
    }

    private void checkSectionIndex(int sectionIndex) {
        if (sectionIndex < 0 || sectionIndex >= mCount) {
            throw new IndexOutOfBoundsException("Invalid parameter - sectionIndex out of bounds [ sectionIndex = " + sectionIndex + ", sectionCount = " + mCount + " ]");
        }
    }

    /**
     * Class <tt>SectionListIterator</tt> is an implementation of a {@link Iterator}.
     */
    /* package */ final class SectionListIterator implements Iterator<E> {
        private int mIndex;
        private int mSectionIndex;

        @Override
        public boolean hasNext() {
            return (mIndex < mSize);
        }

        @Override
        public E next() {
            DebugUtils.__checkError(mIndex >= mSize, "NoSuchElementException");
            final int startIndex  = mIndexes[mSectionIndex];
            final List<E> section = mSections[mSectionIndex];
            final E value = section.get(mIndex - startIndex);

            if (++mIndex >= startIndex + section.size()) {
                ++mSectionIndex;
            }

            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final SectionList EMPTY_IMMUTABLE_LIST;

    static {
        EMPTY_IMMUTABLE_LIST = new SectionList(new int[0]);
    }
}
