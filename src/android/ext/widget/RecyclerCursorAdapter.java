package android.ext.widget;

import android.database.Cursor;
import android.ext.util.FileUtils;
import android.ext.widget.CursorAdapter.CursorAdapterImpl;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.ext.widget.Filters.CursorFilterClient;
import android.ext.widget.Filters.DataSetObserver;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * Abstract class RecyclerCursorAdapter
 * @author Garfield
 * @version 1.0
 */
public abstract class RecyclerCursorAdapter<VH extends ViewHolder> extends Adapter<VH> implements Filterable, CursorObserverClient, CursorFilterClient, DataSetObserver {
    /**
     * If set the adapter will register a content observer on the
     * cursor and will call {@link #onContentChanged(boolean, Uri)}
     * when a notification comes in.
     */
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x01;

    /**
     * The internal cursor adapter.
     */
    private final CursorAdapter mAdapter;

    /**
     * Constructor
     * @param cursor The cursor from which to get the data. May be <tt>null</tt>.
     * @param flags Flags used to determine the behavior of this adapter. May be
     * <tt>0</tt> or any combination of FLAG_XXX constants.
     */
    public RecyclerCursorAdapter(Cursor cursor, int flags) {
        mAdapter = new CursorAdapterImpl(cursor, (flags & FLAG_REGISTER_CONTENT_OBSERVER) != 0 ? new CursorObserver(this) : null);
    }

    /**
     * Returns the cursor associated with this adapter.
     * @return The <tt>Cursor</tt>, or <tt>null</tt> if
     * there was not present.
     * @see #changeCursor(Cursor)
     * @see #swapCursor(Cursor)
     */
    public final Cursor getCursor() {
        return mAdapter.mCursor;
    }

    /**
     * Equivalent to calling <tt>getItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The <tt>Cursor</tt>, or <tt>null</tt> if there was not present
     * or can not move to the specified <em>position</em>.
     * @see #getItem(int)
     */
    public final Cursor getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != RecyclerView.NO_POSITION ? getItem(position) : null);
    }

    /**
     * Returns the cursor associated with this adapter. The returned
     * cursor is already moved to the specified <em>position</em>.
     * @param position The position of the cursor.
     * @return The <tt>Cursor</tt>, or <tt>null</tt> if there was not
     * present or can not move to the specified <em>position</em>.
     * @see #getItem(ViewHolder)
     */
    public Cursor getItem(int position) {
        return mAdapter.getItem(position);
    }

    /**
     * @see #swapCursor(Cursor)
     * @see #getCursor()
     */
    @Override
    public void changeCursor(Cursor cursor) {
        FileUtils.close(mAdapter.swapCursor(cursor, this));
    }

    /**
     * Swap in a new cursor, returning the old cursor. Unlike {@link #changeCursor(Cursor)},
     * the returned old cursor is <b>not</b> closed.
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set cursor, or <tt>null</tt> if there was a not one. If
     * the given new cursor is the same instance is the previously set cursor, <tt>null</tt>
     * is also returned.
     * @see #changeCursor(Cursor)
     * @see #getCursor()
     */
    public Cursor swapCursor(Cursor newCursor) {
        return mAdapter.swapCursor(newCursor, this);
    }

    /**
     * Register an observer that is called when changes happen to the content backing this
     * adapter. <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER} then invoking this method has no effect.</p>
     * @see #unregisterContentObserver()
     */
    public final void registerContentObserver() {
        mAdapter.registerContentObserver();
    }

    /**
     * Unregister an observer that has previously been registered with this adapter.
     * <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER} then invoking this method has no effect.</p>
     * @see #registerContentObserver()
     */
    public final void unregisterContentObserver() {
        mAdapter.unregisterContentObserver();
    }

    @Override
    public int getItemCount() {
        return mAdapter.getCount();
    }

    @Override
    public long getItemId(int position) {
        return (hasStableIds() ? mAdapter.getItemId(position) : RecyclerView.NO_ID);
    }

    @Override
    public Filter getFilter() {
        return mAdapter.getFilter(this);
    }

    @Override
    public void notifyDataSetInvalidated() {
        notifyDataSetChanged();
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return null;
    }

    @Override
    public Cursor onPerformFiltering(CharSequence constraint) {
        return mAdapter.mCursor;
    }

    @Override
    public void onContentChanged(boolean selfChange, Uri uri) {
    }
}
