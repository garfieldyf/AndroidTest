package android.ext.page;

import org.json.JSONArray;
import android.ext.util.DebugUtils;

/**
 * Class <tt>JSONPage</tt> is an implementation of a {@link Page}.
 * @author Garfield
 */
public final class JSONPage<E> implements Page<E> {
    private final JSONArray mData;

    /**
     * Constructor
     * @param data A {@link JSONArray} of this page data.
     * @see Pages#newPage(JSONArray)
     */
    public JSONPage(JSONArray data) {
        DebugUtils.__checkError(data == null || data.length() <= 0, "data == null || data.length() <= 0");
        mData = data;
    }

    @Override
    public int getCount() {
        return mData.length();
    }

    @Override
    @SuppressWarnings("unchecked")
    public E getItem(int position) {
        return (E)mData.opt(position);
    }
}
