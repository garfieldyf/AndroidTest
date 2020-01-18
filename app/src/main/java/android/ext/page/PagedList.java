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
import java.util.List;

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
    private static final List[] EMPTY_LIST_ARRAY = new List[0];

    /* package */ int mItemCount;
    /* package */ int mPageCount;
    /* package */ List[] mPages;
    /* package */ int[] mPositions;

    /**
     * Constructor
     * @see #PagedList(int)
     * @see #PagedList(Collection)
     */
    public PagedList() {
        mPages = EMPTY_LIST_ARRAY;
        mPositions = EMPTY_INT_ARRAY;
    }

    /**
     * Constructor
     * @param capacity The initial capacity of this <tt>PagedList</tt>.
     * @see #PagedList()
     * @see #PagedList(Collection)
     */
    public PagedList(int capacity) {
        DebugUtils.__checkError(capacity < 0, "capacity < 0");
        if (capacity == 0) {
            mPages = EMPTY_LIST_ARRAY;
            mPositions = EMPTY_INT_ARRAY;
        } else {
            mPages = new List[capacity];
            mPositions = new int[capacity];
        }
    }

    /**
     * Constructor
     * @param pages The collection of pages to add.
     * @see #PagedList()
     * @see #PagedList(int)
     */
    public PagedList(Collection<? extends List<?>> pages) {
        this(pages.size());

        // Adds the page collection to mPages.
        for (List<?> page : pages) {
            mPages[mPageCount] = page;
            mPositions[mPageCount++] = mItemCount;
            mItemCount += page.size();
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
        return (E)mPages[Pages.getOriginalPage(combinedPosition)].get((int)combinedPosition);
    }

    @Override
    public E set(int position, E value) {
        final long combinedPosition = getPageForPosition(position);
        return (E)mPages[Pages.getOriginalPage(combinedPosition)].set((int)combinedPosition, value);
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
     * Returns the page at the specified <em>pageIndex</em> in this <tt>PagedList</tt>.
     * @param pageIndex The index of the page.
     * @return The {@link List} at the specified <em>pageIndex</em>.
     * @see #getPageCount()
     * @see #setPage(int, List)
     */
    public List<E> getPage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        return mPages[pageIndex];
    }

    /**
     * Replaces the page at the specified <em>pageIndex</em> in this
     * <tt>PagedList</tt> with the specified <em>page</em>.
     * @param pageIndex The index at which to put the <em>page</em>.
     * @param page The {@link List} to put.
     * @return The previous page at the <em>pageIndex</em>.
     * @see #getPage(int)
     */
    public List<E> setPage(int pageIndex, List<?> page) {
        DebugUtils.__checkError(ArrayUtils.getSize(page) == 0, "The page is null or 0-size");
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        final List<E> oldPage = mPages[pageIndex];
        mPages[pageIndex] = page;

        final int sizeDelta = page.size() - oldPage.size();
        if (sizeDelta != 0) {
            mItemCount += sizeDelta;
            DebugUtils.__checkError(mItemCount < 0, "Error: The PagedList's itemCount(" + mItemCount + ") < 0");
            computePositions(pageIndex);
        }

        return oldPage;
    }

    /**
     * Adds the specified <em>page</em> at the end of this <tt>PagedList</tt>.
     * @param page The {@link List} to add.
     * @see #addPage(int, List)
     */
    public void addPage(List<?> page) {
        DebugUtils.__checkError(ArrayUtils.getSize(page) == 0, "The page is null or 0-size");
        if (mPageCount == mPages.length) {
            final int newLength = mPageCount + ARRAY_CAPACITY_INCREMENT;
            mPages = ArrayUtils.copyOf(mPages, mPageCount, newLength);
            mPositions = ArrayUtils.copyOf(mPositions, mPageCount, newLength);
        }

        mPages[mPageCount] = page;
        mPositions[mPageCount++] = mItemCount;
        mItemCount += page.size();
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
        DebugUtils.__checkError(ArrayUtils.getSize(page) == 0, "The page is null or 0-size");
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
            mItemCount += page.size();
            computePositions(pageIndex);
        }
    }

    /**
     * Removes the page at the specified <em>pageIndex</em> from this <tt>PagedList</tt>.
     * @param pageIndex The index of the page to remove.
     * @return The start position of the removed page in this <tt>PagedList</tt>.
     */
    public int removePage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0 || pageIndex >= mPageCount, "Invalid pageIndex = " + pageIndex + ", pageCount = " + mPageCount);
        mItemCount -= mPages[pageIndex].size();
        System.arraycopy(mPages, pageIndex + 1, mPages, pageIndex, --mPageCount - pageIndex);
        mPages[mPageCount] = null;  // Prevent memory leak.

        // Computes all pages start position from the removed page index.
        DebugUtils.__checkError(mItemCount < 0, "Error: The PagedList's itemCount(" + mItemCount + ") < 0");
        DebugUtils.__checkError(mPageCount < 0, "Error: The PagedList's pageCount(" + mPageCount + ") < 0");
        return computePositions(pageIndex);
    }

    /**
     * Given a position within this <tt>PagedList</tt>, returns the combined position of
     * the corresponding page within the array of pages.<p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the page.
     * <li>bit 32-63 : Higher 32 bits of the index of the page.</p>
     * @param position The position of the item within this <tt>PagedList</tt>.
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
            final List page = mPages[i];
            result.setLength(0);

            final int startPos = mPositions[i];
            final int size = page.size();
            formatter.format("  Page %-2d ==> ", i);
            printer.println(DebugUtils.toString(page, result).append(" { startPos = ").append(startPos).append(", endPos = ").append(startPos + size - 1).append(", count = ").append(size).append(" }").toString());
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
        for (int i = 0; i < mPageCount; ++i) {
            final int index = mPages[i].indexOf(value);
            if (index != -1) {
                return mPositions[i] + index;
            }
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object value) {
        for (int i = mPageCount - 1; i >= 0; --i) {
            final int index = mPages[i].lastIndexOf(value);
            if (index != -1) {
                return mPositions[i] + index;
            }
        }

        return -1;
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

    private Object[] copyTo(Object[] contents) {
        for (int i = 0, index = 0; i < mPageCount; ++i) {
            final List page = mPages[i];
            for (int j = 0, size = page.size(); j < size; ++j) {
                contents[index++] = page.get(j);
            }
        }

        return contents;
    }

    private int computePositions(int pageIndex) {
        final int result  = mPositions[pageIndex];
        for (int position = result; pageIndex < mPageCount; ++pageIndex) {
            mPositions[pageIndex] = position;
            position += mPages[pageIndex].size();
        }

        return result;
    }

    private List[] newPageArray(int pageIndex, int newLength) {
        final List[] newPages = new List[newLength];
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
            final List<E> page = mPages[mPageIndex];
            final E value = page.get(mPosition - position);

            if (++mPosition >= position + page.size()) {
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
