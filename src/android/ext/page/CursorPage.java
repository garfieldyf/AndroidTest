package android.ext.page;

import java.io.Closeable;
import android.database.Cursor;
import android.ext.util.DebugUtils;

/**
 * Class <tt>CursorPage</tt> is an implementation of a {@link Page}.
 * @author Garfield
 */
public final class CursorPage implements Page<Cursor>, Closeable {
    /* package */ final Cursor mCursor;

    /**
     * Constructor
     * @param cursor A {@link Cursor} of this page data.
     * @see Pages#newPage(Cursor)
     */
    public CursorPage(Cursor cursor) {
        DebugUtils.__checkError(cursor == null || cursor.getCount() <= 0, "cursor == null || cursor.getCount() <= 0");
        mCursor = cursor;
    }

    @Override
    public void close() {
        mCursor.close();
    }

    @Override
    public int getCount() {
        return mCursor.getCount();
    }

    @Override
    public Cursor getItem(int position) {
        return (mCursor.moveToPosition(position) ? mCursor : null);
    }
}