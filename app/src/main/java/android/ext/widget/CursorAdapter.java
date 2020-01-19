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
import android.support.v7.widget.RecyclerView.ViewHolder;

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
        setCursor(cursor);
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
     */
    public final Cursor getItem(ViewHolder viewHolder) {
        final int position = viewHolder.getAdapterPosition();
        return (position != NO_POSITION ? getItem(position) : null);
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
     * @param path May be <tt>null</tt>. The path to match.
     * @see #unregisterReceiver(Context)
     * @see DatabaseReceiver
     */
    public final void registerReceiver(Context context, String scheme, String path) {
        DebugUtils.__checkUIThread("registerReceiver");
        if (mReceiver == null) {
            mReceiver = new CursorReceiver();
            mReceiver.register(context, scheme, path);
        }
    }

    /**
     * Unregister a receiver that has previously been registered with this adapter.
     * @param context The <tt>Context</tt>.
     * @see #registerReceiver(Context, String, String)
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
     * Called when this adapter receives a local broadcast.
     * @param context The <tt>Context</tt>.
     * @param intent The <tt>Intent</tt> being received.
     */
    protected void onContentChanged(Context context, Intent intent) {
    }

    /**
     * Class <tt>CursorReceiver</tt> is an implementation of a {@link DatabaseReceiver}.
     */
    /* package */ final class CursorReceiver extends DatabaseReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            onContentChanged(context, intent);
        }
    }
}
