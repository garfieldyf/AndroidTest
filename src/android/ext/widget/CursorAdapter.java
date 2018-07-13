package android.ext.widget;

import android.database.Cursor;
import android.ext.util.FileUtils;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.ext.widget.Filters.CursorFilter;
import android.ext.widget.Filters.CursorFilterClient;
import android.ext.widget.Filters.DataSetObserver;
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
 * @version 3.0
 */
public abstract class CursorAdapter extends BaseAdapter implements Filterable, CursorObserverClient, CursorFilterClient, DataSetObserver {
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
        swapCursor(cursor, this);
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
        FileUtils.close(swapCursor(cursor, this));
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
        return swapCursor(newCursor, this);
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
            view = newView(mCursor, position, parent);
        }

        bindView(mCursor, position, view);
        return view;
    }

    @Override
    public Filter getFilter() {
        return getFilter(this);
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

    @Override
    public void onContentChanged(boolean selfChange, Uri uri) {
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     * @param cursor The cursor from which to get the data. The cursor
     * is already moved to the correct position.
     * @param position The position of the item within the adapter's
     * data set of the item whose view we want.
     * @param parent The parent to which the new view is attached to.
     * @return The newly created view.
     * @see #bindView(Cursor, int, View)
     */
    protected abstract View newView(Cursor cursor, int position, ViewGroup parent);

    /**
     * Binds an existing view to the data pointed to by cursor.
     * @param cursor The cursor from which to get the data. The cursor
     * is already moved to the correct position.
     * @param position The position of the item within the adapter's
     * data set of the item whose view we want.
     * @param view Existing view, returned earlier by {@link #newView}.
     * @see #newView(Cursor, int, ViewGroup)
     */
    protected abstract void bindView(Cursor cursor, int position, View view);

    /**
     * Constructor
     * @param cursor The cursor from which to get the data. May be <tt>null</tt>.
     * @param observer The {@link CursorObserver}.
     */
    /* package */ CursorAdapter(Cursor cursor, CursorObserver observer) {
        mObserver = observer;
        swapCursor(cursor, this);
    }

    /**
     * Returns a {@link Filter} that can be used to constrain data
     * with a filtering pattern.
     * @param client The {@link CursorFilterClient}.
     * @return A <tt>Filter</tt> used to constrain data.
     */
    /* package */ final Filter getFilter(CursorFilterClient client) {
        if (mFilter == null) {
            mFilter = new CursorFilter(client);
        }

        return mFilter;
    }

    /**
     * Swap in a new cursor, returning the old cursor.
     * @param newCursor The new cursor to be used.
     * @param observer The {@link DataSetObserver}.
     * @return Returns the previously set cursor, or <tt>null</tt> if
     * there was a not one. If the given new cursor is the same instance
     * is the previously set cursor, <tt>null</tt> is also returned.
     */
    /* package */ final Cursor swapCursor(Cursor newCursor, DataSetObserver observer) {
        Cursor oldCursor = null;
        if (mCursor != newCursor) {
            // Unregister the ContentObserver from old cursor.
            oldCursor = mCursor;
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
                observer.notifyDataSetChanged();
            } else {
                // Notifies the attached observers that the underlying data is no longer valid.
                mRowIDColumn = -1;
                observer.notifyDataSetInvalidated();
            }
        }

        return oldCursor;
    }

    /**
     * Class <tt>CursorAdapterImpl</tt> is an implementation of a {@link CursorAdapter}.
     */
    /* package */ static final class CursorAdapterImpl extends CursorAdapter {
        public CursorAdapterImpl(Cursor cursor, CursorObserver observer) {
            super(cursor, observer);
        }

        @Override
        protected void bindView(Cursor cursor, int position, View view) {
        }

        @Override
        protected View newView(Cursor cursor, int position, ViewGroup parent) {
            return null;
        }
    }
}
