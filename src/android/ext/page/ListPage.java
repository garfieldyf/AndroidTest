package android.ext.page;

import java.util.List;
import android.ext.util.DebugUtils;

/**
 * Class <tt>ListPage</tt> is an implementation of a {@link Page}.
 * @author Garfield
 */
public final class ListPage<E> implements Page<E> {
    private final List<E> mData;

    /**
     * Constructor
     * @param data A {@link List} of this page data.
     * @see Pages#newPage(E[])
     * @see Pages#newPage(List)
     */
    public ListPage(List<E> data) {
        DebugUtils.__checkError(data == null || data.size() <= 0, "data == null || data.size() <= 0");
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public E getItem(int position) {
        return mData.get(position);
    }
}
