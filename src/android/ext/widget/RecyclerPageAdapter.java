package android.ext.widget;

import java.util.List;
import org.json.JSONArray;
import android.ext.util.ArrayUtils;
import android.ext.util.Caches.Cache;
import android.ext.util.DebugUtils;
import android.ext.util.JSONUtils;
import android.ext.util.UIHandler;
import android.ext.widget.Pages.JSONPage;
import android.ext.widget.Pages.ListPage;
import android.ext.widget.Pages.Page;
import android.ext.widget.Pages.PageAdapterImpl;
import android.ext.widget.Pages.PageLoader;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;

/**
 * Class <tt>RecyclerPageAdapter</tt> allows to loading data by page.
 * <h5>RecyclerPageAdapter's generic types</h5>
 * <p>The two types used by a page adapter are the following:</p>
 * <ol><li><tt>E</tt>, The item data type of the adapter.</li>
 * <li><tt>VH</tt>, A class that extends <tt>ViewHolder</tt> that will
 * be used by the adapter.</li></ol>
 * @author Garfield
 * @version 3.0
 */
public abstract class RecyclerPageAdapter<E, VH extends ViewHolder> extends Adapter<VH> implements PageLoader<E> {
    private RecyclerView mRecyclerView;
    private final PageAdapterImpl<E> mImpl;

    /**
     * Constructor
     * @param maxPages The maximum number of pages to allow in the page cache.
     * Pass <tt>0</tt> that the page cache is the <b>unlimited-size</b> cache.
     * @param pageSize The item count of per-page (page index > 0).
     * @param firstPageSize The item count of the first page (page index == 0).
     * @see #RecyclerPageAdapter(Cache, int, int)
     */
    public RecyclerPageAdapter(int maxPages, int pageSize, int firstPageSize) {
        this(Pages.<E>createPageCache(maxPages), pageSize, firstPageSize);
    }

    /**
     * Constructor
     * @param pageCache The {@link Page} {@link Cache} to store the pages.
     * @param pageSize The item count of per-page (page index > 0).
     * @param firstPageSize The item count of the first page (page index == 0).
     * @see #RecyclerPageAdapter(int, int, int)
     */
    public RecyclerPageAdapter(Cache<Integer, ? extends Page<E>> pageCache, int pageSize, int firstPageSize) {
        mImpl = new PageAdapterImpl<E>(this, pageCache, pageSize, firstPageSize);
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
     * <p>This method will be call {@link #loadPage(int, int, int, int)} to obtain the item when
     * the item was not present.</p>
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(ViewHolder)
     */
    public E getItem(int position) {
        return mImpl.getItem(position);
    }

    /**
     * Equivalent to calling <tt>getItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(int)
     */
    public final E getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != RecyclerView.NO_POSITION ? getItem(position) : null);
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>Unlike {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int, int)}
     * when the item was not present.</p>
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(ViewHolder)
     */
    public E peekItem(int position) {
        return mImpl.peekItem(position);
    }

    /**
     * Equivalent to calling <tt>peekItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(int)
     */
    public final E peekItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != RecyclerView.NO_POSITION ? peekItem(position) : null);
    }

    /**
     * Returns the {@link Page} associated with the specified index <em>page</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int, int)} to obtain the page when
     * the page was not present.</p>
     * @param page The index of the page.
     * @return The <tt>Page</tt> at the specified index, or <tt>null</tt> if there was not present.
     * @see #peekPage(int)
     */
    public final Page<E> getPage(int page) {
        return mImpl.getPage(page, mImpl.getAdapterPosition(page, 0));
    }

    /**
     * Returns the {@link Page} associated with the specified index <em>page</em> in this adapter.
     * <p>Unlike {@link #getPage}, this method do not call {@link #loadPage(int, int, int, int)}
     * when the page was not present.</p>
     * @param page The index of the page.
     * @return The <tt>Page</tt> at the specified index, or <tt>null</tt> if there was not present.
     * @see #getPage(int)
     */
    public final Page<E> peekPage(int page) {
        DebugUtils.__checkUIThread("peekPage");
        return mImpl.mPageCache.get(page);
    }

    /**
     * Sets the {@link Page} at the specified index <em>page</em> in this adapter.
     * <p>This is useful when asynchronously loading to prevent blocking the UI.</p>
     * @param page The index of the page.
     * @param data The <tt>Page</tt> or <tt>null</tt> if load failed.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged(int, int, Object)}.
     * @see #setPage(int, List)
     * @see #setPage(int, List, Object)
     * @see #setPage(int, JSONArray)
     * @see #setPage(int, JSONArray, Object)
     */
    public void setPage(int page, Page<E> data, Object payload) {
        final int count = mImpl.setPage(page, data);
        if (count > 0) {
            UIHandler.notifyItemRangeChanged(mRecyclerView, mImpl.getAdapterPosition(page, 0), count, payload);
        }
    }

    /**
     * Equivalent to calling <tt>setPage(page, new ListPage(data), null)</tt>.
     * @param page The index of the page.
     * @param data The {@link List} of the page data or <tt>null</tt> if load failed.
     * @see #setPage(int, Page, Object)
     * @see #setPage(int, List, Object)
     */
    public final void setPage(int page, List<E> data) {
        setPage(page, (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(data) : null), null);
    }

    /**
     * Equivalent to calling <tt>setPage(page, new ListPage(data), payload)</tt>.
     * @param page The index of the page.
     * @param data The {@link List} of the page data or <tt>null</tt> if load failed.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged(int, int, Object)}.
     * @see #setPage(int, List)
     * @see #setPage(int, Page, Object)
     */
    public final void setPage(int page, List<E> data, Object payload) {
        setPage(page, (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(data) : null), payload);
    }

    /**
     * Equivalent to calling <tt>setPage(page, new JSONPage(data), null)</tt>.
     * @param page The index of the page.
     * @param data The {@link JSONArray} of the page data or <tt>null</tt> if load failed.
     * @see #setPage(int, Page, Object)
     * @see #setPage(int, JSONArray, Object)
     */
    public final void setPage(int page, JSONArray data) {
        setPage(page, (JSONUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null), null);
    }

    /**
     * Equivalent to calling <tt>setPage(page, new JSONPage(data), payload)</tt>.
     * @param page The index of the page.
     * @param data The {@link JSONArray} of the page data or <tt>null</tt> if load failed.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged(int, int, Object)}.
     * @see #setPage(int, JSONArray)
     * @see #setPage(int, Page, Object)
     */
    public final void setPage(int page, JSONArray data, Object payload) {
        setPage(page, (JSONUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null), payload);
    }

    /**
     * Returns the {@link Page} {@link Cache} associated with this adapter.
     * @return The page cache.
     */
    @SuppressWarnings("unchecked")
    public final <T extends Cache<Integer, Page<E>>> T getPageCache() {
        return (T)mImpl.mPageCache;
    }

    /**
     * Returns the index of the page with the given the adapter position.
     * @param position The adapter position of the item.
     * @return The index of the page.
     * @see #getPagePosition(int)
     * @see #getAdapterPosition(int, int)
     */
    public final int getPageIndex(int position) {
        return mImpl.getPageIndex(position);
    }

    /**
     * Returns the position of the item in the page with the given the adapter position.
     * @param position The adapter position of the item.
     * @return The position of the item in the page.
     * @see #getPageIndex(int)
     * @see #getAdapterPosition(int, int)
     */
    public final int getPagePosition(int position) {
        return mImpl.getPagePosition(position);
    }

    /**
     * Returns the adapter position with the given <em>page</em> and <em>pagePosition</em>.
     * @param page The index of the page.
     * @param pagePosition The position of the item in the <em>page</em>.
     * @return The adapter position of the item in this adapter.
     * @see #getPageIndex(int)
     * @see #getPagePosition(int)
     */
    public final int getAdapterPosition(int page, int pagePosition) {
        return mImpl.getAdapterPosition(page, pagePosition);
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
     * @param position The adapter position of the item in this adapter.
     * @param page The index of the page whose data should be returned.
     * @param offset The start index of the first item.
     * @param itemCount The number of items in the <em>page</em>.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     * @see #setPage(int, Page, Object)
     */
    public abstract Page<E> loadPage(int position, int page, int offset, int itemCount);
}
