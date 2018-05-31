package android.ext.widget;

import java.util.List;
import org.json.JSONArray;
import android.ext.util.ArrayUtils;
import android.ext.util.Caches.Cache;
import android.ext.util.DebugUtils;
import android.ext.util.JSONUtils;
import android.ext.widget.Pages.JSONPage;
import android.ext.widget.Pages.ListPage;
import android.ext.widget.Pages.Page;
import android.ext.widget.Pages.PageAdapterImpl;
import android.ext.widget.Pages.PageLoader;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Class <tt>PageAdapter</tt> allows to loading data by page.
 * @param E The item data type of the adapter.
 * @author Garfield
 * @version 3.0
 */
public abstract class PageAdapter<E> extends BaseAdapter implements PageLoader<E> {
    private final PageAdapterImpl<E> mImpl;

    /**
     * Constructor
     * @param maxPages The maximum number of pages to allow in the page cache.
     * Pass <tt>0</tt> that the page cache is the <b>unlimited-size</b> cache.
     * @param pageSize The item count of per-page (page index > 0).
     * @param firstPageSize The item count of the first page (page index == 0).
     * @see #PageAdapter(Cache, int, int)
     */
    public PageAdapter(int maxPages, int pageSize, int firstPageSize) {
        this(Pages.<E>createPageCache(maxPages), pageSize, firstPageSize);
    }

    /**
     * Constructor
     * @param pageCache The {@link Page} {@link Cache} to store the pages.
     * @param pageSize The item count of per-page (page index > 0).
     * @param firstPageSize The item count of the first page (page index == 0).
     * @see #PageAdapter(int, int, int)
     */
    public PageAdapter(Cache<Integer, Page<E>> pageCache, int pageSize, int firstPageSize) {
        mImpl = new PageAdapterImpl<E>(this, pageCache, pageSize, firstPageSize);
    }

    /**
     * Sets total number of items in this adapter.
     * @param count The total number of items in this adapter.
     * @see #getCount()
     */
    public void setCount(int count) {
        if (mImpl.mItemCount != count) {
            mImpl.setItemCount(count);
            notifyDataSetChanged();
        }
    }

    /**
     * @see #setCount(int)
     */
    @Override
    public int getCount() {
        return mImpl.mItemCount;
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (view == null) {
            view = newView(position, parent);
        }

        bindView(getItem(position), position, view);
        return view;
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int, int)} to obtain the item when
     * the item was not present.</p>
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(int)
     */
    @Override
    public E getItem(int position) {
        return mImpl.getItem(position);
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>Unlike {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int, int)}
     * when the item was not present.</p>
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(int)
     */
    public E peekItem(int position) {
        return mImpl.peekItem(position);
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
     * @see #setPage(int, List)
     * @see #setPage(int, JSONArray)
     */
    public void setPage(int page, Page<E> data) {
        if (mImpl.setPage(page, data) > 0) {
            notifyDataSetChanged();
        }
    }

    /**
     * Equivalent to calling <tt>setPage(page, new ListPage(data), null)</tt>.
     * @param page The index of the page.
     * @param data The {@link List} of the page data or <tt>null</tt> if load failed.
     * @see #setPage(int, Page)
     * @see #setPage(int, JSONArray)
     */
    public final void setPage(int page, List<E> data) {
        setPage(page, (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(data) : null));
    }

    /**
     * Equivalent to calling <tt>setPage(page, new JSONPage(data), null)</tt>.
     * @param page The index of the page.
     * @param data The {@link JSONArray} of the page data or <tt>null</tt> if load failed.
     * @see #setPage(int, Page)
     * @see #setPage(int, List)
     */
    public final void setPage(int page, JSONArray data) {
        setPage(page, (JSONUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null));
    }

    /**
     * Returns the item count of the specified index <em>page</em>.
     * @param page The index of the page.
     * @return The item count.
     */
    public final int getPageSize(int page) {
        return (page > 0 ? mImpl.mPageSize : mImpl.mFirstPageSize);
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

    /**
     * Returns a new {@link View} to hold the item data.
     * @param position The position of the item.
     * @param parent The parent to which the new view is attached to.
     * @return The newly created view.
     * @see #bindView(E, int, View)
     */
    protected abstract View newView(int position, ViewGroup parent);

    /**
     * Binds an existing {@link View} to hold the item data.
     * @param item The item to bind view or <tt>null</tt>.
     * @param position The position of the item.
     * @param view Existing view, returned earlier by {@link #newView}.
     * @see #newView(int, ViewGroup)
     */
    protected abstract void bindView(E item, int position, View view);

    /**
     * Returns the {@link Page} at the given position <em>page</em>. Subclasses
     * must implement this method to return <tt>Page</tt> for a particular page.
     * <p>If you want to asynchronously load the page data to prevent blocking
     * the UI, it is possible to return <tt>null</tt> and at a later time call
     * {@link #setPage(int, Page)}.<p>
     * @param position The adapter position of the item in this adapter.
     * @param page The index of the page whose data should be returned.
     * @param offset The start index of the first item.
     * @param itemCount The number of items in the <em>page</em>.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     * @see #setPage(int, Page)
     */
    public abstract Page<E> loadPage(int position, int page, int offset, int itemCount);
}
