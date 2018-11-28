package android.ext.widget;

import android.database.Cursor;
import android.ext.util.FileUtils;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.ext.widget.Filters.CursorFilter;
import android.ext.widget.Filters.CursorFilterClient;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

/**
 * Abstract class ExpandableCursorTreeAdapter
 * @author Garfield
 */
public abstract class ExpandableCursorTreeAdapter extends ExpandableCursorAdapter implements Filterable, CursorFilterClient, CursorObserverClient {
    /**
     * If set the adapter will register a content observer on the group
     * cursor and will call {@link #onContentChanged(boolean, Uri)} when
     * a notification comes in.
     */
    public static final int FLAG_REGISTER_GROUP_CONTENT_OBSERVER = 0x02;

    private final CursorWrapper mGroup;
    private Filter mFilter;

    /**
     * Constructor
     * @param groupCursor The group cursor from which to get the data.
     * @param flags Flags used to determine the behavior of this
     * adapter. May be 0 or any combination of FLAG_XXX constants.
     */
    public ExpandableCursorTreeAdapter(Cursor groupCursor, int flags) {
        super(flags);
        mGroup = new CursorWrapper(groupCursor, (flags & FLAG_REGISTER_GROUP_CONTENT_OBSERVER) != 0 ? new CursorObserver(this) : null);
    }

    /**
     * Returns the group cursor.
     * @return The group <tt>Cursor</tt>, or <tt>null</tt>
     * if there was not present.
     * @see #swapGroupCursor(Cursor)
     * @see #changeCursor(Cursor)
     */
    public final Cursor getGroupCursor() {
        return mGroup.mCursor;
    }

    /**
     * Changes the group underlying cursor to a new cursor.
     * If there is an existing cursor it will be closed.
     * @param newCursor The new group cursor to be used.
     * @see #getGroupCursor()
     * @see #swapGroupCursor(Cursor)
     */
    @Override
    public void changeCursor(Cursor newCursor) {
        FileUtils.close(mGroup.swapCursor(newCursor, true));
    }

    /**
     * Swap the group in a new cursor, returning the old cursor. Unlike
     * {@link #changeCursor(Cursor)} the returned old cursor is
     * <b>not</b> closed.
     * @param newCursor The new group cursor to be used.
     * @return Returns the group previously set cursor, or <tt>null</tt> if
     * there was a not one. If the given new cursor is the same instance is
     * the previously set cursor, <tt>null</tt> is also returned.
     * @see #getGroupCursor()
     * @see #changeCursor(Cursor)
     */
    public Cursor swapGroupCursor(Cursor newCursor) {
        return mGroup.swapCursor(newCursor, true);
    }

    /**
     * Register an observer that is called when changes happen to the group content backing
     * this adapter. <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_GROUP_CONTENT_OBSERVER} then invoking this method has no effect.</p>
     * @see #unregisterGroupContentObserver()
     */
    public final void registerGroupContentObserver() {
        mGroup.registerContentObserver();
    }

    /**
     * Unregister an observer that has previously been registered with the group.
     * <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_GROUP_CONTENT_OBSERVER} then invoking this method
     * has no effect.</p>
     * @see #registerGroupContentObserver()
     */
    public final void unregisterGroupContentObserver() {
        mGroup.unregisterContentObserver();
    }

    @Override
    public int getGroupCount() {
        return mGroup.getCount();
    }

    @Override
    public Cursor getGroup(int groupPosition) {
        return mGroup.moveToPosition(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return mGroup.getId(groupPosition);
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View view, ViewGroup parent) {
        final Cursor cursor = mGroup.moveToPosition(groupPosition);
        if (cursor == null) {
            throw new IllegalStateException("Couldn't move group cursor to position : " + groupPosition);
        }

        if (view == null) {
            view = newGroupView(cursor, groupPosition, isExpanded, parent);
        }

        bindGroupView(cursor, groupPosition, view, isExpanded);
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
    public CharSequence convertToString(Cursor cursor) {
        return null;
    }

    @Override
    public Cursor onPerformFiltering(CharSequence constraint) {
        return mGroup.mCursor;
    }

    @Override
    public void onContentChanged(boolean selfChange, Uri uri) {
    }

    /**
     * Makes a new group view to hold the group data pointed to by cursor.
     * @param cursor The group cursor from which to get the data.
     * The cursor is already moved to the correct position.
     * @param groupPosition The position of the group.
     * @param isExpanded Whether the group is expanded.
     * @param parent The parent to which the new view is attached to.
     * @return The newly created view.
     * @see #bindGroupView(Cursor, int, View, boolean)
     */
    protected abstract View newGroupView(Cursor cursor, int groupPosition, boolean isExpanded, ViewGroup parent);

    /**
     * Binds an existing view to the group data pointed to by cursor.
     * @param cursor The group cursor from which to get the data.
     * The cursor is already moved to the correct position.
     * @param groupPosition The position of the group.
     * @param view Existing view, returned earlier by {@link #newGroupView}.
     * @param isExpanded Whether the group is expanded.
     * @see #newGroupView(Cursor, int, boolean, ViewGroup)
     */
    protected abstract void bindGroupView(Cursor cursor, int groupPosition, View view, boolean isExpanded);
}