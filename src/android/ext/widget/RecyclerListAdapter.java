package android.ext.widget;

import java.util.List;
import android.content.Context;
import android.database.DataSetObserver;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.view.ViewGroup;

/**
 * Abstract class RecyclerListAdapter
 * @author Garfield
 */
public abstract class RecyclerListAdapter<T, VH extends ViewHolder> extends Adapter<VH> implements CursorObserverClient {
    private final BaseListAdapter<T> mAdapter;

    /**
     * Constructor
     * @param data The data to represent in the <tt>RecyclerView</tt> or <tt>null</tt>.
     */
    public RecyclerListAdapter(List<T> data) {
        mAdapter = new ListAdapterImpl<T>(data, this);
    }

    /**
     * Returns the underlying data of this adapter.
     * @return The underlying data of this adapter.
     * @see #changeData(List)
     */
    public final List<T> getData() {
        return mAdapter.mData;
    }

    /**
     * Returns the item associated with the specified
     * <em>position</em> in this adapter.
     * @param position The position of the item.
     * @return The item at the specified <em>position</em>.
     * @see #getItem(ViewHolder)
     */
    public T getItem(int position) {
        return mAdapter.mData.get(position);
    }

    /**
     * Equivalent to calling <tt>getItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The item at the specified position or <tt>null</tt>.
     * @see #getItem(int)
     */
    public final T getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != RecyclerView.NO_POSITION ? getItem(position) : null);
    }

    /**
     * Changes the underlying data to a new data.
     * @param newData The new data to be used or
     * <tt>null</tt> to clear the underlying data.
     * @see #getData()
     */
    public void changeData(List<T> newData) {
        mAdapter.changeData(newData);
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
        mAdapter.registerContentObserver(context, uri, notifyForDescendents, this);
    }

    /**
     * Unregister an observer that has previously been registered with this adapter.
     * @param context The <tt>Context</tt>.
     * @see #registerContentObserver(Context, Uri, boolean)
     */
    public final void unregisterContentObserver(Context context) {
        mAdapter.unregisterContentObserver(context);
    }

    @Override
    public int getItemCount() {
        return mAdapter.mData.size();
    }

    @Override
    public void onContentChanged(boolean selfChange, Uri uri) {
    }

    /**
     * Class <tt>ListAdapterImpl</tt> is an implementation of a {@link BaseListAdapter}.
     */
    private static final class ListAdapterImpl<T> extends BaseListAdapter<T> {
        public ListAdapterImpl(List<T> data, Adapter<?> adapter) {
            super(data);
            registerDataSetObserver(new AdapterDataObserver(adapter));
        }

        @Override
        protected View newView(int position, ViewGroup parent) {
            return null;
        }

        @Override
        protected void bindView(T itemData, int position, View view) {
        }
    }

    /**
     * Class <tt>AdapterDataObserver</tt> is an implementation of a {@link DataSetObserver}.
     */
    /* package */ static final class AdapterDataObserver extends DataSetObserver {
        private final Adapter<?> mAdapter;

        /**
         * Constructor
         * @param adapter The <tt>Adapter</tt>.
         */
        public AdapterDataObserver(Adapter<?> adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onChanged() {
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onInvalidated() {
            mAdapter.notifyDataSetChanged();
        }
    }
}
