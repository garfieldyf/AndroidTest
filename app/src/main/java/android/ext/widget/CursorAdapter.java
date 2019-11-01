package android.ext.widget;

import static android.support.v7.widget.RecyclerView.NO_ID;
import static android.support.v7.widget.RecyclerView.NO_POSITION;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.ext.database.DatabaseReceiver;
import android.ext.util.DebugUtils;
import android.ext.util.FileUtils;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.provider.BaseColumns;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.View;

/**
 * Abstract class CursorAdapter
 * @author Garfield
 */
public abstract class CursorAdapter<VH extends ViewHolder> extends BaseAdapter<VH> implements CursorObserverClient {
    /**
     * If set the adapter will register a content observer on the
     * cursor and will call {@link #onContentChanged(boolean, Uri)}
     * when a notification comes in.
     */
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x01;

    private Cursor mCursor;
    private int mRowIDColumn;
    private CursorReceiver mReceiver;
    private final CursorObserver mObserver;

    /**
     * Constructor
     * @param cursor The cursor from which to get the data. May be <tt>null</tt>.
     * @param flags Flags used to determine the behavior of this adapter. May be
     * <tt>0</tt> or any combination of FLAG_XXX constants.
     */
    public CursorAdapter(Cursor cursor, int flags) {
        mObserver = ((flags & FLAG_REGISTER_CONTENT_OBSERVER) != 0 ? new CursorObserver(this) : null);
        mRowIDColumn = -1;
        if (cursor != null) {
            setCursor(cursor);
        }
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
     * Equivalent to calling <tt>getItem(viewHolder.getAdapterPosition())</tt>.
     * @param viewHolder The {@link ViewHolder} to query its adapter position.
     * @return The <tt>Cursor</tt>, or <tt>null</tt> if there was not present
     * or can not move to the specified adapter position.
     * @see #getItem(int)
     * @see #getItem(View)
     */
    public final Cursor getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != NO_POSITION ? getItem(position) : null);
    }

    /**
     * Equivalent to calling <tt>getItem(recyclerView.getChildAdapterPosition(child))</tt>.
     * @param child The child of the <tt>RecyclerView</tt> to query for the
     * <tt>ViewHolder</tt>'s adapter position.
     * @return The <tt>Cursor</tt>, or <tt>null</tt> if there was not present or can not move
     * to the specified adapter position.
     * @see #getItem(int)
     * @see #getItem(ViewHolder)
     */
    public final Cursor getItem(View child) {
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        final int position = mRecyclerView.getChildAdapterPosition(child);
        return (position != NO_POSITION ? getItem(position) : null);
    }

    /**
     * Returns the cursor associated with this adapter. The returned
     * cursor is already moved to the specified <em>position</em>.
     * @param position The position of the cursor.
     * @return The <tt>Cursor</tt>, or <tt>null</tt> if there was not
     * present or can not move to the specified <em>position</em>.
     * @see #getItem(View)
     * @see #getItem(ViewHolder)
     */
    public Cursor getItem(int position) {
        return (mCursor != null && mCursor.moveToPosition(position) ? mCursor : null);
    }

    /**
     * Changes the underlying cursor to a new cursor.
     * If there is an existing cursor it will be closed.
     * @param cursor The new cursor to be used.
     * @see #swapCursor(Cursor)
     * @see #getCursor()
     */
    public void changeCursor(Cursor cursor) {
        FileUtils.close(swapCursor(cursor));
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
        Cursor oldCursor = null;
        if (mCursor != newCursor) {
            oldCursor = mCursor;
            setCursor(newCursor);
            postNotifyDataSetChanged();
        }

        return oldCursor;
    }

    /**
     * Equivalent to calling <tt>notifyItemChanged(id, null)</tt>.
     * @param id The stable ID of the item.
     * @see #notifyItemChanged(long, Object)
     */
    public final void notifyItemChanged(long id) {
        notifyItemChanged(id, null);
    }

    /**
     * Notify any registered observers that the item's stable ID equals the <em>id</em>
     * has changed with an optional payload object.
     * @param id The stable ID of the item.
     * @param payload Optional parameter, pass to {@link #notifyItemChanged(int, Object)}.
     * @see #notifyItemChanged(long)
     */
    public void notifyItemChanged(long id, Object payload) {
        DebugUtils.__checkError(mRowIDColumn == -1, "The cursor has no rowID column.");
        DebugUtils.__checkError(mRecyclerView == null, "This adapter not attached to RecyclerView.");
        final LayoutManager layoutManager = mRecyclerView.getLayoutManager();
        if (mCursor == null || id == NO_ID || !(layoutManager instanceof LinearLayoutManager)) {
            return;
        }

        final LinearLayoutManager layout = (LinearLayoutManager)layoutManager;
        int firstPos = layout.findFirstVisibleItemPosition();
        final int lastPos = layout.findLastVisibleItemPosition();
        if (firstPos == NO_POSITION || lastPos == NO_POSITION) {
            return;
        }

        for (; firstPos <= lastPos; ++firstPos) {
            if (mCursor.moveToPosition(firstPos) && mCursor.getLong(mRowIDColumn) == id) {
                postNotifyItemRangeChanged(firstPos, 1, payload);
                break;
            }
        }
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

    /**
     * Register a receiver for any local broadcasts that match the given <em>scheme</em>.
     * @param context The <tt>Context</tt>.
     * @param scheme The <tt>Intent</tt> data scheme to match. May be <tt>"databasename.tablename"</tt>
     * @see #unregisterReceiver(Context)
     * @see DatabaseReceiver
     */
    public final void registerReceiver(Context context, String scheme) {
        DebugUtils.__checkUIThread("registerReceiver");
        if (mReceiver == null) {
            mReceiver = new CursorReceiver();
            mReceiver.register(context, scheme, null);
        }
    }

    /**
     * Unregister a receiver that has previously been registered with this adapter.
     * @param context The <tt>Context</tt>.
     * @see #registerReceiver(Context, String)
     * @see DatabaseReceiver
     */
    public final void unregisterReceiver(Context context) {
        DebugUtils.__checkUIThread("unregisterReceiver");
        if (mReceiver != null) {
            mReceiver.unregister(context);
            mReceiver = null;
        }
    }

    @Override
    public int getItemCount() {
        return (mCursor != null ? mCursor.getCount() : 0);
    }

    @Override
    public long getItemId(int position) {
        return (mCursor != null && mRowIDColumn != -1 && mCursor.moveToPosition(position) ? mCursor.getLong(mRowIDColumn) : NO_ID);
    }

    private void setCursor(Cursor cursor) {
        // Unregister the ContentObserver from old cursor.
        unregisterContentObserver();
        mCursor = cursor;

        if (mCursor != null) {
            // Register the ContentObserver to new cursor.
            if (mObserver != null) {
                mObserver.register(mCursor);
            }

            // Get the rowID column index from the new cursor.
            mRowIDColumn = mCursor.getColumnIndex(BaseColumns._ID);
        } else {
            // The rowID column index is no longer valid.
            mRowIDColumn = -1;
        }
    }

    /**
     * Class <tt>CursorReceiver</tt> is an implementation of a {@link DatabaseReceiver}.
     */
    /* package */ final class CursorReceiver extends DatabaseReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onContentChanged(false, intent.getData());
        }
    }
}
