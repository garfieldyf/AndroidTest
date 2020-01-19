package android.ext.page;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import android.ext.cache.ArrayMapCache;
import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.widget.BaseAdapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Printer;
import java.util.BitSet;
import java.util.Formatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Class <tt>PageAdapter</tt> allows to loading data by page.
 * <h3>PageAdapter's generic types</h3>
 * <p>The two types used by a page adapter are the following:</p>
 * <ol><li><tt>E</tt>, The item data type of the adapter.</li>
 * <li><tt>VH</tt>, A class that extends {@link ViewHolder} that
 * will be used by the adapter.</li></ol>
 * @author Garfield
 */
public abstract class PageAdapter<E, VH extends ViewHolder> extends BaseAdapter<VH> {
    private final int mPageSize;
    private final int mInitialSize;
    private final int mPrefetchDistance;

    private int mItemCount;
    private final BitSet mLoadStates;
    private final Cache<Integer, List<E>> mPageCache;

    /**
     * Constructor
     * <p>Creates a <tt>PageAdapter</tt> with an <b>unlimitted-size</b> capacity.</p>
     * @param initialSize The item count of the first page (pageIndex = 0).
     * @param pageSize The item count of the each page (pageIndex > 0).
     * @param prefetchDistance Defines how far to the first or last item in the page
     * to this adapter should prefetch the data. Pass <tt>0</tt> indicates this adapter
     * will not prefetch data.
     * @see #PageAdapter(int, int, int, int)
     */
    public PageAdapter(int initialSize, int pageSize, int prefetchDistance) {
        this(0, initialSize, pageSize, prefetchDistance);
    }

    /**
     * Constructor
     * <p>Creates a <tt>PageAdapter</tt> with a <em>maxPageCount</em> capacity.</p>
     * @param maxPageCount The maximum number of pages to allow in the page cache.
     * @param initialSize The item count of the first page (pageIndex = 0).
     * @param pageSize The item count of the each page (pageIndex > 0).
     * @param prefetchDistance Defines how far to the first or last item in the page
     * to this adapter should prefetch the data. Pass <tt>0</tt> indicates this adapter
     * will not prefetch data.
     * @see #PageAdapter(int, int, int)
     */
    public PageAdapter(int maxPageCount, int initialSize, int pageSize, int prefetchDistance) {
        DebugUtils.__checkError(pageSize <= 0 || initialSize <= 0, "pageSize <= 0 || initialSize <= 0");
        DebugUtils.__checkError(prefetchDistance > Math.min(pageSize, initialSize), "prefetchDistance = " + prefetchDistance + " greater than pageSize = " + Math.min(pageSize, initialSize));
        mPageCache   = (maxPageCount > 0 ? new SimpleLruCache<Integer, List<E>>(maxPageCount) : new ArrayMapCache<Integer, List<E>>(8));
        mPageSize    = pageSize;
        mLoadStates  = new BitSet();
        mInitialSize = initialSize;
        mPrefetchDistance = prefetchDistance;
    }

    /**
     * @see #setItemCount(int)
     */
    @Override
    public int getItemCount() {
        return mItemCount;
    }

    /**
     * Sets total number of items in this adapter.
     * @param itemCount The total number of items in this adapter.
     * @see #getItemCount()
     */
    public void setItemCount(int itemCount) {
        DebugUtils.__checkUIThread("setItemCount");
        DebugUtils.__checkError(itemCount < 0, "itemCount < 0");
        mPageCache.clear();
        mLoadStates.clear();
        mItemCount = itemCount;
        postNotifyDataSetChanged();
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to retrieve the item when the
     * item was not present.</p>
     * @param position The adapter position of the item in this adapter.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(ViewHolder)
     */
    public E getItem(int position) {
        DebugUtils.__checkUIThread("getItem");
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        final long combinedPosition = getPageForPosition(position);
        final int pageIndex = Pages.getOriginalPage(combinedPosition);
        final List<E> page  = getPage(pageIndex);
        if (mPrefetchDistance > 0) {
            prefetchPage(pageIndex, (int)combinedPosition, mPrefetchDistance);
        }

        return (page != null ? page.get((int)combinedPosition) : null);
    }

    /**
     * Equivalent to calling <tt>getItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(int)
     */
    public final E getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != NO_POSITION ? getItem(position) : null);
    }

    /**
     * Equivalent to calling <tt>setItem(position, value, null)</tt>.
     * @param position The adapter position of the item in this adapter.
     * @param value The value to set.
     * @return The previous item at the specified <em>position</em>
     * or <tt>null</tt> if the item not found.
     * @see #setItem(int, E, Object)
     */
    public final E setItem(int position, E value) {
        return setItem(position, value, null);
    }

    /**
     * Sets the item at the specified <em>position</em> in this adapter with the specified <em>value</em>.
     * This method will be call {@link #notifyItemChanged(int, Object)} when the <em>value</em> has set.
     * <p>Unlike {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int)} when
     * the item was not present.</p>
     * @param position The adapter position of the item in this adapter.
     * @param value The value to set.
     * @param payload Optional parameter, pass to {@link #notifyItemChanged}.
     * @return The previous item at the specified <em>position</em> or <tt>null</tt> if the item not found.
     * @see #setItem(int, E)
     */
    public E setItem(int position, E value, Object payload) {
        DebugUtils.__checkUIThread("setItem");
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);

        E previous = null;
        final long combinedPosition = getPageForPosition(position);
        final List<E> page = mPageCache.get(Pages.getOriginalPage(combinedPosition));
        if (page != null) {
            previous = page.set((int)combinedPosition, value);
            postNotifyItemRangeChanged(position, 1, payload);
        }

        return previous;
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>Unlike {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int)}
     * when the item was not present.</p>
     * @param position The adapter position of the item in this adapter.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(ViewHolder)
     */
    public E peekItem(int position) {
        DebugUtils.__checkUIThread("peekItem");
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        final long combinedPosition = getPageForPosition(position);
        final List<E> page = mPageCache.get(Pages.getOriginalPage(combinedPosition));
        return (page != null ? page.get((int)combinedPosition) : null);
    }

    /**
     * Equivalent to calling <tt>peekItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(int)
     */
    public final E peekItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != NO_POSITION ? peekItem(position) : null);
    }

    /**
     * Returns the page associated with the specified <em>pageIndex</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to retrieve the page
     * when the page was not present.</p>
     * @param pageIndex The index of the page.
     * @return The page at the specified index, or <tt>null</tt> if there was not present.
     * @see #peekPage(int)
     */
    @SuppressWarnings("unchecked")
    public List<E> getPage(int pageIndex) {
        DebugUtils.__checkUIThread("getPage");
        DebugUtils.__checkError(pageIndex < 0, "pageIndex < 0");
        List<E> page = mPageCache.get(pageIndex);
        if (page == null && !mLoadStates.get(pageIndex)) {
            // Computes the startPosition and itemCount to load.
            final int startPosition, itemCount;
            if (pageIndex == 0) {
                itemCount = mInitialSize;
                startPosition = 0;
            } else {
                itemCount = mPageSize;
                startPosition = (pageIndex - 1) * mPageSize + mInitialSize;
            }

            // Loads the page data and sets the page loading state.
            mLoadStates.set(pageIndex);
            page = (List<E>)loadPage(pageIndex, startPosition, itemCount);
            if (ArrayUtils.getSize(page) > 0) {
                // Clears the page loading state.
                mLoadStates.clear(pageIndex);
                mPageCache.put(pageIndex, page);
            }
        }

        return page;
    }

    /**
     * Returns the page associated with the specified <em>pageIndex</em> in this adapter.
     * <p>Unlike {@link #getPage}, this method do not call {@link #loadPage(int, int, int)}
     * when the page was not present.</p>
     * @param pageIndex The index of the page.
     * @return The page at the specified index, or <tt>null</tt> if there was not present.
     * @see #getPage(int)
     */
    public List<E> peekPage(int pageIndex) {
        DebugUtils.__checkUIThread("peekPage");
        return mPageCache.get(pageIndex);
    }

    /**
     * Equivalent to calling <tt>setPage(pageIndex, page, null)</tt>.
     * @param pageIndex The index of the page.
     * @param page May be <tt>null</tt>. The page to add.
     * @see #setPage(int, List, Object)
     */
    public final void setPage(int pageIndex, List<?> page) {
        setPage(pageIndex, page, null);
    }

    /**
     * Sets the page at the specified <em>pageIndex</em> in this adapter. This
     * method will be call {@link #notifyItemRangeChanged(int, int, Object)}
     * when the <em>page</em> has added. <p>This is useful when asynchronously
     * loading to prevent blocking the UI.</p>
     * @param pageIndex The index of the page.
     * @param page May be <tt>null</tt>. The page to add.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged}.
     * @see #setPage(int, List)
     */
    @SuppressWarnings("unchecked")
    public void setPage(int pageIndex, List<?> page, Object payload) {
        DebugUtils.__checkUIThread("setPage");
        DebugUtils.__checkError(pageIndex < 0, "pageIndex < 0");

        // Clears the page loading state when the page is load complete.
        mLoadStates.clear(pageIndex);
        final int itemCount = ArrayUtils.getSize(page);
        if (itemCount > 0) {
            mPageCache.put(pageIndex, (List<E>)page);
            postNotifyItemRangeChanged(getPositionForPage(pageIndex), itemCount, payload);
        }
    }

//    /**
//     * Adds the page at the specified <em>pageIndex</em> in this adapter. This
//     * method will be call {@link #notifyItemRangeInserted(int, int)} when the
//     * <em>page</em> has added. <p>This is useful when asynchronously loading
//     * to prevent blocking the UI.</p>
//     * @param pageIndex The index of the page.
//     * @param page May be <tt>null</tt>. The page to add.
//     */
//    @SuppressWarnings("unchecked")
//    public void addPage(int pageIndex, List<?> page) {
//        DebugUtils.__checkUIThread("addPage");
//        DebugUtils.__checkError(pageIndex < 0, "pageIndex < 0");
//
//        // Clears the page loading state when the page is load complete.
//        mLoadStates.clear(pageIndex);
//        final int itemCount = ArrayUtils.getSize(page);
//        if (itemCount > 0) {
//            mItemCount += itemCount;
//            mPageCache.put(pageIndex, (List<E>)page);
//            postNotifyItemRangeInserted(getPositionForPage(pageIndex), itemCount);
//        }
//    }

    /**
     * Returns the item count in the specified page.
     * @param pageIndex The index of the page.
     * @return The item count.
     */
    public final int getPageSize(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0, "pageIndex < 0");
        return (pageIndex > 0 ? mPageSize : mInitialSize);
    }

    /**
     * Given a position within this adapter, returns the combined position of the
     * corresponding page within the array of pages.<p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the page.
     * <li>bit 32-63 : Higher 32 bits of the index of the page.</p>
     * @param position The adapter position of the item.
     * @return The combined position of the page.
     * @see #getPositionForPage(int)
     * @see Pages#getOriginalPage(long)
     * @see Pages#getOriginalPosition(long)
     */
    public final long getPageForPosition(int position) {
        DebugUtils.__checkError(position < 0, "position < 0");
        final int pageIndex, itemIndex, offset = position - mInitialSize;
        if (offset < 0) {
            pageIndex = 0;
            itemIndex = position;
        } else {
            pageIndex = offset / mPageSize + 1;
            itemIndex = offset % mPageSize;
        }

        return (((long)pageIndex << 32) | (itemIndex & 0xFFFFFFFFL));
    }

    /**
     * Given the index of a page within this adapter, returns the starting
     * position of that page within this adapter.
     * @param pageIndex The index of the page.
     * @return The starting position of that page within this adapter.
     * @see #getPageForPosition(int)
     */
    public final int getPositionForPage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0, "pageIndex < 0");
        return (pageIndex > 0 ? (pageIndex - 1) * mPageSize + mInitialSize : 0);
    }

    public final void dump(Printer printer) {
        DebugUtils.__checkUIThread("dump");
        final StringBuilder result = new StringBuilder(128);
        final Formatter formatter  = new Formatter(result);
        final Set<Entry<Integer, List<E>>> entries = mPageCache.snapshot().entrySet();

        DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ initialSize = %d, pageSize = %d, itemCount = %d ] ", getClass().getSimpleName(), mInitialSize, mPageSize, mItemCount);
        result.setLength(0);
        printer.println(DebugUtils.toString(mPageCache, result.append("  PageCache [ ")).append(", size = ").append(entries.size()).append(" ]").toString());

        for (Entry<Integer, List<E>> entry : entries) {
            final List<E> page = entry.getValue();
            result.setLength(0);

            formatter.format("    Page %-2d ==> ", entry.getKey());
            printer.println(DebugUtils.toString(page, result).append(" { count = ").append(page.size()).append(" }").toString());
        }
    }

    /**
     * Prefetch the page with the given <em>pageIndex</em> and <em>position</em>. The
     * default implementation load the previous and next page near by the current page.
     * @param pageIndex The index of the current page.
     * @param position The index of the item in the page.
     * @param prefetchDistance Defines how far to the first or last item in the page.
     */
    protected void prefetchPage(int pageIndex, int position, int prefetchDistance) {
        // Prefetch the previous page data.
        if (pageIndex > 0 && position == prefetchDistance - 1) {
            getPage(pageIndex - 1);
        }

        final int lastPage = (mItemCount - mInitialSize - 1) / mPageSize + 1;
        if (pageIndex < lastPage) {
            // Prefetch the next page data.
            final int pageSize = (pageIndex > 0 ? mPageSize : mInitialSize);
            if (position == pageSize - prefetchDistance) {
                getPage(pageIndex + 1);
            }
        }
    }

    /**
     * Returns a page at the given the <em>pageIndex</em>. Subclasses must implement
     * this method to return <tt>List</tt> for a particular page. <p>If you want to
     * asynchronously load the page data to prevent blocking the UI, it is possible
     * to return <tt>null</tt> and at a later time call {@link #setPage}.<p>
     * @param pageIndex The index of the page whose data should be returned.
     * @param startPosition The starting position of the page within this adapter.
     * @param itemCount The number of items to load.
     * @return The page, or <tt>null</tt>.
     */
    protected abstract List<?> loadPage(int pageIndex, int startPosition, int itemCount);
}
