package android.ext.widget;

import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.ext.widget.Filters.DataSetObserver;
import android.ext.widget.Filters.ListFilter;
import android.ext.widget.Filters.ListFilterClient;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * Abstract class BaseListAdapter
 * @author Garfield
 */
public abstract class BaseListAdapter<T> extends BaseAdapter implements Filterable, ListFilterClient<T>, CursorObserverClient, DataSetObserver {
    /* package */ List<T> mData;
    private Filter mFilter;
    private CursorObserver mObserver;

    /**
     * Constructor
     * @param data The data to represent in the <tt>ListView</tt> or <tt>null</tt>.
     */
    public BaseListAdapter(List<T> data) {
        mData = (data != null ? data : Collections.<T>emptyList());
    }

    /**
     * Returns the underlying data of this adapter.
     * @return The underlying data of this adapter.
     * @see #changeData(List)
     */
    public final List<T> getData() {
        return mData;
    }

    /**
     * @see #getData()
     */
    @Override
    public void changeData(List<T> newData) {
        changeData(newData, this);
    }

    /**
     * Register an observer that gets callbacks when data identified by a given
     * content URI changes.
     * @param context The <tt>Context</tt>.
     * @param uri The URI to watch for changes. This can be a specific row URI,
     * or a base URI for a whole class of content.
     * @param notifyForDescendents If <tt>true</tt> changes to URIs beginning with
     * uri will also cause notifications to be sent. If <tt>false</tt> only changes
     * to the exact URI specified by uri will cause notifications to be sent.
     * @see #unregisterContentObserver(Context)
     */
    public final void registerContentObserver(Context context, Uri uri, boolean notifyForDescendents) {
        registerContentObserver(context, uri, notifyForDescendents, this);
    }

    /**
     * Unregister an observer that has previously been registered with this adapter.
     * @param context The <tt>Context</tt>.
     * @see #registerContentObserver(Context, Uri, boolean)
     */
    public final void unregisterContentObserver(Context context) {
        if (mObserver != null) {
            mObserver.unregister(context);
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return -1;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final T itemData = mData.get(position);
        if (view == null) {
            view = newView(itemData, position, parent);
        }

        bindView(itemData, position, view);
        return view;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ListFilter<T>(mData, this);
        }

        return mFilter;
    }

    @Override
    public void onContentChanged(boolean selfChange, Uri uri) {
    }

    @Override
    public CharSequence convertToString(T itemData) {
        return itemData.toString();
    }

    @Override
    public List<T> onPerformFiltering(CharSequence constraint, List<T> originalData) {
        return mData;
    }

    /**
     * Makes a new view to hold the item data.
     * @param itemData The item data to create a new view.
     * @param position The position of the item data.
     * @param parent The parent to which the new view is attached to.
     * @return The newly created view.
     * @see #bindView(T, int, View)
     */
    protected abstract View newView(T itemData, int position, ViewGroup parent);

    /**
     * Binds an existing view to hold the item data.
     * @param itemData The item data to bind view.
     * @param position The position of the item data.
     * @param view Existing view, returned earlier by <tt>newView</tt>.
     * @see #newView(T, int, ViewGroup)
     */
    protected abstract void bindView(T itemData, int position, View view);

    /**
     * Changes the underlying data to a new data.
     * @param newData The new data to be used.
     * @param observer The {@link DataSetObserver}.
     */
    /* package */ final void changeData(List<T> newData, DataSetObserver observer) {
        if (mData != newData) {
            mFilter = null;
            mData = (newData != null ? newData : Collections.<T>emptyList());
            if (mData.size() > 0) {
                observer.notifyDataSetChanged();
            } else {
                observer.notifyDataSetInvalidated();
            }
        }
    }

    /**
     * Register an observer that gets callbacks when data identified by a given
     * content URI changes.
     * @param context The <tt>Context</tt>.
     * @param uri The URI to watch for changes. This can be a specific row URI,
     * or a base URI for a whole class of content.
     * @param notifyForDescendents If <tt>true</tt> changes to URIs beginning with
     * uri will also cause notifications to be sent. If <tt>false</tt> only changes
     * to the exact URI specified by uri will cause notifications to be sent.
     * @param client The {@link CursorObserverClient}.
     */
    /* package */ final void registerContentObserver(Context context, Uri uri, boolean notifyForDescendents, CursorObserverClient client) {
        if (mObserver == null) {
            mObserver = new CursorObserver(client);
        }

        mObserver.register(context, uri, notifyForDescendents);
    }

    /**
     * Class <tt>ListAdapterImpl</tt> is an implementation of a {@link BaseListAdapter}.
     */
    /* package */ static final class ListAdapterImpl<T> extends BaseListAdapter<T> {
        public ListAdapterImpl(List<T> data) {
            super(data);
        }

        @Override
        protected void bindView(T itemData, int position, View view) {
        }

        @Override
        protected View newView(T itemData, int position, ViewGroup parent) {
            return null;
        }
    }
}
