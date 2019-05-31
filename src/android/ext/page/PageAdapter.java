package android.ext.page;

import android.ext.cache.Cache;
import android.ext.page.Pages.PageAdapterImpl;
import android.ext.page.Pages.PageLoader;
import android.ext.util.DebugUtils;
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
     * Pass <tt>0</tt> indicates the page cache is the <b>unlimited-size</b> cache.
     * @param initialSize The item count of the first page (page index == 0).
     * @param pageSize The item count of the each page (page index > 0).
     * @param prefetchDistance Defines how far to the first or last item in the
     * page to this adapter should prefetch the data. Pass <tt>0</tt> indicates
     * this adapter will not prefetch data.
     * @see #PageAdapter(Cache, int, int, int)
     * @see Pages#newPageCache(int)
     */
    public PageAdapter(int maxPages, int initialSize, int pageSize, int prefetchDistance) {
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
     * @see #PageAdapter(int, int, int, int)
     */
    public PageAdapter(Cache<Integer, ? extends Page<? extends E>> pageCache, int initialSize, int pageSize, int prefetchDistance) {
        mImpl = new PageAdapterImpl<E>(pageCache, initialSize, pageSize, prefetchDistance, this);
    }

    /**
     * Sets total number of items in this adapter.
     * @param count The total number of items in this adapter.
     * @see #getCount()
     */
    public void setCount(int count) {
        mImpl.setItemCount(count);
        if (count > 0) {
            notifyDataSetChanged();
        } else {
            notifyDataSetInvalidated();
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
     * <p>This method will be call {@link #loadPage(int, int, int)} to obtain the item when the
     * item was not present.</p>
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
     * <p>Unlike {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int)}
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
     * Sets the {@link Page} at the specified index <em>page</em> in this adapter.
     * <p>This is useful when asynchronously loading to prevent blocking the UI.</p>
     * @param page The index of the page.
     * @param data May be <tt>null</tt>. The <tt>Page</tt> to set.
     * @see Pages#newPage(java.util.List)
     */
    public void setPage(int page, Page<E> data) {
        if (mImpl.setPage(page, data) > 0) {
            notifyDataSetChanged();
        }
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
     * @param itemData The item data to bind view or <tt>null</tt>.
     * @param position The adapter position of the item.
     * @param view Existing view, returned earlier by {@link #newView}.
     * @see #newView(int, ViewGroup)
     */
    protected abstract void bindView(E itemData, int position, View view);

    /**
     * Returns the {@link Page} at the given position <em>page</em>. Subclasses
     * must implement this method to return <tt>Page</tt> for a particular page.
     * <p>If you want to asynchronously load the page data to prevent blocking
     * the UI, it is possible to return <tt>null</tt> and at a later time call
     * {@link #setPage(int, Page)}.<p>
     * @param page The index of the page whose data should be returned.
     * @param startPosition The position of the first item to load.
     * @param count The number of items to load.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     */
    public abstract Page<E> loadPage(int page, int startPosition, int count);
}
