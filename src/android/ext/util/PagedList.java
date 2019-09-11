package android.ext.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import android.ext.page.Page;
import android.ext.page.Pages;
import android.util.Printer;

/**
 * Class <tt>PagedList</tt> allows to loading data by page.
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public class PagedList<E> {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;
    private static final int[] EMPTY_INT_ARRAY = new int[0];
    private static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];

    private int mItemCount;
    private int mPageCount;
    private Object[] mPages;
    private int[] mPositions;

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
     * @param capacity The initial capacity of this <tt>PagedList</tt>.
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
     * @param initPages The collection of {@link Page}s to add.
     * @see #PagedList()
     * @see #PagedList(int)
     */
    public PagedList(Collection<? extends Page<? extends E>> initPages) {
        this(initPages.size());
        addPages(initPages);
    }

    /**
     * Removes all items from this <tt>PagedList</tt>, leaving it empty.
     * @see #getItemCount()
     */
    public void clear() {
        if (mItemCount > 0) {
            Arrays.fill(mPages, 0, mPageCount, null);
            mPageCount = 0;
            mItemCount = 0;
        }
    }

    /**
     * Returns the number of items in this <tt>PagedList</tt>.
     * @return The number of items in this <tt>PagedList</tt>.
     * @see #getItem(int)
     */
    public int getItemCount() {
        return mItemCount;
    }

    /**
     * Returns the item at the specified <em>position</em> in this <tt>PagedList</tt>.
     * @param position The index of the item.
     * @return The item at the specified <em>position</em>.
     * @see #getItemCount()
     * @see #getPageForPosition(int)
     */
    public E getItem(int position) {
        final long combinedPosition = getPageForPosition(position);
        final int index = Pages.getOriginalPage(combinedPosition);
        return ((Page<E>)mPages[index]).getItem((int)combinedPosition);
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
     * Returns the page at the specified <em>index</em> in this <tt>PagedList</tt>.
     * @param index The index of the page.
     * @return The {@link Page} at the specified <em>index</em>.
     * @see #getPageCount()
     * @see #getPageForPosition(int)
     */
    public Page<E> getPage(int index) {
        DebugUtils.__checkError(index < 0 || index >= mPageCount, "Index out of bounds - pageIndex = " + index + ", pageCount = " + mPageCount);
        return (Page<E>)mPages[index];
    }

    /**
     * Adds the specified <em>page</em> at the end of this <tt>PagedList</tt>.
     * @param page The {@link Page} to add.
     * @return The number of items to add in the <em>page</em>.
     * @see #addPages(Collection)
     * @see Pages#newPage(List)
     */
    public int addPage(Page<? extends E> page) {
        final int count = Pages.getCount(page);
        if (count > 0) {
            if (mPageCount >= mPages.length) {
                mPages = newArray(mPages);
                mPositions = newArray(mPositions);
            }

            mPages[mPageCount] = page;
            mPositions[mPageCount++] = mItemCount;
            mItemCount += count;
        }

        return count;
    }

    /**
     * Adds the pages in the specified collection to the end of this <tt>PagedList</tt>.
     * @param pages The collection of {@link Page}s to add.
     * @return The number of items to add in the collection.
     * @see #addPage(Page)
     * @see Pages#newPage(List)
     */
    public int addPages(Collection<? extends Page<? extends E>> pages) {
        DebugUtils.__checkError(pages == null, "pages == null");
        int count = 0;
        for (Page<? extends E> page : pages) {
            count += addPage(page);
        }

        return count;
    }

    /**
     * Removes the page at the specified <em>index</em> from this <tt>PagedList</tt>.
     * @param index The index of the page to remove.
     * @return The removed page's first item position in this <tt>PagedList</tt>.
     * @see #getPageForPosition(int)
     */
    public int removePage(int index) {
        DebugUtils.__checkError(index < 0 || index >= mPageCount, "Index out of bounds - pageIndex = " + index + ", pageCount = " + mPageCount);
        mItemCount -= ((Page<?>)mPages[index]).getCount();
        System.arraycopy(mPages, index + 1, mPages, index, --mPageCount - index);
        mPages[mPageCount] = null;  // Prevent memory leak.

        // Computes all pages start position from the removed page index.
        final int result = mPositions[index];
        for (int i = index, startPosition = result; i < mPageCount; ++i) {
            mPositions[i]  = startPosition;
            startPosition += ((Page<?>)mPages[i]).getCount();
        }

        return result;
    }

    /**
     * Returns the combined position of the page with the given the <em>position</em>.
     * <p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the page.
     * <li>bit 32-63 : Higher 32 bits of the index of the page.</p>
     * @param position The position of the item in this <tt>PagedList</tt>.
     * @return The combined position of the page.
     * @see Pages#getOriginalPage(long)
     * @see Pages#getOriginalPosition(long)
     */
    public long getPageForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
        int index = Arrays.binarySearch(mPositions, 0, mPageCount, position);
        if (index < 0) {
            index = -index - 2;
        }

        DebugUtils.__checkError(index < 0, "Invalid page index - " + index);
        return (((long)index << 32) | ((position - mPositions[index]) & 0xFFFFFFFFL));
    }

    @SuppressWarnings("resource")
    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(90);
        final Formatter formatter  = new Formatter(result);
        DebugUtils.dumpSummary(printer, result, 90, " Dumping %s [ itemCount = %d, pageCount = %d ] ", getClass().getSimpleName(), mItemCount, mPageCount);

        for (int i = 0; i < mPageCount; ++i) {
            final Page<?> page = (Page<?>)mPages[i];
            result.setLength(0);
            formatter.format("  Page %-2d ==> ", i);
            printer.println(DebugUtils.toString(page, result).append(" { position = ").append(mPositions[i]).append(", count = ").append(page.getCount()).append(" }").toString());
        }
    }
    private <T> T newArray(Object srcArray) {
        final Object newArray = Array.newInstance(srcArray.getClass().getComponentType(), mPageCount + ARRAY_CAPACITY_INCREMENT);
        System.arraycopy(srcArray, 0, newArray, 0, mPageCount);
        return (T)newArray;
    }
}
