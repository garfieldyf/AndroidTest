package android.ext.widget;

import static android.support.v7.widget.RecyclerView.NO_POSITION;
import android.ext.cache.ArrayMapCache;
import android.ext.cache.Cache;
import android.ext.cache.SimpleLruCache;
import android.ext.content.AbsAsyncTask;
import android.ext.util.ArrayUtils;
import android.ext.util.DebugUtils;
import android.ext.util.DeviceUtils;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.Printer;
import android.view.View;
import java.util.BitSet;
import java.util.Formatter;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executor;

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
    private int mItemCount;
    private int mMaxPageIndex;
    private int mLastPosition;
    private List<E> mInitialPage;

    private final Config mConfig;
    private final BitSet mLoadStates;
    private final Cache<Integer, List<E>> mPageCache;

    /**
     * Constructor
     * @param config The {@link Config}, which defines how the adapter will load data.
     */
    public PageAdapter(Config config) {
        DebugUtils.__checkError(config == null, "Invalid parameters - config == null");
        mConfig = config;
        mLoadStates = new BitSet();
        mPageCache  = config.createPageCache();
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
        DebugUtils.__checkError(itemCount < 0, "Invalid parameter - itemCount(" + itemCount + ") must be >= 0");
        mItemCount = itemCount;
        mPageCache.clear();
        mLoadStates.clear();
        mLastPosition = 0;
        mInitialPage  = null;
        mMaxPageIndex = (itemCount > mConfig.initialSize ? (int)Math.ceil((double)(itemCount - mConfig.initialSize) / mConfig.pageSize) : 0);
        postNotifyDataSetChanged();
    }

    /**
     * Equivalent to calling <tt>getItem(position, null)</tt>.
     * @param position The adapter position of the item in this adapter.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(View)
     * @see #getItem(int, E)
     * @see #getItem(ViewHolder)
     */
    public final E getItem(int position) {
        return getItem(position, null);
    }

    /**
     * Returns the item associated with the specified position <em>position</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to retrieve the item when the
     * item was not present.</p>
     * @param position The adapter position of the item in this adapter.
     * @return The item at the specified position, or <em>fallback</em> if there was not present.
     * @see #getItem(int)
     * @see #getItem(View)
     * @see #getItem(ViewHolder)
     */
    public E getItem(int position, E fallback) {
        DebugUtils.__checkUIThread("getItem");
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        final long combinedPosition = getPageForPosition(position);
        final int pageIndex = getOriginalPage(combinedPosition);
        final int itemIndex = (int)combinedPosition;
        final List<E> page  = getPage(pageIndex);

        if (mLastPosition > position) {
            // Prefetch the pageIndex previous page data.
            if (pageIndex > 0 && itemIndex == mConfig.prefetchDistance) {
                DebugUtils.__checkDebug(true, "PageAdapter", "prefetch data pageIndex = " + (pageIndex - 1) + ", itemIndex = " + itemIndex);
                getPage(pageIndex - 1);
            }
        } else if (pageIndex < mMaxPageIndex) {
            // Prefetch the pageIndex next page data.
            if (itemIndex == mConfig.getPrefetchIndex(pageIndex)) {
                DebugUtils.__checkDebug(true, "PageAdapter", "prefetch data pageIndex = " + (pageIndex + 1) + ", itemIndex = " + itemIndex);
                getPage(pageIndex + 1);
            }
        }

        mLastPosition = position;   // Saves the last position.
        return (page != null ? page.get(itemIndex) : fallback);
    }

    /**
     * Equivalent to calling <tt>getItem(recyclerView.getChildAdapterPosition(child))</tt>.
     * @param child The child of the <tt>RecyclerView</tt> to query for the
     * <tt>ViewHolder</tt>'s adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(int)
     * @see #getItem(int, E)
     * @see #getItem(ViewHolder)
     */
    public final E getItem(View child) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        final int position = mRecyclerView.getChildAdapterPosition(child);
        return (position != NO_POSITION ? getItem(position, null) : null);
    }

    /**
     * Equivalent to calling <tt>getItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position, or <tt>null</tt> if there was not present.
     * @see #getItem(int)
     * @see #getItem(View)
     * @see #getItem(int, E)
     */
    public final E getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != NO_POSITION ? getItem(position, null) : null);
    }

    /**
     * Sets the item at the specified <em>position</em> in this adapter with the specified <em>value</em>.
     * This method will be call {@link #notifyItemChanged(int)} when the <em>value</em> has set. <p>Unlike
     * {@link #getItem}, this method do <b>not</b> call {@link #loadPage(int, int, int)} when the item was
     * not present.</p>
     * @param position The adapter position of the item in this adapter.
     * @param value The value to set.
     * @return The previous item at the specified <em>position</em> or <tt>null</tt> if the item not found.
     */
    public E setItem(int position, E value) {
        DebugUtils.__checkUIThread("setItem");
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);

        E previous = null;
        final long combinedPosition = getPageForPosition(position);
        final List<E> page = getPageImpl(getOriginalPage(combinedPosition));
        if (page != null) {
            previous = page.set((int)combinedPosition, value);
            postNotifyItemRangeChanged(position, 1, null);
        }

        return previous;
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
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        final long combinedPosition = getPageForPosition(position);
        final List<E> page = getPageImpl(getOriginalPage(combinedPosition));
        return (page != null ? page.get((int)combinedPosition) : null);
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
        return (position != NO_POSITION ? peekItem(position) : null);
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
        return (position != NO_POSITION ? peekItem(position) : null);
    }

    /**
     * Returns the {@link Config} associated with this adapter.
     * @return The <tt>Config</tt>.
     */
    public final Config getConfig() {
        return mConfig;
    }

    /**
     * Given a position within this adapter, returns the combined position of the
     * corresponding page within the array of pages.<p>The returned combined position:
     * <li>bit &nbsp;&nbsp;0-31 : Lower 32 bits of the index of the item in the page.
     * <li>bit 32-63 : Higher 32 bits of the index of the page.</p>
     * @param position The adapter position of the item.
     * @return The combined position of the page.
     * @see #getPositionForPage(int)
     * @see #getOriginalPage(long)
     * @see #getOriginalPosition(long)
     */
    public final long getPageForPosition(int position) {
        DebugUtils.__checkError(position < 0 || position >= mItemCount, "Invalid position = " + position + ", itemCount = " + mItemCount);
        final int pageIndex, itemIndex, offset = position - mConfig.initialSize;
        if (offset < 0) {
            pageIndex = 0;
            itemIndex = position;
        } else {
            pageIndex = offset / mConfig.pageSize + 1;
            itemIndex = offset % mConfig.pageSize;
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
        DebugUtils.__checkError(pageIndex < 0, "Invalid parameter - pageIndex(" + pageIndex + ") must be >= 0");
        return (pageIndex > 0 ? (pageIndex - 1) * mConfig.pageSize + mConfig.initialSize : 0);
    }

    public final void dump(Printer printer) {
        DebugUtils.__checkUIThread("dump");
        if (mPageCache instanceof ArrayMapCache) {
            dump(printer, ((ArrayMapCache<Integer, List<E>>)mPageCache).entrySet());
        } else if (mPageCache instanceof SimpleLruCache) {
            dump(printer, ((SimpleLruCache<Integer, List<E>>)mPageCache).snapshot().entrySet());
        }
    }

    /**
     * Returns the index of the page with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by {@link #getPageForPosition}.
     * @return The index of the page.
     * @see #getOriginalPosition(long)
     */
    public static int getOriginalPage(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "Invalid parameter - combinedPosition(" + combinedPosition + ") must be >= 0");
        return (int)(combinedPosition >> 32);
    }

    /**
     * Returns the index of the item in the page with the given the <em>combinedPosition</em>.
     * @param combinedPosition The combined position, returned earlier by {@link #getPageForPosition}.
     * @return The index of the item in the page.
     * @see #getOriginalPage(long)
     */
    public static int getOriginalPosition(long combinedPosition) {
        DebugUtils.__checkError(combinedPosition < 0, "Invalid parameter - combinedPosition(" + combinedPosition + ") must be >= 0");
        return (int)combinedPosition;
    }

    /**
     * Called on a background thread to load a page with given the <em>pageIndex</em>.
     * @param pageIndex The index of the page whose data should be returned.
     * @param startPosition The start position of data to load.
     * @param loadSize The number of items should be load.
     * @return The page, or <tt>null</tt>.
     */
    protected abstract List<E> loadPage(int pageIndex, int startPosition, int loadSize);

    /**
     * Called on the UI thread when the page load complete. This
     * method will be call {@link #notifyItemRangeChanged(int, int)}
     * when the <em>page</em> was added.
     * @param pageIndex The index of the page.
     * @param page May be <tt>null</tt>. The page to add.
     */
    protected void onLoadComplete(int pageIndex, List<E> page) {
        DebugUtils.__checkUIThread("onLoadComplete");
        DebugUtils.__checkError(pageIndex < 0, "Invalid parameter - pageIndex(" + pageIndex + ") must be >= 0");

        // Clears the page loading state.
        mLoadStates.clear(pageIndex);
        final int size = ArrayUtils.getSize(page);
        if (size > 0) {
            if (pageIndex == 0) {
                mInitialPage = page;
            } else {
                mPageCache.put(pageIndex, page);
            }

            postNotifyItemRangeChanged(getPositionForPage(pageIndex), size, null);
        }
    }

    /**
     * Returns the page associated with the specified <em>pageIndex</em>.
     */
    private List<E> getPageImpl(int pageIndex) {
        return (pageIndex == 0 ? mInitialPage : mPageCache.get(pageIndex));
    }

    /**
     * Returns the page associated with the specified <em>pageIndex</em> in this adapter.
     * <p>This method will be call {@link #loadPage(int, int, int)} to retrieve the page
     * when the page was not present.</p>
     * @param pageIndex The index of the page.
     * @return The page at the specified index, or <tt>null</tt> if there was not present.
     */
    private List<E> getPage(int pageIndex) {
        DebugUtils.__checkError(pageIndex < 0, "Invalid parameter - pageIndex(" + pageIndex + ") must be >= 0");
        List<E> page = getPageImpl(pageIndex);
        if (page == null && !mLoadStates.get(pageIndex)) {
            // Computes the startPosition and loadSize to load.
            final int startPosition, loadSize;
            if (pageIndex == 0) {
                loadSize = mConfig.initialSize;
                startPosition = 0;
            } else {
                loadSize = mConfig.pageSize;
                startPosition = (pageIndex - 1) * mConfig.pageSize + mConfig.initialSize;
            }

            // Loads the page data and sets the page loading state.
            mLoadStates.set(pageIndex);
            new LoadTask(this, pageIndex).executeOnExecutor(mConfig.mExecutor, startPosition, loadSize);
        }

        return page;
    }

    /**
     * Dump this page cache.
     */
    private void dump(Printer printer, Set<Entry<Integer, List<E>>> entries) {
        final StringBuilder result = new StringBuilder(128);
        final Formatter formatter  = new Formatter(result);
        DeviceUtils.dumpSummary(printer, result, 100, " Dumping %s [ initialSize = %d, pageSize = %d, itemCount = %d ] ", getClass().getSimpleName(), mConfig.initialSize, mConfig.pageSize, mItemCount);
        result.setLength(0);
        printer.println(DeviceUtils.toString(mPageCache, result.append("  PageCache [ ")).append(", size = ").append(entries.size() + (mInitialPage != null ? 1 : 0)).append(" ]").toString());

        if (mInitialPage != null) {
            result.setLength(0);
            printer.println(DeviceUtils.toString(mInitialPage, result.append("    Page 0  ==> ")).append(" { count = ").append(mInitialPage.size()).append(" }").toString());
        }

        for (Entry<Integer, List<E>> entry : entries) {
            final List<E> page = entry.getValue();
            result.setLength(0);

            formatter.format("    Page %-2d ==> ", entry.getKey());
            printer.println(DeviceUtils.toString(page, result).append(" { count = ").append(page.size()).append(" }").toString());
        }
    }

    /**
     * Class <tt>Config</tt> used to {@link PageAdapter} to loads data.
     */
    public static final class Config {
        /**
         * The number of items for each page (pageIndex > 0)
         * loaded by the <tt>PageAdapter</tt>.
         */
        public final int pageSize;

        /**
         * The number of items for initial page (pageIndex = 0)
         * loaded by the <tt>PageAdapter</tt>.
         */
        public final int initialSize;

        /**
         * Prefetch distance which defines how far to the first
         * or last item in the page should prefetch data.
         */
        public final int prefetchDistance;

        /* package */ final Object mPageCache;
        /* package */ final Executor mExecutor;

        /**
         * Returns the maximum number of pages in the page cache.
         * @return The maximum number of pages, <tt>0</tt> if the page
         * cache is <b>unlimitted-size</b> or <tt>-1</tt> if unknown.
         */
        public final int getMaximumPageCount() {
            return (mPageCache instanceof Integer ? (int)mPageCache : -1);
        }

        /**
         * Constructor
         */
        /* package */ Config(Executor executor, Object pageCache, int initialSize, int pageSize, int prefetchDistance) {
            this.pageSize    = pageSize;
            this.mExecutor   = executor;
            this.mPageCache  = pageCache;
            this.initialSize = initialSize;
            this.prefetchDistance = prefetchDistance;
        }

        /**
         * Returns the prefetch index in the specified page.
         */
        /* package */ final int getPrefetchIndex(int pageIndex) {
            return (pageIndex > 0 ? pageSize : initialSize) - prefetchDistance - 1;
        }

        /**
         * Creates an new page {@link Cache}.
         */
        @SuppressWarnings("unchecked")
        /* package */ final <E> Cache<Integer, List<E>> createPageCache() {
            if (mPageCache instanceof Cache) {
                return (Cache<Integer, List<E>>)mPageCache;
            }

            final int maxPageCount = (mPageCache instanceof Integer ? (int)mPageCache : 0);
            return (maxPageCount != 0 ? new SimpleLruCache<Integer, List<E>>(maxPageCount) : new ArrayMapCache<Integer, List<E>>(8));
        }

        /**
         * Class <tt>Builder</tt> used to create a {@link Config}.
         * <h3>Usage</h3>
         * <p>Here is an example:</p><pre>
         * final Config config = new Builder(executor)
         *     .setInitialSize(16)
         *     .setPageSize(64)
         *     .setPrefetchDistance(5)
         *     .setMaximumPageCount(12)
         *     .build();</pre>
         */
        public static final class Builder {
            private Object mPageCache;
            private int mPageSize;
            private int mInitialSize;
            private int mPrefetchDistance;
            private final Executor mExecutor;

            /**
             * Constructor
             * @param executor The <tt>Executor</tt> to executing load task.
             */
            public Builder(Executor executor) {
                mExecutor = executor;
            }

            /**
             * Sets the number of items for each page (pageIndex > 0) to load.
             * @param pageSize The number of items.
             * @return This builder.
             */
            public final Builder setPageSize(int pageSize) {
                mPageSize = pageSize;
                return this;
            }

            /**
             * Sets the number of items for initial page (pageIndex = 0) to load.
             * @param initialSize The number of items.
             * @return This builder.
             */
            public final Builder setInitialSize(int initialSize) {
                mInitialSize = initialSize;
                return this;
            }

            /**
             * Sets the maximum number of pages to allow in the page cache, must be <tt>> 1</tt>.
             * @param maxPageCount The maximum number of pages.
             * @return This builder.
             */
            public final Builder setMaximumPageCount(int maxPageCount) {
                mPageCache = maxPageCount;
                return this;
            }

            /**
             * Sets prefetch distance which defines how far to the
             * first or last item in the page should prefetch data.
             * @param prefetchDistance The distance should prefetch.
             * @return This builder.
             */
            public final Builder setPrefetchDistance(int prefetchDistance) {
                mPrefetchDistance = prefetchDistance;
                return this;
            }

            /**
             * Sets the page {@link Cache} to store the loaded pages.
             * @param pageCache The page <tt>Cache</tt>.
             * @return This builder.
             */
            public final <E> Builder setPageCache(Cache<Integer, List<E>> pageCache) {
                mPageCache = pageCache;
                return this;
            }

            /**
             * Creates a {@link Config} with the arguments supplied to this builder.
             * @return The <tt>Config</tt>.
             */
            public final Config build() {
                this.__checkParameters();
                return new Config(mExecutor, mPageCache, mInitialSize > 0 ? mInitialSize : mPageSize, mPageSize, mPrefetchDistance);
            }

            private void __checkParameters() {
                if (mPageSize <= 0) {
                    throw new AssertionError("Invalid parameter - pageSize(" + mPageSize + ") must be > 0");
                }

                if (mPageCache instanceof Integer) {
                    final int maxPageCount = (int)mPageCache;
                    if (maxPageCount < 0) {
                        throw new AssertionError("Invalid parameter - maxPageCount(" + maxPageCount + ") must be >= 0");
                    }
                }

                final int pageSize = (mInitialSize > 0 ? Math.min(mPageSize, mInitialSize) : mPageSize);
                if (mPrefetchDistance >= pageSize) {
                    throw new AssertionError("Invalid parameter - prefetchDistance(" + mPrefetchDistance + ") must be < " + pageSize);
                }
            }
        }
    }

    /**
     * Class <tt>LoadTask</tt> is an implementation of a {@link AbsAsyncTask}.
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static final class LoadTask extends AbsAsyncTask<Integer, Object, List> {
        private final int mPageIndex;

        public LoadTask(Object owner, int pageIndex) {
            super(owner);
            mPageIndex = pageIndex;
        }

        @Override
        protected List doInBackground(Integer... params) {
            final PageAdapter adapter = getOwner();
            return (adapter != null ? adapter.loadPage(mPageIndex, params[0], params[1]) : null);
        }

        @Override
        protected void onPostExecute(List result) {
            final PageAdapter adapter = getOwner();
            if (adapter != null) {
                adapter.onLoadComplete(mPageIndex, result);
            }
        }
    }
}
