package android.ext.temp;

import android.database.Cursor;
import android.ext.temp.Filters.CursorFilter;
import android.ext.temp.Filters.CursorFilterClient;
import android.ext.util.FileUtils;
import android.ext.widget.CursorObserver;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.net.Uri;
import android.provider.BaseColumns;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * Abstract class CursorAdapter
 * @author Garfield
 */
public abstract class CursorAdapter extends BaseAdapter implements Filterable, CursorObserverClient, CursorFilterClient {
    /**
     * If set the adapter will register a content observer on the
     * cursor and will call {@link #onContentChanged(boolean, Uri)}
     * when a notification comes in.
     */
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x01;

    /* package */ Cursor mCursor;
    /* package */ Filter mFilter;

    private int mRowIDColumn = -1;
    private final CursorObserver mObserver;

    /**
     * Constructor
     * @param cursor The cursor from which to get the data. May be <tt>null</tt>.
     * @param flags Flags used to determine the behavior of this adapter. May be
     * <tt>0</tt> or any combination of FLAG_XXX constants.
     */
    public CursorAdapter(Cursor cursor, int flags) {
        mObserver = ((flags & FLAG_REGISTER_CONTENT_OBSERVER) != 0 ? new CursorObserver(this) : null);
        swapCursor(cursor);
    }

    /**
     * Constructor
     * @param cursor The cursor from which to get the data. May be <tt>null</tt>.
     * @param observer The {@link CursorObserver}.
     */
    /* package */ CursorAdapter(Cursor cursor, CursorObserver observer) {
        mObserver = observer;
        swapCursor(cursor);
    }

    /**
     * Returns the cursor associated with this adapter.
     * @return The <tt>Cursor</tt>, or <tt>null</tt> if
     * there was not present.
     * @see #changeCursor(Cursor)
     * @see #swapCursor(Cursor)
     */
    public final Cursor getCursor() {
        return mCursor;
    }

    /**
     * @see #swapCursor(Cursor)
     * @see #getCursor()
     */
    @Override
    public void changeCursor(Cursor cursor) {
        FileUtils.close(swapCursor(cursor));
    }

    /**
     * Swap in a new cursor, returning the old cursor. Unlike {@link #changeCursor(Cursor)},
     * the returned old cursor is <b>not</b> closed.
     * @param newCursor The new cursor to be used.
     * @return Returns the previously set cursor, or <tt>null</tt> if there was a not
     * one. If the given new cursor is the same instance is the previously set cursor,
     * <tt>null</tt> is also returned.
     * @see #changeCursor(Cursor)
     * @see #getCursor()
     */
    public Cursor swapCursor(Cursor newCursor) {
        if (mCursor == newCursor) {
            return null;
        }

        // Unregister the ContentObserver from old cursor.
        final Cursor oldCursor = mCursor;
        unregisterContentObserver();
        mCursor = newCursor;

        if (mCursor != null) {
            // Register the ContentObserver to new cursor.
            if (mObserver != null) {
                mObserver.register(mCursor);
            }

            // Notifies the attached observers that the underlying data has been
            // changed and any View reflecting the data set should refresh itself.
            mRowIDColumn = mCursor.getColumnIndex(BaseColumns._ID);
            notifyDataSetChanged();
        } else {
            // Notifies the attached observers that the underlying data is no longer valid.
            mRowIDColumn = -1;
            notifyDataSetInvalidated();
        }

        return oldCursor;
    }

    /**
     * Register an observer that is called when changes happen to the content backing this
     * adapter. <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER} then invoking this method has no effect.</p>
     * @see #unregisterContentObserver()
     */
    public final void registerContentObserver() {
        if (mCursor != null && mObserver != null) {
            mObserver.register(mCursor);
        }
    }

    /**
     * Unregister an observer that has previously been registered with this adapter.
     * <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER} then invoking this method has no effect.</p>
     * @see #registerContentObserver()
     */
    public final void unregisterContentObserver() {
        if (mCursor != null && mObserver != null) {
            mObserver.unregister(mCursor);
        }
    }

    @Override
    public int getCount() {
        return (mCursor != null ? mCursor.getCount() : 0);
    }

    @Override
    public Cursor getItem(int position) {
        return (mCursor != null && mCursor.moveToPosition(position) ? mCursor : null);
    }

    @Override
    public long getItemId(int position) {
        return (mCursor != null && mRowIDColumn != -1 && mCursor.moveToPosition(position) ? mCursor.getLong(mRowIDColumn) : -1);
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("Couldn't move cursor to position : " + position);
        }

        if (view == null) {
            view = newView(position, parent);
        }

        bindView(mCursor, position, view);
        return view;
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new CursorFilter(this);
        }

        return mFilter;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public CharSequence convertToString(Cursor cursor) {
        return null;
    }

    @Override
    public Cursor onPerformFiltering(CharSequence constraint) {
        return mCursor;
    }

    /**
     * Returns a new {@link View} to hold the data pointed to by cursor.
     * @param position The position of the item within the adapter's data
     * set of the item whose view we want.
     * @param parent The parent to which the new view is attached to.
     * @return The newly created view.
     * @see #bindView(Cursor, int, View)
     */
    protected abstract View newView(int position, ViewGroup parent);

    /**
     * Binds an existing {@link View} to the data pointed to by cursor.
     * @param cursor The cursor from which to get the data. The cursor
     * is already moved to the correct position.
     * @param position The position of the item within the adapter's
     * data set of the item whose view we want.
     * @param view Existing view, returned earlier by {@link #newView}.
     * @see #newView(int, ViewGroup)
     */
    protected abstract void bindView(Cursor cursor, int position, View view);
}
