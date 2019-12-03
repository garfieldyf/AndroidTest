package android.ext.page;

import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.util.Printer;
import java.lang.reflect.Array;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.Iterator;

/**
 * Class <tt>PagedList</tt> allows to adding data by page. This class
 * does not support adding or removing the item, but the items can be
 * set. Setting an item modifies the underlying array.
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public class PagedList<E> extends AbstractList<E> implements Cloneable {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    /* package */ int mItemCount;
    /* package */ int mPageCount;
    /* package */ Object[] mPages;
    /* package */ int[] mPositions;

    /**
     * Constructor
     * @see #PagedList(int)
     * @see #PagedList(Collection)
     */
    public PagedList() {
        this(0);
    }

    /**
     * Constructor
     * @param capacity The initial capacity of the page.
     * @see #PagedList()
     * @see #PagedList(Collection)
     */
    public PagedList(int capacity) {
        DebugUtils.__checkError(capacity < 0, "capacity < 0");
        if (capacity == 0) {
            mPages = EMPTY_OBJECT_ARRAY;
            mPositions = EMPTY_INT_ARRAY;
        } else {
            mPages = new Object[capacity];
            mPositions = new int[capacity];
        }
    }

    /**
     * Constructor
     * @param pages The collection of {@link Page}s to add.
     * @see #PagedList()
     * @see #PagedList(int)
     */
    public PagedList(Collection<? extends Page<? extends E>> pages) {
        this(pages.size());

        // Adds the page collection to mPages.
        for (Page<? extends E> page : pages) {
            mPages[mPageCount] = page;
            mPositions[mPageCount++] = mItemCount;
            mItemCount += page.getCount();
        }
    }

    @Override
    public void clear() {
        if (mItemCount > 0) {
            Arrays.fill(mPages, 0, mPageCount, null);
            mPageCount = 0;
            mItemCount = 0;
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
    public E get(int position) {
        final long combinedPosition = getPageForPosition(position);
        return ((Page<E>)mPages[Pages.getOriginalPage(combinedPosition)]).getItem((int)combinedPosition);
    }

    @Override
    public E set(int position, E value) {
        final long combinedPosition = getPageForPosition(position);
        return ((Page<E>)mPages[Pages.getOriginalPage(combinedPosition)]).setItem((int)combinedPosition, value);
    }

    /**
     * Returns the number of pages in this <tt>PagedList</tt>.
     * @return The number of pages in this <tt>PagedList</tt>.
     * @see #getPage(int)
     */
    public int getPageCount() {
        return mPageCount;
    }

    /**
     * Returns the {@link Page} at the specified <em>pageIndex</em> in this <tt>PagedList</tt>.
     * @param pageIndex The index of the page.
     * @return The {@link Page} at the specified <em>pageIndex</em>.
     * @see #getPageCount()
     * @see #setPage(int, Page)
     */
    public <T extends Page<E>> T getPage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        return (T)mPages[pageIndex];
    }

    /**
     * Replaces the page at the specified <em>pageIndex</em> in this
     * <tt>PagedList</tt> with the specified <em>page</em>.
     * @param pageIndex The index at which to put the <em>page</em>.
     * @param page The {@link Page} to put.
     * @return The previous <tt>Page</tt> at the <em>pageIndex</em>.
     * @see #getPage(int)
     */
    public Page<E> setPage(int pageIndex, Page<? extends E> page) {
        DebugUtils.__checkError(Pages.getCount(page) == 0, "The page is null or 0-count");
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        final Page<E> oldPage = (Page<E>)mPages[pageIndex];
        mPages[pageIndex] = page;

        final int countDelta = page.getCount() - oldPage.getCount();
        if (countDelta != 0) {
            mItemCount += countDelta;
            DebugUtils.__checkError(mItemCount < 0, "Error: The PagedList's itemCount(" + mItemCount + ") < 0");
            computePositions(pageIndex);
        }

        return oldPage;
    }

    /**
     * Adds the specified <em>page</em> at the end of this <tt>PagedList</tt>.
     * @param page The {@link Page} to add.
     * @see #addPage(int, Page)
     */
    public void addPage(Page<? extends E> page) {
        DebugUtils.__checkError(Pages.getCount(page) == 0, "The page is null or 0-count");
        if (mPageCount == mPages.length) {
            final int newLength = mPageCount + ARRAY_CAPACITY_INCREMENT;
            mPages = ArrayUtils.copyOf(mPages, mPageCount, newLength);
            mPositions = ArrayUtils.copyOf(mPositions, mPageCount, newLength);
        }

        mPages[mPageCount] = page;
        mPositions[mPageCount++] = mItemCount;
        mItemCount += page.getCount();
    }

    /**
     * Inserts the specified <em>page</em> into this <tt>PagedList</tt> at the specified <em>pageIndex</em>. The <em>page</em>
     * is inserted before the current page at the specified <em>pageIndex</em>. If the <em>pageIndex</em> is equal to the page
     * count of this <tt>PagedList</tt>, the <em>page</em> is added at the end.
     * @param pageIndex The index at which to insert.
     * @param page The {@link Page} to add.
     * @see #addPage(Page)
     */
    public void addPage(int pageIndex, Page<? extends E> page) {
        DebugUtils.__checkError(Pages.getCount(page) == 0, "The page is null or 0-count");
        DebugUtils.__checkError(pageIndex < 0 || pageIndex > mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        if (pageIndex == mPageCount) {
            addPage(page);
        } else {
            if (mPageCount < mPages.length) {
                System.arraycopy(mPages, pageIndex, mPages, pageIndex + 1, mPageCount - pageIndex);
            } else {
                final int newLength = mPageCount + ARRAY_CAPACITY_INCREMENT;
                mPages = newPageArray(pageIndex, newLength);
                mPositions = ArrayUtils.copyOf(mPositions, mPageCount, newLength);
            }

            ++mPageCount;
            mPages[pageIndex] = page;
            computePositions(pageIndex);
            mItemCount += page.getCount();
        }
    }

    /**
     * Removes the page at the specified <em>pageIndex</em> from this <tt>PagedList</tt>.
     * @param pageIndex The index of the page to remove.
     * @return The start position of the removed page in this <tt>PagedList</tt>.
     */
    public int removePage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        mItemCount -= ((Page<?>)mPages[pageIndex]).getCount();
        System.arraycopy(mPages, pageIndex + 1, mPages, pageIndex, --mPageCount - pageIndex);
        mPages[mPageCount] = null;  // Prevent memory leak.

        // Computes all pages start position from the removed page index.
        DebugUtils.__checkError(mItemCount < 0, "Error: The PagedList's itemCount(" + mItemCount + ") < 0");
        DebugUtils.__checkError(mPageCount < 0, "Error: The PagedList's pageCount(" + mPageCount + ") < 0");
        return computePositions(pageIndex);
    }

    /**
     * Returns the combined position of the page with the given the <em>position</em>.
     * <p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the page.
     * <li>bit 32-63 : Higher 32 bits of the index of the page.</p>
     * @param position The position within this <tt>PagedList</tt>.
     * @return The combined position of the page.
     * @see #getPositionForPage(int)
     * @see Pages#getOriginalPage(long)
     * @see Pages#getOriginalPosition(long)
     */
    public long getPageForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        int pageIndex = Arrays.binarySearch(mPositions, 0, mPageCount, position);
        if (pageIndex < 0) {
            pageIndex = -pageIndex - 2;
        }

        DebugUtils.__checkError(pageIndex < 0, "Invalid pageIndex = " + pageIndex);
        return (((long)pageIndex << 32) | ((position - mPositions[pageIndex]) & 0xFFFFFFFFL));
    }

    /**
     * Given the index of a page within the array of pages, returns the starting
     * position of that page within this <tt>PagedList</tt>.
     * @param pageIndex The index of the page.
     * @return The starting position of that page within this <tt>PagedList</tt>.
     * @see #getPageForPosition(int)
     */
    public int getPositionForPage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        return mPositions[pageIndex];
    }

    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(100);
        final Formatter formatter  = new Formatter(result);
        DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ size = %d, pageCount = %d ] ", getClass().getSimpleName(), mItemCount, mPageCount);

        for (int i = 0; i < mPageCount; ++i) {
            final Page<?> page = (Page<?>)mPages[i];
            result.setLength(0);

            final int startPos = mPositions[i];
            final int count = page.getCount();
            formatter.format("  Page %-2d ==> ", i);
            printer.println(DebugUtils.toString(page, result).append(" { startPos = ").append(startPos).append(", endPos = ").append(startPos + count - 1).append(", count = ").append(count).append(" }").toString());
        }
    }

    @Override
    public Iterator<E> iterator() {
        return new PagedListIterator();
    }

    @Override
    public boolean contains(Object value) {
        return (indexOf(value) != -1);
    }

    @Override
    public int indexOf(Object value) {
        return (value != null ? indexOfValue(value) : indexOfNull());
    }

    @Override
    public int lastIndexOf(Object value) {
        return (value != null ? lastIndexOfValue(value) : lastIndexOfNull());
    }

    @Override
    public PagedList<E> clone() {
        try {
            final PagedList<E> result = (PagedList<E>)super.clone();
            result.mPages = mPages.clone();
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

    private int indexOfNull() {
        for (int i = 0; i < mPageCount; ++i) {
            final Page<?> page = (Page<?>)mPages[i];
            for (int j = 0, count = page.getCount(); j < count; ++j) {
                if (page.getItem(j) == null) {
                    return mPositions[i] + j;
                }
            }
        }

        return -1;
    }

    private int indexOfValue(Object value) {
        for (int i = 0; i < mPageCount; ++i) {
            final Page<?> page = (Page<?>)mPages[i];
            for (int j = 0, count = page.getCount(); j < count; ++j) {
                if (value.equals(page.getItem(j))) {
                    return mPositions[i] + j;
                }
            }
        }

        return -1;
    }

    private int lastIndexOfNull() {
        for (int i = mPageCount - 1; i >= 0; --i) {
            final Page<?> page = (Page<?>)mPages[i];
            for (int j = page.getCount() - 1; j >= 0; --j) {
                if (page.getItem(j) == null) {
                    return mPositions[i] + j;
                }
            }
        }

        return -1;
    }

    private int lastIndexOfValue(Object value) {
        for (int i = mPageCount - 1; i >= 0; --i) {
            final Page<?> page = (Page<?>)mPages[i];
            for (int j = page.getCount() - 1; j >= 0; --j) {
                if (value.equals(page.getItem(j))) {
                    return mPositions[i] + j;
                }
            }
        }

        return -1;
    }

    private Object[] copyTo(Object[] contents) {
        for (int i = 0, index = 0; i < mPageCount; ++i) {
            final Page<?> page = (Page<?>)mPages[i];
            for (int j = 0, count = page.getCount(); j < count; ++j) {
                contents[index++] = page.getItem(j);
            }
        }

        return contents;
    }

    private int computePositions(int pageIndex) {
        final int result = mPositions[pageIndex];
        for (int position = result; pageIndex < mPageCount; ++pageIndex) {
            mPositions[pageIndex] = position;
            position += ((Page<?>)mPages[pageIndex]).getCount();
        }

        return result;
    }

    private Object[] newPageArray(int pageIndex, int newLength) {
        final Object[] newPages = new Object[newLength];
        System.arraycopy(mPages, 0, newPages, 0, pageIndex);
        System.arraycopy(mPages, pageIndex, newPages, pageIndex + 1, mPageCount - pageIndex);
        return newPages;
    }

    /**
     * Class <tt>PagedListIterator</tt> is an implementation of a {@link Iterator}.
     */
    /* package */ final class PagedListIterator implements Iterator<E> {
        private int mPosition;
        private int mPageIndex;

        @Override
        public boolean hasNext() {
            return (mPosition < mItemCount);
        }

        @Override
        public E next() {
            DebugUtils.__checkError(mPosition >= mItemCount, "NoSuchElementException");
            final int position = mPositions[mPageIndex];
            final Page<E> page = (Page<E>)mPages[mPageIndex];
            final E value = page.getItem(mPosition - position);

            if (++mPosition >= position + page.getCount()) {
                ++mPageIndex;
            }

            return value;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }
    }
}
