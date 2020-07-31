package android.ext.util;

import android.annotation.TargetApi;
import android.util.Printer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class <tt>SectionList</tt> allows to adding or removing data by section.
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public class SectionList<E> extends ArrayList<E> implements Cloneable {
    private static final int ARRAY_CAPACITY_INCREMENT = 16;
    private int mCount;
    private int[] mSizes;
    private int[] mIndexes;

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
        super(capacity);
        mSizes   = new int[ARRAY_CAPACITY_INCREMENT];
        mIndexes = new int[ARRAY_CAPACITY_INCREMENT];
    }

    /**
     * Constructor
     * @param collection The collection of elements to add.
     * @see #SectionList()
     * @see #SectionList(int)
     */
    public SectionList(Collection<?> collection) {
        this(collection.size());
        addAll((Collection<? extends E>)collection);
    }

    @Override
    public void clear() {
        mCount = 0;
        super.clear();
    }

    @Override
    public boolean add(E value) {
        updateSize(1);
        return super.add(value);
    }

    @Override
    public void add(int index, E value) {
        DebugUtils.__checkError(index < 0 || index > size(), "Invalid index = " + index + ", size = " + size());
        if (index == size()) {
            add(value);
        } else {
            updateSize(index, 1);
            super.add(index, value);
        }
    }

    @Override
    public E remove(int index) {
        DebugUtils.__checkError(index < 0 || index >= size(), "Invalid index = " + index + ", size = " + size());
        final int sectionIndex = getSectionForPosition(index);
        if (--mSizes[sectionIndex] == 0) {
            // Removes the section, if the section is empty.
            DebugUtils.__checkDebug(true, "SectionList", "remove the section = " + sectionIndex);
            System.arraycopy(mSizes, sectionIndex + 1, mSizes, sectionIndex, --mCount - sectionIndex);
        }

        updateIndexes(sectionIndex);
        return super.remove(index);
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
    @TargetApi(24)
    public boolean removeIf(Predicate<? super E> filter) {
        final Iterator<E> itor = listIterator(0);
        boolean result = false;
        while (itor.hasNext()) {
            if (filter.test(itor.next())) {
                itor.remove();
                result = true;
            }
        }

        return result;
    }

    @Override
    public boolean addAll(Collection<? extends E> collection) {
        DebugUtils.__checkError(collection == null, "Invalid parameter - collection == null");
        final int size = collection.size();
        if (size > 0) {
            updateSize(size);
            return super.addAll(collection);
        }

        return false;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> collection) {
        DebugUtils.__checkError(collection == null, "Invalid parameter - collection == null");
        DebugUtils.__checkError(index < 0 || index > size(), "Invalid index = " + index + ", size = " + size());
        if (index == size()) {
            return addAll(collection);
        }

        final int size = collection.size();
        if (size > 0) {
            updateSize(index, size);
            return super.addAll(index, collection);
        }

        return false;
    }

    @Override
    public boolean removeAll(Collection<?> collection) {
        return removeAll(collection, true);
    }

    @Override
    public boolean retainAll(Collection<?> collection) {
        return removeAll(collection, false);
    }

    @Override
    public Iterator<E> iterator() {
        return listIterator(0);
    }

    @Override
    public SectionList<E> clone() {
        final SectionList<E> result = (SectionList<E>)super.clone();
        result.mSizes   = mSizes.clone();
        result.mIndexes = mIndexes.clone();
        return result;
    }

    @Override
    public int hashCode() {
        int hashCode = super.hashCode();
        for (int i = 0; i < mCount; ++i) {
            hashCode = hashCode * 31 + mSizes[i];
            hashCode = hashCode * 31 + mIndexes[i];
        }

        return hashCode;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (object instanceof SectionList) {
            final SectionList<?> list = (SectionList<?>)object;
            return (mCount == list.mCount && ArrayUtils.equals(mSizes, 0, list.mSizes, 0, mCount) && ArrayUtils.equals(mIndexes, 0, list.mIndexes, 0, mCount) && super.equals(object));
        }

        return false;
    }

    /**
     * Returns the number of sections in this <tt>SectionList</tt>.
     * @return The number of sections in this <tt>SectionList</tt>.
     * @see #getSectionSize(int)
     */
    public int getSectionCount() {
        return mCount;
    }

    /**
     * Returns the number of elements in the specified section.
     * @param sectionIndex The index of the section.
     * @return The number of elements.
     * @see #getSectionCount()
     */
    public int getSectionSize(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mCount);
        return mSizes[sectionIndex];
    }

    /**
     * Returns the section at the specified <em>sectionIndex</em> in this <tt>SectionList</tt>.
     * The returns section as a view of this <tt>SectionList</tt>. Any change that occurs in
     * the returned section will be reflected to this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @return The section at the specified <em>sectionIndex</em>.
     * @see #setSection(int, Collection)
     */
    public List<E> getSection(int sectionIndex) {
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mCount);
        final int index = mIndexes[sectionIndex];
        return subList(index, index + mSizes[sectionIndex]);
    }

    /**
     * Replaces the section at the specified <em>sectionIndex</em> in this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section.
     * @param section The section to add.
     * @return <tt>true</tt> if this <tt>SectionList</tt> is modified, <tt>false</tt> otherwise.
     * @see #getSection(int)
     */
    public boolean setSection(int sectionIndex, Collection<?> section) {
        DebugUtils.__checkError(section == null, "Invalid parameter - section == null");
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mCount);
        final int newSize = section.size();
        if (newSize == 0) {
            return false;
        }

        int index = mIndexes[sectionIndex], oldSize = mSizes[sectionIndex];
        if (newSize == oldSize) {
            for (Object value : section) {
                set(index++, (E)value);
            }
        } else {
            // Updates the section size at the sectionIndex.
            mSizes[sectionIndex] = newSize;
            updateIndexes(sectionIndex);

            // Updates the elements at the index.
            removeRange(index, index + oldSize);
            super.addAll(index, (Collection<? extends E>)section);
        }

        return true;
    }

    /**
     * Adds the specified <em>section</em> at the end of this <tt>SectionList</tt>.
     * @param section The section to add.
     * @return <tt>true</tt> if this <tt>SectionList</tt> is modified, <tt>false</tt> otherwise.
     * @see #addSection(int, Collection)
     */
    public boolean addSection(Collection<?> section) {
        DebugUtils.__checkError(section == null, "Invalid parameter - section == null");
        final int size = section.size();
        if (size == 0) {
            return false;
        }

        if (mCount == mSizes.length) {
            final int newLength = mCount + ARRAY_CAPACITY_INCREMENT;
            mSizes   = ArrayUtils.copyOf(mSizes, mCount, newLength);
            mIndexes = ArrayUtils.copyOf(mIndexes, mCount, newLength);
        }

        mSizes[mCount] = size;
        mIndexes[mCount++] = size();
        return super.addAll((Collection<? extends E>)section);
    }

    /**
     * Inserts the specified <em>section</em> into this <tt>SectionList</tt> at the specified <em>sectionIndex</em>. The <em>section</em>
     * is inserted before the current section at the specified <em>sectionIndex</em>. If the <em>sectionIndex</em> is equal to the section
     * count of this <tt>SectionList</tt>, the <em>section</em> is added at the end.
     * @param sectionIndex The index at which to insert.
     * @param section The section to add.
     * @return <tt>true</tt> if this <tt>SectionList</tt> is modified, <tt>false</tt> otherwise.
     * @see #addSection(Collection)
     */
    public boolean addSection(int sectionIndex, Collection<?> section) {
        DebugUtils.__checkError(section == null, "Invalid parameter - section == null");
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex > mCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mCount);
        if (sectionIndex == mCount) {
            return addSection(section);
        }

        final int size = section.size();
        if (size == 0) {
            return false;
        }

        // Inserts a new section at the sectionIndex.
        final int index = mIndexes[sectionIndex];
        if (mCount == mSizes.length) {
            final int newLength = mCount + ARRAY_CAPACITY_INCREMENT;
            mSizes   = newSizeArray(sectionIndex, newLength);
            mIndexes = ArrayUtils.copyOf(mIndexes, mCount, newLength);
        } else {
            System.arraycopy(mSizes, sectionIndex, mSizes, sectionIndex + 1, mCount - sectionIndex);
        }

        ++mCount;
        mSizes[sectionIndex] = size;
        updateIndexes(sectionIndex);

        // Inserts the elements at the index.
        return super.addAll(index, (Collection<? extends E>)section);
    }

    /**
     * Removes the section at the specified <em>sectionIndex</em> from this <tt>SectionList</tt>.
     * @param sectionIndex The index of the section to remove.
     * @return The start index of the removed section in this <tt>SectionList</tt>.
     */
    public int removeSection(int sectionIndex) {
        // Removes the elements from startIndex to startIndex + size
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mCount);
        final int startIndex = mIndexes[sectionIndex];
        removeRange(startIndex, startIndex + mSizes[sectionIndex]);

        // Removes the section at the sectionIndex.
        System.arraycopy(mSizes, sectionIndex + 1, mSizes, sectionIndex, --mCount - sectionIndex);
        updateIndexes(sectionIndex);
        return startIndex;
    }

    /**
     * Given a position within this <tt>SectionList</tt>, returns the index of the
     * section within the array of sections.
     * @param position The position of the element within this <tt>SectionList</tt>.
     * @return The index of the section within the array of sections.
     * @see #getPositionForSection(int)
     */
    public int getSectionForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= size(), "Invalid position = " + position + ", size = " + size());
        final int sectionIndex = Arrays.binarySearch(mIndexes, 0, mCount, position);
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
        DebugUtils.__checkError(sectionIndex < 0 || sectionIndex >= mCount, "Invalid sectionIndex = " + sectionIndex + ", sectionCount = " + mCount);
        return mIndexes[sectionIndex];
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(128);
        DeviceUtils.dumpSummary(printer, result, 100, " Dumping %s [ size = %d, sectionCount = %d ] ", getClass().getSimpleName(), size(), mCount);
        for (int i = 0; i < mCount; ++i) {
            result.setLength(0);
            final int size  = mSizes[i];
            final int start = mIndexes[i];
            printer.println(result.append("  section ").append(i).append(" [ startIndex = ").append(start).append(", endIndex = ").append(start + size - 1).append(", size = ").append(size).append(" ]").toString());

            final int end = start + size;
            for (int j = start; j < end; ++j) {
                result.setLength(0);
                printer.println(result.append("    index = ").append(j).append(", value = ").append(get(j)).toString());
            }
        }
    }

    private void updateSize(int size) {
        if (mCount == 0) {
            // Adds a new section, if no section.
            mCount = 1;
            mSizes[0] = size;
        } else {
            // Updates the last section size.
            mSizes[mCount - 1] += size;
        }
    }

    private void updateSize(int index, int size) {
        final int sectionIndex = getSectionForPosition(index);
        mSizes[sectionIndex] += size;
        updateIndexes(sectionIndex);
    }

    private void updateIndexes(int sectionIndex) {
        for (int index = mIndexes[sectionIndex]; sectionIndex < mCount; ++sectionIndex) {
            mIndexes[sectionIndex] = index;
            index += mSizes[sectionIndex];
        }
    }

    private boolean removeAll(Collection<?> collection, boolean contains) {
        final Iterator<?> itor = listIterator(0);
        boolean result = false;
        while (itor.hasNext()) {
            if (collection.contains(itor.next()) == contains) {
                itor.remove();
                result = true;
            }
        }

        return result;
    }

    private int[] newSizeArray(int sectionIndex, int newLength) {
        final int[] newSizes = new int[newLength];
        System.arraycopy(mSizes, 0, newSizes, 0, sectionIndex);
        System.arraycopy(mSizes, sectionIndex, newSizes, sectionIndex + 1, mCount - sectionIndex);
        return newSizes;
    }
}
