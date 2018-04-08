package android.ext.widget;

import java.util.List;
import android.content.Context;
import android.ext.widget.BaseListAdapter.ListAdapterImpl;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.ext.widget.Filters.DataSetObserver;
import android.ext.widget.Filters.ListFilterClient;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * Abstract class ListRecyclerAdapter
 * @author Garfield
 * @version 1.0
 */
public abstract class ListRecyclerAdapter<T, VH extends ViewHolder> extends RecyclerAdapter<VH> implements Filterable, ListFilterClient<T>, CursorObserverClient, DataSetObserver {
    private final BaseListAdapter<T> mAdapter;

    /**
     * Constructor
     * @param view The {@link RecyclerView}.
     * @param data The data to represent in the <tt>RecyclerView</tt> or <tt>null</tt>.
     * @param flags Flags used to determine the behavior of this adapter.
     * May be <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @see #ListRecyclerAdapter(RecyclerView, AnimatorManager, List, int)
     */
    public ListRecyclerAdapter(RecyclerView view, List<T> data, int flags) {
        super(view, flags);
        mAdapter = new ListAdapterImpl<T>(data);
    }

    /**
     * Constructor
     * @param view The {@link RecyclerView}.
     * @param animatorManager May be <tt>null</tt>. The {@link AnimatorManager}.
     * @param data The data to represent in the <tt>RecyclerView</tt> or <tt>null</tt>.
     * @param flags Flags used to determine the behavior of this adapter. May be
     * <tt>0</tt> or any combination of <tt>FLAG_XXX</tt> constants.
     * @see #ListRecyclerAdapter(RecyclerView, List, int)
     */
    public ListRecyclerAdapter(RecyclerView view, AnimatorManager animatorManager, List<T> data, int flags) {
        super(view, animatorManager, flags);
        mAdapter = new ListAdapterImpl<T>(data);
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
     * Returns the item associated with this adapter.
     * @param child The child of the <tt>RecyclerView</tt> to query for
     * the <tt>ViewHolder</tt>'s adapter position.
     * @return The item at the specified position or <tt>null</tt>.
     * @see #getItem(int)
     * @see #getItem(ViewHolder)
     */
    public final T getItem(View child) {
        final int position = ((RecyclerView)mFocusManager.mRootView).getChildAdapterPosition(child);
        return (position != RecyclerView.NO_POSITION ? getItem(position) : null);
    }

    /**
     * Returns the item associated with the <em>holder's</em>
     * adapter position in this adapter.
     * @param holder The {@link ViewHolder} to obtain position.
     * @return The item at the specified position or <tt>null</tt>.
     * @see #getItem(int)
     * @see #getItem(View)
     */
    public final T getItem(ViewHolder holder) {
        final int position = holder.getAdapterPosition();
        return (position != RecyclerView.NO_POSITION ? getItem(position) : null);
    }

    /**
     * Returns the item associated with the specified
     * <em>position</em> in this adapter.
     * @param position The position of the item.
     * @return The item at the specified <em>position</em>.
     * @see #getItem(View)
     * @see #getItem(ViewHolder)
     */
    public T getItem(int position) {
        return mAdapter.mData.get(position);
    }

    /**
     * @see #getData()
     */
    @Override
    public void changeData(List<T> newData) {
        mAdapter.changeData(newData, this);
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
    public Filter getFilter() {
        return mAdapter.getFilter(this);
    }

    @Override
    public void onContentChanged(boolean selfChange, Uri uri) {
    }

    @Override
    public CharSequence convertToString(T item) {
        return item.toString();
    }

    @Override
    public List<T> onPerformFiltering(CharSequence constraint, List<T> originalData) {
        return mAdapter.mData;
    }
}
