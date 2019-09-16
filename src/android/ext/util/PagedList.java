package android.ext.util;

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

        // Adds the page collection to mPages.
        for (Page<? extends E> page : initPages) {
            mPages[mPageCount] = page;
            mPositions[mPageCount++] = mItemCount;
            mItemCount += page.getCount();
        }
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
     * @see #setPage(int, Page)
     */
    public Page<E> getPage(int index) {
        DebugUtils.__checkError(index < 0 || index >= mPageCount, "Invalid page index - " + index + ", page count = " + mPageCount);
        return (Page<E>)mPages[index];
    }

    /**
     * Replaces the page at the specified <em>index</em> in this <tt>PagedList</tt> with the specified <em>page</em>.
     * @param index The index at which to put the <em>page</em>.
     * @param page The {@link Page} to put.
     * @return The previous <tt>Page</tt> at the index.
     * @see #getPage(int)
     */
    public Page<E> setPage(int index, Page<? extends E> page) {
        DebugUtils.__checkError(Pages.getCount(page) == 0, "The page is null or 0-length");
        DebugUtils.__checkError(index < 0 || index >= mPageCount, "Invalid page index - " + index + ", page count = " + mPageCount);
        final Page<E> oldPage = (Page<E>)mPages[index];
        final int newCount = page.getCount();
        final int oldCount = oldPage.getCount();
        mPages[index] = page;

        if (oldCount != newCount) {
            mItemCount -= oldCount;
            mItemCount += newCount;
            computePositions(index);
        }

        return oldPage;
    }

    /**
     * Adds the specified <em>page</em> at the end of this <tt>PagedList</tt>.
     * @param page The {@link Page} to add.
     * @see #addPage(int, Page)
     * @see Pages#newPage(List)
     */
    public void addPage(Page<? extends E> page) {
        DebugUtils.__checkError(Pages.getCount(page) == 0, "The page is null or 0-length");
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
     * Inserts the specified <em>page</em> into this <tt>PagedList</tt> at the specified <em>index</em>. The
     * <em>page</em> is inserted before the current page at the specified <em>index</em>. If the <em>index</em>
     * is equal to the page count of this <tt>PagedList</tt>, the <em>page</em> is added at the end.
     * @param index The index at which to insert.
     * @param page The {@link Page} to add.
     * @see #addPage(Page)
     * @see Pages#newPage(List)
     */
    public void addPage(int index, Page<? extends E> page) {
        DebugUtils.__checkError(Pages.getCount(page) == 0, "The page is null or 0-length");
        DebugUtils.__checkError(index < 0 || index > mPageCount, "Invalid page index - " + page + ", page count = " + mPageCount);
        if (index == mPageCount) {
            addPage(page);
        } else {
            if (mPageCount < mPages.length) {
                System.arraycopy(mPages, index, mPages, index + 1, mPageCount - index);
            } else {
                final int newLength = mPageCount + ARRAY_CAPACITY_INCREMENT;
                final Object[] newPages = new Object[newLength];
                System.arraycopy(mPages, 0, newPages, 0, index);
                System.arraycopy(mPages, index, newPages, index + 1, mPageCount - index);
                mPages = newPages;
                mPositions = ArrayUtils.copyOf(mPositions, mPageCount, newLength);
            }

            ++mPageCount;
            mPages[index] = page;
            computePositions(index);
            mItemCount += page.getCount();
        }
    }

    /**
     * Removes the page at the specified <em>index</em> from this <tt>PagedList</tt>.
     * @param index The index of the page to remove.
     * @return The removed page's first item position in this <tt>PagedList</tt>.
     */
    public int removePage(int index) {
        DebugUtils.__checkError(index < 0 || index >= mPageCount, "Invalid page index - " + index + ", page count = " + mPageCount);
        mItemCount -= ((Page<?>)mPages[index]).getCount();
        System.arraycopy(mPages, index + 1, mPages, index, --mPageCount - index);
        mPages[mPageCount] = null;  // Prevent memory leak.

        // Computes all pages start position from the removed page index.
        return computePositions(index);
    }

    /**
     * Returns the combined position of the page with the given the <em>position</em>.
     * <p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the page.
     * <li>bit 32-63 : Higher 32 bits of the index of the page.</p>
     * @param position The position of the item in this <tt>PagedList</tt>.
     * @return The combined position of the page.
     * @see #getPositionForPage(int, int)
     * @see Pages#getOriginalPage(long)
     * @see Pages#getOriginalPosition(long)
     */
    public long getPageForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position - " + position + ", itemCount = " + mItemCount);
        int index = Arrays.binarySearch(mPositions, 0, mPageCount, position);
        if (index < 0) {
            index = -index - 2;
        }

        DebugUtils.__checkError(index < 0, "Invalid page index - " + index);
        return (((long)index << 32) | ((position - mPositions[index]) & 0xFFFFFFFFL));
    }

    /**
     * Returns the position with the given <em>page</em> and <em>position</em>.
     * @param page The index of the page.
     * @param position The index of the item in the <em>page</em>.
     * @return The position of the item in this <tt>PagedList</tt>.
     * @see #getPageForPosition(int)
     */
    public int getPositionForPage(int page, int position) {
        DebugUtils.__checkError(position < 0, "Invalid position - " + position);
        DebugUtils.__checkError(page < 0 || page >= mPageCount, "Invalid page index - " + page + ", page count = " + mPageCount);
        return (mPositions[page] + position);
    }

    @SuppressWarnings("resource")
    public final void dump(Printer printer) {
        final StringBuilder result = new StringBuilder(100);
        final Formatter formatter  = new Formatter(result);
        DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ itemCount = %d, pageCount = %d ] ", getClass().getSimpleName(), mItemCount, mPageCount);

        for (int i = 0; i < mPageCount; ++i) {
            final Page<?> page = (Page<?>)mPages[i];
            result.setLength(0);
            final int startPos = mPositions[i];
            final int count = page.getCount();
            formatter.format("  Page %-2d ==> ", i);
            printer.println(DebugUtils.toString(page, result).append(" { startPos = ").append(startPos).append(", endPos = ").append(startPos + count - 1).append(", count = ").append(count).append(" }").toString());
        }
    }

    private int computePositions(int index) {
        final int result = mPositions[index];
        for (int i = index, startPosition = result; i < mPageCount; ++i) {
            mPositions[i]  = startPosition;
            startPosition += ((Page<?>)mPages[i]).getCount();
        }

        return result;
    }
}
