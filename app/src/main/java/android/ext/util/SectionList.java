package android.ext.util;

import android.util.Printer;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

/**
 * Class <tt>SectionList</tt> allows to adding data by section. This
 * class does not support adding or removing the item, but the items
 * can be set. Setting an item modifies the underlying array.
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public class SectionList<E> extends AbstractList<E> implements Cloneable {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final List[] EMPTY_LIST_ARRAY = new List[0];

    /* package */ int mItemCount;
    /* package */ int mSectionCount;
    /* package */ List[] mSections;
    /* package */ int[] mPositions;

    /**
     * Constructor
     * @see #SectionList(int)
     * @see #SectionList(Collection)
     */
    public SectionList() {
        mPositions = EMPTY_INT_ARRAY;
        mSections  = EMPTY_LIST_ARRAY;
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
            mSections  = EMPTY_LIST_ARRAY;
        } else {
            mPositions = new int[capacity];
            mSections  = new List[capacity];
        }
    }

    /**
     * Constructor
     * @param sections The collection of sections to add.
     * @see #SectionList()
     * @see #SectionList(int)
     */
    public SectionList(Collection<? extends List<?>> sections) {
        this(sections.size());

        // Adds the section collection to mSections.
        for (List<?> section : sections) {
            final int size = ArrayUtils.getSize(section);
            if (size > 0) {
                mSections[mSectionCount] = section;
                mPositions[mSectionCount++] = mItemCount;
                mItemCount += size;
            }
        }
    }

    @Override
    public void clear() {
        if (mItemCount > 0) {
            Arrays.fill(mSections, 0, mSectionCount, null);
            mItemCount = 0;
            mSectionCount = 0;
        }
    }

    @Override
    public int size() {
        return mItemCount;
    }

    @Override
    public boolean isEmpty() {
        return (mItemCount == 0);
    }

    @Override
    public boolean add(E value) {
        if (mSectionCount == 0) {
            final List<E> section = createSection();
            section.add(value);
            addSection(section);
        } else {
            ++mItemCount;
            mSections[mSectionCount - 1].add(value);
        }

        return true;
    }

    @Override
    public void add(int position, E value) {
        DebugUtils.__checkError(position < 0 || position > mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        if (position == mItemCount) {
            add(value);
        } else {
            final long combinedPosition = getSectionForPosition(position);
            final int sectionIndex = getOriginalSection(combinedPosition);

            ++mItemCount;
            mSections[sectionIndex].add((int)combinedPosition, value);
            computePositions(sectionIndex);
        }
    }

    @Override
    public E get(int position) {
        final long combinedPosition = getSectionForPosition(position);
        return (E)mSections[getOriginalSection(combinedPosition)].get((int)combinedPosition);
    }

    @Override
    public E set(int position, E value) {
        final long combinedPosition = getSectionForPosition(position);
        return (E)mSections[getOriginalSection(combinedPosition)].set((int)combinedPosition, value);
    }

    @Override
    public boolean remove(Object value) {
        final int index = indexOf(value);
        if (index != -1) {
            remove(index);
            return true;
        }

        return false;
    }

    @Override
    public E remove(int position) {
        final long combinedPosition = getSectionForPosition(position);
        final int sectionIndex = getOriginalSection(combinedPosition);
        final List section = mSections[sectionIndex];
        final E value = section.remove((int)combinedPosition);

        --mItemCount;
        if (section.size() == 0) {
            removeSection(sectionIndex);
        } else {
            computePositions(sectionIndex);
        }

        return value;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        final int size = ArrayUtils.getSize(collection);
        if (size == 0) {
            return false;
        }

        if (mSectionCount == 0) {
            final List<E> section = createSection();
            section.addAll(collection);
            addSection(section);
        } else {
            mItemCount += size;
            mSections[mSectionCount - 1].addAll(collection);
        }

        return true;
    }

    @Override
    public boolean addAll(int position, Collection<? extends E> collection) {
        DebugUtils.__checkError(position < 0 || position > mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        if (position == mItemCount) {
            return addAll(collection);
        }

        final int size = ArrayUtils.getSize(collection);
        if (size == 0) {
            return false;
        }

        final long combinedPosition = getSectionForPosition(position);
        final int sectionIndex = getOriginalSection(combinedPosition);

        mItemCount += size;
        mSections[sectionIndex].addAll((int)combinedPosition, collection);
        computePositions(sectionIndex);
        return true;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        super.removeAll(collection);
        if (ArrayUtils.getSize(collection) == 0) {
            return false;
        }

        boolean result = false;
        for (Object value : collection) {
            final int index = indexOf(value);
            if (index != -1) {
                remove(index);
                result = true;
            }
        }

        return result;
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object value) {
        for (int i = 0; i < mSectionCount; ++i) {
            final int index = mSections[i].indexOf(value);
            if (index != -1) {
                return mPositions[i] + index;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object value) {
        for (int i = mSectionCount - 1; i >= 0; --i) {
            final int index = mSections[i].lastIndexOf(value);
            if (index != -1) {
                return mPositions[i] + index;
            }
        }

        return -1;
    }

    @Override
    public boolean contains(Object value) {
        return (indexOf(value) != -1);
    }

    @Override
    public boolean containsAll(Collection<?> collection) {
        super.containsAll(collection);
        if (ArrayUtils.getSize(collection) == 0) {
            return false;
        }

        for (Object value : collection) {
            if (indexOf(value) == -1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Iterator<E> iterator() {
        return new SectionListIterator();
    }

    @Override
    public Object[] toArray() {
        return copyTo(new Object[mItemCount]);
    }

    @Override
    public <T> T[] toArray(T[] contents) {
        if (contents.length < mItemCount) {
            contents = (T[])Array.newInstance(contents.getClass().getComponentType(), mItemCount);
        }

        if (copyTo(contents).length > mItemCount) {
            contents[mItemCount] = null;
        }

        return contents;
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

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(Object object) {
        return (this == object);
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
     * Returns the section at the specified <em>sectionIndex</em> in this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @return The {@link List} at the specified <em>sectionIndex</em>.
     * @see #getSectionCount()
     * @see #setSection(int, List)
     */
    public List<E> getSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mSectionCount);
        return mSections[sectionIndex];
    }

    /**
     * Replaces the section at the specified <em>sectionIndex</em> in this
     * <tt>SectionList</tt> with the specified <em>section</em>.
     * @param sectionIndex The index at which to put the <em>section</em>.
     * @param section The {@link List} to put.
     * @return The previous section at the <em>sectionIndex</em> or <tt>null</tt>
     * if the <em>section</em> is empty.
     * @see #getSection(int)
     */
    public List<E> setSection(int sectionIndex, List<?> section) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mSectionCount);
        final int size = ArrayUtils.getSize(section);
        List<E> oldSection = null;
        if (size > 0) {
            oldSection = mSections[sectionIndex];
            mSections[sectionIndex] = section;

            final int sizeDelta = size - oldSection.size();
            if (sizeDelta != 0) {
                mItemCount += sizeDelta;
                DebugUtils.__checkError(mItemCount < 0, "Error: The SectionList's itemCount(" + mItemCount + ") < 0");
                computePositions(sectionIndex);
            }
        }

        return oldSection;
    }

    /**
     * Adds the specified <em>section</em> at the end of this <tt>SectionList</tt>.
     * @param section The {@link List} to add.
     * @return <tt>true</tt> if the <em>section</em> added or <tt>false</tt> if the
     * <em>section</em> is empty.
     * @see #addSection(int, List)
     */
    public boolean addSection(List<?> section) {
        final int size = ArrayUtils.getSize(section);
        if (size == 0) {
            return false;
        }

        if (mSectionCount == mSections.length) {
            final int newLength = mSectionCount + ARRAY_CAPACITY_INCREMENT;
            mSections  = ArrayUtils.copyOf(mSections, mSectionCount, newLength);
            mPositions = ArrayUtils.copyOf(mPositions, mSectionCount, newLength);
        }

        mSections[mSectionCount] = section;
        mPositions[mSectionCount++] = mItemCount;
        mItemCount += size;
        return true;
    }

    /**
     * Inserts the specified <em>section</em> into this <tt>SectionList</tt> at the specified <em>sectionIndex</em>. The <em>section</em>
     * is inserted before the current section at the specified <em>sectionIndex</em>. If the <em>sectionIndex</em> is equal to the section
     * count of this <tt>SectionList</tt>, the <em>section</em> is added at the end.
     * @param sectionIndex The index at which to insert.
     * @param section The {@link List} to add.
     * @return <tt>true</tt> if the <em>section</em> added or <tt>false</tt> if the <em>section</em> is empty.
     * @see #addSection(List)
     */
    public boolean addSection(int sectionIndex, List<?> section) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex > mSectionCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mSectionCount);
        if (sectionIndex == mSectionCount) {
            return addSection(section);
        }

        final int size = ArrayUtils.getSize(section);
        if (size == 0) {
            return false;
        }

        if (mSectionCount < mSections.length) {
            System.arraycopy(mSections, sectionIndex, mSections, sectionIndex + 1, mSectionCount - sectionIndex);
        } else {
            final int newLength = mSectionCount + ARRAY_CAPACITY_INCREMENT;
            mSections  = newSectionArray(sectionIndex, newLength);
            mPositions = ArrayUtils.copyOf(mPositions, mSectionCount, newLength);
        }

        ++mSectionCount;
        mSections[sectionIndex] = section;
        mItemCount += size;
        computePositions(sectionIndex);
        return true;
    }

    /**
     * Removes the section at the specified <em>sectionIndex</em> from this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section to remove.
     * @return The start position of the removed section in this <tt>SectionList</tt>.
     */
    public int removeSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mSectionCount);
        mItemCount -= mSections[sectionIndex].size();
        System.arraycopy(mSections, sectionIndex + 1, mSections, sectionIndex, --mSectionCount - sectionIndex);
        mSections[mSectionCount] = null;  // Prevent memory leak.

        // Computes all pages start position from the removed section index.
        DebugUtils.__checkError(mItemCount < 0, "Error: The SectionList's itemCount(" + mItemCount + ") < 0");
        DebugUtils.__checkError(mSectionCount < 0, "Error: The SectionList's sectionCount(" + mSectionCount + ") < 0");
        return computePositions(sectionIndex);
    }

    /**
     * Given a position within this <tt>SectionList</tt>, returns the combined position of the
     * corresponding section within the array of sections.<p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the section.
     * <li>bit 32-63 : Higher 32 bits of the index of the section.</p>
     * @param position The position of the item within this <tt>SectionList</tt>.
     * @return The combined position of the section.
     * @see #getPositionForSection(int)
     * @see #getOriginalSection(long)
     * @see #getOriginalPosition(long)
     */
    public long getSectionForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        int sectionIndex = Arrays.binarySearch(mPositions, 0, mSectionCount, position);
        if (sectionIndex < 0) {
            sectionIndex = -sectionIndex - 2;
        }

        DebugUtils.__checkError(sectionIndex < 0, "Invalid sectionIndex = " + sectionIndex);
        return (((long)sectionIndex << 32) | ((position - mPositions[sectionIndex]) & 0xFFFFFFFFL));
    }

    /**
     * Given the index of a section within the array of sections, returns the starting
     * position of that section within this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @return The starting position of that section within this <tt>SectionList</tt>.
     * @see #getSectionForPosition(int)
     */
    public int getPositionForSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mSectionCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mSectionCount);
        return mPositions[sectionIndex];
    }

    /**
     * Returns the index of the section with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by {@link #getSectionForPosition}.
     * @return The index of the section.
     * @see #getOriginalPosition(long)
     */
    public static int getOriginalSection(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "combinedPosition < 0");
        return (int)(combinedPosition >> 32);
    }

    /**
     * Returns the index of the item in the section with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by {@link #getSectionForPosition}.
     * @return The index of the item in the section.
     * @see #getOriginalSection(long)
     */
    public static int getOriginalPosition(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "combinedPosition < 0");
        return (int)combinedPosition;
    }

    /**
     * Returns a new section to add to this <tt>SectionList</tt>.
     * The default implementation returns a new {@link ArrayList}.
     */
    protected List<?> createSection() {
        return new ArrayList();
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(100);
        final Formatter formatter  = new Formatter(result);
        DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ itemCount = %d, sectionCount = %d ] ", getClass().getSimpleName(), mItemCount, mSectionCount);

        for (int i = 0; i < mSectionCount; ++i) {
            final List section = mSections[i];
            result.setLength(0);

            final int startPos = mPositions[i];
            final int size = section.size();
            formatter.format("  Section %-2d ==> ", i);
            printer.println(DebugUtils.toString(section, result).append(" { startPos = ").append(startPos).append(", endPos = ").append(startPos + size - 1).append(", count = ").append(size).append(" }").toString());
        }
    }

    private Object[] copyTo(Object[] contents) {
        for (int i = 0, index = 0; i < mSectionCount; ++i) {
            final List section = mSections[i];
            for (int j = 0, size = section.size(); j < size; ++j) {
                contents[index++] = section.get(j);
            }
        }

        return contents;
    }

    private int computePositions(int sectionIndex) {
        final int result  = mPositions[sectionIndex];
        for (int position = result; sectionIndex < mSectionCount; ++sectionIndex) {
            mPositions[sectionIndex] = position;
            position += mSections[sectionIndex].size();
        }

        return result;
    }

    private List[] newSectionArray(int sectionIndex, int newLength) {
        final List[] newSections = new List[newLength];
        System.arraycopy(mSections, 0, newSections, 0, sectionIndex);
        System.arraycopy(mSections, sectionIndex, newSections, sectionIndex + 1, mSectionCount - sectionIndex);
        return newSections;
    }

    /**
     * Class <tt>SectionListIterator</tt> is an implementation of a {@link Iterator}.
     */
    /* package */ final class SectionListIterator implements Iterator<E> {
        private int mPosition;
        private int mSectionIndex;

        @Override
        public boolean hasNext() {
            return (mPosition < mItemCount);
        }

        @Override
        public E next() {
            DebugUtils.__checkError(mPosition >= mItemCount, "NoSuchElementException");
            final int position = mPositions[mSectionIndex];
            final List<E> section = mSections[mSectionIndex];
            final E value = section.get(mPosition - position);

            if (++mPosition >= position + section.size()) {
                ++mSectionIndex;
            }

            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
