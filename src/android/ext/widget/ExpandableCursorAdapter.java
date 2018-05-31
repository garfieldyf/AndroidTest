package android.ext.widget;

import android.database.Cursor;
import android.ext.util.FileUtils;
import android.ext.widget.CursorObserver.CursorObserverClient;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

/**
 * Abstract class ExpandableCursorAdapter
 * @author Garfield
 * @version 2.0
 */
public abstract class ExpandableCursorAdapter extends BaseExpandableListAdapter {
    /**
     * If set the adapter will register a content observer on the child
     * cursor and will call {@link #getChildrenCursor(int, Uri)} when a
     * notification comes in.
     */
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 0x01;

    /* package */ final int mFlags;
    private final SparseArray<CursorWrapper> mChildren;

    /**
     * Constructor
     * @param flags Flags used to determine the behavior of this
     * adapter. May be 0 or any combination of FLAG_XXX constants.
     */
    public ExpandableCursorAdapter(int flags) {
        mFlags = flags;
        mChildren = new SparseArray<CursorWrapper>();
    }

    /**
     * Returns the child cursor.
     * @param groupPosition The position of the group.
     * @return The child <tt>Cursor</tt>, or <tt>null</tt>
     * if there was not present.
     * @see #swapChildCursor(int, Cursor)
     * @see #changeChildCursor(int, Cursor)
     */
    public final Cursor getChildCursor(int groupPosition) {
        return getChildrenCursorWrapper(groupPosition).mCursor;
    }

    /**
     * Changes the child underlying cursor to a new cursor. If there is an
     * existing cursor it will be closed. This is useful when asynchronously
     * querying to prevent blocking the UI.
     * @param groupPosition The group whose child is being set via this cursor.
     * @param newCursor The new child cursor to be used.
     * @see #getChildCursor(int)
     * @see #swapChildCursor(int, Cursor)
     */
    public void changeChildCursor(int groupPosition, Cursor newCursor) {
        FileUtils.close(getChildrenCursorWrapper(groupPosition).swapCursor(newCursor, false));
    }

    /**
     * Swap the child in a new cursor, returning the old cursor. Unlike
     * {@link #changeChildCursor(int, Cursor)} the returned old cursor is
     * <b>not</b> closed.
     * @param groupPosition The group whose child is being swap new cursor.
     * @param newCursor The new child cursor to be used.
     * @return Returns the child previously set cursor, or <tt>null</tt> if
     * there was a not one. If the given new cursor is the same instance is
     * the previously set cursor, <tt>null</tt> is also returned.
     * @see #getChildCursor(int)
     * @see #changeChildCursor(int, Cursor)
     */
    public Cursor swapChildCursor(int groupPosition, Cursor newCursor) {
        return getChildrenCursorWrapper(groupPosition).swapCursor(newCursor, false);
    }

    /**
     * Register an observer that is called when changes happen to the child content backing
     * this adapter. <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER} then invoking this method has no effect.</p>
     * @param groupPosition The group whose child is being register.
     * @see #unregisterChildContentObserver(int)
     */
    public final void registerChildContentObserver(int groupPosition) {
        getChildrenCursorWrapper(groupPosition).registerContentObserver();
    }

    /**
     * Unregister an observer that has previously been registered with the child.
     * <p>Note: If the constructor's parameter <em>flags</em> not contains the
     * {@link #FLAG_REGISTER_CONTENT_OBSERVER} then invoking this method has no
     * effect.</p>
     * @param groupPosition The group whose child is being unregister.
     * @see #registerChildContentObserver(int)
     */
    public final void unregisterChildContentObserver(int groupPosition) {
        getChildrenCursorWrapper(groupPosition).unregisterContentObserver();
    }

    /**
     * Notifies a data set change, but with the option of closing any cached
     * children's cursors.
     * @param closeCursors Whether to close any cached children's cursors.
     */
    public void notifyDataSetChanged(boolean closeCursors) {
        if (closeCursors) {
            closeChildrenCursorWrapper();
        }

        super.notifyDataSetChanged();
    }

    /**
     * Returns the original group Id from combined Id.
     * @param combinedId The combined Id, returned
     * earlier by {@link #getCombinedGroupId(long)}.
     * @return The original group Id.
     * @see #getOriginalChildId(long)
     */
    public long getOriginalGroupId(long combinedId) {
        return ((combinedId >>> 32) & 0x7FFFFFFF);
    }

    /**
     * Returns the original child Id from combined Id.
     * @param combinedId The combined Id, returned
     * earlier by {@link #getCombinedChildId(long, long)}.
     * @return The original child Id, If this does not
     * contain a child Id, returns 0.
     * @see #getOriginalGroupId(long)
     */
    public long getOriginalChildId(long combinedId) {
        return (combinedId & 0x00000000FFFFFFFFL);
    }

    @Override
    public void notifyDataSetChanged() {
        notifyDataSetChanged(true);
    }

    @Override
    public void notifyDataSetInvalidated() {
        closeChildrenCursorWrapper();
        super.notifyDataSetInvalidated();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getChildrenCursorWrapper(groupPosition).getCount();
    }

    @Override
    public Cursor getChild(int groupPosition, int childPosition) {
        return getChildrenCursorWrapper(groupPosition).moveToPosition(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return getChildrenCursorWrapper(groupPosition).getId(childPosition);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
        final Cursor cursor = getChildrenCursorWrapper(groupPosition).moveToPosition(childPosition);
        if (cursor == null) {
            throw new IllegalStateException("Couldn't move child cursor to position : " + childPosition);
        }

        if (view == null) {
            view = newChildView(cursor, groupPosition, childPosition, isLastChild, parent);
        }

        bindChildView(cursor, groupPosition, childPosition, view, isLastChild);
        return view;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return -1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * Returns the cursor for the child at the given group. Subclasses must
     * implement this method to return the child data for a particular group.
     * <p>If you want to asynchronously query a provider to prevent blocking
     * the UI, it is possible to return <tt>null</tt> and at a later time call
     * {@link #changeChildCursor(int, Cursor)}.<p>
     * @param groupPosition The position to the group whose child cursor should
     * be returned.
     * @param uri The Uri of the changed content, or <tt>null</tt> if unknown.
     * @return The cursor for the child of a particular group, or <tt>null</tt>.
     */
    protected abstract Cursor getChildrenCursor(int groupPosition, Uri uri);

    /**
     * Makes a new child view to hold the data pointed to by cursor.
     * @param cursor The group cursor from which to get the data. The cursor
     * is already moved to the correct position.
     * @param groupPosition The position of the group.
     * @param childPosition The position of the child.
     * @param isLastChild Whether the child is the last child within its group.
     * @param parent The parent to which the new view is attached to.
     * @return the newly created view.
     * @see #bindChildView(Cursor, int, int, View, boolean)
     */
    protected abstract View newChildView(Cursor cursor, int groupPosition, int childPosition, boolean isLastChild, ViewGroup parent);

    /**
     * Binds an existing view to the child data pointed to by cursor.
     * @param cursor The group cursor from which to get the data. The cursor
     * is already moved to the correct position.
     * @param groupPosition The position of the group.
     * @param childPosition The position of the child.
     * @param view Existing view, returned earlier by {@link #newChildView}.
     * @param isLastChild Whether the child is the last child within its group.
     * @see #newChildView(Cursor, int, int, boolean, ViewGroup)
     */
    protected abstract void bindChildView(Cursor cursor, int groupPosition, int childPosition, View view, boolean isLastChild);

    private synchronized CursorWrapper getChildrenCursorWrapper(int groupPosition) {
        CursorWrapper cursorWrapper = mChildren.get(groupPosition, null);
        if (cursorWrapper == null) {
            cursorWrapper = new CursorWrapper(getChildrenCursor(groupPosition, null));
            mChildren.append(groupPosition, cursorWrapper);
        }

        return cursorWrapper;
    }

    private synchronized void closeChildrenCursorWrapper() {
        for (int i = mChildren.size() - 1; i >= 0; --i) {
            mChildren.valueAt(i).close();
        }

        mChildren.clear();
    }

    /* package */ synchronized final int findGroupPosition(CursorWrapper wrapper) {
        final int index = mChildren.indexOfValue(wrapper);
        return (index != -1 ? mChildren.keyAt(index) : -1);
    }

    /**
     * Nested class <tt>CursorWrapper</tt> is an implementation of a {@link CursorObserverClient}.
     */
    /* package */ final class CursorWrapper implements CursorObserverClient {
        /* package */ Cursor mCursor;
        private int mRowIDColumn = -1;
        private final CursorObserver mObserver;

        public CursorWrapper(Cursor cursor) {
            mCursor = cursor;
            mObserver = ((mFlags & FLAG_REGISTER_CONTENT_OBSERVER) != 0 ? new CursorObserver(this) : null);
            registerContentObserver();
        }

        public CursorWrapper(Cursor cursor, CursorObserver observer) {
            mCursor = cursor;
            mObserver = observer;
            registerContentObserver();
        }

        public int getCount() {
            return (mCursor != null ? mCursor.getCount() : 0);
        }

        public long getId(int position) {
            return (mCursor != null && mRowIDColumn != -1 && mCursor.moveToPosition(position) ? mCursor.getLong(mRowIDColumn) : -1);
        }

        public Cursor moveToPosition(int position) {
            return (mCursor != null && mCursor.moveToPosition(position) ? mCursor : null);
        }

        public Cursor swapCursor(Cursor newCursor, boolean closeCursors) {
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
                    notifyDataSetChanged(closeCursors);
                } else {
                    // Notifies the attached observers that the underlying data is no longer valid.
                    mRowIDColumn = -1;
                    notifyDataSetInvalidated();
                }
            }

            return oldCursor;
        }

        public void registerContentObserver() {
            if (mCursor != null && mObserver != null) {
                mCursor.registerContentObserver(mObserver);
            }
        }

        public void unregisterContentObserver() {
            if (mCursor != null && mObserver != null) {
                mCursor.unregisterContentObserver(mObserver);
            }
        }

        @Override
        public void onContentChanged(boolean selfChange, Uri uri) {
            final int groupPosition = findGroupPosition(this);
            if (groupPosition >= 0) {
                final Cursor cursor = getChildrenCursor(groupPosition, uri);
                if (cursor != null) {
                    FileUtils.close(swapCursor(cursor, false));
                }
            }
        }

        /* package */ final void close() {
            if (mCursor != null) {
                mCursor.close();
                mCursor = null;
            }
        }
    }
}