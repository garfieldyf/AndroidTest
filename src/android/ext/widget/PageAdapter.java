package android.ext.widget;

import java.util.Arrays;
import java.util.List;
import org.json.JSONArray;
import android.ext.cache.Cache;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.JSONUtils;
import android.ext.widget.Pages.JSONPage;
import android.ext.widget.Pages.ListPage;
import android.ext.widget.Pages.Page;
import android.ext.widget.Pages.PageAdapterImpl;
import android.ext.widget.Pages.PageLoader;
import android.util.Printer;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * Class <tt>PageAdapter</tt> allows to loading data by page.
 * @param E The item data type of the adapter.
 * @author Garfield
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
     * @see Pages#createPageCache(int)
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
    public PageAdapter(Cache<Integer, ? extends Page<? extends E>> pageCache, int pageSize, int firstPageSize) {
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
            if (count > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
            }
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
        return mImpl.getPage(page, mImpl.getPositionForPage(page, 0));
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
     * @param data May be <tt>null</tt>. The <tt>Page</tt> object.
     * @see #setPage(int, E[])
     * @see #setPage(int, List)
     * @see #setPage(int, JSONArray)
     */
    public void setPage(int page, Page<E> data) {
        if (mImpl.setPage(page, data) > 0) {
            notifyDataSetChanged();
        }
    }

    /**
     * Equivalent to calling <tt>setPage(page, new ListPage(data))</tt>.
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The {@link List} of the page data.
     * @see #setPage(int, E[])
     * @see #setPage(int, Page)
     * @see #setPage(int, JSONArray)
     */
    public final void setPage(int page, List<E> data) {
        setPage(page, (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(data) : null));
    }

    /**
     * Equivalent to calling <tt>setPage(page, new JSONPage(data))</tt>.
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The {@link JSONArray} of the page data.
     * @see #setPage(int, E[])
     * @see #setPage(int, Page)
     * @see #setPage(int, List)
     */
    public final void setPage(int page, JSONArray data) {
        setPage(page, (JSONUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null));
    }

    /**
     * Equivalent to calling <tt>setPage(page, new ListPage(Arrays.asList(data)))</tt>.
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. An array of the page data.
     * @see #setPage(int, List)
     * @see #setPage(int, Page)
     * @see #setPage(int, JSONArray)
     */
    @SuppressWarnings("unchecked")
    public final void setPage(int page, E... data) {
        setPage(page, (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(Arrays.asList(data)) : null));
    }

    /**
     * Returns the item count of the specified index <em>page</em>.
     * @param page The index of the page.
     * @return The item count.
     */
    public final int getPageSize(int page) {
        DebugUtils.__checkError(page < 0, "page < 0");
        return (page > 0 ? mImpl.mPageSize : mImpl.mFirstPageSize);
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

    /**
     * Returns a new {@link View} to hold the item data.
     * @param position The adapter position of the item.
     * @param parent The parent to which the new view is attached to.
     * @return The newly created view.
     * @see #bindView(E, int, View)
     */
    protected abstract View newView(int position, ViewGroup parent);

    /**
     * Binds an existing {@link View} to hold the item data.
     * @param item The item to bind view or <tt>null</tt>.
     * @param position The adapter position of the item.
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
     * @param pageOffset The start position of the first item in the <em>page</em>.
     * @param pageSize The number of items in the <em>page</em>.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     * @see #setPage(int, Page)
     */
    public abstract Page<E> loadPage(int position, int page, int pageOffset, int pageSize);
}
