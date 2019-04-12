package android.ext.page;

import android.ext.cache.Cache;
import android.ext.page.Pages.PageAdapterImpl;
import android.ext.page.Pages.PageLoader;
import android.ext.util.DebugUtils;
import android.ext.util.UIHandler;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Printer;
import android.view.View;

/**
 * Class <tt>RecyclerPageAdapter</tt> allows to loading data by page.
 * <h5>RecyclerPageAdapter's generic types</h5>
 * <p>The two types used by a page adapter are the following:</p>
 * <ol><li><tt>E</tt>, The item data type of the adapter.</li>
 * <li><tt>VH</tt>, A class that extends <tt>ViewHolder</tt> that will
 * be used by the adapter.</li></ol>
 * @author Garfield
 */
public abstract class RecyclerPageAdapter<E, VH extends ViewHolder> extends Adapter<VH> implements PageLoader<E> {
    private RecyclerView mRecyclerView;
    private final PageAdapterImpl<E> mImpl;

    /**
     * Constructor
     * @param maxPages The maximum number of pages to allow in the page cache.
     * Pass <tt>0</tt> that the page cache is the <b>unlimited-size</b> cache.
     * @param initialSize The item count of the first page (page index == 0).
     * @param pageSize The item count of the each page (page index > 0).
     * @param prefetchDistance Defines how far to the first or last item in the
     * page to this adapter should prefetch the data. Pass <tt>0</tt> indicates
     * this adapter will not prefetch data.
     * @see #RecyclerPageAdapter(Cache, int, int, int)
     * @see Pages#newPageCache(int)
     */
    public RecyclerPageAdapter(int maxPages, int initialSize, int pageSize, int prefetchDistance) {
        this(Pages.<E>newPageCache(maxPages), initialSize, pageSize, prefetchDistance);
    }

    /**
     * Constructor
     * @param pageCache The {@link Page} {@link Cache} to store the pages.
     * @param initialSize The item count of the first page (page index == 0).
     * @param pageSize The item count of the each page (page index > 0).
     * @param prefetchDistance Defines how far to the first or last item in the
     * page to this adapter should prefetch the data. Pass <tt>0</tt> indicates
     * this adapter will not prefetch data.
     * @see #RecyclerPageAdapter(int, int, int, int)
     */
    public RecyclerPageAdapter(Cache<Integer, ? extends Page<? extends E>> pageCache, int initialSize, int pageSize, int prefetchDistance) {
        mImpl = new PageAdapterImpl<E>(pageCache, initialSize, pageSize, prefetchDistance, this);
    }

    /**
     * @see #setItemCount(int)
     */
    @Override
    public int getItemCount() {
        return mImpl.mItemCount;
    }

    /**
     * Sets total number of items in this adapter.
     * @param count The total number of items in this adapter.
     * @see #getItemCount()
     */
    public void setItemCount(int count) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        if (mImpl.mItemCount != count) {
            mImpl.setItemCount(count);
            UIHandler.notifyDataSetChanged(mRecyclerView);
        }
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to obtain the item when the
     * item was not present.</p>
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(View)
     * @see #getItem(ViewHolder)
     */
    public E getItem(int position) {
        return mImpl.getItem(position);
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
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(View)
     * @see #peekItem(ViewHolder)
     */
    public E peekItem(int position) {
        return mImpl.peekItem(position);
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
        return mImpl.getPage(page);
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
        return mImpl.mPageCache.get(page);
    }

    /**
     * Equivalent to calling <tt>setPage(page, data, null)</tt>.
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The <tt>Page</tt> object.
     * @see #setPage(int, Page, Object)
     * @see Pages#newPage(java.util.List)
     */
    public final void setPage(int page, Page<E> data) {
        setPage(page, data, null);
    }

    /**
     * Sets the {@link Page} at the specified index <em>page</em> in this adapter.
     * <p>This is useful when asynchronously loading to prevent blocking the UI.</p>
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The <tt>Page</tt> object.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged(int, int, Object)}.
     * @see #setPage(int, Page)
     * @see Pages#newPage(java.util.List)
     */
    public void setPage(int page, Page<E> data, Object payload) {
        DebugUtils.__checkUIThread("setPage");
        DebugUtils.__checkError(page < 0, "page < 0");
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");

        // Clears the page loading state when the page is load complete.
        mImpl.mPageStates.clear(page);
        final int itemCount = Pages.getCount(data);
        if (itemCount > 0) {
            mImpl.mPageCache.put(page, data);
            UIHandler.notifyItemRangeChanged(mRecyclerView, mImpl.getPositionForPage(page, 0), itemCount, payload);
        }
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
     * Returns the maximum number of pages in this adapter.
     * @return The maximum number of pages.
     */
    public final int getMaxPageCount() {
        return mImpl.getMaxPageCount();
    }

    /**
     * Returns the item count of the specified index <em>page</em>.
     * @param page The index of the page.
     * @return The item count.
     */
    public final int getPageSize(int page) {
        DebugUtils.__checkError(page < 0, "page < 0");
        return (page > 0 ? mImpl.mPageSize : mImpl.mInitialSize);
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
        return mImpl.getPageForPosition(position);
    }

    /**
     * Returns the adapter position with the given <em>page</em> and <em>position</em>.
     * @param page The index of the page.
     * @param position The index of the item in the <em>page</em>.
     * @return The adapter position of the item in this adapter.
     * @see #getPageForPosition(int)
     */
    public final int getPositionForPage(int page, int position) {
        return mImpl.getPositionForPage(page, position);
    }

    public final void dump(Printer printer) {
        mImpl.dump(printer, getClass().getSimpleName());
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    /**
     * Returns the {@link Page} at the given position <em>page</em>. Subclasses
     * must implement this method to return <tt>Page</tt> for a particular page.
     * <p>If you want to asynchronously load the page data to prevent blocking
     * the UI, it is possible to return <tt>null</tt> and at a later time call
     * {@link #setPage(int, Page, Object)}.<p>
     * @param page The index of the page whose data should be returned.
     * @param startPosition The position of the first item to load.
     * @param itemCount The number of items to load.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     */
    public abstract Page<E> loadPage(int page, int startPosition, int itemCount);
}