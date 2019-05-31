package android.ext.page;

import static android.ext.util.ArrayUtils.EMPTY_INT_ARRAY;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.List;
import android.ext.util.DebugUtils;
import android.util.Printer;

/**
 * Class <tt>PagedList</tt> allows to loading data by page.
 * @author Garfield
 */
public class PagedList<E> {
    private static final int ARRAY_CAPACITY_INCREMENT = 12;

    /**
     * The number of valid positions in the mPositions array.
     */
    private int mSize;

    /**
     * The number of items in this <tt>PagedList</tt>.
     */
    private int mCount;

    /**
     * The page start positions in this <tt>PagedList</tt>.
     */
    private int[] mPositions;

    /**
     * The {@link Page} <tt>List</tt>.
     */
    private final List<Page<? extends E>> mPagedList;

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
        mPagedList = new ArrayList<Page<? extends E>>(capacity);
        mPositions = (capacity > 0 ? new int[capacity] : EMPTY_INT_ARRAY);
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
        if (mCount > 0) {
            mPagedList.clear();
            mCount = mSize = 0;
        }
    }

    /**
     * Returns the number of items in this <tt>PagedList</tt>.
     * @return The number of items in this <tt>PagedList</tt>.
     * @see #getItem(int)
     */
    public int getItemCount() {
        return mCount;
    }

    /**
     * Returns the item at the specified <em>position</em> in this <tt>PagedList</tt>.
     * @param position The index of the item.
     * @return The item at the specified <em>position</em>.
     * @see #getItemCount()
     */
    public E getItem(int position) {
        final long combinedPosition = getPageForPosition(position);
        final int page = Pages.getOriginalPage(combinedPosition);
        return mPagedList.get(page).getItem((int)combinedPosition);
    }

    /**
     * Returns the number of pages in this <tt>PagedList</tt>.
     * @return The number of pages in this <tt>PagedList</tt>.
     * @see #getPage(int)
     */
    public int getPageCount() {
        return mPagedList.size();
    }

    /**
     * Returns the page at the specified <em>page</em> in this <tt>PagedList</tt>.
     * @param page The index of the page.
     * @return The {@link Page} at the specified <em>page</em>.
     * @see #getPageCount()
     */
    public Page<? extends E> getPage(int page) {
        DebugUtils.__checkError(page < 0 || page >= mPagedList.size(), "Index out of bounds - page = " + page + ", count = " + mPagedList.size());
        return mPagedList.get(page);
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
            addPosition(mCount);
            mPagedList.add(page);
            mCount += count;
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
        DebugUtils.__checkError(position < 0 || position >= mCount, "Index out of bounds - position = " + position + ", itemCount = " + mCount);
        int page = Arrays.binarySearch(mPositions, 0, mSize, position);
        if (page < 0) {
            page = -page - 2;
        }

        DebugUtils.__checkError(page < 0, "Invalid page index - " + page);
        return (((long)page << 32) | ((position - mPositions[page]) & 0xFFFFFFFFL));
    }

    @SuppressWarnings("resource")
    public final void dump(Printer printer) {
        final int pageCount = mPagedList.size();
        final StringBuilder result = new StringBuilder(80);
        final Formatter formatter  = new Formatter(result);

        DebugUtils.dumpSummary(printer, result, 80, " Dumping %s [ itemCount = %d, pageCount = %d ] ", getClass().getSimpleName(), mCount, pageCount);
        result.setLength(0);
        printer.println(toString(result.append("  Positions = ")));

        for (int i = 0; i < pageCount; ++i) {
            final Page<? extends E> page = mPagedList.get(i);
            result.setLength(0);
            formatter.format("  Page %-2d ==> ", i);
            printer.println(DebugUtils.toString(page, result).append(" { count = ").append(page.getCount()).append(" }").toString());
        }
    }

    private void addPosition(int position) {
        if (mSize >= mPositions.length) {
            final int[] newPositions = new int[mSize + ARRAY_CAPACITY_INCREMENT];
            System.arraycopy(mPositions, 0, newPositions, 0, mSize);
            mPositions = newPositions;
        }

        mPositions[mSize++] = position;
    }

    private String toString(StringBuilder result) {
        result.append('[');
        for (int i = 0; i < mSize; ++i) {
            if (i != 0) {
                result.append(", ");
            }

            result.append(mPositions[i]);
        }

        return result.append(']').toString();
    }
}
