package android.ext.temp;

import java.util.Collections;
import java.util.List;
import android.content.Context;
import android.ext.widget.CursorObserver;
import android.ext.widget.CursorObserver.CursorObserverClient;
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
public abstract class BaseListAdapter<E> extends BaseAdapter implements Filterable, ListFilterClient<E>, CursorObserverClient {
    /* package */ List<E> mData;
    private Filter mFilter;
    private CursorObserver mObserver;

    /**
     * Constructor
     * @param data The data to represent in the <tt>ListView</tt> or <tt>null</tt>.
     */
    public BaseListAdapter(List<E> data) {
        mData = (data != null ? data : Collections.<E>emptyList());
    }

    /**
     * Returns the underlying data of this adapter.
     * @return The underlying data of this adapter.
     * @see #changeData(List)
     */
    public final List<E> getData() {
        return mData;
    }

    /**
     * @see #getData()
     */
    @Override
    public void changeData(List<E> newData) {
        if (mData != newData) {
            mFilter = null;
            mData = (newData != null ? newData : Collections.<E>emptyList());
            if (mData.size() > 0) {
                notifyDataSetChanged();
            } else {
                notifyDataSetInvalidated();
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
    public E getItem(int position) {
        return mData.get(position);
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

        bindView(mData.get(position), position, view);
        return view;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ListFilter<E>(mData, this);
        }

        return mFilter;
    }

    @Override
    public void onContentChanged(boolean selfChange, Uri uri) {
    }

    @Override
    public CharSequence convertToString(E itemData) {
        return itemData.toString();
    }

    @Override
    public List<E> onPerformFiltering(CharSequence constraint, List<E> originalData) {
        return mData;
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
     * @param itemData The item data to bind view.
     * @param position The position of the item.
     * @param view Existing view, returned earlier by {@link #newView}.
     * @see #newView(int, ViewGroup)
     */
    protected abstract void bindView(E itemData, int position, View view);

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
}
