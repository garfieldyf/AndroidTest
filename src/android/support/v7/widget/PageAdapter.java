package android.support.v7.widget;

import java.util.BitSet;
import java.util.List;
import org.json.JSONArray;
import android.ext.util.ArrayUtils;
import android.ext.util.Caches.Cache;
import android.ext.util.Caches.SimpleLruCache;
import android.ext.util.DebugUtils;
import android.ext.util.JSONUtils;
import android.ext.util.UIHandler;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.ArrayMap;
import android.util.SparseArray;

/**
 * Abstract class PageAdapter
 * @author Garfield
 * @version 2.0
 */
public abstract class PageAdapter<E, VH extends ViewHolder> extends Adapter<VH> {
    private int mItemCount;
    private final int mPageSize;
    private RecyclerView mRecyclerView;

    private final BitSet mPageStates;
    private final Cache<Integer, Page<E>> mPageCache;

    /**
     * Constructor
     * @param maxPages The maximum number of pages to allow in the page cache.
     * Pass <tt>0</tt> that the page cache is the <b>unlimited-size</b> cache.
     * @param pageSize The item count of per-page.
     * @see #PageAdapter(Cache, int)
     */
    public PageAdapter(int maxPages, int pageSize) {
        this(PageAdapter.<E>createPageCache(maxPages), pageSize);
    }

    /**
     * Constructor
     * @param pageCache The {@link Page} {@link Cache} to store the pages.
     * @param pageSize The item count of per-page.
     * @see #PageAdapter(int, int)
     */
    public PageAdapter(Cache<Integer, Page<E>> pageCache, int pageSize) {
        DebugUtils.__checkError(pageSize <= 0, "pageSize <= 0");
        mPageSize   = pageSize;
        mPageCache  = pageCache;
        mPageStates = new BitSet();
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
        if (mItemCount != count) {
            mItemCount = count;
            mPageCache.clear();
            mPageStates.clear();
            UIHandler.notifyDataSetChanged(mRecyclerView);
        }
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to obtain the item when the
     * item was not present.</p>
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(ViewHolder)
     */
    public E getItem(int position) {
        final Page<E> page = getPage(position / mPageSize, position);
        return (page != null ? page.getItem(position % mPageSize) : null);
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
     * <p>Unlike {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int)}
     * when the item was not present.</p>
     * @param position The adapter position of the item.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekItem(ViewHolder)
     */
    public E peekItem(int position) {
        DebugUtils.__checkUIThread("peekItem");
        final Page<E> page = mPageCache.get(position / mPageSize);
        return (page != null ? page.getItem(position % mPageSize) : null);
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
     * Returns the {@link Page} associated with the specified position <em>page</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to obtain the page when the page
     * was not present.</p>
     * @param page The position of the page.
     * @return The <tt>Page</tt> at the specified position, or <tt>null</tt> if there was not present.
     * @see #peekPage(int)
     */
    public final Page<E> getPage(int page) {
        return getPage(page, page * mPageSize);
    }

    /**
     * Returns the {@link Page} associated with the specified position <em>page</em> in this adapter.
     * <p>Unlike {@link #getPage}, this method do not call {@link #loadPage(int, int, int)} when the
     * page was not present.</p>
     * @param page The position of the page.
     * @return The <tt>Page</tt> at the specified position, or <tt>null</tt> if there was not present.
     * @see #getPage(int)
     */
    public final Page<E> peekPage(int page) {
        DebugUtils.__checkUIThread("peekPage");
        return mPageCache.get(page);
    }

    /**
     * Sets the {@link Page} at the specified <em>page</em> in this adapter.
     * <p>This is useful when asynchronously loading to prevent blocking the UI.
     * @param page The position of the page.
     * @param data The <tt>Page</tt> or <tt>null</tt> if load failed.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged(int, int, Object)}.
     * @see #setPage(int, List)
     * @see #setPage(int, List, Object)
     * @see #setPage(int, JSONArray)
     * @see #setPage(int, JSONArray, Object)
     */
    public void setPage(int page, Page<E> data, Object payload) {
        DebugUtils.__checkUIThread("setPage");
        // Clears the page loading state when the page is load complete.
        mPageStates.clear(page);
        final int count = getCount(data);
        if (count > 0) {
            // Adds the page to page cache and notify UI.
            mPageCache.put(page, data);
            UIHandler.notifyItemRangeChanged(mRecyclerView, page * mPageSize, count, payload);
        }
    }

    /**
     * Equivalent to calling <tt>setPage(page, new ListPage(data), null)</tt>.
     * @param page The position of the page.
     * @param data The {@link List} of the page data or <tt>null</tt> if load failed.
     * @see #setPage(int, Page, Object)
     * @see #setPage(int, List, Object)
     */
    public final void setPage(int page, List<E> data) {
        setPage(page, (ArrayUtils.getSize(data) > 0 ? new ListPage<E>(data) : null), null);
    }

    /**
     * Equivalent to calling <tt>setPage(page, new ListPage(data), payload)</tt>.
     * @param page The position of the page.
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
     * @param page The position of the page.
     * @param data The {@link JSONArray} of the page data or <tt>null</tt> if load failed.
     * @see #setPage(int, Page, Object)
     * @see #setPage(int, JSONArray, Object)
     */
    public final void setPage(int page, JSONArray data) {
        setPage(page, (JSONUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null), null);
    }

    /**
     * Equivalent to calling <tt>setPage(page, new JSONPage(data), payload)</tt>.
     * @param page The position of the page.
     * @param data The {@link JSONArray} of the page data or <tt>null</tt> if load failed.
     * @param payload Optional parameter, pass to {@link #notifyItemRangeChanged(int, int, Object)}.
     * @see #setPage(int, JSONArray)
     * @see #setPage(int, Page, Object)
     */
    public final void setPage(int page, JSONArray data, Object payload) {
        setPage(page, (JSONUtils.getSize(data) > 0 ? new JSONPage<E>(data) : null), payload);
    }

    /**
     * Returns the item count of per-page.
     * @return The item count of per-page.
     */
    public final int getPageSize() {
        return mPageSize;
    }

    /**
     * Returns the {@link Page} {@link Cache} associated with this adapter.
     * @return The page cache.
     */
    @SuppressWarnings("unchecked")
    public final <T extends Cache<Integer, Page<E>>> T getPageCache() {
        return (T)mPageCache;
    }

    /**
     * Returns the position of the page with the given the adapter position.
     * @param adapterPosition The adapter position of the item.
     * @return The position of the page.
     * @see #getPagePosition(int)
     * @see #getAdapterPosition(int, int)
     */
    public final int getPosition(int adapterPosition) {
        return (adapterPosition / mPageSize);
    }

    /**
     * Returns the position of the item in the page with the given the adapter position.
     * @param adapterPosition The adapter position of the item.
     * @return The position of the item in the page.
     * @see #getPosition(int)
     * @see #getAdapterPosition(int, int)
     */
    public final int getPagePosition(int adapterPosition) {
        return (adapterPosition % mPageSize);
    }

    /**
     * Returns the adapter position with the given <em>page</em> and <em>pagePosition</em>.
     * @param page The position of the page.
     * @param pagePosition The position of the item in the page.
     * @return The adapter position of the item in this adapter.
     * @see #getPosition(int)
     * @see #getPagePosition(int)
     */
    public final int getAdapterPosition(int page, int pagePosition) {
        return (page * mPageSize + pagePosition);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mRecyclerView = recyclerView;
    }

    /**
     * Returns the total number of items in the <em>page</em>,
     * handling <tt>null Page</tt>.
     * @param page The {@link Page}.
     * @return The total number of items.
     */
    public static int getCount(Page<?> page) {
        return (page != null ? page.getCount() : 0);
    }

    /**
     * Returns the {@link Page} associated with the specified position in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to obtain the page
     * when the page was not present.</p>
     * @param page The position of the page whose data should be returned.
     * @param adapterPosition The adapter position of the item in this adapter.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     */
    protected Page<E> getPage(int page, int adapterPosition) {
        DebugUtils.__checkUIThread("getPage");
        Page<E> result = mPageCache.get(page);
        if (result == null && !mPageStates.get(page)) {
            // Marks the page loading state, if the page is not load.
            mPageStates.set(page);
            result = loadPage(page, mPageSize, adapterPosition);
            if (getCount(result) > 0) {
                // If the page is load successful.
                // 1. Adds the page to page cache.
                // 2. Clears the page loading state.
                mPageStates.clear(page);
                mPageCache.put(page, result);
            }
        }

        return result;
    }

    /**
     * Returns the {@link Page} at the given position <em>page</em>. Subclasses
     * must implement this method to return <tt>Page</tt> for a particular page.
     * <p>If you want to asynchronously load the page data to prevent blocking
     * the UI, it is possible to return <tt>null</tt> and at a later time call
     * {@link #setPage(int, Page, Object)}.<p>
     * @param page The position of the page whose data should be returned.
     * @param pageSize The number of items in the <em>page</em>.
     * @param adapterPosition The adapter position of the item in this adapter.
     * @return The <tt>Page</tt>, or <tt>null</tt>.
     * @see #setPage(int, Page, Object)
     */
    protected abstract Page<E> loadPage(int page, int pageSize, int adapterPosition);

    /**
     * Returns a new {@link Page} {@link Cache} instance.
     * @param maxPages The maximum number of pages to allow in the page cache. Pass
     * <tt>0</tt> that the returned page cache is the <b>unlimited-size</b> cache.
     * @return A new page cache instance.
     */
    private static <E> Cache<Integer, Page<E>> createPageCache(int maxPages) {
        return (maxPages > 0 ? new SimpleLruCache<Integer, Page<E>>(maxPages) : new SparsePageCache<E>());
    }

    /**
     * A <tt>Page</tt> is a collection used to adds the page data to the adapter.
     */
    public static interface Page<E> {
        /**
         * Returns the total number of items in this page.
         * @return The total number of items in this page.
         * @see #getItem(int)
         */
        int getCount();

        /**
         * Returns the item at the specified <em>position</em> in this page.
         * @param position The position of the item.
         * @return The item at the specified <em>position</em>, or <tt>null</tt>
         * if this page has no item at <em>position</em>.
         * @see #getCount()
         */
        E getItem(int position);
    }

    /**
     * Class <tt>ListPage</tt> is an implementation of a {@link Page}.
     */
    public static class ListPage<E> implements Page<E> {
        protected final List<E> mData;

        /**
         * Constructor
         * @param data A {@link List} of this page data.
         */
        public ListPage(List<E> data) {
            DebugUtils.__checkError(data == null || data.size() <= 0, "data == null || data.size() <= 0");
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.size();
        }

        @Override
        public E getItem(int position) {
            return (position >= 0 && position < mData.size() ? mData.get(position) : null);
        }

        @Override
        public String toString() {
            return toString(this, mData);
        }

        /* package */ static String toString(Page<?> page, Object data) {
            final int count = page.getCount();
            final StringBuilder result = DebugUtils.toSimpleString(data, new StringBuilder(64)).append(" { count = ").append(count);
            if (count > 0) {
                result.append(", itemType = ").append(page.getItem(0).getClass().getName());
            }

            return result.append(" }").toString();
        }
    }

    /**
     * Class <tt>JSONPage</tt> is an implementation of a {@link Page}.
     */
    public static class JSONPage<E> implements Page<E> {
        protected final JSONArray mData;

        /**
         * Constructor
         * @param data A {@link JSONArray} of this page data.
         */
        public JSONPage(JSONArray data) {
            DebugUtils.__checkError(data == null || data.length() <= 0, "data == null || data.length() <= 0");
            mData = data;
        }

        @Override
        public int getCount() {
            return mData.length();
        }

        @Override
        @SuppressWarnings("unchecked")
        public E getItem(int position) {
            return (E)mData.opt(position);
        }

        @Override
        public String toString() {
            return ListPage.toString(this, mData);
        }
    }

    /**
     * Class <tt>ArrayPageCache</tt> is an implementation of a {@link Cache}.
     */
    public static class ArrayPageCache<E> implements Cache<Integer, Page<E>> {
        protected final ArrayMap<Integer, Page<E>> mPages;

        /**
         * Constructor
         */
        public ArrayPageCache() {
            mPages = new ArrayMap<Integer, Page<E>>(8);
        }

        @Override
        public void clear() {
            mPages.clear();
        }

        @Override
        public Page<E> remove(Integer key) {
            return mPages.remove(key);
        }

        @Override
        public Page<E> get(Integer key) {
            return mPages.get(key);
        }

        @Override
        public Page<E> put(Integer key, Page<E> page) {
            return mPages.put(key, page);
        }
    }

    /**
     * Class <tt>SparsePageCache</tt> is an implementation of a {@link Cache}.
     */
    public static class SparsePageCache<E> implements Cache<Integer, Page<E>> {
        protected final SparseArray<Page<E>> mPages;

        /**
         * Constructor
         */
        public SparsePageCache() {
            mPages = new SparseArray<Page<E>>(8);
        }

        @Override
        public void clear() {
            mPages.clear();
        }

        @Override
        public Page<E> remove(Integer key) {
            mPages.delete(key);
            return null;
        }

        @Override
        public Page<E> get(Integer key) {
            return mPages.get(key, null);
        }

        @Override
        public Page<E> put(Integer key, Page<E> page) {
            mPages.append(key, page);
            return null;
        }
    }
}
