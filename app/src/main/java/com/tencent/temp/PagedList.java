package com.tencent.temp;

import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.util.Printer;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Iterator;
import java.util.List;

/**
 * Class <tt>PagedList</tt> allows to adding data by page. This class
 * does not support adding or removing the item, but the items can be
 * set. Setting an item modifies the underlying list.
 * @author Garfield
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class PagedList<E> extends AbstractList<E> implements Cloneable {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int ARRAY_INITIAL_CAPACITY   = 8;

    /* package */ int mSize;
    /* package */ int mCount;
    /* package */ List[] mPages;
    /* package */ int[] mIndexes;

    /**
     * Constructor
     * @see #PagedList(int)
     */
    public PagedList() {
        this(new int[ARRAY_INITIAL_CAPACITY]);
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this <tt>PagedList</tt>.
     * @see #PagedList()
     */
    public PagedList(int capacity) {
        this(new int[capacity > 0 ? capacity : ARRAY_INITIAL_CAPACITY]);
    }

    /**
     * Constructor
     */
    private PagedList(int[] indexes) {
        mIndexes = indexes;
        mPages = new List[indexes.length];
    }

    @Override
    public void clear() {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The PagedList is immutable");
        if (mSize > 0) {
            Arrays.fill(mPages, 0, mCount, null);
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
        final int pageIndex = getPageForPosition(index);
        return (E)mPages[pageIndex].get(index - mIndexes[pageIndex]);
    }

    @Override
    public E set(int index, E value) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The PagedList is immutable");
        final int pageIndex = getPageForPosition(index);
        return (E)mPages[pageIndex].set(index - mIndexes[pageIndex], value);
    }

    /**
     * Returns the number of pages in this <tt>PagedList</tt>.
     * @return The number of pages in this <tt>PagedList</tt>.
     * @see #getPage(int)
     */
    public int getPageCount() {
        return mCount;
    }

    /**
     * Returns the page at the specified <em>pageIndex</em> in this <tt>PagedList</tt>.
     * @param pageIndex The index of the page.
     * @return The {@link List} at the specified <em>pageIndex</em>.
     * @see #getPageCount()
     * @see #setPage(int, List)
     */
    public List<E> getPage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mCount, "Invalid parameters - pageIndex out of bounds [ pageIndex = " + pageIndex + ", pageCount = " + mCount + " ]");
        return mPages[pageIndex];
    }

    /**
     * Replaces the page at the specified <em>pageIndex</em>
     * in this <tt>PagedList</tt>.
     * @param pageIndex The index at which to set the page.
     * @param page The {@link List} to set.
     * @return The previous page at the <em>pageIndex</em>.
     * @see #getPage(int)
     */
    public List<E> setPage(int pageIndex, List<?> page) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The PagedList is immutable");
        DebugUtils.__checkError(ArrayUtils.getSize(page) == 0, "Invalid parameters - The page is null or 0-size");
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mCount, "Invalid parameters - pageIndex out of bounds [ pageIndex = " + pageIndex + ", pageCount = " + mCount + " ]");
        final List<E> oldPage = mPages[pageIndex];
        mPages[pageIndex] = page;

        final int sizeDelta = page.size() - oldPage.size();
        if (sizeDelta != 0) {
            mSize += sizeDelta;
            DebugUtils.__checkError(mSize < 0, "Error: The PagedList's size(" + mSize + ") < 0");
            updateIndexes(pageIndex);
        }

        return oldPage;
    }

    /**
     * Adds the specified <em>page</em> at the end of this <tt>PagedList</tt>.
     * @param page The {@link List} to add.
     * @see #addPage(int, List)
     */
    public void addPage(List<?> page) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The PagedList is immutable");
        DebugUtils.__checkError(ArrayUtils.getSize(page) == 0, "Invalid parameters - The page is null or 0-size");
        if (mCount == mPages.length) {
            final int newLength = mCount + ARRAY_CAPACITY_INCREMENT;
            mPages   = ArrayUtils.copyOf(mPages, mCount, newLength);
            mIndexes = ArrayUtils.copyOf(mIndexes, mCount, newLength);
        }

        mPages[mCount] = page;
        mIndexes[mCount++] = mSize;
        mSize += page.size();
    }

    /**
     * Inserts the specified <em>page</em> into this <tt>PagedList</tt> at the specified <em>pageIndex</em>. The <em>page</em>
     * is inserted before the current page at the specified <em>pageIndex</em>. If the <em>pageIndex</em> is equal to the page
     * count of this <tt>PagedList</tt>, the <em>page</em> is added at the end.
     * @param pageIndex The index at which to insert.
     * @param page The {@link List} to add.
     * @see #addPage(List)
     */
    public void addPage(int pageIndex, List<?> page) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The PagedList is immutable");
        DebugUtils.__checkError(ArrayUtils.getSize(page) == 0, "Invalid parameters - The page is null or 0-size");
        DebugUtils.__checkError(pageIndex < 0 || pageIndex > mCount, "Invalid parameters - pageIndex out of bounds [ pageIndex = " + pageIndex + ", pageCount = " + mCount + " ]");
        if (pageIndex == mCount) {
            addPage(page);
        } else {
            if (mCount < mPages.length) {
                System.arraycopy(mPages, pageIndex, mPages, pageIndex + 1, mCount - pageIndex);
            } else {
                final int newLength = mCount + ARRAY_CAPACITY_INCREMENT;
                mPages   = newPageArray(pageIndex, newLength);
                mIndexes = ArrayUtils.copyOf(mIndexes, mCount, newLength);
            }

            ++mCount;
            mPages[pageIndex] = page;
            mSize += page.size();
            updateIndexes(pageIndex);
        }
    }

    /**
     * Removes the page at the specified <em>pageIndex</em> from this <tt>PagedList</tt>.
     * @param pageIndex The index of the page to remove.
     * @return The start index of the removed page in this <tt>PagedList</tt>.
     */
    public int removePage(int pageIndex) {
        DebugUtils.__checkError(this == EMPTY_IMMUTABLE_LIST, "Unsupported operation - The PagedList is immutable");
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mCount, "Invalid parameters - pageIndex out of bounds [ pageIndex = " + pageIndex + ", pageCount = " + mCount + " ]");
        mSize -= mPages[pageIndex].size();
        System.arraycopy(mPages, pageIndex + 1, mPages, pageIndex, --mCount - pageIndex);
        mPages[mCount] = null;  // Prevent memory leak.

        // Computes all pages start index from the removed page index.
        DebugUtils.__checkError(mSize < 0, "Error: The PagedList's size(" + mSize + ") < 0");
        DebugUtils.__checkError(mCount < 0, "Error: The PagedList's pageCount(" + mCount + ") < 0");
        return updateIndexes(pageIndex);
    }

    /**
     * Given a position within this <tt>PagedList</tt>, returns the index of the
     * page within the array of pages.
     * @param index The position of the item within this <tt>PagedList</tt>.
     * @return The index of the page within the array of pages.
     * @see #getPositionForSection(int)
     */
    public int getPageForPosition(int index) {
        DebugUtils.__checkError(index < 0 || index >= mSize, "Invalid parameters - index out of bounds [ index = " + index + ", size = " + mSize + " ]");
        final int pageIndex = Arrays.binarySearch(mIndexes, 0, mCount, index);
        return (pageIndex >= 0 ? pageIndex : -pageIndex - 2);
    }

    /**
     * Given the index of a page within the array of pages, returns the starting
     * index of that page within this <tt>PagedList</tt>.
     * @param pageIndex The index of the page.
     * @return The starting index of that page within this <tt>PagedList</tt>.
     * @see #getPageForPosition(int)
     */
    public int getPositionForSection(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mCount, "Invalid parameters - pageIndex out of bounds [ pageIndex = " + pageIndex + ", pageCount = " + mCount + " ]");
        return mIndexes[pageIndex];
    }

    /**
     * Returns a type-safe empty, immutable {@link PagedList}.
     * @return An empty {@link PagedList}.
     */
    public static <E> PagedList<E> emptyList() {
        return EMPTY_IMMUTABLE_LIST;
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(128);
        final Formatter formatter  = new Formatter(result);
        DeviceUtils.dumpSummary(printer, result, 100, " Dumping %s [ size = %d, pageCount = %d ] ", getClass().getSimpleName(), mSize, mCount);

        for (int i = 0; i < mCount; ++i) {
            final List page = mPages[i];
            result.setLength(0);

            final int start = mIndexes[i];
            final int size  = page.size();
            formatter.format("  Page %-2d ==> ", i);
            printer.println(DeviceUtils.toString(page, result).append(" { start = ").append(start).append(", end = ").append(start + size - 1).append(", size = ").append(size).append(" }").toString());
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new PageListIterator();
    }

    @Override
    public boolean contains(Object value) {
        return (indexOf(value) != -1);
    }

    @Override
    public int indexOf(Object value) {
        for (int i = 0; i < mCount; ++i) {
            final int index = mPages[i].indexOf(value);
            if (index != -1) {
                return mIndexes[i] + index;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object value) {
        for (int i = mCount - 1; i >= 0; --i) {
            final int index = mPages[i].lastIndexOf(value);
            if (index != -1) {
                return mIndexes[i] + index;
            }
        }

        return -1;
    }

    @Override
    public PagedList<E> clone() {
        try {
            final PagedList<E> result = (PagedList<E>)super.clone();
            result.mPages   = mPages.clone();
            result.mIndexes = mIndexes.clone();
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
            final List page = mPages[i];
            for (int j = 0, size = page.size(); j < size; ++j) {
                contents[index++] = page.get(j);
            }
        }

        return contents;
    }

    private int updateIndexes(int pageIndex) {
        final int result = mIndexes[pageIndex];
        for (int index = result; pageIndex < mCount; ++pageIndex) {
            mIndexes[pageIndex] = index;
            index += mPages[pageIndex].size();
        }

        return result;
    }

    private List[] newPageArray(int pageIndex, int newLength) {
        final List[] newPages = new List[newLength];
        System.arraycopy(mPages, 0, newPages, 0, pageIndex);
        System.arraycopy(mPages, pageIndex, newPages, pageIndex + 1, mCount - pageIndex);
        return newPages;
    }

    /**
     * Class <tt>PageListIterator</tt> is an implementation of a {@link Iterator}.
     */
    /* package */ final class PageListIterator implements Iterator<E> {
        private int mIndex;
        private int mPageIndex;

        @Override
        public boolean hasNext() {
            return (mIndex < mSize);
        }

        @Override
        public E next() {
            DebugUtils.__checkError(mIndex >= mSize, "NoSuchElementException");
            final int startIndex = mIndexes[mPageIndex];
            final List<E> page = mPages[mPageIndex];
            final E value = page.get(mIndex - startIndex);

            if (++mIndex >= startIndex + page.size()) {
                ++mPageIndex;
            }

            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }

    private static final PagedList EMPTY_IMMUTABLE_LIST;

    static {
        EMPTY_IMMUTABLE_LIST = new PagedList(new int[0]);
    }
}
