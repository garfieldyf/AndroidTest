package android.ext.page;

import java.util.BitSet;
import java.util.Formatter;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import android.ext.cache.Cache;
import android.ext.util.DebugUtils;
import android.ext.util.UIHandler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Printer;
import android.view.View;

/**
 * Class <tt>PageAdapter</tt> allows to loading data by page.
 * <h3>PageAdapter's generic types</h3>
 * <p>The two types used by a page adapter are the following:</p>
 * <ol><li><tt>E</tt>, The item data type of the adapter.</li>
 * <li><tt>VH</tt>, A class that extends {@link ViewHolder} that
 * will be used by the adapter.</li></ol>
 * @author Garfield
 */
@SuppressWarnings("unchecked")
public abstract class PageAdapter<E, VH extends ViewHolder> extends Adapter<VH> {
    private int mItemCount;
    private final int mPageSize;
    private final int mInitialSize;
    private final int mPrefetchDistance;

    private final BitSet mLoadStates;
    private RecyclerView mRecyclerView;
    private final Cache<Integer, Page<E>> mPageCache;

    /**
     * Constructor
     * @param maxPageCount The maximum number of pages to allow in the page cache.
     * Pass <tt>0</tt> indicates the page cache is the <b>unlimited-size</b> cache.
     * @param initialSize The item count of the first page (page index == 0).
     * @param pageSize The item count of the each page (page index > 0).
     * @param prefetchDistance Defines how far to the first or last item in the
     * page to this adapter should prefetch the data. Pass <tt>0</tt> indicates
     * this adapter will not prefetch data.
     * @see #PageAdapter(Cache, int, int, int)
     * @see Pages#newPageCache(int)
     */
    public PageAdapter(int maxPageCount, int initialSize, int pageSize, int prefetchDistance) {
        this(Pages.<E>newPageCache(maxPageCount), initialSize, pageSize, prefetchDistance);
    }

    /**
     * Constructor
     * @param pageCache The {@link Page} {@link Cache} to store the pages.
     * @param initialSize The item count of the first page (page index == 0).
     * @param pageSize The item count of the each page (page index > 0).
     * @param prefetchDistance Defines how far to the first or last item in the
     * page to this adapter should prefetch the data. Pass <tt>0</tt> indicates
     * this adapter will not prefetch data.
     * @see #PageAdapter(int, int, int, int)
     */
    public PageAdapter(Cache<Integer, ? extends Page<? extends E>> pageCache, int initialSize, int pageSize, int prefetchDistance) {
        DebugUtils.__checkError(pageSize <= 0 || initialSize <= 0, "pageSize <= 0 || initialSize <= 0");
        DebugUtils.__checkError(prefetchDistance > Math.min(pageSize, initialSize), "prefetchDistance = " + prefetchDistance + " greater than pageSize = " + Math.min(pageSize, initialSize));
        mInitialSize = initialSize;
        mLoadStates  = new BitSet();
        mPageSize    = pageSize;
        mPageCache   = (Cache<Integer, Page<E>>)pageCache;
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
     * @param count The total number of items in this adapter.
     * @see #getItemCount()
     */
    public void setItemCount(int count) {
        DebugUtils.__checkUIThread("setItemCount");
        DebugUtils.__checkError(count < 0, "count < 0");
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        mItemCount = count;
        mPageCache.clear();
        mLoadStates.clear();
        UIHandler.notifyDataSetChanged(mRecyclerView);
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to obtain the item when the
     * item was not present.</p>
     * @param position The adapter position of the item in this adapter.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(View)
     * @see #getItem(ViewHolder)
     */
    public E getItem(int position) {
        DebugUtils.__checkUIThread("getItem");
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
        final long combinedPosition = getPageForPosition(position);
        final int page = Pages.getOriginalPage(combinedPosition);
        final Page<E> result = getPage(page);
        if (mPrefetchDistance > 0) {
            prefetchPage(page, (int)combinedPosition, position, mPrefetchDistance);
        }

        return (result != null ? result.getItem((int)combinedPosition) : null);
    }

    /**
     * Equivalent to calling <tt>getItem(recyclerView.getChildAdapterPosition(view))</tt>.
     * @param child The child of the <tt>RecyclerView</tt> to query for the
     * <tt>ViewHolder</tt>'s adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(int)
     * @see #getItem(ViewHolder)
     */
    public final E getItem(View child) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        final int position = mRecyclerView.getChildAdapterPosition(child);
        return (position != RecyclerView.NO_POSITION ? getItem(position) : null);
    }

    /**
     * Equivalent to calling <tt>getItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(int)
     * @see #getItem(View)
     */
    public final E getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != RecyclerView.NO_POSITION ? getItem(position) : null);
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>Unlike {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int)}
     * when the item was not present.</p>
     * @param position The adapter position of the item in this adapter.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(View)
     * @see #peekItem(ViewHolder)
     */
    public E peekItem(int position) {
        DebugUtils.__checkUIThread("peekItem");
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Index out of bounds - position = " + position + ", itemCount = " + mItemCount);
        final long combinedPosition = getPageForPosition(position);
        final Page<E> result = mPageCache.get(Pages.getOriginalPage(combinedPosition));
        return (result != null ? result.getItem((int)combinedPosition) : null);
    }

    /**
     * Equivalent to calling <tt>peekItem(recyclerView.getChildAdapterPosition(view))</tt>.
     * @param child The child of the <tt>RecyclerView</tt> to query for the
     * <tt>ViewHolder</tt>'s adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(int)
     * @see #peekItem(ViewHolder)
     */
    public final E peekItem(View child) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        final int position = mRecyclerView.getChildAdapterPosition(child);
        return (position != RecyclerView.NO_POSITION ? peekItem(position) : null);
    }

    /**
     * Equivalent to calling <tt>peekItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(int)
     * @see #peekItem(View)
     */
    public final E peekItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != RecyclerView.NO_POSITION ? peekItem(position) : null);
    }

    /**
     * Returns the {@link Page} associated with the specified index <em>page</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to obtain the page when the
     * page was not present.</p>
     * @param page The index of the page.
     * @return The <tt>Page</tt> at the specified index, or <tt>null</tt> if there was not present.
     * @see #peekPage(int)
     */
    public Page<E> getPage(int page) {
        DebugUtils.__checkUIThread("getPage");
        DebugUtils.__checkError(page < 0, "page < 0");
        Page<E> result = mPageCache.get(page);
        if (result == null && !mLoadStates.get(page)) {
            // Computes the startPosition and itemCount to load.
            final int startPosition, itemCount;
            if (page == 0) {
                itemCount = mInitialSize;
                startPosition = 0;
            } else {
                itemCount = mPageSize;
                startPosition = (page - 1) * mPageSize + mInitialSize;
            }

            // Loads the page data and marks the page loading state.
            mLoadStates.set(page);
            result = loadPage(page, startPosition, itemCount);
            if (Pages.getCount(result) > 0) {
                // Clears the page loading state.
                mLoadStates.clear(page);
                mPageCache.put(page, result);
            }
        }

        return result;
    }

    /**
     * Returns the {@link Page} associated with the specified index <em>page</em> in this adapter.
     * <p>Unlike {@link #getPage}, this method do not call {@link #loadPage(int, int, int)} when
     * the page was not present.</p>
     * @param page The index of the page.
     * @return The <tt>Page</tt> at the specified index, or <tt>null</tt> if there was not present.
     * @see #getPage(int)
     */
    public Page<E> peekPage(int page) {
        DebugUtils.__checkUIThread("peekPage");
        return mPageCache.get(page);
    }

    /**
     * Equivalent to calling <tt>setPage(page, data, null)</tt>.
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The <tt>Page</tt> to add.
     * @see #setPage(int, Page, Object)
     * @see Pages#newPage(java.util.List)
     */
    public final void setPage(int page, Page<? extends E> data) {
        setPage(page, data, null);
    }

    /**
     * Sets the {@link Page} at the specified index <em>page</em> in this adapter.
     * This method will be call {@link #notifyItemRangeChanged(int, int, Object)}
     * when the <em>page</em> has added. <p>This is useful when asynchronously
     * loading to prevent blocking the UI.</p>
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The <tt>Page</tt> to set.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged}.
     * @see #setPage(int, Page)
     * @see Pages#newPage(java.util.List)
     */
    public void setPage(int page, Page<? extends E> data, Object payload) {
        DebugUtils.__checkUIThread("setPage");
        DebugUtils.__checkError(page < 0, "page < 0");
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");

        // Clears the page loading state when the page is load complete.
        mLoadStates.clear(page);
        final int itemCount = Pages.getCount(data);
        if (itemCount > 0) {
            mPageCache.put(page, (Page<E>)data);
            UIHandler.notifyItemRangeChanged(mRecyclerView, getPositionForPage(page, 0), itemCount, payload);
        }
    }

    /**
     * Adds the {@link Page} at the specified index <em>page</em> in this adapter.
     * This method will be call {@link #notifyItemRangeInserted(int, int)} when
     * the <em>page</em> has added. <p>This is useful when asynchronously loading
     * to prevent blocking the UI.</p>
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The <tt>Page</tt> to add.
     * @see Pages#newPage(java.util.List)
     */
    public void addPage(int page, Page<? extends E> data) {
        DebugUtils.__checkUIThread("addPage");
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");

        // Clears the page loading state when the page is load complete.
        mLoadStates.clear(page);
        final int itemCount = Pages.getCount(data);
        if (itemCount > 0) {
            mItemCount += itemCount;
            mPageCache.put(page, (Page<E>)data);
            UIHandler.notifyItemRangeInserted(mRecyclerView, getPositionForPage(page, 0), itemCount);
        }
    }

    /**
     * Returns a copy of the current page cache of this adapter.
     * @return A copy of the page cache.
     */
    public final Map<Integer, Page<E>> snapshot() {
        return mPageCache.snapshot();
    }

    /**
     * Returns the {@link RecyclerView} associated with this adapter.
     * @return The {@link RecyclerView} object or <tt>null</tt> if
     * this adapter not attached to the <tt>RecyclerView</tt>.
     */
    public final RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    /**
     * Returns the item count in the specified page.
     * @param page The index of the page.
     * @return The item count.
     */
    public final int getPageSize(int page) {
        DebugUtils.__checkError(page < 0, "page < 0");
        return (page > 0 ? mPageSize : mInitialSize);
    }

    /**
     * Returns the combined position of the page with the given the <em>position</em>.
     * <p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the page.
     * <li>bit 32-63 : Higher 32 bits of the index of the page.</p>
     * @param position The adapter position of the item in this adapter.
     * @return The combined position of the page.
     * @see #getPositionForPage(int, int)
     * @see Pages#getOriginalPage(long)
     * @see Pages#getOriginalPosition(long)
     */
    public final long getPageForPosition(int position) {
        DebugUtils.__checkError(position < 0, "position < 0");
        final int offset = position - mInitialSize;
        return (offset < 0 ? (position & 0xFFFFFFFFL) : (((long)(offset / mPageSize + 1) << 32) | ((offset % mPageSize) & 0xFFFFFFFFL)));
    }

    /**
     * Returns the adapter position with the given <em>page</em> and <em>position</em>.
     * @param page The index of the page.
     * @param position The index of the item in the <em>page</em>.
     * @return The adapter position of the item in this adapter.
     * @see #getPageForPosition(int)
     */
    public final int getPositionForPage(int page, int position) {
        DebugUtils.__checkError(page < 0 || position < 0, "page < 0 || position < 0");
        return (page > 0 ? (page - 1) * mPageSize + mInitialSize + position : position);
    }

    @SuppressWarnings("resource")
    public final void dump(Printer printer) {
        DebugUtils.__checkUIThread("dump");
        final StringBuilder result = new StringBuilder(128);
        final Formatter formatter  = new Formatter(result);
        final Set<Entry<Integer, Page<E>>> entries = mPageCache.snapshot().entrySet();

        DebugUtils.dumpSummary(printer, result, 100, " Dumping %s [ initialSize = %d, pageSize = %d, itemCount = %d ] ", getClass().getSimpleName(), mInitialSize, mPageSize, mItemCount);
        result.setLength(0);
        printer.println(DebugUtils.toString(mPageCache, result.append("  PageCache [ ")).append(", size = ").append(entries.size()).append(" ]").toString());

        for (Entry<Integer, Page<E>> entry : entries) {
            final Page<E> page = entry.getValue();
            result.setLength(0);

            formatter.format("    Page %-2d ==> ", entry.getKey());
            printer.println(DebugUtils.toString(page, result).append(" { count = ").append(page.getCount()).append(" }").toString());
        }
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    /**
     * Returns the {@link Page} at the given index <em>page</em>. Subclasses
     * must implement this method to return <tt>Page</tt> for a particular page.
     * <p>If you want to asynchronously load the page data to prevent blocking
     * the UI, it is possible to return <tt>null</tt> and at a later time call
     * {@link #setPage(int, Page, Object)}.<p>
     * @param page The index of the page whose data should be returned.
     * @param startPosition The position of the first item to load.
     * @param itemCount The number of items to load.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     */
    protected abstract Page<E> loadPage(int page, int startPosition, int itemCount);

    /**
     * Prefetch the {@link Page} with the given <em>page</em> and <em>position</em>. The
     * default implementation load the previous and next page data from the current page.
     * @param page The index of the current page.
     * @param position The index of the item in the <em>page</em>.
     * @param adapterPosition The adapter position of the item in this adapter.
     * @param prefetchDistance Defines how far to the first or last item in the page.
     */
    protected void prefetchPage(int page, int position, int adapterPosition, int prefetchDistance) {
        // Prefetch the previous page data.
        if (page > 0 && position == prefetchDistance - 1) {
            DebugUtils.__checkDebug(true, "PageAdapter", "prefetchPage = " + (page - 1) + ", position = " + position + ", adapterPosition = " + adapterPosition);
            getPage(page - 1);
        }

        final int lastPage = (mItemCount - mInitialSize - 1) / mPageSize + 1;
        if (page < lastPage) {
            // Prefetch the next page data.
            final int pageSize = (page > 0 ? mPageSize : mInitialSize);
            if (position == pageSize - prefetchDistance) {
                DebugUtils.__checkDebug(true, "PageAdapter", "prefetchPage = " + (page + 1) + ", position = " + position + ", adapterPosition = " + adapterPosition);
                getPage(page + 1);
            }
        }
    }
}
